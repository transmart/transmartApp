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

<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=8" />
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="shortcut icon" href="${resource(dir:'images',file:'searchtool.ico')}">
		<link rel="icon" href="${resource(dir:'images',file:'searchtool.ico')}">
		<link rel="stylesheet" href="${resource(dir:'js', file:'ext/resources/css/ext-all.css')}"></link>
		<link rel="stylesheet" href="${resource(dir:'js', file:'ext/resources/css/xtheme-gray.css')}"></link>
		<link rel="stylesheet" href="${resource(dir:'css', file:'main.css')}"></link>
		<link rel="stylesheet" href="${resource(dir:'css',file:'/jquery/cupertino/jquery-ui-1.8.18.custom.css')}"></link>   
        <link rel="stylesheet" href="${resource(dir:'css/jquery/skin', file:'ui.dynatree.css')}"></link>
        <link rel="stylesheet" href="${resource(dir:'css', file:'rwg.css')}"></link>
        <link rel="stylesheet" href="${resource(dir:'css', file:'uploadData.css')}"></link>

	<!--[if IE 7]>
		<style type="text/css">
			 div#gfilterresult,div#ptfilterresult, div#jubfilterresult, div#dqfilterresult {
				width: 99%;
			}
		</style>
	<![endif]-->
		
		<g:javascript library="prototype" />
<script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery-1.8.3.min.js')}"></script>
<script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery-ui.min.js')}"></script>
<script type="text/javascript">$j = jQuery.noConflict();</script>
<script type="text/javascript" src="${resource(dir:'js', file:'uploadData.js')}"></script>
<script type="text/javascript" charset="utf-8">
var studyBrowseWindowUrl = '${createLink([controller: 'experiment', action: 'browseExperimentsSingleSelect'])}';
var studyDetailUrl = '${createLink([controller:'experimentAnalysis', action:'expDetail'])}';
var studyHasFolderUrl = '${createLink([controller:'uploadData', action:'studyHasFolder'])}';
var platformTypesUrl = '${createLink([action:'platformsForVendor',controller:'bioAssayPlatform'])}';
var templateDownloadUrl = '${createLink([action:'template',controller:'uploadData'])}';

var IS_EDIT = ${uploadDataInstance?.id ? true : false};
var ANALYSIS_TYPE = null;
jQuery(document).ready(function() {
jQuery("#sensitiveDesc").hide();
jQuery("#sensitiveFlag").val('1');
});

var helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
var contact = '${grailsApplication.config.com.recomdata.searchtool.contactUs}';
var appTitle = '${grailsApplication.config.com.recomdata.searchtool.appTitle}';
var buildVer = 'Build Version: <g:meta name="environment.BUILD_NUMBER"/> - <g:meta name="environment.BUILD_ID"/>';

<g:if test="${study}">
    updateStudyTable('${study.accession}');
</g:if>
</script>
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
    <g:render template="/layouts/commonheader" model="['app':'uploaddata', 'utilitiesMenu':'true']" />
</div>

