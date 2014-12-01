<div class="gtbl" style="overflow: auto;">
    <table class="detail">
        <g:tableHeaderToggle label="Legend" divPrefix="legend" colSpan="3"/>

        <tbody id="legend_detail" style="display: none;">
        <g:each in="${contentlist}" status="i" var="content">
            <tr class="prop">
                <td width="300px" class="name" align=left
                    style="text-align: left; white-space: normal">
                    <a style="text-decoration: underline"
                       onclick="showDialog('AnalysisDet_${content.id}', { title: '${content.shortDescription}', element: 'detaildiv_${content.id}' });">${content.shortDescription}</a>
                </td>
                <td class="value" align=left style="text-align: left">${content.longDescription}</td>
                <td>
                    <div style="display: none">
                        <div id="detaildiv_${content.id}" style="background-color: #ffffff;"><g:render
                                template="/trial/analysisdetail" model="[analysis: content]"/></div>
                    </div>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>

<div id="heatmapViewContainer">
    <p style="padding-bottom: 5px; padding-top: 5px;"><a href="javascript:backToTop();">Back to Top</a></p>
    <table class="" border="0">
        <tr>
            <g:if test="${comtable != null}">
                <td width='${(comtable == null || comtable == "") ? 1 : (hmapwidth)}%'
                    align="center"
                    style="text-align: center; padding: 0 0 0 0; white-space: nowrap;">
                    Gene Expression Comparison<br>

                    <div id="comHeatmapContainer"><img src="${resource(dir: 'images', file: 'loader-mid.gif')}"
                                                       alt="loading"/>
                    </div>
                </td>
            </g:if>
            <g:if test="${cortable != null}">
                <td width='${(cortable == null || cortable == "") ? 1 : (hmapwidth)}%'
                    align="center"
                    style="text-align: center; padding: 0 0 0 0; white-space: nowrap;">Gene
                Expression Correlation<br>

                    <div id="corHeatmapContainer"><img src="${resource(dir: 'images', file: 'loader-mid.gif')}"
                                                       alt="loading"/></div>
                </td>
            </g:if>
            <g:if test="${rbmtable != null}">
                <td width='${(rbmtable == null || rbmtable == "") ? 1 : (hmapwidth)}%'
                    style="text-align: center; padding: 0 0 0 0; white-space: nowrap;">
                    RBM Comparison<br>

                    <div id="rbmHeatmapContainer"></div>
                </td>
            </g:if>
            <g:if test="${rhotable != null}">
                <td width='${(rhotable == null || rhotable == "") ? 1 : (hmapwidth)}%'
                    style="text-align: center; padding: 0 0 0 0; white-space: nowrap;">
                    RBM Spearman Correlation<br>

                    <div id="rhoHeatmapContainer"></div>
                </td>
            </g:if>
        </tr>
    </table>
</div>

<div id="tableContainer" style="display: none">
    <p style="padding-bottom: 5px; padding-top: 5px;"><a href="javascript:backToTop();">Back to Top</a></p>
    <table>
        <g:if test="${comtable != null}">
            <tr>
                <td>Gene Expression Comparison<br>

                    <div id="comTableView"></div>
                </td>
            </tr>
            <tr>
                <td>
                    <hr>
                    <br>
                </td>
            </tr>
        </g:if>

        <g:if test="${cortable != null}">
            <tr>
                <td>Gene Expression Correlation<br>

                    <div id="corTableView"></div>
                </td>
            </tr>
            <tr>
                <td>
                    <hr>
                    <br>
                </td>
            </tr>
        </g:if>

        <g:if test="${rbmtable != null}">
            <tr>
                <td>RBM Comparison<br>

                    <div id="rbmTableView"></div>
                </td>
            </tr>
            <tr>
                <td>
                    <hr>
                    <br>
                </td>
            </tr>
        </g:if>

        <g:if test="${rhotable != null}">
            <tr>
                <td>RBM Spearman Correlation<br>

                    <div id="rhoTableView"></div>
                </td>
            </tr>
        </g:if>
    </table>
</div>

</div>
</body>
</html>
