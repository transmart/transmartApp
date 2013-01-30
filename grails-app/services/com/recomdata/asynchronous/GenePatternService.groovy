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
* $Id: GenePatternService.groovy 10246 2011-10-27 19:58:02Z mmcduffie $
*/
package com.recomdata.asynchronous

import java.awt.Image
import java.awt.Rectangle
import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

import javax.imageio.ImageIO

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.genepattern.client.GPClient
import org.genepattern.webservice.JobResult
import org.genepattern.webservice.Parameter
import org.genepattern.webservice.WebServiceException

import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.web.context.request.RequestContextHolder

import com.recomdata.export.GwasFiles;
import com.recomdata.export.IgvFiles
import com.recomdata.genepattern.JobStatus
import com.sun.pdfview.PDFFile
import com.sun.pdfview.PDFPage

/**
* GenePatternService that manages the calls and jobs to the GenePattern server.
* Implements Job as this will run asynchronously
*
* @author $Author: mmcduffie $
* @version $Revision: 10246 $
*/
class GenePatternService implements Job {
	static scope= "session"
	GPClient gpClient = null
		
	// TODO: Figure out why dependency injection is not working as before.  Is the implements Job causing an issue? 
	def ctx = AH.application.mainContext
	def springSecurityService = ctx.springSecurityService
	def i2b2HelperService = ctx.i2b2HelperService
	def jobResultsService = ctx.jobResultsService
	
	/**
	* Quartz job execute method
	*/
    public void execute (JobExecutionContext jobExecutionContext) {
	    def gpURL = CH.config.com.recomdata.datasetExplorer.genePatternURL + "/gp/jobResults/" 
		def group = "heatmaps"
	   
	    def jobDetail = jobExecutionContext.getJobDetail()
	    def jobName = jobDetail.getName()
	    log.info("${jobName} has been triggered to run ")
	   
	    def jobDataMap = jobDetail.getJobDataMap()
	    if (log.isDebugEnabled())	{
		    jobDataMap.getKeys().each {_key ->
		 	   log.debug("\t${_key} -> ${jobDataMap[_key]}")
		    }
	    } 
	   
		// Note: this is a superset of all parameters for all of the different analysis types.  
		// Some will be present, others will not depending on the type of job
	    def analysis = jobDataMap.get("analysis")		
	    def gctFile = jobDataMap.get("gctFile")
		def clsFile = jobDataMap.get("clsFile")
	    def resulttype = jobDataMap.get("resulttype")
		def userName = jobDataMap.get("userName")
		def nClusters = jobDataMap.get("nclusters")
		def imgTmpDir = jobDataMap.get("imgTmpDir")
		def imgTmpPath = jobDataMap.get("imgTmpPath")
		def ctxtPath = jobDataMap.get("ctxtPath")
		def querySum1 = jobDataMap.get("querySum1")
		def querySum2 = jobDataMap.get("querySum2")
		
		//log.debug("Checking to see if the user cancelled the job")
		if (jobResultsService[jobName]["Status"] == "Cancelled")	{
			log.warn("${jobName} has been cancelled")
			return
		}
		
		JobResult[] jresult
		String sResult
		try	{
			if (analysis == "Compare") {
				jresult = HMap(userName, jobName, gctFile, resulttype)
			} else if (analysis == "Cluster")	{
				jresult = HCluster(userName, jobName, gctFile, resulttype)
			} else if (analysis == "KMeans")	{						
				jresult = kMeansCluster(userName, jobName, gctFile, resulttype, nClusters)
			} else if (analysis == "Select") {
				jresult = CMAnalysis(userName, jobName, gctFile, clsFile, resulttype)
			} else if (analysis == "PCA")	{
				jresult = PCA(userName, jobName, gctFile, clsFile, resulttype)
			} else if (analysis == "Survival")	{
				sResult = survivalAnalysis(userName, jobName, gctFile, clsFile, imgTmpPath, imgTmpDir, ctxtPath, querySum1, querySum2)
			} else if (analysis == "GWAS")	{
				GwasFiles gwasFiles = jobDataMap.get("gwasFiles");
				sResult = gwas(userName, jobName, gwasFiles, querySum1, querySum2);
			} else	{
				log.error("Analysis not implemented yet!")
			}			
		} catch(WebServiceException wse)	{
			log.error("WebServiceException thrown executing job: " + wse.getMessage(), wse)
			jobResultsService[jobName]["Exception"] = wse.getMessage()
			return
		}
		
		if (analysis.equals("Survival")) {
			def jobResults = "<html><header><title>Survival Analysis</title></header><body>" + sResult + "</body></html>"
			jobResultsService[jobName]["Results"] = jobResults
		}	
		else if (analysis.equals("GWAS")) {
			def jobResults = sResult;
			jobResultsService[jobName]["Results"] = jobResults;
		}	
		else {
			def viewerURL = gpURL + jresult[1].getJobNumber() + "?openVisualizers=true"
			log.debug("URL for viewer: " + viewerURL)		
			jobResultsService[jobName]["ViewerURL"] = viewerURL
		
			if (analysis == "Select") {
				def altviewerURL = gpURL + jresult[2].getJobNumber() + "?openVisualizers=true"
				log.debug("URL for second viewer: " + altviewerURL)
				jobResultsService[jobName]["AltViewerURL"] = altviewerURL
			}
		}
    }
	
	/**
	 * Helper to update the status of the job and log it
	 *
	 * @param jobName - the unique job name
	 * @param status - the new status
	 * @return
	 */
    def updateStatus(jobName, status)	{
		jobResultsService[jobName]["Status"] = status
	    log.debug(status)
    }
      
	private JobResult runJob(Parameter[] parameters, String analysisType) throws WebServiceException {
		GPClient gpClient = getGPClient();		
		JobResult result;
		startWorkflowJob(analysisType);
		
				log.debug("sending " + analysisType 
				+ " job to " + gpClient.getServer()
				+ " as user " + gpClient.getUsername()
				+ " with parameters " + parameters);

		try	{
			result = gpClient.runAnalysis(analysisType, parameters);
		} catch(WebServiceException wse)	{
			throw new WebServiceException("User needs to be registered on the Gene Pattern server")
		}
		
		log.debug("Response: " + result);
		log.debug("job number: " + result.getJobNumber());
		log.debug("job was run on: " + result.getServerURL());
		log.debug("Files:\n")
		for (String f : result.getOutputFileNames()) {
			log.debug("\t" + result.getURLForFileName(f) + "\n")
		}
		log.debug("\tdone listing files");
		log.debug("Parameters:");
		//for (Parameter p : result.getParameters()) {
		//	log.trace("\t file" + p + "\n");
		//	log.trace("\t url" + result.getURLForFileName(p) + "\n");
		//}
		log.debug("\tdone listing parameters");

		if (result.hasStandardError()) {
			URL stderrFileURL = result.getURLForFileName("stderr.txt");
			String stderrFile = (String) stderrFileURL.getContent();
			throw new WebServiceException(analysisType + " failed: " + stderrFile);
		}
		completeWorkflowJob(analysisType);
		
		return(result);
	}
	
