</head>
<body>
<div id="header-div" style="overflow:hidden; margin-bottom: 2px;">
    <g:render template="/layouts/commonheader" model="['app': 'search']"/>
    <g:render template="/layouts/searchheader" model="['app': 'search']"/>
    <div id="summarycount-div"
         style="background:#dfe8f6; color:#000; padding:5px 10px 5px 10px;border-top:1px solid #36c;">
        <span id="summarycount-span" style="font-size:13px; font-weight:bold;">
            About ${searchresult?.totalCount()} results found
        </span>
    </div>

    <div id="summary-div" style="padding:5px 10px 5px 10px;font-size:12px;line-height:17px;">
        <b>Filters:</b>&nbsp;${session?.searchFilter?.summaryWithLinks}
    &nbsp;<a class="tiny" style="text-decoration:underline;color:blue;font-size:11px;"
             href="#" onclick="var win = Ext.getCmp('editfilters-window');
            win.show();
            return false;">advanced</a>
        &nbsp;<a class="tiny" style="text-decoration:underline;color:blue;font-size:11px;"
                 href="${createLink(controller: 'customFilter', action: 'create')}">save</a>
        &nbsp;<a class="tiny" style="text-decoration:underline;color:blue;font-size:11px;"
                 href="${createLink(controller: 'search', action: 'index')}">clear all</a>
    </div>
    <g:form controller="geneExprAnalysis" name="globalfilter-form" id="globalfilter-form" action="doSearch">
        <input type="hidden" name="selectedpath" value="">
    </g:form>
</div>
</body>
</html>
