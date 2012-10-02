<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
    <title>subsetPanel.html</title>
	
    <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
    <link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'cortellis.css')}"/>
    <script type="text/javascript" src="${resource(dir:'js', file:'cortellisAnalytics/cortellisAnalytics.js')}"></script>
  </head>
  
  <body>
   <div id="toolbar"></div>
   <div id="analysisBody">
  	<div style="text-align:left;font:12pt arial;">
  		<table class="subsettable" style="margin: 10px; width: 90%; border: 0px none; border-collapse: collapse;">
			<tr>
				<td>
					<span class='AnalysisHeader'>Cohorts</span>
				</td>
				<td align="right">
					<input type="button" value="Save To PDF" onclick="javascript: generatePdfFromHTML('analysisBody', 'DataAssociation.pdf');"></input>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<div id = "cohortSummary"></div>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<hr></hr>
				</td>
			</tr>
  			<tr>
  				<td colspan="2"><span class='AnalysisHeader'>Analysis: </span>
  				<span id="selectedAnalysis">Select Analysis from the "Analysis" menu</span>
  				<input type="hidden" id="analysis" name="analysis" />
  				
				<a href='JavaScript:D2H_ShowHelp(1503,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP )'>
			<img src="${resource(dir:'images',file:'help/helpicon_white.jpg')}" alt="Help" border=0 width=18pt style="margin-top:1pt;margin-bottom:1pt;margin-right:18pt;"/>
				</a>	  				
  				</td>
			</tr>
			<tr>
				<td colspan="2">
					<hr></hr>
				</td>
			</tr>
		</table>
	</div>
	<div id="variableSelection"></div>
	<div style="page-break-after:always"></div>
	<div id="analysisOutput" style="margin:10px;"></div>
   </div>
  </body>
  
</html>