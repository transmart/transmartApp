<g:each var="sample" in="${samples}">

    <table class="biobankResults">
        <g:each var="sampleAttribute" in="${sample}">
            <g:if test="${sampleAttribute.key != 'count'}">
                <tr>
                    <th>
                        ${columnPrettyNameMapping[sampleAttribute.key]}
                    </th>
                    <td style='vertical-align:middle;border-color:black;border-width:1px;border-style:solid;padding: 3px;'>
                        ${sampleAttribute.value}
                    </td>
                </tr>
            </g:if>
        </g:each>
    </table>

    <br/>
    <br/>

</g:each>