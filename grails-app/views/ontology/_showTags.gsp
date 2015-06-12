<br/>
<h2><g:message code="show.tags.header" default="Tags"/>:</h2>
<g:if test="${tags.isEmpty()}">
    <table class="detail">
        <tr><td><g:message code="show.tags.notFound"/></td></tr>
    </table>
</g:if>
<g:else>
    <table class="detail">
        <g:each in="${tags}" var="tag">
            <tr class="prop">
                <td valign="top" class="name">${fieldValue(bean: tag, field: 'name')}:</td>
                <td valign="top" class="value">${fieldValue(bean: tag, field: 'description')}</td>
            </tr>
        </g:each>
    </table>
</g:else>
