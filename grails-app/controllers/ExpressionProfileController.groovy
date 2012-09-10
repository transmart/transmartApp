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
 * @author $Author: mmcduffie $
 * $Id: ExpressionProfileController.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * $Revision: 9178 $
 *
 */

 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
 import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
 import org.jfree.data.statistics.BoxAndWhiskerCalculator;
 import org.jfree.data.statistics.BoxAndWhiskerItem;
 import org.jfree.data.statistics.DefaultBoxAndWhiskerXYDataset;
 import org.jfree.chart.axis.AxisLocation;
 import org.jfree.chart.renderer.category.*;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.*;
 import org.jfree.chart.entity.*;
 import org.jfree.chart.servlet.*;
import org.transmart.ExpressionProfileResult;
import org.transmart.biomart.BioAssayDataStatistics;

 import org.transmart.biomart.BioMarker;
 import org.transmart.biomart.Disease;
import javax.servlet.ServletException;

public class ExpressionProfileController{

	def expressionProfileQueryService

	def datasourceResult = {
			
		// reset profile filter
		session.searchFilter.exprProfileFilter.reset()

		// refresh experiment count
		log.info ">> Count query:"
		def profCount = expressionProfileQueryService.countExperiment(session.searchFilter)
		
		// initialize session with profile results
		ExpressionProfileResult epr = new ExpressionProfileResult();
		session.setAttribute("exprProfileResult", epr)
		
		// load genes and cache
		log.info ">> Gene Query:"
		def genes =  expressionProfileQueryService.listBioMarkers(session.searchFilter)
		if(genes.size()>0) session.searchFilter.exprProfileFilter.bioMarkerId = genes[0].id
		log.info "... number genes: " + genes.size()

		log.info ">> Diseases Query:"
		def diseases = expressionProfileQueryService.listDiseases(session.searchFilter)			
		if(diseases!=null && !diseases.isEmpty()) session.searchFilter.exprProfileFilter.bioDiseaseId = diseases[0].id
		
		def probesets = []
		if(genes!=null && !genes.isEmpty() && diseases!=null && !diseases.isEmpty()){
			log.info ">> Probesets Query:"
			probesets = expressionProfileQueryService.getProbesetsByBioMarker(genes[0], diseases[0]);
			session.searchFilter.exprProfileFilter.probeSet = probesets[0]		
			
			// build graph results, stores in session
			createGraph()			
		}
				
		// cache results
		epr.genes = genes
		epr.diseases = diseases
		epr.probeSets = probesets
		epr.profCount = profCount
		
		renderProfileView();
	}

	def selectGene = {
		log.info ">> selectGene:" + params.bioMarkerId

		// get profile results
		ExpressionProfileResult epr = session.exprProfileResult
				
		// refresh filter selections
		session.searchFilter.exprProfileFilter.reset()
		
		// bind gene selection	
		bindData(session.searchFilter.exprProfileFilter, params)
		BioMarker marker = BioMarker.get(session.searchFilter.exprProfileFilter.bioMarkerId)
		
		// refresh diseases 
		def diseases = expressionProfileQueryService.listDiseases(session.searchFilter)
		session.searchFilter.exprProfileFilter.bioDiseaseId = diseases[0].id
		epr.diseases = diseases
		
		// refresh probesets using first disease
		def probesets = expressionProfileQueryService.getProbesetsByBioMarker(marker, diseases[0]);
		session.searchFilter.exprProfileFilter.probeSet = probesets[0]
		epr.probeSets = probesets
		
		// build graph
		createGraph()
		
		renderProfileView();
	}

	def selectDisease = {
		log.info "select Disease:"+params.bioDiseaseId
		bindData(session.searchFilter.exprProfileFilter, params)

		// get profile results
		ExpressionProfileResult epr = session.exprProfileResult
		
		// load selections
		BioMarker marker = BioMarker.get(session.searchFilter.exprProfileFilter.bioMarkerId)
		Disease disease = Disease.get(session.searchFilter.exprProfileFilter.bioDiseaseId)
				
		// refresh probesets using first disease
		def probesets = expressionProfileQueryService.getProbesetsByBioMarker(marker, disease)
		epr.probeSets = probesets
		session.searchFilter.exprProfileFilter.probeSet = probesets[0]
		
		// build graph and dataset info
		createGraph()
		
		renderProfileView();
	}

