<html>
<head>
    <title>${grailsApplication.config.com.recomdata.appTitle}</title>
    <link rel="stylesheet"
          href="${resource(dir: 'js', file: 'ext/resources/css/ext-all.css')}"></link>
    <link rel="stylesheet"
          href="${resource(dir: 'js', file: 'ext/resources/css/xtheme-gray.css')}"></link>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}"/>
    <g:javascript library="prototype"/>
    <script type="text/javascript"
            src="${resource(dir: 'js', file: 'toggle.js')}"></script>
    <script type="text/javascript">

        function refreshParent(newurl) {
            parent.window.close();
            if (parent != null && parent.window.opener != null && !parent.window.opener.closed) {
                parent.window.opener.location = newurl;
            }
        }

    </script>
    <g:setProvider library="prototype"/>
</head>

<body>
<g:waitIndicator divId="summary_loading"/>
<div id="summary" style="display: block;">
    <p class="Title">
        <span class="Title"></span>
    </p>

    <div id="SummaryHeader">
        <span class="SummaryHeader">Available Pathways</span>
    </div>

    <div class="paginateButtons" style="width: 100%;">
        <span style="font-size:12px;color:#000000;">Results for</span>
        <select class="jubselect" name="datasource" id="datasource"
                style="width:240px;"
                onChange="${remoteFunction(action: 'listAllPathways',
                        before: 'toggleVisible(\'summary_loading\'); toggleVisible(\'summary\');',
                        onComplete: 'toggleVisible(\'summary_loading\'); toggleVisible(\'summary\');',
                        update: 'summary', params: '\'datasource=\'+this.value')}">
            <g:each in="${datasources}" var="source" status="i">
                <option value="${source}" ${(selecteddatasource == null && i == 0) || (selecteddatasource == source) ? "selected" : ""}>${source}</option>
            </g:each>
        </select>
        &nbsp;&nbsp;
        <g:remoteAlphaPaginate update="summary" controller="searchHelp"
                               action="listAllPathways"
                               params="[datasource: selecteddatasource]"/>
    </div>
    <table class="trborderbottom" style="width:100%;">
        <g:each in="${pathways}" var="pathway">
            <tr style="border-bottom:1px solid #CCCCCC;">
                <td>${createKeywordSearchLink(popup: true, jsfunction: "refreshParent", keyword: pathway)}</td>
            </tr>
        </g:each>
    </table>
</div>
</body>
</html>
