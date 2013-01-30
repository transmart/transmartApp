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

<g:form controller="document" action="filterDocument" name="documentfilters">
<table class="jubfilter" style="width:500px">
	<tr>
		<th colspan=2 style="align:right">
			<span class="button">
				<g:actionSubmit class="search" action="filterDocument" value="Filter Results" onclick="return validateDocumentFilters();"/>&nbsp;
			</span>
		</th>
	</tr>
	<tr>
		<td colspan=2 style="border-right:0px solid #ccc">
			<table class="jubfiltersection">
				<tr>
					<td style="vertical-align:top; border-left:0; font-weight:bold; padding-top:7px;" width="110" nowrap="nowrap">
						Repository
					</td>
					<td style="vertical-align:top; line-height:normal;">
						<g:each in="${repositories.keySet()}" status ="i" var="repository">
							<g:checkBox name="repository_${repository.toLowerCase().replace(' ', '_')}"
								value="${filter.repositories.get(repository)}" />
							${repository}<br />
						</g:each>
					</td>
				</tr>
				<tr>
					<td style="vertical-align:top; border-left:0; font-weight:bold; padding-top:7px;" width="110" nowrap="nowrap">
						Path
					</td>
					<td style="line-height:normal;">
						<g:textField name="path" value="${filter.path}" style="width:250px;" />
					</td>
				</tr>
				<tr>
					<td style="vertical-align:top; border-left:0; font-weight:bold; padding-top:7px;" width="110" nowrap="nowrap">
						Document Type
					</td>
					<td style="vertical-align:top; line-height:normal;">
						<g:checkBox name="type_excel" value="${filter.type_excel}" />Excel<br />
						<g:checkBox name="type_html" value="${filter.type_html}" />HTML<br />
						<g:checkBox name="type_pdf" value="${filter.type_pdf}" />PDF<br />
						<g:checkBox name="type_powerpoint" value="${filter.type_powerpoint}" />PowerPoint<br />
						<g:checkBox name="type_text" value="${filter.type_text}" />Text<br />
						<g:checkBox name="type_word" value="${filter.type_word}" />Word<br />
						<g:checkBox name="type_other" value="${filter.type_other}" />Other
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>       
</g:form>