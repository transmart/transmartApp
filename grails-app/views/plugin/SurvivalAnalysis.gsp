<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>subsetPanel.html</title>

<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="this is my page">
<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css"
	href="${resource(dir:'css', file:'datasetExplorer.css')}">

</head>

<body>
	<form>

		<table class="subsettable" border="0px" style="margin: 10px;">
			<tr>
				<td colspan="6">
					<h1>Variable Selection</h1></td>
			</tr>
			<tr>
				<td colspan="6">
					<hr /></td>
			</tr>
			<tr>
				<td align="center">
					<h2>
						<u>Time</u>
						
						<br />
						
						Select time variable from the Data Set Explorer Tree and drag it into the box.  For example, "Survival Time".
						This variable is required.						
						
					</h2>
				</td>
				<td id="subsetdivider" rowspan="2" valign="center" align="center" height="100%">
					<div style="margin: 15px; border: 1px solid black; background: black; width: 1px; height: 150px"></div>
				</td>				
				<td align="center">
					<h2>
						<u>Category</u>
						
						<br />
						
						Select a variable on which you would like to sort the cohort and drag it into the box. For example, "Cancer Stage".  If this variable is continuous (ex. Age), then it should be "binned" using the option below.
                		This variable is not required.
						
					</h2>
				</td>
				<td id="subsetdivider" rowspan="2" valign="center" align="center" height="100%">
					<div style="margin: 15px; border: 1px solid black; background: black; width: 1px; height: 150px"></div>
				</td>				
				<td align="center">
					<h2>
						<u>Censoring Variable</u>
						
						<br />
						
						Select the appropriate censoring variable and drag it into the box. For example, "Survival (Censor) -> Yes".
                		This variable is not required.						
						
					</h2>
				</td>			
			</tr>
			<tr>
				<td align="right">
					<input style="font: 9pt tahoma;" type="button" onclick="clearGroupSurvival('divTimeVariable')" value="X"> <br />
					<div id='divTimeVariable' class="queryGroupIncludeSmall"></div>
				</td>
				<td align="right">
					<input style="font: 9pt tahoma;" type="button" onclick="clearGroupSurvival('divCategoryVariable')" value="X"> <br />
					<div id='divCategoryVariable' class="queryGroupIncludeSmall"></div>
				</td>
				<td align="right"><input style="font: 9pt tahoma;"
					type="button" onclick="clearGroupSurvival('divCensoringVariable')"
					value="X"> <br />
					<div id='divCensoringVariable' class="queryGroupIncludeSmall"></div>
				</td>
			</tr>
		</table>

		<table class="subsettable" border="0px"
			style="margin: 10px; width: 90%;">
			<tr>
				<td>
					<h1>
						Binning&nbsp;&nbsp;&nbsp;<input id="BinningToggle" type="button"
							value="Enable" onClick="toggleBinning();" />
					</h1></td>
			</tr>
			<tr>
				<td>
					<hr /></td>
			</tr>
		</table>

		<div id="divBinning" style="display: none;">

			<table id="tblBinningTable" class="subsettable" border="0px"
				style="margin: 10px; width: 90%;">
				<tr>
					<td align="left"><b>Variable</b> : <select READONLY>
							<option value="">Category</option>
					</select></td>
				</tr>
				<tr>
					<td><b>Variable Type</b> <select id="variableType"
						onChange="updateManualBinning();">
							<option value="Continuous">Continuous</option>
							<option value="Categorical">Categorical</option>
					</select></td>
				</tr>
				<tr>
					<td align="left"><b>Number of Bins</b> : <input type="text"
						id="txtNumberOfBins" onChange="manageBins(this.value);" value="4" />
					</td>
				</tr>
				<tr>
					<td align="left"><b>Bin Assignments (Continuous variables
							only)</b> : <select id="selBinDistribution">
							<option value="EDP">Evenly Distribute Population</option>
							<option value="ESB">Evenly Spaced Bins</option>
					</select></td>
				</tr>
				<tr>
					<td align="left">&nbsp;</td>
				</tr>
				<tr>
					<td align="left"><b>Manual Binning</b> : <input
						type="checkbox" id="chkManualBin" onClick="updateManualBinning();" />
					</td>
				</tr>
				<tr>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td>
						<div id="divManualBinContinuous" style="display: none;">
							<table style="border-style: solid; border-width: thin;"
								id="tblBinContinuous">
								<tr>
									<td>Bin Name</td>
									<td colspan="2" align="center">Range</td>
								</tr>
								<tr id="binningContinousRow1">
									<td>Bin 1</td>
									<td><input type="text" id="txtBin1RangeLow" /> - <input
										type="text" id="txtBin1RangeHigh" /></td>
								</tr>
								<tr id="binningContinousRow2">
									<td>Bin 2</td>
									<td><input type="text" id="txtBin2RangeLow" /> - <input
										type="text" id="txtBin2RangeHigh" /></td>
								</tr>
								<tr id="binningContinousRow1">
									<td>Bin 3</td>
									<td><input type="text" id="txtBin3RangeLow" /> - <input
										type="text" id="txtBin3RangeHigh" /></td>
								</tr>
								<tr id="binningContinousRow4">
									<td>Bin 4</td>
									<td><input type="text" id="txtBin4RangeLow" /> - <input
										type="text" id="txtBin4RangeHigh" /></td>
								</tr>																								
							</table>
						</div>
						<div id="divManualBinCategorical" style="display: none;">
							<table>
								<tr>
									<td style="vertical-align: top;"><b>Categories</b>
										<div id='divCategoricalItems' class="queryGroupIncludeSmall"></div>
									</td>
									<td style="vertical-align: top;"><br />
									<br /><-Drag To Bin-></td>
									<td>
										<table id="tblBinCategorical">

										</table>
									</td>
								</tr>
							</table>
						</div>
					</td>
				</tr>
			</table>
		</div>
		<div align="center" style="width: 65%;">
			<input type="button" value="Run"
				onClick="submitSurvivalJob(this.form);"></input>
		</div>
	</form>
</body>

</html>