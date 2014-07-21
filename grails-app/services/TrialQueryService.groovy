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

import com.recomdata.search.query.AssayAnalysisDataQuery
import com.recomdata.search.query.Query
import org.transmart.*
import org.transmart.biomart.BioAssayAnalysis
import org.transmart.biomart.BioAssayAnalysisData
import org.transmart.biomart.ClinicalTrial

/**
 * $Id: TrialQueryService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */

class TrialQueryService {

    /**
     * count Analysis with criteria
     */
    def countTrial(SearchFilter filter) {

        if (filter == null || filter.globalFilter.isTextOnly()) {
            return 0
        }

        return BioAssayAnalysisData.executeQuery(createQuery("COUNT_EXP", filter))[0]
    }

    def countAnalysis(SearchFilter filter) {

        if (filter == null || filter.globalFilter.isTextOnly()) {
            return 0
        }

        return BioAssayAnalysisData.executeQuery(createQuery("COUNT_ANALYSIS", filter))[0]
    }

    /**
     * retrieve trials with criteria
     */
    def queryTrial(boolean count, SearchFilter filter, paramMap) {

        if (filter == null || filter.globalFilter.isTextOnly()) {
            return []
        }
        def result = BioAssayAnalysisData.executeQuery(createQuery("DATA", filter), paramMap == null ? [:] : paramMap)

        List trialResult = []
        //def analysisCount = 0;
        //def expCount = 0;
        for (row in result) {
            //	analysisCount +=row[1];
            //	expCount++;
            trialResult.add(new TrialAnalysisResult(trial: ClinicalTrial.get(row[0]), analysisCount: row[1], groupByExp: true))
        }
        return new ExpAnalysisResultSet(expAnalysisResults: trialResult, groupByExp: true)
    }

    /**
     *
     */
    def createQuery(countType, SearchFilter filter) {
        if (filter == null || filter.globalFilter.isTextOnly()) {
            return " WHERE 1=0"
        }
        def gfilter = filter.globalFilter

        def query = new AssayAnalysisDataQuery(mainTableAlias: 'baad');
        query.addTable("org.transmart.biomart.BioAssayAnalysisData baad ");
        query.addTable("org.transmart.biomart.ClinicalTrial ct ");
        query.addCondition("baad.experiment.id = ct.id ")

        query.createGlobalFilterCriteria(gfilter);
        createTrialFilterCriteria(filter.trialFilter, query);

        // handle switch scenarios
        if ("COUNT_EXP".equals(countType)) {
            query.addSelect("COUNT(DISTINCT baad.experiment.id) ");
        } else if ("COUNT_ANALYSIS".equals(countType)) {
            //	query.addTable("JOIN baad.markers baad_bm");
            query.addSelect("COUNT(DISTINCT baad.analysis.id) ");
        } else if ("DATA".equals(countType)) {
            query.addTable("JOIN baad.featureGroup.markers baad_bm");
            query.addSelect("DISTINCT baad.experiment.id, COUNT(distinct baad.analysis.id)  ")
            query.addGroupBy(" baad.experiment.id ")
            query.addOrderBy(" COUNT(distinct baad.analysis.id) DESC ")
        } else {
            query.addSelect("DISTINCT baad.experiment.id, COUNT(distinct baad.analysis.id)  ")
            query.addGroupBy(" baad.experiment.id ")
            query.addOrderBy(" COUNT(distinct baad.analysis.id) DESC ")
        }

        def q = query.generateSQL()
        //println(q)
        return q
    }

    /**
     * find distinct trial analyses with current filters
     */
    def createAnalysisIDSelectQuery(SearchFilter filter) {
        if (filter == null || filter.globalFilter.isTextOnly()) {
            return " SELECT -1 FROM org.transmart.biomart.BioAssayAnalysisData baad WHERE 1 = 1 "
        }
        def gfilter = filter.globalFilter

        def query = new AssayAnalysisDataQuery(mainTableAlias: "baad", setDistinct: true);
        query.addTable("org.transmart.biomart.BioAssayAnalysisData baad ");
        query.addTable("org.transmart.biomart.ClinicalTrial ct ");
        query.addCondition("baad.experiment.id = ct.id ")

        query.createGlobalFilterCriteria(gfilter);
        createTrialFilterCriteria(filter.trialFilter, query);

        query.addSelect("baad.analysis.id")

        return query.generateSQL()
    }

