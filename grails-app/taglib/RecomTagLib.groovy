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
  

import java.io.File
import com.recomdata.search.DocumentHit
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * $Id: RecomTagLib.groovy 10280 2011-10-29 03:00:52Z jliu $
 * @author $Author: jliu $
 * @version $Revision: 10280 $
 */

class RecomTagLib {

	def createFileLink = { attrs ->

		def document = attrs["document"]
		def content = attrs["content"]
		def displaylabel = attrs["displayLabel"]
		def label
		def path
		def url

		if (document != null) {
			def repository = document.getRepository()
			content = org.transmart.biomart.ContentRepository.findByRepositoryType(repository)
			label = document.getFileName()
			path = content.location + "/" + document.getFilePath()
			switch (content.getLocationType()) {
			case "FILE":
				url = createLink(controller:"document", action:"downloadFile", params:[file:path])
				break
			case "SHARE":
				path = path.replace("/", "\\").replace("\\", "\\\\")
				url = path
				break
			default:
				url = path
				break;
			}
		} else if (content != null){
			if (displaylabel != null){
				label = displaylabel
			} else {
				label = content.name
			}
			switch (content.getLocationType()) {
			case "FILE":
				path = content.getAbsolutePath()
				url = createLink(controller:"document", action:"downloadFile", params:[file:path])
				break
			case "SHARE":
				path = content.getAbsolutePath().replace("/", "\\").replace("\\", "\\\\")
				url = path
				break
			case "URL":
				//path = content.repository.location + URLEncoder.encode(content.location)
			path = content.repository.location+content.location
				url = path
				break
			default:
				content.getAbsolutePath()
				url = path
				break;
			}
		}
		out << "<a style=\"border: none\" target=\"_blank\" onclick=\"popupWindow('"
		out << url
		out << "', 'documentWindow');\">"

		def start = path.lastIndexOf(".");
		def ext = "txt"
		def imageName = "text.png"
		if (start != -1) {
			ext = path.substring(start + 1, path.length()).toLowerCase()
			switch (ext) {
				case "pdf":
				imageName = "acrobat.png"
				break
				case ["doc", "docx"]:
				imageName = "word.png"
				break
				case ["xls", "xlsx"]:
				imageName = "excel.png"
				break;
				case ["ppt", "pptx"]:
				imageName = "powerpoint.png"
				break
				case ["htm", "html"]:
				imageName = "webpage.png"
				break
				case ["txt", "text"]:
				imageName = "text.png"
				break
				default:
				if (content?.getLocationType() == "URL") {
					imageName = "webpage.png"
				}
			}
		}
		out << "<img src=\""
		out << resource(dir:"images",file:imageName)
		out << "\"/>&nbsp;"

		//		if (isLocked) {
		//			out << "<img style=\"background: transparent;\" src=\""
		//			out << resource(dir:"images",file:"lock_go.png")
		//			out << "\"/>&nbsp;"
		//		}

		out << label.encodeAsHTML()
		out << "</a>"

	}

	def createFilterDetailsLink = { attrs ->

		def id = attrs?.id == null ? "" : attrs.id
		def altId = attrs?.altId == null ? "" : attrs.altId
		def label = attrs?.label == null ? "" : attrs.label
		def type = attrs?.type == null ? "" : attrs.type
		def plain = "true".equals(attrs?.plain)

		//println("\tcreateFilterDetailsLink id:" + id + ", altId:" + altId + ", label:" + label + ", type:" + type)
		if (type == "gene" || type == "pathway" || type == "compound") {
			out << "<a href=\"#\" onclick=\"var w=window.open('"
			out << createLink(controller:"details", action:type, params:[id:id, altId:altId] )
			out << "', 'detailsWindow', 'width=900,height=800'); w.focus(); return false;\">"
			out << "<span class=\"filter-item filter-item-"
			out << type
			out << "\">"
			out << label
			out << "</span>"
			if (!plain) {
				out << "&nbsp;<img class=\"ExternalLink\" src=\""
				out << resource(dir:"images",file:"linkext7.gif")
				out << "\"/>"
			}
			out << "</a>"
		}
		else if (type == "genesig" || type == "genelist") {
				out << "<a href=\"#\" onclick=\"var w=window.open('"
				out << createLink(controller:"geneSignature", action:"showDetail", params:[id:id] )
				out << "', 'detailsWindow', 'width=900,height=800,scrollbars=yes'); w.focus(); return false;\">"
				out << "<span class=\"filter-item filter-item-"
				out << type
				out << "\">"
				out << label
				out << "</span>"
				if (!plain) {
					out << "&nbsp;<img class=\"ExternalLink\" src=\""
					out << resource(dir:"images",file:"linkext7.gif")
					out << "\"/>"
				}
				out << "</a>"
		} else if (type == "text") {
			out << "<span class=\"filter-item filter-item-"
			out << type
			out << "\">\""
			out << label
			out << "\"</span>"
		} else {
			out << "<span class=\"filter-item filter-item-"
			out << type
			out << "\">"
			out << label
			out << "</span>"
		}
	}

