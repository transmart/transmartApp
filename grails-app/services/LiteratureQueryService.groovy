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
  

/**
 * $Id: LiteratureQueryService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */

import org.transmart.GlobalFilter;
import org.transmart.LiteratureFilter;
import org.transmart.SearchFilter;
import org.transmart.biomart.BioDataExternalCode;

import org.transmart.biomart.BioMarker
import org.transmart.biomart.Literature
import org.transmart.biomart.LiteratureAlterationData
import org.transmart.biomart.LiteratureInhibitorData
import org.transmart.biomart.LiteratureInteractionData
import org.transmart.biomart.LiteratureProteinEffectData
import org.transmart.biomart.LiteratureSummaryData
import org.transmart.biomart.LiteratureReferenceData
import org.transmart.biomart.LiteratureModelData
import org.transmart.biomart.LiteratureAssocMoleculeDetailsData
import com.recomdata.search.query.LiteratureDataQuery

class LiteratureQueryService {

	def globalFilterService
	
	/** 
	 * Executes query to get record count for data in curated literature from Jubilant
	 * 
	 * @param table - the table to query
	 * @param namedParams - the named parameters for the query
	 * @param gfilter - the global search filter
	 * @param query - the constructed query
	 * 
	 * @return the count of records
	 */
	def executeLitQueryCount(String table, LinkedHashMap namedParams, 
	        GlobalFilter gfilter, LiteratureDataQuery query) {
		if(gfilter == null || gfilter.isTextOnly()){
			return 0
		}
		query.addSelect("count(distinct data.id)")
		query.addTable(table)
		if (namedParams.containsKey("dd2ID"))	{
		    query.addTable("JOIN data.diseases data_dis2")				
		}		
		query.createGlobalFilterCriteria(gfilter); 
		return Literature.executeQuery(query.generateSQL(), namedParams)[0]		
	}
	
	/** 
	 * Executes query to get the data from curated literature from Jubilant
	 * 
	 * @param table - the table to query
	 * @param namedParams - the named parameters for the query
	 * @param sfilter - the search filter
	 * @param params - the paging parameters
	 * @param query - the constructed query
	 * 
	 * @return the results
	 */
	def executeLitQueryData(String table, LinkedHashMap namedParams, GlobalFilter gfilter, params, LiteratureDataQuery query) {
		if(gfilter == null || gfilter.isTextOnly()){
			return []
		}
		
		if (params != null)	{
		    params = globalFilterService.createPagingParamMap(params)
		}
		
		query.addSelect("data")
		query.addTable(table + " JOIN fetch data.reference")
		if (namedParams.containsKey("dd2ID"))	{
		    query.addTable("JOIN data.diseases data_dis2")				
		}	
		query.createGlobalFilterCriteria(gfilter);
		
		def results = []
		if (params != null)	{
		    results = Literature.executeQuery(query.generateSQL(), namedParams, params)
		} else	{
		    results = Literature.executeQuery(query.generateSQL(), namedParams)
		}
		return results
	}
	
	/** 
	 * Executes query to get record count for summary data in curated literature from Jubilant
	 * 
	 * @param table - the table to query
	 * @param namedParams - the named parameters for the query
	 * @param sfilter - the search filter
	 * @param query - the constructed query
	 * 
	 * @return the count of records
	 */
	def executeLitSumQueryCount(table, LinkedHashMap namedParams, sfilter,  LiteratureDataQuery query){	    
		GlobalFilter gfilter = sfilter.globalFilter
		LiteratureFilter litFilter = sfilter.litFilter
		if(gfilter == null || gfilter.isTextOnly()){
			return 0
		}
		
		query.addSelect("count(distinct sumdata.id)")
		query.addTable(table)
		query.addTable("org.transmart.biomart.LiteratureSummaryData sumdata")
		query.addCondition("sumdata.target=data.reference.component")
		query.addCondition("sumdata.diseaseSite=data.reference.diseaseSite")
		if (litFilter.hasAlterationType()) {
			def types = litFilter.getSelectedAlterationTypes()
			if (types.size() > 0) {
			    query.addCondition("data.alterationType in (:alterationTypes)")
			    namedParams["alterationTypes"] = types
			} else {
			    query.addCondition("data.alterationType is null") 
			}
		}
		if (namedParams.containsKey("dd2ID"))	{
		    query.addTable("JOIN data.diseases data_dis2")				
		}	
		query.createGlobalFilterCriteria(gfilter);
		return Literature.executeQuery(query.generateSQL(), namedParams)[0]
	}
	
