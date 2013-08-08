<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
 
-->

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