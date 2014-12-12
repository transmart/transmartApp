<g:set var="prefix" value="${prefix ?: 'concept'}"/>
<g:if test="${subset.value."${prefix}Data"?.size()}">
<table class="analysis" style="margin: auto; margin-top: 20px">
    <tbody>
    <tr>
        <th>Category
        </th>
        <th>Subset ${subset.key} (n)
        </th>
        <th>Subset ${subset.key} (%n)
        </th>
    </tr>
    <g:set var="total" value="${subset.value."${prefix}Data".values().sum()}" />
    <g:each in="${subset.value."${prefix}Data".entrySet()}" var="point">
        <tr>
            <td>${point.key}</td>
            <td>${point.value}</td>
            <g:if test="${total != 0}">
                <td>${(point.value * 100 / total).doubleValue().round(2)} %</td>
            </g:if>
            <g:else>
                <td>-</td>
            </g:else>
        </tr>
    </g:each>
    </tbody>
</table>
</g:if>