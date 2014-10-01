<h1>Installed Plugins</h1>

<table>
    <g:set var="pluginManager" value="${applicationContext.getBean('pluginManager')}"></g:set>
    <thead>
    <tr style="height: 30px;">
        <th style="vertical-align: middle; width: 250px;"><g:message code="buildInfo.plugin" default="Plugin"/></th>
        <th style="vertical-align: middle; width: 400px;"><g:message code="buildInfo.version" default="Version"/></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${pluginManager.allPlugins.sort({it.name.toUpperCase()})}" status="i" var="plugin">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}" style="height: 30px;">
            <td>${plugin.name}</td><td>${plugin.version}</td>
        </tr>
    </g:each>
    </tbody>
</table>
