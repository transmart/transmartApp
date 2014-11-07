<table class="searchform">
    <tr><td>Select Genes:</td><tr>
    <tr>
        <td>
            <g:if test="${genes.size() != 0}">
                <g:select name="haploviewgenes" id="haploviewgenes" from="${genes}" multiple="multiple"
                          size="5"></g:select>
            </g:if>
            <g:if test="${genes.size() == 0}">
                No snp data found for these subsets.
            </g:if>
        </td>
    </tr>
</table>
