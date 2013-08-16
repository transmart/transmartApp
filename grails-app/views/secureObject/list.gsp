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
        <title>Study List</title>
    </head>
    <body>
        <div class="body">
            <h1>Study List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                            <g:sortableColumn property="displayName" title="Study Name" />
                   	        <g:sortableColumn property="dataType" title="Data Type:" />
                            <g:sortableColumn property="bioDataId" title="Bio Data Id" />
                   	        <g:sortableColumn property="bioDataUniqueId" title="Bio Data Unique Id" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${secureObjectInstanceList}" status="i" var="secureObjectInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${secureObjectInstance.id}">${fieldValue(bean:secureObjectInstance, field:'id')}</g:link></td>

                            <td>${fieldValue(bean:secureObjectInstance, field:'displayName')}</td>

                            <td>${fieldValue(bean:secureObjectInstance, field:'dataType')}</td>
                        
                            <td>${fieldValue(bean:secureObjectInstance, field:'bioDataId')}</td>
                        
                            <td>${fieldValue(bean:secureObjectInstance, field:'bioDataUniqueId')}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${secureObjectInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
