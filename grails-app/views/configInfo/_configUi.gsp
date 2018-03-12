<h2>UI &nbsp;&nbsp;
    <a target="_blank" href="${grailsApplication.config.com.recomdata.adminHelpURL ?: "JavaScript:D2H_ShowHelp('1259','${grailsApplication.config.com.recomdata.adminHelpURL}','wndExternal',CTXT_DISPLAY_FULLHELP )"}">
        <img src="${resource(dir:'images',file:'help/helpicon_white.jpg')}"
	     alt="Help" border=0 width=18pt style="vertical-align:middle;margin-left:5pt;"/>
    </a>
</h2>

<table id="configUi"  class="detail" style="width: 100%">
    <g:tableHeaderToggle
        label="Ui (${configParams.uiParams.size()})"
	divPrefix="config_ui" status="open" colSpan="${3}"/>

    <tbody id="config_ui_detail" style="display: block;">
        <tr>
	    <th>Parameter</th>
	    <th>Value</th>
	    <th width="100%">Description</th>
	</tr>

        <g:each in="${configParams.uiParams}" var="paramData">
            <tr>
	        <td>${paramData.key}</td>
		<g:if test="${paramData.value.value != null}">
		    <td>${paramData.value.value}</td>
		</g:if>
		<g:elseif test="${paramData.value.default != null}">
		    <td>Default: ${paramData.value.default}</td>
		</g:elseif>
		<g:else>
		    <td>&nbsp;</td>
                </g:else>
		<td>${paramData.value.desc}</td>
	    </tr>
	</g:each>

    </tbody>
</table>
