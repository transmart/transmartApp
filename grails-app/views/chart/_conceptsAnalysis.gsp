<table width="100%" style="margin-bottom: 30px">
    <tbody>
        <g:each in="${concepts}" var="concept">
        <tr>
            <td align="center">
            <g:if test="${concept.value}">
                <hr style="margin-bottom: 30px"/>
                <div class="analysistitle">Analysis of ${concept.value.commons.conceptName}</div>
                <g:if test="${concept.value?.commons?.omics_params != null}">
                    <div class="analysissubtitle">${concept.value?.commons?.conceptKey ?: ""}</div>
                </g:if>
                <div style="margin-top: -15px; padding-bottom: 10px;">
                    ${concept.value?.commons?.testmessage}<br/>
                    <g:if test="${concept.value?.commons.pvalue != null}">
                        <g:if test="${concept.value?.commons.tstat != null && concept.value?.commons.tstat != Double.NaN}">
                            With a <i>p-value of ${concept.value?.commons.pvalue}</i> for a <i>T-stat at ${concept.value?.commons.tstat}</i>
                        </g:if>
                        <g:elseif test="${concept.value?.commons.chisquare != null && concept.value?.commons.chisquare != Double.NaN}">
                            With a <i>p-value of ${concept.value?.commons.pvalue}</i> for a <i>χ² at ${concept.value?.commons.chisquare}</i>
                        </g:elseif>
                        <g:else>
                            Variable arithmetically undefined <i>(NaN)</i>
                        </g:else>
                    </g:if>
                </div>
                <g:render template="/chart/${concept.value.commons.type}Comparison" model="${[subsets: concept.value]}"/>
            </g:if>
            </td>
        </tr>
        </g:each>
    </tbody>
</table>