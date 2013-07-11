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
		function validate() {
			// list name required
			if(document.geneSignatureFrm.name.value=="") {
				alert("You must specify a list name");
				return false;
			}
			return true;
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
			<td class="value"><g:textArea name="description" value="${gs.description}" rows="6" cols="85" /></td>
		</tr>
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
		<g:actionSubmit class="next" action="${(wizard.wizardType==1 || wizard.wizardType==2) ? 'edit2' : 'create2'}" value="Meta-Data" onclick="return validate();" />
		<g:actionSubmit class="cancel" action="refreshSummary" onclick="return confirm('Are you sure you want to exit?')" value="Cancel" />
	</div>			

	<br>
	</g:form>
</div>
</body>
</html>
