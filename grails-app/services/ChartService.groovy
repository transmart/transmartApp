import org.apache.commons.math.stat.inference.TestUtils
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartRenderingInfo
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.axis.ValueAxis
import org.jfree.chart.entity.StandardEntityCollection
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer
import org.jfree.chart.renderer.category.CategoryItemRendererState
import org.jfree.chart.renderer.category.ScatterRenderer
import org.jfree.chart.renderer.category.StandardBarPainter
import org.jfree.chart.renderer.xy.StandardXYBarPainter
import org.jfree.chart.renderer.xy.XYBarRenderer
import org.jfree.data.category.CategoryDataset
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.Dataset
import org.jfree.data.general.DefaultPieDataset
import org.jfree.data.statistics.BoxAndWhiskerCalculator
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.jfree.data.statistics.DefaultMultiValueCategoryDataset
import org.jfree.data.statistics.HistogramDataset
import org.jfree.data.statistics.MultiValueCategoryDataset
import org.jfree.graphics2d.svg.SVGGraphics2D
import org.jfree.ui.RectangleInsets
import org.jfree.util.ShapeUtilities
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.dataconstraints.DataConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.querytool.ConstraintByOmicsValue

import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D

/**
 * Created by Florian Guitton <f.guitton@imperial.ac.uk> on 17/12/2014.
 */
class ChartService {

    def i2b2HelperService
    def highDimensionQueryService
    def highDimensionResourceService
    def public keyCache = []

    def getSubsetsFromRequest(params) {

        // We retrieve the result instance ids from the client
        def result_instance_id1 = params.result_instance_id1 ?: null;
        def result_instance_id2 = params.result_instance_id2 ?: null;

        // We create our subset reference Map
        [
            1: [ exists: !(result_instance_id1 == null || result_instance_id1 == ""), instance: result_instance_id1],
            2: [ exists: !(result_instance_id2 == null || result_instance_id1 == ""), instance: result_instance_id2],
            commons: [:]
        ]
    }

    def computeChartsForSubsets(subsets) {

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

        // Let's prepare our subset shared diagrams, we will fill them later
        def ageHistogramHandle = [:]
        def agePlotHandle = [:]

        subsets.findAll { n, p ->
            p.exists
        }.each { n, p ->

            // First we get the Query Definition
            i2b2HelperService.renderQueryDefinition(p.instance, "Query Summary for Subset ${n}", writer)
            p.query = output.toStringAndFlush()

            // Let's fetch the patient count
            p.patientCount = i2b2HelperService.getPatientSetSize(p.instance)
            // Getting the age data
            p.ageData = i2b2HelperService.getPatientDemographicValueDataForSubset("AGE_IN_YEARS_NUM", p.instance).toList()
            if (p.ageData) {
                p.ageStats = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(p.ageData)
                ageHistogramHandle["Subset $n"] = p.ageData
                agePlotHandle["Subset $n"] = p.ageStats
            }

            // Sex chart has to be generated for each subset
            p.sexData = i2b2HelperService.getPatientDemographicDataForSubset("sex_cd", p.instance)
            p.sexPie = getSVGChart(type: 'pie', data: p.sexData, title: "Sex")

            // Same thing for Race chart
            p.raceData = i2b2HelperService.getPatientDemographicDataForSubset("race_cd", p.instance)
            p.racePie = getSVGChart(type: 'pie', data: p.raceData, title: "Race")

        }

        // Let's build our age diagrams now that we have all the points in
        subsets.commons.ageHisto = getSVGChart(type: 'histogram', data: ageHistogramHandle, title: "Age")
        subsets.commons.agePlot = getSVGChart(type: 'boxplot', data: agePlotHandle, title: "Age")

        subsets
    }

    def getConceptsForSubsets(subsets) {

        // We also retrieve all concepts involved in the query
        def concepts = [:]

        i2b2HelperService.getDistinctConceptSet(subsets[1].instance, subsets[2].instance).collect {
            i2b2HelperService.getConceptKeyForAnalysis(it)
        }.findAll() {
            it.indexOf("SECURITY") <= -1
        }.each {
            if (!i2b2HelperService.isHighDimensionalConceptKey(it)) {
                concepts[it] = getConceptAnalysis(concept: it, subsets: subsets)
            }
        }

        concepts
    }

