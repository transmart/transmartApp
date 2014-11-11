import org.transmart.SearchResult


/**
 * $Id: DocumentController.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */

class DocumentController {

//	static Logger log = Logger.getLogger(DocumentController.class);
    def documentService
    def searchService

    def showDocumentFilter = {

        def filter = session.searchFilter.documentFilter
        render(template: 'documentFilter', model: [filter: filter, repositories: filter.repositories])

    }

    def filterDocument = {

        def filter = session.searchFilter.documentFilter
        for (repository in filter.repositories.keySet()) {
            filter.repositories.put(repository, "on".equals(params.get("repository_" + repository.toLowerCase().replace(" ", "_"))))
        }
        filter.path = params.path
        filter.type_excel = "on".equals(params.type_excel)
        filter.type_html = "on".equals(params.type_html)
        filter.type_pdf = "on".equals(params.type_pdf)
        filter.type_powerpoint = "on".equals(params.type_powerpoint)
        filter.type_text = "on".equals(params.type_text)
        filter.type_word = "on".equals(params.type_word)
        filter.type_other = "on".equals(params.type_other)

        def sResult = new SearchResult()
        session.searchFilter.datasource = "document"
        searchService.doResultCount(sResult, session.searchFilter)
        render(view: '/search/list', model: [searchresult: sResult])

    }

    def datasourceDocument = {

        def sResult = new SearchResult()
        sResult.result = documentService.documentData(session.searchFilter, params)
        sResult.documentCount = documentService.documentCount(session.searchFilter)
        render(template: 'documentResult', model: [searchresult: sResult])

    }

    def downloadFile = {

        def types = [
                "doc" : "application/msword",
                "docx": "application/msword",
                "htm" : "text/html",
                "html": "text/html",
                "pdf" : "application/pdf",
                "ppt" : "application/ms-powerpoint",
                "pptx": "application/ms-powerpoint",
                "rtf" : "application/rtf",
                "txt" : "text/plain",
                "xls" : "application/ms-excel",
                "xlsx": "application/ms-excel",
                "xml" : "text/xml"
        ]
        def fileName = URLDecoder.decode(params.file)
        def file = new File(fileName)
        if (!file.exists()) {
            def title = "Unable to Display File"
            def message = "\"" + file.getAbsolutePath() + "\" was not found on the server."
            render(view: "search/error", model: [title: title, message: message])
        } else if (!file.canRead()) {
            def title = "Unable to Display File"
            def message = "\"" + file.getAbsolutePath() + "\" could not be accessed on the server."
            render(view: "search/error", model: [title: title, message: message])
        } else {
            def contentType = "application/octet-stream"
            def start = file.getName().lastIndexOf(".");
            if (start != -1) {
                def ext = file.getName().substring(start + 1, file.getName().length())
                contentType = types.get(ext)
                if (contentType == null) {
                    contentType = "application/octet-stream"
                }
            }

            if (contentType == "application/pdf") {
                response.setHeader("Content-Type", contentType)
                response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"")
            } else if (contentType == "application/octet-stream") {
                response.setHeader("Content-Type", contentType)
                response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                response.setHeader("Content-Length", Long.toString(file.size()))
            } else if (contentType == "text/hmtl") {
                response.setHeader("Content-Type", contentType)
                response.setHeader("Content-Length", Long.toString(file.size()))
            } else if (contentType != "text/html") {
                response.setHeader("Content-Type", contentType)
                response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
            }
            file.withInputStream {
                response.outputStream << it
            }
            response.outputStream.flush()
        }

    }

}
