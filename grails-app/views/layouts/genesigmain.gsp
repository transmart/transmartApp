<html>
<head>
    <title><g:layoutTitle default="Gene Signature"/></title>

    <link href="${resource(dir: 'images', file: 'searchtool.ico')}" rel="shortcut icon" />
    <link href="${resource(dir: 'images', file: 'searchtool.ico')}" rel="icon" />

    <g:javascript library="jquery" />
    <r:require module="signatureTab" />
    <r:layoutResources/>

    <script type="text/javascript" charset="utf-8">
        Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";
        Ext.Ajax.timeout = 180000;
        Ext.onReady(function () {
            Ext.QuickTips.init()
        });

        var $j = window.$j = jQuery.noConflict();

    </script>
</head>

<body>
<div id="page">
    <div id="header-div" class="header-div"><g:render template="/layouts/commonheader" model="['app': 'genesignature']"/></div>

    <div id="app"><g:layoutBody/></div>
</div>
<r:layoutResources/>
</body>
</html>
