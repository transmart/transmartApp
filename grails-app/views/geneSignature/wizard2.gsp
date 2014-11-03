<g:if test="${existingValues.'experimentTypeConceptCode.bioConceptCode' == 'IN_VIVO_ANIMAL' || existingValues.'experimentTypeConceptCode.bioConceptCode' == 'IN_VIVO_HUMAN'}">
    <tr id="in_vivo_id" style="display: inline;">
</g:if>
<g:else>
    <tr id="in_vivo_id" style="display: none;">
</g:else>
<td style="border: none;">
    <label>For 'in vivo', describe model<g:requiredIndicator/>:</label><br><g:textField name="experimentTypeInVivoDescr"
                                                                                        value="${gs.experimentTypeInVivoDescr}"
                                                                                        size="100%" maxlength="255"/>
</td>
</tr>
<tr><td style="border: none;"><label>If applicable, ATCC designation:</label><br><g:textField
        name="experimentTypeATCCRef" value="${gs.experimentTypeATCCRef}" size="100%" maxlength="255"/></td></tr>
</table>
</td>
</tr>
</table>

<div class="buttons">
    <g:actionSubmit class="previous"
                    action="${(wizard.wizardType == 1 || wizard.wizardType == 2) ? 'edit1' : 'create1'}"
                    value="Definition"/>
    <g:actionSubmit class="next" action="${(wizard.wizardType == 1 || wizard.wizardType == 2) ? 'edit3' : 'create3'}"
                    value="Next" onclick="return validate();"/>
    <g:actionSubmit class="cancel" action="refreshSummary" onclick="return confirm('Are you sure you want to exit?')"
                    value="Cancel"/>
</div>

</g:form>
</div>
</body>
</html>
