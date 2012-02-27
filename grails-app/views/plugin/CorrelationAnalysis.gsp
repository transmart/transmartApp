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
					<span class='AnalysisHeader'>Variables</span>
					
					<br /><br />
					
					Drag two or more <b>numerical</b> concepts from the tree into the box below that you wish to generate correlation statistics on. 
					
				</td>
			</tr>
	
			<tr>
				<td align="right">
					<input style="font: 9pt tahoma;" type="button" onclick="clearGroupCorrelation('divVariables')" value="X"> <br />
					<div id='divVariables' class="queryGroupIncludeLong"></div>
				</td>			
			</tr>
		</table>
		<table class="subsettable" border="0px" style="margin: 10px">
			<tr>
				<td>
					<b>Run Correlation</b>
					<select id = "correlationBy">
					  <option value="variable">By variable</option>
					</select> 
				</td>	
			</tr>
			<tr>
				<td>
					<b>Correlation Type</b>
					<select id = "correlationType">
					  <option value="spearman">Spearman</option>
					  <option value="pearson">Pearson</option>
					  <option value="kendall">Kendall</option>
					</select> 
				</td>	
			</tr>			
			<tr>
				<td>
					<input type="button" value="Run" onClick="submitCorrelationAnalysisJob(this.form);"></input>
				</td>
			</tr>
		</table>
	</form>
</body>

</html>