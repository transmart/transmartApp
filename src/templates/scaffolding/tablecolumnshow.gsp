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

<% import org.codehaus.groovy.grails.orm.hibernate.support.ClosureEventTriggeringInterceptor as Events %>
<%=packageName%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show ${className}</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="\${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">${className} List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New ${className}</g:link></span>
        </div>
        <div class="body">
            <h1>Show ${className}</h1>
            <g:if test="\${flash.message}">
            <div class="message">\${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    <%
                        excludedProps = ['version',
                                           Events.ONLOAD_EVENT,
                                           Events.BEFORE_DELETE_EVENT,
                                           Events.BEFORE_INSERT_EVENT,
                                           Events.BEFORE_UPDATE_EVENT]
                        props = domainClass.properties.findAll { !excludedProps.contains(it.name) }
                        Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))
                        props.each { p -> %>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="${propertyName}.${p.name}" default="${p.naturalName}"/>:</td>
                            <% if(p.isEnum()) { %>
                            <td valign="top" class="value">\${${propertyName}?.${p.name}?.encodeAsHTML()}</td>
                            <% } else if(p.oneToMany) { %>
                            <td  valign="top" style="text-align:left;" class="value">
                                <ul>
                                <g:each var="${p.name[0]}" in="\${${propertyName}.${p.name}}">
                                    <li><g:link controller="${p.referencedDomainClass?.propertyName}" action="show" id="\${${p.name[0]}.id}">\${${p.name[0]}?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            <%  } else if(p.manyToOne || p.oneToOne) { %>
                            <td valign="top" class="value"><g:link controller="${p.referencedDomainClass?.propertyName}" action="show" id="\${${propertyName}?.${p.name}?.id}">\${${propertyName}?.${p.name}?.encodeAsHTML()}</g:link></td>
                            <%  } else  { %>
                            <td valign="top" class="value">\${fieldValue(bean:${propertyName}, field:'${p.name}')}</td>
                            <%  } %>
                        </tr>
                    <%  } %>
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="\${${propertyName}?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
