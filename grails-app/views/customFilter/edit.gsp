</head>
<body>
<g:render template="/layouts/commonheader" model="['app': 'customfilters']"/>
<div class="nav">
    <span class="menuButton"><g:link class="list" action="list">Saved Filters</g:link></span>
    <% topicID = "1022" %>
    <a HREF='JavaScript:D2H_ShowHelp(<%=topicID%>,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP )'>
        <img src="${resource(dir: 'images', file: 'help/helpbutton.jpg')}" alt="Help" border=0 width=18pt
             style="margin-top:1pt;margin-bottom:1pt;margin-right:18pt;float:right"/>
    </a>
</div>

<div style="padding: 20px 10px 10px 10px;">
    <h1 style="font-weight:bold; font-size:10pt; padding-bottom:5px;">Edit Filter</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${customFilterInstance}">
        <div class="errors">
            <g:renderErrors bean="${customFilterInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form method="post">
        <input type="hidden" name="id" value="${customFilterInstance?.id}"/>
        <input type="hidden" name="searchUserId" value="${customFilterInstance?.searchUserId}"/>

        <div class="dialog">
            <table>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="name">Name:</label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: customFilterInstance, field: 'name', 'errors')}">
                        <g:textField size="80" name="name"
                                     value="${fieldValue(bean: customFilterInstance, field: 'name')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="description">Description:</label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: customFilterInstance, field: 'description', 'errors')}">
                        <g:textArea rows="2" cols="61" name="description"
                                    value="${fieldValue(bean: customFilterInstance, field: 'description')}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="privateFlag">Private Flag:</label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: customFilterInstance, field: 'privateFlag', 'errors')}">
                        <g:checkBox name="privateFlag"
                                    value="${fieldValue(bean: customFilterInstance, field: 'privateFlag') == 'Y'}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name"><label for="items">Summary:</label></td>
                    <td valign="top">${customFilterInstance.summary}</td>
                </tr>
            </table>
        </div>

        <div class="buttons">
            <g:actionSubmit class="save" value="Update"/>
            <g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete"/>
            <g:actionSubmit class="cancel" action="list" value="Cancel"/>
        </div>
    </g:form>
</div>
</body>
</html>
