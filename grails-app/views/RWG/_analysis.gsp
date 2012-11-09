<g:each in="${aList}" status="i" var="a">
    <g:render template="/RWG/bmanalysis" model="['analysis':a]" />
</g:each>




