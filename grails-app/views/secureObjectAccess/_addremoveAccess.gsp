              					<g:if test="${flash.message}">
            <td colspan=3><div class="message">${flash.message}</div></td>
            </g:if>
            <g:else>
                        <td> <g:select class="addremoveselect" name="sobjectstoremove" from="${secureObjectAccessList}" size="15" multiple="yes" optionKey="id" optionValue="objectAccessName" /></td>

						  <td class="addremovebuttonholder">
							<button class="ltarrowbutton" onclick="${remoteFunction(action:'addSecObjectsToPrincipal',update:[success:'permissions', failure:''],  params:'$(\'sobjectstoadd\').serialize()+\'&searchtext=\'+document.getElementById(\'searchtext\').value+\'&id=\'+document.getElementById(\'currentprincipalid\').value+\'&accesslevelid=\'+document.getElementById(\'accesslevelid\').options[document.getElementById(\'accesslevelid\').selectedIndex].value')}; return false;">&LT;&LT;Add</button><br>
							<button class="ltarrowbutton" onclick="${remoteFunction(action:'removeSecObjectsFromPrincipal',update:[success:'permissions', failure:''],  params:'$(\'sobjectstoremove\').serialize()+\'&searchtext=\'+document.getElementById(\'searchtext\').value+\'&id=\'+document.getElementById(\'currentprincipalid\').value+\'&accesslevelid=\'+document.getElementById(\'accesslevelid\').options[document.getElementById(\'accesslevelid\').selectedIndex].value')}; return false;">Remove&GT;&GT;</button>
							</td>
							<td>
							<div id="addfrombox">
							<g:select class="addremoveselect" width="100px" size="15" name="sobjectstoadd" from="${objectswithoutaccess}" multiple="yes" optionKey="id" optionValue="displayName" ></g:select></td>
							</div>
							</td>
			</g:else>


