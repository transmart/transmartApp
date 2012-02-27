              					<g:if test="${flash.message}">
            <tr><td><div class="message">${flash.message}</div></td></tr>
            </g:if>
                        <td> <g:select class="addremoveselect" name="groupstoremove" from="${groupswithuser}" size="15" multiple="yes" optionKey="id" optionValue="name" /></td>
						  <td class="addremovebuttonholder">
							<button class="ltarrowbutton" onclick="${remoteFunction(action:'addUserToGroups',update:[success:'groups', failure:''],  params:'$(\'groupstoadd\').serialize()+\'&searchtext=\'+document.getElementById(\'searchtext\').value+\'&id=\'+document.getElementById(\'currentprincipalid\').value')}; return false;">&LT;&LT;Add</button><br>
							<button class="ltarrowbutton" onclick="${remoteFunction(action:'removeUserFromGroups',update:[success:'groups', failure:''],  params:'$(\'groupstoremove\').serialize()+\'&searchtext=\'+document.getElementById(\'searchtext\').value+\'&id=\'+document.getElementById(\'currentprincipalid\').value')}; return false;">Remove&GT;&GT;</button>
							</td>
							<td>
							<div id="addfrombox">
							<g:select class="addremoveselect" width="100px" size="15" name="groupstoadd" from="${groupswithoutuser}" multiple="yes" optionKey="id" optionValue="name" ></g:select></td>
							</div>
							</td>

