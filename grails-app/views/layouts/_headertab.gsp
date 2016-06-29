<g:if test="${id == app}"><th class="menuVisited">${title}</th></g:if>
<g:else><th class="menuLink"><g:link controller="${controller}">${title}</g:link></th></g:else>