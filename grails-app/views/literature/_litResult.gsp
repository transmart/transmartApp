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
<g:setProvider library="prototype"/>
<!-- loading image -->
<g:waitIndicator divId="jubresult_loading" />

<div id="jubresult" style="display: block;">
<g:if test="${searchresult?.literatureCount() > 0}">
	<table style="padding:10px 5px 0px 5px;">
		<tr>
			<td>
				<table>
					<tr><td style="line-height: 13px;"><b>Jubilant Oncology (${searchresult?.litJubOncCount()})</b></td></tr>
					<tr><td style="padding:2px 0px 0px 20px;">Alterations (${searchresult?.litJubOncAltCount})</td></tr>
					<tr><td style="padding:2px 0px 0px 20px;">Inhibitors (${searchresult?.litJubOncInhCount})</td></tr>
					<tr><td style="padding:2px 0px 0px 20px;">Interactions (${searchresult?.litJubOncIntCount})</td></tr>
				</table>
			</td>
			<td>
				<table>
					<tr><td><b>Jubilant Asthma (${searchresult?.litJubAsthmaCount()})</b></td></tr>
					<tr><td style="padding:2px 0px 0px 20px;">Alterations (${searchresult?.litJubAsthmaAltCount})</td></tr>
					<tr><td style="padding:2px 0px 0px 20px;">Interactions (${searchresult?.litJubAsthmaIntCount})</td></tr>
					<tr><td style="padding:2px 0px 0px 20px;">Protein Effects (${searchresult?.litJubAsthmaPECount})</td></tr>
				</table>
			</td>
 			<td width="50%">&nbsp;</td>
		</tr>
	</table>
	<div class="paginateButtons" style="width: 100%;">
		<span style="font-size:12px;color:#000000;">Results for </span>
		<select class="jubselect" name="resultType" id="resultType" style="width:240px;"
				onChange="${remoteFunction(action:'datasourceJubilant', 
										   before:'toggleVisible(\'jubresult_loading\'); toggleVisible(\'jubresult\');', 
								           onComplete:'toggleVisible(\'jubresult_loading\'); toggleVisible(\'jubresult\');',
								           update:'jubresult', params:'\'datatype=\'+this.value')}" >
		<g:if test="${searchresult?.litJubOncAltCount > 0}">
			<option value="JUBILANT_ONCOLOGY_ALTERATION" ${searchresult.resultType == null || searchresult.resultType == 'JUBILANT_ONCOLOGY_ALTERATION' ? "selected" : ""}>Jubilant Oncology Alterations</option>
		</g:if>
		<g:if test="${searchresult?.litJubOncInhCount > 0}">
			<option value="JUBILANT_ONCOLOGY_INHIBITOR" ${searchresult.resultType == 'JUBILANT_ONCOLOGY_INHIBITOR' ? "selected" : ""}>Jubilant Oncology Inhibitors</option>
		</g:if>
		<g:if test="${searchresult?.litJubOncIntCount > 0}">
			<option value="JUBILANT_ONCOLOGY_INTERACTION" ${searchresult.resultType == 'JUBILANT_ONCOLOGY_INTERACTION' ? "selected" : ""}>Jubilant Oncology Interactions</option>
		</g:if>
		<g:if test="${searchresult?.litJubAsthmaAltCount > 0}">
			<option value="JUBILANT_ASTHMA_ALTERATION" ${searchresult.resultType == 'JUBILANT_ASTHMA_ALTERATION' ? "selected" : ""}>Jubilant Asthma Alterations</option>
		</g:if>
		<g:if test="${searchresult?.litJubAsthmaInhCount > 0}">
			<option value="JUBILANT_ASTHMA_INHIBITOR" ${searchresult.resultType == 'JUBILANT_ASTHMA_INHIBITOR' ? "selected" : ""}>Jubilant Asthma Inhibitors</option>
		</g:if>
		<g:if test="${searchresult?.litJubAsthmaIntCount > 0}">
			<option value="JUBILANT_ASTHMA_INTERACTION" ${searchresult.resultType == 'JUBILANT_ASTHMA_INTERACTION' ? "selected" : ""}>Jubilant Asthma Interactions</option>
		</g:if>
		<g:if test="${searchresult?.litJubAsthmaPECount > 0}">
			<option value="JUBILANT_ASTHMA_PROTEIN_EFFECT" ${searchresult.resultType == 'JUBILANT_ASTHMA_PROTEIN_EFFECT' ? "selected" : ""}>Jubilant Asthma Protein Effects</option>
		</g:if>
		</select>
		&nbsp;&nbsp;
		<g:remotePaginate update="jubresult" total="${searchresult.resultCount}"
			controller="literature" action="datasourceJubilant"
			params="[datatype:searchresult.resultType]"
			maxsteps="${grailsApplication.config.com.recomdata.search.paginate.maxsteps}"
			max="${grailsApplication.config.com.recomdata.search.paginate.max}" />
	</div>
	<table width="100%">
