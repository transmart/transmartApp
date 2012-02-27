<table class="categoryListCheckbox">
	<TBODY>
	
	<tr>
		<td style="font-weight:bold;">
			By ${termName.replace("_"," ")}
		</td>
	</tr>	
	</TBODY>
	
	<TBODY>
	<g:each var="term" in="${termList}" status="iterator" >
		<g:if test="${(iterator == grailsApplication.config.com.recomdata.solr.maxLinksDisplayed)}">
			</TBODY>
			<TBODY id="tbodyMoreLink${termName}">
				<tr>
					<td>
						<a href="#" onClick="toggleMoreResults(document.getElementById('tbodyMoreLink${termName}'),document.getElementById('tbodyLessLink${termName}'),document.getElementById('tbodyHiddenResults${termName}'))">More [+]</a>
					</td>
				</tr>
			</TBODY>
			<TBODY id="tbodyHiddenResults${termName}" style="display:none;">
		</g:if>
		<tr>
			<td>
				<g:checkBox name="${termName}_${term.key}" value="${JSONData[termName]?.contains(term.key)}" onClick="updateFilterList('${term.key.toString()}',this.checked,'${termName}');"/>
				<a href="#" class="categoryLinks" onClick="toggleMainCategorySelection('${term.key}','${termName}')">${term.key} (${term.value})</a>
			</td>
		</tr>		
	</g:each>
	</TBODY>	
	
	<TBODY id="tbodyLessLink${termName}" style="display:none;">
		<tr>
			<td>
				<a href="#" onClick="toggleMoreResults(document.getElementById('tbodyMoreLink${termName}'),document.getElementById('tbodyLessLink${termName}'),document.getElementById('tbodyHiddenResults${termName}'))">Less [-]</a>
			</td>
		</tr>
	</TBODY>	
</table>