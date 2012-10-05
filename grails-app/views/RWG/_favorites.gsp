			<h3>My Favorites</h3>
			
			<g:if test="${favorites.size()==0}">
			   You have no Favorite Filters saved.
			   <br />
			</g:if>
			<g:else>
    			<h2>Click on name of favorite to load the saved faceted search</h2>
			    <table id="favoritesTable">
			        <tr>
			        	<td>
					    	    <div>
					    	    	<div style="float: left;width:100px;margin:5px;font-weight:bold">
						    				Create Date
							    	</div>
					    	    	<div style="float: left;width:200px;margin:5px;font-weight:bold">
					    	    			Name
							    	</div>
					    	    </div>
			        	</td>			        	
			        </tr>
					<g:each in="${favorites}" var="f">					
				        <tr id="home_favorites_${f.id}" >
				        	<td>
						    	    <div class="staticSearchDiv" >
						    	    	<div style="float: left;width:100px;margin:5px;">
							    				${f.createDt.format("MM/dd/yyyy")}
								    	</div>
						    	    	<div style="float: left;width:200px;margin:5px;">
							    				<a href="#" onclick="loadSearch(${f.id}); return false;"><span id="home_labelSearchName_${f.id}" >${f.name}</span></a>
						    	    </div>
							</td>
						</tr>
					</g:each>
			    </table>
			</g:else>
			<br />
		
			
