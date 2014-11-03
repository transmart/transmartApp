<g:if test="${!layout}">
    <i>No columns have been set up for the file view</i>
</g:if>
<g:each in="${layout}" var="layoutRow">
    <div class="infolabel">
        ${layoutRow.displayName}
    </div>

    <div class="infovalue">
        <g:if test="${layoutRow.dataType == 'date'}">

        </g:if>
        <g:else><%-- In all other cases, display as string --%>
            ${fieldValue(bean: file, field: layoutRow.column)}
        </g:else>
    </div>
</g:each>

