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

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/> 
        <meta name="layout" content="admin" />
        <title>AccessLog List</title>
        <script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery-1.7.1.min.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery-ui-1.8.17.custom.min.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'prototype.js')}"></script>
		<link rel="stylesheet" type="text/css" href="${resource(dir:'css/jQueryUI/smoothness', file:'jquery-ui-1.8.17.custom.css')}">
    </head>
    <body>
    <div class="body">
        <g:form name="form">
        <table style="width:500px"><tr><td>
        Start Date<input id="startdate" name="startdate" type="text" value="${startdate}"></td>
		<td>End Date<input id="enddate" name="enddate" type="text" value="${enddate}"></td>
		<td><br><g:actionSubmit class="filter" value="Filter" action="list" />&nbsp&nbsp&nbsp&nbsp<g:actionSubmit class="filter" value="Export to Excel" action="export" />
        </td></tr></table>
        </g:form>
            <h1>AccessLog List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
                        <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <th><g:message code="accessLogInstance.accesstime" default="Access Time"/></th>
                   	             	   
                        
                   	        <th><g:message code="accessLogInstance.username" default="User"/></th>
                   	   
                        
                   	        <th><g:message code="accessLogInstance.event" default="Event"/></th>
                   	   
                        
                   	        <th><g:message code="accessLogInstance.eventmessage" default="Event Message"/></th>
                   	             
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${accessLogInstanceList}" status="i" var="accessLogInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                        	<td>${fieldValue(bean:accessLogInstance, field:'accesstime')}</td>
                            <td>${fieldValue(bean:accessLogInstance, field:'username')}</td>
                        
                            <td>${fieldValue(bean:accessLogInstance, field:'event')}</td>
                        
                            <td>${fieldValue(bean:accessLogInstance, field:'eventmessage')}</td>                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>                                  
            </div>
            <div class="paginateButtons">
                <g:paginate 
                	total="${totalcount}"
                	maxsteps="${grailsApplication.config.com.recomdata.search.paginate.maxsteps}"
                	max="${grailsApplication.config.com.recomdata.search.paginate.max}" />
            </div>      
        </div> 
            <script>
            	jQuery(function() {
                	jQuery("#startdate").datepicker();
                });
                jQuery(function() {
                    jQuery("#enddate").datepicker();
                });
            </script>    
	</body>
</html>