	def createRemoveFilterLink = { attrs ->

		def id = attrs["id"] // Note that text will use the text string for its id value.

		out << "<a class=\"filter-item filter-item-remove\" href=\"#\" onclick=\"removeFilter('"
		out << id
		out << "');\"><img alt=\"remove\" src=\""
		out << resource(dir:"images",file:"remove.png")
		out << "\" /></a>"

	}

	/**
	 * this function takes a search keyword instance to create a search link
	 */
	def createKeywordSearchLink = { attrs ->
		def keyword = attrs["keyword"]
		def function = attrs["jsfunction"]
		def controller = "search"
		def link =  createLink(controller:controller, action:"search", params:[id:keyword.id])
		if (function != null){
			out << "<a href=\""
			out<< link
			out << "\" onclick=\""
			out << function
			out << "('"
			out<< link
			out<< "');\">"
		} else {
			out << "<a href=\""
			out<< link
			out << "\" onclick=\"window.document.location='"
			out<< link
			out<<"';\">"
		}
		out << keyword.keyword
		out << "<img class=\"ExternalLink\" alt=\"search\" src=\""
		out << resource(dir:"images",file:"internal-link.gif")
		out << "\"/></a>"
	}

	def createPropertyTableRow = { attrs ->

		def width = attrs["width"]
		def label = attrs["label"]
		def value = attrs["value"]
		out << "<tr class=\"prop\">"
		out << "<td class=\"name\" width=\"" << width << "\" align=\"right\">"
		out << "<span class=\"Label\">" << label << ":</span>"
		out << "</td>"
		out << "<td class=\"value\">"
		if (value != null) {
			out << value
		} else {
			out << "&nbsp;"
		}
		out << "</td>"
		out << "</tr>"

	}

	def createNameValueRow = { attrs ->

		def name = message(code:attrs.name, default:attrs.name)
		def value = attrs?.value == null ? "" : attrs.value.toString().trim()

		if (value.length() > 0) {
			out << "<tr class=\"prop\">"
			out << "<td valign=\"top\" class=\"name\">" << name << ":</td>"
			out << "<td valign=\"top\" class=\"value\">" << value << "</td>"
			out << "</tr>"
		}

	}

	def createCustomFilterEmailLink = { attrs ->

		def customFilter = attrs.customFilter

		out << "<a href=\"mailto:?subject=Link to "
		out << customFilter?.name.replace("\"", "%22")
		out << "&body=The following is a link to the "
		out << customFilter?.name.replace("\"", "%22")
		out << " saved filter in the "
		out << grailsApplication.config.com.recomdata.searchtool.appTitle
		out << " application.%0A%0A"
		out << createLink(controller:'search', action:'searchCustomFilter', id:customFilter.id, absolute:true)
		out << "\" traget=\"_blank\" class=\"tiny\" style=\"text-decoration:underline;color:blue;font-size:11px;\">email</a>"

	}

	/**
	 * display wait image. This tag takes attributes 'divId' - the div id for you <div> and 'message' - the message to display in the wait image
	 * If the message is null by default 'Loading...' displays. The <div> is initally hidden so you will need to toggle the display style to show
	 */
	def waitIndicator = { attrs, body ->

		// tag attributes
		def divId = attrs.divId
		def message = attrs.message
		if(message==null) message = "Loading..."

		// render tag
		out << "<div id='"
		out << divId
		out << "' class='loading-indicator' style='display: none;'>"
		out << message
		out << "</div>"
	}

	/**
	 * useful for input screens to identify required fields with a red asterisk
	 */
	def requiredIndicator = { attrs, body ->
		out << "<span style='color: red;'>*</span>"
	}

	/**
	 * creates a <thead> element in a table with a highlighted header along with a toggle for a specified div id prefix defined in <tbody> element
	 * attribute label - header text to display
	 * attribute divPrefix - div id prefix to be used for the <tbody> div id with format '<prefix>_detail' which is toggled by js fct toggleDetail()
	 * attribute status - open or closed (indicates if arrow is down (closed) or up (open)
	 */
	def tableHeaderToggle = { attrs, body ->

		def label = attrs.label
		def divPrefix = attrs.divPrefix
		def status = attrs.status
		def colSpan = attrs.colSpan
		if(status==null) status = "closed"
		boolean bOpen = (status=="open")
		if(colSpan==null) colSpan = 1

		def openStyle = bOpen ? "visibility: hidden; display: none; vertical-align: middle;" : "'visibility: visible; display: block; vertical-align: middle;"
		def closedStyle = bOpen ? "visibility: visible; display: block; vertical-align: middle;" : "visibility: hidden; display: none; vertical-align: middle;"

		out << "<thead><tr><th colSpan='"+colSpan+"' class='tableToggle'>"
		out << "<a id='"+divPrefix+"_fopen' style='"+openStyle+"' "
		out << 		"onclick=\"javascript:toggleDetail('"+divPrefix+"');\">"+label+"&nbsp;<img alt='Open' src=\"${resource(dir:'images/skin',file:'sorted_desc.gif')}\" /></a> "
		out << "<a id='"+divPrefix+"_fclose' style='"+closedStyle+"' "
		out << 		"onclick=\"javascript:toggleDetail('"+divPrefix+"');\">"+label+"&nbsp;<img alt='Close' src=\"${resource(dir:'images/skin',file:'sorted_asc.gif')}\" /></a> "
		out << "</th></tr></thead>"
	}

}
