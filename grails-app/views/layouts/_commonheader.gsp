<table class="menuDetail" width="100%" style="border-bottom: 2px solid #ddd;">
	<tr>
		<th style="text-align: left;">
			<!-- menu links -->
			<table class="menuDetail" style="width: auto;">
		    	<tr>
	   				<g:if test="${'search'==app}"><th class="menuVisited">Search</th></g:if>
		   			<g:else><th class="menuLink"><g:link controller="search">Search</g:link></th></g:else>

			       	<g:if test="${'datasetExplorer'==app}"><th class="menuVisited">Dataset Explorer</th></g:if>
	       			<g:else><th class="menuLink"><g:link controller="secure">Dataset Explorer</g:link></th></g:else>
	       			
	   				<g:if test="${'sampleexplorer'==app}"><th class="menuVisited">Sample Explorer</th></g:if>
		   			<g:else><th class="menuLink"><g:link controller="sampleExplorer">Sample Explorer</g:link></th></g:else>	   
		   			    			
	   				<g:if test="${'genesignature'==app}"><th class="menuVisited">Gene Signature/Lists</th></g:if>
		   			<g:else><th class="menuLink"><g:link controller="geneSignature">Gene Signature/Lists</g:link></th></g:else>
		   			
		      		<sec:ifAnyGranted roles="ROLE_ADMIN">
	   					<g:if test="${'accesslog'==app}"><th class="menuVisited">Admin</th></g:if>
		   				<g:else><th class="menuLink"><g:link controller="accessLog">Admin</g:link></th></g:else>
		       		</sec:ifAnyGranted>
		       	<tr>
		 	</table>
		</th> 		 
	</tr>
</table>