<div id="mainUploadPane">
    <g:uploadForm name="dataUpload" action="upload" method="post">
        <div id="formPage1" class="formPage">

            <div class="dataFormTitle" id="dataFormTitle1">
                <g:if test="${uploadDataInstance?.id ? true : false}">
                    Edit Metadata
                </g:if>
                <g:else>
                    Upload Analysis Data
                </g:else>
            </div>
            <div style="position: relative; text-align:right;">
                <a class="button" href="mailto:${grailsApplication.config.com.recomdata.dataUpload.adminEmail}">Email administrator</a>
                 <tmpl:/help/helpIcon id="1331"/>&nbsp;
                <div class="uploadMessage">If you are unable to locate the relevant study, email the administrator by clicking the button above.</div>
            </div>
            <g:if test="${flash.message}">
                <div class="message">${flash.message}</div>
            </g:if>
            <table class="uploadTable">
                <tr>
                    <td width="10%">&nbsp;</td>
                    <td width="90%">&nbsp;</td>
                </tr>
                <tr>
                    <td>
                        Study:
                    </td>
                    <td>
                        <div id="studyErrors">
                            <g:eachError bean="${uploadDataInstance}" field="study">
                                <div class="fieldError"><g:message error="${it}"/></div>
                            </g:eachError>
                        </div>
                        <tmpl:extSearchField width="600" fieldName="study" searchAction="extSearch" searchController="experiment" value="${study?.accession}" label="${study?.title}" paramString="'studyType':UPLOAD_STUDY_TYPE"/>
                        <a id="studyChangeButton" class="upload" onclick="$j('#studyDiv').empty().slideUp('slow'); changeField('study-combobox', 'study')">Change</a>
                        <a style="margin-left: 32px;" id="studyBrowseButton" class="upload" onclick="generateBrowseWindow();">Browse</a>
                        <br/><br/>
                        <div id="studyNoFolderMessage" class="fieldError" style="display: none;"></div>
                        <div id="studyDiv" style="height: 200px; width: 540px; overflow: auto; display: none;">&nbsp;</div>
                    </td>
                </tr>
            </table>
            <div id="uploadAnalysisPane">
                <table class="uploadTable">
                    <tr>
                        <td width="10%">
                            Analysis Type to Upload:<br/>
                        </td>
                        <td width="90%">
                            <div id="dataTypeErrors">
                                <g:eachError bean="${uploadDataInstance}" field="dataType">
                                    <div class="fieldError"><g:message error="${it}"/></div>
                                </g:eachError>
                            </div>
                            <g:select name="dataType" name="dataType" noSelection="${['null':'Select...']}" from="${['GWAS':'GWAS','Metabolic GWAS':'GWAS Metabolomics','EQTL':'eQTL']}" optionKey="${{it.key}}" optionValue="${{it.value}}" value="${uploadDataInstance?.dataType}"/>
                            <a class="upload" href="#" onclick="downloadTemplate();">Download Template</a>
                        </td>
                    </tr>

                    <tr>
                        <td>
                            Analysis Name:
                        </td>
                        <td>
                            <div id="analysisNameErrors">
                                <g:eachError bean="${uploadDataInstance}" field="analysisName">
                                    <div class="fieldError"><g:message error="${it}"/></div>
                                </g:eachError>
                            </div>
                            <g:textField name="analysisName" style="width: 90%" value="${uploadDataInstance.analysisName}"/>
                        </td>
                    </tr>
                    <tr>
                        <td>Analysis Description:</td>
                        <td colspan="3">
                            <g:textArea name="description" style="width: 90%; height: 100px">${uploadDataInstance.description}</g:textArea>
                        </td>
                    </tr>
                </table>
            </div>

            <div id="uploadFilePane">
                <table class="uploadTable">
                    <tr>
                        <td width="10%">
                            File to Upload:
                        </td>
                        <td width="90%">
                            <div id="uploadFileErrors">
                                &nbsp;
                            </div>
                            <input type="file" id="uploadFile" name="uploadFile" style="border: 1px dotted #CCC" />
                        </td>

                    </tr>

                    <tr>
                        <td>
                            File Name:
                        </td>
                        <td>
                            <div id="uploadFileNameErrors">
                                <g:eachError bean="${uploadFileInstance}" field="displayName">
                                    <div class="fieldError"><g:message error="${it}"/></div>
                                </g:eachError>
                            </div>
                            <g:textField name="displayName" style="width: 90%" value="${uploadFileInstance?.displayName}"/>
                            <br/>
                            <div class="uploadMessage">If this is left blank, the original file name will be used.</div>
                        </td>
                    </tr>
                    %{--<tr>--}%
                        %{--<td>File Description:</td>--}%
                        %{--<td colspan="3">--}%
                            %{--<g:textArea name="fileDescription" style="width: 90%; height: 100px">${uploadFileInstance?.fileDescription}</g:textArea>--}%
                        %{--</td>--}%
                    %{--</tr>--}%
                </table>
            </div>

            <div class="buttonbar">
                <a class="button" id="enterMetadataButton" onclick="showDataUploadForm()">Enter metadata</a>
                <g:actionSubmit class="upload" value="Upload File" action="uploadFile" id="uploadFileButton" onclick="validateFile(event)"/>
                <a class="button" href="${createLink([action:'index',controller:'search'])}">Cancel</a>
            </div>
        </div>

        <div id="formPage2" class="formPage" style="display: none;">

            <div class="dataFormTitle" id="dataFormTitle2">Upload Data</div>
                <div style="position: relative; text-align:right;">
                    <a class="button" href="mailto:${grailsApplication.config.com.recomdata.dataUpload.adminEmail}">Email administrator</a>
                    <tmpl:/help/helpIcon id="1332"/>&nbsp;
                    <div class="uploadMessage">If you are unable to locate the relevant autocomplete fields, email the administrator by clicking the button above.</div>
                </div>

                <%-- Appalling hack from previous version - leaving this in until I can sort it properly --%>
                        <g:if test="${flash.message2}">
                            <div class="fieldError">${flash.message2}</div>
                        </g:if>
                        <table class="uploadTable">
                            <tr>
                                <td width="10%">
                                    File:
                                </td>
                                <td colspan="3">
                                    <g:if test="${uploadDataInstance.status == 'ERROR' || uploadDataInstance.status == 'NEW'}">
                                        <input type="file" id="file" name="file" style="border: 1px dotted #CCC" />
                                        <a class="upload" href="#" onclick="downloadTemplate();">Download Template</a>
                                    </g:if>
                                    <g:else>
                                        <i>This data set is ${uploadDataInstance.status} - the file cannot be changed.</i>
                                    </g:else>
                                    <br/>
                                    <i>Upload should be a tab-delimited plain text file</i>
                                </td>
                            </tr>

                            <tr class="borderbottom bordertop">
                                <td id="tagsLabel">
                                    Phenotype:
                                </td>
                                <td colspan="3">
                                    <tmpl:extDiseaseSearchField fieldName="tags" searchAction="extSearch" searchController="disease" values="${tags}"/>
                                    <%--<a id="tagsLink" class="upload" href="#">Add more Phenotypes/Tags</a>--%>
                                </td>
                            </tr>
                            <tr>
                                <td>Population:</td>
                                <td>
                                    <g:textField name="population" value="${uploadDataInstance.population}"/>
                                </td>
                                <td>Sample Size:</td>
                                <td>
                                    <g:textField name="sampleSize" value="${uploadDataInstance.sampleSize}"/>
                                </td>
                            </tr>
                            <tr>
                                <td>Tissue:</td>
                                <td>
                                    <g:textField name="tissue" value="${uploadDataInstance.tissue}"/>
                                </td>
                                <td>Cell Type:</td>
                                <td>
                                    <g:textField name="cellType" value="${uploadDataInstance.cellType}"/>
                                </td>
                            </tr>
                            <tr class="bordertop borderbottom">
                                <td id="platformLabel">
                                    Platform:
                                </td>
                                <td colspan="3">

                                    <div style="width: 100%" id="genotypePlatform-tags" class="tagBox" name="genotypePlatform">

                                        <g:each in="${genotypePlatforms}" var="value">
                                            <span class="tag" id="genotypePlatform-tag-${value.key}" name="${value.key}">${value.value}</span>
                                        </g:each>

                                    </div>
                                    <div class="breaker">&nbsp;</div>
                                    <div style="background-color: #E4E4E4; float:left; padding: 8px; border-radius: 8px;">
                                        <div style="float: left; font-style: italic; line-height: 32px; margin-right: 8px">Add new: </div>
                                        <div style="float: left; margin-right: 8px">
                                            <div class="textsmaller">Vendor</div>
                                            <g:select style="width: 400px" name="genotypePlatformVendor" noSelection="${['null':'Select...']}" from="${snpVendors}" onChange="loadPlatformTypes('genotypePlatform', 'SNP')"/>
                                        </div>
                                        <div style="float: left">
                                            <div class="textsmaller">Platform</div>
                                            <g:select style="width: 200px" name="genotypePlatformName" from="${snpVendors}" onchange="addPlatform('genotypePlatform')"/>
                                            <select id="genotypePlatform" name="genotypePlatform" multiple="multiple" style="display: none;">
                                                <g:each in="${genotypePlatforms}" var="value">
                                                    <option selected="selected" value="${value.key}">${value.value}</option>
                                                </g:each>
                                            </select>
                                        </div>
                                    </div>
                                    <div class="breaker">&nbsp;</div>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    Genome Version:
                                </td>
                                <td colspan="3">
                                    <g:select name="genomeVersion" noSelection="${['null':'Select...']}" from="${['HG18':'HG18','HG19':'HG19']}" optionKey="${{it.key}}" optionValue="${{it.value}}" value="${uploadDataInstance.genomeVersion}"/>
                                </td>
                            </tr>
                            <tr id="expressionPlatformRow" class="bordertop borderbottom">
                                <td>
                                    Expression Platform:
                                </td>
                                <td colspan="3">
                                    <div style="width: 100%" id="expressionPlatform-tags" class="tagBox" name="expressionPlatform">
                                        <g:each in="${expressionPlatforms}" var="value">
                                            <span class="tag" id="expressionPlatform-tag-${value.key}" name="${value.key}">${value.value}</span>
                                        </g:each>
                                    </div>
                                    <div class="breaker">&nbsp;</div>
                                    <div style="background-color: #E4E4E4; float:left; padding: 8px; border-radius: 8px;">
                                        <div style="float: left; font-style: italic; line-height: 32px; margin-right: 8px">Add new: </div>
                                        <div style="float: left; margin-right: 8px">
                                            <div class="textsmaller">Vendor</div>
                                            <g:select style="width: 400px" name="expressionPlatformVendor" noSelection="${['null':'Select...']}" from="${expVendors}" onChange="loadPlatformTypes('expressionPlatform', 'Gene Expression')"/>
                                        </div>
                                        <div style="float: left">
                                            <div class="textsmaller">Platform</div>
                                            <g:select style="width: 200px" name="expressionPlatformName" onchange="addPlatform('expressionPlatform')" from="${expVendors}"/>
                                            <select id="expressionPlatform" name="expressionPlatform" multiple="multiple" style="display: none;">
                                                <g:each in="${expressionPlatforms}" var="value">
                                                    <option selected="selected" value="${value.key}">${value.value}</option>
                                                </g:each>
                                            </select>
                                        </div>
                                    </div>
                                    <div class="breaker">&nbsp;</div>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    Model Name:
                                </td>
                                <td>
                                    <g:textField name="modelName" value="${uploadDataInstance.modelName}"/>
                                </td>
                                <td>
                                    Model Description:
                                </td>
                                <td>
                                    <g:textField name="modelDescription" value="${uploadDataInstance.modelDescription}"/>
                                </td>
                            </tr>
                            <tr class="borderbottom">

                                <td>
                                    Statistical Test:
                                </td>
                                <td>
                                    <g:textField name="statisticalTest" value="${uploadDataInstance.statisticalTest}"/>
                                </td>
                                <td>
                                    P-value cutoff &lt;=
                                </td>
                                <td>
                                    <g:textField name="pValueCutoff" value="${uploadDataInstance.pValueCutoff}"/>
                                </td>
                            </tr>
                            <tr class="borderbottom">
                                <td>
                                    Research Unit:
                                </td>
                                <td>
                                    <div style="width: 100%" id="researchUnit-tags" class="tagBox" name="researchUnit">
                                    <g:each in="${researchUnit}" var="value">
                                            <span class="tag" id="researchUnit-tag-${value.key}" name="${value.key}">${value.value}</span>
                                        </g:each>
                                    </div>
                                    <g:select style="width: 300px" name="researchUnitName" noSelection="${['null':'Select...']}" from="${ResearchUnits}" onchange="addResearchUnit('researchUnit')"/>
                                    <select id="researchUnit" name="researchUnit" multiple="multiple" style="display: none;">
                                                <g:each in="${researchUnit}" var="value">
                                                    <option selected="selected" value="${value.key}">${value.value}</option>
                                                </g:each>
                                            </select>


                                </td>
                                <td style="width:190px;">
                                    <input type="checkbox" style="width:20px;" name="sensitiveFlag" value="${uploadDataInstance.sensitiveFlag}" id="sensitiveFlag"  onclick="isDataSensitive();">Is Data Sensitive?
                                    </input>
                                </td>
                                <td>
                                    <g:textField name="sensitiveDesc" value="${uploadDataInstance.sensitiveDesc}"/>
                                </td>
                            </tr>
                        </table>
                        <div class="buttonbar">
                            <a class="button" onclick="showAnalysisForm()">Back</a>
                            <g:actionSubmit class="upload" value="Upload" action="upload"/>
                            <a class="button" href="${createLink([action:'index',controller:'GWAS'])}">Cancel</a>
                        </div>
                    </div>


                    <g:hiddenField name="id" value="${uploadDataInstance?.id}"/>

                </div>
            </g:uploadForm>
        </div>

        <div id="uploadSidebarWrapper">
            <div id="uploadSidebar">
                <div id="title-upload-target" class="ui-widget-header">
                    <h2 style="float:left" class="title">Upload target</h2>
                </div>
                <ul class="dynatree-container">
                    <li class="sidebarRadio" id="uploadAnalysisRadio">
                        <span class="dynatree-node dynatree-folder dynatree-exp-c dynatree-ico-cf">
                            <span class="dynatree-no-connector">

                            </span>
                            <a class="dynatree-title">Upload analysis data</a>
                        </span>
                    </li>
                    %{--<li class="sidebarRadio" id="uploadFileRadio">--}%
                        %{--<span class="dynatree-node dynatree-folder dynatree-exp-c dynatree-ico-cf">--}%
                            %{--<span class="dynatree-no-connector"></span>--}%
                            %{--<a class="dynatree-title">Upload file to Faceted Search</a>--}%
                        %{--</span>--}%
                    %{--</li>--}%
                    <li class="sidebarRadio" id="uploadFileDatasetExplorerRadio">
                        <span class="dynatree-node dynatree-folder dynatree-exp-c dynatree-ico-cf">
                            <span class="dynatree-no-connector"></span>
                            <a class="dynatree-title">Upload file to Dataset Explorer</a>
                        </span>
                    </li>
                </ul>
            </div>
            <div id="uploadSidebarDisabled" style="display: none;">
                Editing metadata for an existing upload.
            </div>
        </div>

		<!-- Browse dialog -->
		<div id="divBrowseStudies" title="Studies" style="display: none;">
		   	<img src="${resource(file:'ajax-loader.gif', dir:'images')}"/>
		</div>

	</body>
</html>