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