<table width="100%" style="padding-top: 30px; padding-bottom: 30px; table-layout: fixed;">
    <tbody>
    <tr>
        <td colspan="2" align="center">
            <div class="analysistitle" id="analysis_title">
                Summary Statistics
            </div>
        </td>
    </tr>
    <tr>
        <td width="50%" align="center" valign="top" style="padding: 10px">
            <br/><br/>
            ${subsets[1].query}
            <br/><br/>
        </td>
        <td width="50%" align="center" valign="top" style="padding: 10px">
            <br/><br/>
            ${subsets[2].query}
            <br/><br/>
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