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
  

import grails.converters.*
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

import java.io.*;
import java.util.HashMap;
import java.awt.*;
import java.awt.font.*;
import javax.servlet.http.*;
import java.text.*;
import java.util.List;
import java.util.Arrays;
import java.util.LinkedHashMap;
import org.jfree.ui.RectangleInsets;
import org.jfree.data.statistics.*;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerXYDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.chart.servlet.*;
import org.jfree.chart.*;
import org.jfree.chart.entity.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.data.*;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import java.sql.*;
import java.util.*;
import java.util.logging.*;
import java.text.*;

import org.transmart.searchapp.AccessLog;
import org.transmart.searchapp.AuthUser;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;
import com.recomdata.charting.PieRenderer;
import com.recomdata.export.ExportTableNew;
import com.recomdata.statistics.StatHelper;
//import edu.mit.wi.haploview.*;
import org.jfree.chart.servlet.*;
import org.apache.commons.math.stat.inference.TestUtils;

class ChartController {

    def index = { }

    def i2b2HelperService
    def springSecurityService
    def i2b2ModifierHelperService
    def gridViewService

    def displayChart = {
    		HttpSession session = request.getSession();
            String filename = request.getParameter("filename");
            log.trace("Trying to display:"+filename)
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
    		def user = springSecurityService.getPrincipal()
    		log.trace("Called childConceptPatientCounts action in ChartController")
    		log.trace("User is:"+user.username);
    		log.trace(user.toString());
    		def concept_key=params.concept_key;
    		def counts=i2b2HelperService.getChildrenWithPatientCountsForConcept(concept_key)
    		def access=i2b2HelperService.getChildrenWithAccessForUserNew(concept_key, user)
    		log.trace("access:"+(access as JSON));
    		log.trace("counts = " + (counts as JSON))

    		def obj=[counts:counts, accesslevels:access, test1: "works"]
    		render obj as JSON
    }

   /**
    * Action to get the patient count for a concept
    */
   def conceptPatientCount = {
    		String concept_key=params.concept_key;
    		PrintWriter pw=new PrintWriter(response.getOutputStream());
    		pw.write(i2b2HelperService.getPatientCountForConcept(concept_key).toString());
    		pw.flush();
     }



  /**
   * Action to get the distribution histogram for a concept
   */
  def conceptDistribution = {

		   String concept_key=params.concept_key;
		   def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"DatasetExplorer-Set Value Concept Histogram", eventmessage:"Concept:"+concept_key, accesstime:new java.util.Date())
  			al.save()
			def concept_cd=i2b2HelperService.getConceptCodeFromKey(concept_key);
			def concept_name=i2b2HelperService.getShortNameFromKey(concept_key);

			double[] values=i2b2HelperService.getConceptDistributionDataForValueConcept(concept_key);
			HistogramDataset dataset = new HistogramDataset();
			dataset.addSeries("H1", values, 10, StatHelper.min(values), StatHelper.max(values));

	JFreeChart chart = ChartFactory.createHistogram(
            "Histogram of "+concept_name+" for all",
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
        PrintWriter pw=new PrintWriter(response.getOutputStream());

        String filename = ServletUtilities.saveChartAsJPEG(chart, 245, 180, info, request.getSession());
        String graphURL = request.getContextPath() + "/chart/displayChart?filename=" + filename;

        //  Write the image map to the PrintWriter
		//pw.write("<html><body>");
		pw.write("<img src='"+graphURL+"' width=245 height=180 border=0 usemap='#"+filename+"'>");
		ChartUtilities.writeImageMap(pw, filename, info, false);
		pw.flush();
	}


 def conceptDistributionForSubset = {
		String concept_key=params.concept_key;
		def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"DatasetExplorer-Set Value Concept Histogram for subset", eventmessage:"Concept:"+concept_key, accesstime:new java.util.Date())
			al.save()
		def result_instance_id1=params.result_instance_id1;
   		def result_instance_id2=params.result_instance_id2;
		def concept_cd=i2b2HelperService.getConceptCodeFromKey(concept_key);
		def concept_name=i2b2HelperService.getShortNameFromKey(concept_key);

		if(result_instance_id1!="" && result_instance_id1 != null)
		{
		double[] values2=i2b2HelperService.getConceptDistributionDataForValueConcept(concept_key, result_instance_id1);
		double[] values=i2b2HelperService.getConceptDistributionDataForValueConcept(concept_key);
		HistogramDataset dataset2 = new HistogramDataset();
		//changed following line from values2 min max to values to syncronize scales
		dataset2.addSeries("H1", values2, 10, StatHelper.min(values), StatHelper.max(values));
		dataset2.addSeries("H2", values, 10, StatHelper.min(values), StatHelper.max(values));
		JFreeChart chart2 = ChartFactory.createHistogram(
	            "Histogram of "+concept_name +" for subset",
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

	        PrintWriter pw=new PrintWriter(response.getOutputStream());
			pw.write("<img src='"+graphURL2+"' width=245 height=180 border=0 usemap='#"+filename2+"'>");
			ChartUtilities.writeImageMap(pw, filename2, info2, false);
			pw.flush();
		}
   }

 /**
  * Gets an analysis for a concept key and comparison
  */
   def analysis={
        String concept_key= (params.concept_key).encodeAsHTML();
        PrintWriter pw=new PrintWriter(response.getOutputStream());
        def concept_cd = i2b2HelperService.getConceptCodeFromLongPath(concept_key);
        if(concept_cd == "")
        {
            pw.write("<html><body><div> Invalid request parameter.");
        }else{

            def result_instance_id1=params.result_instance_id1;
            def result_instance_id2=params.result_instance_id2;
            def tQD1 = i2b2HelperService.getQueryDefinition(result_instance_id1);
            def tQD2 = i2b2HelperService.getQueryDefinition(result_instance_id2);

            String analysis_key=i2b2HelperService.getConceptKeyForAnalysis(concept_key);

            def children = i2b2HelperService.getChildKeysFromParentKey(analysis_key);

            TransmartQueryPanel tQP1 = new TransmartQueryPanel();
            TransmartQueryItem tQI1 = new TransmartQueryItem();

            TransmartQueryPanel tQP2 = new TransmartQueryPanel();
            TransmartQueryItem tQI2 = new TransmartQueryItem();

            Boolean tQD1AddedItem = false;
            Boolean tQD2AddedItem = false;
            if (children.size() > 0)
            {

                for(cC in children)
                {
                    //if (!tQD1?.isKeyInPanels(cC))
                    //{
                    tQI1 = new TransmartQueryItem();
                    tQI1.itemKey = cC;
                    tQP1.items.add(tQI1);
                    tQD1AddedItem = tQD1AddedItem || true;
                    //}

                    //if (!tQD2?.isKeyInPanels(cC))
                    //{
                    tQI2 = new TransmartQueryItem();
                    tQI2.itemKey = cC;
                    tQP2.items.add(tQI2);
                    tQD2AddedItem = tQD2AddedItem || true;
                    //}
                }

            }
            else {

                //if (!tQD1?.isKeyInPanels(analysis_key))
                //{
                tQI1.itemKey = analysis_key;
                tQP1.items.add(tQI1);
                tQD1AddedItem = tQD1AddedItem || true;

                //}

                //if (!tQD2?.isKeyInPanels(analysis_key))
                //{
                tQI2.itemKey = analysis_key;
                tQP2.items.add(tQI2);
                tQD2AddedItem = tQD2AddedItem || true;

                //}
            }

            if (tQD1 && tQD1AddedItem)
            {
                tQD1.panels.add(tQP1);
            }

            if (tQD2 && tQD2AddedItem)
            {
                tQD2.panels.add(tQP2);
            }

            def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"DatasetExplorer-Analysis by Concept", eventmessage:"RID1:"+result_instance_id1+" RID2:"+result_instance_id2+" Concept:"+concept_key, accesstime:new java.util.Date())
            al.save()

            pw.write("<html><head><link rel='stylesheet' type='text/css' href='css/chartservlet.css'></head><body><div class='analysis'>");

            log.debug("in analysis controller about to run render concept: "+analysis_key+" result_instance_id1:"+result_instance_id1);

            log.debug("calling renderConceptAnalysisNew from analysis with analysis_key:"+analysis_key);
            renderConceptAnalysisNew(analysis_key,concept_cd, tQD1, tQD2, pw, request);
            pw.write("<hr>");

            if(!i2b2HelperService.isLeafConceptKey(analysis_key)) //must be a folder so render all the value children
            {
                log.debug("iterating through all items in folder")

                for(String c : i2b2HelperService.getChildValueConceptsFromParentKey(concept_key))
                {
                    log.debug("-- rendering "+c)

                    renderConceptAnalysisNew(c, tQD1, tQD2, pw, request);
                    pw.write("<hr>");
                }
            }
        }
        pw.write("</div></body></html>");
        pw.flush();
    }

   /**
     * Gets an analysis for a modifier key and comparison
     */
   def analysisModifier = {

        def result_instance_id1=params.result_instance_id1;
        def result_instance_id2=params.result_instance_id2;
        def tQD1 = i2b2HelperService.getQueryDefinition(result_instance_id1);
        def tQD2 = i2b2HelperService.getQueryDefinition(result_instance_id2);

        def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"DatasetExplorer-Analysis by Modifier", eventmessage:"RID1:"+result_instance_id1+" RID2:"+result_instance_id2+" Modifier:"+ params.name, accesstime:new java.util.Date())
        al.save()

        PrintWriter pw=new PrintWriter(response.getOutputStream());
        pw.write("<html><head><link rel='stylesheet' type='text/css' href='css/chartservlet.css'></head><body><div class='analysis'>");

        //Create the modifier object.
        def analysisModifier = i2b2ModifierHelperService.createModifierObject(params.modifierCode,params.level,params.oktousevalues,params.name,params.inOutCode)

        //If it's a leaf text node we need to get its parent and use that.
        if(params.level == "leaf" && params.oktousevalues == "N")
        {
            analysisModifier = i2b2ModifierHelperService.getModifierObjectParent(analysisModifier)
        }

        //Render the analysis.
        renderModifierAnalysisNew(analysisModifier, tQD1, tQD2, pw, request);

        pw.write("<hr>");

        //If this is a folder, render all the child value values as a box plot.
        if(!(params.level == "leaf"))
        {
            for(Map c : i2b2ModifierHelperService.getChildModifiersFromParentKey(params.name, true, params.inOutCode))
            {
                renderModifierAnalysisNew(c, tQD2, tQD2, pw, request);
                pw.write("<hr>");
            }
        }

        pw.write("</div></body></html>");
        pw.flush();

    }

