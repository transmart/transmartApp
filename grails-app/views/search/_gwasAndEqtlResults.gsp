<g:if test="${results.gwasResults && results.eqtlResults}">
	<div style="height: 300px; overflow: auto;">
		<tmpl:tableViewResults analysisData="${results.gwasResults.analysisData}" columnNames="${results.gwasResults.columnNames}" max="${results.gwasResults.max}" offset="${results.gwasResults.offset}" totalCount="${results.gwasResults.totalCount}" wasRegionFiltered="${results.gwasResults.wasRegionFiltered}" cutoff="${cutoff}" sortField="${sortField}" order="${order}" search="${search}"/>
	</div>
	<div style="height: 300px; overflow: auto;">
		<tmpl:tableViewResults analysisData="${results.eqtlResults.analysisData}" columnNames="${results.eqtlResults.columnNames}" max="${results.eqtlResults.max}" offset="${results.eqtlResults.offset}" totalCount="${results.eqtlResults.totalCount}" wasRegionFiltered="${results.eqtlResults.wasRegionFiltered}" cutoff="${cutoff}" sortField="${sortField}" order="${order}" search="${search}"/>
	</div>
</g:if>
<g:elseif test="${results.gwasResults}">
		<tmpl:tableViewResults analysisData="${results.gwasResults.analysisData}" columnNames="${results.gwasResults.columnNames}" max="${results.gwasResults.max}" offset="${results.gwasResults.offset}" totalCount="${results.gwasResults.totalCount}" wasRegionFiltered="${results.gwasResults.wasRegionFiltered}" cutoff="${cutoff}" sortField="${sortField}" order="${order}" search="${search}"/>
</g:elseif>
<g:elseif test="${results.eqtlResults}">
		<tmpl:tableViewResults analysisData="${results.eqtlResults.analysisData}" columnNames="${results.eqtlResults.columnNames}" max="${results.eqtlResults.max}" offset="${results.eqtlResults.offset}" totalCount="${results.eqtlResults.totalCount}" wasRegionFiltered="${results.eqtlResults.wasRegionFiltered}" cutoff="${cutoff}" sortField="${sortField}" order="${order}" search="${search}"/>
</g:elseif>
<g:else>
	No data was found for this search!
</g:else>