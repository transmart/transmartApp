<%@ page import="org.codehaus.groovy.grails.validation.routines.UrlValidator" %>
<br/>
<h2><g:message code="show.tags.header" default="Tags"/>:</h2>
<g:if test="${tags.isEmpty()}">
    <table class="detail">
        <tr><td><g:message code="show.tags.notFound"/></td></tr>
    </table>
</g:if>
<g:else>
    <g:set var="urlValidator" value="${new UrlValidator(['http', 'https', 'ftp'] as String[])}"/>
    <table class="detail">
        <g:each in="${tags}" var="tag">
            <tr class="prop">
                <td valign="top" class="name">${fieldValue(bean: tag, field: 'name')}:</td>
                <td valign="top" class="value">
                    <g:if test="${urlValidator.isValid(tag.description)}">
                        <a href="${fieldValue(bean: tag, field: 'description')}" target="_blank">
                            ${fieldValue(bean: tag, field: 'description')}
                        </a>
                    </g:if>
                    <g:else>
                        ${fieldValue(bean: tag, field: 'description')}
                    </g:else>
                </td>
            </tr>
        </g:each>
    </table>
</g:else>
