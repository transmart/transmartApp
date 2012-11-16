			<h3 id="loadSearchModalTitle"></h3><br />
			
			<g:if test="${favorites.size()==0}">
			   There are no saved searches!
			   <br />
			</g:if>
			<g:else>

			<div id="saved-filters-holder" style="height:500px; overflow:auto;">
			    <table>
					<g:each in="${favorites}" var="f">					
				        <tr id="filter_favorites_${f.id}" >
				        	<td>
						    	    <div id="staticSearchDiv_${f.id}" class="staticSearchDiv" >
						    	    	<div style="float: left;width:100px;margin:10px;">
							    				${f.createDt.format("MM/dd/yyyy")}
								    	</div>
						    	    	<div style="float: left;width:200px;margin:10px;" >
							    				<a href="#" id="linkSearchName_${f.id}" class="searchTooltip" onclick="loadSearch('${f.searchType}', ${f.id}); return false;"><span id="labelSearchName_${f.id}" >${f.name}</span></a>
							    											    				
								    	</div>
						    	    	<div style="float: left;width:40px;margin:10px;">
								    	    	<a href="#" onclick="showEditSearchDiv(${f.id}); return false;">Rename</a>
								    	</div>
						    	    	<div style="float: left;width:30px;margin:10px;">
								    	    	<a href="#" onclick="deleteSearch(${f.id}); return false;">Delete</a>							    	    
								    	</div>
						    	    </div>
		
						      		<div id="editSearchDiv_${f.id}" class="editSearchDiv modal-controls">
										<input type="text" id="searchName_${f.id}" size="75" maxlength="100" value="${f.name}" />
										<br /><br /><br />
										<a href="#" onclick="updateSearch(${f.id}); return false;">Save</a>&nbsp;   
										<a href="#" onclick="hideEditSearchDiv(${f.id}); return false;">Cancel</a>&nbsp;   
										<br />
						    	    	<div id="modal-status-message_${f.id}" style="display:none; padding:5px; margin-top:15px;"></div>
						    	    </div>
							</td>
						</tr>
					</g:each>
			    </table>
			    </div>
			</g:else>
			<br />
			<span class="modal-controls">
			<a href="#" onclick="jQuery.modal.close();return false;" class="actions" >Close</a>  
			</span>
			 
<script>	registerSearchTooltipEvents();
</script>			
