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

<html>

<body>
<div id="summary">
	<div id="SummaryHeader"><span class="SummaryHeader">Please Select A Cell Line</span></div>

	<table class="trborderbottom" width="100%">
		<thead>
		<tr>
			<th>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
			<th>Name</th>
			<th style="white-space: nowrap;">ATCC Number</th>			
			<th>Species</th>			
			<th>Disease</th>		
		</tr>
		</thead>
	
		<tbody>
		<g:each in="${cellLines}" var="cl" status="i">
		<tr style="border-bottom:1px solid #CCCCCC;padding-botton:2px;">
			<td>&nbsp;<g:radio name="cellLine" value="${cl.id}" onclick="selectCellLine(${cl.id},'${cl.cellLineName +' ('+cl.attcNumber+')'}');" /></td>
	  		<td>${cl.cellLineName}</td>			
	  		<td>${cl.attcNumber}</td>			
	  		<td>${cl.species}</td>			
	  		<td>${cl.disease}</td>	
		</tr>
		</g:each>		
		</tbody>		
	</table>
	
</div>
</body>
</html>