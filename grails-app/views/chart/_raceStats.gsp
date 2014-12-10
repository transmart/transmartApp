<img src="${subset.value.racePie}" width="300" height="200" border="0">
<g:if test="${subset.value.raceData?.size()}">
<table class="analysis">
    <tbody>
    <tr>
        <th>Category
        </th>
        <th>Subset ${subset.key} (n)
        </th>
        <th>Subset ${subset.key} (%n)
        </th>
    </tr>
    <g:set var="total" value="${subset.value.raceData.values().sum()}" />
    <g:each in="${subset.value.raceData.entrySet()}" var="point">
        <tr>
            <td>${point.key}</td>
            <td>${point.value}</td>
            <td>${point.value * 100 / total} %</td>
        </tr>
    </g:each>
    </tbody>
</table>
</g:if>