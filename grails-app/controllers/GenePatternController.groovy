import com.rdc.snp.haploview.PEDFormat
import com.recomdata.export.GenePatternFiles
import com.recomdata.export.GwasFiles
import com.recomdata.export.SurvivalAnalysisFiles
import com.recomdata.genepattern.JobStatus
import com.recomdata.genepattern.WorkflowStatus
import grails.converters.JSON
import org.json.JSONObject
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.SimpleTrigger
import org.transmart.CohortInformation
import org.transmart.ExperimentData
import org.transmart.HeatmapValidator
import org.transmart.searchapp.AccessLog

class GenePatternController {
    def quartzScheduler
    def genePatternService
    def springSecurityService
    def i2b2HelperService
    def jobResultsService
    def asyncJobService
    def dataSource

    static String GENE_PATTERN_WHITE_SPACE_DEFAULT = "0";
    static String GENE_PATTERN_WHITE_SPACE_EMPTY = "";

    /**
     * Method that is called asynchronously from the datasetExplorer Javascript
     * Will determine the type of heatmap to run and then kick off the heatmap job in genePatternService
     */
    def runheatmap = {

        //If debug mode is enabled, write all the parameters to the debug log.
        if (log.isDebugEnabled()) {
            request.getParameterMap().keySet().each { _key -> log.debug("${_key} -> ${request.getParameter(_key)}") }
        }

        //Retrieve the parameters from the submitted form.
        //This is the analysis type. In a heat map we have 'PCA','Select','KMeans','Cluster','Compare'
        def analysis = request.getParameter("analysis")

        //If we are analyzing RMB data this is the Panels filter.
        def rbmPanels1 = request.getParameter("rbmPanels1")
        def rbmPanels2 = request.getParameter("rbmPanels2")

        //If we are filtering by a sample code it gets passed in here.
        def sample1 = request.getParameter("sample1")
        def sample2 = request.getParameter("sample2")

        //On the DataSet explorer side we build an i2b2 Query that we later reference the results by this result instance id.
        def rID1 = nullCheck(request.getParameter("result_instance_id1"))
        def rID2 = nullCheck(request.getParameter("result_instance_id2"))

        //Number of clusters when running a clustering algorithm.
        def nclusters = request.getParameter("nclusters")

        //If we are filtering by timepoints they get passed in here.
        def timepoints1 = request.getParameter("timepoints1")
        def timepoints2 = request.getParameter("timepoints2")

        //This may be deprecated but we will leave it in in case we want to support different result types in the future. There used to be an "Image" result type.
        def resulttype = request.getParameter("resulttype")

        //This tells us the mRNA platform or if the data type is RBM.
        def datatype = request.getParameter("datatype")

        //The name of the job as given by the job creation web service call.
        def jobName = request.getParameter("jobName")

        //Build the status list that we display to the user as the job processes.
        // TODO: Stick this in JobResultsService as an enum
        def statusList = ["Validating Parameters", "Obtaining Query Definitions",
                          "Obtaining subject IDs", "Obtaining concepts", "Obtaining heatmap data",
                          "Triggering GenePattern job"]
        if (analysis == "Compare") {
            statusList.add("Uploading file")
            statusList.add("Running Heatmap Viewer")
        } else if (analysis == "Cluster") {
            statusList.add("Performing Hierarchical Clustering")
            statusList.add("Running Hierarchical Clustering Viewer")
        } else if (analysis == "KMeans") {
            statusList.add("Performing KMeans Clustering")
            statusList.add("Running KMeans Clustering Viewer")
        } else if (analysis == "Select") {
            statusList.add("Imputing Missing Value KNN")
            statusList.add("Performing Comparative Marker Selection")
            statusList.add("Extracting Comparative Marker Results")
            statusList.add("Running Heatmap Viewer")
            statusList.add("Running Comparative Marker Selection Viewer")
        } else if (analysis == "PCA") {
            statusList.add("Uploading file")
            statusList.add("Running PCA")
            statusList.add("Running PCA Viewer")
        }

        //Update our job object to have the list relevant to the job we are running.
        jobResultsService[jobName]["StatusList"] = statusList

        //This updates the status and checks to see if the job has been cancelled.
        if (asyncJobService.updateStatus(jobName, statusList[0])) {
            return
        }

        //Create the object which represents the gene pattern files we use to run the job.
        GenePatternFiles gpf = new GenePatternFiles()

        // Get the user name so we can use it in the access log.
        def userName = springSecurityService.getPrincipal().username

        //Create an entry in the access log.
        def al = new AccessLog(username: userName, event: "Heatmap Analysis: ${analysis}, Job: ${jobName}", eventmessage: "RID1: ${rID1}, RID2: ${rID2}", accesstime: new java.util.Date())
        al.save()

        //Get the pathway name based on the id passed in.
        def pathway_name = derivePathwayName(analysis, request.getParameter("pathway_name"))
        log.info("Pathway Name set to ${pathway_name}")

        //We once again validate to make sure two subsets were selected when we run a Comparative Marker Analysis.
        log.debug("Ensuring at least two subsets for comparative marker selection...")
        if (analysis == "Select" && (rID1 == null || rID2 == null)) {
            def error = "Comparative marker selection requires two subsets"
            jobResultsService[jobName]["Status"] = "Error"
            jobResultsService[jobName]["Exception"] = error
            log.error(error)
            return
        }

        //This updates the status and checks to see if the job has been canceled.
        if (asyncJobService.updateStatus(jobName, statusList[1])) {
            return
        }

        //Create stringwriters which we use for writing the query definition to the debug log.
        StringWriter def1 = new StringWriter()
        StringWriter def2 = new StringWriter()

        //Get the query definition from the result instance id.
        i2b2HelperService.renderQueryDefinition(rID1, "Subset1", def1)
        i2b2HelperService.renderQueryDefinition(rID2, "Subset2", def2)

        //Write the query definition to the log.
        if (log.isDebugEnabled()) {
            log.debug("def1: " + def1.toString())
            log.debug("def2: " + def2.toString())
        }

        //This updates the status and checks to see if the job has been canceled.
        if (asyncJobService.updateStatus(jobName, statusList[2])) {
            return
        }

        //Get the subject IDs from the result instance id.
        def subjectIds1 = i2b2HelperService.getSubjects(rID1)
        def subjectIds2 = i2b2HelperService.getSubjects(rID2)

        //If debug is enabled, write the subject IDs to the log.
        if (log.isDebugEnabled()) {
            log.debug("subjectIds1: ${subjectIds1}")
            log.debug("subjectIds2: ${subjectIds2}")
        }

        //This updates the status and checks to see if the job has been canceled.
        if (asyncJobService.updateStatus(jobName, statusList[3])) {
            return
        }

        //Get the concept codes based on the result instance id.
        def concepts1 = i2b2HelperService.getConcepts(rID1)
        def concepts2 = i2b2HelperService.getConcepts(rID2)

        //If debug is enabled, write the concept IDs to the log.
        if (log.isDebugEnabled()) {
            log.debug("concepts1: ${concepts1}")
            log.debug("concepts2: ${concepts2}")
        }

        //If we are doing a Heatmap we need to address a "*" in the subject heading.
        boolean fixlast = analysis == "Compare"
        //We use the raw microarray data for the Comparative Marker Selection, otherwise we use LOG2.
        boolean rawdata = analysis == "Select"

        //This updates the status and checks to see if the job has been canceled.
        if (asyncJobService.updateStatus(jobName, statusList[4])) {
            return
        }
        try {
            i2b2HelperService.getHeatMapData(pathway_name, subjectIds1, subjectIds2,
                    concepts1, concepts2, timepoints1, timepoints2, sample1, sample2,
                    rbmPanels1, rbmPanels2, datatype, gpf, fixlast, rawdata, analysis)
            def expfilename = System.getProperty("java.io.tmpdir") + File.separator + "datasetexplorer" + File.separator + gpf.getCSVFileName()
            session.expdsfilename = expfilename
            log.info("Filename for export has been set to ${expfilename}")
        } catch (Exception e) {
            def error = e.getMessage()
            log.error("Exception: ${error}", e)
            jobResultsService[jobName]["Status"] = "Error"
            jobResultsService[jobName]["Exception"] = error
            return
        }

        log.debug("Checking to see if the user cancelled the job prior to running it")
        if (jobResultsService[jobName]["Status"] == "Cancelled") {
            log.warn("${jobName} has been cancelled")
            return
        }

        def jdm = new JobDataMap()
        jdm.put("analysis", analysis)
        jdm.put("gctFile", gpf.gctFile())
        jdm.put("clsFile", gpf.clsFile())
        jdm.put("resulttype", resulttype)
        jdm.put("nclusters", nclusters)
        jdm.put("userName", userName)

        def group = "heatmaps"
        def jobDetail = new JobDetail(jobName, group, genePatternService.getClass())
        jobDetail.setJobDataMap(jdm)

        if (asyncJobService.updateStatus(jobName, statusList[5])) {
            return
        }
        def trigger = new SimpleTrigger("triggerNow", group)
        quartzScheduler.scheduleJob(jobDetail, trigger)
        ////println "WIP: Gene Pattern replacement"
        // log.debug('WIP: Gene Pattern replacement')

        JSONObject jsonResult = new JSONObject()
        jsonResult.put("jobName", jobName)
        response.setContentType("text/json")
        response.outputStream << jsonResult.toString()
    }

