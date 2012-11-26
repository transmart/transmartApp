
<g:if test="${favorites.size()==0}">
   You have no ${title}.
   <br />
</g:if>
<g:else>
 	<h2 id="homeFavoritesTitle_${id}">${title}</h2>
    <table id="homefavoritesTable_${id}" class='CTAtable'>
        <tr>
        	<th style="width:100px">Create Date</th>
        	<th>Filter Name</th>      	
        </tr>
		<g:each in="${favorites}" var="f" status="i">					
	        <g:if test="${i<5}">	
	        
		        <tr id="${id}home_favorites_${f.id}" >
		        	<td>${f.createDt.format("MM/dd/yyyy")}</td>
					<td><a href="#" id="${id}homelinkSearchName_${f.id}" class="searchTooltip" onclick="loadSearch('${f.searchType}', ${f.id}); return false;">
							<span id="${id}home_labelSearchName_${f.id}" >${f.name}</span>
						</a>
					</td>
				</tr>
				
			</g:if>
		</g:each>
    </table>
    <g:if test="${favorites.size()>5}"><br />
	   <a href="#" onclick="openLoadSearchDialog(${isXT});">View All</a>
	</g:if>
</g:else>

<script>	
	registerSearchTooltipEvents();
    jQuery('#homefavoritesTable').find('tr:even').css({'background-color':'#efefef'})
    .end().find('tr:odd').css({'background-color':'#fff'});
</script>			
