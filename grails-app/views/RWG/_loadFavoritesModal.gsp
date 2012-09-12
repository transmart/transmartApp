			<h3>Load Favorite</h3><br />
			
			<g:if test="${favorites.size()==0}">
			   There are no saved searches!
			   <br />
			</g:if>
			<g:else>
    			<h2>Click on name of favorite to load the saved faceted search</h2>
			    <table id="favoritesTable">
					<g:each in="${favorites}" var="f">
				        <tr id="favorites_${f.id}" >
				        	<td>
						    	    <div id="staticSearchDiv_${f.id}" class="staticSearchDiv" >
						    	    	<div style="float: left;width:200px;margin-bottom:10px;">
							    				<a href="#" onclick="loadSearch(${f.id}); return false;"><span  title="${f.description}" id="labelSearchName_${f.id}" >${f.name}</span></a>
								    	</div>
						    	    	<div style="float: left;width:50px">
								    	    	<a href="#" onclick="showEditSearchDiv(${f.id}); return false;">Edit</a>
								    	</div>
						    	    	<div style="float: left;width:50px">
								    	    	<a href="#" onclick="deleteSearch(${f.id}); return false;">Delete</a>							    	    
								    	</div>
						    	    </div>
		
						      		<div id="editSearchDiv_${f.id}" class="editSearchDiv">
										Enter Name <input type="text" id="searchName_${f.id}" size="50" maxlength="50" value="${f.name}" /><br/><br/>
										Enter Description <textarea id="searchDescription_${f.id}" rows="5" cols="60">${f.description}</textarea><br/>
										<br/>
										<a href="#" onclick="updateSearch(${f.id}); return false;">Save</a>&nbsp;   
										<a href="#" onclick="hideEditSearchDiv(${f.id}); return false;">Cancel</a>&nbsp;   
						    	    
						    	    </div>
							</td>
						</tr>
					</g:each>
			    </table>
			</g:else>
			<br />
			<a href="#" onclick="jQuery.modal.close();return false;" class="actions" >Close</a>   
			
