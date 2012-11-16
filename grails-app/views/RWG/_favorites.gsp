
<g:if test="${favorites.size()==0}">
   You have no Favorite Filters saved.
   <br />
</g:if>
<g:else>
 	<h2>Saved Filters</h2>
    <table id="favoritesTable" class='CTAtable'>
        <tr>
        	<th style="width:100px">Create Date</th>
        	<th>Filter Name</th>      	
        </tr>
		<g:each in="${favorites}" var="f">					
	        <tr id="home_favorites_${f.id}" >
	        	<td>${f.createDt.format("MM/dd/yyyy")}</td>
				<td><a href="#" id="linkSearchName_${f.id}" class="searchTooltip" onclick="loadSearch(${f.id}); return false;">
						<span id="home_labelSearchName_${f.id}" >${f.name}</span>
					</a>
				</td>
			</tr>
		</g:each>
    </table>
</g:else>

<script>	
	registerSearchTooltipEvents();
</script>			
