import java.math.BigDecimal;
import java.math.MathContext

import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter

import com.recomdata.upload.DataUploadResult;

/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/

public class DataUploadService{
	def dataSource

	def verifyFields(providedFields, uploadType) {
		
		def requiredFields = RequiredUploadField.findAllByType(uploadType)*.field
		def missingFields = []
		for (field in requiredFields) {
			def found = false
			for (providedField in providedFields) {
				if (providedField.trim().toLowerCase().equals(field.trim().toLowerCase())) {
					found = true
					break
				}
				//Special case for p-value - if we have log p-value, count this as present as well
				if(providedField.trim().toLowerCase().equals("log_p_value") &&
					field.trim().toLowerCase().equals("p_value")) {
					found = true
					break
				}
			}
			if (!found) {
				missingFields.add(field)
			}
		}
		def success = (missingFields.size() == 0)
		def result = new DataUploadResult(success: success, requiredFields: requiredFields, providedFields: providedFields, missingFields: missingFields, error: "Required fields were missing from the uploaded file.")
		return (result);
	}
	def BigDecimal log10(BigDecimal b, int dp)
	{
		final int NUM_OF_DIGITS = dp+2; // need to add one to get the right number of dp
										//  and then add one again to get the next number
										//  so I can round it correctly.

		MathContext mc = new MathContext(NUM_OF_DIGITS, RoundingMode.HALF_EVEN);

		//special conditions:
		// log(-x) -> exception
		// log(1) == 0 exactly;
		// log of a number lessthan one = -log(1/x)
		if(b.signum() <= 0)
			throw new ArithmeticException("log of a negative number! (or zero)");
		else if(b.compareTo(BigDecimal.ONE) == 0)
			return BigDecimal.ZERO;
		else if(b.compareTo(BigDecimal.ONE) < 0)
			return (log10((BigDecimal.ONE).divide(b,mc),dp)).negate();

		StringBuffer sb = new StringBuffer();
		//number of digits on the left of the decimal point
		int leftDigits = b.precision() - b.scale();

		//so, the first digits of the log10 are:
		sb.append(leftDigits - 1).append(".");

		//this is the algorithm outlined in the webpage
		int n = 0;
		while(n < NUM_OF_DIGITS)
		{
			b = (b.movePointLeft(leftDigits - 1)).pow(10, mc);
			leftDigits = b.precision() - b.scale();
			sb.append(leftDigits - 1);
			n++;
		}

		BigDecimal ans = new BigDecimal(sb.toString());

		//Round the number to the correct number of decimal places.
		ans = ans.round(new MathContext(ans.precision() - ans.scale() + dp, RoundingMode.HALF_EVEN));
		return ans;
	}
	def getAnalysisData(analysisIds, ranges, Long limit, Long offset, Double cutoff, String sortField, String order, String search, String type, geneNames, transcriptGeneNames, doCount) {

		def con, stmt, rs = null;
		con = dataSource.getConnection()
		StringBuilder queryCriteria = new StringBuilder();
		def analysisQuery
		def countQuery
		StringBuilder regionList = new StringBuilder();
		def analysisQCriteria = new StringBuilder();
		def analysisNameQuery = analysisNameSqlQuery;
		def hg19only = false;

		def analysisNameMap =[:]

		if (ConfigurationHolder.config.com.recomdata.gwas.usehg19table) {
			if(!ranges){
				hg19only = true;
			}else {
				hg19only = true; // default to true
				for(range in ranges){
					if(range.ver!='19'){
						hg19only = false;
						break;
					}
				}
			}
		}

		if (type.equals("gwas")) {
			analysisQuery = gwasSqlQuery
			countQuery = gwasSqlCountQuery
			if(hg19only){ // for hg19, special query
				analysisQuery = gwasHg19SqlQuery;
				countQuery = gwasHg19SqlCountQuery
			}
		}
		else if (type.equals("eqtl")) {
			analysisQuery = eqtlSqlQuery
			countQuery = eqtlSqlCountQuery
			if(hg19only){
				analysisQuery = eqtlHg19SqlQuery
				countQuery = eqtlHg19SqlCountQuery
			}
		}
		else {
			throw new Exception("Unrecognized data type")
		}

		def rangesDone = 0;

		//if (!ranges) {
		//If no ranges, force HG19
		//	regionList.append("hg_version='19'") -- we have a special sql for hg19
		//}
		//else {
		if(ranges!=null){
			for (range in ranges) {
				if (rangesDone != 0) {
					regionList.append(" OR ")
				}
				//Chromosome
				if (range.chromosome != null) {
					if (range.low == 0 && range.high == 0) {
						regionList.append("(info.chrom = '${range.chromosome}' ")
					}
					else {
						regionList.append("(info.pos >= ${range.low} AND info.pos <= ${range.high} AND info.chrom = '${range.chromosome}' ")
					}

					if(hg19only== false) {
						regionList.append("  AND info.hg_version = '${range.ver}' ")
					}
					regionList.append(")");
				}
				//Gene
				else {
					regionList.append("(info.pos >= ${range.low} AND info.pos <= ${range.high} ")
					if(hg19only== false) {
						regionList.append("  AND info.hg_version = '${range.ver}' ")
					}
					regionList.append(")")
				}
				rangesDone++
			}
		}

		//Add analysis IDs
		if (analysisIds) {
			analysisQCriteria.append(" AND data.BIO_ASSAY_ANALYSIS_ID IN (" + analysisIds[0]);
			for (int i = 1; i < analysisIds.size(); i++) {
				analysisQCriteria.append(", " + analysisIds[i]);
			}
			analysisQCriteria.append(") ")
			queryCriteria.append(analysisQCriteria.toString())

			//Originally we only selected the analysis name if there was a need to (more than one analysis) - but this query is much faster
			analysisQuery = analysisQuery.replace("_analysisSelect_", "DATA.bio_assay_analysis_id AS analysis_id, ")
			analysisQuery = analysisQuery.replace("_analysisJoin_", "");
		}

		//Add gene names
		if (geneNames) {
			// quick fix for hg19 only
			if(hg19only){
				queryCriteria.append(" AND info.rsgene IN (")
			}else{
				queryCriteria.append(" AND info.gene_name IN (");
			}
			queryCriteria.append( "'" + geneNames[0] + "'");
			for (int i = 1; i < geneNames.size(); i++) {
				queryCriteria.append(", " + "'" + geneNames[i] + "'");
			}
			queryCriteria.append(") ")
		}

		else if (type.equals("eqtl") && transcriptGeneNames) {
			queryCriteria.append(" AND data.gene IN (")
			queryCriteria.append( "'" + transcriptGeneNames[0] + "'");
			for (int i = 1; i < transcriptGeneNames.size(); i++) {
				queryCriteria.append(", " + "'" + transcriptGeneNames[i] + "'");
			}
			queryCriteria.append(") ")
		}


		if (cutoff) {
			queryCriteria.append(" AND p_value <= ?");
		}
		if (search) {
			queryCriteria.append(" AND (data.rs_id LIKE '%${search}%'")
			queryCriteria.append(" OR data.ext_data LIKE '%${search}%'")
			if(hg19only){
				queryCriteria.append(" OR info.rsgene LIKE '%${search}%'")
			}else{
				queryCriteria.append(" OR info.gene_name LIKE '%${search}%'");
			}
			queryCriteria.append(" OR info.pos LIKE '%${search}%'")
			queryCriteria.append(" OR info.chrom LIKE '%${search}%'")
			if (type.equals("eqtl")) {
				queryCriteria.append(" OR data.gene LIKE '%${search}%'")
			}
			queryCriteria.append(") ")
		}

		// handle null regionlist issue
		// If no regions, default to hg19. If hg19only, we don't need to check this.
		if(regionList.length()==0){
			if (hg19only) {
				regionList.append("1=1")
			}
			else {
				regionList.append("info.hg_version = '19'")
			}
		}

		analysisQuery = analysisQuery.replace("_regionlist_", regionList.toString())

		// this is really a hack
		def sortOrder = sortField?.trim();
		//println(sortField)
		if(hg19only){
			sortOrder = sortOrder.replaceAll("info.gene_name", "info.rsgene");

		}
		//println("after:"+sortOrder)
		analysisQuery = analysisQuery.replace("_orderclause_", sortOrder + " " + order)
		countQuery = countQuery.replace("_regionlist_", regionList.toString())

		// analysis name query


		try {
			def nameQuery = analysisNameQuery + analysisQCriteria.toString();
			log.debug(nameQuery)
			stmt = con.prepareStatement(nameQuery)

			rs = stmt.executeQuery();
			while (rs.next()) {
				analysisNameMap.put(rs.getLong("id"), rs.getString("name"));
			}
		}catch(Exception e){
			log.error(e.getMessage(),e)
			throw e;
		}
		finally {
			rs?.close();
			stmt?.close();
			con?.close();
		}

		//println(analysisNameMap)
		// data query
		def finalQuery = analysisQuery + queryCriteria.toString() + "\n) a";
		if (limit > 0) {
			finalQuery += " where a.row_nbr between ${offset+1} and ${offset+limit}";
		}
		stmt = con.prepareStatement(finalQuery);

		//stmt.setString(1, sortField)
		if (cutoff) {
			stmt.setDouble(1, cutoff);
		}

		log.debug("Executing: " + finalQuery)


		def results = []
		try{
			rs = stmt.executeQuery();
			while(rs.next()){
				if ((type.equals("gwas"))) {
					def logp=rs.getDouble("logpvalue");
					if (logp == Double.POSITIVE_INFINITY )
					{
						logp=0 - log10(new BigDecimal(rs.getString("p_value_char")),10);
							
					}
					results.push([rs.getString("rsid"), rs.getDouble("pvalue"),logp, rs.getString("extdata"),analysisNameMap.get( rs.getLong("analysis_id")), rs.getString("rsgene"), rs.getString("chrom"), rs.getLong("pos"), rs.getString("intronexon"), rs.getString("recombinationrate"), rs.getString("regulome")]);
				}
				else {
					results.push([rs.getString("rsid"), rs.getDouble("pvalue"), rs.getDouble("logpvalue"), rs.getString("extdata"), analysisNameMap.get(rs.getLong("analysis_id")), rs.getString("rsgene"), rs.getString("chrom"), rs.getLong("pos"), rs.getString("intronexon"), rs.getString("recombinationrate"), rs.getString("regulome"), rs.getString("gene")]);
				}
			}
		}
		catch(Exception e){
			log.error(e.getMessage(), e);
		}
		finally{
			rs?.close();
			stmt?.close();
		}

		//Count - skip if we're not to do this (loading results from cache)
		def total = 0;
		if (doCount) {
			try {
				def finalCountQuery = countQuery + queryCriteria.toString();
				stmt = con.prepareStatement(finalCountQuery)
				if (cutoff) {
					stmt.setDouble(1, cutoff);
				}

				log.debug("Executing count query: " + finalQuery)

				rs = stmt.executeQuery();
				if (rs.next()) {
					total = rs.getLong("TOTAL")
				}
			}
			catch (Exception e) {
				log.error(e, e.getMessage())
				throw e;
			}
			finally {
				rs?.close();
				stmt?.close();
				con?.close();
			}
		}

		return [results: results, total: total];
	}

