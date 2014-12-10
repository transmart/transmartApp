<table>
    <tr>
        <td>
            <img src="${subset.value.sexPie}" width="300" height="200" border="0">
            <g:render template="detailedStats" model="${[subset: subset, prefix: 'sex']}"/>
        </td>
    </tr>
    <tr>
        <td>
            <img src="${subset.value.racePie}" width="300" height="200" border="0">
            <g:render template="detailedStats" model="${[subset: subset, prefix: 'race']}"/>
        </td>
    </tr>
</table>