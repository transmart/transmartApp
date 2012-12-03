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

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="genesigmain" />
	<g:if test="${wizard.wizardType==1}">
		<title>Gene Signature Edit</title>	
	</g:if>
	<g:else>
		<title>Gene Signature Create</title>	
	</g:else>
	<link rel="stylesheet" type="text/css" href="${resource(dir:'css/jquery/cupertino', file:'jquery-ui-1.8.18.custom.css')}">
    <script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery.min.js')}"></script>
	<script>jQuery.noConflict();</script> 
	<script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery-ui.min.js')}"></script>	
	<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.idletimeout.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.idletimer.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'sessiontimeout.js')}"></script>
	<script type="text/javascript">
		function validate() {
			var errorMsg = "";
			var formName = "geneSignatureFrm";
			
			// list name required
			if(document.geneSignatureFrm.name.value=="") {
				errorMsg="\n- You must specify a list name";
			}
			
			//species required
			var species = document.forms[formName].elements['speciesConceptCode.id'];	
			if(species.value=="null") errorMsg = errorMsg + "\n- Please select a relevant species";


			var speciesText = species.options[species.selectedIndex].text.toUpperCase();

			// if mouse, requires a source
			if(speciesText.indexOf('Mouse'.toUpperCase()) != -1)  {
				var speciesSource = document.forms[formName].elements['speciesMouseSrcConceptCode.id'];
				if(speciesSource.value=="null") errorMsg = errorMsg + "\n- Please select species source";
			}

			// if mouse is knockout or transgenic or other details must be filled
			if (speciesText == 'Mouse (knockout or transgenic)'.toUpperCase() || speciesText=='Mouse (other)'.toUpperCase() ) {
				var speciesDetail = document.forms[formName].elements['speciesMouseDetail'];	
				if(speciesDetail.value=="") errorMsg = errorMsg + "\n- Please enter species detail";
			}
				 				
			//tech platform required
			var techPlat = document.forms[formName].elements['techPlatform.id'];
			if(techPlat.value=="null") errorMsg = errorMsg + "\n- Please select a technology platform";
					
			//p-value cutoff required
			var cutoff = document.forms[formName].elements['pValueCutoffConceptCode.id'];
			if(cutoff.value=="null") errorMsg += "\n- Please select a p-value cutoff";

			//file schema
			var schema = document.forms[formName].elements['fileSchema.id'];
			if(schema.value=="null") errorMsg = errorMsg + "\n- Please select a file schema";

			//fold change metric
			var metricType = document.forms[formName].elements['foldChgMetricConceptCode.id'];
			if(metricType.value=="null") errorMsg = errorMsg + "\n- Please select a fold-change metric";

			// upload file
			<g:if test="${wizard.wizardType==0}">
			var geneSigData = jQuery("#genes").val();
			var geneSigFile = document.geneSignatureFrm.uploadFile.value;
			if((geneSigFile=="")&&(geneSigData=="")) errorMsg = errorMsg + "\n- Please select a file to upload with your gene signature or use the text area to specify gene signature items";
			</g:if>

			// if no errors, continue submission
			if(errorMsg=="") return true;

			alert("Please correct the following errors:\n" + errorMsg);
			return false;
		}	

		function speciesToggle(selectItem) {			
			var mouseSrc = document.getElementById('mouse_source_div')
			var moutseOther = document.getElementById('mouse_other_id')
			var selectVal = selectItem.value
			var selectText = selectItem.options[selectItem.selectedIndex].text
			
			// toggle mouse source
			//if(selectVal=='MOUSE_1' || selectVal=='MOUSE_2' || selectVal=='MOUSE_3' || selectVal=='MOUSE_4') 
			if(selectText.indexOf('Mouse')!=-1)
				mouseSrc.style.display='inline';
			else 
				mouseSrc.style.display='none';

			// toggle mouse other
			//if(selectVal=='MOUSE_3' || selectVal=='MOUSE_4') 
			if(selectText=='Mouse (knockout or transgenic)' || selectText=='Mouse (other)') 				
				moutseOther.style.display='block';
			else 
				moutseOther.style.display='none';					
		}

		function toggleFileUpload(){
			var geneSigData = jQuery("#genes").val();
			if (geneSigData==""){
				//jQuery("#uploadFile").removeAttr("disabled");
				//jQuery("#uploadFile").attr("title", "Choose file to upload");
			}else{
				//jQuery("#uploadFile").attr("disabled", "disabled");
				//jQuery("#uploadFile").attr("title", "Clear the text area above to be able to upload a file");
			}
		}
	</script>
</head>

<body>

