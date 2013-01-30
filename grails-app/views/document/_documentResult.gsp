<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
 
-->

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