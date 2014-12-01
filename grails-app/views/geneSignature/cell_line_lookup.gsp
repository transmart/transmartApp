<html>

<body>
<div id="summary">
    <div id="SummaryHeader"><span class="SummaryHeader">Please Select A Cell Line</span></div>

    <table class="trborderbottom" width="100%">
        <thead>
        <tr>
            <th>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
            <th>Name</th>
            <th style="white-space: nowrap;">ATCC Number</th>
            <th>Species</th>
            <th>Disease</th>
        </tr>
        </thead>

        <tbody>
        <g:each in="${cellLines}" var="cl" status="i">
            <tr style="border-bottom:1px solid #CCCCCC;padding-botton:2px;">
                <td>&nbsp;<g:radio name="cellLine" value="${cl.id}"
                                   onclick="selectCellLine(${cl.id},'${cl.cellLineName + ' (' + cl.attcNumber + ')'}');"/></td>
                <td>${cl.cellLineName}</td>
                <td>${cl.attcNumber}</td>
                <td>${cl.species}</td>
                <td>${cl.disease}</td>
            </tr>
        </g:each>
        </tbody>
    </table>

</div>
</body>
</html>