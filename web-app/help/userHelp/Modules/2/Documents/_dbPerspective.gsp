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

<g:form name="frmDashboard" action="index">
	<table width="100%" border="0" cellpadding="0" cellspacing="1" bgcolor="#455f78">
		<tr>
 			<td colspan=2 style="color: #FFF; font-size: 16px; padding-top: 10px; padding-left: 10px; padding-bottom: 10px; font-weight: bold;">
				<g:ifAnyGranted role="ROLE_DEMO">Perspective [ PHI ]</g:ifAnyGranted>
				<g:ifNotGranted role="ROLE_DEMO">Perspective [ ${db.filterLabel} ]</g:ifNotGranted>
			</td>


<!-- ************************************** -->
<!-- Role of user determines the help topic to display -->

<g:ifAnyGranted role="ROLE_ADMIN,ROLE_MANAGER">
<% topicID = "1092" %>
</g:ifAnyGranted>

<g:ifAnyGranted role="ROLE_PRACTICE_MANAGER">
<% topicID = "1063" %>
</g:ifAnyGranted>

<!-- Section Chief has provider role -->
<g:ifAnyGranted role="ROLE_PROVIDER">
<% topicID = "1034" %>
</g:ifAnyGranted>

<!-- This adds the Help button -->
<td><A HREF='JavaScript:D2H_ShowHelp(<%=topicID%>,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP )'><img src="/quality/images/info_button_small.gif" border=0 width=18pt style="margin-top:10pt;margin-bottom:10pt;margin-right:18pt;float:right"></a>
</td>
<!-- ************************************** -->

		
	</tr>

		<tr bgcolor="#eeeeee" style="font-weight: bold;">		
			<!--  practice filter (admin only) -->
			<g:if test="${db.viewType.equals(db.USER_VIEW_ADMIN)}">			
			<td style="padding: 5px;">Practice:&nbsp;
				<g:select optionKey="id" from="${db.practices}" value="${db.selectPracticeId}" 
				          name="practiceId" optionValue="${{it.clinicName}}" noSelection="['':'All']" />	
				<span class="button">&nbsp;<g:actionSubmit action="practiceView" value="Filter" /></span>			          				          			
			</td>
			
			<!--  section filter (admin) -->
			<td style="padding: 5px;">Section:&nbsp;
				<g:select optionKey="id" from="${db.sections}" value="${db.selectSectionId}" 
				          name="sectionId" optionValue="${{it.sectionName}}" noSelection="['':'All']" />	
				<span class="button">&nbsp;<g:actionSubmit action="sectionView" value="Filter" /></span>		
			</td>		
			</g:if>
								
			<!-- provider filter (admin, section chief, and office managers) -->
			<td style="padding: 5px;">Provider:&nbsp;
			<g:if test="${db.viewType.equalsIgnoreCase(db.USER_VIEW_MANAGER)}">			
				<g:select optionKey="id" from="${db.providers}" value="${db.selectProviderId}" name="providerId" optionValue="displayName" />
			</g:if>
			<g:else>
				<g:select optionKey="id" from="${db.providers}" value="${db.selectProviderId}" name="providerId" optionValue="displayName" noSelection="['':db.providerAllLabel]" />				
			</g:else>		          
			<span class="button">&nbsp;<g:actionSubmit action="providerView" value="Filter" /></span>
			</td>		

		</tr>
	</table>    
 </g:form>