    /**
     * Method that is called asynchronously from the datasetExplorer Javascript
     * Will determine the type of heatmap to run and then kick off the heatmap job in genePatternService
     */
    def runheatmapsample = {

        //Gather our parameters from the form submission.
        def sampleIdList = request.getParameter("sampleIdList");

        //The sampleIdList will look like {"SampleIdList":{"subset1":["Sample1"],"subset2":[],"subset3":[]}}
        def sampleIdListJSON = JSON.parse(sampleIdList);

        def analysis = request.getParameter("analysis")
        def datatype = request.getParameter("datatype")
        def pathway_name = request.getParameter("pathway_name");
        def nclusters = request.getParameter("nclusters")
        def resulttype = request.getParameter("resulttype")
        def jobName = request.getParameter("jobName")
        def userName = springSecurityService.getPrincipal().username
        def error = null

        // TODO: Put switch in based on analysis
        def statusList = ["Validating Parameters", "Obtaining heatmap data", "Writing GenePattern files", "Triggering GenePattern job"]

        if (analysis == "Compare") {
            statusList.add("Uploading file")
            statusList.add("Running Heatmap Viewer")
        } else if (analysis == "Cluster") {
            statusList.add("Performing Hierarchical Clustering")
            statusList.add("Running Hierarchical Clustering Viewer")
        } else if (analysis == "KMeans") {
            statusList.add("Performing KMeans Clustering")
            statusList.add("Running KMeans Clustering Viewer")
        } else if (analysis == "Select") {
            statusList.add("Imputing Missing Value KNN")
            statusList.add("Performing Comparative Marker Selection")
            statusList.add("Extracting Comparative Marker Results")
            statusList.add("Running Heatmap Viewer")
            statusList.add("Running Comparative Marker Selection Viewer")
        } else if (analysis == "PCA") {
            statusList.add("Uploading file")
            statusList.add("Running PCA")
            statusList.add("Running PCA Viewer")
        }

        jobResultsService[jobName]["StatusList"] = statusList

        //This updates the status and checks to see if the job has been canceled.
        if (asyncJobService.updateStatus(jobName, statusList[0])) {
            return
        }

        //Set a flag based on the type of analysis we are doing.
        boolean fixlast = analysis == "Compare"
        boolean rawdata = analysis == "Select"

        //We need to convert from the Search_Keyword_id to the gene name.
        pathway_name = derivePathwayName(analysis, pathway_name)

        //For most cases, GenePattern server cannot accept gct file with empty expression ratio.
        //Use 0 rather than empty cell. However, Comparative Marker Select needs to use empty space
        String whiteString = GENE_PATTERN_WHITE_SPACE_DEFAULT;
        if (analysis == "Select") whiteString = GENE_PATTERN_WHITE_SPACE_EMPTY;

        //Create the gene patterns file object we use to pass to the gene pattern server.
        GenePatternFiles gpf = new GenePatternFiles();

        //This is the object we use to build the GenePatternFiles.
        ExperimentData experimentData = new ExperimentData();

        experimentData.gpf = gpf;
        experimentData.dataType = datatype;
        experimentData.analysisType = analysis;
        experimentData.sampleIdList = sampleIdListJSON;
        experimentData.whiteString = whiteString;
        experimentData.fixlast = fixlast;
        experimentData.rawdata = rawdata;
        experimentData.pathwayName = pathway_name;

        //
        if (asyncJobService.updateStatus(jobName, statusList[1])) {
            return
        }

        experimentData.getHeatMapDataSample();

        //
        if (asyncJobService.updateStatus(jobName, statusList[2])) {
            return
        }

        experimentData.writeGpFiles();

        //Verify user has not cancelled job.
        log.debug("Checking to see if the user cancelled the job prior to running it")
        def isCancelled = jobResultsService[jobName + ":Status"]
        if (isCancelled == "Cancelled") {
            log.warn("${jobName} has been cancelled")
            return
        }

        //Job information
        def jdm = new JobDataMap()
        jdm.put("analysis", analysis)
        jdm.put("gctFile", experimentData.gpf.gctFile())
        jdm.put("clsFile", gpf.clsFile())
        jdm.put("resulttype", resulttype)
        jdm.put("userName", userName)
        jdm.put("nclusters", nclusters)
        jdm.put("error", error)

        def group = "heatmaps"
        def jobDetail = new JobDetail(jobName, group, genePatternService.getClass())
        jobDetail.setJobDataMap(jdm)

        //
        if (asyncJobService.updateStatus(jobName, statusList[3])) {
            return
        }

        def trigger = new SimpleTrigger("triggerNow", group)
        quartzScheduler.scheduleJob(jobDetail, trigger)
        //   println "WIP: Gene Pattern replacement"
        // log.debug('WIP: Gene Pattern replacement')

        //We feed some text we got back from the job call back to the browser.
        JSONObject jsonResult = new JSONObject()
        jsonResult.put("jobName", jobName)
        response.setContentType("text/json")
        response.outputStream << jsonResult.toString()

    }

