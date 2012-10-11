
	<g:if test="${wasRegionFiltered}">
		<i>These results have been filtered according to gene/chromosome region criteria.</i><br/><br/>
	</g:if>
	<div id="analysis_results_table_${analysisId}_length" class="dataTables_length">
		<label>Show 
			<g:select size="1" from="${['10':'10','25':'25','50':'50','100':'100']}" optionKey="${{it.key}}" optionValue="${{it.value}}" id="analysis_results_table_${analysisId}_length" onclick="loadAnalysisResultsGrid(${analysisId}, jQuery(this).val(), 0, jQuery('#analysis_results_table_${analysisId}_cutoff').val())" value="${max}"/> entries
		</label>	
	</div>
	<div class="dataTables_filter" id="analysis_results_table_${analysisId}_filter">
		<label>P-value cutoff: 
			<input value="${cutoff}" type="text" id="analysis_results_table_${analysisId}_cutoff" aria-controls="analysis_results_table_${analysisId}">
			<span class="linkbutton" onclick="loadAnalysisResultsGrid(${analysisId}, ${max}, 0, jQuery('#analysis_results_table_${analysisId}_cutoff').val())">OK</span>
		</label>
	</div>
	<table id="analysis_results_table_${analysisId}" class="dataTable" aria-describedby="analysis_results_table_${analysisId}_info">
		<thead>
			<tr role="row">
				<g:each in="${columnNames}" var="col">
					<th class="sorting" tabindex="0" rowspan="1" colspan="1" style="width: 100px; ">${col.sTitle}</th>
				</g:each>
			</tr>
		</thead>
		<tbody role="alert" aria-live="polite" aria-relevant="all">
			
				<g:each in="${analysisData}" var="row" status="i">
				
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<g:each in="${row}" var="data">
						<td class="">${data}</td>
					</g:each>
					</tr>
				</g:each>
			
		</tbody>
	</table>						
	<div class="dataTables_info" id="analysis_results_table_${analysisId}_info">Showing ${offset+1} to ${Math.min(totalCount,offset+max)} of ${totalCount} entries</div>
	<div class="dataTables_paginate paging_two_button" id="analysis_results_table_${analysisId}_paginate">
		<g:if test="${offset > 0}">
			<a class="paginate_enabled_previous" tabindex="0" role="button" id="analysis_results_table_${analysisId}_previous" aria-controls="analysis_results_table_${analysisId}" onclick="loadAnalysisResultsGrid(${analysisId}, ${max}, ${offset-max}, jQuery('#analysis_results_table_${analysisId}_cutoff').val())">Previous</a>
		</g:if>
		<g:else>
			<a class="paginate_disabled_previous" tabindex="0" role="button" id="analysis_results_table_${analysisId}_previous" aria-controls="analysis_results_table_${analysisId}">Previous</a>
		</g:else>
		
		<g:if test="${offset + max < totalCount}">
			<a class="paginate_enabled_next" tabindex="0" role="button" id="analysis_results_table_${analysisId}_next" aria-controls="analysis_results_table_${analysisId}" onclick="loadAnalysisResultsGrid(${analysisId}, ${max}, ${offset+max}, jQuery('#analysis_results_table_${analysisId}_cutoff').val())">Next</a>
		</g:if>
		<g:else>
			<a class="paginate_disabled_next" tabindex="0" role="button" id="analysis_results_table_${analysisId}_next" aria-controls="analysis_results_table_${analysisId}">Next</a>
		</g:else>
	</div>