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
	
	<script type="text/javascript">
		// id prefix for ajax window
	    var lkupWinId = null;
	
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

		function toggleExpType(selectItem) {
			var toggleDiv = document.getElementById('in_vivo_id')
			var cellLineDiv = document.getElementById('exp_cell_line_div')
			var selectVal = selectItem.value
			var selectText = selectItem.options[selectItem.selectedIndex].text
			
			// toggle mouse source			
			if(selectText.indexOf('in vivo')!=-1) 
				toggleDiv.style.display='inline';
			else 
				toggleDiv.style.display='none';

			// toggle established cell line
			if(selectText=='Established cell line') 
				cellLineDiv.style.display='inline';
			else 
				cellLineDiv.style.display='none';
		}

		function validate() {
			var errorMsg = "";
			var formName = "geneSignatureFrm";
			
			//species required
			var species = document.forms[formName].elements['speciesConceptCode.id'];	
			if(species.value=="null") errorMsg = "\n- Please select a relevant species";

			//tech platform required
			var techPlat = document.forms[formName].elements['techPlatform.id'];
			if(techPlat.value=="null") errorMsg = errorMsg + "\n- Please select a technology platform";
					
			if(errorMsg=="") return true;

			alert("Please correct the following errors:\n" + errorMsg);
			return false;
		}

		// show cell line lookup dialog
		function showCellLineLookup() {
			// must select a species first
			var species = document.forms['geneSignatureFrm'].elements['speciesConceptCode.id'];	
			if(species.value=="null") {
				alert("Please select a species before picking a cell line!");
				return false;
			}

			// pass species filter
			var lkupUrl = '/${grailsApplication.metadata['app.name']}/geneSignature/cellLineLookup/'+species.value;
			//alert("url: "+lkupUrl);
			lkupWinId = "lkup"+(new Date()).getTime();
			showDialog(lkupWinId, { title: 'Cell Line Lookup', url: lkupUrl })
		}

		// populate cell line controls from ajax selection
		function selectCellLine(clId, clText) {
			var clIdBox = document.forms['geneSignatureFrm'].elements['experimentTypeCellLine.id'];	
			clIdBox.value = clId;

			var clTextBox = document.forms['geneSignatureFrm'].elements['experimentTypeCellLineText'];	
			clTextBox.value = clText;
			
			// destroy ajax window
			var ajaxWin = Ext.getCmp(lkupWinId + '-win');
			ajaxWin.destroy();
		}		
		
	</script>
</head>

<body>