    /**
     * Method that will run a survival analysis and is called asynchronously from the datasetexplorer
     */
    def runsurvivalanalysis = {
        if (log.isDebugEnabled()) {
            request.getParameterMap().keySet().each { _key ->
                log.debug("${_key} -> ${request.getParameter(_key)}")
            }
        }

        def rID1 = nullCheck(request.getParameter("result_instance_id1"))
        def rID2 = nullCheck(request.getParameter("result_instance_id2"))
        def qS1 = request.getParameter("querySummary1")
        def qS2 = request.getParameter("querySummary2")
        def jobName = request.getParameter("jobName")

        def statusList = ["Validating Parameters", "Obtaining Cohort Information",
                          "Obtaining Survival Analysis data", "Triggering GenePattern job",
                          "Running Cox Regression", "Calculating Survival Curve"]

        jobResultsService[jobName]["StatusList"] = statusList

        asyncJobService.updateStatus(jobName, statusList[0])

        SurvivalAnalysisFiles saFiles = new SurvivalAnalysisFiles()

        def userName = springSecurityService.getPrincipal().username
        def al = new AccessLog(username: userName, event: "Survival Analysis, Job: ${jobName}",
                eventmessage: "RID1: ${rID1}, RID2: ${rID2}", accesstime: new java.util.Date())
        al.save()

        List<String> subjectIds1;
        List<String> subjectIds2;
        List<String> concepts1;
        List<String> concepts2;
        def hv1 = new HeatmapValidator();
        def hv2 = new HeatmapValidator();
        def ci1 = new CohortInformation();
        def ci2 = new CohortInformation();

        asyncJobService.updateStatus(jobName, statusList[1])
        if (rID1 != null) {
            subjectIds1 = i2b2HelperService.getSubjectsAsList(rID1)
            concepts1 = i2b2HelperService.getConceptsAsList(rID1)
            i2b2HelperService.fillHeatmapValidator(subjectIds1, concepts1, hv1)
            i2b2HelperService.fillCohortInformation(subjectIds1, concepts1, ci1, CohortInformation.TRIALS_TYPE)
        }

        if (rID2 != null) {
            subjectIds2 = i2b2HelperService.getSubjectsAsList(rID2)
            concepts2 = i2b2HelperService.getConceptsAsList(rID2)
            i2b2HelperService.fillHeatmapValidator(subjectIds2, concepts2, hv2)
            i2b2HelperService.fillCohortInformation(subjectIds2, concepts2, ci2, CohortInformation.TRIALS_TYPE)
        }

        asyncJobService.updateStatus(jobName, statusList[2])
        try {
            i2b2HelperService.getSurvivalAnalysisData(concepts1, concepts2, subjectIds1, subjectIds2, saFiles)
        } catch (Exception e) {
            def error = e.getMessage()
            log.error("Exception: ${error}", e)
            jobResultsService[jobName]["Status"] = "Error"
            jobResultsService[jobName]["Exception"] = error
            return
        }

        log.debug("Checking to see if the user cancelled the job prior to running it")
        if (jobResultsService[jobName]["Status"] == "Cancelled") {
            log.warn("${jobName} has been cancelled")
            return
        }

        def imgTmpDir = "/images/datasetExplorer"

        def jdm = new JobDataMap()
        jdm.put("analysis", "Survival")
        jdm.put("gctFile", saFiles.getDataFile())
        jdm.put("clsFile", saFiles.getClsFile())
        jdm.put("imgTmpDir", imgTmpDir)
        jdm.put("imgTmpPath", servletContext.getRealPath(imgTmpDir))
        jdm.put("ctxtPath", servletContext.getContextPath())
        jdm.put("querySum1", qS1)
        jdm.put("querySum2", qS2)

        jdm.put("userName", userName)

        def group = "heatmaps"
        def jobDetail = new JobDetail(jobName, group, genePatternService.getClass())
        jobDetail.setJobDataMap(jdm)

        asyncJobService.updateStatus(jobName, statusList[3])
        def trigger = new SimpleTrigger("triggerNow", group)
        quartzScheduler.scheduleJob(jobDetail, trigger)
        //println "WIP: Gene Pattern replacement"
        //log.debug('WIP: Gene Pattern replacement')
        JSONObject jsonResult = new JSONObject()
        jsonResult.put("jobName", jobName)
        response.setContentType("text/json")
        response.outputStream << jsonResult.toString()
    }

