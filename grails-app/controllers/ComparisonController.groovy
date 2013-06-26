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
  


import grails.converters.*

class ComparisonController {

    def index = { }

    def i2b2HelperService

    def getQueryDefinition= {
    		 String qid = request.getParameter("qid");
    		 String q=i2b2HelperService.getQueryDefinitionXMLFromQID(qid);
    		 log.debug(q);
    		 PrintWriter pw=new PrintWriter(response.getOutputStream());
    		 pw.write(q);
    		 pw.flush();
    }

    def save = {    		 
            def qid1 = request.getParameter("result_instance_id1")
            def qid2 = request.getParameter("result_instance_id2")
    		def s = new i2b2.Comparison()
			
			try	{
				s.queryResultId1=Integer.parseInt(qid1)
			} catch(NumberFormatException nfe)	{
				s.queryResultId1 = -1
			}
			
			try	{
				s.queryResultId2=Integer.parseInt(qid2)
			} catch(NumberFormatException nfe)	{
				s.queryResultId2= -1
			}

    		boolean success=s.save()

    		def link = new StringBuilder()
			link.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ID: <input type=\"text\" size=\"10\" value=\"${s.id}\">")
			link.append("<br/><br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"mailto:?subject=Link to ")
    		link.append("Saved comparison ID=${s.id}")
    		link.append("&body=The following is a link to the saved comparison in tranSMART.  Please, note that you need to be logged into tranSMART prior to using this link.%0A%0A")    		
    		link.append(createLink(controller:'datasetExplorer', action:'index', id:s.id, absolute:true))
    		link.append("\" target=\"_blank\" class=\"tiny\" style=\"text-decoration:underline;color:blue;font-size:11px;\">Email this comparison</a><br /><br />")
    		def result=[success: success, id: s.id, link: link ]
    		log.trace(result as JSON)
    		render result as JSON
    }
}