	/** 
	 * Executes query to get the summary data from curated literature from Jubilant
	 * 
	 * @param table - the table to query
	 * @param namedParams - the named parameters for the query
	 * @param sfilter - the search filter
	 * @param params - the paging parameters
	 * @param query - the constructed query
	 * 
	 * @return the summary results
	 */
	def executeLitSumQueryData(table, LinkedHashMap namedParams, sfilter, params, LiteratureDataQuery query) {
		GlobalFilter gfilter = sfilter.globalFilter
		LiteratureFilter litFilter = sfilter.litFilter
		if(gfilter == null || gfilter.isTextOnly()){
			return []
		}
		
		def sort = params?.sort != null ? params.sort : 'dataType'
		def dir = params?.dir != null ? params.dir : 'ASC'
		params = globalFilterService.createPagingParamMap(params)
				
		query.setDistinct = true
        query.addSelect("sumdata")
		query.addTable(table)
		query.addTable("org.transmart.biomart.LiteratureSummaryData sumdata")
		query.addCondition("sumdata.target=data.reference.component")
		query.addCondition("sumdata.diseaseSite=data.reference.diseaseSite")
		if (litFilter.hasAlterationType()) {		    
			def types = litFilter.getSelectedAlterationTypes()
			if (types.size() > 0) {
			    query.addCondition("data.alterationType in (:alterationTypes)")
			    namedParams["alterationTypes"] = types
			} else {
			    query.addCondition("data.alterationType is null") 
			}
		}
		query.addOrderBy("sumdata." + sort + " " + dir)
		if (namedParams.containsKey("dd2ID"))	{
		    query.addTable("JOIN data.diseases data_dis2")				
		}	
		query.createGlobalFilterCriteria(gfilter);
		return Literature.executeQuery(query.generateSQL(), namedParams, params)
	}

	/*************************************************************************
	 * Julilant Oncology Alteration methods
	 *************************************************************************/
	def litJubOncAltCount(SearchFilter sfilter) {
	    def query = new LiteratureDataQuery(mainTableAlias:"data")    
		return executeLitQueryCount("LiteratureAlterationData data",
		        litAltConditions(sfilter.litFilter, "JUBILANT_ONCOLOGY_ALTERATION", query),
				sfilter.globalFilter, query)
	}

	def litJubOncAltData(SearchFilter sfilter, params) {
	    def query = new LiteratureDataQuery( mainTableAlias:"data")
		return executeLitQueryData("LiteratureAlterationData data",
				litAltConditions(sfilter.litFilter, "JUBILANT_ONCOLOGY_ALTERATION", query),
				sfilter.globalFilter, params, query)
	}

	def litJubAsthmaAltCount(SearchFilter sfilter) {
	    def query = new LiteratureDataQuery(mainTableAlias:"data")
		return executeLitQueryCount("LiteratureAlterationData data",
				litAltConditions(sfilter.litFilter, "JUBILANT_ASTHMA_ALTERATION", query),
				sfilter.globalFilter, query)
	}

	def litJubAsthmaAltData(SearchFilter sfilter, params) {
	    def query = new LiteratureDataQuery( mainTableAlias:"data")
		return executeLitQueryData("LiteratureAlterationData data",
				litAltConditions(sfilter.litFilter, "JUBILANT_ASTHMA_ALTERATION", query),
				sfilter.globalFilter, params, query)
	}

	def litJubOncAltSumCount(SearchFilter sfilter) {
	    def query = new LiteratureDataQuery( mainTableAlias:"data")
		return executeLitSumQueryCount("LiteratureAlterationData data",
				litAltConditions(sfilter.litFilter, "JUBILANT_ONCOLOGY_ALTERATION", query),
				sfilter, query)
	}

	def litJubOncAltSumData(SearchFilter sfilter, params) {
	    def query = new LiteratureDataQuery( mainTableAlias:"data")
		return executeLitSumQueryData("LiteratureAlterationData data",
				litAltConditions(sfilter.litFilter, "JUBILANT_ONCOLOGY_ALTERATION", query),
				sfilter, params, query)
	}
	
