<!--
 Copyright 2008-2012 Janssen Research & Development, LLC.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>advancedExport.html</title>

<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="this is my page">
<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'datasetExplorer.css')}">

</head>

<body>
	<form>
		<div style="text-align:center;">
			<span class="subsettable">
				<b>Drag in the clinical concepts you would like to export. Note: only these concepts will be exported for the selected subset.</b>
			</span>
			<table style="margin-left: auto; margin-right: auto; width:50%;">
				<tr>
					<td style="text-align:right;">
						<input style="font: 9pt tahoma;" type="button" onclick="clearExportableConcepts()" value="X"> <br />
						<div id='divExportableConcepts' class="queryGroupIncludeLong excludeValuePopup" style="width:95%;height:200px;"></div> <br />
					</td>
				</tr>
				<tr>
					<td style="text-align:center;">
						<input style="font: 9pt tahoma;" type="button" onclick="hideAdvancedExport()" value="Cancel"> 
					</td>
				</tr>
			</table>
		</div>
	</form>
</body>

</html>