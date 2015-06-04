import com.recomdata.export.ExportTableNew
import com.recomdata.statistics.StatHelper
import grails.converters.JSON
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartRenderingInfo
import org.jfree.chart.ChartUtilities
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.entity.StandardEntityCollection
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.StandardXYBarPainter
import org.jfree.chart.renderer.xy.XYBarRenderer
import org.jfree.chart.servlet.ChartDeleter
import org.jfree.chart.servlet.ServletUtilities
import org.jfree.data.statistics.HistogramDataset
import org.transmart.searchapp.AccessLog
import org.transmart.searchapp.AuthUser

import javax.servlet.ServletException
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpSession
import java.awt.*
import java.util.List

//import edu.mit.wi.haploview.*;
class ChartController {

    def index = {}

    def i2b2HelperService
    def springSecurityService
    def chartService
    def omicsQueryService


    def displayChart = {
        HttpSession session = request.getSession();
        String filename = request.getParameter("filename");
        log.trace("Trying to display:" + filename)
        if (filename == null) {
            throw new ServletException("Parameter 'filename' must be supplied");
        }

        //  Replace ".." with ""
        //  This is to prevent access to the rest of the file system
        filename = ServletUtilities.searchReplace(filename, "..", "");

        //  Check the file exists
        File file = new File(System.getProperty("java.io.tmpdir"), filename);
        if (!file.exists()) {
            throw new ServletException("File '" + file.getAbsolutePath()
                    + "' does not exist");
        }

        //  Check that the graph being served was created by the current user
        //  or that it begins with "public"
        boolean isChartInUserList = false;
        ChartDeleter chartDeleter = (ChartDeleter) session.getAttribute(
                "JFreeChart_Deleter");
        if (chartDeleter != null) {
            isChartInUserList = chartDeleter.isChartAvailable(filename);
        }

        boolean isChartPublic = false;
        if (filename.length() >= 6) {
            if (filename.substring(0, 6).equals("public")) {
                isChartPublic = true;
            }
        }

        boolean isOneTimeChart = false;
        if (filename.startsWith(ServletUtilities.getTempOneTimeFilePrefix())) {
            isOneTimeChart = true;
        }

        //if (isChartInUserList || isChartPublic || isOneTimeChart) {
        /*Code change by Jeremy Isikoff, Recombinant Inc. to always serve up images*/

        //  Serve it up
        ServletUtilities.sendTempFile(file, response);
        return;
    }

    /**
     * Action to get the counts for the children of the passed in concept key
     */
    def childConceptPatientCounts = {

        def paramMap = params;

        def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        log.trace("Called childConceptPatientCounts action in ChartController")
        log.trace("User is:" + user.username);
        log.trace(user.toString());
        def concept_key = params.concept_key;
        log.trace("Requested counts for parent_concept_path=" + concept_key);
        def counts = i2b2HelperService.getChildrenWithPatientCountsForConcept(concept_key)
        def access = i2b2HelperService.getChildrenWithAccessForUserNew(concept_key, user)
        log.trace("access:" + (access as JSON));
        log.trace("counts = " + (counts as JSON))

        def obj = [counts: counts, accesslevels: access, test1: "works"]
        render obj as JSON
    }

    /**
     * Action to get the patient count for a concept
     */
    def conceptPatientCount = {
        String concept_key = params.concept_key;
        PrintWriter pw = new PrintWriter(response.getOutputStream());
        pw.write(i2b2HelperService.getPatientCountForConcept(concept_key).toString());
        pw.flush();
    }

    /**
     * Action to get the distribution histogram for a concept
     */
    def conceptDistribution = {

        // Lets put a bit of 'audit' in here
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Set Value Concept Histogram", eventmessage: "Concept:" + params.concept_key, accesstime: new java.util.Date()).save()

        // We retrieve the result instance ids from the client
        def concept = params.concept_key ?: null
        def concepts = [:]

        // We retrieve the omics parameters from the client, if they were passed
        def omics_params = [:]
        if (params.omics_value_type != null) {
            omics_params.omics_value_type = params.omics_value_type
            omics_params.omics_platform = params.omics_platform
            omics_params.omics_projection_type = params.omics_projection_type
            omics_params.omics_selector = params.omics_selector
        }
        else {
            omics_params = null
        }

        // Collect concept information
        // We need to force computation for an empty instance ID
        concept = chartService.getConceptAnalysis(concept: i2b2HelperService.getConceptKeyForAnalysis(concept), omics_params: omics_params, subsets: [ 1: [ exists: true, instance : "" ], 2: [ exists: false ], commons: [:]], chartSize : [width : 245, height : 180])

        PrintWriter pw = new PrintWriter(response.getOutputStream());
        pw.write(concept.commons.conceptHisto)
        pw.flush();
    }


