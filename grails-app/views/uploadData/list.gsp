<!--
  tranSMART - translational medicine data mart

  Copyright 2008-2012 Janssen Research & Development, LLC.

  This product includes software developed at Janssen Research & Development, LLC.

  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.

  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.


-->

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.0 Strict//EN">
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=8" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="shortcut icon" href="${resource(dir:'images',file:'searchtool.ico')}">
    <link rel="icon" href="${resource(dir:'images',file:'searchtool.ico')}">
    <link rel="stylesheet" href="${resource(dir:'js', file:'ext/resources/css/ext-all.css')}"></link>
    <link rel="stylesheet" href="${resource(dir:'js', file:'ext/resources/css/xtheme-gray.css')}"></link>
    <link rel="stylesheet" href="${resource(dir:'css', file:'main.css')}"></link>
    <link rel="stylesheet" href="${resource(dir:'css', file:'uploadData.css')}"></link>

    <!--[if IE 7]>
		<style type="text/css">
			 div#gfilterresult,div#ptfilterresult, div#jubfilterresult, div#dqfilterresult {
				width: 99%;
			}



		</style>
	<![endif]-->

    <style type="text/css">
    .uploadfieldtable th, .uploadfieldtable td { font-size: 8px }

    </style>

    <g:javascript library="prototype" />
    <script type="text/javascript" src="${resource(dir:'js', file:'searchcombobox.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js', file:'picklist.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js', file:'utilitiesMenu.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery-1.7.1.min.js')}"></script>
    <script type="text/javascript">$j = jQuery.noConflict();</script>

    <title>${grailsApplication.config.com.recomdata.dataUpload.appTitle}</title>
    <!-- ************************************** -->
    <!-- This implements the Help functionality -->
    <script type="text/javascript" src="${resource(dir:'js', file:'help/D2H_ctxt.js')}"></script>
    <script language="javascript">
        helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
    </script>
    <!-- ************************************** -->
</head>
<body>
<div id="header-div">
    <g:render template="/layouts/commonheader" model="['app':'uploaddata']" />

    <br/><br/>
    <div class="uploadwindow" style="width: 95%">
        <div>List of the 20 most recent data uploads
        </div>
        <table class="uploadfieldtable" style="margin:0px">
            <thead>
            <tr>
                <th>id</th>
                <th>study</th>
                <th>dataType</th>
                <th>analysisName</th>
                <th>phenotypeIds</th>
                <th>genotypePlatformIds</th>
                <th>expressionPlatformIds</th>
                <th>statisticalTest</th>
                <th>researchUnit</th>
                <th>sampleSize</th>
                <th>cellType</th>
                <th>modelName</th>
                <th>pValueCutoff</th>
                <th>etlDate</th>
                <th>processDate</th>
                <th>filename</th>
                <th>status</th>
                <th>sensitiveFlag</th>
                <th>sensitiveDesc</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${uploads}" var="upload">
                <tr>
                    <td>${upload.id}</td>
                    <td>${upload.study}</td>
                    <td>${upload.dataType}</td>
                    <td>${upload.analysisName}</td>
                    <td><g:each in="${upload.phenotypeIds?.split(';')}" var="me">${me}<br/></g:each></td>
                    <td>${upload.genotypePlatformIds}</td>
                    <td>${upload.expressionPlatformIds}</td>
                    <td>${upload.statisticalTest}</td>
                    <td>${upload.researchUnit}</td>
                    <td>${upload.sampleSize}</td>
                    <td>${upload.cellType}</td>
                    <td>${upload.modelName}</td>
                    <td>${upload.pValueCutoff}</td>
                    <td>${upload.etlDate}</td>
                    <td>${upload.processDate}</td>
                    <td>${upload.filename}</td>
                    <td>${upload.status}</td>
                    <td>${upload.sensitiveFlag}</td>
                    <td>${upload.sensitiveDesc}</td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

</div>

</body>
</html>