/**
   * Action to get the basic statistics for the subset comparison and render them
   */
   def basicStatistics = {
  		log.trace("*******************Called basicStatistics action in ChartController")
  		request.getSession().setAttribute("gridtable", null);
  		log.trace("Clearing grid in basicstatistics")
  		def result_instance_id1=params.result_instance_id1;
  		def result_instance_id2=params.result_instance_id2;
  		def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"DatasetExplorer-Basic Statistics", eventmessage:"RID1:"+result_instance_id1+" RID2:"+result_instance_id2, accesstime:new java.util.Date())
		log.trace(al.toString())
		log.trace(al.username)
		log.trace(al.event)
  		al.save()
  		log.trace("Result instance 1:"+result_instance_id1);
  		log.trace("Result instance 2:"+result_instance_id2);
  		def boolean s1=true;
		def boolean s2=true;
		if(result_instance_id1=="" || result_instance_id1==null){s1=false;}
		if(result_instance_id2=="" || result_instance_id2==null){s2=false;}
		log.trace("s1:"+s1)
		log.trace("s2:"+s2)
		PrintWriter pw=new PrintWriter(response.getOutputStream());

		pw.write("<html><head><link rel='stylesheet' type='text/css' href='css/chartservlet.css'></head><body><div class='analysis'>");
		pw.write("<table width='100%'>");
		pw.write("<tr><td colspan='2' align='center'><div class='analysistitle'>Summary Statistics</div></td></tr>");
		pw.write("<tr><td width='50%' align='center'>");
		if(s1){i2b2HelperService.renderQueryDefinitionTable(result_instance_id1,"Query Summary for Subset 1", pw, null);}
		pw.write("</td><td align='center'>");
		if(s2){i2b2HelperService.renderQueryDefinitionTable(result_instance_id2, "Query Summary for Subset 2", pw, null);}
		pw.write("</tr>");
		pw.write("<tr><td colspan='2' align='center'>");
		renderPatientCountInfoTable(result_instance_id1, result_instance_id2, pw);

		/*get the data*/
		log.trace("Getting age data")
		double[] values3=i2b2HelperService.getPatientDemographicValueDataForSubset("AGE_IN_YEARS_NUM", result_instance_id1);
		double[] values4=i2b2HelperService.getPatientDemographicValueDataForSubset("AGE_IN_YEARS_NUM", result_instance_id2);

		log.trace("Rendering age histograms")
		/*render the double histogram*/
		HistogramDataset dataset3 = new HistogramDataset();
		if(s1){dataset3.addSeries("Subset 1", values3, 10, StatHelper.min(values3), StatHelper.max(values3));}
		if(s2){dataset3.addSeries("Subset 2", values4, 10, StatHelper.min(values4), StatHelper.max(values4));}
		JFreeChart chart3 = ChartFactory.createHistogram(
				"Histogram of Age",
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
		pw.write("</td></tr><tr><td colspan=2 align='center'><table><tr>");
		pw.write("<td><img src='"+graphURL3+"' width=245 height=180 border=0 usemap='#"+filename3+"'>");
		ChartUtilities.writeImageMap(pw, filename3, info3, false);
		pw.write("</td>");

		/*Render the box plot*/
		DefaultBoxAndWhiskerCategoryDataset dataset7= new DefaultBoxAndWhiskerCategoryDataset();

		ArrayList<Number> l1=new ArrayList<Number>();
		for(int i=0; i<values3.length; i++)
		{
			l1.add(values3[i]);
		}
		ArrayList<Number> l2=new ArrayList<Number>();
		for(int i=0; i<values4.length; i++)
		{
			l2.add(values4[i]);
		}
		BoxAndWhiskerItem boxitem1=BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(l1);
		BoxAndWhiskerItem boxitem2=BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(l2);
		if(s1 && l1.size()>0){dataset7.add(boxitem1, "Series 1", "Subset 1");}
		if(s2 && l2.size()>0){dataset7.add(boxitem2, "Series 2", "Subset 2");}

            JFreeChart chart7 = ChartFactory.createBoxAndWhiskerChart(
                    "Comparison of Age","", "Value", dataset7,
                    false);
            chart7.getTitle().setFont(new Font("SansSerif", Font.BOLD, 12));
            CategoryPlot plot7 = (CategoryPlot) chart7.getPlot();
            plot7.setDomainGridlinesVisible(true);

            NumberAxis rangeAxis7 = (NumberAxis) plot7.getRangeAxis();
            BoxAndWhiskerRenderer rend7=(BoxAndWhiskerRenderer)plot7.getRenderer();
			rend7.setMaximumBarWidth(0.10);
            //rangeAxis7.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        	ChartRenderingInfo info7 = new ChartRenderingInfo(new StandardEntityCollection());

            String filename7 = ServletUtilities.saveChartAsJPEG(chart7, 200, 300, info7, request.getSession());
			String graphURL7 = request.getContextPath() + "/chart/displayChart?filename=" + filename7;
			pw.write("<td align='center'>");
			if(s1 && l1.size()>0){pw.write("<div class='smalltitle'><b>Subset 1</b></div>");
			renderBoxAndWhiskerInfoTable(l1, pw);}
			pw.write("</td>");
			pw.write("<td><img src='"+graphURL7+"' width=200 height=300 border=0 usemap='#"+filename7+"'>");
			pw.write("<td valign='top'><div style='position:relative;left:-30px;'><a  href=\"javascript:showInfo('help/boxplot.html');\"><img src=\"images/information.png\"></a></div></td>");
			pw.write("</td><td align='center'>");
			if(s2 && l2.size()>0){pw.write("<div class='smalltitle'><b>Subset 2</b></div>");
			renderBoxAndWhiskerInfoTable(l2, pw);}
			ChartUtilities.writeImageMap(pw, filename7, info7, false);
		pw.write("</td></tr></table>");
		pw.write("</td></tr><tr><td width='50%' align='center'>");

		if(s1){
		HashMap<String,Integer> sexs1=i2b2HelperService.getPatientDemographicDataForSubset("sex_cd", result_instance_id1);
		JFreeChart chart=createConceptAnalysisPieChart(hashMapToPieDataset(sexs1, "Sex"), "Sex");
		info7 = new ChartRenderingInfo(new StandardEntityCollection());
        filename7 = ServletUtilities.saveChartAsJPEG(chart, 200, 200, info7, request.getSession());
        graphURL7 = request.getContextPath() + "/chart/displayChart?filename=" + filename7;
		pw.write("<img src='"+graphURL7+"' width=200 height=200 border=0 usemap='#"+filename7+"'>");
		ChartUtilities.writeImageMap(pw, filename7, info7, false);
		//pw.write("<b>Sex</b>");
		renderCategoryResultsHashMap(sexs1,"Subset 1", i2b2HelperService.getPatientSetSize(result_instance_id1), pw);
		}

		pw.write("</td><td width='50%' align='center'>");
	    if(s2){
	    HashMap<String, Integer> sexs2=i2b2HelperService.getPatientDemographicDataForSubset("sex_cd", result_instance_id2);
		JFreeChart chart=createConceptAnalysisPieChart(hashMapToPieDataset(sexs2, "Sex"), "Sex");
		info7 = new ChartRenderingInfo(new StandardEntityCollection());
        filename7 = ServletUtilities.saveChartAsJPEG(chart, 200, 200, info7, request.getSession());
        graphURL7 = request.getContextPath() + "/chart/displayChart?filename=" + filename7;
		pw.write("<img src='"+graphURL7+"' width=200 height=200 border=0 usemap='#"+filename7+"'>");
		ChartUtilities.writeImageMap(pw, filename7, info7, false);
		//pw.write("<b>Sex</b>");
		renderCategoryResultsHashMap(sexs2,"Subset 2",i2b2HelperService.getPatientSetSize(result_instance_id2), pw);
	    }


	    HashMap<String, Integer> raceResults1;
	    HashMap<String, Integer> raceResults2;

	    // get race statistics
	    if (s1) {
			raceResults1=i2b2HelperService.getPatientDemographicDataForSubset("race_cd", result_instance_id1);
			log.debug("raceResults1: "+raceResults1)
	    }
	    if (s2) {
			raceResults2=i2b2HelperService.getPatientDemographicDataForSubset("race_cd", result_instance_id2);
			log.debug("raceResults2: "+raceResults2)
	    }

	    HashMap<String, Integer> race1 = new HashMap<String, Integer>();
	    HashMap<String, Integer> race2 = new HashMap<String, Integer>();
	    // filter cases where there are zero subjects of a given race
	    if (s1) {
			Iterator itr = raceResults1.keySet().iterator();
		    while (itr.hasNext()) {
		    	String s = itr.next()
				log.trace("examining: "+s+", count:"+raceResults1.get(s))
		    	if (raceResults1.get(s) != 0) {
		    		race1.put(s, raceResults1.get(s));
		    		race2.put(s, 0);
		    		log.trace("added to race1, race2")
		    	}
		    }
		    log.trace("race1: "+race1)
	    }

	    if (s2) {
			Iterator itr = raceResults2.keySet().iterator();
		    while (itr.hasNext()) {
		    	String s = itr.next()
				log.trace("examining "+s+", count:"+raceResults2.get(s))
				if (raceResults2.get(s) != 0) {
		    		race2.put(s, raceResults2.get(s));
		    		log.trace("added to race2")
		    		if (! race1.containsKey(s)) {
		    			race1.put(s, 0)
			    		log.trace("also added to race2")
		    		}
		    	}
		    }
		    log.trace("race2: "+race2)
	    }

		pw.write("</td></tr>");
		pw.write("<tr><td width='50%' align='center'>");
		if(s1){
			pw.write("<br>");
			// HashMap<String, Integer> race1=i2b2HelperService.getPatientDemographicDataForSubset("race_cd", result_instance_id1);
			JFreeChart chart=createConceptAnalysisPieChart(hashMapToPieDataset(race1, "Race"), "Race");
			info7 = new ChartRenderingInfo(new StandardEntityCollection());
	        filename7 = ServletUtilities.saveChartAsJPEG(chart, 300, 200, info7, request.getSession());
	        graphURL7 = request.getContextPath() + "/chart/displayChart?filename=" + filename7;
			pw.write("<img src='"+graphURL7+"' width=300 height=200 border=0 usemap='#"+filename7+"'>");
			ChartUtilities.writeImageMap(pw, filename7, info7, false);
			//pw.write("<b>Race</b>");
			renderCategoryResultsHashMap(race1,"Subset 1", i2b2HelperService.getPatientSetSize(result_instance_id1), pw);
		}

		pw.write("</td><td width='50%' align='center'>");

		if(s2){
			pw.write("<br>");
			// HashMap<String, Integer> race2=i2b2HelperService.getPatientDemographicDataForSubset("race_cd", result_instance_id2);
			JFreeChart chart=createConceptAnalysisPieChart(hashMapToPieDataset(race2, "Race"), "Race");
			info7 = new ChartRenderingInfo(new StandardEntityCollection());
            filename7 = ServletUtilities.saveChartAsJPEG(chart, 300, 200, info7, request.getSession());
            graphURL7 = request.getContextPath() + "/chart/displayChart?filename=" + filename7;
			pw.write("<img src='"+graphURL7+"' width=300 height=200 border=0 usemap='#"+filename7+"'>");
			ChartUtilities.writeImageMap(pw, filename7, info7, false);
			//pw.write("<b>Race</b>");
			renderCategoryResultsHashMap(race2,"Subset 2",i2b2HelperService.getPatientSetSize(result_instance_id2), pw);
		}

		pw.write("</td></tr></table>");

		/*get all distinct  concepts for analysis from both subsets into hashmap*/
		List<String> keys=i2b2HelperService.getConceptKeysInSubsets(result_instance_id1, result_instance_id2);
		pw.write("<hr>");
		pw.write("<table width='100%'><tr><td align='center'><div class='analysistitle'>Analysis of concepts found in Subsets</div></td></tr></table>");
		pw.write("<hr>");
		/*Analyze each concept in subsets*/
/*
		log.debug("Keys: " + keys);
		Set<String> distinctConcepts = i2b2HelperService.getDistinctConceptSet(result_instance_id1, result_instance_id2);
		Set<String> uniqueConcepts = new HashSet<String>();

		for (c in distinctConcepts) {
			String uKey = i2b2HelperService.getConceptKeyForAnalysis(c);
			uniqueConcepts.add(uKey);
		}

		log.debug("Unique concepts: " + uniqueConcepts);

		// for (int i = 0; i < keys.size(); i++)
		for (k in uniqueConcepts)
		{
		   //String analysis_key = i2b2HelperService.getConceptKeyForAnalysis(keys.get(i));
		   //String analysis_key = i2b2HelperService.getConceptKeyForAnalysis(k);
		   String analysis_key = k;
		   if(analysis_key.indexOf("SECURITY")>-1){continue;}
		   //log.trace("Analysis Key: "+i+", "+analysis_key);
		   log.debug("calling renderConceptAnalysisNew from basic statistics:\tk:"+k+"\tanalysis_key:"+analysis_key);
		   renderConceptAnalysisNew(analysis_key, result_instance_id1, result_instance_id2, pw, request);
		   pw.write("<hr>");
		}
		*//*test harness*//**//*
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
		pw.write("</pre>");*//*
		*//*end test*/
      //Determine which dimension this query is using.
      def queryDimension = i2b2ModifierHelperService.determineQueryDimension(result_instance_id1, result_instance_id2)

      log.debug("Query Dimension : " + queryDimension)

      def tQD1 = s1 ? i2b2HelperService.getQueryDefinition(result_instance_id1) : null;
      def tQD2 = s2 ? i2b2HelperService.getQueryDefinition(result_instance_id2) : null;
      // for (int i = 0; i < keys.size(); i++)
      //Depending on the dimension type, we handle the display of concepts differently.
      if (queryDimension == "MODIFIER_DIMENSION") {
          //Get all the modifiers used to make this subset.
          def modifierObjectArray = i2b2ModifierHelperService.getModifierObjectForAnalysis(result_instance_id1, result_instance_id2)
          Set<Map> uniqueModifierObjects = new HashSet<Map>();

          //Loop through each of the modifiers, we need to determine if it's a value node, leaf text node, or a folder. The leaf text nodes must be replaced with their parent folder.
          for (modifierObject in modifierObjectArray) {

              //If we are a value node or not a leaf node, add it to the array.
              if (modifierObject["oktousevalues"] == "Y" || modifierObject["level"] != "leaf") {
                  uniqueModifierObjects.add(modifierObject)
              } else {
                  //We aren't a value and we are a leaf node, so we need to retrieve the parent.
                  uniqueModifierObjects.add(i2b2ModifierHelperService.getModifierObjectParent(modifierObject))
              }

          }

          for (modifierObject in uniqueModifierObjects) {

              //Don't try to compare security keys.
              //if(analysis_key.indexOf("SECURITY")>-1){continue;}

              //Compare this concept across subsets.
              renderModifierAnalysisNew(modifierObject, tQD1, tQD2, pw, request);
              pw.write("<hr>");
          }
      } else {

          renderConceptsAnalysis(tQD1, tQD2, pw);

      }
		pw.write("</div></body></html>");
		pw.flush();
   }

   private void renderConceptsAnalysis(TransmartQueryDefinition tQD1, TransmartQueryDefinition tQD2, PrintWriter pw) {

        //Get a distinct list of concepts used to generate either subset.
        Set<String> distinctConcepts = i2b2HelperService.getDistinctConceptSet(tQD1?.resultInstanceId, tQD2?.resultInstanceId);
        Set<String> uniqueConcepts = new HashSet<String>();

        for (c in distinctConcepts) {
            String uKey = i2b2HelperService.getConceptKeyForAnalysis(c);
            uniqueConcepts.add(uKey);
        }

        log.debug("Unique concepts: " + uniqueConcepts);

        for (k in uniqueConcepts) {

            String analysis_key = k;

            //Don't try to compare security keys.
            if (analysis_key.indexOf("SECURITY") > -1) {
                continue;
            }

            log.debug("calling renderConceptAnalysisNew from basic statistics:\tk:" + k + "\tanalysis_key:" + analysis_key);

            //Compare this concept across subsets.
            renderConceptAnalysisNew(analysis_key, tQD1, tQD2, pw, request);
            pw.write("<hr>");
        }
    }

    def basicGrid = {

        def echo = params["sEcho"];
        def result_instance_id1=params.result_instance_id1;
        def result_instance_id2=params.result_instance_id2;
        def columnsOnly = params.columnsOnly;
        def codeType = params.codeType;

        /*which subsets are present? */

        boolean s1=true;
        boolean s2=true;
        if(result_instance_id1=="" || result_instance_id1==null){s1=false;}
        if(result_instance_id2=="" || result_instance_id2==null){s2=false;}

        PrintWriter pw=new PrintWriter(response.getOutputStream());
        ExportTableNew table;

        def gtSession = request.getSession().getAttribute("gridtable");

        if (!gtSession) {
            table=new ExportTableNew();

            if(s1){gridViewService.addAllPatientDemographicDataForSubsetToTable(table, result_instance_id1, "subset1");}
            if(s2){gridViewService.addAllPatientDemographicDataForSubsetToTable(table, result_instance_id2, "subset2");}

            if(codeType == "Modifier")
            {
                //Get all the modifiers used to make this subset.
                def modifierObjectArray = i2b2ModifierHelperService.getModifierObjectForAnalysis(result_instance_id1, result_instance_id2)
                Set<Map> uniqueModifierObjects = new HashSet<Map>();

                //Unique the modifierObject list.
                for (modifierObject in modifierObjectArray)
                {
                    uniqueModifierObjects.add(modifierObject)
                }

                for(modifierObject in uniqueModifierObjects)
                {
                    if(s1){i2b2ModifierHelperService.addModifierDataToTable(table, modifierObject, result_instance_id1);}
                    if(s2){i2b2ModifierHelperService.addModifierDataToTable(table, modifierObject, result_instance_id2);}
                }
            }
            else
            {

                List<String> keys=i2b2HelperService.getConceptKeysInSubsets(result_instance_id1, result_instance_id2);
                //System.out.println("id1 " + result_instance_id1);
                //System.out.println("id2 " + result_instance_id2);
                Set<String> uniqueConcepts = i2b2HelperService.getDistinctConceptSet(result_instance_id1, result_instance_id2);

                log.debug("Unique concepts: " + uniqueConcepts);

                for(int i=0;i<keys.size();i++)
                {
                    log.trace("adding concept data for "+keys.get(i));
                    if(s1){gridViewService.addConceptDataToTable(table, keys.get(i), result_instance_id1);}
                    if(s2){gridViewService.addConceptDataToTable(table, keys.get(i), result_instance_id2);}
                }
            }
        }
        else {
            table=(ExportTableNew)gtSession;
        }

        String jsonToReturn;

        if (Boolean.parseBoolean(columnsOnly)) {
            jsonToReturn = table.getJSONColumns().toString(5);
        }
        else {
            jsonToReturn = table.toJSON_DataTables(echo).toString(5);

        }

        pw.write(jsonToReturn);
        pw.flush();

        request.getSession().setAttribute("gridtable", table);
    }