	/**
	 * Gathers the WHERE clause conditions for the Alteration data
	 * 
	 * @param litFilter the LiteratureFilter
	 * @param dataType (asthma or oncology)
	 * @param query the LiteratureDataQuery that will be used to query the database
	 * 
	 * @returns the named parameters for the query
	 */
	def litAltConditions(LiteratureFilter litFilter, dataType, LiteratureDataQuery query) {
	    def namedParameters = [:]
	    
	    query.addCondition("data.dataType= :dataType")
	    namedParameters["dataType"] = dataType
	    
	    if (litFilter.hasDisease())	{	        
	        query.addCondition("data_dis2.id= :dd2ID")
	        namedParameters["dd2ID"] = litFilter.bioDiseaseId
	    }
		if (litFilter.hasDiseaseSite()) {
		    query.addCondition("data.reference.diseaseSite in (:diseaseSites)")
		    namedParameters["diseaseSites"] = litFilter.diseaseSite
		}				
		if (litFilter.hasComponent())	{
		    query.addCondition("data.reference.component in (:compList)")
		    query.addCondition("data.reference.geneId in (:geneList)")
		    namedParameters["compList"] = litFilter.pairCompList
		    namedParameters["geneList"] = litFilter.pairGeneList
		}		
		if (litFilter.hasMutationType()) {
		    query.addCondition("data.mutationType= :mutationType")
		    namedParameters["mutationType"] = litFilter.mutationType
		}
		if (litFilter.hasMutationSite()) {
		    query.addCondition("data.mutationSites= :mutationSite")
		    namedParameters["mutationSite"] = litFilter.mutationSite
		}
		if (litFilter.hasEpigeneticType()) {
		    query.addCondition("data.epigeneticType= :epigeneticType")
		    namedParameters["epigeneticType"] = litFilter.epigeneticType
		}
		if (litFilter.hasEpigeneticRegion()) {
			query.addCondition("data.epigeneticRegion= :epigeneticRegion")
			namedParameters["epigeneticRegion"] = litFilter.epigeneticRegion
		}
		if (litFilter.hasAlterationType()) {	
			def types = litFilter.getSelectedAlterationTypes()
			if (types.size() > 0) {
			    query.addCondition("data.alterationType in (:alterationTypes)")
			    namedParameters["alterationTypes"] = types
			} else {
			    query.addCondition("data.alterationType is null") 
			}
		}
		if (litFilter.hasMoleculeType()) {
		    query.addCondition("data.reference.moleculeType= :moleculeType")
		    namedParameters["moleculeType"] = litFilter.moleculeType
		}
		if (litFilter.hasRegulation()) {
			if (litFilter.regulation.equals("Expression")) {
			    query.addCondition("data.totalExpPercent is not null")
			} else if (litFilter.regulation.equals("OverExpression")) {
				query.addCondition("data.overExpPercent is not null")				
			}
		}
		if (litFilter.hasPtmType()) {
		    query.addCondition("data.ptmType= :ptmType")
		    namedParameters["ptmType"] = litFilter.ptmType
		}
		if (litFilter.hasPtmRegion()) {
		    query.addCondition("data.ptmRegion= :ptmRegion")
		    namedParameters["ptmRegion"] = litFilter.ptmRegion
		}
		return namedParameters
	}

	/*************************************************************************
	 * Julilant Oncology Inhibitor methods
	 *************************************************************************/
	def litJubOncInhCount(SearchFilter sfilter) {
	    def query = new LiteratureDataQuery(mainTableAlias:"data")
		return executeLitQueryCount("LiteratureInhibitorData data",
				litInhConditions(sfilter.litFilter, "JUBILANT_ONCOLOGY_INHIBITOR", query),
				sfilter.globalFilter, query)
	}

	def litJubOncInhData(SearchFilter sfilter, params) {
	    def query = new LiteratureDataQuery(mainTableAlias:"data")
		return executeLitQueryData("LiteratureInhibitorData data",
				litInhConditions(sfilter.litFilter, "JUBILANT_ONCOLOGY_INHIBITOR", query),
				sfilter.globalFilter, params, query)
	}

	def litJubAsthmaInhCount(SearchFilter sfilter) {	    
		return 0
// TODO: Uncomment this code after the asthma inhibitor data has been loaded.
//      def query = new LiteratureDataQuery(mainTableAlias:"data")
//		return executeLitQueryCount("LiteratureInhibitorData data",
//				litInhConditions(sfilter.litFilter, "JUBILANT_ASTHMA_INHIBITOR", query),
//				sfilter.globalFilter, query)
	}

