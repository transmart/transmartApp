<div style="margin:5px;">
Therapeutic Areas by:
<g:select name="subcategoryid"
          from="${subcategories.sort()}"
          value="${currentsubcategoryid}"
          optionValue="${{it.name} }"
          optionKey="${{it.id} }"
          onchange="launchHomePage( this.value )" />       
<div id="homegraphs">
	<table style="min-height:250px;width:800;">
	<g:each in="${categories}" status="ti" var="cat">
	<tr>
	<td>
	<table style="height:270px;width:810px;">
	<tr><td align="center" colspan="2"><h2 align="center">${cat.name}</h2></td></tr>
	<tr><td style="text-align:center;vertical-align:middle;" id="${'cat_'+cat.id+'_div_s'}" class="pie-chart"><h2>Loading...</h2>
	</td>
	<td style="text-align:center;vertical-align:middle;"  id="${'cat_'+cat.id+'_div_a'}" class="pie-chart"><h2>Loading...</h2>
	</td></tr>
	</table>
	</td>
	</tr>
	</g:each>
	</table>
</div>

<br />
</div>
<div style="padding:10px;margin:5px; border:1px solid grey;">
<div id="homefavorites">
	<g:render template="/RWG/favorites"
		model="['favorites':favorites]" />
</div>
<g:each in="${categories}" status="ti" var="cat">
<script>
var sdiv='${"cat_"+cat.id+"_div_s"}';
var adiv='${"cat_"+cat.id+"_div_a"}';
getPieChartData(sdiv, ${cat.id}, ${currentsubcategoryid}, false, 'studies');
getPieChartData(adiv, ${cat.id}, ${currentsubcategoryid}, false, 'analyses');
</script>
</g:each>
</div>