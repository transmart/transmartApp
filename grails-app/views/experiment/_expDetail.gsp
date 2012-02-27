<table class="detail" style="width: 515px;">
	<tbody>
		<tr class="prop">
			<td valign="top" class="name"><g:message code="experimentInstance.design" default="Accession" />:</td>
			<td valign="top" class="value">
				${fieldValue(bean:experimentInstance,field:'accession')}
				<g:if test="${experimentInstance?.files.size() > 0}">
					<g:set var="fcount" value="${0}" />
					<g:each in="${experimentInstance.files}" var="file">
						<g:if test="${file.content.type=='Experiment Web Link'}">
							<g:set var="fcount" value="${fcount++}" />
							<g:if test="${fcount > 1}">, </g:if>
							<g:createFileLink content="${file.content}" displayLabel="${file.content.repository.repositoryType}"/>
						</g:if>
						<g:elseif test="${file.content.type=='Dataset Explorer Node Link'&&search==1}">
						<g:link controller="datasetExplorer" action="index" params="[path:file.content.location]">Dataset Explorer<img src="${resource(dir:'images', file:'internal-link.gif')}"/></g:link>
						</g:elseif>
					</g:each>
				</g:if>
				<g:if test="${searchId!=null}">
					| <g:link controller="search" action="newSearch" id="${searchId}">Search analyzed Data <img src="${resource(dir:'images', file:'internal-link.gif')}"/></g:link>
				</g:if>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><g:message code="experimentInstance.type" default="Type" />:</td>
			<td valign="top" class="value">${fieldValue(bean:experimentInstance,field:'type')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><g:message code="experimentInstance.title" default="Title" />:</td>
			<td valign="top" class="value">${fieldValue(bean:experimentInstance,field:'title')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><g:message code="experimentInstance.description" default="Description" />:</td>
			<td valign="top" class="value">${fieldValue(bean:experimentInstance,field:'description')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><g:message code="experimentInstance.description" default="Overall Design" />:</td>
			<td valign="top" class="value">${fieldValue(bean:experimentInstance,field:'overallDesign')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><g:message code="experimentInstance.design" default="Experiment Design" />:</td>
			<td valign="top" class="value">${fieldValue(bean:experimentInstance,field:'design')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><g:message code="experimentInstance.design" default="Status" />:</td>
			<td valign="top" class="value">${fieldValue(bean:experimentInstance,field:'status')}</td>
		</tr>

		<tr class="prop">
			<td valign="top" class="name"><g:message code="experimentInstance.startDate" default="Start Date" />:</td>
			<td valign="top" class="value"><g:formatDate format="yyyy-MM-dd" date="${experimentInstance.startDate}"/></td>
		</tr>

		<tr class="prop">
			<td valign="top" class="name"><g:message code="experimentInstance.completionDate" default="Completion Date" />:</td>
			<td valign="top" class="value"><g:formatDate format="yyyy-MM-dd" date="${experimentInstance.completionDate}"/></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><g:message code="experimentInstance.primaryInvestigator" default="Primary Investigator" />:</td>
			<td valign="top" class="value">${fieldValue(bean:experimentInstance,field:'primaryInvestigator')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><g:message code="experimentInstance.platforms" default="Platform(s)" />:</td>
			<td valign="top" style="text-align: left;" class="value">
				<g:each var="pf" in="${expPlatforms}">
					${pf?.name.encodeAsHTML()}<br>
				</g:each>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><g:message code="experimentInstance.organism" default="Organism" />:</td>
			<td valign="top" style="text-align: left;" class="value">
				<g:each var="og" in="${expOrganisms}">
					${og?.encodeAsHTML()}<br>
				</g:each>
			</td>
		</tr>

	</tbody>
</table>

