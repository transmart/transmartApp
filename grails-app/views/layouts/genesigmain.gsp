<html>
<head>
    <title><g:layoutTitle default="Gene Signature"/></title>
    <link rel="shortcut icon" href="${resource(dir: 'images', file: 'searchtool.ico')}">
    <link rel="icon" href="${resource(dir: 'images', file: 'searchtool.ico')}">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'js', file: 'ext/resources/css/ext-all.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'js', file: 'ext/resources/css/xtheme-gray.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'genesignature.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css/jquery/ui', file: 'jquery-ui-1.9.1.custom.css')}">

    <script type="text/javascript" src="${resource(dir: 'js', file: "jquery/jquery-${org.codehaus.groovy.grails.plugins.jquery.JQueryConfig.SHIPPED_VERSION}.js", plugin: 'jquery')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery/jquery-migrate-1.2.1.min.js')}"></script>

    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery/jquery-ui-1.9.1.custom.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'ext/adapter/ext/ext-base.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'ext/ext-all.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'maintabpanel.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'toggle.js')}"></script>
    <script type="text/javascript" charset="utf-8">
        Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";

        // set ajax to 90*1000 milliseconds
        Ext.Ajax.timeout = 180000;


        Ext.onReady(function () {
            Ext.QuickTips.init()

            /*
             var helpURL = '
            ${grailsApplication.config.com.recomdata.adminHelpURL}';
             var contact = '
            ${grailsApplication.config.com.recomdata.contactUs}';
             var appTitle = '
            ${grailsApplication.config.com.recomdata.appTitle}';
             var buildVer = 'Build Version:
            <g:meta name="environment.BUILD_NUMBER"/> -
            <g:meta name="environment.BUILD_ID"/>';

             var viewport = new Ext.Viewport({
             layout: "border",
             items:[new Ext.Panel({
             region: "center",
             tbar: createUtilitiesMenu(helpURL, contact, appTitle,'
            ${request.getContextPath()}', buildVer, 'gs-utilities-div'),
             autoScroll:true,
             contentEl: "page"
             })]
             });

             viewport.doLayout();
             */
        });

        var $j = window.$j = jQuery.noConflict();

    </script>
    <g:layoutHead/>
    <r:layoutResources/>
</head>

<body>
<div id="page">
    <div id="header"><g:render template="/layouts/commonheader" model="['app': 'genesignature']"/></div>

    <div id="app"><g:layoutBody/></div>
</div>
<r:layoutResources/>
</body>
</html>
