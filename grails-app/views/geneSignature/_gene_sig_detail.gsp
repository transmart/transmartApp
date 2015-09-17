<g:if test="${gs.experimentTypeConceptCode?.bioConceptCode == 'IN_VIVO_ANIMAL' || gs.experimentTypeConceptCode?.bioConceptCode == 'IN_VIVO_HUMAN'}">
    <tr>
        <td style="border: none;">'in vivo' model:&nbsp;${gs.experimentTypeInVivoDescr}</td>
    </tr>
</g:if>
<tr><td style="border: none;">ATCC designation:&nbsp;${gs.experimentTypeATCCRef}</td></tr>
</table>
</td>
</tr>
</tbody>
</table>

<table class="detail" style="width: 100%">
    <g:tableHeaderToggle label="Analysis Meta-Data" divPrefix="${gs.id}_analysis" colSpan="2"/>

    <tbody id="${gs.id}_analysis_detail" style="display: none;">
    <tr class="prop">
        <td class="name">Analysis Performed By:</td>
        <td class="value">${gs.analystName}</td>
    </tr>
    <tr class="prop">
        <td class="name">Normalization Method:</td>
        <td class="value">
            <g:if test="${gs.normMethodConceptCode?.id == 1}">${gs.normMethodOther}</g:if>
            <g:else>${gs.normMethodConceptCode?.codeName}</g:else>
        </td>
    </tr>
    <tr class="prop">
        <td class="name">Analytic Category:</td>
        <td class="value">
            <g:if test="${gs.analyticCatConceptCode?.id == 1}">${gs.analyticCatOther}</g:if>
            <g:else>${gs.analyticCatConceptCode?.codeName}</g:else>
        </td>
    </tr>
    <tr class="prop">
        <td class="name">Analysis Method:</td>
        <td class="value">
            <g:if test="${gs.analysisMethodConceptCode?.id == 1}">${gs.analysisMethodOther}</g:if>
            <g:else>${gs.analysisMethodConceptCode?.codeName}</g:else>
        </td>
    </tr>
    <tr class="prop">
        <td class="name">Multiple Testing Correction:</td>
        <td class="value"><g:if
                test="${gs.multipleTestingCorrection != null}">${gs.multipleTestingCorrection == 1 ? 'Yes' : 'No'}</g:if></td>
    </tr>
    <tr class="prop">
        <td class="name">P-value Cutoff:</td>
        <td class="value">${gs.pValueCutoffConceptCode?.codeName}</td>
    </tr>
    <tr class="prop">
        <td class="name">Fold-change metric:</td>
        <td class="value">${gs.foldChgMetricConceptCode?.codeName}</td>
    </tr>
    <tr class="prop">
        <td class="name">Original upload file:</td>
        <td class="value">${gs.uploadFile}</td>
    </tr>
    </tbody>
</table>

<table style="width: 100%">
    <g:tableHeaderToggle label="Gene Signature Items" divPrefix="${gs.id}_items"/>

    <tbody id="${gs.id}_items_detail" style="display: none;">
    <tr><td>
        <table class="detail" width="100%" border=1>
            <tr>
                <td style="font-weight: bold;">Gene Symbol</td>
                <td style="font-weight: bold;">Probeset ID</td>
                <td style="font-weight: bold;">SNP</td>
                <g:if test="${gs.foldChgMetricConceptCode?.bioConceptCode != 'NOT_USED'}"><td
                        style="font-weight: bold; white-space: nowrap;">Fold-Change Metric</td></g:if>
            </tr>
            <g:each in="${gs.geneSigItems}">
                <tr>
                    <td class="name">${it.geneSymbol.join("/")}</td>
                    <td class="name">${it.probeset}</td>
                    <td class="name">${it.bioDataUniqueId.startsWith('SNP:')? it.bioDataUniqueId.substring(4) : ""}</td>
                    <g:if test="${gs.foldChgMetricConceptCode?.bioConceptCode != 'NOT_USED'}">
                        <g:if test="${gs.foldChgMetricConceptCode?.bioConceptCode == 'TRINARY'}"><td class="name"
                                                                                                     style="text-align: right;"><g:formatNumber
                                    number="${it.foldChgMetric}" format="0"/></td></g:if>
                        <g:else><td class="name" style="text-align: right;"><g:formatNumber number="${it.foldChgMetric}"
                                                                                            format="##0.###"/></td></g:else>
                    </g:if>
                </tr>
            </g:each>
        </table>
    </td></tr>
    </tbody>
</table>
