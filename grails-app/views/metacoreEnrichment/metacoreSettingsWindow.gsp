<script language="javascript">
	function onRadioChange() {
		if ( !(typeof radioDemo === 'undefined') && radioDemo.checked ) {
			jQuery('#settingsTable :input').prop('disabled', true);
		}
		else if ( !(typeof radioSystem === 'undefined') && radioSystem.checked ){
			jQuery('#settingsTable :input').prop('disabled', true);
		}
		else {
			jQuery('#settingsTable :input').prop('disabled', false);
		}
	}
</script>

<form style="padding: 5px 5px 5px 5px" id="metacoreSettingsForm">
	<input type="hidden" name="accountType_original" value="${settingsMode}"/>
	<input type="hidden" name="baseUrl_original" value="${settings.baseUrl}" />
	<input type="hidden" name="login_original" value="${settings.login}"/>
	<input type="hidden" name="password_original" value="*****"/>
	
	<input id="radioDemo" type="radio" name="accountType" value="demo" onChange="onRadioChange()"/> I don't have MetaCore account. 
	Run FREE enrichment on a collection of <a href="http://pathwaymaps.com/" target="_blank">publicly available maps</a> instead, or you can <a href="http://ip-science.thomsonreuters.com/info/insight-free-trial/" target="_blank">try MetaCore for free</a>!
	<br/><br/>
	<g:if test="${settingsConfigured}">
		<g:if test="${systemSettingsDefined}">
			<input id="radioSystem" type="radio" name="accountType" value="system" onChange="onRadioChange()"/> Use company account. 
			This account is specified in your tranSMART server settings and provides a shared environment for all users. You will still need to know login/password in order to open individual maps.
			<br/><br/>
		</g:if>
		<input id="radioRegular" type="radio" name="accountType" value="user" onChange="onRadioChange()"/> Use my personal MetaCore account:
		<br/>
		<div id="settingsTable">
		<table>
			<tr>
				<td>Base URL</td><td><input type="text" name="baseUrl" size="30" value="${settings.baseUrl?:'https://portal.genego.com'}"/></td>
			</tr>
			<tr>
				<td>Username</td><td><input type="text" name="login" size="30" value="${settings.login}"/></td>
			</tr>
			<tr>	
				<td>Password</td><td><input type="password" name="password1" size="30" value="*****"/></td>
			</tr>
			<tr>	
				<td>Confirm password</td><td><input type="password" name="password2" size="30" value="*****"/></td>
			</tr>
		</table>
		</div>
	</g:if>
	<g:else>
		<div>
			<b>Warning!</b> The database is not properly configured to support MetaCore Settings. Only free enrichment option is available.
			Please, contact your systems administrator to enable other options.
		</div>
	</g:else>
	
	<script language="javascript">
		if ("${settingsMode}" == 'user') { 
			jQuery('#radioRegular').prop('checked', true);
		}
		else if ("${settingsMode}" == 'system') {
			jQuery('#radioSystem').prop('checked', true);
			jQuery('#settingsTable :input').prop('disabled', true);
		}
		else {
			jQuery('#radioDemo').prop('checked', true);
			jQuery('#settingsTable :input').prop('disabled', true);
		}
	</script>
</form>