	def litJubAsthmaInhData(SearchFilter sfilter, params) {
	    def query = new LiteratureDataQuery(mainTableAlias:"data")
		return executeLitQueryData("LiteratureInhibitorData data",
				litInhConditions(sfilter.litFilter, "JUBILANT_ASTHMA_INHIBITOR", query),
				sfilter.globalFilter, params, query)
	}

	/**
	 * Gathers the WHERE clause conditions for the Inhibitor data
	 * 
	 * @param litFilter the LiteratureFilter
	 * @param dataType (asthma or oncology)
	 * @param query the LiteratureDataQuery that will be used to query the database
	 * 
	 * @returns the named parameters for the query
	 */
	def litInhConditions(LiteratureFilter litFilter, dataType, LiteratureDataQuery query) {
	    def namedParameters = [:]
		    
		query.addCondition("data.dataType= :dataType")
		namedParameters["dataType"] = dataType
		   
		if (litFilter.hasDisease())	{
		    query.addCondition("data_dis2.id= :dd2ID")
		    namedParameters["dd2ID"] = litFilter.bioDiseaseId
		}
	    if (litFilter.hasDiseaseSite()) {
		    query.addCondition("data.reference.diseaseSite in (:diseaseSites)")
		    namedParameters["diseaseSites"] = litFilter.diseaseSite
		}	
		if (litFilter.hasComponent())	{
		    query.addCondition("data.reference.component in (:compList)")
		    query.addCondition("data.reference.geneId in (:geneList)")
		    namedParameters["compList"] = litFilter.pairCompList
		    namedParameters["geneList"] = litFilter.pairGeneList
		}	
		if (litFilter.hasTrialType()) {
		    query.addCondition("data.trialType= :trialType")
		    namedParameters["trialType"] = litFilter.trialType
		}
		if (litFilter.hasTrialPhase()) {
		    query.addCondition("data.trialPhase= :trialPhase")
		    namedParameters["trialPhase"] = litFilter.trialPhase
		}
		if (litFilter.hasInhibitorName()) {
		    query.addCondition("data.inhibitor= :inhibitor")
		    namedParameters["inhibitor"] = litFilter.inhibitorName
		}
		if (litFilter.hasTrialExperimentalModel()) {
		    query.addCondition("data.trialExperimentalModel= :trialExperimentalModel")
		    namedParameters["trialExperimentalModel"] = litFilter.trialExperimentalModel
		}
		return namedParameters
	}

	/*************************************************************************
	 * Julilant Oncology Interaction methods
	 *************************************************************************/
	def litJubOncIntCount(SearchFilter sfilter) {
	    def query = new LiteratureDataQuery(mainTableAlias:"data")
		def table = "org.transmart.biomart.LiteratureInteractionData data"
		if (sfilter.litFilter.hasExperimentalModel()) {
			table += " LEFT OUTER JOIN data.inVivoModel invivo LEFT OUTER JOIN data.inVitroModel invitro"
		}
		return executeLitQueryCount(table,
				litIntConditions(sfilter.litFilter, "JUBILANT_ONCOLOGY_INTERACTION", query),
				sfilter.globalFilter, query)
	}

	def litJubOncIntData(SearchFilter sfilter, params) {
	    def query = new LiteratureDataQuery(mainTableAlias:"data")
		def table = "org.transmart.biomart.LiteratureInteractionData data"
		if (sfilter.litFilter.hasExperimentalModel()) {
			table += " LEFT OUTER JOIN data.inVivoModel invivo LEFT OUTER JOIN data.inVitroModel invitro"
		}
		return executeLitQueryData(table,
				litIntConditions(sfilter.litFilter, "JUBILANT_ONCOLOGY_INTERACTION", query),
				sfilter.globalFilter, params, query)
	}

	def litJubAsthmaIntCount(SearchFilter sfilter) {
	    def query = new LiteratureDataQuery(mainTableAlias:"data")
		def table = "org.transmart.biomart.LiteratureInteractionData data"
		if (sfilter.litFilter.hasExperimentalModel()) {
			table += " LEFT OUTER JOIN data.inVivoModel invivo LEFT OUTER JOIN data.inVitroModel invitro"
		}
		return executeLitQueryCount(table,
				litIntConditions(sfilter.litFilter, "JUBILANT_ASTHMA_INTERACTION", query),
				sfilter.globalFilter, query)
	}

