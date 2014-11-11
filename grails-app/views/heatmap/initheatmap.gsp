<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <g:javascript library="prototype"/>

    <script type="text/javascript">

        function init() {

            setTimeout("showheatmap();", 500)
        }

        function showheatmap() {
            document.showheatmapform1.submit();


        }

    </script>
</head>

<body onLoad="javascript:init()">
<div style="margin-top:100px;text-align: center;">

    <img src="${resource(dir: 'images', file: 'loader-large.gif')}" alt="loading"/>
</div>

<div style="margin-top:20px; text-align: center;"><b>Generating Heatmap...</b>
</div>

<div style="display:none">
    <g:form name="showheatmapform1" controller="heatmap" action="showheatmap">
    </g:form>
</div>
</body>
</html>