	def quickQueryGwas = """
	
		SELECT analysis, chrom, pos, rsgene, rsid, pvalue, logpvalue, extdata, intronexon, recombinationrate, regulome FROM biomart.BIO_ASY_ANALYSIS_GWAS_TOP50
		WHERE analysis = ?
		ORDER BY pvalue
	
	"""
	// changed ORDER BY rnum by pvalue
	def quickQueryEqtl = """
	
		SELECT analysis, chrom, pos, rsgene, rsid, pvalue, logpvalue, extdata, intronexon, recombinationrate, regulome, gene FROM biomart.BIO_ASY_ANALYSIS_EQTL_TOP50
		WHERE analysis = ?
		ORDER BY pvalue
	
	"""
	def quickQueryGwasIdx = """
	
		select FIELD_NAME, FIELD_IDX from BIOMART.bio_asy_analysis_data_idx where ext_type= 'GWAS'
	
	"""
	def quickQueryEqtlIdx = """
	
		select FIELD_NAME, FIELD_IDX from BIOMART.bio_asy_analysis_data_idx where ext_type= 'EQTL'
	
	"""
	def quickQueryMetabolicIdx = """
	
		select FIELD_NAME, FIELD_IDX from BIOMART.bio_asy_analysis_data_idx where ext_type= 'Metabolic GWAS'
	
	"""
	def getQuickAnalysisDataByName(analysisName, type) {

		def con, stmt, rs = null;
		con = dataSource.getConnection()
		StringBuilder queryCriteria = new StringBuilder();
		def quickQuery

		if (type.equals("eqtl")) {
			quickQuery = quickQueryEqtl
		}
		else {
			quickQuery = quickQueryGwas
		}

		def results = []
		try {
			stmt = con.prepareStatement(quickQuery)
			stmt.setString(1, analysisName);

			rs = stmt.executeQuery();
			if (type.equals("eqtl")) {
				while(rs.next()){
					results.push([rs.getString("rsid"), rs.getDouble("pvalue"), rs.getDouble("logpvalue"), rs.getString("extdata"), rs.getString("analysis"), rs.getString("rsgene"), rs.getString("chrom"), rs.getLong("pos"), rs.getString("intronexon"), rs.getString("recombinationrate"), rs.getString("regulome"), rs.getString("gene")]);
				}
			}
			else {
				while(rs.next()){
					results.push([rs.getString("rsid"), rs.getDouble("pvalue"), rs.getDouble("logpvalue"), rs.getString("extdata"), rs.getString("analysis"), rs.getString("rsgene"), rs.getString("chrom"), rs.getLong("pos"), rs.getString("intronexon"), rs.getString("recombinationrate"), rs.getString("regulome")]);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs?.close();
			stmt?.close();
			con?.close();
		}

		println("Returning " + results.size())
		return [results: results]

	}
	def getColumnIndex(uploadType)
	{
		def con, stmt, rs = null;
		HashMap columnIdx=new HashMap()
		con = dataSource.getConnection()
		
		StringBuilder queryCriteria = new StringBuilder();
		def quickQuery

		if (uploadType.equals("EQTL")) {
			quickQuery = quickQueryEqtlIdx
		} else if (uploadType.equals("Metabolic GWAS")) {
			quickQuery = quickQueryMetabolicIdx
		} else {
			quickQuery = quickQueryGwasIdx
		}

		def results = []
		try {
			stmt = con.prepareStatement(quickQuery)

			rs = stmt.executeQuery();
				while(rs.next()){
					columnIdx.putAt(rs.getString("FIELD_NAME"), rs.getLong("FIELD_IDX"))
				}
			}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs?.close();
			stmt?.close();
			con?.close();
		}

		println("Returning " + results.size())
		return columnIdx
	}

	def writeFile(location, file, upload) throws Exception {
		CSVReader csvRead=null;
		CSVWriter csv = null;
		try {
		csvRead = new CSVReader(new InputStreamReader(file.getInputStream()), '\t'.charAt(0), CSVWriter.NO_QUOTE_CHARACTER);
		
		String[] header = csvRead.readNext();
		log.debug("Read file");
		//Verify fields and return immediately if we don't have a required one
		def result = verifyFields(header, upload.dataType)
		if (!result.success) {
			return result;
		}
		log.debug("Fields verified");
		def headerList = header.toList();
		//contains index from the database of field name and index
		//<String, long>
		HashMap columnOrder=getColumnIndex(upload.dataType)
		
//Currently ignoring: HETISQ
//  HETPVAL
		def pValueIndex = -1;
		def logpValueIndex = -1;
		def rsIdIndex=-1;
		def numberOfColumns = headerList.size();
		//contains index from the file to the index defined in the database.
		//<Long, int>
		//based on header field build this map, keep the field index and header Index.
		HashMap fileColumnIdx=new HashMap()
		
		for (int i = 0; i < headerList.size(); i++) {
			def column = headerList[i];
			if (column.trim().toLowerCase().equals("p_value")) {
				pValueIndex = i;
			}
			else if (column.trim().toLowerCase().equals("log_p_value")) {
				logpValueIndex = i;
			}
			else if(column.trim().toLowerCase().equals("rs_id")){
				rsIdIndex=i;
			}
			else
			{
				def idx = columnOrder.get(headerList[i].toUpperCase())
				if (idx==null)
				System.out.println(headerList[i].toUpperCase()+" at index "+ i)
				else
				fileColumnIdx.put (idx,i);
			}
		}

		//If we don't have p-value or log p-value, add this column at the end
		if (pValueIndex < 0) {
			pValueIndex = headerList.size();
			headerList[headerList.size()] = "p_value";
		}
		else if (logpValueIndex < 0) {
			logpValueIndex = headerList.size();
			headerList[headerList.size()] = "log_p_value";
		}
		log.info("RS at "+rsIdIndex+" pvalue "+pValueIndex+" -logPvalue "+ logpValueIndex)
		fileColumnIdx.put ((Long)0,(int)rsIdIndex)
		def pvalCol=(Long)columnOrder.size()+1
		fileColumnIdx.put (pvalCol,(int)pValueIndex)
		def pvalLog10Col=(Long)(columnOrder.size()+2)
		fileColumnIdx.put (pvalLog10Col,(int)logpValueIndex);



		/* Columns are sorted - now start writing the file */

	

			csv = new CSVWriter(new FileWriter(new File(location)), '\t'.charAt(0), CSVWriter.NO_QUOTE_CHARACTER) //How to specify character in Grails...?!
			String [] headerRow=new String[fileColumnIdx.size()];
			for (int rowIdx=0; rowIdx < fileColumnIdx.size();rowIdx++)
			{
				//System.out.println(rowIdx)
				def index=fileColumnIdx.get((Long)rowIdx)
				if (index==null)
				log.info("file column Index is null for "+ rowIdx +"-"+ fileColumnIdx.toString())
				else if (headerList[index]==null)
				log.info("header list is null for "+index+" - "+headerList.toString());
				else
				headerRow[rowIdx]=headerList[fileColumnIdx.get((Long)rowIdx)]
				
			}
			//csv.writeNext(headerList as String[])
			csv.writeNext(headerRow);

			//For each line, check the value and p-value - if we have one but not the other, calculate and fill it
			String[] nextLine
			def pflag=false;
			String linenos="";
			int lineno=1;
			while ((nextLine = csvRead.readNext()) != null) {
				String [] curRow=new String[fileColumnIdx.size()];
				def columns = nextLine.toList()
				def currentpValue = columns[pValueIndex]
				def currentlogpValue = columns[logpValueIndex]
				lineno++;
				int flag=1;
				if (!currentpValue && !currentlogpValue) {
					linenos += columns[rsIdIndex]+",";
					pflag=true;
					flag=0;
					//  throw new Exception("No p_value or log_p_value was provided for a row."+ linenos);

				}
				else if (!currentpValue) {
					columns[pValueIndex] = 0 - Math.power(10.0, Double.parseDouble(currentlogpValue))
				}
				else if (!currentlogpValue) {
					def logp=Math.log10(Double.parseDouble(currentpValue));
					if (logp == Double.POSITIVE_INFINITY )
					{
						logp=0 - log10(new BigDecimal(currentpValue),10);

					}
					columns[logpValueIndex] = 0 - logp
				}

				//This row is now complete - write it!
				if(flag==1){
					// csv.writeNext(columns as String[])
					for (int rowIdx=0; rowIdx<fileColumnIdx.size();rowIdx++)
					{
						System.out.println(rowIdx)
						def index=fileColumnIdx.get((Long)rowIdx)
						if (index==null)
						log.info("file column Index is null for "+ rowIdx +"-"+ fileColumnIdx.toString())
						else
						curRow[rowIdx]=columns[index]
					}
					//csv.writeNext(headerList as String[])
					csv.writeNext(curRow);
				}
			}
			if(pflag){
				//result.success=false;
				//result.error="No p_value or log_p_value was provided for snps "+linenos;
				throw new Exception ("No p_value or log_p_value was provided for SNPs "+linenos);
			}

			return result;
		}
		catch (Exception e) {
			log.error(e.printStackTrace());
			throw e;
		}
		//			upload.status = "ERROR"
		//			upload.save(flush: true)
		//			render(view: "complete", model: [result: new DataUploadResult(success:false, error: "Could not write file: " + e.getMessage()), uploadDataInstance: upload]);
		//			return;
		//		}
		finally {
			if (csvRead != null) {
				csvRead.close();
			}
			if (csv != null) {
				csv.flush()
				csv.close()
			}
		}
	}
	
	def runStaging(etlId) throws Exception {
		
			def etlPath = ConfigurationHolder.config.com.recomdata.dataUpload.etl.dir
			def stageScript = ConfigurationHolder.config.com.recomdata.dataUpload.stageScript
			ProcessBuilder pb = new ProcessBuilder(etlPath + stageScript, String.valueOf(etlId));
			pb.directory(new File(new File(etlPath).getCanonicalPath()))
			
			pb.start()
		
	}
}

