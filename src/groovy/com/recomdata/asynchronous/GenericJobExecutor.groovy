package com.recomdata.asynchronous

import com.recomdata.transmart.data.export.exception.DataNotFoundException
import com.recomdata.transmart.data.export.util.FTPUtil
import com.recomdata.transmart.data.export.util.ZipUtil
import grails.util.Holders
import groovy.util.logging.Log4j
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor
import org.quartz.Job
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.RConnection
import org.rosuda.REngine.Rserve.RserveException
import org.transmart.authorization.CurrentUserBeanProxyFactory
import org.transmart.spring.QuartzSpringScope

import java.lang.reflect.UndeclaredThrowableException

/**
 * This class will encompass the job scheduled by Quartz. When the execute method is called we will travel down a list of predefined methods to prep data
 *
 * @author MMcDuffie
 *
 */
@Log4j
class GenericJobExecutor implements Job {

    def ctx = Holders.grailsApplication.mainContext
    def springSecurityService = ctx.springSecurityService
    def jobResultsService = ctx.jobResultsService
    def i2b2HelperService = ctx.i2b2HelperService
    def dataExportService = ctx.dataExportService
    def asyncJobService = ctx.asyncJobService

    QuartzSpringScope quartzSpringScope = ctx.quartzSpringScope

    final String tempFolderDirectory = Holders.config.com.recomdata.plugins.tempFolderDirectory

    String jobTmpDirectory
    //This is where all the R scripts get run, intermediate files are created, images are initially saved, etc.
    String jobTmpWorkingDirectory
    String finalOutputFile

    def jobDataMap
    def jobName

    File jobInfoFile

    // TODO -- NEED TO BE REVIEWED (f.guitton@imperial.ac.uk)
    private void init() {
        //Put an entry in our log.
        log.info("${jobName} has been triggered to run ")

        //Get the data map which shows the attributes for our job.

        //Write our attributes to a log file.
        if (log.isDebugEnabled()) {
            jobDataMap.getKeys().each { _key ->
                log.debug("\t${_key} -> ${jobDataMap[_key]}")
            }
        }

        log.info("Data Export Service: " + dataExportService)

//		grailsApplication = jobDataMap.get("SGA")
//		jobResultsService = jobDataMap.get("SJRS")
//		asyncJobService = jobDataMap.get("SAJS")
//		dataExportService = jobDataMap.get("SDES")

    }
    // --

    public void execute(JobExecutionContext jobExecutionContext) {
        def userInContext = jobExecutionContext.jobDetail.jobDataMap['userInContext']

        // put the user in context
        quartzSpringScope."${CurrentUserBeanProxyFactory.SUB_BEAN_QUARTZ}" =
                userInContext

        PersistenceContextInterceptor interceptor
        try {
            interceptor = Holders.applicationContext.persistenceInterceptor
            interceptor.init()
            doExecute(jobExecutionContext.jobDetail)
        } finally {
            // Thread will be reused, need to clear user in context
            quartzSpringScope.clear()
            interceptor.flush()
            interceptor.destroy()
        }
    }

