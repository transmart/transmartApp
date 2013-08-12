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
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 * 
 *
 ******************************************************************/


package com.recomdata.transmart.data.export

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import org.json.JSONArray
import org.json.JSONObject

import com.recomdata.transmart.validate.RequestValidator;

class ImperialHeatmapService {

	boolean transactional = true
	def geneExpressionDataService
	def grailsApplication
	def dataCountService
	
	def dataSource
	def config = ConfigurationHolder.config

	def getData(params) {
		def dataTypesMap = grailsApplication.config.com.recomdata.transmart.data.export.dataTypesMap

		//The result instance id's are stored queries which we can use to get information from the i2b2 schema.
		def rID1 = RequestValidator.nullCheck(params.result_instance_id1)
		def rID2 = RequestValidator.nullCheck(params.result_instance_id2)
		def rIDs = null
		if (rID1 && rID1?.trim() != '' && rID2 && rID2?.trim() != '') rIDs = rID1 + ',' + rID2
		else if (rID1 && rID1?.trim() != '') rIDs = rID1
		else if (rID2 && rID2?.trim() != '') rIDs = rID2

		def subsetLen = (rID1 && rID2) ? 2 : (rID1 || rID2) ? 1 : 0
		log.debug('rID1 :: ' + rID1 + ' :: rID2 :: ' + rID2)

		/**********************************************************************************
		 * wsc add for new gene exp data
		 */
		def wscFile1 = getMicroarray(rID1,1);
		def wscFile2 = getMicroarray(rID2,2);
		log.debug("wsc File1: " + wscFile1);
		log.debug("wsc File2: " + wscFile2);

		String rScriptDirectory = config.com.recomdata.transmart.data.export.rScriptDirectory;
		log.debug("wsc rScriptDirectory: " + rScriptDirectory);
		execute("/usr/bin/Rscript ${rScriptDirectory}/GeneExpression/Microarray.R " + wscFile1 + " " + wscFile2 + " &");

		def resultList = readFilteredResultFromFile(wscFile1);

		/************************************* wsc *********************************************/

		//Retrieve the counts for each subset. We get back a map that looks like ['RBM':2,'MRNA':30]
		def subset1CountMap = dataCountService.getDataCounts(rID1, rIDs)
		def subset2CountMap = dataCountService.getDataCounts(rID2, rIDs)
		log.debug('subset1CountMap :: ' + subset1CountMap + ' :: subset2CountMap :: ' + subset2CountMap)

		//This is the map we render to JSON.
		def finalMap = [:]

		//Add our counts to the map.
		finalMap['subset1'] = subset1CountMap
		finalMap['subset2'] = subset2CountMap
		//render '{"subset1": [{"PLINK": "102","RBM":"28"}],"subset2": [{"PLINK": "1","RBM":"2"}]}'
		JSONObject result = new JSONObject()
		result.put('noOfSubsets', subsetLen)

		JSONArray rows = new JSONArray();
		dataTypesMap.each { key, value ->
			def dataType = [:]
			def dataTypeHasCounts = false
			dataType['dataTypeId'] = key
			dataType['dataTypeName'] = value
			//TODO replace 2 with subsetLen
			for (i in 1..2) {
				JSONArray files = new JSONArray();

				if (key == 'CLINICAL') {
					files.put(createJSONFileObject('.TXT', 'Data', finalMap["subset${i}"][key], null, null))
				} else if (key == 'MRNA') {
					def countsMap = createCountsMap('.TXT', 'Processed Data', finalMap, key, i)
					dataTypeHasCounts=dataTypeHasCounts||countsMap.get('dataTypeHasCounts')
					files.put(countsMap)
					files.put(createJSONFileObject('.CEL', 'Raw Data', finalMap["subset${i}"][key+'_CEL'], null, null))
				} else if (key == 'SNP') {
					files.put(createJSONFileObject('.PED, .MAP & .CNV', 'Processed Data', finalMap["subset${i}"][key], null, null))
					files.put(createJSONFileObject('.CEL', 'Raw Data', finalMap["subset${i}"][key+'_CEL'], null, null))
				} else if (key == 'ADDITIONAL') {
					files.put(createJSONFileObject('', 'Additional Data', finalMap["subset${i}"][key], null, null))
				} else if (key == 'GSEA') {
					if (i==1) {
						def countsMap = createCountsMap('.GCT & .CLS', 'Processed Data (for both subsets)',finalMap, key, i)
						dataTypeHasCounts=dataTypeHasCounts||countsMap.get('dataTypeHasCounts')
						files.put(countsMap)
					}
				}
				if (!(['MRNA', 'GSEA'].contains(key)) && (null != finalMap["subset${i}"][key] && finalMap["subset${i}"][key] > 0))
					dataTypeHasCounts = true;

				dataType['metadataExists'] = true
				dataType['subsetId'+i] = "subset"+i
				dataType['subsetName'+i] = "Subset "+i
				dataType['subset'+i] = files
			}
			if (dataTypeHasCounts) rows.put(dataType)
		}

		result.put("success", true)
		result.put('imperialHeatmapData', rows)
		// wsc add microarray data
		result.put("microarray", resultList);
		return result
	}
	
