<td align="center" valign="top" width="50%">
    ${subsets[1]."${prefix}Pie"}
    <g:render template="detailedStats" model="${[subset: subsets.entrySet().find {it.key == 1}, prefix: prefix]}"/>
</td>
<td align="center" valign="top">
    <g:if test="${subsets[2].exists}">
        ${subsets[2]."${prefix}Pie"}
        <g:render template="detailedStats" model="${[subset: subsets.entrySet().find {it.key == 2}, prefix: prefix]}"/>
    </g:if>
</td>