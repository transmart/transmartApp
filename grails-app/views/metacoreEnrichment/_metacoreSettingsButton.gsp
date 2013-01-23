<!-- if you make changes to classes and descriptions here, don't forget to modify metacoreEnrichment.js and metacore.css -->
<g:if test="${settingsMode == 'user'}">
	<g:set var="buttonClass" value="metacoreSettingsUser"/>
	<g:set var="buttonDescription" value="Using personal account" />
</g:if>
<g:else>
	<g:if test="${settingsMode == 'system'}">
		<g:set var="buttonClass" value="metacoreSettingsSystem"/>
		<g:set var="buttonDescription" value="Using company account" />
	</g:if>
	<g:else>
		<g:set var="buttonClass" value="metacoreSettingsDemo" />
		<g:set var="buttonDescription" value="Using free enrichment" />
	</g:else>
</g:else>

<span id="metacoreSettingsButton" class="${buttonClass}"><a href="javascript:showMetacoreSettingsWindow();" title="${buttonDescription}">METACORE SETTINGS</a></span>
