<g:if test="${!platform}">
    <table class="detail">
        <tr><td><g:message code="show.platform.notFound"/></td></tr>
    </table>
</g:if>
<g:else>
    <table class="detail">
        <g:if test="${platform.title}">
            <tr class="prop">
                <td valign="top" class="name"><g:message code="show.platform.title" default="Title"/>:</td>
                <td valign="top" class="value">${fieldValue(bean: platform, field: 'title')}</td>
            </tr>
        </g:if>
        <g:if test="${platform.genomeReleaseId}">
            <tr class="prop">
                <td valign="top" class="name">
                    <g:message code="show.platform.genomeBuild" default="Genome Build"/>:
                </td>
                <td valign="top" class="value">${fieldValue(bean: platform, field: 'genomeReleaseId')}</td>
            </tr>
        </g:if>
        <g:if test="${platform.organism}">
            <tr class="prop">
                <td valign="top" class="name"><g:message code="show.platform.organism" default="Organism"/>:</td>
                <td valign="top" class="value">${fieldValue(bean: platform, field: 'organism')}</td>
            </tr>
        </g:if>
        <g:if test="${platform.markerType}">
            <tr class="prop">
                <td valign="top" class="name"><g:message code="show.platform.markerType" default="Marker Type"/>:</td>
                <td valign="top" class="value">${fieldValue(bean: platform, field: 'markerType')}</td>
            </tr>
        </g:if>
        <g:if test="${platform.annotationDate}">
            <tr class="prop">
                <td valign="top" class="name">
                    <g:message code="show.platform.annotationDate" default="Annotation Date"/>:
                </td>
                <td valign="top" class="value">${fieldValue(bean: platform, field: 'annotationDate')}</td>
            </tr>
        </g:if>
    </table>
</g:else>
