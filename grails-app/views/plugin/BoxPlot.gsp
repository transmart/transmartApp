<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>subsetPanel.html</title>

<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="this is my page">
<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'datasetExplorer.css')}">

</head>

<body>
	<form>
	
		<table class="subsettable" border="0px" style="margin: 10px">
			<tr>
				<td colspan="4">
					<h1>Variable Selection</h1>
				</td>			
			</tr>	
			<tr>
				<td colspan="4">
					<hr />
				</td>
			</tr>	
			<tr>
				<td align="center">
					<h2>
						<u>Independent Variable</u>
					</h2>
				</td>
				<td id="subsetdivider" rowspan="21" valign="center" align="center" height="100%">
					<div style="margin: 15px; border: 1px solid black; background: black; width: 1px; height: 150px"></div>
				</td>
				<td align="center">
					<h2>
						<u>Dependent Variable</u>
					</h2>
				</td>					
			</tr>
	
			<tr>
				<td align="right">
					<button style="font: 9pt tahoma;" onclick="clearGroup('divDependentVariable')">X</button> <br />
					<div id='divDependentVariable' class="queryGroupIncludeSmall"></div>
				</td>
				<td align="right">
					<input style="font: 9pt tahoma;" type="button" onclick="clearGroup('divIndependentVariable')" value="X"> <br />
					<div id='divIndependentVariable' class="queryGroupIncludeSmall"></div>
				</td>			
			</tr>
		</table>
		<table class="subsettable" border="0px"	style="margin: 10px; width: 90%;">
			<tr>
				<td>
					<h1>Binning&nbsp;&nbsp;&nbsp;<input id="BinningToggle" type = "button" value="Enable" onClick="toggleBinning();" /></h1>
				</td>
			</tr>
			<tr>
				<td>
					<hr />
				</td>
			</tr>
		</table>

		<div id="divBinning" style="display:none;">
		
			<table id="tblBinningTable" class="subsettable" border="0px" style="margin: 10px; width: 90%;">
				<tr>		
					<td align="left">
						<b>Variable</b> : 
						<select READONLY>
						  <option value="">Category</option>
						</select>
					</td>
				</tr>
				<tr>		
					<td align="left">
						<b>Number of Bins</b> :
					 
						<input type="text" />
					</td>
				</tr>
				<tr>
					<td align="left">
						<b>Bin Assignments</b> : 
	
						<select>
						  <option value="EDP">Evenly Distribute Population</option>
						  <option value="ESB">Evenly Spaced Bins</option>
						</select>
					</td>
				</tr>
				<tr>
					<td align="left">
						&nbsp;
					</td>
				</tr>
				<tr>
					<td align="left">
						<b>Manual Binning</b> : <input type="checkbox" onClick="Ext.get('divManualBin').toggle();" />
					</td>
				</tr>			
				<tr>
					<td>
						<div id="divManualBin" style="display:none;">
						
							<br />
							<br />
							
							<table style="border-style:solid;border-width:thin;">
								<tr>
									<td colspan="2" align="center">
										&nbsp;

										Range
									</td>
								</tr>
								<tr>
									<td>
										Bin 1
									</td>							
									<td>
										<input type="text" /> - <input type="text" />
									</td>
								</tr>		
								<tr>
									<td>
										Bin 2
									</td>							
									<td>
										<input type="text" /> - <input type="text" />
									</td>
								</tr>													
							</table>
							
							<br />
							<br />
							
							<table>
								<tr>
									<td rowspan = "3">
										<select MULTIPLE SIZE="5">
											<option>MUT1</option>
											<option>MUT2</option>
											<option>MUT3</option>
										</select>
									</td>
									<td>
										Bin 1
									</td>
									<td>
										<div id='div1' class="queryGroupIncludeSmall"></div>
									</td>
								</tr>
								<tr>
									<td>
										Bin 2
									</td>
									<td>
										<div id='div1' class="queryGroupIncludeSmall"></div>
									</td>									
								</tr>
							</table>										
						</div>
					</td>
				</tr>
			</table>
		</div>	
		<input type="button" value="Run" onClick="submitJob();"></input>
	</form>
</body>

</html>