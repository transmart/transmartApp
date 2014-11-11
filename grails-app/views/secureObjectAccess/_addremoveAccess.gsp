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
