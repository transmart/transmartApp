			<h3>Load Favorite</h3><br />
			
			<g:if test="${favorites.size()==0}">
			   There are no saved searches!
			   <br />
			</g:if>
			<g:else>
				<table>
				<tr>
				   <td><h2>Name</h2></td>
				</tr>			
				<g:each in="${favorites}" var="f">
				    <tr id="favorites_${f.id}">
				    	<td>
				    	    <div>
				    			<a href="#" onclick="loadSearch(${f.id}); return false;"><div  title="${f.description}" id="labelSearchName_${f.id}" >${f.name}</div></a>
				    	    </div>
				    	    
				    	</td>
				    	<td>
				    	    <a href="#" onclick="showEditSearchDiv(${f.id}); return false;">Edit</a>
				    	    <a href="#" onclick="deleteSearch(${f.id},'${f.name}'); return false;">Delete</a>
				    	    
				    	</td>
				      </tr>
				      <tr>
				        <td colspan="2">
				      		<div id="editSearchDiv_${f.id}" style="display:none;background-color:white">
								Enter Name <input type="text" id="searchName_${f.id}" size="50" maxlength="50" value="${f.name}" /><br/><br/>
								Enter Description <textarea id="searchDescription_${f.id}" rows="5" cols="70">${f.description}</textarea><br/>
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
			<a href="#" onclick="jQuery.modal.close();return false;">Cancel</a>   
			
