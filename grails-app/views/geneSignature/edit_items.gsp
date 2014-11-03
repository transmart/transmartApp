<g:if test="${errorFlag}">
    <g:if test="${gs?.fileSchema.id != 3}">
        <td style="text-align: center;"><g:textField name="biomarker_${n}" value="${params.get('biomarker_' + n)}"
                                                     maxlength="25"/></td>
    </g:if>
    <g:if test="${gs?.fileSchema.id == 3}">
        <td style="text-align: center;"><g:textField name="probeset_${n}" value="${params.get('probeset_' + n)}"
                                                     maxlength="25"/></td>
    </g:if>
    <g:if test="${gs.foldChgMetricConceptCode?.bioConceptCode != 'NOT_USED'}"><td><g:textField name="foldChgMetric_${n}"
                                                                                               value="${params.get('foldChgMetric_' + n)}"
                                                                                               maxlength="20"/></td></g:if>
</g:if>
<g:else>
    <g:if test="${gs?.fileSchema.id != 3}">
        <td><g:textField name="biomarker_${n}" maxlength="25"/></td>
    </g:if>
    <g:if test="${gs?.fileSchema.id == 3}">
        <td><g:textField name="probeset_${n}" maxlength="25"/></td>
    </g:if>
    <g:if test="${gs.foldChgMetricConceptCode?.bioConceptCode != 'NOT_USED'}"><td><g:textField name="foldChgMetric_${n}"
                                                                                               maxlength="20"/></td></g:if>
</g:else>

<td style="text-align: center;"><img alt="remove item" onclick="javascript:removeNewItem(${n});"
                                     src="${resource(dir: 'images', file: 'remove.png')}"/></td>
</tr>
</g:while>
</tbody>
</table>

<div class="buttons">
    <g:actionSubmit class="save" action="addItems" value="Add Items"/>
    <g:actionSubmit class="delete" action="deleteItems" value="Delete Checked"
                    onclick="return confirm('Are you sure you want to delete these items?')"/>
    <g:actionSubmit class="cancel" action="refreshSummary" onclick="return confirm('Are you sure you want to exit?')"
                    value="Cancel"/>
</div>

</g:form>
</div>
</body>
</html>

