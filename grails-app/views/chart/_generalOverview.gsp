<table width="100%">
    <tbody>
    <tr>
        <td colspan="2" align="center">
            <div class="analysistitle" id="analysis_title">
                Summary Statistics
            </div>
        </td>
    </tr>
    <tr>
        <td width="50%" align="center" valign="top">
            ${subsets[1].query}
        </td>
        <td align="center" valign="top">
            ${subsets[2].query}
        </td>
    </tr>
    <tr>
        <td colspan="2" align="center">
            <g:render template="patientCount" model="${[subsets: subsets]}"/>
        </td>
    </tr>
    <tr>
        <td colspan="2" align="center">
            <g:render template="valueComparison" model="${[subsets: subsets, prefix: 'age']}"/>
        </td>
    </tr>
    <tr>
        <g:render template="subsetCharts" model="${[subsets: subsets, prefix: 'sex']}"/>
    </tr>
    <tr>
        <g:render template="subsetCharts" model="${[subsets: subsets, prefix: 'race']}"/>
    </tr>
    </tbody>
</table>