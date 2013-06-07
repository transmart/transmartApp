<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
  
 
-->

<div style="background-color:#EEEEEE;height:100%;font: 12px tahoma,verdana,helvetica;text-align:center;">
	<br />
	<br />
	<br />
	<font style="font: 18px tahoma,verdana,helvetica;color:#800080;font-weight:bold;">Select a primary search filter</font>
	<hr style="width:30%;margin-left: auto;margin-right: auto;" />
	<br />
	<br />
	
	<table style="margin-left: auto;margin-right: auto;width:75%;">
		<tr>
			<td style="height:15px;vertical-align:middle;">
				<font style="font-size:150%">Search For Filter</font>
			</td>
		</tr>
		<tr>
			<td>
				&nbsp;
			</td>
		</tr>		
		<tr>
			<td>
				<div id="search-categories"></div>
			</td>
		</tr>
		
		<tr>
			<td>
				<div id="search-text"></div>
			</td>
		</tr>
	</table>
	
	
	<br />
	<br />
	<br />
	
	<div style="margin-left: auto;margin-right: auto;width:75%;text-align:left;">
		<font style="font-size:150%">Browse for filter</font>
	</div>

<br />

	<table style="width:75%;text-align:left;padding:0;margin-left: auto;margin-right: auto;">
			<tr>		
				<g:each var="term" in="${termsMap}" status="iterator">
					<td style="vertical-align:top;">	
						<g:render template="categoryList" model="[termName:term.key,termList:term.value]" />
					</td>
					<g:if test="${((iterator+1) % 3) == 0}">
						</tr>
						<tr>
							<td>
								&nbsp;
							</td>
						</tr>
						<tr>
					</g:if>
				</g:each>
			</tr>		
	</table>	
</div>