    def getHighDimensionalConceptsForSubsets(subsets) {
        // We also retrieve all concepts involved in the query
        def concepts = [:]
        highDimensionQueryService.getHighDimensionalConceptSet(subsets[1].instance, subsets[2].instance).findAll() {
            it.concept_key.indexOf("SECURITY") <= -1
        }.each {
            def key = it.concept_key + it.omics_selector + " - " + it.omics_projection_type
            if (!concepts.containsKey(key))
              concepts[key] = getConceptAnalysis(concept: it.concept_key, subsets: subsets, omics_params: it)
        }
        concepts
    }

    def getConceptAnalysis (Map args) {
        // Retrieving function parameters
        def subsets = args.subsets ?: null
        def concept = args.concept ?: null
        def chartSize = args.chartSize ?: null

        // We create our result holder and initiate it from subsets
        def result = [:]
        subsets.each { k, v ->
            result[k] = [:]
            v.exists == null ?: (result[k].exists = v.exists)
            v.instance == null ?: (result[k].instance = v.instance)
        }

        // We retrieve the basics
        result.commons.conceptCode = i2b2HelperService.getConceptCodeFromKey(concept);
        result.commons.conceptKey = concept.substring(concept.substring(3).indexOf('\\') + 3)
        result.commons.conceptName = i2b2HelperService.getShortNameFromKey(concept);
        result.commons.omics_params = args.omics_params ?: null

        if (i2b2HelperService.isValueConceptCode(result.commons.conceptCode)) {

            result.commons.type = 'value'

            // Let's prepare our subset shared diagrams, we will fill them later
            def conceptHistogramHandle = [:]
            def conceptPlotHandle = [:];

            result.findAll { n, p ->
                p.exists
            }.each { n, p ->

                p.patientCount = i2b2HelperService.getPatientSetSize(p.instance)

                // Getting the concept data
                p.conceptData = i2b2HelperService.getConceptDistributionDataForValueConceptFromCode(result.commons.conceptCode, p.instance).toList()
                p.conceptStats = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(p.conceptData)
                conceptHistogramHandle["Subset $n"] = p.conceptData
                conceptPlotHandle["Subset $n"] = p.conceptStats
            }

            // Let's build our concept diagrams now that we have all the points in
            result.commons.conceptHisto = getSVGChart(type: 'histogram', data: conceptHistogramHandle, size: chartSize)
            result.commons.conceptPlot = getSVGChart(type: 'boxplot', data: conceptPlotHandle, size: chartSize)

            // Let's calculate the T test if possible
            if (result[2].exists) {

                if (result[1].conceptData.toArray() == result[2].conceptData.toArray())
                    result.commons.testmessage = 'No T-test calculated: these are the same subsets'
                else if (result[1].conceptData.size() < 2 || result[2].conceptData.size() < 2)
                    result.commons.testmessage = 'No T-test calculated: not enough data'
                else {

                    def double [] o = (double[])result[1].conceptData.toArray()
                    def double [] t = (double[])result[2].conceptData.toArray()

                    result.commons.tstat = TestUtils.t(o, t).round(5)
                    result.commons.pvalue = TestUtils.tTest(o, t).round(5)
                    result.commons.significance = TestUtils.tTest(o, t, 0.05)

                    if (result.commons.significance)
                        result.commons.testmessage = 'T-test demonstrated results are significant at a 95% confidence level'
                    else
                        result.commons.testmessage = 'T-test demonstrated results are <b>not</b> significant at a 95% confidence level'

                }
            }
        } else if (i2b2HelperService.isHighDimensionalConceptCode(result.commons.conceptCode) && result.commons.omics_params) {

            result.commons.type = 'value'
            result.commons.conceptName = result.commons.omics_params.omics_selector + " in " + result.commons.conceptName

            // Lets prepare our subset shared diagrams, we will fill them later
            def conceptHistogramHandle = [:]
            def conceptPlotHandle = [:];

            def resource = highDimensionResourceService.getHighDimDataTypeResourceFromConcept(concept)

            result.findAll { n, p ->
                p.exists
            }.each { n, p ->

                // Getting the concept data
                p.conceptData =
                        resource.getDistribution(
                        new ConstraintByOmicsValue(projectionType: result.commons.omics_params.omics_projection_type,
                                               property      : result.commons.omics_params.omics_property,
                                               selector      : result.commons.omics_params.omics_selector),
                        concept,
                        (p.instance == "" ? null : p.instance as Long)).collect {k, v -> v}

                p.conceptStats = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(p.conceptData)
                conceptHistogramHandle["Subset $n"] = p.conceptData
                conceptPlotHandle["Subset $n"] = p.conceptStats
            }

            // Lets build our concept diagrams now that we have all the points in
            result.commons.conceptHisto = getSVGChart(type: 'histogram', data: conceptHistogramHandle, size: chartSize,
                                                      xlabel: Projection.prettyNames.get(args.omics_params.omics_projection_type, args.omics_params.omics_projection_type),
                                                      ylabel: "", bins: args.omics_params.omics_hist_bins ?: 10)
            result.commons.conceptPlot = getSVGChart(type: 'boxplot-and-points', data: conceptHistogramHandle, boxplotdata: conceptPlotHandle, size: chartSize)

            // Lets calculate the T test if possible
            if (result[2].exists) {

                if (result[1].conceptData.toArray() == result[2].conceptData.toArray())
                    result.commons.testmessage = 'No T-test calculated: these are the same subsets'
                else if (result[1].conceptData.size() < 2 || result[2].conceptData.size() < 2)
                    result.commons.testmessage = 'No T-test calculated: not enough data'
                else {

                    def double [] o = (double[])result[1].conceptData.toArray()
                    def double [] t = (double[])result[2].conceptData.toArray()

                    result.commons.tstat = TestUtils.t(o, t).round(5)
                    result.commons.pvalue = TestUtils.tTest(o, t).round(5)
                    result.commons.significance = TestUtils.tTest(o, t, 0.05)

                    if (result.commons.significance)
                        result.commons.testmessage = 'T-test demonstrated results are significant at a 95% confidence level'
                    else
                        result.commons.testmessage = 'T-test demonstrated results are <b>not</b> significant at a 95% confidence level'

                }
            }

        }
        else {

            result.commons.type = 'traditional'

            result.findAll { n, p ->
                p.exists
            }.each { n, p ->

                p.patientCount = i2b2HelperService.getPatientSetSize(p.instance)

                // Getting the concept data
                p.conceptData = i2b2HelperService.getConceptDistributionDataForConcept(concept, p.instance)
                p.conceptBar = getSVGChart(type: 'bar', data: p.conceptData, size: [width: 400, height: p.conceptData.size() * 15 + 80])

            }

            // Let's calculate the χ² test if possible
            if (result[2].exists) {

                def junction = false

                result[1].conceptData.each { k, v ->
                    junction = junction ?: (v > 0 && result[2].conceptData[k] > 0)
                }

                if (!junction)
                    result.commons.testmessage = 'No χ² test calculated: subsets are disjointed'
                else if (result[1].conceptData == result[2].conceptData)
                    result.commons.testmessage = 'No χ² test calculated: these are the same subsets'
                else if (result[1].conceptData.size() != result[2].conceptData.size())
                    result.commons.testmessage = 'No χ² test calculated: subsets have different sizes'
                else if (result[1].conceptData.size() < 2)
                    result.commons.testmessage = 'No χ² test calculated: insufficient dimension'
                else {

                    def long [][] counts = [result[1].conceptData.values(), result[2].conceptData.values()]

                    result.commons.chisquare = TestUtils.chiSquare(counts).round(5)
                    result.commons.pvalue = TestUtils.chiSquareTest(counts).round(5)
                    result.commons.significance = TestUtils.chiSquareTest(counts, 0.05)

                    if (result.commons.significance)
                        result.commons.testmessage = 'χ² test demonstrated results are significant at a 95% confidence level'
                    else
                        result.commons.testmessage = 'χ² test demonstrated results are <b>not</b> significant at a 95% confidence level'

                }
            }
        }

        return result
    }

