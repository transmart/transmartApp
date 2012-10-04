	<form>
	
		<table class="subsettable" style="margin: 10px;width:300px; border: 0px none; border-collapse: collapse;" >
			<tr>
				<td colspan="4">
					<span class='AnalysisHeader'>Variable Selection</span>
					<a href='JavaScript:D2H_ShowHelp(1505,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP )'>
				<img src="${resource(dir:'images/help', file:'helpicon_white.jpg')}" alt="Help" border=0 width=18pt style="margin-top:1pt;margin-bottom:1pt;margin-right:18pt;float:right"/>
					</a>						
				</td>			
			</tr>	
			<tr>
				<td colspan="4">
					<hr />
				</td>
			</tr>	
			<tr>
				<td align="center">
					<span class='AnalysisHeader'>Data preparation</span>
					<br />
					<br />
					Select a High Dimensional Data node from the Data Set Explorer Tree and drag it into the box.
				</td>
			</tr>	
			<tr>
				<td align="right">
					<input style="font: 9pt tahoma;" type="button" onclick="clearGroupHeatmap('divIndependentVariableMetaCoreEnrichment')" value="X"> 
					<br />
					<div id='divIndependentVariableMetaCoreEnrichment' class="queryGroupIncludeSmall"></div>
				</td>
			</tr>
			<tr>
				<td align="right">
					<input style="font: 9pt tahoma;" type="button" onclick="gatherHighDimensionalData('divIndependentVariableMetaCoreEnrichment')" value="High Dimensional Data">
					<input type="hidden" id="multipleSubsets" name="multipleSubsets" value="true" />
				</td>
			</tr>
			<tr><td><br/></td></tr>
			<tr>
				<td>
					<div id = "displaydivIndependentVariableMetaCoreEnrichment"></div>
				</td>
			</tr>
			<tr><td><br/></td></tr>
			<tr>
				<td colspan="4" align="center">
					<input type="button" value="Run" onClick="submitHeatmapJob(this.form);"></input>
				</td>
			</tr>
		</table>
	</form>
	
