<g:each in="${subResourcesAssayMultiMap}" var="entry">
    <g:set var="hdResource" value="${entry.key}"/>
    <g:set var="assays" value="${entry.value}"/>
    <br/>
    <h2><g:message code="highDim.summary.hd.generalInfo" default="HD General Information"/>:</h2>
    <table class="detail">
        <tr class="prop">
            <td valign="top" class="name"><g:message code="highDim.summary.data.type" default="Data Type"/>:</td>
            <td valign="top" class="value">${hdResource.dataTypeDescription?.encodeAsHTML()}</td>
        </tr>
        <tr class="prop">
            <td valign="top" class="name">
                <g:message code="highDim.summary.assays.number" default="Number of Samples"/>:
            </td>
            <td valign="top" class="value">${assays?.size() ?: 0}</td>
        </tr>
        <g:if test="${assays}">
            <g:set var="tissues" value="${assays*.tissueType*.label.unique().findAll()}"/>
            <g:if test="${tissues}">
                <tr class="prop">
                    <td valign="top" class="name">
                        <g:message code="highDim.summary.assays.tissueType" default="Tissue Type"/>:
                    </td>
                    <td valign="top" class="value">${tissues.join(', ')}</td>
                </tr>
            </g:if>

            <g:set var="samples" value="${assays*.sampleType*.label.unique().findAll()}"/>
            <g:if test="${samples}">
                <tr class="prop">
                    <td valign="top" class="name">
                        <g:message code="highDim.summary.assays.sampleType" default="Sample Type"/>:
                    </td>
                    <td valign="top" class="value">${samples.join(', ')}</td>
                </tr>
            </g:if>
        </g:if>
    </table>

    <g:if test="${assays}">
        <g:set var="platforms" value="${assays*.platform.unique()}"/>
        <br/>
        <h2><g:message code="highDim.summary.platformInfo" default="Platform Information"/>:</h2>
        <g:each in="${platforms}" var="platform">
            <g:render template="showPlatform" model="${[platform: platform]}"/>
        </g:each>
    </g:if>
</g:each>
