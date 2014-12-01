<html>
<head>
    <title><g:layoutTitle default=""/></title>
    <link rel="shortcut icon" href="${resource(dir: 'images', file: 'searchtool.ico')}">
    <link rel="icon" href="${resource(dir: 'images', file: 'searchtool.ico')}">
    <link rel="stylesheet" href="${resource(dir: 'js', file: 'ext/resources/css/ext-all.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'js', file: 'ext/resources/css/xtheme-gray.css')}">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}"/>

    <script type="text/javascript" src="${resource(dir: 'js', file: "jquery/jquery-${org.codehaus.groovy.grails.plugins.jquery.JQueryConfig.SHIPPED_VERSION}.js", plugin: 'jquery')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery/jquery-migrate-1.2.1.min.js')}"></script>
    
    <script type="text/javascript" src="${resource(dir: 'js', file: 'ext/adapter/ext/ext-base.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'ext/ext-all.js')}"></script>
    <script type="text/javascript" charset="utf-8">

        Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";
        Ext.Ajax.timeout = 180000;
        Ext.QuickTips.init();

        var $j = window.$j = jQuery.noConflict();

    </script>
    <g:layoutHead/>
    <r:layoutResources/>
</head>

<body>
<g:layoutBody/>
<r:layoutResources/>
</body>
</html>
