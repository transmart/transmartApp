import com.recomdata.transmart.domain.i2b2.AsyncJob
import org.json.JSONObject

class GenericJobController {

    def springSecurityService
    def jobResultsService
    def asyncJobService

    /**
     * Method that will create the new asynchronous job name
     * Current methodology is username-jobtype-ID from sequence generator
     */
    def createnewjob = {
        def userName = springSecurityService.getPrincipal().username
        def analysis = request.getParameter("analysis")
        def jobStatus = "Started"

        def newJob = new AsyncJob(lastRunOn: new Date())
        newJob.save()
        def jobName = userName.replaceAll(/[^0-9A-Za-z]*/, "") + "-" + analysis + "-" + newJob.id
        newJob.jobName = jobName
        newJob.jobStatus = jobStatus
        newJob.jobType = analysis
        newJob.save()

        jobResultsService[jobName] = [:]
        asyncJobService.updateStatus(jobName, jobStatus)

        log.debug("Sending ${jobName} back to the client")
        JSONObject result = new JSONObject()
        result.put("jobName", jobName)
        result.put("jobStatus", jobStatus)
        response.setContentType("text/json")
        response.outputStream << result.toString()
    }
}
