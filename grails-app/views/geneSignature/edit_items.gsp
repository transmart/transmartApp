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
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/> 
    <meta name="layout" content="genesigmain" />
    <title>Edit Gene Signature Items</title>
	
	<script type="text/javascript">

		// hide inidcated row
		function removeNewItem(rowNum) {
			var rowId = "new_item_"+rowNum;
			var geneId = "biomarker_"+rowNum;
			var probesetId = "probeset_"+rowNum;
			var metricId = "foldChgMetric_"+rowNum;

			// elements			
			var rowItem = document.getElementById(rowId);
			var geneItem = document.getElementById(geneId);
			var probesetItem = document.getElementById(probesetId);
			var metricItem = document.getElementById(metricId);
					
			// remove and reset
			geneItem.value="";
			probesetItem.value="";
			metricItem.value=""
			rowItem.style.display="none";
		}
		
	</script>	
 </head>
 
 <body>
 <div class="body">  	
    <!-- user message -->
   	<g:if test="${flash.message}">${flash.message}<br></g:if>
      
    <h1><g:link action="list">Gene Signature List</g:link> > Gene Signature Items Edit: '${gs.name}'</h1>
    
	<table class="detail" style="width: 100%">
		<g:tableHeaderToggle label="Gene Signature Info" divPrefix="${gs.id}_general" colspan="2" />
	
		<tbody id="${gs.id}_general_detail" style="display: none;">
			<tr class="prop">
				<td valign="top" class="name">Name:</td>
				<td valign="top" class="value">${gs.name}</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name">Description:</td>
				<td valign="top" class="value">${gs.description}</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name">Public Status:</td>
				<td valign="top" class="value">${gs.publicFlag ? 'Public':'Private'}</td>
			</tr>		
			<tr class="prop">
				<td valign="top" class="name">Author:</td>
				<td valign="top" class="value">${gs.createdByAuthUser?.userRealName}</td>
			</tr>		
			<tr class="prop">
				<td valign="top" class="name">Create Date:</td>
				<td valign="top" class="value">${gs.dateCreated}</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name">Modified By:</td>
				<td valign="top" class="value">${gs.modifiedByAuthUser?.userRealName}</td>
			</tr>		
			<tr class="prop">
				<td valign="top" class="name">Modified Date:</td>
				<td valign="top" class="value"><g:if test="${gs.modifiedByAuthUser!=null}">${gs.lastUpdated}</g:if></td>
			</tr>		
		</tbody>
	</table>    
   	<br>
  
 	<g:form frm="geneSignatureItemFrm" method="post">   	
	<g:hiddenField name="id" value="${gs.id}" />	 	 
   	
   	<!-- existing items -->
   	<table class="detail">    	
       	<thead>
           	<tr>
           		<th>#</th>   	   
       	       	<g:if test="${gs?.fileSchema.id!=3}"> <th>Gene Symbol</th> </g:if>
       	       	<g:if test="${gs?.fileSchema.id==3}"> <th>Probeset ID</th> </g:if>                	       
           		<g:if test="${gs.foldChgMetricConceptCode?.bioConceptCode!='NOT_USED'}"><th>Fold-Change Metric</th></g:if>
               	<th style="text-align: center;">Delete</th>
        	</tr>
       	</thead>
      	<tbody>       		
      		<g:each var="item" in="${gs.geneSigItems}" status="i">
      		<tr>
      			<td style="color: gray;">${i+1}</td>
				<g:if test="${gs?.fileSchema.id!=3}"> <td>${item.bioMarker?.name}</td> </g:if>
				<g:if test="${gs?.fileSchema.id==3}"> <td>${item.probeset?.name}</td> </g:if>
            	<g:if test="${gs.foldChgMetricConceptCode?.bioConceptCode!='NOT_USED'}"><td>${item.foldChgMetric}</td></g:if>  								
				<td style="text-align: center;"><input type="checkbox" name="delete" value="${item.id}" /></td>
			</tr>		     
		</g:each>			
		</tbody>
    </table>       	
     
  	<!-- new items -->
  	<br>
    <table class="detail">
		<g:tableHeaderToggle label="Expand to Add Items" divPrefix="${gs.id}_new_items" colSpan="${(gs.foldChgMetricConceptCode.bioConceptCode!='NOT_USED') ? '4' : '3'}" />
		<tbody id="${gs.id}_new_items_detail" style="display: none;">
       		<tr id="new_header">
            	<th style="text-align: center;">#</th>
        	    <g:if test="${gs?.fileSchema.id!=3}"> <th style="text-align: center;">Gene Symbol</th> </g:if>  
        	    <g:if test="${gs?.fileSchema.id==3}"> <th style="text-align: center;">Probeset ID</th> </g:if>     
              	<g:if test="${gs.foldChgMetricConceptCode?.bioConceptCode!='NOT_USED'}"><th style="text-align: center;">Fold-Change Metric</th></g:if>
         	    <th style="text-align: center;">Remove</th>
         	</tr>		
          
		<!-- hidden items for adding -->	
		<g:set var="n" value="${0}"/>
		<g:while test="${n < 10}">
		    <%n++%>				
       		<tr id="new_item_${n}">
      			<td style="color: gray;">${n}_a</td>
      			
      			<!-- check if coming from an error -->
      			<g:if test="${errorFlag}">
      				<g:if test="${gs?.fileSchema.id!=3}">
						<td style="text-align: center;"><g:textField name="biomarker_${n}" value="${params.get('biomarker_'+n)}" maxlength="25" /></td>
					</g:if>
					<g:if test="${gs?.fileSchema.id==3}">
						<td style="text-align: center;"><g:textField name="probeset_${n}" value="${params.get('probeset_'+n)}" maxlength="25" /></td>
        	    	</g:if>
        	    	<g:if test="${gs.foldChgMetricConceptCode?.bioConceptCode!='NOT_USED'}"><td><g:textField name="foldChgMetric_${n}" value="${params.get('foldChgMetric_'+n)}" maxlength="20" /></td></g:if>				
      			</g:if>
      			<g:else>  
      				<g:if test="${gs?.fileSchema.id!=3}">    		
						<td><g:textField name="biomarker_${n}" maxlength="25" /></td>
					</g:if>
					<g:if test="${gs?.fileSchema.id==3}">
						<td><g:textField name="probeset_${n}" maxlength="25" /></td>
					</g:if>
            		<g:if test="${gs.foldChgMetricConceptCode?.bioConceptCode!='NOT_USED'}"><td><g:textField name="foldChgMetric_${n}" maxlength="20" /></td></g:if>
      			</g:else>
	
				<td style="text-align: center;"><img alt="remove item" onclick="javascript:removeNewItem(${n});" src="${resource(dir:'images',file:'remove.png')}" /></td>
			</tr>
		</g:while>
		</tbody>     
	</table>
	
	<div class="buttons">
		<g:actionSubmit class="save" action="addItems" value="Add Items" />
		<g:actionSubmit class="delete" action="deleteItems" value="Delete Checked" onclick="return confirm('Are you sure you want to delete these items?')" />
		<g:actionSubmit class="cancel" action="refreshSummary" onclick="return confirm('Are you sure you want to exit?')" value="Cancel" />
	</div>	   	
   	   	
   	</g:form>
</div>
</body>
</html>