	def Map createJSONFileObject(fileType, dataFormat, fileDataCount, gplId, gplTitle) {
		def file = [:]
		if(dataFormat!=null){
			file['dataFormat'] = dataFormat
		}
		if(fileType!=null){
			file['fileType'] = fileType
		}
		if(fileDataCount!=null){
			file['fileDataCount'] = fileDataCount
		}
		if(gplId!=null){
			file['gplId']=gplId
		}
		if(gplTitle!=null){
			file['gplTitle']=gplTitle
		}
		return file
	}

	def private getMicroarray(rID, subset){
		// wsc add the following for what...

		groovy.sql.Sql wscsql = new groovy.sql.Sql(dataSource);

		def wscdataList = [];

		try {

			log.debug("wsc db type: " + dataSource.toString());

			//String wsccommandString = "SELECT * FROM i2b2demodata.qt_patient_set_collection";

			String wsccommandString = "SELECT DISTINCT " +
					"sub2.s1pn, dma.gene_symbol, dma.probe_id, sub2.maraw " +
					"FROM deapp.de_mrna_annotation AS dma " +
					"INNER JOIN " +
					"( " +
					"       SELECT  " +
					"               sub1.pn AS s1pn, ma.raw_intensity AS maraw, ma.probeset_id AS mapid " +
					"       FROM " +
					"               deapp.de_subject_microarray_data AS ma " +
					"       INNER JOIN " +
					"       ( " +
					"       SELECT patient_num as pn " +
					"       FROM i2b2demodata.qt_patient_set_collection " +
					"       WHERE result_instance_id = ? " +
					"       ) AS sub1 " +
					"       ON (ma.patient_id = sub1.pn) " +
					") AS sub2 " +
					"ON (dma.probeset_id = sub2.mapid)";

			log.debug("wsc output his sql: " + wsccommandString);

			wscsql.eachRow(wsccommandString, [rID], { row->
				def wscdataMap = [:];
				wscdataMap["patientNum"] = row[0];
				wscdataMap["geneSymbol"] = row[1];
				wscdataMap["probesetId"] = row[2];
				wscdataMap["rawValue"] = row[3];
				wscdataList.add(wscdataMap);
			});
			// wsc writes down csv file

			String wscfilename = "/tmp/" + rID + "_" + System.currentTimeMillis() + ".csv";
			PrintWriter wsccsvout = null;
			try {
				// yannis surppose there is no missing value in gene data
				wsccsvout = new PrintWriter(new FileWriter(wscfilename));

				TreeMap<String, Map<String, BigDecimal>> wscgeneMap = new TreeMap<String, TreeMap<String, BigDecimal>>();

				for(Map wscEntry : wscdataList) {
					String wscgeneId = wscEntry.get("geneSymbol") + ":" + wscEntry.get("probesetId");
					if (wscgeneMap.containsKey(wscgeneId)) {
						TreeMap wscpidMap = (TreeMap)(wscgeneMap.get(wscgeneId));
						wscpidMap.put(wscEntry.get("patientNum"), new BigDecimal(wscEntry.get("rawValue")));
					} else {
						TreeMap<String, String> wscpidMap = new TreeMap<String, String>();
						wscpidMap.put(wscEntry.get("patientNum"), new BigDecimal(wscEntry.get("rawValue")));
						wscgeneMap.put(wscgeneId, wscpidMap);
					}
				}
				// wsc writes down input csv file for R q-val
				wsccsvout.print("GeneSymbol");
				for (Map.Entry<String, Map<String, String>> wscEntry : wscgeneMap.entrySet()) {
					// wsc write down column names
					for (String pid : wscEntry.value.keySet()) {
						wsccsvout.print(",S" + subset + "_" + pid);
					}
					wsccsvout.println();
					break;
				}
				for (Map.Entry<String, Map<String, String>> wscEntry : wscgeneMap.entrySet()) {
					// wsc gene name and raw values
					wsccsvout.print(wscEntry.key);
					for (String rawdata : wscEntry.value.values()) {
						wsccsvout.print("," + rawdata);
					}
					wsccsvout.println();
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				wsccsvout.close();
			}
			return wscfilename;
		} catch (Exception e) {
			log.debug("wsc new sql query exceptions!!!");
			log.debug("wsc msg: " + e.getMessage());
			log.debug("wsc stack: " + e.getStackTrace());

		} finally {
			wscsql.close();
		}
		//***********************************************************/

	}
	
	def private execute(cmd) {

		String result = "";
		InputStream input = null;
		Process ps = null;
		try {
			if (cmd == null || cmd.equals(""))
				return result;
			String[] fullcmd = ["/bin/sh", "-c", cmd];
			ps = Runtime.getRuntime().exec(fullcmd);
			input = ps.getInputStream();
			result = loadStream(input);
		} catch (Exception e) {
			log.error("Bash Execution Error " + cmd);
			log.error(e.getStackTrace());
		} finally {
			if (input != null) {
				input.close();
			}
			if (ps != null) {
				try {
					Thread.sleep(2300);

				} catch (InterruptedException e) {
					log.error(e.getStackTrace());
				}
				ps.destroy();
			}
		}

		return result;

	}
	def private loadStream(InputStream input) throws IOException {
		int ptr = 0;
		input = new BufferedInputStream(input);
		StringBuffer buffer = new StringBuffer();
		while ((ptr = input.read()) != -1) {
			buffer.append((char) ptr);
		}
		return buffer.toString();
	}
	def private readFilteredResultFromFile(File1) {
		BufferedReader fileIn;
		StringTokenizer stin;
		String line;
		List<String> row = null;
		List<List> resultList = new ArrayList<ArrayList>();
		try {
			fileIn = new BufferedReader(new FileReader(File1));
			
			while ((line = fileIn.readLine()) != null) {
				stin = new StringTokenizer(line, ",");
				// wsc skip the row name
				stin.nextToken();
				row = new ArrayList<String>();
				while (stin.hasMoreTokens()) {
					String str = stin.nextToken();
					str = str.replace("\"", "");
					str = str.replace("\\", "");
					row.add(str);
				}
				resultList.add(row);
			}
			
		} catch (FileNotFoundException e) {
			log.error(e.getStackTrace());
		} catch (IOException e) {
			log.error(e.getStackTrace());
		} finally {
			fileIn.close();
		}
		
		File fileTobeDel = null;
		try {
			// delete temp file
			fileTobeDel = new File(File1);
			fileTobeDel.delete();
		} catch (Exception e) {
			log.error(e.getStackTrace());
		}
		
		return resultList;
	}
	
	def createCountsMap(fileType, dataFormat, finalMap, key, subsetIdx){
		def dataTypeHasCounts = false
		def countsMap = createJSONFileObject(fileType, dataFormat, null, null, null)
		def platforms = new JSONArray()
		finalMap["subset${subsetIdx}"][key].each {gplId, count->
			if(count>0){
				platforms.put(createJSONFileObject(null, null,
						count, gplId, geneExpressionDataService.getGplTitle(gplId)))
			}
			dataTypeHasCounts = (dataTypeHasCounts||(count>0))
		}
		countsMap.put('platforms',platforms)
		countsMap.put('dataTypeHasCounts', dataTypeHasCounts)
		return countsMap
	}
}
