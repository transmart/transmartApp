<html>
<head>
    <title>Transmart</title>

    <link href="${resource(dir: 'images', file: 'searchtool.ico')}" rel="shortcut icon" />
    <link href="${resource(dir: 'images', file: 'searchtool.ico')}" rel="icon" />
    <link href="${resource(dir: 'css', file: 'main.css')}" rel="stylesheet" />

    <g:javascript library="jquery" />
    <r:require module="extjs" />
    <g:layoutHead/>
    <r:layoutResources/>

    <script type="text/javascript" charset="utf-8">

        Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";
        // set ajax to 180*1000 milliseconds
        Ext.Ajax.timeout = 180000;
        Ext.QuickTips.init();

        var $j = window.$j = jQuery.noConflict();

    </script>

</head>

<body>

<g:layoutBody/>
<r:layoutResources/>
</body>
</html>
