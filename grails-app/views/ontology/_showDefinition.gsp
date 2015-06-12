<g:if test="${!subResourcesAssayMultiMap && !tags && !browseStudyInfo}">
    <g:message code="show.definition.noInfo" default="No Information Found"/>
</g:if>
<g:else>
    <style>
    tr.prop > td.name {
        width: 150px;
    }
    </style>
    <g:if test="${subResourcesAssayMultiMap}">
        <g:render template="highDimSummary" model="subResourcesAssayMultiMap"/>
    </g:if>
    <g:if test="${tags}">
        <g:render template="showTags" model="tags"/>
    </g:if>
    <g:if test="${browseStudyInfo}">
        <g:render template="showBrowseStudyInfo" model="browseStudyInfo"/>
    </g:if>
</g:else>