    /**
     *
     */
    def queryTrialAnalysis(clinicalTrialId, SearchFilter filter) {

        def gfilter = filter.globalFilter

        def query = new AssayAnalysisDataQuery(mainTableAlias: "baad", setDistinct: true)
        query.addTable("org.transmart.biomart.BioAssayAnalysisData baad")
        query.addTable("JOIN baad.featureGroup.markers baad_bm")
        query.addSelect("baad")
        query.addSelect("baad_bm")
        query.addCondition("baad.experiment.id = " + clinicalTrialId)
        // expand biomarkers
        query.createGlobalFilterCriteria(gfilter, true);

        createTrialFilterCriteria(filter.trialFilter, query);
        def sql = query.generateSQL();
        //log.info(">> TrialAnalysis query:\n"+sql);

        def result = null;
        def tResult = new TrialAnalysisResult(trial: ClinicalTrial.get(clinicalTrialId))
        // println("exe sql:"+sql)
        if (!gfilter.getBioMarkerFilters().isEmpty()) {
            result = BioAssayAnalysisData.executeQuery(sql)
            processAnalysisResult(result, tResult)
        } else {
            def allAnalysis = getAnalysesForExpriment(clinicalTrialId, filter);
            result = []
            for (row in allAnalysis) {
                def analysisId = row[0]
                def countGene = row[1]
                //result = getTopAnalysisDataForAnalysis(analysisId, 5);
                result = BioAssayAnalysis.getTopAnalysisDataForAnalysis(analysisId, 50);
                def analysisResult = new AnalysisResult(analysis: BioAssayAnalysis.get(analysisId), bioMarkerCount: countGene)
                tResult.analysisResultList.add(analysisResult)
                processAnalysisResultNoSort(result, analysisResult)
            }
        }

        return tResult;
    }

    def createBaseQuery(SearchFilter filter, trialFilter) {
        def gfilter = filter.globalFilter

        def query = new AssayAnalysisDataQuery(mainTableAlias: "baad", setDistinct: true)
        query.addTable("org.transmart.biomart.BioAssayAnalysisData baad")
        if (filter != null)
            query.createGlobalFilterCriteria(gfilter, true);
        if (trialFilter != null)
            createTrialFilterCriteria(trialFilter, query);
        return query;
    }

    /**
     *  get ananlysis only
     */
    def getAnalysesForExpriment(clinicalTrialId, SearchFilter filter) {
        // need both filters here
        def query = createBaseQuery(filter, filter.trialFilter);
        query.addSelect("baad.analysis.id")
        query.addTable("JOIN baad.featureGroup.markers baad_bm")
        query.addSelect("COUNT(DISTINCT baad_bm.id)")
        query.addCondition("baad.experiment.id =" + clinicalTrialId)
        query.addGroupBy("baad.analysis")
        query.addOrderBy("COUNT(DISTINCT baad_bm.id) DESC")
        return BioAssayAnalysisData.executeQuery(query.generateSQL());
    }

    /**
     * process analysis result
     */
    def processAnalysisResultNoSort(List result, AnalysisResult aresult) {

        //def aresult = new AnalysisResult(analysis);
        for (row in result) {
            def analysisData = row[0]
            def biomarker = row[1];
            aresult.assayAnalysisValueList.add(new AssayAnalysisValue(analysisData: analysisData, bioMarker: biomarker))

        }

    }

    /**
     *
     */
    def processAnalysisResult(List result, TrialAnalysisResult tar) {
        LinkedHashMap analysisResultMap = new LinkedHashMap()

        for (row in result) {
            def analysisData = row[0]
            def biomarker = row[1]; //org.transmart.biomart.BioMarker.get(row[1])
            //println(biomarker)
            def aresult = analysisResultMap.get(analysisData.analysis.id)
            if (aresult == null) {
                aresult = new AnalysisResult(analysis: analysisData.analysis)
                analysisResultMap.put(analysisData.analysis.id, aresult)

            }
            aresult.assayAnalysisValueList.add(new AssayAnalysisValue(analysisData: analysisData, bioMarker: biomarker))
        }

        def mc = [
                compare: { a, b -> a.equals(b) ? 0 : (((double) a.size()) / ((double) a.analysis.dataCount)) > (((double) b.size()) / ((double) b.analysis.dataCount)) ? -1 : 1 }
        ] as Comparator


        Collection allanalysis = analysisResultMap.values().sort(mc)
        tar.analysisResultList.addAll(allanalysis)
    }

