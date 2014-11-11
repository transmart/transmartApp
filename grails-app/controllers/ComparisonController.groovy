import grails.converters.JSON

class ComparisonController {

    def index = {}

    def i2b2HelperService

    def getQueryDefinition = {
        String qid = request.getParameter("qid");
        String q = i2b2HelperService.getQueryDefinitionXMLFromQID(qid);
        log.debug(q);
        PrintWriter pw = new PrintWriter(response.getOutputStream());
        pw.write(q);
        pw.flush();
    }

    def save = {
        def qid1 = request.getParameter("result_instance_id1")
        def qid2 = request.getParameter("result_instance_id2")
        def s = new i2b2.Comparison()

        try {
            s.queryResultId1 = Integer.parseInt(qid1)
        } catch (NumberFormatException nfe) {
            s.queryResultId1 = -1
        }

        try {
            s.queryResultId2 = Integer.parseInt(qid2)
        } catch (NumberFormatException nfe) {
            s.queryResultId2 = -1
        }

        boolean success = s.save()

        def link = new StringBuilder()
        link.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"mailto:?subject=Link to ")
        link.append("Saved comparison ID=${s.id}")
        link.append("&body=The following is a link to the saved comparison in tranSMART.  Please, note that you need to be logged into tranSMART prior to using this link.%0A%0A")
        link.append(createLink(controller: 'datasetExplorer', action: 'index', id: s.id, absolute: true))
        link.append("\" target=\"_blank\" class=\"tiny\" style=\"text-decoration:underline;color:blue;font-size:11px;\">Email this comparison</a><br /><br />")
        def result = [success: success, id: s.id, link: link]
        log.trace(result as JSON)
        render result as JSON
    }
}
