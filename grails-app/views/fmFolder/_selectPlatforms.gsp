<g:if test="${platforms}">
    <g:select style="min-width: 400px" id="platform" name="platform" noSelection="${['null': 'Select...']}"
              from="${platforms}" onchange="addToPlatformField()" optionKey="uniqueId" optionValue="fullName"/>
</g:if>
<g:else>
    <g:select style="min-width: 400px" id="platform" name="platform"
              noSelection="${['null': 'Select a filter above first...']}" from="${[]}"/>
</g:else>
