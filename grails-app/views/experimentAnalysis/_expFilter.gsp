<g:form controller="experimentAnalysis" action="filterResult">
    <g:set var="expAnalysisFilter" value="${session.searchFilter.expAnalysisFilter}"/>

    <table class="jubfilter" style="width: 650px">
        <tr>
            <th colspan=2 style="align: right">
                <span class="button"><g:actionSubmit class="search" action="filterResult"
                                                     value="Filter Results"/>&nbsp;</span>
            </th>
        </tr>
        <tr>
            <td colspan=2 style="border-right: 0px solid #ccc">
                <table class="jubfiltersection">
                    <tr>
                        <td style="width: 200px; white-space: nowrap;">Platform Species:</td>
                        <td><g:select from="${platformOrganisms}" name="species" value="${expAnalysisFilter.species}"
                                      noSelection="['': '-- Any --']"/></td>
                    </tr>
                    <tr>
                        <td style="width: 200px; white-space: nowrap;">Disease:</td>
                        <td><g:select from="${diseases}" name="bioDiseaseId" optionKey="id"
                                      optionValue="preferredName" value="${expAnalysisFilter.bioDiseaseId}"
                                      noSelection="['': '-- Any --']"/></td>
                    </tr>
                    <tr>
                        <td style="width: 200px; white-space: nowrap;">Compound:</td>
                        <td><g:select from="${compounds}" name="bioCompoundId" optionKey="bioDataId"
                                      optionValue="keyword"
                                      value="${expAnalysisFilter.bioCompoundId}" noSelection="['': '-- Any --']"/></td>
                    </tr>
                    <tr>
                        <td style="width: 200px; white-space: nowrap;">Experiment Design:</td>
                        <td><g:select from="${expDesigns}" name="expDesign" value="${expAnalysisFilter.expDesign}"
                                      noSelection="['': '-- Any --']"/></td>
                    </tr>
                    <tr>
                        <td style="width: 200px; white-space: nowrap;">Data Fold Change Cut Off:</td>
                        <td style="font-weight:normal"><g:textField name="foldChange"
                                                                    value="${expAnalysisFilter.foldChange}"/> (Minimum Fold Change Ratio +/-1.0)</td>
                    </tr>
                    <tr>
                        <td style="width: 200px; white-space: nowrap;">Data p Value Less Than:</td>
                        <td style="font-weight:normal"><g:textField name="pvalue"
                                                                    value="${expAnalysisFilter.pvalue}"/> (Maximum P-Value 0.1)</td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</g:form>
