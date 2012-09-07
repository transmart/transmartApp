		<!-- Load load favorites modal content -->
		<div id="load-modal-content" style="display:none;">
			<h3>Load Favorite</h3><br />
			<table>
			<tr>
			   <td><h2>Favorite Name</h2></td>
			</tr>
			<g:each in="${getFavorites()}" var="f">
			    <tr>
			    	<td>
			    	    <div title="${f.description}">
			    				<a href="#" onclick="loadSearch(${f.id}); return false;">${f.name}</a>&nbsp;   
			    	    </div>
			    	</td> 
			    </tr>
			</g:each>
			</table>
			<br />
			<a href="#" onclick="jQuery.modal.close();return false;">Cancel</a>   
			
		</div>