<div class="body">
	<!-- initialize -->
	<g:set var="gs" value="${wizard.geneSigInst.properties}" />
	
	<g:if test="${wizard.wizardType==0}"><h1>Gene Signature Create</h1></g:if>	
	<g:if test="${wizard.wizardType==1}"><h1>Gene Signature Edit: ${gs.name}</h1></g:if>	
	<g:if test="${wizard.wizardType==2}"><h1>Gene Signature Clone: ${gs.name}</h1></g:if>
		
	<!-- instructions -->	
	<g:render template="instructions" />
	<br>
	
	<g:form name="geneSignatureFrm" method="post">
	<g:hiddenField name="page" value="2" />
	
	<p style="font-weight: bold;">Page 2: Meta-Data:</p>
	<table class="detail">
		<tr class="prop">
			<td class="name">Source of list</td>
			<td class="value">			
				<g:select name="sourceConceptCode.id"
    				      from="${wizard.sources}"
    				      value="${gs.sourceConceptCode?.id}"
         				  noSelection="['null':'select source']"
         				  optionValue="codeName"
         				  optionKey="id"
         				  onChange="javascript: toggleOtherDiv(this, 'source_other_div');" />
				<!--  toggle source other div accordingly -->
				<g:if test="${gs.sourceConceptCode?.bioConceptCode=='OTHER'}">      				  	
				<div id="source_other_div" style="display: block;">
				</g:if>
				<g:else>			
				<div id="source_other_div" style="display: none;">	
				</g:else>					
					<label>please provide 'other' detail<g:requiredIndicator/>:</label>
					<br><g:textField name="sourceOther" value="${gs.sourceOther}" size="100%" maxlength="255" />
				</div>	
			</td>
		</tr>		
		<tr class="prop">
			<td class="name">Owner of data</td>
			<td class="value">
				<g:select name="ownerConceptCode.id"
    				      from="${wizard.owners}"
    				      value="${gs.ownerConceptCode?.id}"
         				  noSelection="['null':'select owner of the data']"
         				  optionValue="codeName"
         				  optionKey="id" />
			</td>			
		</tr>
		<tr class="prop">
			<td class="name">Stimulus</td>
			<td class="value">
				<table>				
					<tr>
						<td style="border: none; width; 33%;">i.e. LPS, polyIC, etc:</td><td style="border: none;"><g:textArea name="stimulusDescription" value="${gs.stimulusDescription}" rows="3" cols="85" /></td>
					</tr>
					<tr>						
						<td style="border: none; width; 33%;">Dose, units, and time:</td><td style="border: none;"><g:textField name="stimulusDosing" value="${gs.stimulusDosing}" size="67%" maxlength="255" /></td>
					</tr>
				</table>
			</td>
		</tr>
		<tr class="prop">
			<td class="name">Treatment</td>
			<td class="value">
				<table>			
					<tr>
						<td style="border: none; width; 33%;">Drug treatment used in assay:</td><td style="border: none;"><g:textArea name="treatmentDescription" value="${gs.treatmentDescription}" rows="6" cols="85" /></td>
						</td>
					</tr>
					<tr>						
						<td style="border: none; width; 33%;">Dose, units, and time:</td><td style="border: none;"><g:textField name="treatmentDosing" value="${gs.treatmentDosing}" size="67%" maxlength="255" /></td>
					</tr>
					<tr><td style="border: none; font-weight: bold; font-style: italic;" colspan=2>OR Enter:</td></tr>
					<tr>
						<td style="border: none; width; 33%;">Compound:</td>
						<td style="border: none;"><g:select name="treatmentCompound.id"
								    					from="${wizard.compounds}"
								    					value="${gs.treatmentCompound?.id}"
								         				noSelection="['null':'select compound']"
								         				optionValue="${{it?.getName()}}"
								         				optionKey="id" />
						</td>
					</tr>
					<tr>						
						<td style="border: none; width; 33%;">Protocol Number:</td><td style="border: none;"><g:textField name="treatmentProtocolNumber" value="${gs.treatmentProtocolNumber}" size="67%" maxlength="255" /></td>
					</tr>			
				</table>
			</td>
		</tr>
		<tr class="prop">
			<td class="name">PMIDs (comma separated)</td>
			<td class="value"><g:textField name="pmIds" value="${gs.pmIds}" size="67%" maxlength="255" /></td>
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
						<div id="mouse_source_div" style="display: inline;">For Mouse, enter source<g:requiredIndicator/>:						
						</g:if>   
						<g:else>
						<div id="mouse_source_div" style="display: none;">For Mouse, enter source<g:requiredIndicator/>:						
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
				<tr id="mouse_other_id" style="display: block;"><td">		
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
			<td class="name">Tissue Type</td>
			<td class="value">
				<g:select name="tissueTypeConceptCode.id"
  				      	from="${wizard.tissueTypes}"
  				      	value="${gs.tissueTypeConceptCode?.id}"
       				  	noSelection="['null':'select relevant tissue']"
       				  	optionValue="codeName"
       				  	optionKey="id" />
			</td>
		</tr>
		<tr class="prop">
			<td class="name">Experiment Type</td>
			<td class="value">
				<table>				
				<tr>
					<td style="border: none; width: 50%">					
						<g:select name="experimentTypeConceptCode.id"
  				      		from="${wizard.expTypes}"
  				      		value="${gs.experimentTypeConceptCode?.id}"
       				  		noSelection="['null':'select experiment type']"
       				  		optionValue="codeName"
       				  		optionKey="id" 
       				  		onChange="javascript:toggleExpType(this);" />
       				  		
						<!--  toggle established cell line accordingly -->
						<g:if test="${gs.experimentTypeConceptCode?.bioConceptCode=='ESTABLISHED'}">      				  	
						<div id="exp_cell_line_div" style="display: inline;">Enter cell line<g:requiredIndicator/>:						
						</g:if>   
						<g:else>
						<div id="exp_cell_line_div" style="display: none;">Enter cell line<g:requiredIndicator/>:						
						</g:else>	  		
       				  		<!-- cell line lookup support -->
							<g:hiddenField name="experimentTypeCellLine.id" value="${gs.experimentTypeCellLine? gs.experimentTypeCellLine.id : 'null'}" /> 							
							<g:if test="${gs.experimentTypeCellLine!=null}"><g:textField name="experimentTypeCellLineText" value="${gs.experimentTypeCellLine?.cellLineName + ' ('+gs.experimentTypeCellLine?.attcNumber+')'}" readonly="readonly" size="80%" /> </g:if>
							<g:else><g:textField name="experimentTypeCellLineText" readonly="readonly" size="80%" /> </g:else>
       				  		&nbsp;<a onclick="javascript:showCellLineLookup();"><img alt="Cell Line Lookup" src="${resource(dir:'images',file:'filter.png')}" /></a>       				  		       				  		       				  		
						</div>		
					</td>
				<tr>	
				<!--  toggle in vivo model accordingly -->
				<g:if test="${gs.experimentTypeConceptCode?.bioConceptCode=='IN_VIVO_ANIMAL' || gs.experimentTypeConceptCode?.bioConceptCode=='IN_VIVO_HUMAN'}">      				  								
				<tr id="in_vivo_id" style="display: inline;">
				</g:if>
				<g:else>	
				<tr id="in_vivo_id" style="display: none;">	
				</g:else>							
					<td style="border: none;">
						<label>For 'in vivo', describe model<g:requiredIndicator/>:</label><br><g:textField name="experimentTypeInVivoDescr" value="${gs.experimentTypeInVivoDescr}" size="100%" maxlength="255" />						
					</td>
				</tr>
				<tr><td style="border: none;"><label>If applicable, ATCC designation:</label><br><g:textField name="experimentTypeATCCRef" value="${gs.experimentTypeATCCRef}" size="100%" maxlength="255" /></td></tr>										
				</table>
			</td>
		</tr>
	</table>
	
	<div class="buttons">
		<g:actionSubmit class="previous" action="${(wizard.wizardType==1 || wizard.wizardType==2) ? 'edit1' : 'create1'}" value="Definition" />
		<g:actionSubmit class="next" action="${(wizard.wizardType==1 || wizard.wizardType==2) ? 'edit3' : 'create3'}" value="Next" onclick="return validate();" />
		<g:actionSubmit class="cancel" action="refreshSummary" onclick="return confirm('Are you sure you want to exit?')" value="Cancel" />
	</div>		
		
	</g:form>	
</div>
</body>
</html>
