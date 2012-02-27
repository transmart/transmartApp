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
	
		<br />
		<br />	
		<span class='AnalysisHeader'>Scatter Plot</span><br /><br />

		<img src='${imageLocation}'  width="600" height="600"/>
	
		<br />
		<br />
		<span class='AnalysisHeader'>Linear Regression Result</span><br /><br />
		
		${linearRegressionData}
		
		<br />
		<br />
		<a class='AnalysisLink' href="${zipLink}">Download raw R data</a>
		
	</form>
</body>

</html>