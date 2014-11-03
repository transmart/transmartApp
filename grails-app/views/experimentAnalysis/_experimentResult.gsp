<table style="background-color: #fffff;" width="100%">
    <tbody>
    <g:set var="counter" value="${1}"/>
    <g:each in="${ear.pagedAnalysisList}" status="i"
            var="analysisResult">
        <g:if test="${!teaDisplay || analysisResult.bSignificantTEA}">
            <g:set var="counter" value="${counter + 1}"/>
            <g:render template="/trial/teaAnalysisSummary"
                      model="[analysisResult: analysisResult, counter: counter, showTrial: true]"/>
        </g:if>
    </g:each>
    </tbody>
</table>
</div>
</g:else>

</g:else>
</div>
