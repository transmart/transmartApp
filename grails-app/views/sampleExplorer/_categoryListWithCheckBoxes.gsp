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
				<input type = "checkBox" name="${termName}_${term.key}" <g:if test="${JSONData[termName]?.contains(term.key)}">checked</g:if> onClick="updateFilterList('${term.key.replace("'","\\'")}',this.checked,'${termName}');" />
				<a href="#" class="categoryLinks" onClick="toggleMainCategorySelection('${term.key.replace("'","\\'")}','${termName}')">${term.key} (${term.value})</a>
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