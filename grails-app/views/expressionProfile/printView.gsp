<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.0 Strict//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="shortcut icon" href="${resource(dir:'images',file:'searchtool.ico')}">
		<link rel="icon" href="${resource(dir:'images',file:'searchtool.ico')}">
		<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}"></link>
		<title>${grailsApplication.config.com.recomdata.searchtool.appTitle}</title>
	</head>
	<body onload="window.print();">
		<table>
			<tr><td><img src="${createLink(action:'displayChart') + '?filename=' + filename}"/></td></tr>
			<tr><td>&nbsp;</td></tr>
			<tr><td><center>
				<a href="#" onclick="window.print();">
					<img src="${resource(dir:'images',file:'print.png')}" />
					Print
				</a>
			</center></td></tr>
		</table>
	</body>
</html>