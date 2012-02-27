<g:form controller="trial" name="trialfilter-form" id="trialfilter-form" action="filterTrial">
	<input type="hidden" name="checked" value="">
	<g:set var="trialFilter" value="${session.searchFilter.trialFilter}"/>

	<table class="jubfilter" style="width:600px">
		<tr>
			<th colspan=2 style="align: right">
				<span class="button">
					<g:submitButton class="search" onclick="submitChecked('trialfilter');" value="Filter Results" action="filterTrial" name="trialfilterbutton" />&nbsp;
				</span>
			</th>
		</tr>
		<tr>
			<td colspan=2 style="border-right:0px solid #ccc">
				<table class="jubfiltersection">
					<tr>
						<td style="width: 175px; white-space: nowrap;">Disease:</td>
						<td>
							<g:select from="${diseases}" name="bioDiseaseId" optionKey="id" optionValue="preferredName" value="${trialFilter.bioDiseaseId}" noSelection="['':'-- Any --']"/>
						</td>
					</tr>
					<tr>
						<td style="width: 175px; white-space: nowrap;">Compound:</td>
						<td>
							<g:select from="${compounds}" name="bioCompoundId" optionKey="id" optionValue="name" value="${trialFilter.bioCompoundId}" noSelection="['':'-- Any --']"/>
						</td>
					</tr>
						<tr>
						<td style="width: 175px; white-space: nowrap;">Study Phase:</td>
						<td>
							<g:select from="${phases}" name="phase" value="${trialFilter.phase}" noSelection="['':'-- Any --']"/>
						</td>
					</tr>
					<tr>
						<td style="width: 175px; white-space: nowrap;">Study Type:</td>
						<td>
							<g:select from="${studyTypes}" name="studyType" value="${trialFilter.studyType}" noSelection="['':'-- Any --']"/>
						</td>
					</tr>
					<tr>
						<td style="width: 175px; white-space: nowrap;">Study Design:</td>
						<td>
							<g:select from="${studyDesigns}" name="studyDesign" value="${trialFilter.studyDesign}" noSelection="['':'-- Any --']"/>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td colspan=2 style="border-right:0px solid #ccc">
				<table class="jubfiltersection">
					<tr>
						<td style="width: 175px; white-space: nowrap;">Data Platform:</td>
						<td>
							<g:select from="${studyPlatform}"  name="platform" value="${trialFilter?.platform}" noSelection="['':'-- Any --']"/>
						</td>
					</tr>
					<tr>
						<td style="width: 175px; white-space: nowrap;">Fold Change Cut Off:</td>
						<td style="font-weight:normal">
							<g:textField name="foldChange" value="${trialFilter?.foldChange}"/> (Minimum Fold Change Ratio +/-1.0)
						</td>
					</tr>
					<tr>
						<td style="width: 175px; white-space: nowrap;">p Value Is Less Than:</td>
						<td style="font-weight:normal">
							<g:textField name="pValue" value="${trialFilter?.pValue}"/> (Maximum P-Value 0.1)
						</td>

					</tr>
						<tr>
						<td style="width: 175px; white-space: nowrap;">Absolute R/Rho Value is Less Than:</td>
						<td style="font-weight:normal">
							<g:textField name="rValue" value="${trialFilter?.rValue}"/>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td colspan=2 style="border-right: 0px solid #ccc">
				<table class="jubfiltersection">
					<tr>
						<td>
						 <b>Clinical Trials:</b>
						 <br>
						 <br>
							<div id="trialfilter-div" style="overflow:auto; height:350px; width:700px; border:0px solid #c3daf9;"></div>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</g:form>