    /**
     * trial filter criteria
     */
    def createTrialFilterCriteria(trialfilter, Query query) {

        // disease
        if (trialfilter.hasDisease()) {
            def alias = query.mainTableAlias + "_dis"
            query.addTable("JOIN " + query.mainTableAlias + ".experiment.diseases " + alias)
            query.addCondition(alias + ".id = " + trialfilter.bioDiseaseId)
        }

        // compound
        if (trialfilter.hasCompound()) {
            def alias = query.mainTableAlias + "_cpd"
            query.addTable("JOIN " + query.mainTableAlias + ".experiment.compounds " + alias)
            query.addCondition(alias + ".id = " + trialfilter.bioCompoundId)
        }

        // design
        if (trialfilter.hasStudyDesign()) {
            query.addTable("org.transmart.biomart.ClinicalTrial ct ");
            query.addCondition("baad.experiment.id = ct.id ")
            query.addCondition("ct.design = '" + trialfilter.studyDesign + "'")
        }

        // type
        if (trialfilter.hasStudyType()) {
            query.addTable("org.transmart.biomart.ClinicalTrial ct ");
            query.addCondition("baad.experiment.id = ct.id ")
            query.addCondition("ct.studyType = '" + trialfilter.studyType + "'")
        }

        // study phase
        if (trialfilter.hasPhase()) {
            query.addTable("org.transmart.biomart.ClinicalTrial ct ");
            query.addCondition("baad.experiment.id = ct.id ")
            query.addCondition("ct.studyPhase = '" + trialfilter.phase + "'")
        }

        def bFirstWhereItem = true;
        StringBuilder s = new StringBuilder();

        // fold change on BioAssayAnalysisData
        if (trialfilter.hasFoldChange()) {
            //s.append("(baad.foldChangeRatio >=")
            //	.append(trialfilter.foldChange)
            //  .append(" OR baad.foldChangeRatio <= ")
            //	.append(-trialfilter.foldChange).append(")")
            //	.append(" OR baad.foldChangeRatio IS NULL)")
            bFirstWhereItem = false
            s.append("( abs(baad.foldChangeRatio) >= ").append(trialfilter.foldChange).append(" OR baad.foldChangeRatio IS NULL)")
        }

        // preferred p value on BioAssayAnalysisData
        if (trialfilter.hasPValue()) {
            if (bFirstWhereItem) {
                s.append(" (baad.preferredPvalue <= ").append(trialfilter.pvalue).append(" )")
            } else {
                s.append(" AND (baad.preferredPvalue <= ").append(trialfilter.pvalue).append(" )")
            }
            //.append(" OR baad.preferredPvalue IS NULL)")
        }
        //		 rvalue on BioAssayAnalysisData
        if (trialfilter.hasRValue()) {
            if (bFirstWhereItem) {
                s.append(" ((baad.rvalue >= abs(").append(trialfilter.rvalue).append(")) OR (baad.rhoValue>=abs(").append(trialfilter.rvalue).append(")) OR baad.rhoValue IS NULL)");
            } else {
                s.append(" AND (baad.rvalue >= abs(").append(trialfilter.rvalue).append(")) OR (baad.rhoValue>=abs(").append(trialfilter.rvalue).append(")) OR baad.rhoValue IS NULL)");
            }

        }
        // platform filter
        if (trialfilter.hasPlatform()) {
            if (bFirstWhereItem) {
                s.append(" (baad.analysis.assayDataType = '").append(trialfilter.platform).append("')")
            } else {
                s.append(" AND (baad.analysis.assayDataType = '").append(trialfilter.platform).append("')")
            }
        }

        // add filter criteria
        if (s.size() > 0) query.addCondition(s.toString());

        // clinical trials
        if (trialfilter.hasSelectedTrials()) {
            query.addTable("org.transmart.biomart.ClinicalTrial ct ");
            query.addCondition("baad.experiment.id = ct.id ")
            query.addCondition("ct.id in (" + trialfilter.createTrialInclause() + ")")

        }
    }