	def selectProbeset = {
		log.info "select Probeset:"+params.probeSet
		bindData(session.searchFilter.exprProfileFilter, params)		
		
		// only update graph
		createGraph()
		render(template:'graphView', model:[epr:session.exprProfileResult])    
	}
	
    /**
     * render expression profile view for indicated model
     */
    def renderProfileView = {
    	// grab model from session
    	render(view:'expressionProfileView', model:[epr:session.exprProfileResult])    		
    }
	
	def createGraph(){
		// session items
		def eFilter = session.searchFilter.exprProfileFilter
		def epr = session.exprProfileResult
		
		//setup variables
		DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		BoxAndWhiskerItem boxitem;

		def height=200;
		def offset=10;
		def chartname = "Box Plot"
		if(eFilter.filterBioMarker()){
			chartname = BioMarker.get(eFilter.bioMarkerId).name
		}

		log.info ">> Boxplot query:"
		def allData = expressionProfileQueryService.queryStatisticsDataExpField(session.searchFilter);
		// don't create graph if no data
		log.info "... number boxplot filter records: " + allData.size()
		if(allData.size()==0) {
			epr.graphURL = "empty"
			epr.datasetItems = null	
			return
		}
		
		def seriesName=""
		def itemName=""
		def dsItems = []
		def chartMinVal = null
		def chartMaxVal = null
		def statdata = null;
		int i = 0
		
		for(drow in allData) {
			// BioAssayDataStats record
			statdata = drow[0];
			dsItems.add(statdata)
			
			seriesName = drow[1];	
			itemName = statdata.dataset.name+"("+statdata.sampleCount+")"
						
			// min, max outlier settings
			def minOutlier
			def maxOutlier 
			if(statdata.minValue!=null) {
				if(chartMinVal==null) chartMinVal = statdata.minValue		
				minOutlier = Math.min(0,statdata.minValue-2)			
				if(statdata.minValue<chartMinVal) chartMinVal = statdata.minValue				
			}
			
			if(statdata.maxValue!=null){ 
				if(chartMaxVal==null) chartMaxVal = statdata.maxValue
				maxOutlier = statdata.maxValue+3;
				if(statdata.maxValue>chartMaxVal) chartMaxVal = statdata.maxValue	
			}
											
			boxitem=new BoxAndWhiskerItem(statdata.meanValue,
					statdata.quartile2,
					statdata.quartile1,
					statdata.quartile3,
					statdata.minValue,
					statdata.maxValue,
					minOutlier,
					maxOutlier,
					null);
			dataset.add(boxitem, seriesName, itemName);
			height=height+offset;			
		}
		
		//create the chart
		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(chartname, "Samples", "Log(2) Expression", dataset, true);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        chart.setBackgroundPaint(java.awt.Color.white);
        plot.setBackgroundPaint(new java.awt.Color(245,250,250));
        plot.setDomainGridlinePaint(java.awt.Color.lightGray);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(java.awt.Color.lightGray);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setLowerBound(chartMinVal-0.5);
        rangeAxis.setUpperBound(chartMaxVal+0.5)       
        log.info "INFO: calculated info ... lowest val: "+chartMinVal+"; highest val: "+chartMaxVal
        
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        BoxAndWhiskerRenderer rend =(BoxAndWhiskerRenderer)plot.getRenderer();

        rend.setMaximumBarWidth(0.2);
		rend.setFillBox(true);

		ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
        String filename = ServletUtilities.saveChartAsJPEG(chart, 800, height, info, session);
		String graphURL = request.getContextPath() + "/expressionProfile/displayChart?filename=" + filename;
		log.info graphURL
		
		// store results
		epr.graphURL = graphURL
		epr.datasetItems = dsItems
	}

	def displayChart = {

		String filename = request.getParameter("filename");
        // log.info "Trying to display:"+filename
        if (filename != null) {

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
        }
        return;
    }
        
    def printChart = {
    	render(view:'printView', model:[filename:params.filename])		
    }
        
}
