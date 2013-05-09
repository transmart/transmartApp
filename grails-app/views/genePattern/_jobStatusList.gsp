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

<body>
  <div id="divjobstatus" style="background:#fff;height:100%;padding:5px; font:12px tahoma, arial, helvetica, sans-serif;">  
    <h3>Step ${statusIndex + 1} of ${jobStatuses.size()}</h3>
    <table width="100%" style="background:#fff;">
        <g:each status="i" in="${jobStatuses}" var="jobStatus">
            <tr>
                <td>${jobStatus}
                    <g:if test="${i < statusIndex}">
                        <img src="${resource(dir:'images',file:'green_check.png')}"/>
                    </g:if>
                    <g:elseif test="${i == statusIndex}">
                        <img src="${resource(dir:'images', file:'loading-balls.gif')}"/>
                    </g:elseif>
                </td>
            </tr>
        </g:each>
    </table> 
  </div>
</body>