	/**
	 * Run job with no workflow (asynchronous)
	 * 
	 * @param userName - The user requesting the job.
	 * @param parameters - the job parameters 
	 * @param analysisType - the type of job to perform on the GenePattern server
	 * 
	 * @return the job result from the GenePattern server
	 */
	private JobResult runJobNoWF(String userName, Parameter[] parameters, String analysisType) throws WebServiceException {
		GPClient gpClient = getGPClient(userName)				
		if (log.isDebugEnabled())	{
			log.debug("Sending ${analysisType} job to ${gpClient.getServer()}")
			log.debug("As user ${gpClient.getUsername()} with parameters: ")
			for (parameter in parameters)	{
				log.debug("\t${parameter}")
			}
		}
				
		JobResult result = gpClient.runAnalysis(analysisType, parameters)		
		if (log.isDebugEnabled())	{
			log.debug("Response: ${result}")
			log.debug("Job Number: ${result.getJobNumber()}")
			log.debug("Run on server: ${result.getServerURL()}")
			log.debug("Files:")
			for (filename in result.getOutputFileNames())	{
				log.debug("\t${filename}")
				log.debug("\t${result.getURLForFileName(filename)}")
			}
		}
		if (result.hasStandardError()) {
			log.error("Result has standard error")
			URL stderrFileURL = result.getURLForFileName("stderr.txt");
			String stderrFile = (String) stderrFileURL.getContent();
			log.error("${stderrFile}")
			throw new WebServiceException("${analysisType} failed: ${stderrFile}")
		}
		return result
	}
	
	/**
	 * 
	 * Runs the clustering heatmap
	 * 
	 * @param userName - The user requesting the job
	 * @param jobName - The name of the job given by the GP controller
	 * @param gctFile - file with the heatmap data
	 * @param resultType - applet or image
	 * 
	 * @return the JobResult array from the GenePattern server
	 */
	public JobResult[] HCluster(String userName, String jobName, File gctFile, String resultType) throws WebServiceException { 
		Parameter    inputFilename          = new Parameter("input.filename", gctFile);
		Parameter    columnDistanceMeasure  = new Parameter("column.distance.measure", 2);
		Parameter    rowDistanceMeasure     = new Parameter("row.distance.measure", 2);
		Parameter    clusteringMethod       = new Parameter("clustering.method", "m");
		
		Parameter[] clusterParameters  = new Parameter[4];
		clusterParameters[0] = inputFilename;
		clusterParameters[1] = columnDistanceMeasure;
		clusterParameters[2] = rowDistanceMeasure;
		clusterParameters[3] = clusteringMethod;
		
		updateStatus(jobName, "Performing Hierarchical Clustering")		
		JobResult preProcessed = runJobNoWF(userName, clusterParameters, "HierarchicalClustering");		
		JobResult viewed;

		if (resultType == "applet") {
			Parameter    cdtFile = new Parameter("cdt.file", preProcessed.getURL("cdt").toString());
			Parameter    gtrFile = new Parameter("gtr.file", preProcessed.getURL("gtr").toString());
			Parameter    atrFile = new Parameter("atr.file", preProcessed.getURL("atr").toString());		
			Parameter[] viewParameters  = new Parameter[3];
			viewParameters[0] = cdtFile;
			viewParameters[1] = gtrFile;
			viewParameters[2] = atrFile;
			updateStatus(jobName, "Running Hierarchical Clustering Viewer")
			viewed= runJobNoWF(userName, viewParameters, "HierarchicalClusteringViewer");
		} else {
			Parameter    cdtFile    = new Parameter("cdt", preProcessed.getURL("cdt").toString());
			Parameter    gtrFile    = new Parameter("gtr", preProcessed.getURL("gtr").toString());
			Parameter    atrFile    = new Parameter("atr", preProcessed.getURL("atr").toString());		
			Parameter    columnSize = new Parameter("column.size", 10);
			Parameter    rowSize    = new Parameter("row.size", 10);
			Parameter    rowDescs   = new Parameter("show.row.descriptions", "yes");

			Parameter[] viewParameters  = new Parameter[6];
			viewParameters[0] = cdtFile;
			viewParameters[1] = gtrFile;
			viewParameters[2] = atrFile;
			viewParameters[3] = columnSize;
			viewParameters[4] = rowSize;
			viewParameters[5] = rowDescs;
			log.debug("Run job to load the viewer")
			viewed= runJobNoWF(userName, viewParameters, "HierarchicalClusteringImage");
		}

		JobResult[] toReturn = [preProcessed, viewed]
		log.debug("Returning ${preProcessed} and ${viewed}")
		
		return toReturn
	}

	/**
	 * Runs the KMeans clustering heatmap
	 * 
	 * @param userName - The user requesting the job
	 * @param jobName - The name of the job given by the GP controller
	 * @param gctFile - file with the heatmap data
	 * @param nClusters - the number of clusters
	 * @param resultType - applet or image
	 */
	public JobResult[] kMeansCluster(String userName, String jobName, File gctFile, String resultType, String nClusters) throws WebServiceException {
		Integer nC = 1
		try	{
			nC = Integer.valueOf(nClusters)
		} catch(NumberFormatException nfe)	{
			log.warn("Cluster is not an integer ${nClusters}, using 1")
		}
		Parameter    inputFilename          = new Parameter("input.filename", gctFile);
		Parameter    numberOfClusters       = new Parameter("number.of.clusters", nC)
		Parameter    clusterBy              = new Parameter("cluster.by", 1); // 0 = rows, 1 = columns
		Parameter    distanceMetric         = new Parameter("distance.metric", 0); // 0 = Euclidean
		
		Parameter[] clusterParameters  = new Parameter[4];
		clusterParameters[0] = inputFilename;
		clusterParameters[1] = numberOfClusters;
		clusterParameters[2] = clusterBy;
		clusterParameters[3] = distanceMetric;
		
		updateStatus(jobName, "Performing KMeans Clustering")
		JobResult preProcessed = runJobNoWF(userName, clusterParameters, "KMeansClustering");
		
		JobResult viewed;

		if (resultType == "applet") {
			Parameter    dataset        = new Parameter("dataset", preProcessed.getURL("KMcluster_output.gct").toString());
			Parameter[] viewParameters  = new Parameter[1];
			viewParameters[0]           = dataset;
			updateStatus(jobName, "Running KMeans Clustering Viewer")
			viewed = runJobNoWF(userName, viewParameters, "HeatMapViewer");
		} else {
			Parameter    inputDataset   = new Parameter("input.dataset", 
					preProcessed.getURL("KMcluster_output.gct").toString());
			Parameter    columnSize = new Parameter("column.size", 10);
			Parameter    rowSize = new Parameter("row.size", 10);
			Parameter    rowDescs      = new Parameter("show.row.descriptions", "yes");

			Parameter[] viewParameters  = new Parameter[4];
			viewParameters[0] = inputDataset;
			viewParameters[1] = columnSize;
			viewParameters[2] = rowSize;
			viewParameters[3] = rowDescs;
			log.debug("Run job to load the viewer")
			viewed = runJobNoWF(userName, viewParameters, "HeatMapImage");
		}

		JobResult[] toReturn = [preProcessed, viewed]
		log.debug("Returning ${preProcessed} and ${viewed}")
		
		return toReturn
	}