/*def analysisGrid = {
		String concept_key=params.concept_key;
		def result_instance_id1=params.result_instance_id1;
  		def result_instance_id2=params.result_instance_id2;

  		*//*which subsets are present? *//*
  		boolean s1=true;
  		boolean s2=true;
  		if(result_instance_id1=="" || result_instance_id1==null){s1=false;}
  		if(result_instance_id2=="" || result_instance_id2==null){s2=false;}

  		def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"DatasetExplorer-Grid Analysis Drag", eventmessage:"RID1:"+result_instance_id1+" RID2:"+result_instance_id2+" Concept:"+concept_key, accesstime:new java.util.Date())
			al.save()
		ExportTableNew table=(ExportTableNew)request.getSession().getAttribute("gridtable");
		if(table==null)
		{
			table=new ExportTableNew();
			if(s1){i2b2HelperService.addAllPatientDemographicDataForSubsetToTable(table, result_instance_id1, "subset1");}
			if(s2){i2b2HelperService.addAllPatientDemographicDataForSubsetToTable(table, result_instance_id2, "subset2");}
		}
		PrintWriter pw=new PrintWriter(response.getOutputStream());

		// need to modify to try looking for equivalent concepts on both subsets
		String parentConcept = i2b2HelperService.lookupParentConcept(i2b2HelperService.keyToPath(concept_key));
		log.debug("parent concept: "+parentConcept);

		Set<String> cconcepts = i2b2HelperService.lookupChildConcepts(parentConcept, result_instance_id1, result_instance_id2);
		def conceptKeys = [];
		def prefix = concept_key.substring(0, concept_key.indexOf("\\",2));

		if (!cconcepts.isEmpty()) {
			//
			for(cc in cconcepts){
				def ck = prefix+i2b2HelperService.getConceptPathFromCode(cc);
				conceptKeys.add(ck);
			}
		} else {
            def leafSet = []
            def fetchChildren = { inRoot ->
                inRoot.each {
                    leafSet << it
                    def childNodes = i2b2HelperService.getChildPathsFromParentKey(it).collect {
                        prefix + it
                    }
                    if(childNodes)
                        owner.call(childNodes)
                }
            }([concept_key])
            conceptKeys.addAll(leafSet)
		}

		//	println(prefix);
		log.debug("child concepts: "+cconcepts);
//		println("org concept key:"+concept_key);
		for(ck in conceptKeys){
	//	println("new conceptkeys:"+ck);
		if(s1){i2b2HelperService.addConceptDataToTable(table, ck, result_instance_id1);}
		if(s2){i2b2HelperService.addConceptDataToTable(table, ck, result_instance_id2);}
		}
		pw.write(table.toJSONObject().toString(5));
		pw.flush();
		request.getSession().setAttribute("gridtable", table);
	}*/

    def analysisGrid = {
        String concept_key=params.concept_key;
        def result_instance_id1=params.result_instance_id1;
        def result_instance_id2=params.result_instance_id2;

        /*which subsets are present? */
        boolean s1=true;
        boolean s2=true;
        if(result_instance_id1=="" || result_instance_id1==null){s1=false;}
        if(result_instance_id2=="" || result_instance_id2==null){s2=false;}

        def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"DatasetExplorer-Grid Analysis Drag", eventmessage:"RID1:"+result_instance_id1+" RID2:"+result_instance_id2+" Concept:"+concept_key, accesstime:new java.util.Date())
        al.save()
        ExportTableNew table=(ExportTableNew)request.getSession().getAttribute("gridtable");
        if(table==null)
        {
            table=new ExportTableNew();
            if(s1){gridViewService.addAllPatientDemographicDataForSubsetToTable(table, result_instance_id1, "subset1");}
            if(s2){gridViewService.addAllPatientDemographicDataForSubsetToTable(table, result_instance_id2, "subset2");}
        }
        PrintWriter pw=new PrintWriter(response.getOutputStream());

        if (i2b2HelperService.isLeafConceptKey(concept_key))
        {
            addLeafConceptToTable(result_instance_id1, result_instance_id2, log, concept_key, table, s1, s2)
        }
        else if (i2b2HelperService.isFolderConceptKey(concept_key))
        {
            addFolderConceptToTable(result_instance_id1, result_instance_id2, log, concept_key, table)
        }

        def jSONToReturn = table.toJSON_DataTables().toString(5);

        pw.write(jSONToReturn);
        pw.flush();
        request.getSession().setAttribute("gridtable", table);
    }

    def clearGrid = {
		log.debug("Clearing grid");
		request.getSession().setAttribute("gridtable", null);
		log.debug("Setting export filename to null, since there is nothing to export")
		request.getSession().setAttribute("expdsfilename", null);
		PrintWriter pw=new PrintWriter(response.getOutputStream());
		pw.write("grid cleared!");
		pw.flush();
	}


