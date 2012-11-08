<div style="margin-left:10px; margin-right:10px;">

<h1>Faceted Search Home</h1>

	<div id="homefavorites" class='home-content-box' style="margin-right:20px">
		<g:render template="/RWG/favorites"
			model="['favorites':favorites]" />
	</div>

	<div id="savedCrossTrialAnalysis" class='home-content-box'>
	<h2>Load Saved Cross Trial Analysis</h2>
		<p>You have no Cross Trial Analyses saved</p>
	</div>

<br />

<h1>Summary Charts</h1>

Group by: <g:select name="subcategoryid"
          from="${subcategories.sort({it.name})}"
          value="${currentsubcategoryid}"
          optionValue="${{it.name} }"
          optionKey="${{it.id} }"
          onchange="launchHomePage( this.value, jQuery('#charttype').val(), jQuery('#showAll').is(':checked') )"
          id="subcategoryid" /> 
     
     <br />
     <br />
     
	<table>		
		<g:each in="${categories}" status="ti" var="cat">
		<tr>
			<td>
				<table style="border-collapse:collapse;">
				<tr>
					<td style="width:100%;" colspan="2">
						<h2>${cat.name}</h2>
					</td>
				</tr>
				<tr>
					<td style="text-align:center;vertical-align:middle;" id="${'cat_'+cat.id+'_div_s'}" class="pie-chart">
						<h2>Loading...</h2>
					</td>
					<td style="text-align:center;vertical-align:middle;" id="${'cat_'+cat.id+'_div_a'}" class="pie-chart">
						<h2>Loading...</h2>
					</td>
				</tr>
				</table>
			</td>
		</tr>
		</g:each>
	</table>

	<g:each in="${categories}" status="ti" var="cat">
	<script>
	var sdiv='${"cat_"+cat.id+"_div_s"}';
	var adiv='${"cat_"+cat.id+"_div_a"}';
	getPieChartData(sdiv, ${cat.id}, ${currentsubcategoryid}, false, 'studies', null ,[{ddid:${currentsubcategoryid}, ddname:'${currentsubcategoryname}', color:null}]);
	getPieChartData(adiv, ${cat.id}, ${currentsubcategoryid}, false, 'analyses', null,[{ddid:${currentsubcategoryid}, ddname:'${currentsubcategoryname}', color:null}]);
	</script>
	</g:each>
	
	</div>
