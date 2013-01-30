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
<g:setProvider library="prototype"/>

            <g:if test="${flash.message}">
                <tr><td><div class="message">${flash.message}</div></td></tr>
            </g:if>
                        <td> <g:select class="addremoveselect" name="groupstoremove" from="${groupswithuser}" size="15" multiple="yes" optionKey="id" optionValue="name" /></td>
						  <td class="addremovebuttonholder">
							<button class="ltarrowbutton" onclick="${remoteFunction(action:'addUserToGroups',update:[success:'groups', failure:''],  params:'$(\'groupstoadd\').serialize()+\'&searchtext=\'+document.getElementById(\'searchtext\').value+\'&id=\'+document.getElementById(\'currentprincipalid\').value')}; return false;">&LT;&LT;Add</button><br>
							<button class="ltarrowbutton" onclick="${remoteFunction(action:'removeUserFromGroups',update:[success:'groups', failure:''],  params:'$(\'groupstoremove\').serialize()+\'&searchtext=\'+document.getElementById(\'searchtext\').value+\'&id=\'+document.getElementById(\'currentprincipalid\').value')}; return false;">Remove&GT;&GT;</button>
							</td>
							<td>
							<div id="addfrombox">
							<g:select class="addremoveselect" width="100px" size="15" name="groupstoadd" from="${groupswithoutuser}" multiple="yes" optionKey="id" optionValue="name" ></g:select></td>
							</div>
							</td>

