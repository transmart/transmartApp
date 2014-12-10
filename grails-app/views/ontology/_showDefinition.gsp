<g:if test="${tags.isEmpty()}">
    <table class="detail">
        <tr><td>No tags found.</td></tr>
    </table>
</g:if>
<g:else>
    <table class="detail" style="width: 515px;">
        <g:each in="${tags}" var="tag">
            <tr class="prop">
                <td valign="top" class="name"><g:message code="${'tag' + tag.tagtype}" default="${tag.tagtype}"/>:</td>
                <td valign="top" class="value">${fieldValue(bean: tag, field: 'tag')}</td>
            </tr>
        </g:each>
    </table>
</g:else>