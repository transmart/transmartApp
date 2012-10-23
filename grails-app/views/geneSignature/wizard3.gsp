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
		function clearMTC() {
			var bg = document.geneSignatureFrm.multipleTestingCorrection;
			bg[0].checked=false;
			bg[1].checked=false;
		}

		function validate() {

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

	<g:form name="geneSignatureFrm" enctype="multipart/form-data" method="post">
	<g:hiddenField name="page" value="3" />

	<p style="font-weight: bold;">Page 3: Analysis Meta-Data:</p>
	<table class="detail">
		<tr class="prop">
			<td class="name">Analysis Performed By</td>
			<td class="value"><g:textField name="analystName" value="${gs.analystName}" size="100%" maxlength="100" /></td>
		</tr>
		<tr class="prop">
			<td class="name">Normalization Method</td>
			<td class="value">
				<g:select name="normMethodConceptCode.id"
    				      from="${wizard.normMethods}"
    				      value="${gs.normMethodConceptCode?.id}"
         				  noSelection="['null':'select normalization method']"
         				  optionValue="codeName"
         				  optionKey="id"
         				  onchange="javascript: toggleOtherDiv(this, 'norm_method_other_div');" />
				<!--  toggle other div accordingly -->
				<g:if test="${gs.normMethodConceptCode?.bioConceptCode=='OTHER'}">
				<div id="norm_method_other_div" style="display: block;">
				</g:if>
				<g:else>
				<div id="norm_method_other_div" style="display: none;">
				</g:else>
					<label>Please provide 'other' detail<g:requiredIndicator/>:</label><br><g:textField name="normMethodOther" value="${gs.normMethodOther}" size="100%" maxlength="255" />
				</div>
			</td>
		</tr>
		<tr class="prop">
			<td class="name">Analysis Info</td>
			<td class="value">
				<table>
					<tr>
						<td style="width:20%; border: none;">Category:</td>
						<td style="border: none;">
							<g:select name="analyticCatConceptCode.id"
							      		from="${wizard.analyticTypes}"
							      		value="${gs.analyticCatConceptCode?.id}"
			     				  		noSelection="['null':'select category']"
			     				  		optionValue="${{(it?.bioConceptCode=='OTHER')? it?.codeName : it?.codeName + ' (' + it?.codeDescription + ')'}}"
			     				  		optionKey="id"
			     				  		onchange="javascript: toggleOtherDiv(this, 'analysis_cat_other_div');" />
							<!--  toggle other div accordingly -->
							<g:if test="${gs.analyticCatConceptCode?.bioConceptCode=='OTHER'}">
							<div id="analysis_cat_other_div" style="display: block;">
							</g:if>
							<g:else>
							<div id="analysis_cat_other_div" style="display: none;">
							</g:else>
								<label>Please provide 'other' detail<g:requiredIndicator/>:</label><br><g:textField name="analyticCatOther" value="${gs.analyticCatOther}" size="100%" maxlength="255" />
							</div>
						</td>
					</tr>
					<tr>
						<td style="width:20%; border: none;">Method:</td>
						<td style="border: none;">
							<g:select name="analysisMethodConceptCode.id"
			    				      from="${wizard.analysisMethods}"
			    				      value="${gs.analysisMethodConceptCode?.id}"
			         				  noSelection="['null':'select analysis method']"
			         				  optionValue="codeName"
			         				  optionKey="id"
			         				  onchange="javascript: toggleOtherDiv(this, 'analysis_method_other_div');" />
							<!--  toggle other div accordingly -->
							<g:if test="${gs.analysisMethodConceptCode?.bioConceptCode=='OTHER'}">
							<div id="analysis_method_other_div" style="display: block;">
							</g:if>
							<g:else>
							<div id="analysis_method_other_div" style="display: none;">
							</g:else>
								<label>Please provide 'other' detail<g:requiredIndicator/>:</label><br><g:textField name="analysisMethodOther" value="${gs.analysisMethodOther}" size="100%" maxlength="255" />
							</div>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr class="prop">
			<td class="name">Multiple Testing Correction Employed?</td>
			<td class="value">
				<g:radioGroup name="multipleTestingCorrection" values="[1,0]" labels="['Yes','No']" value="${gs.multipleTestingCorrection}" >
					${it.radio}&nbsp;<g:message code="${it.label}" />&nbsp;
				</g:radioGroup>
				&nbsp;<a href="javascript:clearMTC();">clear</a>
			</td>
		</tr>
	</table>
	<br>

	<div class="buttons">
		<g:actionSubmit class="previous" action="${(wizard.wizardType==1 || wizard.wizardType==2) ? 'edit2' : 'create2'}" value="Meta-Data" />
		<g:actionSubmit class="save" action="${(wizard.wizardType==1) ? 'update' : 'save'}" value="Save" onclick="return validate();" />
		<g:actionSubmit class="cancel" action="refreshSummary" onclick="return confirm('Are you sure you want to exit?')" value="Cancel" />
	</div>

	</g:form>

</div>
</body>
</html>