def exportGrid = {
	byte[] bytes =((ExportTableNew)request.getSession().getAttribute("gridtable")).toCSVbytes();
	int outputSize=bytes.length;
	//response.setContentType("application/vnd.ms-excel");
	response.setContentType("text/csv");
	response.setHeader("Content-disposition", "attachment; filename=" + "export.csv");
	response.setContentLength(outputSize);
	ServletOutputStream servletoutputstream = response.getOutputStream();
	servletoutputstream.write(bytes);
	servletoutputstream.flush();
	}

private void renderBoxAndWhiskerInfoTable(List<Number> values, PrintWriter pw)
{
	NumberFormat form;
	form=DecimalFormat.getInstance();
	form.setMaximumFractionDigits(2);
	Number[] t=new Number[values.size()];
	BoxAndWhiskerItem b=BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(values);
	pw.write("<table class='analysis'>");
	pw.write("<tr><td><b>Mean:</b> "+form.format(b.getMean())+"</td></tr>");
	pw.write("<tr><td><b>Median:</b> "+form.format(b.getMedian())+"</td></tr>");
	pw.write("<tr><td><b>IQR:</b> "+form.format((b.getQ3().doubleValue()-b.getQ1().doubleValue()))+"</td></tr>");
	pw.write("<tr><td><b>SD:</b> "+form.format(Statistics.getStdDev(values.toArray(t)))+"</td></tr>");
	pw.write("<tr><td><b>Data Points:</b> "+values.size()+"</td></tr>");
	pw.write("</table>");
	return;
}

private void renderPatientCountInfoTable(String result_instance_id1, String result_instance_id2, PrintWriter pw)
{
	try{
		pw.write("<table width='100%'><tr><td align='center'><div class='smalltitle'><b>Subject Totals</b></div>");
		pw.write("<table class='analysis'>");
		pw.write("<tr><th>Subset 1</th><th>Both</th><th>Subset 2</th></th>");
		pw.write("<tr><td>"+i2b2HelperService.getPatientSetSize(result_instance_id1)+"</td><td>"+i2b2HelperService.getPatientSetIntersectionSize(result_instance_id1, result_instance_id2)+"</td><td>"+i2b2HelperService.getPatientSetSize(result_instance_id2)+"</td></tr>");
		pw.write("</table></td></tr></table>");
	} catch (Exception e) {
		log.error(e);
	}finally{
	}
	return;
}

private void renderCategoryResultsHashMap(HashMap<String,Integer> results, String title, Integer totalsubjects, PrintWriter pw)
{
	pw.write("<table class='analysis'>");
	int mapsize = results.size();
	int total=0;
	NumberFormat form;
	form=NumberFormat.getPercentInstance();
	form.setMaximumFractionDigits(1);
	Iterator keyValuePairs1 = results.entrySet().iterator();
	//get the sum of the results so i can do percents
	for (int i = 0; i < mapsize; i++)
	{
	  Map.Entry<String,Integer> entry = (Map.Entry<String,Integer>) keyValuePairs1.next();
	  Integer value = entry.getValue();
	  total=total+value;
	}
	Iterator keyValuePairs2 = results.entrySet().iterator();
	pw.write("<tr>"
				+ "<th>Category</th>"
			    + "<th>" + title + " (n)</th>"
				+ "<th>" + title + " (%n)</th>"
				// + "<th>" + title + " (% " + totalsubjects + " Subjects)</th>"
				+ "</tr>"
			);
	for (int i = 0; i < mapsize; i++)
	{
	  Map.Entry<String,Integer> entry = (Map.Entry<String,Integer>) keyValuePairs2.next();
	  String key = entry.getKey();
	  Integer value = entry.getValue();
	  Double test=((double)value / (double)totalsubjects);
      if (value) {
	  pw.write("<tr>"
			   + "<td>"+ key + "</td>"
			   + "<td>" + value.toString() + "</td>"
			   + "<td>" + form.format(((double)value / (double)total)) + "</td>"
			   // + "<td>" + form.format(test) + "</td>"
			   + "</tr>"
			   );
      }
	}
    def percentVal = (total > 0) ? form.format((double)total/(double)total) : "N/A";
	 pw.write("<tr>"
			  + "<td><b>Total</b></td>"
			  + "<td><b>" + total + "</b></td>"
			  + "<td><b>" + percentVal+ "</b></td>"
			  // + "<td><b>N/A</b></td>"
			  + "</tr>"
			  );
	pw.write("</table>");
}

