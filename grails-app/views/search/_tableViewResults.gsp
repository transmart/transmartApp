
	<div class='vis-toolBar' >
		<div id="btnResultsExport" class='vis-toolbar-item'><a href="${createLink([controller:'search', action:'getTableResults', params:[export: true]])}"><img alt="" src="${resource(dir:'images',file:'internal-link.gif')}" /> Export as CSV</a></div>
	</div>
	<g:if test="${wasRegionFiltered}">
		<i>These results have been filtered according to gene/chromosome region criteria.</i><br/><br/>
	</g:if>
	<div id="table_results_length_div" class="dataTables_length">
		<label>Show 
			<g:select size="1" from="${['100':'100','150':'150','200':'200','500':'500']}" optionKey="${{it.key}}" optionValue="${{it.value}}" id="table_results_length" onclick="loadTableResultsGrid({max: jQuery(this).val()})" value="${max}"/> entries
		</label>	
	</div>
	<div class="dataTables_filter">
		<label>P-value cutoff: 
			<input value="${cutoff}" type="text" id="table_results_cutoff">
		</label>
		<label>Search: 
			<input value="${search}" type="text" id="table_results_search">
			<span class="linkbutton" onclick="loadTableResultsGrid({cutoff: jQuery('#table_results_cutoff').val(), search: jQuery('#table_results_search').val()})">OK</span>
		</label>
	</div>
	<table id="table_results" class="dataTable">
		<thead>
			<tr role="row">
				<g:each in="${columnNames}" var="col">
					<g:if test="${col.sortField}">
						<%-- If this is sorted, give link to asc/desc (opposite current order) --%>
						<g:if test="${col.sortField.equals(sortField)}">
							<g:if test="${order.equals('desc')}">
								<th "class="sorting" tabindex="0" rowspan="1" colspan="1" style="width: 100px; cursor: pointer;" onclick="loadTableResultsGrid({sortField: '${col.sortField}', order: 'asc'})">
									${col.sTitle} <img src="${resource([file:'desc.gif', dir:'images'])}"/>
								</th>
							</g:if>
							<g:else>
								<th "class="sorting" tabindex="0" rowspan="1" colspan="1" style="width: 100px; cursor: pointer;" onclick="loadTableResultsGrid({sortField: '${col.sortField}', order: 'desc'})">
									${col.sTitle} <img src="${resource([file:'asc.gif', dir:'images'])}"/>
								</th>
							</g:else>
						</g:if>
						<%-- Otherwise just provide asc link --%>
						<g:else>
							<th "class="sorting" tabindex="0" rowspan="1" colspan="1" style="width: 100px; cursor: pointer;" onclick="loadTableResultsGrid({sortField: '${col.sortField}', order: 'asc'})">
								${col.sTitle}
							</th>
						</g:else>
					</g:if>
					<g:else>
						<th class="sorting" tabindex="0" rowspan="1" colspan="1" style="width: 100px; ">${col.sTitle}</th>
					</g:else>
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
	<div class="dataTables_info" id="table_results_info">
		<g:if test="${totalCount > 0}">
			Showing ${Math.min(offset+1, totalCount)} to ${Math.min(totalCount,offset+max)} of ${totalCount} entries
		</g:if>
		<g:else>
			No entries to display
		</g:else>
	</div>
	<div class="dataTables_paginate paging_two_button">
		<g:if test="${offset > 0}">
			<a class="paginate_enabled_previous" tabindex="0" role="button" id="table_results_previous" aria-controls="results_table" onclick="loadTableResultsGrid({offset: ${offset-max}})">Previous</a>
		</g:if>
		<g:else>
			<a class="paginate_disabled_previous" tabindex="0" role="button" id="table_results_previous" aria-controls="results_table">Previous</a>
		</g:else>
		
		<g:if test="${offset + max < totalCount}">
			<a class="paginate_enabled_next" tabindex="0" role="button" id="table_results_next" aria-controls="results_table" onclick="loadTableResultsGrid({offset: ${offset+max}})">Next</a>
		</g:if>
		<g:else>
			<a class="paginate_disabled_next" tabindex="0" role="button" id="table_results_next" aria-controls="results_table">Next</a>
		</g:else>
	</div>