	/**
	 * Runs the simple compare heatmap
	 * 
	 * @param userName - The user requesting the job
	 * @param jobName - The name of the job given by the GP controller
	 * @param gctFile - file with the heatmap data
	 * @param resultType - applet or image
	 * 
	 * @return the JobResult array from the GenePattern server
	 */
	public JobResult[] HMap(String userName, String jobName, File gctFile, String resultType) throws WebServiceException { 
		Parameter    inputFilename      = new Parameter("input.filename", gctFile)
		Parameter[]  preProcParameters  = [inputFilename]	
		
		updateStatus(jobName, "Uploading file")
		JobResult preprocessed = runJobNoWF(userName, preProcParameters, "ConvertLineEndings")

		def gctURL = preprocessed.getURL("gct").toString()
		
		JobResult viewed

		if (resultType == "applet") {
			Parameter    dataset        = new Parameter("dataset", gctURL);
			Parameter[] viewParameters  = [dataset]
			updateStatus(jobName, "Running Heatmap Viewer")
			viewed = runJobNoWF(userName, viewParameters, "HeatMapViewer");
		} else {
			log.debug("resultType = ${resultType}")
			Parameter    inputDataset   = new Parameter("input.dataset", gctURL);
			Parameter    columnSize 	= new Parameter("column.size", 10);
			Parameter    rowSize 		= new Parameter("row.size", 10);
			Parameter    rowDescs      	= new Parameter("show.row.descriptions", "yes");

			Parameter[] viewParameters  = [inputDataset, columnSize, rowSize, rowDescs]
			log.debug("Run job to load the viewer")
			viewed = runJobNoWF(userName, viewParameters, "HeatMapImage");
		}
		
		JobResult[] toReturn = [preprocessed, viewed]
		log.debug("Returning ${preprocessed} and ${viewed}")
						
		return toReturn
	}
	
	/**
	 * Runs the comparative marker analysis
	 * 
	 * @param userName - The user requesting the job
	 * @param jobName - The name of the job given by the GP controller
	 * @param gctFile - file with the heatmap data
	 * @param clsFile - file with the heatmap data
	 * @param resultType - applet or image
	 */
	public JobResult[] CMAnalysis(String userName, String jobName, File gctFile, File clsFile, String resultType) throws WebServiceException {		
		Parameter    dataFilename     = new Parameter("data.filename", gctFile);
		Parameter    k                = new Parameter("k", 10);
		Parameter    rowMax           = new Parameter("rowmax", 0.5);
		Parameter    colMax           = new Parameter("colmax", 0.8);

		Parameter proxyedOdfFile = null;
		Parameter proxyedDatasetFilename = null;

		Parameter[]  impParameters    = new Parameter[4];
		impParameters[0] = dataFilename;
		impParameters[1] = k;
		impParameters[2] = rowMax;
		impParameters[3] = colMax;

		updateStatus(jobName, "Imputing Missing Value KNN")
		JobResult    imputedMissing   = runJobNoWF(userName, impParameters, "ImputeMissingValuesKNN")
		
		Parameter    inputFilename     = new Parameter("input.file", getGenePatternRealURLBehindProxy(imputedMissing.getURL("gct").toString()));
		Parameter    clsFilename       = new Parameter("cls.file", clsFile);
		Parameter    testDirection     = new Parameter("test.direction", 2);
		Parameter    testStatistic     = new Parameter("test.statistic", 0);
		Parameter    numPermutations   = new Parameter("number.of.permutations", 1000);
		Parameter    complete          = new Parameter("complete", "false");
		Parameter    balanced          = new Parameter("balanced", "false");
		Parameter    randomSeed        = new Parameter("random.seed", 779948241);
		Parameter    smoothPValues     = new Parameter("smooth.p.values", "true");
		
		Parameter[] cmsParameters  = new Parameter[9];
		cmsParameters[0] = inputFilename;
		cmsParameters[1] = clsFilename;
		cmsParameters[2] = testDirection;
		cmsParameters[3] = testStatistic;
		cmsParameters[4] = numPermutations
		cmsParameters[5] = complete;
		cmsParameters[6] = balanced;
		cmsParameters[7] = randomSeed;
		cmsParameters[8] = smoothPValues;
					
		updateStatus(jobName, "Performing Comparative Marker Selection")
		JobResult preProcessed = runJobNoWF(userName, cmsParameters, "ComparativeMarkerSelection");
  	    proxyedOdfFile = new Parameter("comparative.marker.selection.filename",	preProcessed.getURL("odf").toString());
		Parameter    odfFile         = new Parameter("comparative.marker.selection.filename", getGenePatternRealURLBehindProxy(preProcessed.getURL("odf").toString()));
		proxyedDatasetFilename = new Parameter("dataset.filename", imputedMissing.getURL("gct").toString());		
		Parameter    datasetFilename = new Parameter("dataset.filename", getGenePatternRealURLBehindProxy(imputedMissing.getURL("gct").toString()));
						
		Parameter    field           = new Parameter("field", "Rank");
		Parameter    max             = new Parameter("max", 100);

		Parameter[] ecmrParameters  = new Parameter[4];
		ecmrParameters[0] = odfFile;
		ecmrParameters[1] = datasetFilename;
		ecmrParameters[2] = field;
		ecmrParameters[3] = max;

		updateStatus(jobName, "Extracting Comparative Marker Results")
		JobResult extracted = runJobNoWF(userName, ecmrParameters, "ExtractComparativeMarkerResults");

		JobResult viewed;
		JobResult cmsv;

		if (resultType == "applet") {
			Parameter    dataset        = new Parameter("dataset", extracted.getURL("gct").toString());
			Parameter[] viewParameters  = new Parameter[1];
			viewParameters[0]           = dataset;
			updateStatus(jobName, "Running Heatmap Viewer")
			viewed = runJobNoWF(userName, viewParameters, "HeatMapViewer");

			Parameter[] cmsvParameters  = new Parameter[2];
			cmsvParameters[0] = proxyedOdfFile;
			cmsvParameters[1] = proxyedDatasetFilename;
			updateStatus(jobName, "Running Comparative Marker Selection Viewer")
			cmsv = runJobNoWF(userName, cmsvParameters, "ComparativeMarkerSelectionViewer");

		} else {
			Parameter   inputDataset   = new Parameter("input.dataset", extracted.getURL("gct").toString());
			Parameter    columnSize    = new Parameter("column.size", 10);
			Parameter    rowSize       = new Parameter("row.size", 10);
			Parameter    rowDescs      = new Parameter("show.row.descriptions", "yes");
			Parameter[] viewParameters = new Parameter[4];
			viewParameters[0] = inputDataset;
			viewParameters[1] = columnSize;
			viewParameters[2] = rowSize;
			viewParameters[3] = rowDescs;
			log.debug("Run job to load the viewer")
			viewed = runJobNoWF(userName, viewParameters, "HeatMapImage");
		}

		JobResult[] toReturn = [extracted, viewed, cmsv]
		log.debug("Returning ${extracted}, ${viewed} and ${cmsv}")
		
		return toReturn
	}

