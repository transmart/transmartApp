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
import org.jfree.chart.labels.StandardPieSectionLabelGenerator
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.plot.PiePlot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer
import org.jfree.chart.renderer.category.StandardBarPainter
import org.jfree.chart.renderer.xy.StandardXYBarPainter
import org.jfree.chart.renderer.xy.XYBarRenderer
import org.jfree.chart.renderer.xy.XYItemRenderer
import org.jfree.chart.servlet.ChartDeleter
import org.jfree.chart.servlet.ServletUtilities
import org.jfree.chart.title.TextTitle
import org.jfree.data.Range
import org.jfree.data.RangeType
import org.jfree.data.category.CategoryDataset
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.Dataset
import org.jfree.data.general.DefaultPieDataset
import org.jfree.data.general.PieDataset
import org.jfree.data.statistics.*
import org.jfree.graphics2d.svg.SVGGraphics2D
import org.jfree.ui.RectangleEdge
import org.jfree.ui.RectangleInsets
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
        def ageHistogramHandle = [:]
        def agePlotHandle = [:]

        subsets.findAll { n, p ->
            p.exists
        }.each { n, p ->

            // First we get the Query Definition
            i2b2HelperService.renderQueryDefinition(p.instance, "Query Summary for Subset ${n}", writer)
            p.query = output.toStringAndFlush()

            // Lets fetch the patient count
            p.patientCount = i2b2HelperService.getPatientSetSize(p.instance)

            // Getting the age data
            p.ageData = i2b2HelperService.getPatientDemographicValueDataForSubset("AGE_IN_YEARS_NUM", p.instance).toList()
            p.ageStats = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(p.ageData)
            ageHistogramHandle["Subset $n"] = p.ageData
            agePlotHandle["Subset $n"] = p.ageStats

            // Sex chart has to be generated for each subset
            p.sexData = i2b2HelperService.getPatientDemographicDataForSubset("sex_cd", p.instance)
            p.sexPie = getSVGChart(type: 'pie', data: p.sexData, title: "Sex")

            // Same thing for Race chart
            p.raceData = i2b2HelperService.getPatientDemographicDataForSubset("race_cd", p.instance)
            p.racePie = getSVGChart(type: 'pie', data: p.raceData, title: "Race")

        }

        // Lets build our age diagrams now that we have all the points in
        subsets.commons.ageHisto = getSVGChart(type: 'histogram', data: ageHistogramHandle)
        subsets.commons.agePlot = getSVGChart(type: 'boxplot', data: agePlotHandle)

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
            def conceptHistogramHandle = [:]
            def conceptPlotHandle = [:];

            result.findAll { n, p ->
                p.exists
            }.each { n, p ->

                // Getting the concept data
                p.conceptData = i2b2HelperService.getConceptDistributionDataForValueConceptFromCode(result.commons.conceptCode, p.instance).toList()
                p.conceptStats = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(p.conceptData)
                conceptHistogramHandle["Subset $n"] = p.conceptData
                conceptPlotHandle["Series $n"] = p.conceptStats
            }

            // Lets build our concept diagrams now that we have all the points in
            result.commons.conceptHisto = getSVGChart(type: 'histogram', data: conceptHistogramHandle)
            result.commons.conceptPlot = getSVGChart(type: 'boxplot', data: conceptPlotHandle)

        } else {

            result.commons.type = 'traditional'

            result.findAll { n, p ->
                p.exists
            }.each { n, p ->

                // Getting the concept data
                p.conceptData = i2b2HelperService.getConceptDistributionDataForConcept(concept, p.instance)
                p.conceptBar = getSVGChart(type: 'bar', data: p.conceptData, size: [width: 400, height: p.conceptData.size() * 15 + 80])

            }
        }

        return result
    }

    private def getSVGChart(Map args) {

        // Retrieving function parameters
        def type = args.type ?: null
        def data = args.data ?: [:]
        def size = args.size ?: [:]
        def title = args.title ?: ""

        // We retrieve the dimension if provided
        def width = size?.width ?: 300
        def height = size?.height ?: 300

        // If no data is being sent we return an empty string
        if (data.isEmpty()) return ''

        // We initialize a couple of objects that we are going to need
        Dataset set = null
        JFreeChart chart = null
        Color transparent = new Color(255, 255, 255, 0)
        SVGGraphics2D renderer = new SVGGraphics2D(width, height)

        // If not already defined, we add a method for defaulting parameters
        if (!JFreeChart.metaClass.getMetaMethod("setChartParameters", []))
            JFreeChart.metaClass.setChartParameters = {

                padding = RectangleInsets.ZERO_INSETS
                backgroundPaint = transparent
                plot.outlineVisible = false
                plot?.backgroundPaint = transparent

                if (plot instanceof CategoryPlot || plot instanceof XYPlot) {

                    plot?.domainGridlinePaint = Color.LIGHT_GRAY
                    plot?.rangeGridlinePaint = Color.LIGHT_GRAY
                    plot?.renderer?.setSeriesPaint(0, new Color(254, 220, 119, 150))
                    plot?.renderer?.setSeriesPaint(1, new Color(110, 158, 200, 150))
                    plot?.renderer?.setSeriesOutlinePaint(0, new Color(214, 152, 13))
                    plot?.renderer?.setSeriesOutlinePaint(1, new Color(30, 113, 85))

                    if (plot?.renderer instanceof BarRenderer) {

                        plot?.renderer?.drawBarOutline = true
                        plot?.renderer?.shadowsVisible = false
                        plot?.renderer?.barPainter = new StandardBarPainter()
                    }

                    if (plot?.renderer instanceof XYBarRenderer) {

                        plot?.renderer?.drawBarOutline = true
                        plot?.renderer?.shadowsVisible = false
                        plot?.renderer?.barPainter = new StandardXYBarPainter()
                    }
                }
            }

        // Depending on the type of chart we proceed
        switch (type) {
            case 'histogram':

                def min = null
                def max = null
                set = new HistogramDataset()
                data.each { k, v ->
                    min = min != null ? (min > v.min() ? v.min() : min) : v.min()
                    max = max != null ? (max < v.max() ? v.max() : max) : v.max()
                }.each { k, v ->
                    if (k) set.addSeries(k, (double [])v.toArray(), 10, min, max)
                }

                chart = ChartFactory.createHistogram(title, null, "", set, PlotOrientation.VERTICAL, true, true, false)
                chart.setChartParameters()
                chart.legend.visible = false
                break;

            case 'boxplot':

                set = new DefaultBoxAndWhiskerCategoryDataset();
                data.each { k, v ->
                    if (k) set.add(v, k, k)
                }

                chart = ChartFactory.createBoxAndWhiskerChart(title, "", "", set, false)
                chart.setChartParameters()
                chart.plot.renderer.maximumBarWidth = 0.09

                break;

            case 'pie':

                set = new DefaultPieDataset();
                data.each { k, v ->
                    if (k) set.setValue(k, v)
                }

                chart = ChartFactory.createPieChart(title, set, false, false, false)
                chart.setChartParameters()

                chart.title.font.size = 13
                chart.title.padding = new RectangleInsets(30, 0, 0, 0)
                chart.plot.labelBackgroundPaint = new Color(230, 230, 230)
                chart.plot.labelOutlinePaint = new Color(130, 130, 130)
                chart.plot.labelShadowPaint = transparent
                chart.plot.labelPadding = new RectangleInsets(5, 5, 5, 5)
                chart.plot.maximumLabelWidth = 0.2
                chart.plot.shadowPaint = transparent
                chart.plot.interiorGap = 0
                chart.plot.baseSectionOutlinePaint = new Color(213, 18, 42)

                data.eachWithIndex { o, i ->
                    chart.plot.setSectionPaint(o.key, new Color(213, 18, 42, (255 / (data.size() + 1) * (data.size() - i)).toInteger()))
                }

                break;

            case 'bar':

                set = new DefaultCategoryDataset();
                data.each { k, v ->
                    if (k) set.setValue(v, '', k)
                }

                chart = ChartFactory.createBarChart(title, "", "", set, PlotOrientation.HORIZONTAL, false, true, false)
                chart.setChartParameters()

                chart.plot.renderer.setSeriesPaint(0, new Color(128, 193, 119))
                chart.plot.renderer.setSeriesOutlinePaint(0, new Color(84, 151, 12))

                break;
        }

        chart.draw(renderer, new Rectangle(0, 0, width, height), new ChartRenderingInfo(new StandardEntityCollection()));
        renderer.getSVGDocument()
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

