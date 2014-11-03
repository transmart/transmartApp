<br>
<table id="publicSignatures" class="detail" style="width: 100%">
    <g:tableHeaderToggle
            label="${adminFlag ? ('Other Signatures (' + pubItems.size() + ')') : ('Public Signatures (' + pubItems.size() + ')')}"
            divPrefix="pub_signatures" colSpan="${12}"/>

    <tbody id="pub_signatures_detail" style="display: none;">
    <tr>
        <th>Name</th>
        <th>Author</th>
        <th>Date Created</th>
        <th>Species</th>
        <th>Tech Platform</th>
        <th>Tissue Type</th>
        <th>Public</th>
        <th>Gene List</th>
        <th># Genes</th>
        <th># Up-Regulated</th>
        <th># Down-Regulated</th>
        <th>&nbsp;</th>
    </tr>

    <g:each var="gs" in="${pubItems}" status="idx">
        <g:render template="/geneSignature/summary_record" model="[gs: gs, idx: idx]"/>
    </g:each>

    </tbody>
</table>

</g:form>
</div>
</body>
</html>
