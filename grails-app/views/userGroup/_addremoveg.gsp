<g:if test="${flash.message}">
    <tr><td><div class="message">${flash.message}</div></td></tr>
</g:if>
<td><g:select class="addremoveselect" name="groupstoremove" from="${groupswithuser}" size="15" multiple="yes"
              optionKey="id" optionValue="name"/></td>
<td class="addremovebuttonholder">
    <button class="ltarrowbutton"
            onclick="${remoteFunction(action:'addUserToGroups', update:[success:'groups', failure:''],
                     params: 'addremove_buildAddUser()')};
            return false;">&LT;&LT;Add</button><br>
    <button class="ltarrowbutton"
            onclick="${remoteFunction(action:'removeUserFromGroups', update:[success:'groups', failure:''],
                     params: 'addremove_buildRemoveUser()')};
            return false;">Remove&GT;&GT;</button>
</td>
<td>
    <div id="addfrombox">
        <g:select class="addremoveselect" width="100px" size="15" name="groupstoadd" from="${groupswithoutuser}"
                  multiple="yes" optionKey="id" optionValue="name"/>
    </div>
</td>

<r:script>
    (function () {
        'use strict';

        window.addremove_buildAddUser = function (el) {
            return Recom.rc.serializeFormElements.call(el,
                    ['currentprincipalid', 'searchtext', 'groupstoadd'])
        }

        window.addremove_buildRemoveUser = function (el) {
            return Recom.rc.serializeFormElements.call(el,
                    ['currentprincipalid', 'searchtext', 'groupstoremove'])
        }
    })()
</r:script>
