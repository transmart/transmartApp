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

<table class="biobankResults">
	<tr>
		<th>
			Sample Tube ID
		</th>
		<th>
			Client Sample Tube ID
		</th>
		<th>
			Container ID
		</th>		
		<th>
			Source Type
		</th>		
		<th>
			Accession Number
		</th>
		<th>
			Import Date
		</th>						
	</tr>

	<g:each var="sample" in="${samples}" status="iterator">
		<g:if test="${sample}">
			<tr>
				<td class="${(iterator % 2) == 0 ? '' : 'altRow'}">
					${sample.id}
				</td>
				<td class="${(iterator % 2) == 0 ? '' : 'altRow'}">
					${sample.client_sample_tube_id}
				</td>
				<td class="${(iterator % 2) == 0 ? '' : 'altRow'}">
					${sample.container_id}
				</td>
				<td class="${(iterator % 2) == 0 ? '' : 'altRow'}">
					${sample.source_type}
				</td>
				<td class="${(iterator % 2) == 0 ? '' : 'altRow'}">
					${sample.accession_number}
				</td>
				<td class="${(iterator % 2) == 0 ? '' : 'altRow'}">
					${sample.import_date}
				</td>
			</tr>															
		</g:if>
	</g:each>
</table>