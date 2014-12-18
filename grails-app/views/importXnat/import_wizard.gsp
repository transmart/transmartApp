<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="admin" />
		<title>Import XNAT Clinical Data</title>
		
	</head>		
	<body>
		<script type="text/javascript">
			$(document).ready(function() {
			  $("#import_wizard_form").submit(function(event) {
			    $(".message").remove();
			    $("#import_info").html("<br /><img src=\"/transmart/static/images/ajax-loader.gif\" />&nbsp;&nbsp;<p style=\"background-color:#FEC0C0; font-size:15px; display:inline; border-style:solid; border-color:FD4F4F; border-width:2px;\">The data is now imported in XNAT. This can take a few seconds to minutes, depending on the size of the dataset. Please wait until this message is updated.</p><br />");
			  })
			});
		</script>

		<div class="body">
			<h1>Import XNAT Clinical Data</h1>

			<g:form name="import_wizard_form">
			<div class="dialog">
				<table>
				<tbody>
					<tr class="prop">
						<td valign="top" class="name"><label for="password">XNAT password for username ${importXnatConfiguration.username}:</label></td>
						<td valign="top" class="value">
							<input type="password" name="password" />
						</td>
					</tr>
				</tbody>
				</table>
			</div>
	
			<div class="buttons">
				<input type="hidden" name="id" value="${importXnatConfiguration.id}" />
				<span class="button"><input type="submit" name="_action_Import_variables" value="Import data" class="save" /></span>
			</div>

			</g:form>

			<div id="import_info"></div>

			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
		</div>
	</body>
</html>