	/**
	 * Runs the PCA analysis
	 *
	 * @param userName - The user requesting the job
	 * @param jobName - The name of the job given by the GP controller
	 * @param gctFile - file with the heatmap data
	 * @param clsFile - file with the heatmap data
	 * @param resultType - applet or image
	 */
	public JobResult[] PCA(String userName, String jobName, File gctFile, File clsFile, String resultType) throws WebServiceException { 
		/** Perhaps due to configuration issues of Gene Pattern server in transmartdev environment,
		 * The input dataset file is imported into Gene Pattern correctly through web services interface, but is save into an 
		 * inaccessible location after use. The viewer applet needs to access the input data file, and will fail for these tasks.
		 * The work-around is to use non-change tasks like ConvertLineEndings to put the input dataset file as a result file, and then
		 * use the URL of this result file as input file to the later tasks and viewers. */
		Parameter    inputFileGctParam      = new Parameter("input.filename", gctFile);
		Parameter[]  preProcGctParameters  = new Parameter[1];
		preProcGctParameters[0]            = inputFileGctParam;
				
		updateStatus(jobName, "Uploading file")
		JobResult preprocessedGct = runJobNoWF(userName, preProcGctParameters, "ConvertLineEndings");
		String gctURL = preprocessedGct.getURL("gct").toString();
		String gctURLReal = getGenePatternRealURLBehindProxy(gctURL);
		
		Parameter    inputFileClsParam      = new Parameter("input.filename", clsFile);
		Parameter[]  preProcClsParameters  = new Parameter[1];
		preProcClsParameters[0]            = inputFileClsParam;		
		JobResult preprocessedCls = runJobNoWF(userName, preProcClsParameters, "ConvertLineEndings");
		String clsURL = preprocessedCls.getURL("cls").toString();
		
		Parameter    inputFileParam2      = new Parameter("input.filename", gctURLReal);
		Parameter    clusterByParam      = new Parameter("cluster.by", "3");
		Parameter    outputFileNameParam      = new Parameter("output.file", "<input.filename_basename>");
		
		Parameter[]  pcaProcParameters  = new Parameter[3];
		pcaProcParameters[0]            = inputFileParam2;
		pcaProcParameters[1]            = clusterByParam;		
		pcaProcParameters[2]            = outputFileNameParam;		
		
		updateStatus(jobName, "Running PCA")
		JobResult pcaResult = runJobNoWF(userName, pcaProcParameters, "PCA");
		
		JobResult viewed;

		if (resultType == "applet") {
			Parameter datasetParam = new Parameter("dataset.file", gctURL);
			
			String tFileName, uFileName, sFileName;
			String[] outputFileNames = pcaResult.getOutputFileNames();
			for (int i = 0; i < outputFileNames.length; i ++) {
				String fileName = outputFileNames[i];
				if (fileName.endsWith("_t.odf"))
					tFileName = fileName;
				else if (fileName.endsWith("_s.odf"))
					sFileName = fileName;
				else if (fileName.endsWith("_u.odf"))
					uFileName = fileName;
			}
			
			Parameter sFileParam = new Parameter("s.matrix.file", pcaResult.getURLForFileName(sFileName).toString());
			Parameter tFileParam = new Parameter("t.matrix.file", pcaResult.getURLForFileName(tFileName).toString());
			Parameter uFileParam = new Parameter("u.matrix.file", pcaResult.getURLForFileName(uFileName).toString());
			
			Parameter clsFileParam = new Parameter("cls.or.sample.info.file", clsURL);
			
			Parameter[] viewParameters  = new Parameter[5];
			viewParameters[0]           = datasetParam;
			viewParameters[1]           = sFileParam;
			viewParameters[2]           = tFileParam;
			viewParameters[3]           = uFileParam;
			viewParameters[4]           = clsFileParam;
			
			updateStatus(jobName, "Running PCA Viewer")
			viewed = runJobNoWF(userName, viewParameters, "PCAViewer");
		} else {
			Parameter    inputDataset   = new Parameter("input.dataset", gctURL);
			Parameter    columnSize = new Parameter("column.size", 10);
			Parameter    rowSize = new Parameter("row.size", 10);
			Parameter    rowDescs      = new Parameter("show.row.descriptions", "yes");

			Parameter[] viewParameters  = new Parameter[4];
			viewParameters[0] = inputDataset;
			viewParameters[1] = columnSize;
			viewParameters[2] = rowSize;
			viewParameters[3] = rowDescs;
			
			log.debug("Run job to load the viewer")
			viewed = runJobNoWF(userName, viewParameters, "PCAViewer");
		}
		
		JobResult[] toReturn = [pcaResult, viewed]
		log.debug("Returning ${pcaResult} and ${viewed}")
				
		return toReturn
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// TEST - REMOVE AFTER DEBUGGING
	/**
	* Runs the comparative marker analysis
	*
	* @param userName - The user requesting the job
	* @param jobName - The name of the job given by the GP controller
	* @param gctFile - file with the heatmap data
	* @param clsFile - file with the heatmap data
	* @param resultType - applet or image
	*/
   public JobResult[] CMAnalysis(File gctFile, File clsFile, String resultType) throws WebServiceException {
	   Parameter    dataFilename     = new Parameter("data.filename", gctFile);
	   Parameter    k                = new Parameter("k", 10);
	   Parameter    rowMax           = new Parameter("rowmax", 0.5);
	   Parameter    colMax           = new Parameter("colmax", 0.8);

	   Parameter proxyedOdfFile = null;
	   Parameter proxyedDatasetFilename = null;

	   Parameter[]  impParameters    = new Parameter[4];
	   impParameters[0] = dataFilename;
	   impParameters[1] = k;
	   impParameters[2] = rowMax;
	   impParameters[3] = colMax;

	   JobResult    imputedMissing   = runJob(impParameters, "ImputeMissingValuesKNN")
	   
	   Parameter    inputFilename     = new Parameter("input.file", getGenePatternRealURLBehindProxy(imputedMissing.getURL("gct").toString()));
	   Parameter    clsFilename       = new Parameter("cls.file", clsFile);
	   Parameter    testDirection     = new Parameter("test.direction", 2);
	   Parameter    testStatistic     = new Parameter("test.statistic", 0);
	   Parameter    numPermutations   = new Parameter("number.of.permutations", 1000);
	   Parameter    complete          = new Parameter("complete", "false");
	   Parameter    balanced          = new Parameter("balanced", "false");
	   Parameter    randomSeed        = new Parameter("random.seed", 779948241);
	   Parameter    smoothPValues     = new Parameter("smooth.p.values", "true");
	   
	   Parameter[] cmsParameters  = new Parameter[9];
	   cmsParameters[0] = inputFilename;
	   cmsParameters[1] = clsFilename;
	   cmsParameters[2] = testDirection;
	   cmsParameters[3] = testStatistic;
	   cmsParameters[4] = numPermutations
	   cmsParameters[5] = complete;
	   cmsParameters[6] = balanced;
	   cmsParameters[7] = randomSeed;
	   cmsParameters[8] = smoothPValues;
				   
	   JobResult preProcessed = runJob(cmsParameters, "ComparativeMarkerSelection");
		 proxyedOdfFile = new Parameter("comparative.marker.selection.filename",	preProcessed.getURL("odf").toString());
	   Parameter    odfFile         = new Parameter("comparative.marker.selection.filename", getGenePatternRealURLBehindProxy(preProcessed.getURL("odf").toString()));
	   proxyedDatasetFilename = new Parameter("dataset.filename", imputedMissing.getURL("gct").toString());
	   Parameter    datasetFilename = new Parameter("dataset.filename", getGenePatternRealURLBehindProxy(imputedMissing.getURL("gct").toString()));
					   
	   Parameter    field           = new Parameter("field", "Rank");
	   Parameter    max             = new Parameter("max", 100);

	   Parameter[] ecmrParameters  = new Parameter[4];
	   ecmrParameters[0] = odfFile;
	   ecmrParameters[1] = datasetFilename;
	   ecmrParameters[2] = field;
	   ecmrParameters[3] = max;

	   JobResult extracted = runJob(ecmrParameters, "ExtractComparativeMarkerResults");

	   JobResult viewed;
	   JobResult cmsv;

	   if (resultType == "applet") {
		   Parameter    dataset        = new Parameter("dataset", extracted.getURL("gct").toString());
		   Parameter[] viewParameters  = new Parameter[1];
		   viewParameters[0]           = dataset;
		   viewed = runJob(viewParameters, "HeatMapViewer");

		   Parameter[] cmsvParameters  = new Parameter[2];
		   cmsvParameters[0] = proxyedOdfFile;
		   cmsvParameters[1] = proxyedDatasetFilename;
		   cmsv = runJob(cmsvParameters, "ComparativeMarkerSelectionViewer");

	   } else {
		   Parameter   inputDataset   = new Parameter("input.dataset", extracted.getURL("gct").toString());
		   Parameter    columnSize    = new Parameter("column.size", 10);
		   Parameter    rowSize       = new Parameter("row.size", 10);
		   Parameter    rowDescs      = new Parameter("show.row.descriptions", "yes");
		   Parameter[] viewParameters = new Parameter[4];
		   viewParameters[0] = inputDataset;
		   viewParameters[1] = columnSize;
		   viewParameters[2] = rowSize;
		   viewParameters[3] = rowDescs;
		   viewed = runJob(viewParameters, "HeatMapImage");
	   }

	   JobResult[] toReturn = [extracted, viewed, cmsv]
	   log.debug("Returning ${extracted}, ${viewed} and ${cmsv}")
	   
	   return toReturn
   }

   /**
	* Runs the PCA analysis
	*
	* @param userName - The user requesting the job
	* @param jobName - The name of the job given by the GP controller
	* @param gctFile - file with the heatmap data
	* @param clsFile - file with the heatmap data
	* @param resultType - applet or image
	*/
   public JobResult[] PCA(File gctFile, File clsFile, String resultType) throws WebServiceException {
	   /** Perhaps due to configuration issues of Gene Pattern server in transmartdev environment,
		* The input dataset file is imported into Gene Pattern correctly through web services interface, but is save into an
		* inaccessible location after use. The viewer applet needs to access the input data file, and will fail for these tasks.
		* The work-around is to use non-change tasks like ConvertLineEndings to put the input dataset file as a result file, and then
		* use the URL of this result file as input file to the later tasks and viewers. */
	   Parameter    inputFileGctParam      = new Parameter("input.filename", gctFile);
	   Parameter[]  preProcGctParameters  = new Parameter[1];
	   preProcGctParameters[0]            = inputFileGctParam;
			   
	   JobResult preprocessedGct = runJob(preProcGctParameters, "ConvertLineEndings");
	   String gctURL = preprocessedGct.getURL("gct").toString();
	   String gctURLReal = getGenePatternRealURLBehindProxy(gctURL);
	   
	   Parameter    inputFileClsParam      = new Parameter("input.filename", clsFile);
	   Parameter[]  preProcClsParameters  = new Parameter[1];
	   preProcClsParameters[0]            = inputFileClsParam;
	   JobResult preprocessedCls = runJob(preProcClsParameters, "ConvertLineEndings");
	   String clsURL = preprocessedCls.getURL("cls").toString();
	   
	   Parameter    inputFileParam2      = new Parameter("input.filename", gctURLReal);
	   Parameter    clusterByParam      = new Parameter("cluster.by", "3");
	   Parameter    outputFileNameParam      = new Parameter("output.file", "<input.filename_basename>");
	   
	   Parameter[]  pcaProcParameters  = new Parameter[3];
	   pcaProcParameters[0]            = inputFileParam2;
	   pcaProcParameters[1]            = clusterByParam;
	   pcaProcParameters[2]            = outputFileNameParam;
	   
	   JobResult pcaResult = runJob(pcaProcParameters, "PCA");
	   
	   JobResult viewed;

	   if (resultType == "applet") {
		   Parameter datasetParam = new Parameter("dataset.file", gctURL);
		   
		   String tFileName, uFileName, sFileName;
		   String[] outputFileNames = pcaResult.getOutputFileNames();
		   for (int i = 0; i < outputFileNames.length; i ++) {
			   String fileName = outputFileNames[i];
			   if (fileName.endsWith("_t.odf"))
				   tFileName = fileName;
			   else if (fileName.endsWith("_s.odf"))
				   sFileName = fileName;
			   else if (fileName.endsWith("_u.odf"))
				   uFileName = fileName;
		   }
		   
		   Parameter sFileParam = new Parameter("s.matrix.file", pcaResult.getURLForFileName(sFileName).toString());
		   Parameter tFileParam = new Parameter("t.matrix.file", pcaResult.getURLForFileName(tFileName).toString());
		   Parameter uFileParam = new Parameter("u.matrix.file", pcaResult.getURLForFileName(uFileName).toString());
		   
		   Parameter clsFileParam = new Parameter("cls.or.sample.info.file", clsURL);
		   
		   Parameter[] viewParameters  = new Parameter[5];
		   viewParameters[0]           = datasetParam;
		   viewParameters[1]           = sFileParam;
		   viewParameters[2]           = tFileParam;
		   viewParameters[3]           = uFileParam;
		   viewParameters[4]           = clsFileParam;
		   
		   viewed = runJob(viewParameters, "PCAViewer");
	   } else {
		   Parameter    inputDataset   = new Parameter("input.dataset", gctURL);
		   Parameter    columnSize = new Parameter("column.size", 10);
		   Parameter    rowSize = new Parameter("row.size", 10);
		   Parameter    rowDescs      = new Parameter("show.row.descriptions", "yes");

		   Parameter[] viewParameters  = new Parameter[4];
		   viewParameters[0] = inputDataset;
		   viewParameters[1] = columnSize;
		   viewParameters[2] = rowSize;
		   viewParameters[3] = rowDescs;
		   
		   viewed = runJob(viewParameters, "PCAViewer");
	   }
	   
	   JobResult[] toReturn = [pcaResult, viewed]
	   log.debug("Returning ${pcaResult} and ${viewed}")
			   
	   return toReturn
   }
   // TEST - REMOVE AFTER DEBUGGING
   //////////////////////////////////////////////////////////////////////////////////////////

	public JobResult[] snpViewer(File dataFile, File sampleFile) throws Exception { 
		// The file submitted through web service interface is not accessible to Java Applet-based viewer.
		 // The work-around is to use non-change tasks like ConvertLineEndings to put the input dataset file as a result file, and then
		 // use the URL of this result file as input file to the later tasks and viewers. 
		Parameter    inputFileDataParam      = new Parameter("input.filename", dataFile);
		Parameter[]  preProcDataParameters  = new Parameter[1];
		preProcDataParameters[0]            = inputFileDataParam;
		JobResult preprocessedData = runJob(preProcDataParameters, "ConvertLineEndings");
		String dataURL = preprocessedData.getURL("xcn").toString();
		
		Parameter    inputFileSampleParam      = new Parameter("input.filename", sampleFile);
		Parameter[]  preProcSampleParameters  = new Parameter[1];
		preProcSampleParameters[0]            = inputFileSampleParam;		
		JobResult preprocessedSample = runJob(preProcSampleParameters, "ConvertLineEndings");
		String sampleURL = preprocessedSample.getURL("sample.cvt.txt").toString();
		
		Parameter    inputURLDataParam      = new Parameter("dataset.filename", dataURL);
		Parameter    inputURLSampleParam      = new Parameter("sample.info.filename", sampleURL);
		
		Parameter[]  snpProcParameters  = new Parameter[2];
		snpProcParameters[0]            = inputURLDataParam;
		snpProcParameters[1]            = inputURLSampleParam;
		
		JobResult snpResult = runJob(snpProcParameters, "SnpViewer");
		
		JobResult[] toReturn = new JobResult[2];
		toReturn[1] = snpResult;
				
		return(toReturn);
	}

	public JobResult[] igvViewer(IgvFiles igvFiles, String genomeVersion, String locus, String userName) 
			throws Exception { 
		// The file submitted through web service interface is not accessible to Java Applet-based viewer.
		 // The work-around is to use non-change tasks like ConvertLineEndings to put the input dataset file as a result file, and then
		 // use the URL of this result file as input file to the later tasks and viewers. 
		String sampleFileUrl = igvFiles.getFileUrlWithSecurityToken(igvFiles.getSampleFile(), userName);
		
		File sessionFile = igvFiles.getSessionFile();
		sessionFile << "<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n<Session genome='hg19' version='3'>\n<Resources>\n";
		List<File> cnFileList = igvFiles.getCopyNumberFileList();
		for (File cnFile : cnFileList) {
			String cnFileUrl = igvFiles.getFileUrlWithSecurityToken(cnFile, userName);
			sessionFile << "<Resource path='" + cnFileUrl + "'/>\n";
		}
		sessionFile << "</Resources>\n</Session>";
		
		String sessionURL = getGPFileConvertUrl(sessionFile);
		
		Parameter    inputURLDataParam      = new Parameter("input.file", sessionURL);
		
		Parameter[]  igvProcParameters  = new Parameter[1];
		igvProcParameters[0]            = inputURLDataParam;
		
		JobResult igvResult = runJob(igvProcParameters, "IGV");
		
		JobResult[] toReturn = new JobResult[2];
		toReturn[1] = igvResult;
				
		return(toReturn);
	}

	/**
	 * This function submit a file to GenePattern server, and get the URL to its GenePattern location
	 * @param file
	 * @return
	 */
	public String getGPFileConvertUrl(File file) throws Exception {
		String fileName = file.getName();
		int posSuffix = fileName.lastIndexOf(".");
		String fileSuffix = fileName.substring(posSuffix + 1);
		Parameter    inputFileDataParam      = new Parameter("input.filename", file);
		Parameter[]  preProcDataParameters  = new Parameter[1];
		preProcDataParameters[0]            = inputFileDataParam;
		JobResult preprocessedData = runJob(preProcDataParameters, "ConvertLineEndings");
		String dataURL = preprocessedData.getURL(fileSuffix).toString();
		return dataURL;
	}

	/**
	* Runs the Survival Analysis
	*
	* @param userName - The user requesting the job
	* @param jobName - The name of the job given by the GP controller
	* @param gctFile - file with the survival analysis data
	* @param clsFile - file with the survival analysis data
	* @param imageTempPath - path to store the temporary images
	* @param imageTempDirName - the image temporary directory
	* @param contextPath - the servlet context path
	* @param querySummary1 - Results from the first subset
	* @param querySummary2 - Results from the second subset
	*/
public String survivalAnalysis(String userName, String jobName, File dataFile, File clsFile, String imageTempPath, String imageTempDirName, String contextPath,
		String querySummary1, String querySummary2) throws WebServiceException {
		 
		if (dataFile == null)	{
			throw new WebServiceException("The data file for survival analysis does not exist")
		}
		if (clsFile == null)	{
			throw new WebServiceException("The cls file for survival analysis does not exist")
		}
		
		JobResult[] jobResults = new JobResult[2];
		
		Parameter inputFileDataParam = new Parameter("input.surv.data.filename", dataFile);
		Parameter inputFileClsParam = new Parameter("input.cls.filename", clsFile);
		Parameter[] coxParameters = new Parameter[11];
		coxParameters[0] = inputFileDataParam;		
		coxParameters[1]  = inputFileClsParam;		
		coxParameters[2] = new Parameter("output.file", "CoxRegression_result");		
		coxParameters[3] = new Parameter("time", "time");		
		coxParameters[4] = new Parameter("status", "censor");		
		coxParameters[5] = new Parameter("variable.continuous", "cls");		
		coxParameters[6] = new Parameter("variable.category", "NA");		
		coxParameters[7] = new Parameter("variable.interaction.terms", "NA");		
		coxParameters[8] = new Parameter("strata", "NA");		
		coxParameters[9] = new Parameter("input.subgroup", "NA");		
		coxParameters[10] = new Parameter("variable.selection", "none");
		updateStatus(jobName, "Running Cox Regression")
		JobResult coxResult = runJobNoWF(userName, coxParameters, "CoxRegression");
		
		String coxStr = getOutputFileText(coxResult, "CoxRegression_result", imageTempPath);
		coxStr = parseCoxRegressionStr(coxStr);
		
		Parameter dataFileParam = new Parameter("input.surv.data.file", dataFile);
		Parameter clsFileParam = new Parameter("input.cls.file", clsFile);
		Parameter[] curveParameters = new Parameter[15];
		curveParameters[0] = dataFileParam;		
		curveParameters[1] = clsFileParam;		
		curveParameters[2] = new Parameter("time.field", "time");		
		curveParameters[3] = new Parameter("censor.field", "censor");		
		curveParameters[4] = new Parameter("print.fit.results", "T");		
		curveParameters[5] = new Parameter("line.type.color.assign", "automatic");		
		curveParameters[6]  = new Parameter("line.width", "1");		
		curveParameters[7] = new Parameter("time.conversion", "1");		
		curveParameters[8] = new Parameter("surv.function.lower", "0");		
		curveParameters[9] = new Parameter("surv.function.higher", "1");		
		curveParameters[10] = new Parameter("curve.type", "log");		
		curveParameters[11] = new Parameter("show.conf.interval", "0");		
		curveParameters[12] = new Parameter("add.legend", "T");		
		curveParameters[13] = new Parameter("legend.position", "left-bottom");		
		curveParameters[14] = new Parameter("output.filename", "<input.surv.data.file_basename>");
		updateStatus(jobName, "Calculating Survival Curve")
		JobResult curveResult = runJobNoWF(userName, curveParameters, "SurvivalCurve");
		
		String summaryStr = getOutputFileText(curveResult, "FitSummary", imageTempPath);
		summaryStr = parseSurvivalCurveSummary(summaryStr);
		
		String graphFileName = getOutputFileName(curveResult, "SurvivalCurve");
		File graphFile = curveResult.downloadFile(graphFileName, imageTempPath)
		def imageFileName = graphFile.getName()

		try	{
			imageFileName = convertPdfToPng(graphFile, imageTempPath);
		} catch(IOException ioe)	{		
			log.warn(ioe.getMessage())
			log.warn("GP server updated so PDF is no longer part of the results, just use png image file")
		}			
		
		def imageUrlPath = contextPath + imageTempDirName + "/" + imageFileName
		
		StringBuffer buf = new StringBuffer();
		buf.append("<h2>Survival Analysis</h2>");
		buf.append("<table border='1' width='100%'><tr><th>Subset 1 Query</th><th>Subset 2 Query</th></tr><tr><td>" + 
			querySummary1 + "</td><td>" + querySummary2 + "</td></tr></table>");
		buf.append("<p>Cox Regression Result:</p>");
		buf.append(coxStr);
		buf.append("<p>Survival Curve Fitting Summary:</p>");
		buf.append(summaryStr);
		buf.append("<img src='" + imageUrlPath + "' />");
		
		return buf.toString()		
	}
		
	public String gwas(String userName, String jobName, GwasFiles gwasFiles,
			String querySummary1, String querySummary2) throws WebServiceException {
		 
		if (gwasFiles == null)	{
			throw new WebServiceException("The object gwasFiles does not exist")
		}
		
		JobResult[] jobResults = new JobResult[2];
		try {
			i2b2HelperService.runPlink(gwasFiles);
			i2b2HelperService.reportGwas(userName, gwasFiles, querySummary1, querySummary2);
		}
		catch (WebServiceException wse) {
			throw wse;
		}
		catch (Exception e) {
			throw new WebServiceException("Failed to run PLINK on server or report the result: " + e.getMessage());
		}
		File reportFile = gwasFiles.getReportFile();
		return reportFile.getText();
	}
		
	public String convertPdfToPng(File graphFile, String imageTempPath) throws Exception {
		if (graphFile == null) throw new Exception("The PDF file is empty");
		if (imageTempPath == null) throw new Exception("The temporary path for image folder is not defined");
			
		RandomAccessFile raf = new RandomAccessFile(graphFile, "r");
		FileChannel channel = raf.getChannel();
		ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
		PDFFile pdffile = new PDFFile(buf);
	
		// draw the first page to an image
		PDFPage page = pdffile.getPage(0);
			
		//get the width and height for the doc at the default zoom
		Rectangle rect = new Rectangle(0,0, (int)page.getBBox().getWidth(), (int)page.getBBox().getHeight());
			
		//generate the image
		Image img = page.getImage(
			rect.width.intValue(), rect.height.intValue(), //width & height
			rect, // clip rect
			null, // null for the ImageObserver
			true, // fill background with white
			true  // block until drawing is done
		);
		String graphFileName = graphFile.getName();
		String imageFileName = graphFileName.replace(".pdf", ".png");
		ImageIO.write(img, "png", new File(imageTempPath + File.separator + imageFileName));
		return imageFileName;
	}
	
	public String getOutputFileName(JobResult jobResult, String partialFileName) {
		if (jobResult == null || partialFileName == null  || partialFileName.length() == 0) return null;
		String[] fileNames = jobResult.getOutputFileNames();
		if (fileNames == null || fileNames.length == 0) return null;
		for (String fileName : fileNames) {
			if (fileName.indexOf(partialFileName) >= 0) 
				return fileName;
		}
		return null;
	}
	
	public String getOutputFileText(JobResult jobResult, String partialFileName, String downloadDirPath) {
		if (jobResult == null || partialFileName == null  || partialFileName.length() == 0) return null;
		String[] fileNames = jobResult.getOutputFileNames();
		if (fileNames == null || fileNames.length == 0) return null;
		String outFileName = null;
		for (String fileName : fileNames) {
			if (fileName.indexOf(partialFileName) >= 0) 
				outFileName = fileName;
		}
		if (outFileName == null) return null;
		File outFile = jobResult.downloadFile(outFileName, downloadDirPath);
		String outStr = outFile.getText();
		return outStr;
	}
	
	public GPClient getGPClient() throws Exception {
		if (gpClient != null) return gpClient;
		
		String userName = springSecurityService.getPrincipal().username
		log.debug("starting genepattern client at " +
				CH.config.com.recomdata.datasetExplorer.genePatternURL +
				" as " + userName
				);
	
		gpClient = new GPClient(
			 CH.config.com.recomdata.datasetExplorer.genePatternURL,
			 userName);
		log.debug("genepattern client initialized");
		return gpClient;
	}
	
	/**
	 * Returns a new GenePattern Client object authenticated by the given userName
	 * 
	 * @param userName - The user requesting the GenePattern work
	 * 
	 * @return - the GenePattern Client object
	 */
	public GPClient getGPClient(String userName) throws WebServiceException	{
		def gpURL = CH.config.com.recomdata.datasetExplorer.genePatternURL 
		if (gpClient != null && userName.compareToIgnoreCase(gpClient.getUsername()) == 0)	{
			log.debug("GPClient is already initialized for ${userName}, returning existing client")
			return gpClient
		}		
		log.debug("Starting GPClient at ${gpURL} as ${userName}")	
		gpClient = new GPClient(gpURL, userName)
		log.debug("GPClient has been initialized")
		return gpClient
	}
	
	public String parseCoxRegressionStr(String inStr) {
		StringBuffer buf = new StringBuffer();
		String numSubject, coef, hazardRatio, standardError, pVal, lower95, upper95;
		boolean nextLineHazard = false, nextLine95 = false;
		inStr.eachLine {
			if (it.indexOf("n=") >=0) {
				numSubject = it.substring(it.indexOf("n=") + 2).trim();
			}
			else if (it.indexOf("se(coef)") >= 0) {
				nextLineHazard = true;
			}
			else if (it.indexOf("cls") >= 0 && nextLineHazard == true) {
				nextLineHazard = false;
				String[] resultArray = it.split();
				coef = resultArray[1];
				hazardRatio = resultArray[2];
				standardError = resultArray[3];
				pVal = resultArray[5];
			}
			else if (it.indexOf("lower") >= 0) {
				nextLine95 = true
			}
			else if (it.indexOf("cls") >= 0 && nextLine95 == true) {
				nextLine95 = false;
				String[] resultArray = it.split();
				lower95 = resultArray[3];
				upper95 = resultArray[4];
			}
		}
		buf.append("<table border='1'  width='100%'><tr><th>Number of Subjects</th><td>" + numSubject + "</td></tr>");
		buf.append("<tr><th>Hazard Ratio (95% CI)</th><td>" + hazardRatio + " (" + lower95 + " - " + upper95 + ")</td></tr>");
		buf.append("<tr><th>Relative Risk (p Value)</th><td>" + coef + " (" + pVal + ")</td></tr>");
		buf.append("</table>");
		return buf.toString();
	}
	
	public String parseSurvivalCurveSummary(String inStr) {
		StringBuffer buf = new StringBuffer();
		buf.append("<table border='1' width='100%'><tr><th>Subset</th><th>Number of Subjects</th><th>Number of Events</th><th>Median Value</th><th>Lower Range of 95% Confidence Level</th><th>Upper Range of 95% Confidence Level</th></tr>")
		inStr.eachLine {
			if (it.indexOf("cls=") >=0) {
				String[] strArray = it.split();
				if (strArray[0].indexOf("cls=1") >=0)
					buf.append("<tr><td>Subset 1</td>");
				else if (strArray[0].indexOf("cls=2") >=0)
					buf.append("<tr><td>Subset 2</td>");
					
				for(int i = 1; i < 6; i++) {
					String value = strArray[i];
					if (value.indexOf("Inf") >= 0) {
						value = "infinity";
					}
					buf.append("<td>" + value + "</td>");
				}
				buf.append("</tr>");
			}
		}
		buf.append("</table>");
		return buf.toString();
	}
	
	/**
	 * Some GenePattern modules cannot access GenePattern stored files, if the GenePattern server is accessed through a proxy.
	 * Separate GenePattern server is to have more resource for demanding tasks. Same URL for GenePattern as the Transmart server is to avoid
	 * security issue of launching Java Applet from IE.
	 * In the case of PCA, transmart code first submit input to GenePattern server. In PCA call, the URL of the GenePattern-stored input
	 * file is submitted as input. These input URL is from the proxy, like https://transmartdev/gp/jobResults/2374/gp_df_8595641542636053092.cvt.cls.
	 * Inside code of PCA module cannot programmatically access this input URL through proxy. We need to manually convert this URL to the real URL
	 * http://xxx.xxx.xxx.xxx:xxxx/gp/jobResults/2374/gp_df_8595641542636053092.cvt.cls.
	 * 
	 * Somehow, GenePattern server put the result file in the proxy'ed URL. In IE, the Java applet is launched to access this proxy'ed URL,
	 * and will work correctly.
	 * This function should NOT be used on input parameters to the Viewer modules.
	 * 
	 * Note: the idea of proxy for the separately-hosted GenePattern is too complicated, and may break other things.
	 * @param gpURLIn
	 * @return
	 */
	public String getGenePatternRealURLBehindProxy(String gpURLIn) {
		String gpServerRealURL = CH.config.com.recomdata.datasetExplorer.genePatternRealURLBehindProxy;
		if (gpServerRealURL == null || gpServerRealURL.length() == 0 || gpServerRealURL.equals("{}") )
			return gpURLIn;
		String gpServerURL = CH.config.com.recomdata.datasetExplorer.genePatternURL;
		gpURLIn.replace(gpServerURL, gpServerRealURL);
	}
	
	public void startWorkflowJob(String jobName) throws WebServiceException {
		if(RequestContextHolder.currentRequestAttributes().getSession()?.getAttribute("workflowstatus")?.isCancelled()){
			throw new WebServiceException("Workflow cancelled by user!");
		}
		RequestContextHolder.currentRequestAttributes().getSession()?.getAttribute("workflowstatus")?.setCurrentJobStatus(new JobStatus(name:jobName,status:"R"));
	}
	
	public void completeWorkflowJob(String jobName){
		RequestContextHolder.currentRequestAttributes().getSession()?.getAttribute("workflowstatus")?.setCurrentJobStatus(new JobStatus(name:jobName,status:"C"));
		
	}
	
	
	public JobResult[] PLINK(File pedFile, File mapFile) throws WebServiceException {
		
		Parameter    inputPed       = new Parameter("ped.input", pedFile);
		Parameter    inputMap       = new Parameter("map.input", mapFile);
		
		Parameter[] plinkParameters  = new Parameter[2];
		plinkParameters[0] = inputPed;
		plinkParameters[1] = inputMap;
		
		JobResult result = runJob(plinkParameters, "PLINK");
		
		return result;
	}
}