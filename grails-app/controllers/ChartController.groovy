import com.recomdata.charting.PieRenderer
import com.recomdata.export.ExportTableNew
import com.recomdata.statistics.StatHelper
import grails.converters.JSON
import org.apache.commons.lang.ArrayUtils
import org.apache.commons.math.stat.inference.TestUtils
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartRenderingInfo
import org.jfree.chart.ChartUtilities
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.AxisLocation
import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.entity.StandardEntityCollection
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator
import org.jfree.chart.labels.StandardCategoryToolTipGenerator
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.plot.PiePlot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer
import org.jfree.chart.renderer.xy.StandardXYBarPainter
import org.jfree.chart.renderer.xy.XYBarRenderer
import org.jfree.chart.servlet.ChartDeleter
import org.jfree.chart.servlet.ServletUtilities
import org.jfree.chart.title.TextTitle
import org.jfree.data.Range
import org.jfree.data.RangeType
import org.jfree.data.category.CategoryDataset
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import org.jfree.data.general.PieDataset
import org.jfree.data.statistics.*
import org.transmart.searchapp.AccessLog
import org.transmart.searchapp.AuthUser

import javax.servlet.ServletException
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.awt.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.List

//import edu.mit.wi.haploview.*;
class ChartController {

    def index = {}

    def i2b2HelperService
    def springSecurityService


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

        String concept_key = params.concept_key;
        def result_instance_id1 = params.result_instance_id1;
        def result_instance_id2 = params.result_instance_id2;
        def al = new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Analysis by Concept", eventmessage: "RID1:" + result_instance_id1 + " RID2:" + result_instance_id2 + " Concept:" + concept_key, accesstime: new java.util.Date())
        al.save()

        String analysis_key = i2b2HelperService.getConceptKeyForAnalysis(concept_key);
        PrintWriter pw = new PrintWriter(response.getOutputStream());
        pw.write("<html><head><link rel='stylesheet' type='text/css' href='${resource(dir: 'css', file: 'chartservlet.css')}'></head><body><div class='analysis'>");
        //renderConceptAnalysis(analysis_key, result_instance_id1, result_instance_id2, pw, request);
        log.debug("in analysis controller about to run render concept: " + analysis_key + " result_instance_id1:" + result_instance_id1);

        // need to modify to try looking for equivalent concepts on both subsets
        //String parentConcept = i2b2HelperService.lookupParentConcept(i2b2HelperService.keyToPath(concept_key));
        //log.debug("parent concept: "+parentConcept);

        //Set<String> cconcepts = i2b2HelperService.lookupChildConcepts(parentConcept, result_instance_id1, result_instance_id2);
        //if (cconcepts.isEmpty()) {
        //		cconcepts.add(concept_key);
        //	}

        //	log.debug("child concepts: "+cconcepts);