    /**
     * Method that is called asynchronously from the datasetExplorer Javascript
     * Will run the haploviewer but does not use the Quartz job scheduler due to the need for the database connection
     */
    def runhaploviewer = {
        if (log.isDebugEnabled()) {
            request.getParameterMap().keySet().each { _key ->
                log.debug("${_key} -> ${request.getParameter(_key)}")
            }
        }
        def rID1 = nullCheck(request.getParameter("result_instance_id1"))
        def rID2 = nullCheck(request.getParameter("result_instance_id2"))
        def genes = request.getParameter("genes")
        def jobName = request.getParameter("jobName")

        def statusList = ["Validating Parameters"]

        def statusIndex = 1

        if (rID1 != null) {
            statusList.add("Creating haploview for subset 1")
        }

        if (rID2 != null) {
            statusList.add("Creating haploview for subset 2")
        }

        jobResultsService[jobName]["StatusList"] = statusList

        asyncJobService.updateStatus(jobName, statusList[0])

        def userName = springSecurityService.getPrincipal().username
        def al = new AccessLog(username: userName, event: "Haploview Job: ${jobName}",
                eventmessage: "RID1: ${rID1}, RID2: ${rID2}, Genes: ${genes}", accesstime: new java.util.Date())
        al.save()

        def con

        StringBuilder sb = new StringBuilder()
        sb.append("<a  href=\"javascript:showInfo('help/happloview.html');\"><img src=\"${resource(dir:'images',file:'information.png')}\"></a>")
        sb.append("<b>Genes Selected: " + genes + "</b>")
        sb.append("<table><tr>")

        try {
            con = dataSource.getConnection()
            if (rID1 != null) {
                asyncJobService.updateStatus(jobName, statusList[statusIndex])
                sb.append(createHaploView(rID1, genes, con))
                statusIndex += 1
            }
            if (rID2 != null) {
                asyncJobService.updateStatus(jobName, statusList[statusIndex])
                sb.append(createHaploView(rID2, genes, con))
            }
        } catch (Exception e) {
            def error = e.getMessage()
            log.error("Exception: ${error}", e)
            jobResultsService[jobName]["Status"] = "Error"
            jobResultsService[jobName]["Exception"] = error
        } finally {
            con?.close()
        }

        sb.append("</tr></table>")
        jobResultsService[jobName]["Results"] = sb.toString()
    }

