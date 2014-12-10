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

        // We retrieve the result instance ids from the client
        def result_instance_id1 = params.result_instance_id1 ?: null;
        def result_instance_id2 = params.result_instance_id2 ?: null;
        def concept = params.concept_key ?: null;

        // Lets put a bit of 'audit' in here
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Analysis by Concept", eventmessage: "RID1:" + result_instance_id1 + " RID2:" + result_instance_id2 + " Concept:" + concept, accesstime: new java.util.Date()).save()

        // We create our subset reference Map
        def subsets = [
            1: [ exists: !(result_instance_id1 == null || result_instance_id1 == ""), instance: result_instance_id1],
            2: [ exists: !(result_instance_id2 == null || result_instance_id1 == ""), instance: result_instance_id2],
            commons: [:]
        ]

        def concepts = [:]

        // Collect concept information
        concepts[concepts] = getConceptAnalysis(concept: i2b2HelperService.getConceptKeyForAnalysis(concept), subsets: subsets)

        // Time to delivery !
        render(template: "conceptsAnalysis", model: [concepts: concepts])
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
            p.sexPie = request.getContextPath() + "/chart/displayChart?filename=" + sexPieFile

            // Same thing for Race chart
            p.raceData = i2b2HelperService.getPatientDemographicDataForSubset("race_cd", p.instance)
            JFreeChart racePieChart = createConceptAnalysisPieChart(hashMapToPieDataset(p.raceData, "Race"), "Race");
            String racePieFile = ServletUtilities.saveChartAsJPEG(racePieChart, 300, 200, new ChartRenderingInfo(new StandardEntityCollection()), request.getSession())
            p.racePie = request.getContextPath() + "/chart/displayChart?filename=" + racePieFile

        }

        // Lets build our age diagrams now that we have all the points in
        JFreeChart ageHistoChart = ChartFactory.createHistogram("Histogram of Age", null, "Count", ageHistoHandle, PlotOrientation.VERTICAL, true, true, false)
        String ageHistoFile = ServletUtilities.saveChartAsJPEG(ageHistoChart, 245, 180, new ChartRenderingInfo(new StandardEntityCollection()), request.getSession())
        subsets.commons.ageHisto = request.getContextPath() + "/chart/displayChart?filename=" + ageHistoFile

        JFreeChart agePlotChart = ChartFactory.createBoxAndWhiskerChart("Comparison of Age", "", "Value", agePlotHandle, false)
        String agePlotFile = ServletUtilities.saveChartAsJPEG(agePlotChart, 200, 300, new ChartRenderingInfo(new StandardEntityCollection()), request.getSession())
        subsets.commons.agePlot = request.getContextPath() + "/chart/displayChart?filename=" + agePlotFile

        // We also retrieve all concepts involved in the query
        def concepts = [:]

        i2b2HelperService.getDistinctConceptSet(subsets[1].instance, subsets[2].instance).collect {
            i2b2HelperService.getConceptKeyForAnalysis(it)
        }.findAll() {
            it.indexOf("SECURITY") <= -1
        }.each {
            concepts[it] = getConceptAnalysis(concept: it, subsets: subsets)
        }

        // Time to delivery !
        render(template: "summaryStatistics", model: [subsets: subsets, concepts: concepts])
    }

    private def getConceptAnalysis (Map args) {

        // Retrieving function parameters
        def subsets = args.subsets ?: null
        def concept = args.concept ?: null

        // We create our result holder and initiate it from subsets
        def result = [:]
        subsets.each { k, v ->
            result[k] = [:]
            v.exists == null ?: (result[k].exists = v.exists)
            v.instance == null ?: (result[k].instance = v.instance)
        }

        // We retrieve the basics
        result.commons.conceptCode = i2b2HelperService.getConceptCodeFromKey(concept);
        result.commons.conceptName = i2b2HelperService.getShortNameFromKey(concept);

        if (i2b2HelperService.isValueConceptCode(result.commons.conceptCode)) {

            result.commons.type = 'value'

            // Lets prepare our subset shared diagrams, we will fill them later
            HistogramDataset conceptHistoHandle = new HistogramDataset();
            DefaultBoxAndWhiskerCategoryDataset conceptPlotHandle = new DefaultBoxAndWhiskerCategoryDataset();

            result.findAll { n, p ->
                p.exists
            }.each { n, p ->

                // Getting the concept data
                p.conceptData = (double [])i2b2HelperService.getConceptDistributionDataForValueConceptFromCode(result.commons.conceptCode, p.instance).toArray()
                conceptHistoHandle.addSeries("Subset $n", p.conceptData, 10, StatHelper.min(p.conceptData), StatHelper.max(p.conceptData));
                p.conceptStats = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(Arrays.asList(ArrayUtils.toObject(p.conceptData)))
                conceptPlotHandle.add(p.conceptStats, "Series $n", "Subset $n")
            }

            // Lets build our concept diagrams now that we have all the points in
            JFreeChart conceptHistoChart = ChartFactory.createHistogram("", null, "Count", conceptHistoHandle, PlotOrientation.VERTICAL, true, true, false)
            String conceptHistoFile = ServletUtilities.saveChartAsJPEG(conceptHistoChart, 245, 180, new ChartRenderingInfo(new StandardEntityCollection()), request.getSession())
            result.commons.conceptHisto = request.getContextPath() + "/chart/displayChart?filename=" + conceptHistoFile

            JFreeChart conceptPlotChart = ChartFactory.createBoxAndWhiskerChart("", "", "Value", conceptPlotHandle, false)
            String conceptPlotFile = ServletUtilities.saveChartAsJPEG(conceptPlotChart, 200, 300, new ChartRenderingInfo(new StandardEntityCollection()), request.getSession())
            result.commons.conceptPlot = request.getContextPath() + "/chart/displayChart?filename=" + conceptPlotFile

            // TODO Remove when possible
            // This is a little hack to set minimum height for panel focus due to image loading delay
            // This should be set to the height of the tallest JChart
            // This should be removed after implementation of SVG rendering
            result.commons.minimalHeight = 300

        } else {

            result.commons.type = 'traditional'

            result.findAll { n, p ->
                p.exists
            }.each { n, p ->

                // Getting the concept data
                p.conceptData = i2b2HelperService.getConceptDistributionDataForConcept(concept, p.instance)
                JFreeChart conceptBarChart = createConceptAnalysisBarChart(hashMapToCategoryDataset(p.conceptData, "Subset $n"), "Subset $n");
                String conceptBarFile = ServletUtilities.saveChartAsJPEG(conceptBarChart, 400, p.conceptData.size() * 15 + 80, new ChartRenderingInfo(new StandardEntityCollection()), request.getSession());
                p.conceptBar = request.getContextPath() + "/chart/displayChart?filename=" + conceptBarFile;


                // TODO Remove when possible
                // This is a little hack to set minimum height for panel focus due to image loading delay
                // This should be set to the height of the tallest JChart
                // This should be removed after implementation of SVG rendering
                def size = p.conceptData.size() * 15 + 80
                result.commons.minimalHeight = size > result.commons.minimalHeight ? size : result.commons.minimalHeight
            }
        }

        return result
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

}

