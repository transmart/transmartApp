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
  

 import com.recomdata.search.DocumentHit
 import java.io.File
 import java.net.URLDecoder
import org.apache.log4j.Logger
import org.transmart.SearchResult;

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
		render(template:'documentFilter', model:[filter:filter, repositories:filter.repositories])
		
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
		filter.type_text= "on".equals(params.type_text)
		filter.type_word = "on".equals(params.type_word)
		filter.type_other = "on".equals(params.type_other)
		
		def sResult = new SearchResult()
		session.searchFilter.datasource = "document"		
		searchService.doResultCount(sResult, session.searchFilter)
		render(view:'/search/list', model:[searchresult:sResult])
		
	}

	def datasourceDocument = {

		def sResult = new SearchResult()
		sResult.result = documentService.documentData(session.searchFilter, params)
		sResult.documentCount = documentService.documentCount(session.searchFilter)
		render(template:'documentResult', model:[searchresult:sResult])

	}
	
	def downloadFile = {
	
		def types = [
		             "doc":"application/msword",
		             "docx":"application/msword",
		             "htm":"text/html",
		             "html":"text/html",
		             "pdf":"application/pdf",
		             "ppt":"application/ms-powerpoint",
		             "pptx":"application/ms-powerpoint",
		             "rtf":"application/rtf",
		             "txt":"text/plain",
		             "xls":"application/ms-excel",
		             "xlsx":"application/ms-excel",
		             "xml":"text/xml"
		            ]
		def fileName = URLDecoder.decode(params.file)
		def file = new File(fileName)
		if (!file.exists()) {
			def title = "Unable to Display File"
			def message = "\"" + file.getAbsolutePath() + "\" was not found on the server."
			render(view:"search/error", model:[title:title, message:message])
		} else if (!file.canRead()) {
			def title = "Unable to Display File"
			def message = "\"" + file.getAbsolutePath() + "\" could not be accessed on the server."
			render(view:"search/error", model:[title:title, message:message])
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