    /**
     * Method that is called asynchronously from the sampleExplorer Javascript
     * Will run the haploviewer but does not use the Quartz job scheduler due to the need for the database connection
     */
    def runhaploviewersample = {
        if (log.isDebugEnabled()) {
            request.getParameterMap().keySet().each { _key ->
                log.debug("${_key} -> ${request.getParameter(_key)}")
            }
        }

        //Gather our parameters from the form submission.
        def sampleIdList = request.getParameter("sampleIdList");
        def genes = request.getParameter("genes")
        def jobName = request.getParameter("jobName")

        //The sampleIdList will look like {"SampleIdList":{"subset1":["Sample1"],"subset2":[],"subset3":[]}}
        def sampleIdListJSON = JSON.parse(sampleIdList);

        //Initialize status list.
        def statusList = ["Validating Parameters"]

        //Add a status for each subset.
        sampleIdList.each
                {
                    subsetItem ->

                        def subsetSampleList = subsetItem.value

                        //Don't add a subset if there are no items in the subset.
                        if (subsetSampleList.size() > 0) {
                            statusList.add("Creating haploview for subset " + subsetItem.key)
                        }
                }

        //Assign the status list.
        jobResultsService[jobName]["StatusList"] = statusList

        //Update to our initial status.
        asyncJobService.updateStatus(jobName, statusList[0])

        //Log the action firing in our access log.
        def userName = springSecurityService.getPrincipal().username
        def al = new AccessLog(username: userName, event: "Haploview Job: ${jobName}", eventmessage: "Sample IDs JSON: ${sampleIdListJSON}, Genes: ${genes}", accesstime: new java.util.Date())
        al.save()

        //Add some links to the returned page.
        StringBuilder sb = new StringBuilder()
        sb.append("<a  href=\"javascript:showInfo('help/happloview.html');\"><img src=\"${resource(dir:'images',file:'information.png')}\"></a>")
        sb.append("<b>Genes Selected: " + genes + "</b>")
        sb.append("<table><tr>")

        //We need to increment the status as we generate the Halplo data for each set.
        def statusIndex = 1

        try {
            //Create the connection we will use for our SQL statements.
            def con = dataSource.getConnection()

            //Get data for each subset.
            sampleIdList.each
                    {
                        subsetItem ->

                            def subsetSampleList = subsetItem.value

                            //Don't use a subset if there are no items in the subset.
                            if (subsetSampleList.size() > 0) {
                                //Make a note of which subset we are working on.
                                asyncJobService.updateStatus(jobName, statusList[statusIndex])

                                //Attach data from creating the haploview for our subset.
                                sb.append(createHaploViewSample(subsetSampleList, genes, con))

                                //Update our status index.
                                statusIndex += 1
                            }
                    }
        } catch (Exception e) {
            def error = e.getMessage()
            log.error("Exception: ${error}", e)
            jobResultsService[jobName]["Status"] = "Error"
            jobResultsService[jobName]["Exception"] = error
        } finally {
            con?.close()
        }

        sb.append("</tr></table>")
        jobResultsService[jobName]["Results"] = sb.toString()
    }

