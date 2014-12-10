<table width="100%">
    <tbody>
        <g:each in="${concepts}" var="concept">
        <tr>
            <td align="center">
            <g:if test="${concept.value}">
                <hr/>
                <div class="analysistitle">Analysis of ${concept.value.commons.conceptName}</div>
                <g:render template="${concept.value.commons.type}Comparison" model="${[subsets: concept.value]}"/>
            </g:if>
            </td>
        </tr>
        </g:each>
    </tbody>
</table>