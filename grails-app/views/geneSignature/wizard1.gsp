<p style="font-weight: bold;">Page 1: Definition:</p>
<table class="detail">
    <tr class="prop">
        <td class="name">Signature/List Name<g:requiredIndicator/></td>
        <td class="value"><g:textField name="name" value="${gs.name}" size="100%" maxlength="100"/></td>
    </tr>
    <tr>
    <tr class="prop">
        <td class="name">Description</td>
        <td class="value"><g:textArea name="description" value="${gs.description}" rows="6" cols="85"/></td>
    </tr>
    <g:if test="${wizard.wizardType == 1}">
        <tr class="prop">
            <td class="name">Public?</td>
            <td class="value">
                <g:radioGroup name="publicFlag" values="[true, false]" labels="['Yes', 'No']" value="${gs.publicFlag}">
                    ${it.radio}&nbsp;<g:message code="${it.label}"/>&nbsp;
                </g:radioGroup>
            </td>
        </tr>
    </g:if>
    <tr>
        <g:if test="${wizard.wizardType == 1}">
            <td style="font-weight: bold; font-style: italic;"
                colspan=2>Note, the creator of this signature was '${gs.createdByAuthUser?.userRealName}' on ${gs.dateCreated}</td>
        </g:if>
        <g:else>
            <td style="font-weight: bold; font-style: italic;" colspan=2>
                Note, the creator of this signature will be '<sec:loggedInUserInfo
                        field="userRealName"/>' at the current system time
            </td>
        </g:else>
    </tr>
</table>

<div class="buttons">
    <g:actionSubmit class="next" action="${(wizard.wizardType == 1 || wizard.wizardType == 2) ? 'edit2' : 'create2'}"
                    value="Meta-Data" onclick="return validate();"/>
    <g:actionSubmit class="cancel" action="refreshSummary" onclick="return confirm('Are you sure you want to exit?')"
                    value="Cancel"/>
</div>

<br>
</g:form>
</div>
</body>
</html>
