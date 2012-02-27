<div id="dqfilterresult">
<g:if test="${searchresult?.result==null || searchresult?.result.size()==0 }">
	<br><br><br>
	<table class="snoborder" width="100%">
		<tbody>
			<tr>
				<td width="100%" style="text-align: center; font-size: 14px; font-weight: bold">
					No results found
				</td>
			</tr>
		</tbody>
	</table>

</g:if>
<g:else>
	<div class="paginateButtons">
		<g:remotePaginate update="dqfilterresult" total="${searchresult?.documentCount}"
			controller="document" action="datasourceDocument"
			maxsteps="${grailsApplication.config.com.recomdata.search.paginate.maxsteps}"
			max="${grailsApplication.config.com.recomdata.search.paginate.max}" />
	</div>
	<table width="100%">
	<g:each in="${searchresult.result}" status="i" var="document">
		<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			<td>
				<table class="rnoborder" width="100%">
					<tr>
						<td colspan="2">
							<!-- ${document.getFileName().encodeAsHTML()} -->
							${createFileLink(document:document)}
						</td>
					</tr>
					<tr>
						<td width="90%">
							<b>Repository:</b>&nbsp;${document.getRepository()}
							&nbsp;|&nbsp;<b>Path:</b>&nbsp;
							<g:if test="${document.getFilePath().lastIndexOf('/') > -1}">						
								${document.getFilePath().substring(0, document.getFilePath().lastIndexOf("/"))}
							</g:if>
							<g:else>
								-
							</g:else>
						</td>
						<td width="10%">
							<b>Score:</b>&nbsp;<g:formatNumber number="${document.getScore()}" format="0.00000" />
						<td>
					</tr>
					<tr>
						<td colspan="2">
							${document.getFullText()}
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td>
				<div id="${rifdivid}">
				</div>
			</td>
		</tr>
	</g:each>
	</table>
</g:else>
</div>