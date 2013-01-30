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

<table class="detail" style="width: 100%">
	<g:tableHeaderToggle label="General Infomation" divPrefix="${gs.id}_general" status="open" colSpan="2" />

	<tbody id="${gs.id}_general_detail" style="display: block;">
		<tr class="prop">
			<td valign="top" class="name">Name:</td>
			<td valign="top" class="value">${gs.name}&nbsp;
				<g:link class="normal" action="downloadExcel" id="${gs.id}">
						<img alt="download signature" src="${resource(dir:'images',file:'Excel-16.gif')}" />Excel</g:link>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name">Description:</td>
			<td valign="top" class="value">${gs.description}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name">Public Status:</td>
			<td valign="top" class="value">${gs.publicFlag ? 'Public':'Private'}</td>
		</tr>		
		<tr class="prop">
			<td valign="top" class="name">Author:</td>
			<td valign="top" class="value">${gs.createdByAuthUser?.userRealName}</td>
		</tr>		
		<tr class="prop">
			<td valign="top" class="name">Create Date:</td>
			<td valign="top" class="value">${gs.dateCreated}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name">Modified By:</td>
			<td valign="top" class="value">${gs.modifiedByAuthUser?.userRealName}</td>
		</tr>		
		<tr class="prop">
			<td valign="top" class="name">Modified Date:</td>
			<td valign="top" class="value"><g:if test="${gs.modifiedByAuthUser!=null}">${gs.lastUpdated}</g:if></td>
		</tr>		
	</tbody>
</table>

<table class="detail" style="width: 100%">
	<g:tableHeaderToggle label="Meta-Data" divPrefix="${gs.id}_meta_data" colSpan="2" />

	<tbody id="${gs.id}_meta_data_detail" style="display: none;">	
		<tr class="prop">
			<td valign="top" class="name">Source of list:</td>
			<td valign="top" class="value">
				<g:if test="${gs.sourceConceptCode?.id==1}">${gs.sourceOther}</g:if>
				<g:else> ${gs.sourceConceptCode?.codeName}</g:else>
			</td>
		</tr>		
		<tr class="prop">
			<td valign="top" class="name">Owner of data:</td>
			<td valign="top" class="value">${gs.ownerConceptCode?.codeName}</td>
		</tr>				
		<tr class="prop">
			<td class="name">Stimulus:</td>
			<td class="value">
				<table>				
					<tr>
						<td style="border: none; width; 33%;">Description:</td><td style="border: none;">${gs.stimulusDescription}</td>
					</tr>
					<tr>						
						<td style="border: none; width; 33%;">Dose, units, and time:</td><td style="border: none;">${gs.stimulusDosing}</td>
					</tr>
				</table>
			</td>
		</tr>							
		<tr class="prop">
			<td class="name">Treatment:</td>
			<td class="value">
				<table>			
					<tr>
						<td style="border: none; width; 33%;">Drug treatment used in assay:&nbsp;${gs.treatmentDescription}</td>
					</tr>
					<tr>						
						<td style="border: none; width; 33%;">Dose, units, and time:&nbsp;${gs.treatmentDosing}</td>
					</tr>
					<tr>
						<td style="border: none; width; 33%;">Compound:&nbsp;
							<g:if test="${gs.treatmentCompound!=null}">${gs.treatmentCompound?.codeName + ' [' + gs.treatmentCompound?.genericName + ' / ' + gs.treatmentCompound?.brandName +']'}</g:if>
						</td>
					</tr>
					<tr>						
						<td style="border: none; width; 33%;">Protocol Number:&nbsp;${gs.treatmentProtocolNumber}</td>
					</tr>			
				</table>
			</td>
		</tr>
		<tr class="prop">
			<td class="name">PMIDs (comma separated):</td>
			<td class="value">${gs.pmIds}</td>
		</tr>
		<tr class="prop">
			<td class="name">Species:</td>
			<td class="value">			
			<table>
				<tr>
					<td style="border: none; width: 50%">${gs.speciesConceptCode?.codeName}</td>
				<tr>
				<g:if test="${gs.speciesMouseSrcConceptCode!=null}">
				<tr><td style="border: none; width: 50%">Mouse Source:&nbsp;${gs.speciesMouseSrcConceptCode?.codeName}</td></tr>					
				</g:if>
				<g:if test="${gs.speciesMouseDetail!=null}">
				<tr><td style="border: none; width: 50%">'knockout/transgenic' or 'other' mouse strain:&nbsp;${gs.speciesMouseDetail}</td></tr>
				</g:if>
				</tr>	
			</table>			
			</td>
		</tr>				
		<tr class="prop">
			<td class="name">Technology Platform:</td>
			<td class="value"><g:if test="${gs.techPlatform!=null}">${gs.techPlatform?.vendor + ' - ' + gs.techPlatform?.array + ' [' + gs.techPlatform?.accession + ']'}</g:if>
			</td>  
		</tr>		
		<tr class="prop">
			<td class="name">Tissue Type:</td>
			<td class="value">${gs.tissueTypeConceptCode?.codeName}</td>
		</tr>					
		<tr class="prop">
			<td class="name">Experiment Type:</td>
			<td class="value">
				<table>				
				<tr>
					<td style="border: none; width: 50%">${gs.experimentTypeConceptCode?.codeName}</td>
				<tr>

				<!--  toggle established cell line -->
				<g:if test="${gs.experimentTypeConceptCode?.bioConceptCode=='ESTABLISHED'}">			   				  							
				<tr>						
					<td style="border: none;">Cell line:&nbsp;${gs.experimentTypeCellLine?.cellLineName}</td>
				</tr>
				</g:if>
				
				<!--  toggle in vivo model accordingly -->
				<g:if test="${gs.experimentTypeConceptCode?.bioConceptCode=='IN_VIVO_ANIMAL' || gs.experimentTypeConceptCode?.bioConceptCode=='IN_VIVO_HUMAN'}">				   				  								
				<tr>						
					<td style="border: none;">'in vivo' model:&nbsp;${gs.experimentTypeInVivoDescr}</td>
				</tr>
				</g:if>
				<tr><td style="border: none;">ATCC designation:&nbsp;${gs.experimentTypeATCCRef}</td></tr>										
				</table>
			</td>
		</tr>					
	</tbody>