    def conceptDistributionForSubset = {

        // Lets put a bit of 'audit' in here
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Set Value Concept Histogram for subset", eventmessage: "Concept:" + params.concept_key, accesstime: new java.util.Date()).save()

        // We retrieve the result instance ids from the client
        def concept = params.concept_key ?: null
        def concepts = [:]

        // We retrieve the omics parameters from the client, if they were passed
        def omics_params = [:]
        if (params.omics_value_type != null) {
            omics_params.omics_value_type = params.omics_value_type
            omics_params.omics_projection_type = params.omics_projection_type
            omics_params.omics_selector = params.omics_selector
            omics_params.omics_platform = params.omics_platform
        }
        else {
            omics_params = null
        }

        // Collect concept information
        concept = chartService.getConceptAnalysis(concept: i2b2HelperService.getConceptKeyForAnalysis(concept), omics_params: omics_params, subsets: chartService.getSubsetsFromRequest(params), chartSize : [width : 245, height : 180])

        PrintWriter pw = new PrintWriter(response.getOutputStream());
        pw.write(concept.commons.conceptHisto)
        pw.flush();
    }

    def conceptDistributionValues = {
        // Lets put a bit of 'audit' in here
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Concept Values", eventmessage: "Concept:" + params.concept_key, accesstime: new java.util.Date()).save()

        // We retrieve the result instance ids from the client
        def concept = params.concept_key ?: null
        def concepts = [:]

        // We retrieve the omics parameters from the client, if they were passed
        def omics_params = [:]
        if (params.omics_value_type != null) {
            omics_params.omics_value_type = params.omics_value_type
            omics_params.omics_projection_type = params.omics_projection_type
            omics_params.omics_selector = params.omics_selector
            omics_params.omics_platform = params.omics_platform
        }
        else {
            omics_params = null
        }

        // Collect concept information
        concept = chartService.getConceptAnalysis(concept: i2b2HelperService.getConceptKeyForAnalysis(concept), omics_params: omics_params, subsets: [ 1: [ exists: true, instance : "" ], 2: [ exists: false ], commons: [:]], chartSize : [width : 245, height : 180])


        render concept[1].conceptData as JSON
    }

    /**
     * Gets an analysis for a concept key and comparison
     */
    def analysis = {

        // Lets put a bit of 'audit' in here
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Analysis by Concept", eventmessage: "RID1:" + params.result_instance_id1 + " RID2:" + params.result_instance_id2 + " Concept:" + params.concept_key, accesstime: new java.util.Date()).save()

        // We retrieve the result instance ids from the client
        def concept = params.concept_key ?: null
        def concepts = [:]

        // We retrieve the omics parameters from the client, if they were passed
        def omics_params = [:]
        if (params.omics_value_type != null) {
            omics_params.omics_value_type = params.omics_value_type
            omics_params.omics_projection_type = params.omics_projection_type
            omics_params.omics_selector = params.omics_selector
            omics_params.omics_platform = params.omics_platform
        }
        else {
            omics_params = null
        }

        // We add the key to our cache set
        chartService.keyCache.add(concept)

        // Collect concept information
        concepts[concept] = chartService.getConceptAnalysis(concept: i2b2HelperService.getConceptKeyForAnalysis(concept), omics_params: omics_params, subsets: chartService.getSubsetsFromRequest(params))

        // Time to delivery !
        render(template: "conceptsAnalysis", model: [concepts: concepts])
    }

    /**
     * Action to get the basic statistics for the subset comparison and render them
     */
    def basicStatistics = {

        // Lets put a bit of 'audit' in here
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Basic Statistics", eventmessage: "RID1:" + params.result_instance_id1 + " RID2:" + params.result_instance_id2, accesstime: new java.util.Date()).save()

        // We clear the keys in our cache set
        chartService.keyCache.clear()

        // This clears the current session grid view data table and key cache
        request.session.setAttribute("gridtable", null);

        // We retrieve all our charts from our ChartService
        def subsets = chartService.computeChartsForSubsets(chartService.getSubsetsFromRequest(params))
        def concepts = chartService.getConceptsForSubsets(subsets)
        concepts.putAll(chartService.getHighDimensionalConceptsForSubsets(subsets))

        // Time to delivery !
        render(template: "summaryStatistics", model: [subsets: subsets, concepts: concepts])
    }

