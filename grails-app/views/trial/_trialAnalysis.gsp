<table style="background-color: #fffff;" width="100%">
    <tbody>
    <g:each in="${trialresult.analysisResultList}" status="i" var="analysisResult">
        <g:render template="/trial/teaAnalysisSummary"
                  model="[analysisResult: analysisResult, counter: i, showTrial: showTrial]"/>
    </g:each>
    </tbody>
</table>
</g:else>
