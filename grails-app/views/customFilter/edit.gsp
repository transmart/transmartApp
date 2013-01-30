<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
 
-->


<%@ page import="org.transmart.searchapp.CustomFilter" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>${grailsApplication.config.com.recomdata.searchtool.appTitle}</title>
        <!-- ************************************** -->
	    <!-- This implements the Help functionality -->
	    <script type="text/javascript" src="${resource(dir:'js', file:'help/D2H_ctxt.js')}"></script>
	    <script language="javascript">
	    	helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
	    </script>
	    <sec:ifAnyGranted roles="ROLE_ADMIN">
			<script language="javascript">
				helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
			</script>
		</sec:ifAnyGranted>
		<!-- ************************************** -->  
    </head>
    <body>
		<g:render template="/layouts/commonheader" model="['app':'customfilters']" />
        <div class="nav">
            <span class="menuButton"><g:link class="list" action="list">Saved Filters</g:link></span>
            <%topicID="1022" %>
			<a HREF='JavaScript:D2H_ShowHelp(<%=topicID%>,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP )'>
				<img src="${resource(dir:'images',file:'help/helpbutton.jpg')}" alt="Help" border=0 width=18pt style="margin-top:1pt;margin-bottom:1pt;margin-right:18pt;float:right"/>
			</a>
        </div>
        <div style="padding: 20px 10px 10px 10px;">
            <h1 style="font-weight:bold; font-size:10pt; padding-bottom:5px;">Edit Filter</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${customFilterInstance}">
            <div class="errors">
                <g:renderErrors bean="${customFilterInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${customFilterInstance?.id}" />
                <input type="hidden" name="searchUserId" value="${customFilterInstance?.searchUserId}" />
                <div class="dialog">
                    <table>
                       <tr class="prop">
                           <td valign="top" class="name">
                               <label for="name">Name:</label>
                           </td>
                           <td valign="top" class="value ${hasErrors(bean:customFilterInstance,field:'name','errors')}">
								<g:textField size="80" name="name" value="${fieldValue(bean:customFilterInstance, field:'name')}" />
                           </td>
                       </tr> 
                   
                       <tr class="prop">
                           <td valign="top" class="name">
                               <label for="description">Description:</label>
                           </td>
                           <td valign="top" class="value ${hasErrors(bean:customFilterInstance,field:'description','errors')}">
                               <g:textArea rows="2" cols="61" name="description" value="${fieldValue(bean:customFilterInstance, field:'description')}" />
                           </td>
                       </tr> 
                       <tr class="prop">
                           <td valign="top" class="name">
                               <label for="privateFlag">Private Flag:</label>
                           </td>
                           <td valign="top" class="value ${hasErrors(bean:customFilterInstance,field:'privateFlag','errors')}">
								<g:checkBox name="privateFlag" value="${fieldValue(bean:customFilterInstance,field:'privateFlag') == 'Y'}" />
                           </td>
                       </tr> 
                       <tr class="prop">
                           <td valign="top" class="name"><label for="items">Summary:</label></td>
							<td valign="top">${customFilterInstance.summary}</td>
                    	</tr> 
                	</table>
                </div>
                <div class="buttons">
                    <g:actionSubmit class="save" value="Update"/>
                    <g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete"/>
                    <g:actionSubmit class="cancel" action="list" value="Cancel"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
