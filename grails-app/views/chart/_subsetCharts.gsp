<table>
    <tr>
        <td>
            ${subset.value.sexPie}
            <g:render template="detailedStats" model="${[subset: subset, prefix: 'sex']}"/>
        </td>
    </tr>
    <tr>
        <td>
            ${subset.value.racePie}
            <g:render template="detailedStats" model="${[subset: subset, prefix: 'race']}"/>
        </td>
    </tr>
</table>