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

<div style="background-color:white;height:100%;width:100%;">
	<br />
	<br />
	<font style="font: 12px tahoma,verdana,helvetica;color:#800080;font-weight:bold;">Recent Updates (click update for more info)</font>
	<br />
	<hr />
	<br />
	
	<g:if test="${newsUpdates != null && !newsUpdates.isEmpty()}">
		<g:each var="newsUpdate" in="${newsUpdates}" status="iterator">
		
			<a href="#" onClick="showNewsUpdateDetail('${newsUpdate.id}')">
				Data Set <i>${newsUpdate.dataSetName}</i> modified on <i><g:formatDate format="yyyy-MM-dd" date="${newsUpdate.updateDate}"></g:formatDate></i>  
			</a>
			
			<br />
			<br />
		</g:each>
	</g:if>
	<g:else>
		No updates available.
	</g:else>
	
	<br />
	<br />

	
</div>