	def litJubAsthmaIntData(SearchFilter sfilter, params) {
	    def query = new LiteratureDataQuery(mainTableAlias:"data")
		def table = "org.transmart.biomart.LiteratureInteractionData data"
		if (sfilter.litFilter.hasExperimentalModel()) {
			table += " LEFT OUTER JOIN data.inVivoModel invivo LEFT OUTER JOIN data.inVitroModel invitro"
		}
		return executeLitQueryData(table,
				litIntConditions(sfilter.litFilter, "JUBILANT_ASTHMA_INTERACTION", query),
				sfilter.globalFilter, params, query)
	}
	
	/**
	 * Gathers the WHERE clause conditions for the Interaction data
	 * 
	 * @param litFilter the LiteratureFilter
	 * @param dataType (asthma or oncology)
	 * @param query the LiteratureDataQuery that will be used to query the database
	 * 
	 * @returns the named parameters for the query
	 */
	def litIntConditions(LiteratureFilter litFilter, dataType, LiteratureDataQuery query) {
	    def namedParameters = [:]
		    
	    query.addCondition("data.dataType= :dataType")
		namedParameters["dataType"] = dataType
			   
		if (litFilter.hasDisease())	{
			query.addCondition("data_dis2.id= :dd2ID")
			namedParameters["dd2ID"] = litFilter.bioDiseaseId
		}
	    if (litFilter.hasDiseaseSite()) {
		    query.addCondition("data.reference.diseaseSite in (:diseaseSites)")
		    namedParameters["diseaseSites"] = litFilter.diseaseSite
		}	
		if (litFilter.hasComponent())	{	
		    query.addCondition("data.reference.component in (:compList)")
		    query.addCondition("data.reference.geneId in (:geneList)")
		    namedParameters["compList"] = litFilter.pairCompList
		    namedParameters["geneList"] = litFilter.pairGeneList
		}
		if (litFilter.hasSource()) {
		    query.addCondition("data.sourceComponent= :sourceComponent")
		    namedParameters["sourceComponent"] = litFilter.source
		}
		if (litFilter.hasTarget()) {
		    query.addCondition("data.targetComponent= :targetComponent")
		    namedParameters["targetComponent"] = litFilter.target
		}
		if (litFilter.hasExperimentalModel()) {	
		    query.addCondition("(invivo.experimentalModel= :experimentalModel or invitro.experimentalModel= :experimentalModel)")
		    namedParameters["experimentalModel"] = litFilter.experimentalModel
		}
		if (litFilter.hasMechanism()) {
		    query.addCondition("data.mechanism= :mechanism")
		    namedParameters["mechanism"] = litFilter.mechanism
		}
		return namedParameters
	 }
	
	/*************************************************************************
	 * Julilant Protein Effect methods
	 *************************************************************************/
	def litJubAsthmaPECount(SearchFilter sfilter) {
	    def query = new LiteratureDataQuery(mainTableAlias:"data")
		return executeLitQueryCount("LiteratureProteinEffectData data",
				litPEConditions(sfilter.litFilter, "JUBILANT_ASTHMA_PROTEIN_EFFECT", query),
				sfilter.globalFilter, query)
	}

	def litJubAsthmaPEData(SearchFilter sfilter, params) {
	    def query = new LiteratureDataQuery(mainTableAlias:"data")
		return executeLitQueryData("LiteratureProteinEffectData data",
				litPEConditions(sfilter.litFilter, "JUBILANT_ASTHMA_PROTEIN_EFFECT", query),
				sfilter.globalFilter, params, query)
	}
	
	/**
	 * Gathers the WHERE clause conditions for the Interaction data
	 * 
	 * @param litFilter the LiteratureFilter
	 * @param dataType (asthma or oncology)
	 * @param query the LiteratureDataQuery that will be used to query the database
	 * 
	 * @returns the named parameters for the query
	 */
	def litPEConditions(LiteratureFilter litFilter, dataType, LiteratureDataQuery query) {
	    def namedParameters = [:]
		    
		query.addCondition("data.dataType= :dataType")
		namedParameters["dataType"] = dataType
				   
		if (litFilter.hasDisease())	{
			query.addCondition("data_dis2.id= :dd2ID")
			namedParameters["dd2ID"] = litFilter.bioDiseaseId
		}
	    if (litFilter.hasDiseaseSite()) {
		    query.addCondition("data.reference.diseaseSite in (:diseaseSites)")
		    namedParameters["diseaseSites"] = litFilter.diseaseSite
		}	
		if (litFilter.hasComponent())	{
		    query.addCondition("data.reference.component in (:compList)")
		    query.addCondition("data.reference.geneId in (:geneList)")
		    namedParameters["compList"] = litFilter.pairCompList
		    namedParameters["geneList"] = litFilter.pairGeneList
		}
		return namedParameters
	}
	
