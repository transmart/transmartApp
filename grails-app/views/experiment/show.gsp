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


<%@ page import="org.transmart.biomart.Experiment" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show Experiment</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">Experiment List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Experiment</g:link></span>
        </div>
        <div class="body">
            <h1>Show Experiment</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="experimentInstance.id" default="Id"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:experimentInstance, field:'id')}</td>
                            
                        </tr>
                    

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="experimentInstance.type" default="Type"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:experimentInstance, field:'type')}</td>
                            
                        </tr>
                    

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="experimentInstance.title" default="Title"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:experimentInstance, field:'title')}</td>
                            
                        </tr>
                    

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="experimentInstance.description" default="Description"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:experimentInstance, field:'description')}</td>
                            
                        </tr>
                    

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="experimentInstance.design" default="Design"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:experimentInstance, field:'design')}</td>
                            
                        </tr>
                    

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="experimentInstance.startDate" default="Start Date"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:experimentInstance, field:'startDate')}</td>
                            
                        </tr>
                    

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="experimentInstance.completionDate" default="Completion Date"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:experimentInstance, field:'completionDate')}</td>
                            
                        </tr>
                    

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="experimentInstance.primaryInvestigator" default="Primary Investigator"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:experimentInstance, field:'primaryInvestigator')}</td>
                            
                        </tr>
                    

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="experimentInstance.compounds" default="Compounds"/>:</td>
                            
                            <td  valign="top" style="text-align:left;" class="value">
                                <ul>
                                <g:each var="c" in="${experimentInstance.compounds}">
                                    <li><g:link controller="compound" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="experimentInstance.diseases" default="Diseases"/>:</td>
                            
                            <td  valign="top" style="text-align:left;" class="value">
                                <ul>
                                <g:each var="d" in="${experimentInstance.diseases}">
                                    <li><g:link controller="disease" action="show" id="${d.id}">${d?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="experimentInstance.files" default="Docs"/>:</td>
                            
                            <td  valign="top" style="text-align:left;" class="value">
                                <ul>
                                <g:each var="d" in="${experimentInstance.files}">
                                    <li><g:link controller="contentReference" action="show" id="${d.id}">${d?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="experimentInstance.uniqueIds" default="Unique Ids"/>:</td>
                            
                            <td  valign="top" style="text-align:left;" class="value">
                                <ul>
                                <g:each var="u" in="${experimentInstance.uniqueIds}">
                                    <li><g:link controller="bioData" action="show" id="${u.id}">${u?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="experimentInstance.uniqueId" default="Unique Id"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:experimentInstance, field:'uniqueId')}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${experimentInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
