<html>
<head>
    <title>${grailsApplication.config.com.recomdata.appTitle}</title>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}"/>

    <script type="text/javascript">

        function refreshParent(newurl) {
            parent.window.close();
            if (parent != null && parent.window.opener != null && !parent.window.opener.closed) {
                parent.window.opener.location = newurl;
            }
        }

    </script>
</head>

<body>
<div id="summary">

    <p class="Title"><span class="Title"></span></p>

    <div id="SummaryHeader"><span class="SummaryHeader">Available Disease Search Terms</span></div>
    <table class="trborderbottom" width="100%">
        <g:each in="${diseases}" status="i" var="keyword">
            <tr style="border-bottom: 1px solid #CCCCCC;">
                <td>${createKeywordSearchLink(popup: true, jsfunction: 'refreshParent', keyword: keyword)}</td>
            </tr>
        </g:each>
    </table>
    <br/>
</div>
</body>
</html>