    private void callHaploText(String[] args) {
        try {
            // yes, this class does all the work in the constructor and
            // it's instantiated for the collaterals. Don't believe it? See
            // https://github.com/jazzywhit/Haploview/blob/69f7ca282/edu/mit/wi/haploview/HaploText.java#L210
            Class.forName('edu.mit.wi.haploview.HaploText').
                    newInstance(args)
        } catch (ClassNotFoundException e) {
            log.error('Haploview class not found. It is not bundled anymore. ' +
                    'You will need to add its jar as a dependency')
        }
    }

    /**
     * Helper method to create the haploview for each result instance ID
     *
     * @param rID - the result instance ID
     * @param genes - the list of genes
     * @param con - the database connection
     */
    private String createHaploView(String rID, String genes, java.sql.Connection con) {
        def retValue

        PEDFormat ped = new PEDFormat()

        def ids = i2b2HelperService.getSubjects(rID)

        String fileroot = System.getProperty("java.io.tmpdir")
        File tempFile = File.createTempFile("haplo", ".tmp", new File(fileroot))
        def filenamein = tempFile.getName()

        if (!fileroot.endsWith(File.separator)) {
            fileroot = fileroot + File.separator;
        }

        def pathin = fileroot + filenamein
        def pathinped = fileroot + filenamein + ".ped"
        def pathininfo = fileroot + filenamein + ".info"

        boolean s1 = ped.createPEDFile(genes, ids, pathin, con)

        String[] args = ["-nogui", "-quiet", "-pedfile", pathinped, "-info", pathininfo, "-png"]

        callHaploText(args)
        String filename = filenamein + ".ped.LD.PNG"

        String hapleUrl = request.getContextPath() + "/chart/displayChart?filename=" + filename
        if (s1) {
            retValue = "<td><img src='${hapleUrl}'  border=0></td>"
        } else {
            retValue = "<td>Not enough data to generate haploview</td>"
        }

        return retValue
    }