</table>			

<table class="detail" style="width: 100%">
	<g:tableHeaderToggle label="Analysis Meta-Data" divPrefix="${gs.id}_analysis" colSpan="2" />

	<tbody id="${gs.id}_analysis_detail" style="display: none;">	
		<tr class="prop">
			<td class="name">Analysis Performed By:</td>
			<td class="value">${gs.analystName}</td>	
		</tr>
		<tr class="prop">
			<td class="name">Normalization Method:</td>
			<td class="value">
				<g:if test="${gs.normMethodConceptCode?.id==1}">${gs.normMethodOther}</g:if>
				<g:else>${gs.normMethodConceptCode?.codeName}</g:else>
			</td>	
		</tr>
		<tr class="prop">
			<td class="name">Analytic Category:</td>
			<td class="value">				
				<g:if test="${gs.analyticCatConceptCode?.id==1}">${gs.analyticCatOther}</g:if>
				<g:else>${gs.analyticCatConceptCode?.codeName}</g:else>				
			</td>
		</tr>							
		<tr class="prop">		
			<td class="name">Analysis Method:</td>
			<td class="value">			
				<g:if test="${gs.analysisMethodConceptCode?.id==1}">${gs.analysisMethodOther}</g:if>
				<g:else>${gs.analysisMethodConceptCode?.codeName}</g:else>				
			</td>	
		</tr>
		<tr class="prop">
			<td class="name">Multiple Testing Correction:</td>
			<td class="value"><g:if test="${gs.multipleTestingCorrection!=null}">${gs.multipleTestingCorrection==1 ? 'Yes' : 'No'}</g:if></td>
		</tr>	
		<tr class="prop">
			<td class="name">P-value Cutoff:</td>
			<td class="value">${gs.pValueCutoffConceptCode?.codeName}</td>
		</tr>
		<tr class="prop">
			<td class="name">Fold-change metric:</td>
			<td class="value">${gs.foldChgMetricConceptCode?.codeName}</td>
		</tr>
		<tr class="prop">
			<td class="name">Original upload file:</td>
			<td class="value">${gs.uploadFile}</td>
		</tr>
	</tbody>
</table>
		
<table style="width: 100%">
	<g:tableHeaderToggle label="Gene Signature Items" divPrefix="${gs.id}_items" />

	<tbody id="${gs.id}_items_detail" style="display: none;">	
	<tr><td>
		<table class="detail" width="100%" border=1>
			<tr>
				<td style="font-weight: bold;">Gene Symbol</td>
				<td style="font-weight: bold;">Probeset ID</td>
				<g:if test="${gs.foldChgMetricConceptCode?.bioConceptCode!='NOT_USED'}"><td style="font-weight: bold; white-space: nowrap;">Fold-Change Metric</td></g:if>
			</tr>			
			<g:each in="${gs.geneSigItems}">
			<tr>
				<td class="name">${it.bioMarker?.name}</td>		
				<td class="name">${it.probeset?.name}</td>
				<g:if test="${gs.foldChgMetricConceptCode?.bioConceptCode!='NOT_USED'}">		
					<g:if test="${gs.foldChgMetricConceptCode?.bioConceptCode=='TRINARY'}"><td class="name" style="text-align: right;"><g:formatNumber number="${it.foldChgMetric}" format="0"/></td></g:if>
					<g:else><td class="name" style="text-align: right;"><g:formatNumber number="${it.foldChgMetric}" format="##0.###"/></td></g:else>
				</g:if>		
			</tr>
			</g:each>
		</table>
	</td></tr>
	</tbody>
</table>
