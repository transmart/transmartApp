<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>subsetPanel.html</title>
	
    <meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
    <meta http-equiv="description" content="this is my page">
    <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
    <link rel="stylesheet" type="text/css" href="css/datasetExplorer.css">
  </head>
  
  <body>
  	<div style="text-align:left;font:12pt arial;">
  		<table class="subsettable" border="0px"	style="margin: 10px; width: 90%;">
			<tr>
				<td>
					<span class='AnalysisHeader'>Cohorts</span>
				</td>
				<td align="right"><%--
					<input type="button" value="Save To PDF" onClick=""></input>
					<input type="button" value="Print" onClick=""></input>
				--%></td>
			</tr>
			<tr>
				<td colspan="2">
					<div id = "cohortSummary"></div>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<hr />
				</td>
			</tr>
  			<tr>
  				<td>  	
  					<g:form action="variableSelection">
	  					<span class='AnalysisHeader'>Analysis</span>
						<g:select name = "analysis"
							from = "${analysisList}" 
							optionValue = "name"
							optionKey="id"/>	
						<g:submitToRemote url="[controller:'dataAssociation', action:'variableSelection']" value="Submit" 
							onComplete="loadPluginView();" update="variableSelection"/>
					</g:form>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<hr />
				</td>
			</tr>
		</table>
	</div>
	<div id="variableSelection"></div>
	<div id="analysisOutput"></div>
  </body>
  
</html>