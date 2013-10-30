

<table style="border-collapse:collapse; width:100%;">
	<g:set var="detailsSize" value="${details.size()}"/>
	<g:if test="${detailsSize>0}">
	<tr>
		<g:if test="${!encounter}">
			<td style="padding:5px; font-weight:bold; border-bottom: thin solid #6490C5; border-top: thin solid #6490C5;">${title}</td>
			<td style="padding:5px; border-bottom: thin solid #6490C5; border-top: thin solid #6490C5;"/>
		</g:if>
		<g:else>
			<td style="padding:5px; font-weight:bold; border-bottom: thin solid #6490C5; border-top: thin solid #6490C5;">${title}</td>
			<td style="padding:5px; border-bottom: thin solid #6490C5; border-top: thin solid #6490C5;"/>
			<td style="padding:5px; border-bottom: thin solid #6490C5; border-top: thin solid #6490C5;"/>
		</g:else>
	</tr>
	<g:set var="index" value="${1}" />
	<g:each in="${details}" var="detail">
		<g:if test="${(index%2)==0}">
			<g:if test="${index<detailsSize}">
				<tr>
					<g:if test="${!encounter}">
						<td style="padding:5px; font-weight:bold;">${detail.key}</td>
						<td style="padding:5px;">${detail.value}</td>
					</g:if>	
					<g:else>
						<g:each in="${detail.value}" var="detailValue">
							<tr>
								<td style="padding:5px; font-weight:bold;">${detail.key}</td>
								<td style="padding:5px;">${detailValue.key}</td>
								<td style="padding:5px;">${detailValue.value}</td>
							</tr>
						</g:each>
					</g:else>				
				</tr>
			</g:if>
			<g:else>
				<tr>
					<g:if test="${!encounter}">						
						<td style="padding:5px; font-weight:bold; border-bottom: thick solid #6490C5;">${detail.key}</td>					
						<td style="padding:5px;border-bottom: thick solid #6490C5;">${detailValue}</td>
					</g:if>	
					<g:else>
						<g:each in="${detail.value}" var="detailValue">
							<tr>
								<td style="padding:5px; font-weight:bold; ">${detail.key}</td>							
								<td style="padding:5px;">${detailValue.key}</td>
								<td style="padding:5px; ">${detailValue.value}</td>
							</tr>
						</g:each>
						<tr>
							<td style="padding:5px; border-bottom: thick solid #6490C5; "/>
							<td style="padding:5px; border-bottom: thick solid #6490C5; "/>
							<td style="padding:5px; border-bottom: thick solid #6490C5; "/>
						</tr>
					</g:else>							
				</tr>
			</g:else>
		</g:if>
		<g:else>
			<g:if test="${index<detailsSize}">
				<tr style="background-color:#D2DFED	;">
					<g:if test="${!encounter}">						
						<td style="padding:5px; font-weight:bold;">${detail.key}</td>
						<td style="padding:5px;">${detail.value}</td>
					</g:if>	
					<g:else>
						<g:each in="${detail.value}" var="detailValue">
							<tr style="background-color:#D2DFED	;">
								<td style="padding:5px; font-weight:bold;">${detail.key}</td>
								<td style="padding:5px;">${detailValue.key}</td>
								<td style="padding:5px;">${detailValue.value}</td>
							</tr>								
						</g:each>
					</g:else>
				</tr>
			</g:if>
			<g:else>
				<tr style="background-color:#D2DFED	;">
						<g:if test="${!encounter}">
						<td style="padding:5px; font-weight:bold; border-bottom: thick solid #6490C5;">${detail.key}</td>					
						<td style="padding:5px;border-bottom: thick solid #6490C5;">${detail.value}</td>
					</g:if>	
					<g:else>						
						<g:each in="${detail.value}" var="detailValue">
							<tr style="background-color:#D2DFED	;">
								<td style="padding:5px; font-weight:bold;">${detail.key}</td>							
								<td style="padding:5px; ">${detailValue.key}</td>
								<td style="padding:5px; ">${detailValue.value}</td>
							</tr>
						</g:each>
												
					</g:else>	
				</tr>
			</g:else>
		</g:else>
		<span style="display:none;">${index++}</span>
	</g:each>
	</g:if>
</table>