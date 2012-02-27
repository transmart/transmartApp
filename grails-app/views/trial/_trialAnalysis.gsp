<!--  TEA presentation or single gene view -->
<g:if test="${trialresult.bioMarkerCt>1}"> 
	<!--  display significant analyses in open block -->
	<table style="background-color: #fffff;" width="100%">
		<g:tableHeaderToggle label="(${trialresult.analysisCount-trialresult.inSignificantAnalCount}) Significant TEA Analyses" divPrefix="significant_block" status="open" />
	
		<tbody id="significant_block_detail" style="display: block;">
		<g:set var="counter" value="${1}" />
		<g:each in="${trialresult.analysisResultList}" status="i" var="analysisResult">
			<g:if test="${analysisResult.bSignificantTEA}">
				<g:set var="counter" value="${counter+1}" />
				<g:render template="/trial/teaAnalysisSummary" model="[analysisResult: analysisResult, counter: counter, showTrial: showTrial]" />
			</g:if>
		</g:each>
		</tbody>
	</table>
	
	<!--  display insignificant analyses initially closed -->
	<g:if test="${trialresult.inSignificantAnalCount>0}">
	<table style="background-color: #fffff;" width="100%">
		<g:tableHeaderToggle label="(${trialresult.insigAnalResultList.size()}) Insignificant TEA Analyses" divPrefix="insignificant_block" />
	
		<tbody id="insignificant_block_detail" style="display: none;">	
		<g:each in="${trialresult.insigAnalResultList}" status="i" var="analysisResult">
			<g:render template="/trial/teaAnalysisSummary" model="[analysisResult: analysisResult, counter: i, showTrial: showTrial]" />
		</g:each>
		</tbody>
	</table>
	</g:if>
</g:if>
<g:else>
	<!--  display analyses without TEA -->
	<table style="background-color: #fffff;" width="100%">
		<tbody>	
		<g:each in="${trialresult.analysisResultList}" status="i" var="analysisResult">
			<g:render template="/trial/teaAnalysisSummary" model="[analysisResult: analysisResult, counter: i, showTrial: showTrial]" />
		</g:each>
		</tbody>
	</table>
</g:else>