private String sigTestRounder(double val) {
	return(String.format("%.5g%n",val));
}

private void renderChiSquaredHashMap(
		HashMap<String,Integer> results1,
		HashMap<String,Integer> results2,
		PrintWriter pw )
{
	// Prints the p-value from a chi-squared significance test
	// for two sets of categorical results

	if ((results1!=null) && (results2!=null)) {
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

		if (! chk) {
			log.trace("No chi-squared test calculated: disjoint sets.");
			 pw.write("No chi-squared test calculated: disjoint sets.");
		} else if (Arrays.equals(al1.toArray(),al2.toArray())) {
			log.trace("No chi-squared test calculated: subset sizes are identical.");
			 pw.write("No chi-squared test calculated: subset sizes are the same.")
		} else if (results1.size() != results2.size()) {
			log.trace("No chi-squared test calculated: different dimensions.");
			 pw.write("No chi-squared test calculated: different dimensions.");
		} else {

			try {
				long [][] counts = [al1.toArray(), al2.toArray()];

				double chiSquareValue = TestUtils.chiSquare(counts);
				double pValue         = TestUtils.chiSquareTest(counts);
				boolean isSignificant = TestUtils.chiSquareTest(counts, 0.05);

				log.trace("Chi-Squared: "+sigTestRounder(chiSquareValue));
				log.trace("Chi-Squared Test P Value: "+sigTestRounder(pValue));
				log.trace("Significant at 95%: "+isSignificant);

				pw.write("<p><table class=\"analysis\">");
				pw.write("<tr><td> <b>Chi-Squared:<b></td><td>"+sigTestRounder(chiSquareValue) +"</td></tr>");
				pw.write("<tr><td> <b>p-value:<b></td><td>"+sigTestRounder(pValue) +"</td></tr>");

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
		HashMap<String,Integer> results1,
		HashMap<String,Integer> results2,
		PrintWriter pw )
{
	// Prints the p-value from a t test
	// for two sets of categorical results

	if ((!results1.isEmpty()) && (!results2.isEmpty())) {
		log.trace("RENDERING TTest")

		Collection c1 = results1.values();
		Collection c2 = results2.values();

		double[] s1;
		double[] s2;

		for(key1 in results1.keySet()) {
			s1 = results1[key1];
		}

		for(key2 in results2.keySet()) {
			s2 = results2[key2];
		}

		log.debug("s1: "+s1);
		log.debug("s2: "+s2);

		if (Arrays.equals(s1, s2)){
			log.trace("No t test performed; subsets are identical.");
			pw.write("No significance tests were calculated; subsets are identical.")
		} else if (s1.size() < 2 || s2.size() <2 ) {
			pw.write("No significance tests were calculated; one subset was too small to calculate statistics.")
		} else {

			double tStatistic     = TestUtils.t(s1, s2);
			double pValue         = TestUtils.tTest(s1,s2);
			boolean isSignificant = TestUtils.tTest(s1,s2,0.05);

			log.trace("t statistic: "+tStatistic);
			log.trace("T-Test P Value: "+pValue);
			log.trace("Significant at 95%: "+isSignificant);

			pw.write("<p><table class=\"analysis\">");
			pw.write("<tr><td> <b>t statistic:<b></td><td>"+sigTestRounder(tStatistic) +"</td></tr>");
			pw.write("<tr><td> <b>p-value:<b></td><td>"+sigTestRounder(pValue) +"</td></tr>");
			pw.write("</table>");

			if (isSignificant) {
				pw.write("The results are significant at a 95% confidence level.");
			} else {
				pw.write("The results are <i>not</i> significant at a 95% confidence level.");
			}
		}
	}
}

private CategoryDataset hashMapToCategoryDataset(HashMap<String,Integer> results, String seriesname)
{
DefaultCategoryDataset dataset=new DefaultCategoryDataset();
int mapsize = results.size();
Iterator keyValuePairs1 = results.entrySet().iterator();
for (int i = 0; i < mapsize; i++)
{
  Map.Entry<String,Integer> entry = (Map.Entry<String,Integer>) keyValuePairs1.next();
  String key = entry.getKey();
  Integer value = entry.getValue();
  if(key!=null){dataset.addValue(value, seriesname, key);}
}
return dataset;
}

private PieDataset hashMapToPieDataset(HashMap<String,Integer> results, String seriesname)
{
DefaultPieDataset dataset=new DefaultPieDataset();
int mapsize = results.size();
double total=0;
/*Iterator keyValuePairs1 = results.entrySet().iterator();
//get the sum of the results so i can do percents
for (int i = 0; i < mapsize; i++)
{
  Map.Entry<String,Integer> entry = (Map.Entry<String,Integer>) keyValuePairs1.next();
  Integer value = entry.getValue();
  total=total+value;
}*/
Iterator keyValuePairs2 = results.entrySet().iterator();
for (int i = 0; i < mapsize; i++)
{
  Map.Entry<String,Integer> entry = (Map.Entry<String,Integer>) keyValuePairs2.next();
  String key = entry.getKey();
  Integer value = entry.getValue();
  if(key!=null){
	  dataset.setValue(key,(double)(value/*/total*/));}
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
	CategoryAxis domainAxis=(CategoryAxis)plot.getDomainAxis();
	domainAxis.setLabel("Concept");
	domainAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
	NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
	rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	Range r =rangeAxis.getRange();
	Range s=new Range(0,r.getUpperBound()+r.getUpperBound()*0.15);
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

/*private void renderConceptAnalysisNew(String concept_key, String result_instance_id1, String result_instance_id2, PrintWriter pw, HttpServletRequest request)
{
	log.debug("renderConceptAnalysisNew: rendering "+concept_key)
try
{
	log.debug("Rendering concept analysis for concept key: "+concept_key)
	*//*get variables*//*
	String concept_cd=null;
	String concept_name=null;
	concept_cd=i2b2HelperService.getConceptCodeFromKey(concept_key);
	concept_name=i2b2HelperService.getShortNameFromKey(concept_key);
	log.debug("concept:"+concept_key+" - concept_cd "+concept_cd);

	StringWriter sw1=new StringWriter();
	StringWriter sw2=new StringWriter();
	*//*which subsets are present? *//*
	boolean s1=true;
	boolean s2=true;
	if(result_instance_id1=="" || result_instance_id1==null){s1=false;}
	if(result_instance_id2=="" || result_instance_id2==null){s2=false;}

	if(i2b2HelperService.isValueConceptCode(concept_cd))
	{

		*//*get the data*//*
		String      parentConcept = i2b2HelperService.lookupParentConcept(i2b2HelperService.keyToPath(concept_key));
		Set<String> childConcepts = new HashSet<String>();
		if (parentConcept==null) {
			childConcepts.add(concept_cd);
		} else{
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

		log.debug("\tvaluesAlist3:"+valuesAlist3);
		log.debug("\tvaluesAlist4:"+valuesAlist4);

		double[] values3 = valuesAlist3.toArray();
		double[] values4 = valuesAlist4.toArray();

		//double[] values3=i2b2HelperService.getConceptDistributionDataForValueConcept(concept_key, result_instance_id1);
		//double[] values4=i2b2HelperService.getConceptDistributionDataForValueConcept(concept_key, result_instance_id2);

		*//*render the double histogram*//*
		HistogramDataset dataset3 = new HistogramDataset();
		if(s1){dataset3.addSeries("Subset 1", values3, 10, StatHelper.min(values3), StatHelper.max(values3));}
		if(s2){dataset3.addSeries("Subset 2", values4, 10, StatHelper.min(values4), StatHelper.max(values4));}
		JFreeChart chart3 = ChartFactory.createHistogram(
				"Histogram of "+concept_name,
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



		*//*get the data*//*
		*//*
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
		*//*

		def results1;
		def results2;

		log.debug("B getting data distribution for child concepts by trial");

		results1 = i2b2HelperService.getConceptDistributionDataForValueConceptByTrialByConcepts(childConcepts, result_instance_id1);
		results2 = i2b2HelperService.getConceptDistributionDataForValueConceptByTrialByConcepts(childConcepts, result_instance_id2);

		log.debug("s1: "+s1+", s2: "+s2+", results1: "+results1+", results2: "+results2)
		log.debug("class of results1: "+results1.getClass()+", class of results2:"+results2.getClass());

		log.trace(results1 as JSON)
		log.trace("I GOT HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
		def width=200;
		def offset=40;
		DefaultBoxAndWhiskerCategoryDataset dataset= new DefaultBoxAndWhiskerCategoryDataset();
		BoxAndWhiskerItem boxitem;
		if(s1 && results1.size() > 0) {
			log.debug("size of results1: "+results1.size());
			for(key in results1.keySet()) {
				boxitem=BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(results1[key]);
				dataset.add(boxitem, "Subset 1", key);
				width=width+offset;
				sw1.write("<td>")
				renderBoxAndWhiskerInfoTableNew(results1[key], key, sw1);
				sw1.write("</td>")
				}
			if (results1.size() > 1) {
				// add an extra item with data for all trials
				ArrayList<Number> vals = values3;
				boxitem=BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(vals);
				dataset.add(boxitem, "Subset 1", "All trials");
				width=width+offset;
				sw1.write("<td>")
				renderBoxAndWhiskerInfoTableNew(vals, "All trials", sw1);
				sw1.write("</td>")
				}
			} else {
				log.debug("No result found for either "+concept_name+" or equivalent concepts for subset 1.")
			}
		if(s2 && results2.size() > 0 ) {
			log.debug("size of results2: "+results2.size());
			for(key in results2.keySet()) {
				boxitem=BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(results2[key]);
				dataset.add(boxitem, "Subset 2", key);
				width=width+offset;
				sw2.write("<td>")
				renderBoxAndWhiskerInfoTableNew(results2[key], key, sw2);
				sw2.write("<td>")
				}
			if (results2.size() > 1) {
				// add an extra item with data for all trials
				ArrayList<Number> vals = values4;
				boxitem=BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(vals);
				dataset.add(boxitem, "Subset 2", "All trials");
				width=width+offset;
				sw2.write("<td>")
				renderBoxAndWhiskerInfoTableNew(vals, "All trials", sw2);
				sw2.write("</td>")
				}
			} else {
				log.debug("No results found for either "+concept_name+" or equivalent concepts for subset 2.")
			}
		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
				"Comparison of "+concept_name, "Trial", "Value", dataset,
				true);
		chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 12));
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setDomainGridlinesVisible(true);
		CategoryAxis domainAxis =(CategoryAxis)plot.getDomainAxis();
		BoxAndWhiskerRenderer rend=(BoxAndWhiskerRenderer)plot.getRenderer();
		rend.setMaximumBarWidth(0.10);

		//adjust the width depending on number of sets

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		//rangeAxis7.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

		String filename = ServletUtilities.saveChartAsJPEG(chart, width, 300, info, request.getSession());
		String graphURL = request.getContextPath() + "/chart/displayChart?filename=" + filename;
		pw.write("<table>");
		pw.write("<tr><td align='center' colspan='5'><div class='analysistitle'>Analysis of "+concept_name+" for subsets:</div></td></tr>");
		pw.write("<tr>");
		pw.write("<td><img src='"+graphURL3+"' width=245 height=180 border=0 usemap='#"+filename3+"'>");
		ChartUtilities.writeImageMap(pw, filename3, info3, false);
		pw.write("</td>");
		pw.write("<td>");
		pw.write("<img src='"+graphURL+"' width="+width+" height=300 border=0 usemap='#"+filename+"'>");
		ChartUtilities.writeImageMap(pw, filename, info, false);
		pw.write("</td>");
		pw.write("<td valign='top'><div style='position:relative;left:-10px;'><a  href=\"javascript:showInfo('help/boxplot.html');\"><img src=\"../images/information.png\"></a></div></td>");
		pw.write("<td>")
		pw.write("<table><tr><td>");
		if(s1 && results1.size() > 0 ){
			pw.write("<table><tr><td colspan='"+results1.keySet().size()+"' align='center'>")
			pw.write("<h2>Subset 1</h2>");
			pw.write("</td></tr>");
			pw.write("<tr><td><table><tr>");
			pw.write(sw1.toString());
			pw.write("</tr></table></td></tr></table>");
		} else {
			log.debug("No results found for either "+concept_name+" or equivalent concepts for subset 1.")
		}
		pw.write("</td><td>");
		if(s2 && results2.size() > 0 ){
			pw.write("<table><tr><td colspan='"+results2.keySet().size()+"' align='center'>")
			pw.write("<h2>Subset 2</h2>");
			pw.write("</td></tr>");
			pw.write("<tr><td><table><tr>");
			pw.write(sw2.toString());
			pw.write("</tr></table></td></tr></table>");
		} else {
			log.debug("No results found for either "+concept_name+" or equivalent concepts for subset 1.")
		}
		pw.write("</td><tr><td align=\"center\" colspan=2>");

		// significance test
		renderTTestHashMap(results1, results2, pw);

		if (s1 && results1.size() == 0) {
			pw.write("No results found for either "+concept_name+" or equivalent concepts for subset 1.")
		}
		if (s2 && results2.size() == 0) {
			pw.write("No results found for either "+concept_name+" or equivalent concepts for subset 2.")
		}


		pw.write("</td></tr></td></tr></table></td></tr></table>")

	}
	else
	{
		log.debug("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		log.debug("Wasn't a value concept, doing a non-value concept analysis:")
		HashMap<String, Integer> results1;
		HashMap<String, Integer> results2;
		if (s1) {
			results1=i2b2HelperService.getConceptDistributionDataForConcept(concept_key, result_instance_id1);
			log.debug("concept_key:" + concept_key + ", results1: " + results1);
		}
		if (s2) {
			results2=i2b2HelperService.getConceptDistributionDataForConcept(concept_key, result_instance_id2);
			log.debug("concept_key:" + concept_key + ", results2: " + results2);
		}
		int height=80+15*results1.size();
		*//*printHashMap(results1, pw);*//*

		pw.write("<table width='100%'><tr>");
		pw.write("<tr><td align='center' colspan='2'><div class='analysistitle'>Analysis of "+concept_name+" for subsets:</div></td></tr>");
		pw.write("<tr><td width='50%'>");
		if(s1){

			JFreeChart chart5=createConceptAnalysisBarChart(hashMapToCategoryDataset(results1, "Subset 1"), "Subset 1");
			ChartRenderingInfo info5 = new ChartRenderingInfo(new StandardEntityCollection());
			String filename5 = ServletUtilities.saveChartAsJPEG(chart5, 400, height, info5, request.getSession());
			String graphURL5 = request.getContextPath() + "/chart/displayChart?filename=" + filename5;
			pw.write("<img src='"+graphURL5+"' width=400 height="+height+" border=0 usemap='#"+filename5+"'>");
			ChartUtilities.writeImageMap(pw, filename5, info5, false);
		}
		pw.write("</td><td align='center'>");
		if(s2){
			JFreeChart chart6=createConceptAnalysisBarChart(hashMapToCategoryDataset(results2, "Subset 2"), "Subset 2");
			ChartRenderingInfo info6 = new ChartRenderingInfo(new StandardEntityCollection());
			String filename6 = ServletUtilities.saveChartAsJPEG(chart6, 400, height, info6, request.getSession());
			String graphURL6 = request.getContextPath() + "/chart/displayChart?filename=" + filename6;
			pw.write("<img src='"+graphURL6+"' width=400 height="+height+" border=0 usemap='#"+filename6+"'>");
			ChartUtilities.writeImageMap(pw, filename6, info6, false);
		}
		pw.write("</td><tr><td align='center'>");
		if(s1){
			renderCategoryResultsHashMap(results1,"Subset 1",i2b2HelperService.getPatientSetSize(result_instance_id1), pw);
		}
		pw.write("</td><td align='center'>");
		if(s2){
			renderCategoryResultsHashMap(results2, "Subset 2",i2b2HelperService.getPatientSetSize(result_instance_id2), pw);
		}
		pw.write("</td></tr><tr><td align=\"center\" colspan=2><p>");
		renderChiSquaredHashMap(results1, results2, pw);
		pw.write("<td><tr><p></table>")

		}
		log.debug("renderConceptAnalysisNew: finished rendering "+concept_key)
	}
	catch(Exception e){log.error(e);}
}*/
    private void renderConceptAnalysisNew(String concept_key, TransmartQueryDefinition tQD1, TransmartQueryDefinition tQD2, PrintWriter pw, HttpServletRequest request){
        renderConceptAnalysisNew(concept_key,null,tQD1, tQD2, pw, request);
    }

    private void renderConceptAnalysisNew(String concept_key,String concept_cd, TransmartQueryDefinition tQD1, TransmartQueryDefinition tQD2, PrintWriter pw, HttpServletRequest request)
    {
        log.debug("renderConceptAnalysisNew: rendering "+concept_key)
        try {
            log.debug("Rendering concept analysis for concept key: "+concept_key)
            /*get variables*/
            String concept_name=null;
            if (concept_cd == null){
                concept_cd=i2b2HelperService.getConceptCodeFromLongPath(concept_key);
            }
            concept_name=i2b2HelperService.getShortNameFromKey(concept_key);
            log.debug("concept:"+concept_key+" - concept_cd "+concept_cd);

            StringWriter sw1=new StringWriter();
            StringWriter sw2=new StringWriter();


            if(i2b2HelperService.isValueConceptCode(concept_cd)) {
                renderValueConceptCode(tQD1, tQD2, concept_key, concept_cd, concept_name, request, sw1, sw2, pw);
            }
            else {
                renderNonValueConcept(tQD1, tQD2, concept_key, pw, concept_name, request);
            }

            log.debug("renderConceptAnalysisNew: finished rendering "+concept_key);

        }
        catch(Exception e) {
            log.error(e); e.printStackTrace();
        }
    }

    private void renderNonValueConcept(TransmartQueryDefinition tQD1, TransmartQueryDefinition tQD2, String concept_key, PrintWriter pw, String concept_name, HttpServletRequest request) {

        boolean s1=tQD1?.resultInstanceId?.trim();
        boolean s2=tQD2?.resultInstanceId?.trim();

        log.debug("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        log.debug("Wasn't a value concept, doing a non-value concept analysis:")
        HashMap<String, Integer> results1;
        HashMap<String, Integer> results2;
        if (s1) {
            if (!tQD1.isSameEvent) {
                results1 = i2b2HelperService.getConceptDistributionDataForConcept(concept_key, tQD1.resultInstanceId);
            }
            else {
                results1 = i2b2HelperService.getConceptDistributionDataForConceptSameEvent(concept_key, tQD1);
            }
            log.debug("concept_key:" + concept_key + ", results1: " + results1);
        }
        if (s2) {
            if (!tQD2.isSameEvent) {
                results2 = i2b2HelperService.getConceptDistributionDataForConcept(concept_key, tQD2.resultInstanceId);
            }
            else {
                results2 = i2b2HelperService.getConceptDistributionDataForConceptSameEvent(concept_key, tQD2);
            }
            log.debug("concept_key:" + concept_key + ", results2: " + results2);
        }
        int height = 80 + 15 * results1.size();
        /*printHashMap(results1, pw);*/

        pw.write("<table width='100%'>");
        pw.write("<tr><td align='center' colspan='2'><div class='analysistitle'>Analysis of " + concept_name + " for subsets:</div></td></tr>");
        pw.write("<tr><td width='50%'>");
        if (s1) {

            JFreeChart chart5 = createConceptAnalysisBarChart(hashMapToCategoryDataset(results1, "Subset 1"), "Subset 1");
            ChartRenderingInfo info5 = new ChartRenderingInfo(new StandardEntityCollection());
            String filename5 = ServletUtilities.saveChartAsJPEG(chart5, 400, height, info5, request.getSession());
            String graphURL5 = request.getContextPath() + "/chart/displayChart?filename=" + filename5;
            pw.write("<img src='" + graphURL5 + "' width=400 height=" + height + " border=0 usemap='#" + filename5 + "' alt='Summary Stats Graph'>");
            ChartUtilities.writeImageMap(pw, filename5, info5, false);
        }
        pw.write("</td><td align='center'>");
        if (s2) {
            JFreeChart chart6 = createConceptAnalysisBarChart(hashMapToCategoryDataset(results2, "Subset 2"), "Subset 2");
            ChartRenderingInfo info6 = new ChartRenderingInfo(new StandardEntityCollection());
            String filename6 = ServletUtilities.saveChartAsJPEG(chart6, 400, height, info6, request.getSession());
            String graphURL6 = request.getContextPath() + "/chart/displayChart?filename=" + filename6;
            pw.write("<img src='" + graphURL6 + "' width=400 height=" + height + " border=0 usemap='#" + filename6 + "' alt='Summary Stats Graph'>");
            ChartUtilities.writeImageMap(pw, filename6, info6, false);
        }
        pw.write("</td></tr><tr><td align='center'>");
        if (s1) {
            renderCategoryResultsHashMap(results1, "Subset 1", i2b2HelperService.getPatientSetSize(tQD1.resultInstanceId), pw);
        }
        pw.write("</td><td align='center'>");
        if (s2) {
            renderCategoryResultsHashMap(results2, "Subset 2", i2b2HelperService.getPatientSetSize(tQD2.resultInstanceId), pw);
        }
        pw.write("</td></tr><tr><td align=\"center\" colspan=2>");
        renderChiSquaredHashMap(results1, results2, pw);
        pw.write("</td></tr></table>")
    }

    private void renderValueConceptCode(TransmartQueryDefinition tQD1, TransmartQueryDefinition tQD2, String concept_key, String concept_cd, String concept_name, HttpServletRequest request, StringWriter sw1, StringWriter sw2, PrintWriter pw) {

        boolean s1=tQD1?.resultInstanceId?.trim();
        boolean s2=tQD2?.resultInstanceId?.trim();

        /*get the data*/
        String parentConcept = i2b2HelperService.lookupParentConcept(i2b2HelperService.keyToPath(concept_key));
        Set<String> childConcepts = new HashSet<String>();
        if (parentConcept == null) {
            childConcepts.add(concept_cd);
        } else {
            childConcepts.addAll(i2b2HelperService.lookupChildConcepts(parentConcept, tQD1.resultInstanceId, tQD2.resultInstanceId));
        }

        ArrayList<Double> valuesAlist3 = new ArrayList<Double>();
        ArrayList<Double> valuesAlist4 = new ArrayList<Double>();

        log.debug("A iterating through child concepts");
        for (c in childConcepts) {
            log.debug("\tc: " + c);
            valuesAlist3.addAll(i2b2HelperService.getConceptDistributionDataForValueConceptFromCode(c, tQD1?.resultInstanceId));
            log.trace("added to values3");
            valuesAlist4.addAll(i2b2HelperService.getConceptDistributionDataForValueConceptFromCode(c, tQD2?.resultInstanceId));
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

        def results1;
        def results2;

        log.debug("B getting data distribution for child concepts by trial");

        results1 = i2b2HelperService.getConceptDistributionDataForValueConceptByTrialByConcepts(childConcepts, tQD1?.resultInstanceId);
        results2 = i2b2HelperService.getConceptDistributionDataForValueConceptByTrialByConcepts(childConcepts, tQD2?.resultInstanceId);

        log.debug("s1: " + s1 + ", s2: " + s2 + ", results1: " + results1 + ", results2: " + results2)
        log.debug("class of results1: " + results1.getClass() + ", class of results2:" + results2.getClass());

        log.trace(results1 as JSON)
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
                sw2.write("</td>")
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
        pw.write("<td><img src='" + graphURL3 + "' width=245 height=180 border=0 usemap='#" + filename3 + "' alt='Summary Stats Graph'>");
        ChartUtilities.writeImageMap(pw, filename3, info3, false);
        pw.write("</td>");
        pw.write("<td>");
        pw.write("<img src='" + graphURL + "' width=" + width + " height=300 border=0 usemap='#" + filename + "' alt='Summary Stats Graph'>");
        ChartUtilities.writeImageMap(pw, filename, info, false);
        pw.write("</td>");
        pw.write("<td valign='top'><div style='position:relative;left:-10px;'><a  href=\"javascript:showInfo('help/boxplot.html');\"><img src=\"images/information.png\" alt='Information'>")
        pw.write("</a></div></td>");
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
        pw.write("</td></tr><tr><td align=\"center\" colspan=2>");

        // significance test
        renderTTestHashMap(results1, results2, pw);

        if (s1 && results1.size() == 0) {
            pw.write("No results found for either " + concept_name + " or equivalent concepts for subset 1.")
        }
        if (s2 && results2.size() == 0) {
            pw.write("No results found for either " + concept_name + " or equivalent concepts for subset 2.")
        }


        pw.write("</td></tr></table></td></tr></table>")
    }


    private void renderBoxAndWhiskerInfoTableNew(List<Number> values,String trial, StringWriter pw)
{
	NumberFormat form;
	form=DecimalFormat.getInstance();
	form.setMaximumFractionDigits(2);
	Number[] t=new Number[values.size()];
	BoxAndWhiskerItem b=BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(values);
	pw.write("<table class='analysis'>");
	pw.write("<tr><td><b>"+trial+"</b></td></tr>");
	pw.write("<tr><td><b>Mean:</b> "+form.format(b.getMean())+"</td></tr>");
	pw.write("<tr><td><b>Median:</b> "+form.format(b.getMedian())+"</td></tr>");
	pw.write("<tr><td><b>IQR:</b> "+form.format((b.getQ3().doubleValue()-b.getQ1().doubleValue()))+"</td></tr>");
	pw.write("<tr><td><b>SD:</b> "+form.format(Statistics.getStdDev(values.toArray(t)))+"</td></tr>");
	pw.write("<tr><td><b>Data Points:</b> "+values.size()+"</td></tr>");
	pw.write("</table>");
	return;
}
    private void renderModifierAnalysisNew(modifier, TransmartQueryDefinition tQD1, TransmartQueryDefinition tQD2, PrintWriter pw, HttpServletRequest request)
    {
        try
        {
            StringWriter sw1=new StringWriter();
            StringWriter sw2=new StringWriter();

            //Find which subsets are provided.
            boolean s1=true;
            boolean s2=true;

            def result_instance_id1 = tQD1?.resultInstanceId;
            def result_instance_id2 = tQD2?.resultInstanceId;

            if(result_instance_id1=="" || result_instance_id1==null){s1=false;}
            if(result_instance_id2=="" || result_instance_id2==null){s2=false;}

            //Determine if this modifier is a value code or not.
            if(modifier.oktousevalues == "Y")
            {

                ArrayList<Double> valuesAlist3 = new ArrayList<Double>();
                ArrayList<Double> valuesAlist4 = new ArrayList<Double>();

                valuesAlist3.addAll(i2b2ModifierHelperService.getConceptDistributionDataForValueModifierFromCode(modifier, result_instance_id1));
                valuesAlist4.addAll(i2b2ModifierHelperService.getConceptDistributionDataForValueModifierFromCode(modifier, result_instance_id2));

                log.debug("\tvaluesAlist3:"+valuesAlist3);
                log.debug("\tvaluesAlist4:"+valuesAlist4);

                double[] values3 = valuesAlist3.toArray();
                double[] values4 = valuesAlist4.toArray();

                /*render the double histogram*/
                HistogramDataset dataset3 = new HistogramDataset();
                if(s1){dataset3.addSeries("Subset 1", values3, 10, StatHelper.min(values3), StatHelper.max(values3));}
                if(s2){dataset3.addSeries("Subset 2", values4, 10, StatHelper.min(values4), StatHelper.max(values4));}
                JFreeChart chart3 = ChartFactory.createHistogram(
                        "Histogram of "+modifier.name,
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

                def results1;
                def results2;

                log.debug("B getting data distribution for child concepts by trial");

                results1 = i2b2ModifierHelperService.getModifierDistributionDataForValueModifierWithResultInstanceId(modifier, result_instance_id1);
                results2 = i2b2ModifierHelperService.getModifierDistributionDataForValueModifierWithResultInstanceId(modifier, result_instance_id2);

                def width=200;
                def offset=40;
                DefaultBoxAndWhiskerCategoryDataset dataset= new DefaultBoxAndWhiskerCategoryDataset();
                BoxAndWhiskerItem boxitem;
                if(s1 && results1.size() > 0) {
                    log.debug("size of results1: "+results1.size());
                    for(key in results1.keySet()) {
                        boxitem=BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(results1[key]);
                        dataset.add(boxitem, "Subset 1", key);
                        width=width+offset;
                        sw1.write("<td>")
                        renderBoxAndWhiskerInfoTableNew(results1[key], key, sw1);
                        sw1.write("</td>")
                    }
                    if (results1.size() > 1) {
                        // add an extra item with data for all trials
                        ArrayList<Number> vals = values3;
                        boxitem=BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(vals);
                        dataset.add(boxitem, "Subset 1", "All trials");
                        width=width+offset;
                        sw1.write("<td>")
                        renderBoxAndWhiskerInfoTableNew(vals, "All trials", sw1);
                        sw1.write("</td>")
                    }
                } else {
                    log.debug("No result found for either "+modifier.name+" or equivalent concepts for subset 1.")
                }
                if(s2 && results2.size() > 0 ) {
                    log.debug("size of results2: "+results2.size());
                    for(key in results2.keySet()) {
                        boxitem=BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(results2[key]);
                        dataset.add(boxitem, "Subset 2", key);
                        width=width+offset;
                        sw2.write("<td>")
                        renderBoxAndWhiskerInfoTableNew(results2[key], key, sw2);
                        sw2.write("</td>")
                    }
                    if (results2.size() > 1) {
                        // add an extra item with data for all trials
                        ArrayList<Number> vals = values4;
                        boxitem=BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(vals);
                        dataset.add(boxitem, "Subset 2", "All trials");
                        width=width+offset;
                        sw2.write("<td>")
                        renderBoxAndWhiskerInfoTableNew(vals, "All trials", sw2);
                        sw2.write("</td>")
                    }
                } else {
                    log.debug("No results found for either "+modifier.name+" or equivalent concepts for subset 2.")
                }
                JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
                        "Comparison of "+modifier.name, "Trial", "Value", dataset,
                        true);
                chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 12));
                CategoryPlot plot = (CategoryPlot) chart.getPlot();
                plot.setDomainGridlinesVisible(true);
                CategoryAxis domainAxis =(CategoryAxis)plot.getDomainAxis();
                BoxAndWhiskerRenderer rend=(BoxAndWhiskerRenderer)plot.getRenderer();
                rend.setMaximumBarWidth(0.10);

                //adjust the width depending on number of sets

                NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                //rangeAxis7.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

                ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

                String filename = ServletUtilities.saveChartAsJPEG(chart, width, 300, info, request.getSession());
                String graphURL = request.getContextPath() + "/chart/displayChart?filename=" + filename;
                pw.write("<table>");
                pw.write("<tr><td align='center' colspan='5'><div class='analysistitle'>Analysis of "+modifier.name+" for subsets:</div></td></tr>");
                pw.write("<tr>");
                pw.write("<td><img src='"+graphURL3+"' width=245 height=180 border=0 usemap='#"+filename3+"' alt='Summary Stats Graph'>");
                ChartUtilities.writeImageMap(pw, filename3, info3, false);
                pw.write("</td>");
                pw.write("<td>");
                pw.write("<img src='"+graphURL+"' width="+width+" height=300 border=0 usemap='#"+filename+"' alt='Summary Stats Graph'>");
                ChartUtilities.writeImageMap(pw, filename, info, false);
                pw.write("</td>");
                pw.write("<td valign='top'><div style='position:relative;left:-10px;'><a  href=\"javascript:showInfo('help/boxplot.html');\"><img src=\"images/information.png\" alt='Information'>")
                pw.write("</a></div></td>");
                pw.write("<td>")
                pw.write("<table><tr><td>");
                if(s1 && results1.size() > 0 ){
                    pw.write("<table><tr><td colspan='"+results1.keySet().size()+"' align='center'>")
                    pw.write("<h2>Subset 1</h2>");
                    pw.write("</td></tr>");
                    pw.write("<tr><td><table><tr>");
                    pw.write(sw1.toString());
                    pw.write("</tr></table></td></tr></table>");
                } else {
                    log.debug("No results found for either "+modifier.name+" or equivalent concepts for subset 1.")
                }
                pw.write("</td><td>");
                if(s2 && results2.size() > 0 ){
                    pw.write("<table><tr><td colspan='"+results2.keySet().size()+"' align='center'>")
                    pw.write("<h2>Subset 2</h2>");
                    pw.write("</td></tr>");
                    pw.write("<tr><td><table><tr>");
                    pw.write(sw2.toString());
                    pw.write("</tr></table></td></tr></table>");
                } else {
                    log.debug("No results found for either "+modifier.name+" or equivalent concepts for subset 1.")
                }
                pw.write("</td><tr><td align=\"center\" colspan=2>");

                // significance test
                renderTTestHashMap(results1, results2, pw);

                if (s1 && results1.size() == 0) {
                    pw.write("No results found for either "+modifier.name+" or equivalent concepts for subset 1.")
                }
                if (s2 && results2.size() == 0) {
                    pw.write("No results found for either "+modifier.name+" or equivalent concepts for subset 2.")
                }


                pw.write("</td></tr></table></td></tr></table>")

            }
            else
            {
                log.debug("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                log.debug("Wasn't a value concept, doing a non-value concept analysis:")
                HashMap<String, Integer> results1;
                HashMap<String, Integer> results2;
                if (s1) {
                    results1=i2b2ModifierHelperService.getModifierDistributionDataForModifier(modifier, result_instance_id1);
                    log.debug("concept_key:" + modifier.modifierCode + ", results1: " + results1);
                }
                if (s2) {
                    results2=i2b2ModifierHelperService.getModifierDistributionDataForModifier(modifier, result_instance_id2);
                    log.debug("concept_key:" + modifier.modifierCode + ", results2: " + results2);
                }
                int height=80+15*results1.size();

                pw.write("<table width='100%'>");
                pw.write("<tr><td align='center' colspan='2'><div class='analysistitle'>Analysis of "+modifier.name+" for subsets:</div></td></tr>");
                pw.write("<tr><td width='50%'>");
                if(s1){

                    JFreeChart chart5=createConceptAnalysisBarChart(hashMapToCategoryDataset(results1, "Subset 1"), "Subset 1");
                    ChartRenderingInfo info5 = new ChartRenderingInfo(new StandardEntityCollection());
                    String filename5 = ServletUtilities.saveChartAsJPEG(chart5, 400, height, info5, request.getSession());
                    String graphURL5 = request.getContextPath() + "/chart/displayChart?filename=" + filename5;
                    pw.write("<img src='"+graphURL5+"' width=400 height="+height+" border=0 usemap='#"+filename5+"' alt='Summary Stats Graph'>");
                    ChartUtilities.writeImageMap(pw, filename5, info5, false);
                }
                pw.write("</td><td align='center'>");
                if(s2){
                    JFreeChart chart6=createConceptAnalysisBarChart(hashMapToCategoryDataset(results2, "Subset 2"), "Subset 2");
                    ChartRenderingInfo info6 = new ChartRenderingInfo(new StandardEntityCollection());
                    String filename6 = ServletUtilities.saveChartAsJPEG(chart6, 400, height, info6, request.getSession());
                    String graphURL6 = request.getContextPath() + "/chart/displayChart?filename=" + filename6;
                    pw.write("<img src='"+graphURL6+"' width=400 height="+height+" border=0 usemap='#"+filename6+"' alt='Summary Stats Graph'>");
                    ChartUtilities.writeImageMap(pw, filename6, info6, false);
                }
                pw.write("</td></tr><tr><td align='center'>");
                if(s1){
                    renderCategoryResultsHashMap(results1,"Subset 1",i2b2HelperService.getPatientSetSize(result_instance_id1), pw);
                }
                pw.write("</td><td align='center'>");
                if(s2){
                    renderCategoryResultsHashMap(results2, "Subset 2",i2b2HelperService.getPatientSetSize(result_instance_id2), pw);
                }
                pw.write("</td></tr><tr><td align=\"center\" colspan=2>");
                renderChiSquaredHashMap(results1, results2, pw);
                pw.write("</td></tr></table>")

            }
            log.debug("renderConceptAnalysisNew: finished rendering "+modifier.name)
        }
        catch(Exception e){log.error(e); e.printStackTrace();}
    }

    private addLeafConceptToTable(String result_instance_id1, String result_instance_id2, org.apache.commons.logging.Log log, String concept_key, ExportTableNew table, boolean s1, boolean s2) {
        String parentConcept;
        def conceptKeys = [];
        Set<String> cconcepts = i2b2HelperService.lookupChildConcepts(parentConcept, result_instance_id1, result_instance_id2);
        //	println(prefix);
        log.debug("child concepts: "+cconcepts);
        // need to modify to try looking for equivalent concepts on both subsets
        parentConcept = i2b2HelperService.lookupParentConcept(i2b2HelperService.keyToPath(concept_key));
        log.debug("parent concept: "+parentConcept);
        def prefix = concept_key.substring(0, concept_key.indexOf("\\",2));
        if (!cconcepts.isEmpty()) {
            //
            for(cc in cconcepts){
                def ck = prefix+i2b2HelperService.getConceptPathFromCode(cc);
                conceptKeys.add(ck);
            }
        }else{
            conceptKeys.add(concept_key);
        }

        for(ck in conceptKeys){
            if(s1){gridViewService.addConceptDataToTable(table, ck, result_instance_id1);}
            if(s2){gridViewService.addConceptDataToTable(table, ck, result_instance_id2);}
        }
    }

    private addFolderConceptToTable(String result_instance_id1, String result_instance_id2, org.apache.commons.logging.Log log, String concept_key, ExportTableNew table) {

        def childPathsFromKey = i2b2HelperService.getChildPathsFromParentKey(concept_key);
        def s1 = result_instance_id1;
        def s2 = result_instance_id2;

        log.debug("child paths: " + childPathsFromKey);
        log.debug("parent concept: " + concept_key);

        for(ck in childPathsFromKey){
            if(s1){gridViewService.addFolderDataToTable(table, concept_key, ck, result_instance_id1);}
            if(s2){gridViewService.addFolderDataToTable(table, concept_key, ck, result_instance_id2);}
        }

    }
}
