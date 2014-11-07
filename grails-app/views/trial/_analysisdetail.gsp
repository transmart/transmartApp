<table class="detail" style="width: 515px;">
    <tbody>
    <tr class="prop">
        <td valign="top" class="name" style="text-align: right">Title:</td>
        <td valign="top" class="value">${fieldValue(bean: analysis, field: 'shortDescription')}</td>
    </tr>
    <tr class="prop">
        <td valign="top" class="name" style="text-align: right">Analysis Description:</td>
        <td valign="top" class="value">${fieldValue(bean: analysis, field: 'longDescription')}</td>
    </tr>
    <g:if test='${"comparison".equals(analysis.analysisMethodCode)}'>
        <tr class="prop">
            <td valign="top" class="name" style="text-align: right">p-Value    Cut Off:</td>
            <td valign="top" class="value">${fieldValue(bean: analysis, field: 'pvalueCutoff')}</td>
        </tr>
        <tr class="prop">
            <td valign="top" class="name" style="text-align: right">Fold Change Cut Off:</td>
            <td valign="top" class="value">${fieldValue(bean: analysis, field: 'foldChangeCutoff')}</td>
        </tr>
    </g:if>
    <tr class="prop">
        <td valign="top" class="name" style="text-align: right">QA Criteria:</td>
        <td valign="top" class="value">${fieldValue(bean: analysis, field: 'qaCriteria')}</td>
    </tr>
    <tr class="prop">
        <td valign="top" class="name" style="text-align: right">Analysis Platform:</td>
        <td valign="top" class="value">${fieldValue(bean: analysis, field: 'analysisPlatform.platformName')}</td>
    </tr>
    <tr class="prop">
        <td valign="top" class="name" style="text-align: right">Method:</td>
        <td valign="top" class="value">${fieldValue(bean: analysis, field: 'analysisMethodCode')}</td>
    </tr>
    <tr class="prop">
        <td valign="top" class="name" style="text-align: right">Data type:</td>
        <td valign="top" class="value">${fieldValue(bean: analysis, field: 'assayDataType')}</td>
    </tr>
    </tbody>
</table>