        log.debug("calling renderConceptAnalysisNew from analysis with analysis_key:" + analysis_key);
        renderConceptAnalysisNew(analysis_key, result_instance_id1, result_instance_id2, pw, request);
        pw.write("<hr>");
        if (!i2b2HelperService.isLeafConceptKey(analysis_key)) //must be a folder so render all the value children
        {
            log.debug("iterating through all items in folder")

            for (String c : i2b2HelperService.getChildValueConceptsFromParentKey(concept_key)) {
                log.debug("-- rendering " + c)
                //logMessage("child key:"+c);
                renderConceptAnalysisNew(c, result_instance_id1, result_instance_id2, pw, request);
                pw.write("<hr>");
            }


        }
        //renderPatientCountInfoTable(result_instance_id1, result_instance_id2, pw);
        pw.write("</div></body></html>");
        pw.flush();
    }

    /**
     * Action to get the basic statistics for the subset comparison and render them
     */
    def basicStatistics = {

        // This clears the current session concept grid
        request.getSession().setAttribute("gridtable", null);

        // We retrieve the result instance ids from the client
        def result_instance_id1 = params.result_instance_id1 ?: null;
        def result_instance_id2 = params.result_instance_id2 ?: null;

        // Lets put a bit of 'audit' in here
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Basic Statistics", eventmessage: "RID1:" + result_instance_id1 + " RID2:" + result_instance_id2, accesstime: new java.util.Date()).save()

        // We create our subset reference Map
        def subsets = [
            1: [ exists: !(result_instance_id1 == null || result_instance_id1 == ""), instance: result_instance_id1],
            2: [ exists: !(result_instance_id2 == null || result_instance_id1 == ""), instance: result_instance_id2],
            commons: [:]
        ]

        // We intend to use some legacy functions that are used elsewhere
        // We need to use a printer for this
        StringWriter output = new StringWriter()
        PrintWriter writer = new PrintWriter(output)

        // We want to automatically clear the output buffer as we go
        output.metaClass.toStringAndFlush = {
            def tmp = buf.toString()
            buf.setLength(0)
            tmp
        }

        // We need to run some common statistics first
        // This must be changed for multiple (>2) cohort selection
        // We grab the intersection count for our two cohort
        if (subsets[2].exists)
            subsets.commons.patientIntersectionCount = i2b2HelperService.getPatientSetIntersectionSize(subsets[1].instance, subsets[2].instance)

        // Lets prepare our subset shared diagrams, we will fill them later
        HistogramDataset ageHistoHandle = new HistogramDataset();
        DefaultBoxAndWhiskerCategoryDataset agePlotHandle = new DefaultBoxAndWhiskerCategoryDataset();

        // We also create a map to store image paths
        def graphs = [:]

        subsets.findAll { n, p ->
            p.exists
        }.each { n, p ->

            // First we get the Query Definition
            i2b2HelperService.renderQueryDefinition(p.instance, "Query Summary for Subset ${n}", writer)
            p.query = output.toStringAndFlush()

            // Lets fetch the patient count
            p.patientCount = i2b2HelperService.getPatientSetSize(p.instance)

            // Getting the age data
            p.ageData = i2b2HelperService.getPatientDemographicValueDataForSubset("AGE_IN_YEARS_NUM", p.instance)
            ageHistoHandle.addSeries("Subset $n", p.ageData, 10, StatHelper.min(p.ageData), StatHelper.max(p.ageData));
            p.ageStats = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(Arrays.asList(ArrayUtils.toObject(p.ageData)))
            agePlotHandle.add(p.ageStats, "Series $n", "Subset $n")

            // Sex chart has to be generated for each subset
            p.sexData = i2b2HelperService.getPatientDemographicDataForSubset("sex_cd", p.instance)
            JFreeChart sexPieChart = createConceptAnalysisPieChart(hashMapToPieDataset(p.sexData, "Sex"), "Sex");
            String sexPieFile = ServletUtilities.saveChartAsJPEG(sexPieChart, 300, 200, new ChartRenderingInfo(new StandardEntityCollection()), request.getSession())
            graphs."sexPie$n" = request.getContextPath() + "/chart/displayChart?filename=" + sexPieFile

            // Same thing for Race chart
            p.raceData = i2b2HelperService.getPatientDemographicDataForSubset("race_cd", p.instance)
            JFreeChart racePieChart = createConceptAnalysisPieChart(hashMapToPieDataset(p.raceData, "Race"), "Race");
            String racePieFile = ServletUtilities.saveChartAsJPEG(racePieChart, 300, 200, new ChartRenderingInfo(new StandardEntityCollection()), request.getSession())
            graphs."racePie$n" = request.getContextPath() + "/chart/displayChart?filename=" + racePieFile

        }

        // Lets build our age diagrams now that we have all the points in
        JFreeChart ageHistoChart = ChartFactory.createHistogram("Histogram of Age", null, "Count", ageHistoHandle, PlotOrientation.VERTICAL, true, true, false)
        String ageHistoFile = ServletUtilities.saveChartAsJPEG(ageHistoChart, 245, 180, new ChartRenderingInfo(new StandardEntityCollection()), request.getSession())
        graphs.ageHisto = request.getContextPath() + "/chart/displayChart?filename=" + ageHistoFile

        JFreeChart agePlotChart = ChartFactory.createBoxAndWhiskerChart("Comparison of Age", "", "Value", agePlotHandle, false)
        String agePlotFile = ServletUtilities.saveChartAsJPEG(agePlotChart, 200, 300, new ChartRenderingInfo(new StandardEntityCollection()), request.getSession())
        graphs.agePlot = request.getContextPath() + "/chart/displayChart?filename=" + agePlotFile

        // Time to delivery !
        render(template: "summaryStatistics", model: [subsets: subsets, graphs: graphs])
        return;

        renderCategoryResultsHashMap(sexs1, "Subset 1", i2b2HelperService.getPatientSetSize(result_instance_id1), pw);


        /*get all distinct  concepts for analysis from both subsets into hashmap*/
        List<String> keys = i2b2HelperService.getConceptKeysInSubsets(result_instance_id1, result_instance_id2);
        pw.write("<hr>");
        pw.write("<table width='100%'><tr><td align='center'><div class='analysistitle'>Analysis of concepts found in Subsets</div></td></tr></table>");
        pw.write("<hr>");
        /*Analyze each concept in subsets*/

        log.debug("Keys: " + keys);
        Set<String> distinctConcepts = i2b2HelperService.getDistinctConceptSet(result_instance_id1, result_instance_id2);
        Set<String> uniqueConcepts = new HashSet<String>();

        for (c in distinctConcepts) {
            String uKey = i2b2HelperService.getConceptKeyForAnalysis(c);
            uniqueConcepts.add(uKey);
        }

        log.debug("Unique concepts: " + uniqueConcepts);

        // for (int i = 0; i < keys.size(); i++)
        for (k in uniqueConcepts) {
            //String analysis_key = i2b2HelperService.getConceptKeyForAnalysis(keys.get(i));
            //String analysis_key = i2b2HelperService.getConceptKeyForAnalysis(k);
            String analysis_key = k;
            if (analysis_key.indexOf("SECURITY") > -1) {
                continue;
            }
            //log.trace("Analysis Key: "+i+", "+analysis_key);
            log.debug("calling renderConceptAnalysisNew from basic statistics:\tk:" + k + "\tanalysis_key:" + analysis_key);
            renderConceptAnalysisNew(analysis_key, result_instance_id1, result_instance_id2, pw, request);
            pw.write("<hr>");
        }
        /*test harness*//*
        ExportTableNew table=new ExportTableNew();
        addAllPatientDemographicDataForSubsetToTable(table, result_instance_id1, "subset1");
        addAllPatientDemographicDataForSubsetToTable(table, result_instance_id2, "subset2");
        for(int i=0;i<keys.size();i++)
        {
            addConceptDataToTable(table, keys.get(i), result_instance_id1);
            addConceptDataToTable(table, keys.get(i), result_instance_id2);
        }
        pw.write("<pre>");
        pw.write(table.toJSONObject().toString(5));
        pw.write("</pre>");*/
        /*end test*/
        pw.write("</div></body></html>");
        pw.flush();
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

    private void renderCategoryResultsHashMap(HashMap<String, Integer> results, String title, Integer totalsubjects, PrintWriter pw) {
        pw.write("<table class='analysis'>");
        int mapsize = results.size();
        int total = 0;
        NumberFormat form;
        form = NumberFormat.getPercentInstance();
        form.setMaximumFractionDigits(1);
        Iterator keyValuePairs1 = results.entrySet().iterator();
        //get the sum of the results so i can do percents
        for (int i = 0; i < mapsize; i++) {
            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) keyValuePairs1.next();
            Integer value = entry.getValue();
            total = total + value;
        }
        Iterator keyValuePairs2 = results.entrySet().iterator();
        pw.write("<tr>"
                + "<th>Category</th>"
                + "<th>" + title + " (n)</th>"
                + "<th>" + title + " (%n)</th>"
                // + "<th>" + title + " (% " + totalsubjects + " Subjects)</th>"
                + "</tr>"
        );
        for (int i = 0; i < mapsize; i++) {
            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) keyValuePairs2.next();
            String key = entry.getKey();
            Integer value = entry.getValue();
            Double test = ((double) value / (double) totalsubjects);
            pw.write("<tr>"
                    + "<td>" + key + "</td>"
                    + "<td>" + value.toString() + "</td>"
                    + "<td>" + form.format((total == 0) ? 0 : ((double) value / (double) total)) + "</td>"
                    // + "<td>" + form.format(test) + "</td>"
                    + "</tr>"
            );
        }
        pw.write("<tr>"
                + "<td><b>Total</b></td>"
                + "<td><b>" + total + "</b></td>"
                + "<td><b>" + form.format((total == 0) ? 0 : ((double) total / (double) total)) + "</b></td>"
                // + "<td><b>N/A</b></td>"
                + "</tr>"
        );
        pw.write("</table>");
    }

    private String sigTestRounder(double val) {
        return (String.format("%.5g%n", val));
    }

    private void renderChiSquaredHashMap(
            HashMap<String, Integer> results1,
            HashMap<String, Integer> results2,
            PrintWriter pw) {
        // Prints the p-value from a chi-squared significance test
        // for two sets of categorical results

        if ((results1 != null) && (results2 != null)) {
            log.trace("RENDERING ChiSquaredTest")

            Iterator iter1 = results1.keySet().iterator();

            ArrayList<Integer> al1 = new ArrayList(results1.size());
            ArrayList<Integer> al2 = new ArrayList(results2.size());

            // check to make sure that the groups aren't disjoint and
            // add elements to al1 and al2:
            boolean chk = false;
            while (iter1.hasNext()) {
                String key = iter1.next();
                log.debug("examining: " + results1.get(key) + ", " + results2.get(key));
                if (results1.get(key) > 0 && results2.get(key) > 0) {
                    chk = true;
                }
                if (results1.get(key) != 0 || results2.get(key) != 0) {
                    log.debug("adding to lists: " + results1.get(key) + ", " + results2.get(key));
                    al1.add(results1.get(key));
                    al2.add(results2.get(key));
                }
            }

            if (!chk) {
                log.trace("No chi-squared test calculated: disjoint sets.");
                pw.write("No chi-squared test calculated: disjoint sets.");
            } else if (Arrays.equals(al1.toArray(), al2.toArray())) {
                log.trace("No chi-squared test calculated: subset sizes are identical.");
                pw.write("No chi-squared test calculated: subset sizes are the same.")
            } else if (results1.size() != results2.size()) {
                log.trace("No chi-squared test calculated: different dimensions.");
                pw.write("No chi-squared test calculated: different dimensions.");
            } else {

                try {
                    long[][] counts = [al1.toArray(), al2.toArray()];

                    double chiSquareValue = TestUtils.chiSquare(counts);
                    double pValue = TestUtils.chiSquareTest(counts);
                    boolean isSignificant = TestUtils.chiSquareTest(counts, 0.05);

                    log.trace("Chi-Squared: " + sigTestRounder(chiSquareValue));
                    log.trace("Chi-Squared Test P Value: " + sigTestRounder(pValue));
                    log.trace("Significant at 95%: " + isSignificant);

                    pw.write("<p><table class=\"analysis\">");
                    pw.write("<tr><td> <b>Chi-Squared:<b></td><td>" + sigTestRounder(chiSquareValue) + "</td></tr>");
                    pw.write("<tr><td> <b>p-value:<b></td><td>" + sigTestRounder(pValue) + "</td></tr>");

                    pw.write("</tr></table>");

                    if (isSignificant) {
                        pw.write("The results are significant at a 95% confidence level.");
                    } else {
                        pw.write("The results are <i>not</i> significant at a 95% confidence level.");
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }

    private void renderTTestHashMap(
            HashMap<String, Integer> results1,
            HashMap<String, Integer> results2,
            PrintWriter pw) {
        // Prints the p-value from a t test
        // for two sets of categorical results

        if ((!results1.isEmpty()) && (!results2.isEmpty())) {
            log.trace("RENDERING TTest")

            Collection c1 = results1.values();
            Collection c2 = results2.values();

            double[] s1;
            double[] s2;

            for (key1 in results1.keySet()) {
                s1 = results1[key1];
            }

            for (key2 in results2.keySet()) {
                s2 = results2[key2];
            }

            log.debug("s1: " + s1);
            log.debug("s2: " + s2);

            if (Arrays.equals(s1, s2)) {
                log.trace("No t test performed; subsets are identical.");
                pw.write("No significance tests were calculated; subsets are identical.")
            } else if (s1.size() < 2 || s2.size() < 2) {
                pw.write("No significance tests were calculated; one subset was too small to calculate statistics.")
            } else {

                double tStatistic = TestUtils.t(s1, s2);
                double pValue = TestUtils.tTest(s1, s2);
                boolean isSignificant = TestUtils.tTest(s1, s2, 0.05);

                log.trace("t statistic: " + tStatistic);
                log.trace("T-Test P Value: " + pValue);
                log.trace("Significant at 95%: " + isSignificant);

                pw.write("<p><table class=\"analysis\">");
                pw.write("<tr><td> <b>t statistic:<b></td><td>" + sigTestRounder(tStatistic) + "</td></tr>");
                pw.write("<tr><td> <b>p-value:<b></td><td>" + sigTestRounder(pValue) + "</td></tr>");
                pw.write("</table>");

                if (isSignificant) {
                    pw.write("The results are significant at a 95% confidence level.");
                } else {
                    pw.write("The results are <i>not</i> significant at a 95% confidence level.");
                }
            }
        }
    }

    private CategoryDataset hashMapToCategoryDataset(HashMap<String, Integer> results, String seriesname) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int mapsize = results.size();
        Iterator keyValuePairs1 = results.entrySet().iterator();
        for (int i = 0; i < mapsize; i++) {
            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) keyValuePairs1.next();
            String key = entry.getKey();
            Integer value = entry.getValue();
            if (key != null) {
                dataset.addValue(value, seriesname, key);
            }
        }
        return dataset;
    }

    private PieDataset hashMapToPieDataset(HashMap<String, Integer> results, String seriesname) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        int mapsize = results.size();
        double total = 0;
/*Iterator keyValuePairs1 = results.entrySet().iterator();
//get the sum of the results so i can do percents
for (int i = 0; i < mapsize; i++)
{
  Map.Entry<String,Integer> entry = (Map.Entry<String,Integer>) keyValuePairs1.next();
  Integer value = entry.getValue();
  total=total+value;
}*/
        Iterator keyValuePairs2 = results.entrySet().iterator();
        for (int i = 0; i < mapsize; i++) {
            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) keyValuePairs2.next();
            String key = entry.getKey();
            Integer value = entry.getValue();
            if (key != null) {
                dataset.setValue(key, (double) (value/*/total*/));
            }
        }
        return dataset;
    }


    private JFreeChart createConceptAnalysisBarChart(CategoryDataset dataset, String title) {
        // create the chart...
        JFreeChart chart = ChartFactory.createBarChart(
                title,  // chart title
                "",                  // domain axis label
                "", // range axis label
                dataset,                     // data
                PlotOrientation.HORIZONTAL,  // orientation
                false,                        // include legend
                true,
                false
        );
        /*chart.addSubtitle(new TextTitle(
                "Source: http://www.homeoffice.gov.uk/rds/pdfs2/r188.pdf",
                new Font("Dialog", Font.ITALIC, 10)));*/
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 12));
        CategoryPlot plot = (CategoryPlot) chart.getPlot();

        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setItemLabelAnchorOffset(9.0);
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(
                new StandardCategoryItemLabelGenerator());
        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator(
                "{0}, {1} = {2}", new DecimalFormat("0")));
        renderer.setDrawBarOutline(true);
        renderer.setBaseOutlinePaint(Color.white);
        renderer.setBaseOutlineStroke(new BasicStroke(0.5f));
        CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis();
        domainAxis.setLabel("Concept");
        domainAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        Range r = rangeAxis.getRange();
        Range s = new Range(0, r.getUpperBound() + r.getUpperBound() * 0.15);
        rangeAxis.setRange(s);
        rangeAxis.setRangeType(RangeType.POSITIVE);
        rangeAxis.setLabel("Count of Observations");
        rangeAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        //ChartUtilities.applyCurrentTheme(chart);
        return chart;
    }

    private JFreeChart createConceptAnalysisPieChart(PieDataset dataset, String title) {
// create the chart...
        JFreeChart chart = ChartFactory.createPieChart(
                title,  // chart title
                dataset,             // data
                false,                // include legend
                true,
                false);
        TextTitle mytitle = chart.getTitle();
        mytitle.setToolTipText("A title tooltip!");
        mytitle.setFont(new Font("SansSerif", Font.BOLD, 12));

        PiePlot plot = (PiePlot) chart.getPlot();
        Color[] colors = [Color.blue, Color.red,
                          Color.green, Color.yellow, Color.WHITE, Color.orange, Color.PINK, Color.darkGray];

        /* Delegating the choice of color to an inner class */
        PieRenderer renderer = new PieRenderer(colors);
        renderer.setColor(plot, dataset);
        plot.setIgnoreNullValues(true);
        plot.setIgnoreZeroValues(true);
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        plot.setNoDataMessage("No data available");
        plot.setCircular(true);
        plot.setLabelGap(0.02);
        return chart;
    }

    private void renderConceptAnalysisNew(String concept_key, String result_instance_id1, String result_instance_id2, PrintWriter pw, HttpServletRequest request) {
        log.debug("renderConceptAnalysisNew: rendering " + concept_key)
        try {
            log.debug("Rendering concept analysis for concept key: " + concept_key)
            /*get variables*/
            String concept_cd = null;
            String concept_name = null;
            concept_cd = i2b2HelperService.getConceptCodeFromKey(concept_key);
            concept_name = i2b2HelperService.getShortNameFromKey(concept_key);
            log.debug("concept:" + concept_key + " - concept_cd " + concept_cd);

            StringWriter sw1 = new StringWriter();
            StringWriter sw2 = new StringWriter();
            /*which subsets are present? */
            boolean s1 = true;
            boolean s2 = true;
            if (result_instance_id1 == "" || result_instance_id1 == null) {
                s1 = false;
            }
            if (result_instance_id2 == "" || result_instance_id2 == null) {
                s2 = false;
            }

            if (i2b2HelperService.isValueConceptCode(concept_cd)) {

                /*get the data*/
                String parentConcept = i2b2HelperService.lookupParentConcept(i2b2HelperService.keyToPath(concept_key));
                Set<String> childConcepts = new HashSet<String>();
                if (parentConcept == null) {
                    childConcepts.add(concept_cd);
                } else {
                    childConcepts.addAll(i2b2HelperService.lookupChildConcepts(parentConcept, result_instance_id1, result_instance_id2));
                }

                ArrayList<Double> valuesAlist3 = new ArrayList<Double>();
                ArrayList<Double> valuesAlist4 = new ArrayList<Double>();

                log.debug("A iterating through child concepts");
                for (c in childConcepts) {
                    log.debug("\tc: " + c);
                    valuesAlist3.addAll(i2b2HelperService.getConceptDistributionDataForValueConceptFromCode(c, result_instance_id1));
                    log.trace("added to values3");
                    valuesAlist4.addAll(i2b2HelperService.getConceptDistributionDataForValueConceptFromCode(c, result_instance_id2));
                    log.trace("added to values4");
                }

                log.debug("\tA done iterating through child concepts");

                log.debug("\tvaluesAlist3:" + valuesAlist3);
                log.debug("\tvaluesAlist4:" + valuesAlist4);

                double[] values3 = valuesAlist3.toArray();
                double[] values4 = valuesAlist4.toArray();

                //double[] values3=i2b2HelperService.getConceptDistributionDataForValueConcept(concept_key, result_instance_id1);
                //double[] values4=i2b2HelperService.getConceptDistributionDataForValueConcept(concept_key, result_instance_id2);

                /*render the double histogram*/
                HistogramDataset dataset3 = new HistogramDataset();
                if (s1) {
                    dataset3.addSeries("Subset 1", values3, 10, StatHelper.min(values3), StatHelper.max(values3));
                }
                if (s2) {
                    dataset3.addSeries("Subset 2", values4, 10, StatHelper.min(values4), StatHelper.max(values4));
                }
                JFreeChart chart3 = ChartFactory.createHistogram(
                        "Histogram of " + concept_name,
                        null,
                        "Count",
                        dataset3,
                        PlotOrientation.VERTICAL,
                        true,
                        true,
                        false
                );
                chart3.getTitle().setFont(new Font("SansSerif", Font.BOLD, 12));
                XYPlot plot3 = (XYPlot) chart3.getPlot();
                plot3.setForegroundAlpha(0.85f);

                XYBarRenderer renderer3 = (XYBarRenderer) plot3.getRenderer();
                renderer3.setDrawBarOutline(false);
                // flat bars look best...
                renderer3.setBarPainter(new StandardXYBarPainter());
                renderer3.setShadowVisible(false);

                NumberAxis rangeAxis3 = (NumberAxis) plot3.getRangeAxis();
                rangeAxis3.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

                NumberAxis domainAxis3 = (NumberAxis) plot3.getDomainAxis();
                //domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

                ChartRenderingInfo info3 = new ChartRenderingInfo(new StandardEntityCollection());

                String filename3 = ServletUtilities.saveChartAsJPEG(chart3, 245, 180, info3, request.getSession());
                String graphURL3 = request.getContextPath() + "/chart/displayChart?filename=" + filename3;

                /*get the data*/
                /*
                    def results1;
                    if(s1){
                        log.trace("getting data for subset 1 in render value concept analysis")
                        results1=i2b2HelperService.getConceptDistributionDataForValueConceptByTrial(concept_key, result_instance_id1)
                    }

                    def results2;
                    if(s2){
                        log.trace("getting data for subset 1 in render value concept analysis")
                        results2=i2b2HelperService.getConceptDistributionDataForValueConceptByTrial(concept_key, result_instance_id2)
                    }
                */

                def results1;
                def results2;

                log.debug("B getting data distribution for child concepts by trial");

                results1 = i2b2HelperService.getConceptDistributionDataForValueConceptByTrialByConcepts(childConcepts, result_instance_id1);
                results2 = i2b2HelperService.getConceptDistributionDataForValueConceptByTrialByConcepts(childConcepts, result_instance_id2);

                log.debug("s1: " + s1 + ", s2: " + s2 + ", results1: " + results1 + ", results2: " + results2)
                log.debug("class of results1: " + results1.getClass() + ", class of results2:" + results2.getClass());

                log.trace(results1 as JSON)
                log.trace("I GOT HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                def width = 200;
                def offset = 40;
                DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
                BoxAndWhiskerItem boxitem;
                if (s1 && results1.size() > 0) {
                    log.debug("size of results1: " + results1.size());
                    for (key in results1.keySet()) {
                        boxitem = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(results1[key]);
                        dataset.add(boxitem, "Subset 1", key);
                        width = width + offset;
                        sw1.write("<td>")
                        renderBoxAndWhiskerInfoTableNew(results1[key], key, sw1);
                        sw1.write("</td>")
                    }
                    if (results1.size() > 1) {
                        // add an extra item with data for all trials
                        ArrayList<Number> vals = values3;
                        boxitem = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(vals);
                        dataset.add(boxitem, "Subset 1", "All trials");
                        width = width + offset;
                        sw1.write("<td>")
                        renderBoxAndWhiskerInfoTableNew(vals, "All trials", sw1);
                        sw1.write("</td>")
                    }
                } else {
                    log.debug("No result found for either " + concept_name + " or equivalent concepts for subset 1.")
                }
                if (s2 && results2.size() > 0) {
                    log.debug("size of results2: " + results2.size());
                    for (key in results2.keySet()) {
                        boxitem = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(results2[key]);
                        dataset.add(boxitem, "Subset 2", key);
                        width = width + offset;
                        sw2.write("<td>")
                        renderBoxAndWhiskerInfoTableNew(results2[key], key, sw2);
                        sw2.write("<td>")
                    }
                    if (results2.size() > 1) {
                        // add an extra item with data for all trials
                        ArrayList<Number> vals = values4;
                        boxitem = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(vals);
                        dataset.add(boxitem, "Subset 2", "All trials");
                        width = width + offset;
                        sw2.write("<td>")
                        renderBoxAndWhiskerInfoTableNew(vals, "All trials", sw2);
                        sw2.write("</td>")
                    }
                } else {
                    log.debug("No results found for either " + concept_name + " or equivalent concepts for subset 2.")
                }
                JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
                        "Comparison of " + concept_name, "Trial", "Value", dataset,
                        true);
                chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 12));
                CategoryPlot plot = (CategoryPlot) chart.getPlot();
                plot.setDomainGridlinesVisible(true);
                CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis();
                BoxAndWhiskerRenderer rend = (BoxAndWhiskerRenderer) plot.getRenderer();
                rend.setMaximumBarWidth(0.10);

                //adjust the width depending on number of sets

                NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                //rangeAxis7.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

                ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

                String filename = ServletUtilities.saveChartAsJPEG(chart, width, 300, info, request.getSession());
                String graphURL = request.getContextPath() + "/chart/displayChart?filename=" + filename;
                pw.write("<table>");
                pw.write("<tr><td align='center' colspan='5'><div class='analysistitle'>Analysis of " + concept_name + " for subsets:</div></td></tr>");
                pw.write("<tr>");
                pw.write("<td><img src='" + graphURL3 + "' width=245 height=180 border=0 usemap='#" + filename3 + "'>");
                ChartUtilities.writeImageMap(pw, filename3, info3, false);
                pw.write("</td>");
                pw.write("<td>");
                pw.write("<img src='" + graphURL + "' width=" + width + " height=300 border=0 usemap='#" + filename + "'>");
                ChartUtilities.writeImageMap(pw, filename, info, false);
                pw.write("</td>");
                String rmodulesVersion = grailsApplication.mainContext.pluginManager.getGrailsPlugin('rdc-rmodules').version;
                pw.write("<td valign='top'><div style='position:relative;left:-10px;'><a  href=\"javascript:showInfo('plugins/rdc-rmodules-$rmodulesVersion/help/boxplot.html');\"><img src=\"../images/information.png\"></a></div></td>");
                //Should be dynamic to plugin!
                pw.write("<td>")
                pw.write("<table><tr><td>");
                if (s1 && results1.size() > 0) {
                    pw.write("<table><tr><td colspan='" + results1.keySet().size() + "' align='center'>")
                    pw.write("<h2>Subset 1</h2>");
                    pw.write("</td></tr>");
                    pw.write("<tr><td><table><tr>");
                    pw.write(sw1.toString());
                    pw.write("</tr></table></td></tr></table>");
                } else {
                    log.debug("No results found for either " + concept_name + " or equivalent concepts for subset 1.")
                }
                pw.write("</td><td>");
                if (s2 && results2.size() > 0) {
                    pw.write("<table><tr><td colspan='" + results2.keySet().size() + "' align='center'>")
                    pw.write("<h2>Subset 2</h2>");
                    pw.write("</td></tr>");
                    pw.write("<tr><td><table><tr>");
                    pw.write(sw2.toString());
                    pw.write("</tr></table></td></tr></table>");
                } else {
                    log.debug("No results found for either " + concept_name + " or equivalent concepts for subset 1.")
                }
                pw.write("</td><tr><td align=\"center\" colspan=2>");

                // significance test
                renderTTestHashMap(results1, results2, pw);

                if (s1 && results1.size() == 0) {
                    pw.write("No results found for either " + concept_name + " or equivalent concepts for subset 1.")
                }
                if (s2 && results2.size() == 0) {
                    pw.write("No results found for either " + concept_name + " or equivalent concepts for subset 2.")
                }


                pw.write("</td></tr></td></tr></table></td></tr></table>")

            } else {
                log.debug("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                log.debug("Wasn't a value concept, doing a non-value concept analysis:")
                HashMap<String, Integer> results1;
                HashMap<String, Integer> results2;
                if (s1) {
                    results1 = i2b2HelperService.getConceptDistributionDataForConcept(concept_key, result_instance_id1);
                    log.debug("concept_key:" + concept_key + ", results1: " + results1);
                }
                if (s2) {
                    results2 = i2b2HelperService.getConceptDistributionDataForConcept(concept_key, result_instance_id2);
                    log.debug("concept_key:" + concept_key + ", results2: " + results2);
                }
                int height = 80 + 15 * results1.size();
                /*printHashMap(results1, pw);*/

                pw.write("<table width='100%'><tr>");
                pw.write("<tr><td align='center' colspan='2'><div class='analysistitle'>Analysis of " + concept_name + " for subsets:</div></td></tr>");
                pw.write("<tr><td width='50%'>");
                if (s1) {

                    JFreeChart chart5 = createConceptAnalysisBarChart(hashMapToCategoryDataset(results1, "Subset 1"), "Subset 1");
                    ChartRenderingInfo info5 = new ChartRenderingInfo(new StandardEntityCollection());
                    String filename5 = ServletUtilities.saveChartAsJPEG(chart5, 400, height, info5, request.getSession());
                    String graphURL5 = request.getContextPath() + "/chart/displayChart?filename=" + filename5;
                    pw.write("<img src='" + graphURL5 + "' width=400 height=" + height + " border=0 usemap='#" + filename5 + "'>");
                    ChartUtilities.writeImageMap(pw, filename5, info5, false);
                }
                pw.write("</td><td align='center'>");
                if (s2) {
                    JFreeChart chart6 = createConceptAnalysisBarChart(hashMapToCategoryDataset(results2, "Subset 2"), "Subset 2");
                    ChartRenderingInfo info6 = new ChartRenderingInfo(new StandardEntityCollection());
                    String filename6 = ServletUtilities.saveChartAsJPEG(chart6, 400, height, info6, request.getSession());
                    String graphURL6 = request.getContextPath() + "/chart/displayChart?filename=" + filename6;
                    pw.write("<img src='" + graphURL6 + "' width=400 height=" + height + " border=0 usemap='#" + filename6 + "'>");
                    ChartUtilities.writeImageMap(pw, filename6, info6, false);
                }
                pw.write("</td><tr><td align='center'>");
                if (s1) {
                    renderCategoryResultsHashMap(results1, "Subset 1", i2b2HelperService.getPatientSetSize(result_instance_id1), pw);
                }
                pw.write("</td><td align='center'>");
                if (s2) {
                    renderCategoryResultsHashMap(results2, "Subset 2", i2b2HelperService.getPatientSetSize(result_instance_id2), pw);
                }
                pw.write("</td></tr><tr><td align=\"center\" colspan=2><p>");
                renderChiSquaredHashMap(results1, results2, pw);
                pw.write("<td><tr><p></table>")

            }
            log.debug("renderConceptAnalysisNew: finished rendering " + concept_key)
        }
        catch (Exception e) {
            log.error(e); e.printStackTrace();
        }
    }

    private void renderBoxAndWhiskerInfoTableNew(List<Number> values, String trial, StringWriter pw) {
        NumberFormat form;
        form = DecimalFormat.getInstance();
        form.setMaximumFractionDigits(2);
        Number[] t = new Number[values.size()];
        BoxAndWhiskerItem b = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(values);
        pw.write("<table class='analysis'>");
        pw.write("<tr><td><b>" + trial + "</b></td></tr>");
        pw.write("<tr><td><b>Mean:</b> " + form.format(b.getMean()) + "</td></tr>");
        pw.write("<tr><td><b>Median:</b> " + form.format(b.getMedian()) + "</td></tr>");
        pw.write("<tr><td><b>IQR:</b> " + form.format((b.getQ3().doubleValue() - b.getQ1().doubleValue())) + "</td></tr>");
        pw.write("<tr><td><b>SD:</b> " + form.format(Statistics.getStdDev(values.toArray(t))) + "</td></tr>");
        pw.write("<tr><td><b>Data Points:</b> " + values.size() + "</td></tr>");
        pw.write("</table>");
        return;
    }


}

