<td align="center" valign="top" width="50%" style="margin-top: 20px">
    <g:if test="${subsets[1].exists && subsets[1]."${prefix}Pie".size()}">
        ${subsets[1]."${prefix}Pie"}
        <g:render template="/chart/detailedStats" model="${[subset: subsets.entrySet().find {it.key == 1}, prefix: prefix]}"/>
    </g:if>
</td>
<td align="center" valign="top" style="margin-top: 20px">
    <g:if test="${subsets[2].exists && subsets[2]."${prefix}Pie".size()}">
        ${subsets[2]."${prefix}Pie"}
        <g:render template="/chart/detailedStats" model="${[subset: subsets.entrySet().find {it.key == 2}, prefix: prefix]}"/>
    </g:if>
</td>