    def analysisGrid = {

        String concept_key = params.concept_key;
        def result_instance_id1 = params.result_instance_id1;
        def result_instance_id2 = params.result_instance_id2;
        def omics_selector = params.omics_selector ?: null;
        def omics_value_type = params.omics_value_type ?: null;
        def omics_projection_type = params.omics_projection_type ?: null;

        /*which subsets are present? */
        boolean s1 = (result_instance_id1 == "" || result_instance_id1 == null) ? false : true;
        boolean s2 = (result_instance_id2 == "" || result_instance_id2 == null) ? false : true;

        def al = new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Grid Analysis Drag", eventmessage: "RID1:" + result_instance_id1 + " RID2:" + result_instance_id2 + " Concept:" + concept_key, accesstime: new java.util.Date())
        al.save()

        //XXX: session is a questionable place to store this because it breaks multi-window/tab nav
        ExportTableNew table = (ExportTableNew) request.getSession().getAttribute("gridtable");
        if (table == null) {

            table = new ExportTableNew();
            if (s1) i2b2HelperService.addAllPatientDemographicDataForSubsetToTable(table, result_instance_id1, "subset1");
            if (s2) i2b2HelperService.addAllPatientDemographicDataForSubsetToTable(table, result_instance_id2, "subset2");

            List<String> keys = i2b2HelperService.getConceptKeysInSubsets(result_instance_id1, result_instance_id2);
            Set<String> uniqueConcepts = i2b2HelperService.getDistinctConceptSet(result_instance_id1, result_instance_id2);

            log.debug("Unique concepts: " + uniqueConcepts);
            log.debug("keys: " + keys)

            for (int i = 0; i < keys.size(); i++) {

                if (!i2b2HelperService.isHighDimensionalConceptKey(keys.get(i))) {
                    log.trace("adding concept data for " + keys.get(i));
                    if (s1) i2b2HelperService.addConceptDataToTable(table, keys.get(i), result_instance_id1);
                    if (s2) i2b2HelperService.addConceptDataToTable(table, keys.get(i), result_instance_id2);
                }
            }

            def highDimConcepts = omicsQueryService.getHighDimensionalConceptSet(result_instance_id1, result_instance_id2)
            highDimConcepts.each {
                if (s1) omicsQueryService.addHighDimConceptDataToTable(table, it, result_instance_id1)
                if (s2) omicsQueryService.addHighDimConceptDataToTable(table, it, result_instance_id2)
            }
        }
        PrintWriter pw = new PrintWriter(response.getOutputStream());

        if (concept_key && !concept_key.isEmpty()) {

            if (omics_value_type == null) {
                String parentConcept = i2b2HelperService.lookupParentConcept(i2b2HelperService.keyToPath(concept_key));
                Set<String> cconcepts = i2b2HelperService.lookupChildConcepts(parentConcept, result_instance_id1, result_instance_id2);

                def conceptKeys = [];
                def prefix = concept_key.substring(0, concept_key.indexOf("\\", 2));

                if (!cconcepts.isEmpty()) {
                    for (cc in cconcepts) {
                        def ck = prefix + i2b2HelperService.getConceptPathFromCode(cc);
                        conceptKeys.add(ck);
                    }
                } else
                    conceptKeys.add(concept_key);

                for (ck in conceptKeys) {
                    if (s1) i2b2HelperService.addConceptDataToTable(table, ck, result_instance_id1);
                    if (s2) i2b2HelperService.addConceptDataToTable(table, ck, result_instance_id2);
                }
            }

            else {
                def omics_params = [:]
                omics_params.omics_value_type = omics_value_type
                omics_params.omics_projection_type = omics_projection_type
                omics_params.concept_key = concept_key
                omics_params.omics_selector = omics_selector
                if (s1) omicsQueryService.addHighDimConceptDataToTable(table, omics_params, result_instance_id1)
                if (s2) omicsQueryService.addHighDimConceptDataToTable(table, omics_params, result_instance_id2)
            }

        }
        pw.write(table.toJSONObject().toString(5));
        pw.flush();

        request.getSession().setAttribute("gridtable", table);
    }

    def clearGrid = {
        log.debug("Clearing grid");
        request.getSession().setAttribute("gridtable", null);
        log.debug("Setting export filename to null, since there is nothing to export")
        request.getSession().setAttribute("expdsfilename", null);
        PrintWriter pw = new PrintWriter(response.getOutputStream());
        response.setContentType("text/plain");
        pw.write("grid cleared!");
        pw.flush();
    }


    def exportGrid = {
        byte[] bytes = ((ExportTableNew) request.getSession().getAttribute("gridtable")).toCSVbytes();
        int outputSize = bytes.length;
        //response.setContentType("application/vnd.ms-excel");
        response.setContentType("text/csv");
        response.setHeader("Content-disposition", "attachment; filename=" + "export.csv");
        response.setContentLength(outputSize);
        ServletOutputStream servletoutputstream = response.getOutputStream();
        servletoutputstream.write(bytes);
        servletoutputstream.flush();
    }
}

