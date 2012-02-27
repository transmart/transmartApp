	<g:if test="${flash.message}">
            <tr><td><div class="message">${flash.message}</div></td></tr>
            </g:if>
                <td> <g:select class="addremoveselect" name="groupstoremove" from="${secureObjectAccessList}" size="15" multiple="yes" optionKey="id" optionValue="principalAccessName" /></td>
		  <td class="addremovebuttonholder">
						<button class="ltarrowbutton" onclick="${remoteFunction(action:'addPrincipalToAccessList',update:[success:'groups', failure:''],  params:'$(\'groupstoadd\').serialize()+\'&searchtext=\'+document.getElementById(\'searchtext\').value+\'&secureobjectid=\'+document.getElementById(\'secureobjectid\').options[document.getElementById(\'secureobjectid\').selectedIndex].value+\'&accesslevelid=\'+document.getElementById(\'accesslevelid\').options[document.getElementById(\'accesslevelid\').selectedIndex].value')}; return false;">&LT;&LT;Add</button><br>
							<button class="ltarrowbutton" onclick="${remoteFunction(action:'removePrincipalFromAccessList',update:[success:'groups', failure:''],  params:'$(\'groupstoremove\').serialize()+\'&searchtext=\'+document.getElementById(\'searchtext\').value+\'&secureobjectid=\'+document.getElementById(\'secureobjectid\').options[document.getElementById(\'secureobjectid\').selectedIndex].value+\'&accesslevelid=\'+document.getElementById(\'accesslevelid\').options[document.getElementById(\'accesslevelid\').selectedIndex].value')}; return false;">Remove&GT;&GT;</button>
		</td>
	<td>
			<div id="addfrombox">
			<g:select class="addremoveselect" width="100px" size="15" name="groupstoadd" from="${userwithoutaccess}" multiple="yes" optionKey="id" optionValue="principalNameWithType" ></g:select></td>
		</div>
	</td>


