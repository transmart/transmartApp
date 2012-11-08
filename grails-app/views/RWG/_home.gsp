
	
<div style="margin-left:10px;">

<h1>Faceted Search Home</h1>


<div id="homefavorites">
	<g:render template="/RWG/favorites"
		model="['favorites':favorites]" />
</div>

<br />

</div>
<!-- <div id="homegraphs" style="margin:0px;padding:0px;">-->
	

	<div align="center" style="margin-left:-1px;" class="ui-widget-header">
	<h1 style="padding:6px;text-align:left;vertical-align:middle;" class="bigtitle">Summary Charts</h1></div>
	<div style="padding:5px;">
	<br />
Show (<g:checkBox name="showAll" id="showAll" checked="${showAll}"  onchange="launchHomePage( jQuery('#subcategoryid').val(), jQuery('#charttype').val(), this.checked )"/>&nbsp;All Areas) by
<g:select name="subcategoryid"
          from="${subcategories.sort({it.name})}"
          value="${currentsubcategoryid}"
          optionValue="${{it.name} }"
          optionKey="${{it.id} }"
          onchange="launchHomePage( this.value, jQuery('#charttype').val(), jQuery('#showAll').is(':checked') )"
          id="subcategoryid" /> 
     
     <br />
     <br />
    </div>
	<table style="height:285px;width:100%;border-collapse:collapse;margin-left:-1px;">		
	<g:each in="${categories}" status="ti" var="cat">
	<tr>
	<td>
	<table style="border-collapse:collapse;">
	<tr>
	<td align="center" style="width:100%;" colspan="2" class="ui-widget-header"><h2 style="padding:6px;text-align:left;vertical-align:middle;" class="title">${cat.name}</h2></td>
	</tr>
	<tr>
	<td style="text-align:center;vertical-align:middle;" id="${'cat_'+cat.id+'_div_s'}" class="pie-chart"><h2>Loading...</h2></td>
	<td style="text-align:center;vertical-align:middle;" id="${'cat_'+cat.id+'_div_a'}" class="pie-chart"><h2>Loading...</h2></td>
	</tr>
	</table>
	</td>
	</tr>
	</g:each>
	</table>
<!-- </div>-->

<g:each in="${categories}" status="ti" var="cat">
<script>
var sdiv='${"cat_"+cat.id+"_div_s"}';
var adiv='${"cat_"+cat.id+"_div_a"}';
getPieChartData(sdiv, ${cat.id}, ${currentsubcategoryid}, false, 'studies', null ,[{ddid:${currentsubcategoryid}, ddname:'${currentsubcategoryname}', color:null}]);
getPieChartData(adiv, ${cat.id}, ${currentsubcategoryid}, false, 'analyses', null,[{ddid:${currentsubcategoryid}, ddname:'${currentsubcategoryname}', color:null}]);
</script>
</g:each>