    private def getSVGChart(Map args) {

        // Retrieving function parameters
        def type = args.type ?: null
        def data = args.data ?: [:]
        def boxplotdata = args.boxplotdata ?: [:]
        def size = args.size ?: [:]
        def title = args.title ?: ""
        def xlabel = args.xlabel ?: ""
        def ylabel = args.ylabel ?: ""
        def bins = 10
        if (args.containsKey('bins'))
            try {
                bins = args.bins as Integer
            }
            catch (Exception e) {
                log.error "Could not parse provided argument to integer: " + args.bins
            }

        // We retrieve the dimension if provided
        def width = size?.width ?: 300
        def height = size?.height ?: 300

        // If no data is being sent we return an empty string
        if (data.isEmpty()) return ''
        def nValues = 0;
        def nKeys = 0;

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
                    plot?.renderer?.setSeriesOutlinePaint(1, new Color(17, 86, 146))

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

            // requires data values to set min/max
            // can be called with empty values

                data.each { k, v ->
                    if (k) nKeys++;
                    nValues += v.size();
                }
                if(nKeys == 0) return ''
                if(nValues == 0) return ''

                def min = null
                def max = null
                set = new HistogramDataset()
//                data.findAll { it.key && it.value }.each { k, v ->
//                    set.addSeries(k, (double [])v.toArray(), bins)
//                }
                data.each { k, v ->
                    if(v.size()){
                        min = min != null ? (v.min() != null && min > v.min() ? v.min() : min) : v.min()
                        max = max != null ? (v.max() != null && max < v.max() ? v.max() : max) : v.max()
                    }
                }.each { k, v ->
                    if (k && v.size()) set.addSeries(k, (double [])v.toArray(), 10, min, max)
                }

                chart = ChartFactory.createHistogram(title, xlabel, ylabel, set, PlotOrientation.VERTICAL, true, true, false)
                chart.setChartParameters()
                chart.legend.visible = false
                break;

            case 'boxplot':

            // value is BoxAndWhiskerItem which has NaN if no values were given

                data.each { k, v ->
                    if (k) {
                        nKeys++;
                        if(!v.getMean().isNaN()) nValues++;
                    }
                }
                if(nKeys == 0) return ''
                if(nValues == 0) return ''

                set = new DefaultBoxAndWhiskerCategoryDataset();
                data.each { k, v ->
                    if (k && !v.getMean().isNaN())
                        set.add(v, k, k)
                }

                chart = ChartFactory.createBoxAndWhiskerChart(title, xlabel, ylabel, set, false)
                chart.setChartParameters()
                chart.plot.renderer.maximumBarWidth = 0.09

                break;

            case 'boxplot-and-points':

                set = new DefaultBoxAndWhiskerCategoryDataset();
                def set2 = new DefaultMultiValueCategoryDataset()
                String rowname = new String("Row 0")
                boxplotdata.each { k, v ->
                    if (k) set.add(v, rowname, k)
                }
                data.each { k, v ->
                    if (k) set2.add(v, rowname, k)
                }

                final CategoryAxis xAxis = new CategoryAxis(xlabel);
                final NumberAxis yAxis = new NumberAxis(ylabel);
                yAxis.setAutoRangeIncludesZero(false);
                final BoxAndWhiskerRenderer boxAndWhiskerRenderer = new BoxAndWhiskerRenderer();
                boxAndWhiskerRenderer.setBaseToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
                final CategoryPlot catplot = new CategoryPlot(set, xAxis, yAxis, boxAndWhiskerRenderer);

                // add the points
                catplot.setDataset(1, set2);
                def pointsWithJitterRenderer = createScatterWithJitterRenderer(20);
                pointsWithJitterRenderer.setSeriesShape(0, createScatterShape(data));
                catplot.setRenderer(1, pointsWithJitterRenderer);

                chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, catplot, false);

