<div style="margin-left:10px; margin-right:10px; margin-bottom:15px;">

<h1>Faceted Search Home</h1>

	<div id="summary-chart-holder">
	<h2 style="margin-bottom:0">Summary Charts</h2>
			<span style="font-size: 0.8em;color: #222;padding-left: 10px;">Group by: <g:select name="subcategoryid"
	          from="${subcategories.sort({it.name})}"
	          value="${currentsubcategoryid}"
	          optionValue="${{it.name} }"
	          style="font-size:10px"
	          optionKey="${{it.id} }"
	          onchange="launchHomePage( this.value, jQuery('#charttype').val(), jQuery('#showAll').is(':checked') )"
	          id="subcategoryid" /> </span>
		<table>		
			<g:each in="${categories}" status="ti" var="cat">
			<tr>
				<td>
					<table style="border-collapse:collapse;">
					<tr>
						<td style="width:100%;" colspan="2">
							<h2 style="margin-bottom: 0;">${cat.name}</h2>
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
	
	
	<table style="width:100%">
	<tr><td style="width:50%">
		<div id="homefavorites" class='home-content-box' style="width:99%">
			<g:render template="/RWG/favorites"
				model="['favorites':favorites, 'title':'Saved Filters', 'id':'nonxt', 'isXT':false]" />
		</div>
	
	</td><td style="width:50%">
		<div id="savedCrossTrialAnalysis" class='home-content-box'>
			<g:render template="/RWG/favorites"
				model="['favorites':favoritesXT, 'title':'Saved Cross Trial Analyses', 'id':'xt', 'isXT':true]" />
		</div>
	</td></tr>
	</table>
	



	
	
	</div>
