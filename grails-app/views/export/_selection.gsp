<g:overlayPopup title="Export Cart" divContainerId="${params.eleId}">

    <g:form action="sendFiles">

        <div class="list">
            <h2 class="rdc-h2">${folder.title}</h2>
            <table class="baseGrid">
                <tbody>
                <g:each in="${layout}" status="i" var="layoutRow">
                    <tr>
                        <td valign="top" align="right" class="name">${layoutRow.displayName}</td>
                        <td valign="top" align="left" class="value">
                            <g:if test="${fieldValue(bean: folder, field: layoutRow.column).length() < 100}">
                                <g:textField size="100" name="${layoutRow.column}"
                                             value="${fieldValue(bean: folder, field: layoutRow.column)}"/>
                            </g:if>
                            <g:else>
                                <g:textArea size="100" cols="74" rows="5" name="${layoutRow.column}"
                                            value="${fieldValue(bean: folder, field: layoutRow.column)}"/>
                            </g:else>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>

        <div></div>

        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" action="export" id="Export"
                                                 value="${message(code: 'default.button.update.label', default: 'Export')}"/></span>
            <span class="button"><g:actionSubmit class="cancel" action="clear" id="Clear"
                                                 value="${message(code: 'default.button.update.label', default: 'Clear Cart')}"/></span>
            <span class="button"><g:actionSubmit class="list" action="list" id="cancel" value="Cancel"
                                                 onclick="return confirm('Are you sure?')"/></span>
        </div>
    </g:form>
</g:overlayPopup>
