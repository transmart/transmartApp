<!DOCTYPE html>
<html>
<head>
    <!-- Force Internet Explorer 8 to override compatibility mode -->
    <meta http-equiv="X-UA-Compatible" content="IE=edge">

    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>${grailsApplication.config.com.recomdata.appTitle}</title>

    <!-- jQuery CSS for cupertino theme -->
    <link rel="stylesheet" href="${resource(dir: 'css/jquery/ui', file: 'jquery-ui-1.9.1.custom.css')}"></link>
    <link rel="stylesheet" href="${resource(dir: 'css/jquery/skin', file: 'ui.dynatree.css')}"></link>

    <!-- Our CSS -->
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.loadmask.css')}"></link>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}"></link>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'rwg.css')}"></link>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'colorbox.css')}"></link>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery/simpleModal.css')}"></link>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery/multiselect/ui.multiselect.css')}"></link>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery/multiselect/common.css')}"></link>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery/jqueryDatatable.css')}"></link>

    <!-- jQuery JS libraries -->
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.min.js')}"></script>
    <script>jQuery.noConflict();</script>

    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery-ui-1.9.1.custom.min.js')}"></script>

    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.cookie.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.dynatree.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.paging.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.loadmask.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.ajaxmanager.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.numeric.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.colorbox-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.simplemodal.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.dataTables.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'facetedSearch/facetedSearchBrowse.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/ui.multiselect.js')}"></script>


    <!--Datatable styling and scripts-->
    <script type="text/javascript" src="${resource(dir: 'js/', file: 'jquery.dataTables.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'ColVis.min.js')}"></script>

    <!--  SVG Export -->
    <%--<script type="text/javascript" src="${resource(dir:'js', file:'svgExport/rgbcolor.js')}"></script>  --%>


    <g:javascript library="prototype"/>
    <script type="text/javascript">
        var $j = jQuery.noConflict();
    </script>

    <!-- Our JS -->
    <script type="text/javascript" src="${resource(dir: 'js', file: 'rwg.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'maintabpanel.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'datasetExplorer.js')}"></script>

    <!-- Protovis Visualization library and IE plugin (for lack of SVG support in IE8 -->
    <%-- <script type="text/javascript" src="${resource(dir:'js/protovis', file:'protovis-r3.2.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js/protovis', file:'protovis-msie.min.js')}"></script> --%>
</head>

<body>
<div style="width:800px">
    <g:render template="editMetaData"
              model="[folder: folder, , measurements: measurements, technologies: technologies, vendors: vendors, platforms: platforms, layout: layout]"/>
</div>
</body>
</html>