                ChartFactory.chartTheme.apply(chart);
                chart.setChartParameters();
                chart.plot.renderer.maximumBarWidth = 0.09;
                break;

            case 'pie':

            // fails if given a null key (e.g. missing gender values)
            
                data.each { k, v ->
                    if (k) nKeys++;
                }
                if(nKeys == 0) return ''

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
                    if(o.key){
                        chart.plot.setSectionPaint(o.key, new Color(213, 18, 42, (255 / (data.size() + 1) * (data.size() - i)).toInteger()))
                    }
                }

                break;

            case 'bar':

            // skip any null keys
                data.each { k, v ->
                    if (k) nKeys++;
                }
                if(nKeys == 0) return ''

                set = new DefaultCategoryDataset();
                data.each { k, v ->
                    if (k) set.setValue(v, '', k)
                }

                chart = ChartFactory.createBarChart(title, xlabel, ylabel, set, PlotOrientation.HORIZONTAL, false, true, false)
                chart.setChartParameters()

                chart.plot.renderer.setSeriesPaint(0, new Color(128, 193, 119))
                chart.plot.renderer.setSeriesOutlinePaint(0, new Color(84, 151, 12))

                break;
        }

        chart.draw(renderer, new Rectangle(0, 0, width, height), new ChartRenderingInfo(new StandardEntityCollection()));

        def result = renderer.getSVGDocument()

        // We need to remove some of the perturbing DOM injected by JFreeChart
        result = (result =~ /<\?xml(.*)\?>/).replaceAll("")
        result = (result =~ /<!DOCTYPE(.*?)>/).replaceAll("")
        result = (result =~ /xmlns(.*?)="(.*?)"(\s*)/).replaceAll("")
        result
    }

    /**
     * Create a Shape object for a circle with radius depending on the amount of data points
     * @param data Map where the values are lists of data points
     * @return the Shape object
     */
    def createScatterShape(data) {
        int amount = 0;
        data.each { k, v ->
            amount = v.size() > amount ? v.size() : amount
        }
        // radius is at least 3 (at 300 or more data points) and at most 6 (0 data points)
        double radius = Math.max(3.0, -0.01 * amount + 6.0);
        return new Ellipse2D.Double(0.0,0.0,radius,radius);
    }

    /**
     * Create a renderer for a scatterplot with jitter on the category axis
     * @param jitter The amount of jitter. Category axis values for points will be perturbed by (Math.rand() - 0.5) * jitter
     * @return a Renderer object
     */
    def createScatterWithJitterRenderer(double jitter) {
        return new ScatterRenderer() {
            @Override
            public void drawItem(Graphics2D g2, CategoryItemRendererState state,
                                 Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
                                 ValueAxis rangeAxis, CategoryDataset dataset, int row, int column,
                                 int pass) {

                // do nothing if item is not visible
                if (!getItemVisible(row, column)) {
                    return;
                }
                int visibleRow = state.getVisibleSeriesIndex(row);
                if (visibleRow < 0) {
                    return;
                }
                int visibleRowCount = state.getVisibleSeriesCount();

                PlotOrientation orientation = plot.getOrientation();

                MultiValueCategoryDataset d = (MultiValueCategoryDataset) dataset;
                Comparable rowKey = d.getRowKey(row);
                Comparable columnKey = d.getColumnKey(column);
                java.util.List values = d.getValues(rowKey, columnKey);
                if (values == null) {
                    return;
                }
                int valueCount = values.size();
                for (int i = 0; i < valueCount; i++) {
                    // current data point...
                    double x1;
                    if (this.getUseSeriesOffset()) {
                        x1 = domainAxis.getCategorySeriesMiddle(column,
                                dataset.getColumnCount(), visibleRow, visibleRowCount,
                                this.getItemMargin(), dataArea, plot.getDomainAxisEdge());
                    }
                    else {
                        x1 = domainAxis.getCategoryMiddle(column, getColumnCount(),
                                dataArea, plot.getDomainAxisEdge());
                    }
                    // add the jitter here
                    x1 += (Math.random() - 0.5) * jitter;
                    Number n = (Number) values.get(i);
                    double value = n.doubleValue();
                    double y1 = rangeAxis.valueToJava2D(value, dataArea,
                            plot.getRangeAxisEdge());

                    Shape shape = getItemShape(row, column);
                    if (orientation == PlotOrientation.HORIZONTAL) {
                        shape = ShapeUtilities.createTranslatedShape(shape, y1, x1);
                    }
                    else if (orientation == PlotOrientation.VERTICAL) {
                        shape = ShapeUtilities.createTranslatedShape(shape, x1, y1);
                    }
                    if (getItemShapeFilled(row, column)) {
                        if (this.getUseFillPaint()) {
                            g2.setPaint(getItemFillPaint(row, column));
                        }
                        else {
                            g2.setPaint(getItemPaint(row, column));
                        }
                        g2.fill(shape);
                    }
                    if (this.getDrawOutlines()) {
                        if (this.getUseOutlinePaint()) {
                            g2.setPaint(getItemOutlinePaint(row, column));
                        }
                        else {
                            g2.setPaint(getItemPaint(row, column));
                        }
                        g2.setStroke(getItemOutlineStroke(row, column));
                        g2.draw(shape);
                    }
                }

            }
        }
    }
}
