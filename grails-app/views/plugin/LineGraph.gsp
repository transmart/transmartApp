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
	
		<table class="subsettable" border="0px" style="margin: 10px;width:300px;">
			<tr>
				<td colspan="5">
					<span class='AnalysisHeader'>Variable Selection</span>
				</td>			
			</tr>	
			<tr>
				<td colspan="5">
					<hr />
				</td>
			</tr>	
			<tr>
				<td align="center">
					<span class='AnalysisHeader'>Time/Measurement Concepts</span>
					
					<br /><br />
					
					Drag one or more <b>numerical</b> concepts from the tree into the box below. The concepts must come from a data node (Biomarker Data or Clinical Data).  
					
				</td>
				<td id="subsetdivider" rowspan="21" valign="center" align="center" height="100%">
					<div style="margin: 15px; border: 1px solid black; background: black; width: 1px; height: 150px"></div>
				</td>
				<td align="center">
					<span class='AnalysisHeader'>Group Concepts</span>
					
					<br /><br />
					
					Drag one or more concepts from the tree into the box below to divide the subjects into groups (for example, Treatment Groups). A folder may be dragged in to include all leaf nodes under that folder. Each group will be plotted as a distinct line on the graph. 
				</td>										
			</tr>
	
			<tr>
				<td align="right">
					<input style="font: 9pt tahoma;" type="button" onclick="clearGroupLine('divDependentVariable')" value="X"> <br />
					<div id='divDependentVariable' class="queryGroupIncludeLong"></div>
				</td>
				<td align="right">
					<input style="font: 9pt tahoma;" type="button" onclick="clearGroupLine('divGroupByVariable')" value="X"> <br />
					<div id='divGroupByVariable' class="queryGroupIncludeLong"></div>
				</td>
			</tr>
		</table>
		<table class="subsettable" border="0px" style="margin: 10px" width="90%">
			<tr>
				<td colspan="2">
					<b>Graph Type</b>
					<select id = "graphType">
					  <option value="MERR">Mean with error bar</option>
					  <option value="MSTD">Mean with standard deviation</option>
					  <option value="MEDER">Median with error bar</option>
					</select> 
				</td>
			</tr>
			<tr>
				<td>
					<input type="button" value="Run" onClick="submitLineGraphJob(this.form);"></input>
				</td>
			</tr>
		</table>
	</form>
</body>

</html>