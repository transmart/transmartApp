<g:if test="${flash.message}">
    <tr><td><div class="message">${flash.message}</div></td></tr>
</g:if>
<td><g:select class="addremoveselect" name="groupstoremove"
              from="${secureObjectAccessList}" size="15" multiple="yes"
              optionKey="id" optionValue="principalAccessName"/></td>
<td class="addremovebuttonholder">
    <button class="ltarrowbutton"
            onclick="${remoteFunction(action: 'addPrincipalToAccessList',
                                              update: [success: 'groups', failure: ''],
                                              params: 'addremovePrincipal_add_data(this)')};
            return false;">&LT;&LT;Add</button><br>
    <button class="ltarrowbutton"
            onclick="${remoteFunction(action: 'removePrincipalFromAccessList',
                                              update: [success: 'groups', failure: ''],
                                              params: 'addRemovePrincipal_remove_data(this)')};
            return false;">Remove&GT;&GT;</button>
</td>
<td>
    <div id="addfrombox">
    <g:select class="addremoveselect" width="100px" size="15" name="groupstoadd"
              from="${userwithoutaccess}" multiple="yes" optionKey="id"
              optionValue="principalNameWithType"></g:select></td>
</div>
</td>

<r:script>
    (function () {
        'use strict';

        window.addremovePrincipal_add_data = function (el) {
            return Recom.rc.serializeFormElements.call(el,
                    ['groupstoadd', 'searchtext', 'secureobjectid', 'accesslevelid'])
        }

        window.addRemovePrincipal_remove_data = function (el) {
            return Recom.rc.serializeFormElements.call(el,
                    ['groupstoremove', 'searchtext', 'secureobjectid', 'accesslevelid'])
        }
    })()
</r:script>
