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
        <div style="padding: 20px 10px 10px 10px;">
            <%topicID="1017" %>
			<a HREF='JavaScript:D2H_ShowHelp(<%=topicID%>,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP )'>
				<img src="${resource(dir:'images',file:'help/helpbutton.jpg')}" alt="Help" border=0 width=18pt style="margin-top:1pt;margin-bottom:1pt;margin-right:18pt;float:right"/>
			</a>
            <h1 style="font-weight:bold; font-size:10pt; padding-bottom:5px; color: #006DBA;">
            <g:if test="${params.lastFilterID != null}">
    			<g:link controller="search" action="searchCustomFilter" id="${params.lastFilterID}" style="color: #006DBA;">Search</g:link>&nbsp;> Saved Filters
            </g:if>
            <g:else>
            	<g:link controller="search" style="color: #006DBA;">Search</g:link>&nbsp;> Saved Filters
            </g:else>
            </h1>
            <g:if test="${flash.message}"><div class="message">${flash.message}</div></g:if>
            <div>
                <table>
					<tr>
						<th>&nbsp;</th>
					</tr>
                   	<g:each in="${customFilterInstanceList}" status="i" var="customFilterInstance">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
						<td>
							<table class="rnoborder"">
								<tr>
									<td width="100%">
										<g:link style="color:#000000;" controller="search" action="searchCustomFilter" id="${customFilterInstance.id}">
											${fieldValue(bean:customFilterInstance, field:'name')}
										</g:link>
									</td>
									<td>
										<nobr>
										<g:link controller="search" action="searchCustomFilter" id="${customFilterInstance.id}"
											class="tiny" style="text-decoration:underline;color:blue;font-size:11px;">select</g:link>
										<g:link action="edit" id="${customFilterInstance.id}"
											class="tiny" style="text-decoration:underline;color:blue;font-size:11px;">edit</g:link>
										<g:link action="delete" onclick="return confirm('Are you sure?');" id="${customFilterInstance.id}"
											class="tiny" style="text-decoration:underline;color:blue;font-size:11px;">delete</g:link>
										</nobr>
									</td>
								</tr>
								<tr>
		                            <td colspan="2">${fieldValue(bean:customFilterInstance, field:'description')}</td>
		                        </tr>
								<tr>
		                            <td colspan="2">${customFilterInstance.summary}</td>
		                        </tr>
								<tr>
		                            <td colspan="2"><b>Shortcut:</b>
									<g:if test="${customFilterInstance.privateFlag != 'Y'}">
										${createLink(controller:'search', action:'searchCustomFilter', absolute:true, id:customFilterInstance.id)}
										${createCustomFilterEmailLink(customFilter:customFilterInstance)}
									</g:if>
									<g:else>
										Private
									</g:else>
									</td>
		                        </tr>
							</table>
						</td>
					</tr>
                	</g:each>
             	</table>
            </div>
            <br />
            <a href="${createLink(controller:'search', action:'index')}" style="text-decoration:underline;color:blue;font-size:12px;">Return to Search</a>
        </div>
    </body>
</html>
