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

        String concept_key = params.concept_key;
        def al = new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Set Value Concept Histogram", eventmessage: "Concept:" + concept_key, accesstime: new java.util.Date())
        al.save()
        def concept_cd = i2b2HelperService.getConceptCodeFromKey(concept_key);
        def concept_name = i2b2HelperService.getShortNameFromKey(concept_key);

        double[] values = i2b2HelperService.getConceptDistributionDataForValueConcept(concept_key);
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("H1", values, 10, StatHelper.min(values), StatHelper.max(values));

        JFreeChart chart = ChartFactory.createHistogram(
                "Histogram of " + concept_name + " for all",
                null,
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 12));
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setForegroundAlpha(0.85f);

        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();

        renderer.setDrawBarOutline(false);
        // flat bars look best...
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setShadowVisible(false);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        //domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
        PrintWriter pw = new PrintWriter(response.getOutputStream());

        String filename = ServletUtilities.saveChartAsJPEG(chart, 245, 180, info, request.getSession());
        String graphURL = request.getContextPath() + "/chart/displayChart?filename=" + filename;

        //  Write the image map to the PrintWriter
        //pw.write("<html><body>");
        pw.write("<img src='" + graphURL + "' width=245 height=180 border=0 usemap='#" + filename + "'>");
        ChartUtilities.writeImageMap(pw, filename, info, false);
        pw.flush();
    }


    def conceptDistributionForSubset = {
        String concept_key = params.concept_key;
        def al = new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Set Value Concept Histogram for subset", eventmessage: "Concept:" + concept_key, accesstime: new java.util.Date())
        al.save()
        def result_instance_id1 = params.result_instance_id1;
        def result_instance_id2 = params.result_instance_id2;
        def concept_cd = i2b2HelperService.getConceptCodeFromKey(concept_key);
        def concept_name = i2b2HelperService.getShortNameFromKey(concept_key);

        if (result_instance_id1 != "" && result_instance_id1 != null) {
            double[] values2 = i2b2HelperService.getConceptDistributionDataForValueConcept(concept_key, result_instance_id1);
            double[] values = i2b2HelperService.getConceptDistributionDataForValueConcept(concept_key);
            HistogramDataset dataset2 = new HistogramDataset();
            //changed following line from values2 min max to values to syncronize scales
            dataset2.addSeries("H1", values2, 10, StatHelper.min(values), StatHelper.max(values));
            dataset2.addSeries("H2", values, 10, StatHelper.min(values), StatHelper.max(values));
            JFreeChart chart2 = ChartFactory.createHistogram(
                    "Histogram of " + concept_name + " for subset",
                    null,
                    "Count",
                    dataset2,
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    false
            );
            chart2.getTitle().setFont(new Font("SansSerif", Font.BOLD, 12));
            XYPlot plot2 = (XYPlot) chart2.getPlot();
            plot2.setForegroundAlpha(0.85f);

            XYBarRenderer renderer2 = (XYBarRenderer) plot2.getRenderer();
            renderer2.setDrawBarOutline(false);
            renderer2.setSeriesVisible(1, false);
            // flat bars look best...
            renderer2.setBarPainter(new StandardXYBarPainter());
            renderer2.setShadowVisible(false);

            NumberAxis rangeAxis2 = (NumberAxis) plot2.getRangeAxis();
            rangeAxis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

            NumberAxis domainAxis2 = (NumberAxis) plot2.getDomainAxis();
            //domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

            ChartRenderingInfo info2 = new ChartRenderingInfo(new StandardEntityCollection());

            String filename2 = ServletUtilities.saveChartAsJPEG(chart2, 245, 180, info2, request.getSession());
            String graphURL2 = request.getContextPath() + "/chart/displayChart?filename=" + filename2;

            PrintWriter pw = new PrintWriter(response.getOutputStream());
            pw.write("<img src='" + graphURL2 + "' width=245 height=180 border=0 usemap='#" + filename2 + "'>");
            ChartUtilities.writeImageMap(pw, filename2, info2, false);
            pw.flush();
        }
    }

    /**
     * Gets an analysis for a concept key and comparison
     */
    def analysis = {

        // Lets put a bit of 'audit' in here
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Analysis by Concept", eventmessage: "RID1:" + params.result_instance_id1 + " RID2:" + params.result_instance_id2 + " Concept:" + concept, accesstime: new java.util.Date()).save()

        // We retrieve the result instance ids from the client
        def concept = params.concept_key ?: null
        def concepts = [:]

        // Collect concept information
        concepts[concept] = getConceptAnalysis(concept: i2b2HelperService.getConceptKeyForAnalysis(concept), subsets: ChartService.getSubsetsFromRequest(params))

        // Time to delivery !
        render(template: "conceptsAnalysis", model: [concepts: concepts])
    }

    /**
     * Action to get the basic statistics for the subset comparison and render them
     */
    def basicStatistics = {

        // This clears the current session concept grid
        request.getSession().setAttribute("gridtable", null);

        // Lets put a bit of 'audit' in here
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Basic Statistics", eventmessage: "RID1:" + params.result_instance_id1 + " RID2:" + params.result_instance_id2, accesstime: new java.util.Date()).save()

        // We retrieve all our charts from our ChartService
        def subsets = chartService.computeChartsForSubsets(chartService.getSubsetsFromRequest(params))
        def concepts = chartService.getConceptsForSubsets(subsets)

        // Time to delivery !
        render(template: "summaryStatistics", model: [subsets: subsets, concepts: concepts])
    }

    def analysisGrid = {

        String concept_key = params.concept_key;
        def result_instance_id1 = params.result_instance_id1;
        def result_instance_id2 = params.result_instance_id2;

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

                log.trace("adding concept data for " + keys.get(i));
                if (s1) i2b2HelperService.addConceptDataToTable(table, keys.get(i), result_instance_id1);
                if (s2) i2b2HelperService.addConceptDataToTable(table, keys.get(i), result_instance_id2);
            }
        }
        PrintWriter pw = new PrintWriter(response.getOutputStream());

        if (concept_key && !concept_key.isEmpty()) {

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