    private void doExecute(JobDetail jobDetail) {
        //Gather the jobs info.
        jobName = jobDetail.getName()
        jobDataMap = jobDetail.getJobDataMap()

        //Initialize
        init();

        //Initialize the jobTmpDirectory which will be used during bundling in ZipUtil
        jobTmpDirectory = tempFolderDirectory + File.separator + "${jobName}" + File.separator
        jobTmpDirectory = jobTmpDirectory.replace("\\", "\\\\")
        if (new File(jobTmpDirectory).exists()) {
            log.warn("The job folder ${jobTmpDirectory} already exists. It's going to be overwritten.")
            FileUtils.deleteDirectory(new File(jobTmpDirectory))
        }
        jobTmpWorkingDirectory = jobTmpDirectory + "workingDirectory"

        //Try to make the working directory.
        File jtd = new File(jobTmpWorkingDirectory)
        jtd.mkdirs();

        //Create a file that will have all the job parameters for debugging purposes.
        jobInfoFile = new File(jobTmpWorkingDirectory + File.separator + 'jobInfo.txt')

        //Write our parameters to the file.
        jobInfoFile.write("Parameters" + System.getProperty("line.separator"))
        jobDataMap.getKeys().each { _key ->
            jobInfoFile.append("\t${_key} -> ${jobDataMap[_key]}" + System.getProperty("line.separator"))
        }

        //JobResult[] jresult
        String sResult
        try {
            //TODO: Possibly abstract this our so the Quartz job doesn't have all this nonsense.
            updateStatus(jobName, "Gathering Data")
            if (isJobCancelled(jobName)) return
            getData()

            updateStatus(jobName, "Running Conversions")
            if (isJobCancelled(jobName)) return
            runConversions()

            updateStatus(jobName, "Running Analysis")
            if (isJobCancelled(jobName)) return
            runAnalysis()

            updateStatus(jobName, "Rendering Output")
            if (isJobCancelled(jobName)) return
            renderOutput(jobDetail)

        } catch (DataNotFoundException dnfe) {
            log.error("DAO exception thrown executing job: " + dnfe.getMessage(), dnfe)
            jobResultsService[jobName]["Exception"] = dnfe.getMessage()
            return
            /*}catch(WebServiceException wse)	{
                log.error("WebServiceException thrown executing job: " + wse.getMessage(), wse)
                jobResultsService[jobName]["Exception"] = "There was an error running your job. Please contact an administrator."
                return*/
        } catch (RserveException rse) {
            log.error("RserveException thrown executing job: " + rse.getMessage(), rse)
            jobResultsService[jobName]["Exception"] = "There was an error running the R script for your job. Please contact an administrator."
            return
        } catch (Exception e) {
            log.error("Exception thrown executing job: " + e.getMessage(), e)
            def errorMsg = null
            if (e instanceof UndeclaredThrowableException) {
                errorMsg = ((UndeclaredThrowableException) e)?.getUndeclaredThrowable().message
            } else {
                errorMsg = e?.message
            }
            if (!errorMsg?.trim()) {
                errorMsg = "There was an error running your job \'${jobName}\'. Please contact an administrator."
            }
            jobResultsService[jobName]["Exception"] = errorMsg
            return
        } finally {
            if (jobResultsService[jobName]["Exception"] != null) {
                asyncJobService.updateStatus(jobName, "Error", null, null, jobResultsService[jobName]["Exception"])
            }
        }

        //Marking the status as complete makes the
        updateStatus(jobName, "Completed")
    }

    private boolean isStudySelected(int studyCnt, List checkboxList) {
        boolean studySelected = false
        for (checkbox in checkboxList) {
            if (StringUtils.contains(checkbox, "subset" + studyCnt)) {
                studySelected = true
                break
            }
        }
        return studySelected
    }

    //This method assumes we have
    private void getData() throws Exception {
        jobDataMap.put('jobTmpDirectory', jobTmpDirectory)

        dataExportService.exportData(jobDataMap)
    }

    private void runConversions() {
        //Get the data based on the job configuration.
        def conversionSteps = jobDataMap.get("conversionSteps")

        conversionSteps.each
                {
                    currentStep ->

                        switch (currentStep.key) {
                            case "R":

                                //Call a function to process our R commands.
                                runRCommandList(currentStep.value);
                        }

                }
    }

