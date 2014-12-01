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
        <td width="50%" align="center">
            ${subsets[1].query}
        </td>
        <td align="center">
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
            <g:render template="ageComparison" model="${[subsets: subsets, graphs: graphs]}"/>
        </td>
    </tr>
    <tr>
        %{-- This is hardcoded badness. Multiple (>2) cohort selection should work on that --}%
        <td width="50%" align="center">
            <g:render template="subsetCharts" model="${[subset: subsets.entrySet().find {it.key == 1}, graphs: graphs]}"/>
        </td>
        <td width="50%" align="center">
            <g:render template="subsetCharts" model="${[subset: subsets.entrySet().find {it.key == 2}, graphs: graphs]}"/>
        </td>
    </tr>
    </tbody>
</table>