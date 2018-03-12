<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Config Info</title>
</head>

<body>
<script type="text/javascript" src="${resource(dir:'js', file:'toggle.js')}"></script>
<!-- override main.css -->
<style type="text/css">
.detail td a {
    padding-left: 10px;
    vertical-align: top;
}

.detail td a:hover {
    white-space: normal;
}
</style>

<div class="body">
    <h1 class="status-title">Configuration of tranSMART server</h1>
    <p>
      All defined configuration parameters are reported. Undefined known
      parameters are reported with their default values. Any
      unrecognized parameters are reported at the end as "Unknown".
    </p>
    <p>
      Parameters can be defined in the distributed transmart code or
      in directory ~/.grails/transmartConfig/ files Config.groovy and
      DataSource.groovy which supersede any predefined values.
    </p>
    <p>
      Any table can be opened or closed by clicking on the arrow in the header. 
    </p>

    <g:render template="configAdmin"/>
    <g:render template="configAnalysis"/>
    <g:render template="configAnalyze"/>
    <g:render template="configAuth"/>
    <g:render template="configBrowse"/>
    <g:render template="configBuildInfo"/>
    <g:render template="configContact"/>
    <g:render template="configDataSource"/>
    <g:render template="configExport"/>
    <g:render template="configGalaxy"/>
    <g:render template="configGeneral"/>
    <g:render template="configGwas"/>
    <g:render template="configGwava"/>
    <g:render template="configHelp"/>
    <g:render template="configI2b2"/>
    <g:render template="configKerberos"/>
    <g:render template="configLdap"/>
    <g:render template="configLog"/>
    <g:render template="configLogin"/>
    <g:render template="configMetacore"/>
    <g:render template="configOauth"/>
    <g:render template="configRmodules"/>
    <g:render template="configSaml"/>
    <g:render template="configSample"/>
    <g:render template="configSearch"/>
    <g:render template="configSmartr"/>
    <g:render template="configSolr"/>
    <g:render template="configSpring"/>
    <g:render template="configUi"/>
    <g:render template="configUpload"/>
    <g:render template="configX509"/>
    <g:render template="configXnatImport"/>
    <g:render template="configXnatView"/>
    <g:render template="configUnknown"/>
</div>
</body>
</html>
