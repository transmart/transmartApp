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
		<p>Cox Regression Result:</p>
		
		${coxData}
		
		<br />
		<br />		
		<p>Survival Curve Fitting Summary:</p>
		
		${survivalData}

		<img src='${imageLocation}' />
	</form>
</body>

</html>