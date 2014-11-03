<g:if test="${ar.teaScore == null}">
    <br>
    <g:each in="${ar.getAnalysisValueSubList()}" status="ai" var="analysisvalue">
        <g:if test="${ai > 0}">,</g:if>
        <g:createFilterDetailsLink id="${analysisvalue.bioMarker?.id}" label="${analysisvalue.bioMarker?.name}"
                                   type="gene"/>
        <g:if test="${analysisvalue.analysisData?.foldChangeRatio != null}">(Fold Change:${analysisvalue.analysisData?.foldChangeRatio})
            <g:if test="${analysisvalue.analysisData?.foldChangeRatio > 0}">
                <img alt="signature up" src="${resource(dir: 'images', file: 'up_arrow.PNG')}"/>
            </g:if>
            <g:else>
                <img alt="signature down" src="${resource(dir: 'images', file: 'down_arrow.PNG')}"/>
            </g:else>
        </g:if>
        <g:if test="${analysisvalue.analysisData.rvalue != null}">(R Value:${analysisvalue.analysisData.rvalue})</g:if>
        <g:if test="${analysisvalue.analysisData.rhoValue != null}">(Rho Value:${analysisvalue.analysisData.rhoValue})</g:if>
        <g:if test="${analysisvalue.analysisData.resultsValue != null}">(Result:${analysisvalue.analysisData.resultsValue})</g:if>
        <g:if test="${analysisvalue.analysisData.numericValue != null}">&nbsp;&nbsp;&nbsp;<B>${analysisvalue.analysisData.numericValueCode}:</B>&nbsp;${analysisvalue.analysisData.numericValue}</g:if>
    </g:each>
</g:if>

<g:set var="genes" value="${ar.getGeneNames()}"/>

</td>
</tr>
</g:if>
</table>
</td>
</tr>
<tr>
    <td colspan="3">
        <div id="${bmDivId}_detail" class="gtb1" style="display: none;"><g:render
                template="/trial/analysisBiomarkerDetail" model="[analysisresult: ar]"/></div>
    </td>
</tr>
</table>
</td>
</tr>