    private void runAnalysis() {
        //Get the data based on the job configuration.
        def analysisSteps = jobDataMap.get("analysisSteps")

        analysisSteps.each
                {
                    currentStep ->

                        switch (currentStep.key) {
                            case "bundle":
                                /** Access the ZipUtil in a static way */
                                String zipFileLoc = (new File(jobTmpDirectory))?.getParent() + File.separator;
                                finalOutputFile = ZipUtil.zipFolder(jobTmpDirectory, zipFileLoc + jobDataMap.get("jobName") + ".zip")
                                try {
                                    File outputFile = new File(zipFileLoc + finalOutputFile);
                                    if (outputFile.isFile()) {
                                        String remoteFilePath = FTPUtil.uploadFile(true, outputFile);
                                        if (StringUtils.isNotEmpty(remoteFilePath)) {
                                            //Since File has been uploaded to the FTP server, we can delete the
                                            //ZIP file and the folder which has been zipped

                                            //Delete the output Folder
                                            String outputFolder = null;
                                            int index = outputFile.name.lastIndexOf('.');
                                            if (index > 0 && index <= outputFile.name.length() - 2) {
                                                outputFolder = outputFile.name.substring(0, index);
                                            }
                                            File outputDir = new File(zipFileLoc + outputFolder)
                                            if (outputDir.isDirectory()) {
                                                outputDir.deleteDir()
                                            }

                                            //Delete the ZIP file
                                            outputFile.delete();
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error("Failed to FTP PUT the ZIP file: " + e.getMessage);
                                }

                                break
                            case "R":

                                //Call a function to process our R commands.
                                runRCommandList(currentStep.value);
                                break
                        }

                }
    }

    private void renderOutput(jobDetail) {
        //Get the data based on the job configuration.
        def renderSteps = jobDataMap.get("renderSteps")

        renderSteps.each
                {
                    currentStep ->

                        switch (currentStep.key) {
                            case "FILELINK":

                                //Gather the jobs name.
                                def jobName = jobDetail.getName()

                                //Add the result file link to the job.
                                jobResultsService[jobName]['resultType'] = "DataExport"
                                jobResultsService[jobName]["ViewerURL"] = finalOutputFile
                                asyncJobService.updateStatus(jobName, "Rendering Output", finalOutputFile, null, null)
                                break;
                            case "GSP":
                                //Gather the jobs name.
                                def jobName = jobDetail.getName()

                                //Add the link to the output URL to the jobs object. We get the base URL from the job parameters.
                                jobResultsService[jobName]["ViewerURL"] = currentStep.value + "?jobName=" + jobName
                                break;
                        }
                }

    }

    private void runRCommandList(stepList) {

        //We need to get the study ID for this study so we can know the path to the clinical output file.
        def studies = jobDataMap.get("studyAccessions")

        //String representing rOutput Directory.
        String rOutputDirectory = jobTmpWorkingDirectory

        //Make sure an rOutputFiles folder exists in our job directory.
        new File(rOutputDirectory).mkdir()

        //Establish a connection to R Server.
        RConnection c = new RConnection(Holders.config.RModules.host, Holders.config.RModules.port);

        log.debug("Attempting following R Command : " + "setwd('${rOutputDirectory}')".replace("\\", "\\\\"))

        //Set the working directory to be our temporary location.
        String workingDirectoryCommand = "setwd('${rOutputDirectory}')".replace("\\", "\\\\")

        //Run the R command to set the working directory to our temp directory.
        REXP x = c.eval(workingDirectoryCommand);

        //For each R step there is a list of commands.
        stepList.each { currentCommand ->

            //Need to escape backslashes for R commands.
            String reformattedCommand = currentCommand.replace("\\", "\\\\")

            //Replace the working directory flag if it exists in the string.
            reformattedCommand = reformattedCommand.replace("||TEMPFOLDERDIRECTORY||", jobTmpDirectory + "subset1_" + studies[0] + File.separator.replace("\\", "\\\\"))

            //We need to loop through the variable map and do string replacements on the R command.
            jobDataMap.get("variableMap").each { variableItem ->

                //Try and grab the variable from the Job Data Map. These were fed in from the HTML form.
                def valueFromForm = jobDataMap.get(variableItem.value)

                //Clean up the variable if it was found in the form.
                if (valueFromForm) {
                    valueFromForm = valueFromForm.replace("\\", "\\\\").trim()
                } else {
                    valueFromForm = ""
                }

                reformattedCommand = reformattedCommand.replace(variableItem.key, valueFromForm)
            }

            log.debug("Attempting following R Command : " + reformattedCommand)

            //Run the R command against our server.
            //x = c.eval(reformattedCommand);

            REXP r = c.parseAndEval("try(" + reformattedCommand + ",silent=TRUE)");

            if (r.inherits("try-error")) {
                //Grab the error R gave us.
                String rError = r.asString()

                //This is the error we will eventually throw.
                RserveException newError = null

                //If it is a friendly error, use that, otherwise throw the default message.
                if (rError ==~ /.*\|\|FRIENDLY\|\|.*/) {
                    rError = rError.replaceFirst(/.*\|\|FRIENDLY\|\|/, "")
                    newError = new RserveException(c, rError);
                } else {
                    log.error("RserveException thrown executing job: " + rError)
                    newError = new RserveException(c, "There was an error running the R script for your job. Please contact an administrator.");
                }

                c.close();

                throw newError;

            }

        }

        c.close();
    }

    /**
     * Helper to update the status of the job and log it
     *
     * @param jobName - the unique job name
     * @param status - the new status
     * @return
     */
    def updateStatus(jobName, status) {
        jobResultsService[jobName]["Status"] = status
        log.debug(status)
        asyncJobService.updateStatus(jobName, status)
    }

    def boolean isJobCancelled(jobName) {
        boolean jobCancelled = false

        //if no job has been submitted, it cannot be cancelled
        if (! jobName) return false

        //log.debug("Checking to see if the user cancelled the job")
        if (jobResultsService[jobName]["Status"] == "Cancelled") {
            log.warn("${jobName} has been cancelled")
            jobCancelled = true
        }
        return jobCancelled
    }

}
