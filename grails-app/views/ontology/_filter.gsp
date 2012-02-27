<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'datasetExplorer.css')}">
	
<g:formRemote name="ontTagFilterForm" id="ontTagFilterForm" url="[controller:'ontology',action:'ajaxOntTagFilter']" before="if(searchByTagBefore()==false) return false;" onSuccess="searchByTagComplete(e)">
	<table class="searchform" width="100%">
	<tr>
	<td valign="top">
	<b>Search:</b><br>
	<g:textField id="ontsearchterm" name="ontsearchterm" value="" size="10"/><br>
	</td>
	<td valign="top"><br>AND</td>
	<td valign="top">
	<b>Type:</b><br>
	<g:select class="searchform"
        name="tagtype" id="tagtype" from="${tagtypes}"
        onchange="changeType();${remoteFunction(
            controller:'ontology', 
            action:'ajaxGetOntTagFilterTerms', 
            params: '{tagtype:this.value}', 
            update:'tagtermdiv')}"
    ></g:select><br>
    	<div id="tagtermdiv">
    		<!-- <g:render template="depSelectTerm"  model="['tags': tags]"/>-->
    	</div>
    </td></tr>
	</table>
	<table width="100%">
	<tr><td colspan="3" align="center"><input id="ontSearchButton" type="SUBMIT" VALUE="SEARCH" class="searchform"><input type="reset" VALUE="CLEAR" onclick="clearSearch();" class="searchform"><br>
	<div class="searchform" id="searchresultstext"></div></td></tr></table>
	
</g:formRemote>
<script>
function clearSearch()
	{
	document.getElementById('searchresultstext').innerHTML='';
	document.getElementById('tagtermdiv').innerHTML='';	
	 ontFilterForm.setHeight(130);
	 ontFilterPanel.doLayout();
	}
function changeType()
{
 var tagtype=document.getElementById("tagtype");
   if(tagtype.selectedIndex==0)
   {
   	 ontFilterForm.setHeight(130);
   	}
   	else
   	{
   	ontFilterForm.setHeight(250);
   	}
   	ontFilterPanel.doLayout();
}
</script>