<div class="body">
	<!-- initialize -->
	<g:set var="gs" value="${wizard.geneSigInst.properties}" />
	
	<!--  show message -->
    <g:if test="${flash.message}">
    	<div class="warning">${flash.message}</div>
    	<g:hasErrors bean="${wizard.geneSigInst}"><div class="errors"><g:renderErrors bean="${wizard.geneSigInst}" as="list" /></div></g:hasErrors>
    	<br>
    </g:if>
	
	<g:if test="${wizard.wizardType==0}"><h1>Gene Signature Create</h1></g:if>	
	<g:if test="${wizard.wizardType==1}"><h1>Gene Signature Edit: ${gs.name}</h1></g:if>	
	<g:if test="${wizard.wizardType==2}"><h1>Gene Signature Clone: ${gs.name}</h1></g:if>	
		
	<!-- instructions -->	
	<g:render template="instructions" />
	<br>

	<g:form name="geneSignatureFrm" enctype="multipart/form-data" method="post">
		<g:hiddenField name="page" value="1" />
	
		<!-- list definition block -->	
		<p style="font-weight: bold;">Page 1: Definition:</p>	
		<table class="detail">
			<tr class="prop">
				<td class="name">Signature/List Name<g:requiredIndicator/></td>
				<td class="value"><g:textField name="name" value="${gs.name}" size="100%" maxlength="100" /></td>
			</tr>
			<tr>
			<tr class="prop">
				<td class="name">Description</td>
				<td class="value"><g:textArea name="description" value="${gs.description}" rows="6" cols="68" /></td>
			</tr>
			<tr class="prop">
				<td class="name">Species<g:requiredIndicator/></td>
				<td class="value">			
					<table>
						<tr>
							<td style="border: none; width: 50%">					
								<g:select name="speciesConceptCode.id"
		    				      	from="${wizard.species}"
		    				      	value="${gs.speciesConceptCode?.id}"
		         				  	noSelection="['null':'select relevant species']"
		         				  	optionValue="codeName"
		         				  	optionKey="id" 
		         				  	onChange="javascript:speciesToggle(this);" />&nbsp;
								<!--  toggle mouse div accordingly -->
								<g:if test="${gs.speciesConceptCode?.bioConceptCode=='MOUSE_1' || gs.speciesConceptCode?.bioConceptCode=='MOUSE_2' || 
																				gs.speciesConceptCode?.bioConceptCode=='MOUSE_3' || gs.speciesConceptCode?.bioConceptCode=='MOUSE_4'}">      				  	
								<div id="mouse_source_div" style="display: inline;">For Mouse, enter source<g:requiredIndicator/></div>:						
								</g:if>   
								<g:else>
								<div id="mouse_source_div" style="display: none;">For Mouse, enter source<g:requiredIndicator/></div>:						
								</g:else>														
									<g:select name="speciesMouseSrcConceptCode.id"
			    				      	from="${wizard.mouseSources}"
			    				      	value="${gs.speciesMouseSrcConceptCode?.id}"
			    				      	noSelection="['null':'select source']"
			         				  	optionValue="codeName"
			         				  	optionKey="id" />
								</div>
							</td>
						</tr>
						<!--  toggle mouse other accordingly -->
						<g:if test="${gs.speciesConceptCode?.bioConceptCode=='MOUSE_3' || gs.speciesConceptCode?.bioConceptCode=='MOUSE_4'}">      				  								
						<tr id="mouse_other_id" style="display: block;">		
						</g:if>
						<g:else>		
						<tr id="mouse_other_id" style="display: none;">
						</g:else>
							<td style="border: none;">
								<label>Detail for 'knockout/transgenic' or 'other' mouse strain<g:requiredIndicator/>:</label>
								<br><g:textField name="speciesMouseDetail" value="${gs.speciesMouseDetail}" size="100%" maxlength="255" />
							</td>
						</tr>	
					</table>			
				</td>
			</tr>
			<tr class="prop">
				<td class="name">Technology Platform<g:requiredIndicator/></td>
				<td class="value">			
					<g:select name="techPlatform.id"
	    				      from="${wizard.platforms}"
	    				      value="${gs.techPlatform?.id}"
	         				  noSelection="['null':'select tech platform']"
	         				  optionValue="${{it?.vendor + ' - ' + it?.array + ' [' + it?.accession + ']'}}"
	         				  optionKey="id"
	         				  onChange="javascript: toggleOtherDiv(this, 'platform_other_div');" />
					<div id="platform_other_div" style="display: none;">				
						<label>please provide 'other' accession #<g:requiredIndicator/>:</label>
						<br><input type="text" name="techPlatformOther" size="100%" />
					</div>	
			</tr>	
			
			<tr class="prop">
				<td class="name">P-value Cutoff<g:requiredIndicator/></td>
				<td class="value">
					<g:select name="pValueCutoffConceptCode.id"
	    				      from="${wizard.pValCutoffs}"
	    				      value="${gs.pValueCutoffConceptCode?.id}"
	         				  noSelection="['null':'select p-value cutoff']"
	         				  optionValue="codeName"
	         				  optionKey="id" />
				</td>
			</tr>
		</table>
		<g:if test="${wizard.wizardType==1 || wizard.wizardType==2}">
			<table class="detail" style="width: 100%">
			<g:tableHeaderToggle label="Upload New File Only to Override Existing Items" divPrefix="file_info" />
				<tbody id="file_info_detail" style="display: none;">
		</g:if>
		<g:else>
			<table class="detail" style="width: 100%">
				<tbody id="file_info_detail">
		</g:else>
					<tr>
						<td colspan="2" class="name">Enter genes manually or copy and paste them in the box below</td>
					</tr>
					<tr>
						<td colspan="2" class="value">
							<g:textArea wrap="hard" name="genes" value="" rows="6" cols="85" onblur="toggleFileUpload();" value="${wizard.geneSigText}"></g:textArea>
						</td>
					</tr>
					<tr>
						<td colspan="2" style="font-weight: bold; font-size: 12px;">File Upload Information (tab delimited text only, no .xls Excel files):&nbsp;&nbsp;
							<a style="font-style:italic;" href="${resource(dir:'images',file:'gene_sig_samples.txt')}" target="_blank"><img alt="examples" src="${resource(dir:'images',file:'text.png')}" />&nbsp;See Samples</a>
						</td>
					</tr>
					<tr class="prop">
						<td class="name">File Information<g:requiredIndicator/></td>
						<td class="value">
							<table>
								<tr>
									<td style="width:25%; border: none;">File schema:</td>
									<td style="border: none;">
										<g:select name="fileSchema.id" from="${wizard.schemas}" value="${gs.fileSchema?.id}" optionValue="name" optionKey="id" /></td>
								</tr>
								<tr>
									<td style="width:25%; border: none;">Fold change metric:</td>
									<td style="border: none;">
										<g:select name="foldChgMetricConceptCode.id"
						    				      from="${wizard.foldChgMetrics}"
						    				      value="${gs.foldChgMetricConceptCode?.id}"
						         				  noSelection="['null':'select metric indicator']"
						         				  optionValue="codeName"
						         				  optionKey="id" />
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<tr class="prop">
						<td class="name">Upload File<g:if test="${wizard.wizardType==0}"><g:requiredIndicator/></g:if><br>(tab delimited text files only)</td>
						<td class="value"><input type="file" name="uploadFile" <g:if test="${wizard.wizardType==0}">value="${gs.uploadFile}"</g:if><g:else>value=""</g:else> size="100" /></td>
					</tr>
				</tbody>
			</table>
		<table>	
			<g:if test="${wizard.wizardType==1}">		
			<tr class="prop">
				<td class="name">Public?</td>
				<td class="value">			
					<g:radioGroup name="publicFlag" values="[true,false]" labels="['Yes','No']" value="${gs.publicFlag}" >					
						${it.radio}&nbsp;<g:message code="${it.label}" />&nbsp;
					</g:radioGroup>
				</td>
			</tr>	
			</g:if>			
			<tr>
				<g:if test="${wizard.wizardType==1}">
					<td style="font-weight: bold; font-style: italic;" colspan=2>Note, the creator of this signature was '${gs.createdByAuthUser?.userRealName}' on ${gs.dateCreated}</td>			
				</g:if>
				<g:else>
					<td style="font-weight: bold; font-style: italic;" colspan=2>
						Note, the creator of this signature will be '<sec:loggedInUserInfo field="userRealName"/>' at the current system time
					</td>			
				</g:else>
			</tr>								
		</table>
		
		<div class="buttons">
			<g:actionSubmit class="save" action="${(wizard.wizardType==1) ? 'update' : 'save'}" value="Save" onclick="return validate();" />
			<g:actionSubmit class="next" action="${(wizard.wizardType==1 || wizard.wizardType==2) ? 'edit2' : 'create2'}" value="Advanced" onclick="return validate();" />
			<g:actionSubmit class="cancel" action="refreshSummary" onclick="return confirm('Are you sure you want to exit?')" value="Cancel" />
		</div>			
	
		<br>
	</g:form>
</div>
</body>
</html>