    /**
     * Helper method to create the haploview for each subset
     *
     * @param sampleIdList - The list of samples in this subset.
     * @param genes - the list of genes
     * @param con - the database connection
     */
    private String createHaploViewSample(sampleIdList, String genes, java.sql.Connection con) {
        //This will hold the html we send back to the browser.
        def retValue

        //This is the file format that has to be fed to the HaploViewer
        PEDFormat ped = new PEDFormat()

        //Get the list of subject IDs based on Sample ID.
        def ids = i2b2HelperService.getSubjectsAsListFromSample(sampleIdList)

        //Create temporary files.
        String fileroot = System.getProperty("java.io.tmpdir")
        File tempFile = File.createTempFile("haplo", ".tmp", new File(fileroot))
        def filenamein = tempFile.getName()

        if (!fileroot.endsWith(File.separator)) {
            fileroot = fileroot + File.separator;
        }

        //Create path strings.
        def pathin = fileroot + filenamein
        def pathinped = fileroot + filenamein + ".ped"
        def pathininfo = fileroot + filenamein + ".info"

        //Create PED file.
        boolean s1 = ped.createPEDFile(genes, ids, pathin, con)

        //If we were able to write the file,
        if (s1) {
            //Create argument array.
            String[] args = ["-nogui", "-quiet", "-pedfile", pathinped, "-info", pathininfo, "-png"]

            callHaploText(args)

            //This is the filename of the image.
            String filename = filenamein + ".ped.LD.PNG"

            String hapleUrl = request.getContextPath() + "/chart/displayChart?filename=" + filename

            retValue = "<td><img src='${hapleUrl}'  border=0></td>"
        } else {
            retValue = "<td>Not enough data to generate haploview</td>"
        }

        return retValue
    }

