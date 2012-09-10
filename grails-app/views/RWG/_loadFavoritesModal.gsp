			<h3>Load Favorite</h3><br />
			<table>
			<tr>
			   <td><h2>Favorite Name</h2></td>
			</tr>
			<g:each in="${favorites}" var="f">
			    <tr id="favorites_${f.id}">
			    	<td>
			    	    <div title="${f.description}">
			    			<a href="#" onclick="loadSearch(${f.id}); return false;">${f.name}</a>
			    	    </div>
			    	</td>
			    	<td>
			    	    <a href="#" onclick="deleteSearch(${f.id}); return false;">Delete</a>
			    	</td> 
			    </tr>
			</g:each>
			</table>
			<br />
			<a href="#" onclick="jQuery.modal.close();return false;">Cancel</a>   
			
