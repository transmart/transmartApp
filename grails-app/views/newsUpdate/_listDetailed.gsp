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

<div style="background:#eee;height:100%;padding:5px; font:11px tahoma, arial, helvetica, sans-serif;">
	<table>
		<tr>
			<td style="background:#E8E8E8;text-align:right;">
				Records Affected : 
			</td>
			<td style="background:white;">
				${thisUpdate.rowsAffected}
			</td>
		</tr>
		<tr>
			<td style="background:#E8E8E8;text-align:right;">
				Updates run by user : 
			</td>
			<td style="background:white;">
				${thisUpdate.ranByUser}
			</td>
		</tr>		
		<tr>
			<td style="background:#E8E8E8;text-align:right;">
				Operation performed : 
			</td>
			<td style="background:white;">
				${thisUpdate.operation}
			</td>
		</tr>		
		<tr>
			<td style="background:#E8E8E8;text-align:right;">
				Dataset associated with changes : 
			</td>
			<td style="background:white;">
				${thisUpdate.dataSetName}
			</td>
		</tr>	
		<tr>
			<td style="background:#E8E8E8;text-align:right;">
				Date of update : 
			</td>
			<td style="background:white;">
				${thisUpdate.updateDate}
			</td>
		</tr>	
		<tr>
			<td style="background:#E8E8E8;text-align:right;">
				Comments : 
			</td>
			<td style="background:white;">
				${thisUpdate.commentField}
			</td>
		</tr>							
	</table>
</div>