	/*************************************************************************
	 * Jubilant Queries to return URNs for Pathway Studio integration
	 *************************************************************************/
	def findGeneURN(String name)	{
        def geneURN = null
      
        if (name.indexOf('.') < 0)	{
            StringBuffer sql = new StringBuffer("select bdec.code from BioDataExternalCode bdec")
            sql.append(" where bdec.bioDataId in (select innerb.bioDataId from BioDataExternalCode innerb")
            sql.append(" where upper(innerb.code) = ?)")
            sql.append(" and bdec.codeType = ?")        
        
            def result = BioDataExternalCode.executeQuery(sql.toString(), [name.toUpperCase(), "URN"])
            if (result[0] != null)	{
                geneURN = result[0]                
            }          
        }
        return geneURN
    }
            
    def findSmallMolURN(String name)	{
        def smURN = null
       
        StringBuffer sql = new StringBuffer("select bdec.code from BioDataExternalCode bdec")
        sql.append(" where bdec.bioDataId = (select c.id from Compound c")
        sql.append(" where upper(c.codeName) = ? and c.productCategory = ?)")
        sql.append(" and bdec.codeSource = ?")
        sql.append(" and bdec.codeType = ?")        
        def result = BioDataExternalCode.executeQuery(sql.toString(), [name.toUpperCase(), "Small Molecule", "ARIADNE", "URN"])
        if (result.size() == 1)	{
            smURN = result[0]            
        } 
        
        return smURN
    }
    
    def findDiseaseURN(String name)	{
        log.info("Calling findDiseaseURN for " + name)
        def diseaseURN = null
        StringBuffer sql = new StringBuffer("select bdec.code from BioDataExternalCode bdec")
        sql.append(" where bdec.bioDataId in (select bdecInner.bioDataId from BioDataExternalCode bdecInner")
        sql.append(" where upper(bdecInner.code) = ?")
        sql.append(" and bdecInner.codeSource = ?")
        sql.append(" and bdecInner.codeType = ?")
        sql.append(" and bdecInner.bioDataType = ?)")                
        sql.append(" and bdec.codeSource = ?")
        sql.append(" and bdec.codeType = ?")
        sql.append(" and bdec.bioDataType = ?")
        log.trace(sql.toString())
        def result = BioDataExternalCode.executeQuery(sql.toString(),
                [name.toUpperCase(), "ARIADNE", "ALIAS", "BIO_DISEASE", "ARIADNE", "URN", "BIO_DISEASE"])                
        if (result.size() == 1)	{
            diseaseURN = result[0]            
        } else	{
            log.warn("Unable to find the Disease URN.  Size = " + result.size())            
        }
        return diseaseURN  
    }

	/*************************************************************************
	 * Jubilant Oncology Filter Queries
	 *************************************************************************/
	def executeJubOncologyQueryFilter(column, table, searchFilter) {
		def gfilter = searchFilter.globalFilter
		if(gfilter == null || gfilter.isTextOnly()){
			return []
		}
		
		def query = new LiteratureDataQuery( mainTableAlias:"data");
		query.setDistinct = true
		query.addSelect("data." + column)
		query.addTable(table + " data")
		query.addCondition(" data." + column + " is not null ")
		query.addOrderBy(" data." + column)
		query.createGlobalFilterCriteria(gfilter);
		def sql = query.generateSQL()
	    def results = Literature.executeQuery(sql)
	    return results
	}
	
	def diseaseList(searchFilter) {
		//return executeJubOncologyQueryFilter("data_dis.preferredName", "org.transmart.biomart.Literature", searchFilter)
		def gfilter = searchFilter.globalFilter
		if(gfilter == null || gfilter.isTextOnly()){
			return []
		}

		def query = new LiteratureDataQuery(mainTableAlias:"data");
		query.setDistinct = true
		query.addSelect("data_dis2")
		query.addTable("org.transmart.biomart.Literature data");
		query.addTable("JOIN data.diseases data_dis2")
		query.addOrderBy("data_dis2.preferredName")
		query.createGlobalFilterCriteria(gfilter, true);
		def sql = query.generateSQL()
	    def results = Literature.executeQuery(sql)
	    return results		
	}
	
