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
	
		<table class="subsettable" border="0px" style="margin: 10px;width:300px;" >
			<tr>
				<td colspan="4">
					<span class='AnalysisHeader'>Variable Selection</span>
				</td>			
			</tr>	
			<tr>
				<td colspan="4">
					<hr />
				</td>
			</tr>	
			<tr>
				<td align="center">
					<span class='AnalysisHeader'>Independent Variable</span>
					<br />
					<br />
					Select a continuous variable from the Data Set Explorer Tree and drag it into the box.
				</td>
				<td id="subsetdivider" rowspan="2" valign="center" align="center" height="100%">
					<div style="margin: 15px; border: 1px solid black; background: black; width: 1px; height: 150px"></div>
				</td>
				<td align="center">
					<span class='AnalysisHeader'>Dependent Variable</span>
					<br />
					<br />
					Select a continuous variable from the Data Set Explorer Tree and drag it into the box.					
				</td>					
			</tr>
	
			<tr>
				<td align="right">
					<input style="font: 9pt tahoma;" type="button" onclick="clearGroupScatter('divIndependentVariable')" value="X"> 
					<br />
					<div id='divIndependentVariable' class="queryGroupIncludeSmall"></div>
				</td>
				<td align="right">
					<input style="font: 9pt tahoma;" type="button" onclick="clearGroupScatter('divDependentVariable')" value="X"> 
					<br />
					<div id='divDependentVariable' class="queryGroupIncludeSmall"></div>
				</td>
			</tr>
			<tr>
				<td colspan="4" align="center">
					<input type="button" value="Run" onClick="submitScatterPlotJob(this.form);"></input>
				</td>
			</tr>
		</table>
	</form>
</body>

</html>