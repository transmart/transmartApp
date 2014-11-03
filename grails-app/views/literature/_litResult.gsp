<g:if test="${jubData.files.size() > 0}">
    <g:each in="${jubData.files}"
            status="fileCount"
            var="file">
        <g:if test="${fileCount > 0}">,</g:if>
        <g:createFileLink
                content="${file.content}"
                displayLabel="Reference"/>
    </g:each>
</g:if>
<g:else>
    <g:if test="${jubData.reference.referenceId != null && jubData.reference.referenceId != ''}">
        <g:if test="${jubData.reference.referenceId?.startsWith('NCT')}">
            <a style="border: none;"
               href="http://www.clinicaltrials.gov/ct2/show/${jubData.reference.referenceId}"
               target="_blank"
               title="Clinical Trials"><img
                    alt="CT"
                    src="${resource(dir: 'images', file: 'clinicaltrials.png')}"/>&nbsp;Reference
            </a>
        </g:if>
        <g:elseif
                test="${jubData.reference.referenceId?.startsWith('ISRCTN')}">
            <a style="border: none;"
               href="http://www.controlled-trials.com/${jubData.reference.referenceId}"
               target="_blank"
               title="Controlled Trials"><img
                    alt="ISRCTN"
                    src="${resource(dir: 'images', file: 'controlledtrials.png')}"/>&nbsp;Reference
            </a>
        </g:elseif>
        <g:else>
            <a style="border: none;"
               href="http://www.ncbi.nlm.nih.gov/pubmed/${jubData.reference.referenceId}"
               target="_blank"
               title="PubMed ID"><img
                    alt="PubMed"
                    src="${resource(dir: 'images', file: 'ncbi.png')}"/>&nbsp;Reference
            </a>
        </g:else>
    </g:if>
</g:else>
</td>
</tr>
<tr>
    <td colspan="2">
        <span style="white-space:nowrap">
            <g:if test="${jubData.reference.component?.length() > 0}">
                <b>Variant: ${jubData.reference.component}</b>&nbsp;&nbsp;|
            </g:if>
            <g:if test="${jubData.reference.geneId?.length() > 0}">
                &nbsp;<b
                    class="filter-item filter-item-gene">Gene:</b> ${createFilterDetailsLink(altId: jubData.reference.geneId, label: jubData.reference.component, type: "gene")}&nbsp;&nbsp;|
            </g:if>
            <g:if test="${jubData.reference.moleculeType?.length() > 0}">
                &nbsp;<b>Molecule:</b> ${jubData.reference.moleculeType}&nbsp;&nbsp;|
            </g:if>
            <g:if test="${jubData.reference.disease?.length() > 0}">
                &nbsp;<b
                    class="filter-item filter-item-disease">Disease:</b> ${createFilterDetailsLink(altId: jubData.reference.disease, label: jubData.reference.disease, type: "disease")}&nbsp;&nbsp;|
            </g:if>
            <g:if test="${jubData.reference.diseaseSite?.length() > 0}">
                &nbsp;<b>Disease Site:</b> ${jubData.reference.diseaseSite}
            </g:if>
        </span>
    </td>
</tr>
</table>
</td>
</tr>
<tr>
    <td colspan=3>
        <div id="${jubrifdivid}_detail" class="gtb1"
             style="display:none;">
            <g:render template="litDetail"
                      model="[jubData: jubData, resultType: searchresult.resultType]"/>
        </div>
    </td>
</tr>
</g:each>
</g:if>
<g:else>
    <tr>
        <td style="padding: 10px 0px 10px 0px;font-weight:bold;text-align:center;">No Results Found</td>
    </tr>
</g:else>
</table>
</g:if>
<g:else>
    <br><br><br>
    <table class="snoborder" width="100%">
        <tbody>
        <tr>
            <td width="100%"
                style="text-align: center; font-size: 14px; font-weight: bold">No results found</td>
        </tr>
        </tbody>
    </table>
</g:else>
</div>