	def diseaseSiteList(searchFilter) {
		return executeJubOncologyQueryFilter("reference.diseaseSite", "org.transmart.biomart.Literature", searchFilter)
	}
	
	def componentList(searchFilter)	{
	    // return executeJubOncologyQueryFilter("reference.component", "org.transmart.biomart.Literature", searchFilter)
	    // Need to filter and send only genes/proteins
	    def gfilter = searchFilter.globalFilter
		if(gfilter == null || gfilter.isTextOnly()){
			return []
		}
		
		def query = new LiteratureDataQuery( mainTableAlias:"data");
		query.setDistinct = true
		query.addSelect("data.reference.component")
		query.addSelect("data.reference.geneId")
		query.addTable("org.transmart.biomart.Literature data")
		query.addCondition("data.reference.geneId is not null")
		query.addOrderBy("data.reference.component")
		query.createGlobalFilterCriteria(gfilter);
		def sql = query.generateSQL()
	    def results = Literature.executeQuery(sql)
	    return results
	}

	def mutationTypeList(searchFilter) {
		return executeJubOncologyQueryFilter("mutationType", "org.transmart.biomart.LiteratureAlterationData", searchFilter)
	}

	def mutationSiteList(searchFilter) {
		return executeJubOncologyQueryFilter("mutationSites", "org.transmart.biomart.LiteratureAlterationData", searchFilter)
	}

	def epigeneticTypeList(searchFilter) {
		return executeJubOncologyQueryFilter("epigeneticType", "org.transmart.biomart.LiteratureAlterationData", searchFilter)
	}

	def epigeneticRegionList(searchFilter) {
		return executeJubOncologyQueryFilter("epigeneticRegion", "org.transmart.biomart.LiteratureAlterationData", searchFilter)
	}

	def moleculeTypeList(searchFilter) {
		return executeJubOncologyQueryFilter("reference.moleculeType", "org.transmart.biomart.Literature", searchFilter)
	}

	def ptmTypeList(searchFilter) {
		return executeJubOncologyQueryFilter("ptmType", "org.transmart.biomart.LiteratureAlterationData", searchFilter)
	}

	def ptmRegionList(searchFilter) {
		return executeJubOncologyQueryFilter("ptmRegion", "org.transmart.biomart.LiteratureAlterationData", searchFilter)
	}

	def sourceList(searchFilter) {
		return executeJubOncologyQueryFilter("sourceComponent", "org.transmart.biomart.LiteratureInteractionData", searchFilter)
	}

	def targetList(searchFilter) {
		return executeJubOncologyQueryFilter("targetComponent", "org.transmart.biomart.LiteratureInteractionData", searchFilter)
	}

	def experimentalModelList(searchFilter) {
		def gfilter = searchFilter.globalFilter
		if(gfilter == null || gfilter.isTextOnly()){
			return []
		}
		
		def query = new LiteratureDataQuery( mainTableAlias:"data");
		query.setDistinct = true
		query.addSelect("mv.experimentalModel")
		query.addTable("org.transmart.biomart.LiteratureInteractionData data")
		query.addTable("org.transmart.biomart.LiteratureInteractionModelMV mv")
		query.addCondition("data.id = mv.id")
		query.addOrderBy("mv.experimentalModel")
		query.createGlobalFilterCriteria(gfilter);
		def sql = query.generateSQL()
	    def results = Literature.executeQuery(sql)
	    return results
	}

	def mechanismList(searchFilter) {
		return executeJubOncologyQueryFilter("mechanism", "org.transmart.biomart.LiteratureInteractionData", searchFilter)
	}

	def trialTypeList(searchFilter) {
		return executeJubOncologyQueryFilter("trialType", "org.transmart.biomart.LiteratureInhibitorData", searchFilter)
	}

	def trialPhaseList(searchFilter) {
		return executeJubOncologyQueryFilter("trialPhase", "org.transmart.biomart.LiteratureInhibitorData", searchFilter)
	}

	def inhibitorNameList(searchFilter){
		return executeJubOncologyQueryFilter("inhibitor", "org.transmart.biomart.LiteratureInhibitorData", searchFilter)
	}

	def trialExperimentalModelList(searchFilter) {
		return executeJubOncologyQueryFilter("trialExperimentalModel", "org.transmart.biomart.LiteratureInhibitorData", searchFilter)
	}
}
