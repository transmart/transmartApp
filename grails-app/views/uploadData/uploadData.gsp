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
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="shortctu icon" href="${resource(dir:'images',file:'searchtool.ico')}">
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
		
		<g:javascript library="prototype" />
		<script type="text/javascript" src="${resource(dir:'js', file:'ext/adapter/ext/ext-base.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'ext/ext-all.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'ext/miframe.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'searchcombobox.js')}"></script>
	    <script type="text/javascript" src="${resource(dir:'js', file:'picklist.js')}"></script>
	    <script type="text/javascript" src="${resource(dir:'js', file:'utilitiesMenu.js')}"></script>
	    <script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery-1.7.1.min.js')}"></script>
	    <script type="text/javascript">$j = jQuery.noConflict();</script>
	    <script type="text/javascript" src="${resource(dir:'js', file:'uploadData.js')}"></script>
		<script type="text/javascript" charset="utf-8">
		Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";

		// set ajax to 90*1000 milliseconds
		Ext.Ajax.timeout = 180000;

		// qtip on
		Ext.QuickTips.init();

		Ext.onReady(function(){			
		    var helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
		    var contact = '${grailsApplication.config.com.recomdata.searchtool.contactUs}';
		    var appTitle = '${grailsApplication.config.com.recomdata.searchtool.appTitle}';
		    var buildVer = 'Build Version: <g:meta name="environment.BUILD_NUMBER"/> - <g:meta name="environment.BUILD_ID"/>';
		     
			var viewport = new Ext.Viewport({
				layout: "border",
				items:[new Ext.Panel({                          
					   region: "center",
					   tbar: createUtilitiesMenu(helpURL, contact, appTitle,'${request.getContextPath()}', buildVer, 'utilities-div'), 
					   contentEl: "header-div"
				    })
		        ]
			});
			viewport.doLayout();
		});

		var IS_EDIT = ${uploadDataInstance?.id ? true : false};

		function downloadTemplate() {
			var type = $j('#dataType').val();
			window.location = "${createLink([action:'template',controller:'uploadData'])}" + "?type=" + type;
		}

		function loadPlatformTypes(field) {
			var targetField = $j('#' + field + 'Name');
			var sourceField = $j('#' + field + 'Vendor');
			var vendor = sourceField.val();
			fillSelectAjax(targetField, "${createLink([action:'platformsForVendor',controller:'bioAssayPlatform'])}", {vendor:vendor});
		}

		<g:if test="${study}">
		updateStudyTable(${study.id});
		</g:if>
		</script>
		<title>${grailsApplication.config.com.recomdata.searchtool.appTitle}</title>
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
			<g:uploadForm name="dataUpload" action="upload" method="post">
			<div id="formPage1" style="background-color: #EEE">
			<div class="dataFormTitle" id="dataFormTitle1">
				<g:if test="${uploadDataInstance?.id ? true : false}">
					Edit Metadata
				</g:if>
				<g:else>
					Upload Data
				</g:else>
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
							<g:eachError bean="${uploadDataInstance}" field="study">
								<div class="fieldError"><g:message error="${it}"/></div>
							</g:eachError>
							<tmpl:extSearchField fieldName="study.id" searchAction="extSearch" searchController="experiment" value="${study?.id}" label="${study?.title}"/>
							<br/><br/>
							<div id="studyDiv" style="height: 200px; width: 540px; overflow: auto; display: none;">&nbsp;</div>
						</td>
					</tr>
					
					<tr>
						<td>
							Analysis Type to Upload<br/>
						</td>
						<td>
							<g:select name="dataType" name="dataType" from="${['gwas':'GWAS','eqtl':'eQTL']}" optionKey="${{it.key}}" optionValue="${{it.value}}" value="${uploadDataInstance?.dataType}"/>
						</td>
					</tr>
				
					<tr>
						<td>
							Analysis Name:
						</td>
						<td>
							<g:eachError bean="${uploadDataInstance}" field="analysisName">
								<div class="fieldError"><g:message error="${it}"/></div>
							</g:eachError>
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
				
				<div class="buttonbar">
					<a class="button" onclick="showDataUploadForm()">Enter metadata</a>
					<a class="button" href="${createLink([action:'index',controller:'search'])}">Cancel</a>
				</div>
			</div>
			<div id="formPage2" style="background-color: #EEE; visibility:hidden;">
			<div class="dataFormTitle" id="dataFormTitle2">Upload Data</div>
				<table class="uploadTable">
					<tr>
						<td width="10%">
							File:
						</td>
						<td colspan="3">
							<input type="file" id="file" name="file" style="border: 1px dotted #CCC" onchange="verifyHeader()"/>
							<a class="upload" href="#" onclick="downloadTemplate();">Download Template</a>
						</td>
					</tr>
					<%-- Disabled instant check
					<tr>
						<td>
							&nbsp;
						</td>
						<td colspan="3">
							<div id="columnsAll">&nbsp;</div>
							<div id="columnsNotFound">&nbsp;</div>
							<br/>
						</td>
					</tr>
					 --%>
					
					<tr class="borderbottom bordertop">
						<td id="tagsLabel">
							Phenotype:
						</td>
						<td colspan="3">
							<tmpl:extTagSearchField fieldName="tags" searchAction="extSearch" searchController="disease" values="${tags}"/>
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
									<g:select style="width: 400px" name="genotypePlatformVendor" noSelection="${['null':'Select...']}" from="${vendors}" onChange="loadPlatformTypes('genotypePlatform')"/>
								</div>
								<div style="float: left">
									<div class="textsmaller">Platform</div>
									<g:select style="width: 200px" name="genotypePlatformName" onchange="addPlatform('genotypePlatform')"/>
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
							<g:select name="genomeVersion" from="${['HG18':'HG18','HG19':'HG19']}" optionKey="${{it.key}}" optionValue="${{it.value}}" value="${uploadDataInstance.genomeVersion}"/>
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
									<g:select style="width: 400px" name="expressionPlatformVendor" noSelection="${['null':'Select...']}" from="${vendors}" onChange="loadPlatformTypes('expressionPlatform')"/>
								</div>
								<div style="float: left">
									<div class="textsmaller">Platform</div>
									<g:select style="width: 200px" name="expressionPlatformName" onchange="addPlatform('expressionPlatform')"/>
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
					<tr>
						<td>
							Research Unit:
						</td>
						<td colspan="3">
							<g:textField name="researchUnit" value="${uploadDataInstance.researchUnit}"/>
						</td>
					</tr>
				</table>
				<div class="buttonbar">
					<a class="button" onclick="showAnalysisForm()">Back</a>
					<g:actionSubmit class="upload" value="Upload" action="upload"/>
					<a class="button" href="${createLink([action:'index',controller:'search'])}">Cancel</a>
				</div>
			</div>
			
			<g:hiddenField name="id" value="${uploadDataInstance?.id}"/>
			</g:uploadForm>
		</div>

	</body>
</html>