    /**
     * Execute the SOLR query to get the analyses for the trial that match the given search criteria
     * @param solrRequestUrl - the base URL for the SOLR request
     * @param solrQueryParams - the query string for the search, to be passed into the data for the POST request
     * @return List containing the analysis Ids
     */
    def executeSOLRTrialAnalysisQuery = { solrRequestUrl, solrQueryParams ->

        List analysisIds = []

        def slurper = new XmlSlurper()

        // submit request
        def solrConnection = new URL(solrRequestUrl).openConnection()
        solrConnection.requestMethod = "POST"
        solrConnection.doOutput = true

        // add params to request
        def dataWriter = new OutputStreamWriter(solrConnection.outputStream)
        dataWriter.write(solrQueryParams)
        dataWriter.flush()
        dataWriter.close()

        def docs   // will store the document nodes from the xml response in here

        // process response
        if (solrConnection.responseCode == solrConnection.HTTP_OK) {
            def xml

            solrConnection.inputStream.withStream {
                xml = slurper.parse(it)
            }

            // retrieve all the document nodes from the xml
            docs = xml.result.find { it.@name == 'response' }.doc
        } else {
            throw new Exception("SOLR Request failed! Request url:" + solrRequestUrl + "  Response code:" + solrConnection.responseCode + "  Response message:" + solrConnection.responseMessage)
        }

        solrConnection.disconnect()

        // put analysis id for each document into a list to pass back
        for (docNode in docs) {
            def analysisIdNode = docNode.str.find { it.@name == 'ANALYSIS_ID' }
            def analysisId = analysisIdNode.text()

            analysisIds.add(analysisId)
        }

        return analysisIds
    }

    /**
     *   Execute a SOLR query to retrieve all the analyses for a certain trial that match the given criteria
     */
    def querySOLRTrialAnalysis(params, List sessionFilter) {
        RWGController rwgController = new RWGController()

        def trialNumber = params['trialNumber']

        // create a copy of the original list (we don't want to mess with original filter params)
        // (the list is an object, so it is not "passed by val", it's a reference)
        def filter = []
        sessionFilter.each {
            filter.add(it)
        }

        filter.add("STUDY_ID:" + trialNumber)
        def nonfacetedQueryString = rwgController.createSOLRNonfacetedQueryString(filter)

        String solrRequestUrl = rwgController.createSOLRQueryPath()

        // TODO create a conf setting for max rows
        String solrQueryString = rwgController.createSOLRQueryString(nonfacetedQueryString, "", "", 10000, false)
        def analysisIds = executeSOLRTrialAnalysisQuery(solrRequestUrl, solrQueryString)

        def analysisList = []

        // retrieve the descriptions for each analysis
        def results = BioAssayAnalysis.executeQuery("select b.id, b.shortDescription, b.longDescription, b.name " +
                " from org.transmart.biomart.BioAssayAnalysis b" +
                " where b.id in (" + analysisIds.join(',') + ") ORDER BY b.longDescription")

        // retrieve the analyses that are of type Time Course by checking the taxonomy
//		def timeCourseAnalyses = bio.BioAnalysisAttributeLineage.executeQuery("select b1.bioAnalysisAttribute.bioAssayAnalysisID from bio.BioAnalysisAttributeLineage b1" +
//			" where b1.bioAnalysisAttribute.bioAssayAnalysisID in (" + analysisIds.join(',') +  ") " +
//			" and lower(b1.ancestorTerm.termName) = lower('Time Course')" )
// 
        for (r in results) {

            // if current analysis is in time course list then set flag to true
            def isTimeCourse = false
            //if (timeCourseAnalyses.contains(r[0]))  {
            //	isTimeCourse = true
            //}

            // create a map for each record
            def aMap = ['id': r[0], 'shortDescription': r[1], 'longDescription': r[2], 'name': r[3], 'isTimeCourse': isTimeCourse]

            analysisList.add aMap
        }

        return analysisList
    }

}
