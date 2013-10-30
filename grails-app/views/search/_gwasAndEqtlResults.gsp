<g:if test="${results.gwasResults && results.eqtlResults}">
	<div class="dataTables_filter" style="padding-bottom: 10px">
		<label>P-value cutoff: 
			<input value="${cutoff}" type="text" style="width: 50px" id="table_results_cutoff">
		</label>
		<label>Search: 
			<input value="${search}" type="text" style="width: 50px" id="table_results_search">
			<span class="linkbutton" onclick="loadTableResultsGrid({cutoff: jQuery('#table_results_cutoff').val(), search: jQuery('#table_results_search').val()})">OK</span>
		</label>
	</div>
	<div style="height: 300px; overflow: auto; border-top: 1px solid black; margin-bottom: 10px">
		<tmpl:tableViewResults type="GWAS" analysisData="${results.gwasResults.analysisData}" columnNames="${results.gwasResults.columnNames}" max="${results.gwasResults.max}" offset="${results.gwasResults.offset}" totalCount="${results.gwasResults.totalCount}" wasRegionFiltered="${results.gwasResults.wasRegionFiltered}" cutoff="${cutoff}" sortField="${sortField}" order="${order}" search="${search}"/>
	</div>
	<div style="height: 300px; overflow: auto; border-top: 1px solid black; ">
		<tmpl:tableViewResults type="eQTL" analysisData="${results.eqtlResults.analysisData}" columnNames="${results.eqtlResults.columnNames}" max="${results.eqtlResults.max}" offset="${results.eqtlResults.offset}" totalCount="${results.eqtlResults.totalCount}" wasRegionFiltered="${results.eqtlResults.wasRegionFiltered}" cutoff="${cutoff}" sortField="${sortField}" order="${order}" search="${search}"/>
	</div>
</g:if>
<g:elseif test="${results.gwasResults}">
		<tmpl:tableViewResults type="GWAS" showSearch="true" analysisData="${results.gwasResults.analysisData}" columnNames="${results.gwasResults.columnNames}" max="${results.gwasResults.max}" offset="${results.gwasResults.offset}" totalCount="${results.gwasResults.totalCount}" wasRegionFiltered="${results.gwasResults.wasRegionFiltered}" cutoff="${cutoff}" sortField="${sortField}" order="${order}" search="${search}"/>
</g:elseif>
<g:elseif test="${results.eqtlResults}">
		<tmpl:tableViewResults type="eQTL" showSearch="true" analysisData="${results.eqtlResults.analysisData}" columnNames="${results.eqtlResults.columnNames}" max="${results.eqtlResults.max}" offset="${results.eqtlResults.offset}" totalCount="${results.eqtlResults.totalCount}" wasRegionFiltered="${results.eqtlResults.wasRegionFiltered}" cutoff="${cutoff}" sortField="${sortField}" order="${order}" search="${search}"/>
</g:elseif>
<g:else>
	No data was found for this search!
</g:else>