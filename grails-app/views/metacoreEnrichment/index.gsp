<div style="text-align: left; font: 12pt arial;">
	<table
		style="margin: 10px; width: 90%; border: 0px none; border-collapse: collapse;"
		class="subsettable">
		<tbody>
			<tr>
				<td><span class="AnalysisHeader">Cohorts (<i>Early Alpha version</i> - only the first cohort will be used</i>)</span></td>
				<td align="right">&nbsp;
					<!--
					<input type="button"
					onclick="javascript: generatePdfFromHTML('dataAssociationBody', 'DataAssociation.pdf');"
					value="Save To PDF">
					-->
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<div id="cohortSummaryMetaCoreEnrichment">
						<font style="color: red; font-weight: bold;">Warning! You
							have not selected a study and the analyses will not work. Please
							go back to the 'Comparison' tab and make a cohort selection.</font>
					</div>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<hr>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<hr>
				</td>
			</tr>
		</tbody>
	</table>
</div>

<form>

	<table class="subsettable"
		style="margin: 10px; width: 300px; border: 0px none; border-collapse: collapse;">
		<tr>
			<td colspan="4"><span class='AnalysisHeader'>Variable
					Selection</span> <a
				href='JavaScript:D2H_ShowHelp(1505,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP )'>
					<img
					src="${resource(dir:'images', file:'help/helpicon_white.jpg')}"
					alt="Help" border=0 width=18pt
					style="margin-top: 1pt; margin-bottom: 1pt; margin-right: 18pt; float: right" />
			</a></td>
		</tr>
		<tr>
			<td colspan="4">
				<hr />
			</td>
		</tr>
		<tr>
			<td align="center"><span class='AnalysisHeader'>Data
					preparation</span> <br /> <br /> Select a High Dimensional Data node
				from the Data Set Explorer Tree and drag it into the box.</td>
		</tr>
		<tr>
			<td align="right"><input style="font: 9pt tahoma;" type="button"
				onclick="clearGroupHeatmap('divIndependentVariableMetaCoreEnrichment')"
				value="X"> <br />
				<div id='divIndependentVariableMetaCoreEnrichment'
					class="queryGroupIncludeSmall"></div></td>
		</tr>
		<tr>
			<td align="right"><input style="font: 9pt tahoma;" type="button"
				onclick="gatherHighDimensionalData('divIndependentVariableMetaCoreEnrichment')"
				value="High Dimensional Data"> <input type="hidden"
				id="multipleSubsets" name="multipleSubsets" value="true" /></td>
		</tr>
		<tr>
			<td><br /></td>
		</tr>
		<tr>
			<td>
				<div id="displaydivIndependentVariableMetaCoreEnrichment"></div>
			</td>
		</tr>
		<tr>
			<td><br /></td>
		</tr>
		<tr>
			<td colspan="4" align="center">
				Specify a Z-Score threshold (optional):
			</td>
		</tr>
		<tr>
			<td colspan="4" align="center">
				|zscore| &gt;= <input type="text" name="z_threshold_abs" id="z_threshold_abs" size="4" value="0" />
			</td>
		</tr>
		<tr>
			<td><br /></td>
		</tr>		
		<tr>
			<td colspan="4" align="center"><g:metacoreSettingsButton /><input type="button" value="Run"
				onClick="submitMetaCoreEnrichmentJob(this.form);"></td>
		</tr>
	</table>
</form>

<!--  results section -->
<g:render template="enrichmentResult" model="[prefix: '']"/>

