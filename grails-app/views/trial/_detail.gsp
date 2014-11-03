<div class="gtb1">
    <table class="detail">
        <tbody>
        <tr class="prop">
            <td valign="top" class="name">Analysis:</td>
            <td valign="top" class="value">${fieldValue(bean: geneExprAnalysis, field: 'contentID')}</td>
        </tr>
        <tr class="prop">
            <td valign="top" class="name">Gene:</td>
            <td valign="top" class="value">${fieldValue(bean: geneExprAnalysis, field: 'geneSymbol')}</td>
        </tr>
        <tr class="prop">
            <td valign="top" class="name">GenBank Accession:</td>
            <td valign="top" class="value">${fieldValue(bean: geneExprAnalysis, field: 'genBankAccession')}</td>
        </tr>
        <tr class="prop">
            <td valign="top" class="name">Description:</td>
            <td valign="top" class="value">${fieldValue(bean: geneExprAnalysis, field: 'description')}</td>
        </tr>

        <g:if test="${geneExprAnalysis.probeSet != null}">
            <tr class="prop">
                <td valign="top" class="name">Probe Set:</td>
                <td valign="top" class="value">${fieldValue(bean: geneExprAnalysis, field: 'probeSet')}</td>
            </tr>
        </g:if>
        <tr class="prop">
            <td valign="top" class="name">Fold Change:</td>
            <td valign="top" class="value">${fieldValue(bean: geneExprAnalysis, field: 'ratio')}</td>
        </tr>
        <tr class="prop">
            <td valign="top" class="name">Rho Value:</td>
            <td valign="top" class="value">${fieldValue(bean: geneExprAnalysis, field: 'rhovalue')}</td>

        </tr>
        </tbody>
    </table>
</div>