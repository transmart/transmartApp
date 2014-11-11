<%--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
 
--%>
<g:if test="${flash.message}">
    <td colspan=3><div class="message">${flash.message}</div></td>
</g:if>
<g:else>
    <td>
        <g:select class="addremoveselect" name="sobjectstoremove"
                  from="${secureObjectAccessList}" size="15"
                  multiple="yes" optionKey="id" optionValue="objectAccessName"/>
    </td>

    <td class="addremovebuttonholder">
        <button class="ltarrowbutton"
                onclick="${remoteFunction(action: 'addSecObjectsToPrincipal',
                                          update: [success: 'permissions', failure: ''],
                                          params: 'addremoveAccess_add_data(this)')};
                return false;">
            &lt;&lt;Add
        </button>
        <br>
        <button class="ltarrowbutton"
                onclick="${remoteFunction(action: 'removeSecObjectsFromPrincipal',
                                          update: [success: 'permissions', failure: ''],
                                          params: 'addremoveAccess_remove_data(this)')};
                return false;">
            Remove&gt;&gt;
        </button>
    </td>
    <td>
        <div id="addfrombox">
            <g:select class="addremoveselect"
                      width="100px" size="15"
                      name="sobjectstoadd"
                      from="${objectswithoutaccess}"
                      multiple="yes" optionKey="id" optionValue="displayName"/>
        </div>
    </td>
</g:else>

<r:script>
    (function () {
        'use strict';

        window.addremoveAccess_add_data = function (el) {
            return Recom.rc.serializeFormElements.call(el,
                    ['sobjectstoadd', 'searchtext', 'currentprincipalid', 'accesslevelid'])
        }

        window.addremoveAccess_remove_data = function (el) {
            return Recom.rc.serializeFormElements.call(el,
                    ['sobjectstoremove', 'searchtext', 'currentprincipalid', 'accesslevelid'])
        }
    })()
</r:script>
