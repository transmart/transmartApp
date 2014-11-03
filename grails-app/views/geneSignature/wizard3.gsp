<g:if test="${existingValues.'analysisMethodConceptCode.bioConceptCode' == 'OTHER'}">
    <div id="analysis_method_other_div" style="display: block;">
</g:if>
<g:else>
    <div id="analysis_method_other_div" style="display: none;">
</g:else>
<label>Please provide 'other' detail<g:requiredIndicator/>:</label><br><g:textField name="analysisMethodOther"
                                                                                    value="${gs.analysisMethodOther}"
                                                                                    size="100%" maxlength="255"/>
</div>
</td>
</tr>
</table>
</td>
</tr>
<tr class="prop">
    <td class="name">Multiple Testing Correction Employed?</td>
    <td class="value">
        <g:radioGroup name="multipleTestingCorrection" values="[1, 0]" labels="['Yes', 'No']"
                      value="${gs.multipleTestingCorrection}">
            ${it.radio}&nbsp;<g:message code="${it.label}"/>&nbsp;
        </g:radioGroup>
        &nbsp;<a href="javascript:clearMTC();">clear</a>
    </td>
</tr>
<tr class="prop">
    <td class="name">P-value Cutoff<g:requiredIndicator/></td>
    <td class="value">
        <g:select name="pValueCutoffConceptCode.id"
                  from="${wizard.pValCutoffs}"
                  value="${existingValues.'pValueCutoffConceptCode.id'}"
                  noSelection="['null': 'select p-value cutoff']"
                  optionValue="codeName"
                  optionKey="id"/>
    </td>
</tr>
</table>
<br>

<g:if test="${wizard.wizardType == 1 || wizard.wizardType == 2}">
    <table class="detail" style="width: 100%">
    <g:tableHeaderToggle label="Upload New File Only to Override Existing Items" divPrefix="file_info"/>
    <tbody id="file_info_detail" style="display: none;">
    <tr>
        <td colspan="2" style="font-weight: bold; font-size: 12px;">File Upload Information (tab delimited text only, no .xls Excel files):&nbsp;&nbsp;
            <a style="font-style:italic;" href="${resource(dir: 'images', file: 'gene_sig_samples.txt')}" target="_blank"><img
        alt="examples" src="${resource(dir: 'images', file: 'text.png')}"/>&nbsp;See Samples</a>
			</td>
		</tr>
</g:if>
<g:else>
    <p style="font-weight: bold;">File Upload Information (tab delimited text only, no .xls Excel files):&nbsp;&nbsp;
        <a style="font-style:italic;" href="${resource(dir: 'images', file: 'gene_sig_samples.txt')}"
           target="_blank"><img alt="examples" src="${resource(dir: 'images', file: 'text.png')}"/>&nbsp;See Samples</a>
    </p>
    <table class="detail" style="width: 100%">
        <tbody id="file_info_detail">
</g:else>
<tr class="prop">
    <td class="name">File Information<g:requiredIndicator/></td>
    <td class="value">
        <table>
            <tr>
                <td style="width:25%; border: none;">File schema:</td>
                <td style="border: none;">
                    <g:select name="fileSchema.id" from="${wizard.schemas}" value="${existingValues.'fileSchema.id'}"
                              optionValue="name" optionKey="id"/></td>
            </tr>
            <tr>
                <td style="width:25%; border: none;">Fold change metric:</td>
                <td style="border: none;">
                    <g:select name="foldChgMetricConceptCode.id"
                              from="${wizard.foldChgMetrics}"
                              value="${existingValues.'foldChgMetricConceptCode.id'}"
                              noSelection="['null': 'select metric indicator']"
                              optionValue="codeName"
                              optionKey="id"/>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr class="prop">
    <td class="name">Upload File<g:if
            test="${wizard.wizardType == 0}"><g:requiredIndicator/></g:if><br>(tab delimited text files only)</td>
    <td class="value"><input type="file" name="uploadFile"
                             <g:if test="${wizard.wizardType == 0}">value="${gs.uploadFile}"</g:if>
                             <g:else>value=""</g:else> size="100"/></td>
</tr>
</tbody>
</table>

<div class="buttons">
    <g:actionSubmit class="previous"
                    action="${(wizard.wizardType == 1 || wizard.wizardType == 2) ? 'edit2' : 'create2'}"
                    value="Meta-Data"/>
    <g:actionSubmit class="save" action="${(wizard.wizardType == 1) ? 'update' : 'save'}" value="Save"
                    onclick="return validate();"/>
    <g:actionSubmit class="cancel" action="refreshSummary" onclick="return confirm('Are you sure you want to exit?')"
                    value="Cancel"/>
</div>

</g:form>

</div>
</body>
</html>