<g:if test="${searchresult.result?.size() > 0}">
	<g:each in="${searchresult.result}" status="i" var="jubData">
		<g:set var="jubrifdivid" value="jubrif_${jubData.id}" />
		<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			<td colspan="2">
				<table class="rnoborder" width="100%">
					<tr>
						<td align="left">
							<div style="float:none;">
								<a onclick="toggleDetail('${jubrifdivid}')">
									<div id="${jubrifdivid}_fclose" style="visibility:hidden; display:none;width:16px;">
										<img src="${resource(dir:'images',file:'folder-minus.gif')}" style="vertical-align:middle;"/>
									</div>
									<div id="${jubrifdivid}_fopen" style="display:inline;width:16px;">
										<img src="${resource(dir:'images',file:'folder-plus.gif')}" style="vertical-align:middle;"/>
									</div>
								<g:if test="${jubData.reference.referenceTitle?.length() > 0}">
									<g:set var="descr" value="${jubData.reference.referenceTitle}"/>
									<b>${(descr?.length() > 180) ? descr.substring(0, 180)+"..." : descr}</b>
								</g:if>
									<!-- TODO? geneProteinVariant: Inhibitor Studies -->
								</a>
							</div>
						</td>
						<td width="15%" align="right">
							<!-- TODO: Add link to associated articles -->
							<g:if test="${jubData.files.size() > 0}">
								<g:each in="${jubData.files}" status="fileCount" var="file">
									<g:if test="${fileCount > 0}">, </g:if>
									<g:createFileLink content="${file.content}" displayLabel="Reference" />
								</g:each>
							</g:if>
							<g:else>
								<g:if test="${jubData.reference.referenceId != null && jubData.reference.referenceId != ''}">
									<g:if test="${jubData.reference.referenceId?.startsWith('NCT')}">
										<a style="border: none;"
											href="http://www.clinicaltrials.gov/ct2/show/${jubData.reference.referenceId}"
											target="_blank" title="Clinical Trials"><img alt="CT"
											src="${resource(dir:'images',file:'clinicaltrials.png')}" />&nbsp;Reference</a>
									</g:if>
									<g:elseif test="${jubData.reference.referenceId?.startsWith('ISRCTN')}">
										<a style="border: none;"
											href="http://www.controlled-trials.com/${jubData.reference.referenceId}"
											target="_blank" title="Controlled Trials"><img alt="ISRCTN"
											src="${resource(dir:'images',file:'controlledtrials.png')}" />&nbsp;Reference</a>
									</g:elseif>
									<g:else>
										<a style="border: none;"
											href="http://www.ncbi.nlm.nih.gov/pubmed/${jubData.reference.referenceId}"
											target="_blank" title="PubMed ID"><img alt="PubMed"
											src="${resource(dir:'images',file:'ncbi.png')}" />&nbsp;Reference</a>
									</g:else>
								</g:if>
							</g:else>
						</td>
					</tr>
					<tr>
						<td colspan="2">
							<span style="white-space:nowrap">
							<g:if test="${jubData.reference.component?.length() > 0}">
								<b>Variant: ${jubData.reference.component}</b>&nbsp;&nbsp;|
							</g:if>
							<g:if test="${jubData.reference.geneId?.length() > 0}">
								&nbsp;<b class="filter-item filter-item-gene">Gene:</b> ${createFilterDetailsLink(altId: jubData.reference.geneId, label:jubData.reference.component, type:"gene")}&nbsp;&nbsp;|
							</g:if>
							<g:if test="${jubData.reference.moleculeType?.length() > 0}">
								&nbsp;<b>Molecule:</b> ${jubData.reference.moleculeType}&nbsp;&nbsp;|
							</g:if>
							<g:if test="${jubData.reference.disease?.length() > 0}">
								&nbsp;<b class="filter-item filter-item-disease">Disease:</b> ${createFilterDetailsLink(altId:jubData.reference.disease,label:jubData.reference.disease,type:"disease")}&nbsp;&nbsp;|
							</g:if>
							<g:if test="${jubData.reference.diseaseSite?.length() > 0}">
								&nbsp;<b>Disease Site:</b> ${jubData.reference.diseaseSite}
							</g:if>
							</span>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td colspan=3>
				<div id="${jubrifdivid}_detail" class="gtb1" style="display:none;">
					<g:render template="litDetail" model="[jubData:jubData, resultType:searchresult.resultType]"/>
				</div>
			</td>
		</tr>
	</g:each>
</g:if>
<g:else>
		<tr>
			<td style="padding: 10px 0px 10px 0px;font-weight:bold;text-align:center;">No Results Found</td>
		</tr>
</g:else>
	</table>
</g:if>
<g:else>
	<br><br><br>
	<table class="snoborder" width="100%">
		<tbody>
			<tr>
				<td width="100%" style="text-align: center; font-size: 14px; font-weight: bold">No results found</td>
			</tr>
		</tbody>
	</table>
</g:else>
</div>