    def showGwasSelection = {
        String resultInstanceID1 = request.getParameter("result_instance_id1");
        String resultInstanceID2 = request.getParameter("result_instance_id2");

        def snpDatasetNum_1 = 0, snpDatasetNum_2 = 0;
        if (resultInstanceID1 != null && resultInstanceID1.trim().length() != 0) {
            String subjectIds1 = i2b2HelperService.getSubjects(resultInstanceID1);
            List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds1);
            if (idList != null) snpDatasetNum_1 = idList.size();
        }
        if (resultInstanceID2 != null && resultInstanceID2.trim().length() != 0) {
            String subjectIds2 = i2b2HelperService.getSubjects(resultInstanceID2);
            List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds2);
            if (idList != null) snpDatasetNum_2 = idList.size();
        }

        String warningMsg = null;
        if (snpDatasetNum_1 + snpDatasetNum_2 > 10) {
            warningMsg = "Note: The performance may be slow with more than 10 SNP datasets. Please consider displaying individual chromosomes.";
        }
        def chroms = ['ALL', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', 'X', 'Y'];
        [chroms: chroms, snpDatasetNum_1: snpDatasetNum_1, snpDatasetNum_2: snpDatasetNum_2, warningMsg: warningMsg, chromDefault: 'ALL'];
    }

    /**
     * Method that will run a survival analysis and is called asynchronously from the datasetexplorer
     */
    def runGwas = {
        if (log.isDebugEnabled()) {
            request.getParameterMap().keySet().each { _key ->
                log.debug("${_key} -> ${request.getParameter(_key)}")
            }
        }

        def rID1 = nullCheck(request.getParameter("result_instance_id1"))
        def rID2 = nullCheck(request.getParameter("result_instance_id2"))
        def qS1 = request.getParameter("querySummary1")
        def qS2 = request.getParameter("querySummary2")
        def jobName = request.getParameter("jobName");

        def statusList = ["Validating Parameters", "Obtaining Cohort Information",
                          "Obtaining SNP data", "Triggering PLINK job",
                          "Running PLINK"]

        jobResultsService[jobName]["StatusList"] = statusList

        asyncJobService.updateStatus(jobName, statusList[0])

        def userName = springSecurityService.getPrincipal().username
        def al = new AccessLog(username: userName, event: "Survival Analysis, Job: ${jobName}",
                eventmessage: "RID1: ${rID1}, RID2: ${rID2}", accesstime: new java.util.Date())
        al.save()

        List<String> subjectIds1;
        List<String> subjectIds2;
        List<String> concepts1;
        List<String> concepts2;

        asyncJobService.updateStatus(jobName, statusList[1])
        if (rID1 != null) {
            subjectIds1 = i2b2HelperService.getSubjectsAsList(rID1)
            concepts1 = i2b2HelperService.getConceptsAsList(rID1)
        }

        if (rID2 != null) {
            subjectIds2 = i2b2HelperService.getSubjectsAsList(rID2)
            concepts2 = i2b2HelperService.getConceptsAsList(rID2)
        }

        asyncJobService.updateStatus(jobName, statusList[2])

        GwasFiles gwasFiles = new GwasFiles(getGenePatternFileDirName(),
                createLink(controller: 'analysis', action: 'getGenePatternFile', absolute: true))
        String chroms = request.getParameter("chroms");
        try {
            i2b2HelperService.getGwasDataByPatient(subjectIds1, subjectIds2, chroms, gwasFiles);
        } catch (Exception e) {
            def error = e.getMessage()
            log.error("Exception: ${error}", e)
            jobResultsService[jobName]["Status"] = "Error"
            jobResultsService[jobName]["Exception"] = error
            return
        }

        log.debug("Checking to see if the user cancelled the job prior to running it")
        if (jobResultsService[jobName]["Status"] == "Cancelled") {
            log.warn("${jobName} has been cancelled")
            return
        }

        def jdm = new JobDataMap()
        jdm.put("analysis", "GWAS");
        jdm.put("gwasFiles", gwasFiles);
        jdm.put("querySum1", qS1)
        jdm.put("querySum2", qS2)

        jdm.put("userName", userName)

        def group = "heatmaps"
        def jobDetail = new JobDetail(jobName, group, genePatternService.getClass())
        jobDetail.setJobDataMap(jdm)

        asyncJobService.updateStatus(jobName, statusList[3])
        def trigger = new SimpleTrigger("triggerNow", group)
        quartzScheduler.scheduleJob(jobDetail, trigger)
        //println "WIP: Gene Pattern replacement"
        //log.debug('WIP: Gene Pattern replacement')

        JSONObject jsonResult = new JSONObject()
        jsonResult.put("jobName", jobName)
        response.setContentType("text/json")
        response.outputStream << jsonResult.toString()
    }

    protected String getGenePatternFileDirName() {
        String fileDirName = grailsApplication.config.com.recomdata.analysis.genepattern.file.dir;
        String webRootName = servletContext.getRealPath("/");
        if (webRootName.endsWith(File.separator) == false)
            webRootName += File.separator;
        return webRootName + fileDirName;
    }

    /**
     * Helper method to return null from Javascript calls
     *
     * @param inputArg - the input arguments
     * @return null or the input argument if it is not null (or empty or undefined)
     */
    private String nullCheck(inputArg) {
        log.debug("Input argument to nullCheck: ${inputArg}")
        if (inputArg == "undefined" || inputArg == "null" || inputArg == "") {
            log.debug("Returning null in nullCheck")
            return null
        }
        return inputArg
    }

    /**
     * Helper method to derive the pathway name
     *
     * @param analysis the type of analysis to run
     * @param pathway_name the pathway name in the request
     * @return the pathway name, null or the search result
     */
    private String derivePathwayName(analysis, pathway_name) {
        log.info("Derived pathway name as ${pathway_name}")
        if (analysis != "Select" && analysis != "PCA") {
            log.debug("Pathway name has been set to ${pathway_name}")
            if (pathway_name == null || pathway_name.length() == 0 || pathway_name == "null") {
                log.debug("Resetting pathway name to null")
                pathway_name = null
            }
            boolean nativeSearch = grailsApplication.config.com.recomdata.search.genepathway == 'native'
            log.debug("nativeSearch: ${nativeSearch}")
            if (!nativeSearch && pathway_name != null) {
                pathway_name = SearchKeyword.get(Long.valueOf(pathway_name)).uniqueId;
                log.debug("pathway_name has been set to a keyword ID: ${pathway_name}")
            }
        }
        return pathway_name
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // These are for the synchronous operation - soon to be replaced
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    def showWorkflowStatus = {
        def wfstatus = session["workflowstatus"];
        if (wfstatus == null) {
            wfstatus = new WorkflowStatus();
            session["workflowstatus"] = wfstatus;
            session["workflowstatus"].setCurrentJobStatus(new JobStatus(name: "initializing Workflow", status: "R"));
        }

        render(view: "workflowStatus");
    }

    def checkWorkflowStatus = {
        // check session status
        def wfstatus = session["workflowstatus"];

        JSONObject result = wfstatus.result;
        if (result == null) {
            result = new JSONObject();
        }

        def statusHtml = g.render(template: "jobStatus", model: [wfstatus: wfstatus]).toString();
        result.put("statusHTML", statusHtml);
        println(statusHtml);

        if (wfstatus.isCompleted()) {
            result.put("wfstatus", "completed");
            wfstatus.rpCount++;
            result.put("rpCount", wfstatus.rpCount);
        } else {
            result.put("wfstatus", "running");
        }
        render result.toString();
    }

    def cancelJob = {
        def wfstatus = session["workflowstatus"]
        wfstatus.setCancelled();
        render(wfstatus.jobStatusList as JSON)
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
