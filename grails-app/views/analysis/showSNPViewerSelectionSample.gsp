<p><font color='red'>
    ${warningMsg}
</font></p>
<table class="searchform">

    <g:each in="${snpDatasets}" status="i" var="snpSubset">
        <tr>
            <td style='white-space: nowrap'>SNP Datasets in Subset ${snpSubset.key}:</td>
            <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
            <td>
                ${snpSubset.value}
            </td>
        </tr>
    </g:each>

    <tr>
        <td>&nbsp;</td>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td valign='top' style='white-space: nowrap'>Select Chromosomes:</td>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td><g:select name="snpViewChroms" id="snpViewChroms"
                      from="${chroms}" value="${chromDefault}" multiple="multiple" size="5"></g:select>
        </td>
    </tr>
    <tr>
        <td valign='top' style='white-space: nowrap'>Selected Genes:</td>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td><input type="text" size="35" id="selectedGenesSNPViewer"
                   autocomplete="off"/>

            <div id="divPathwaySNPViewer"
                 style="width: 100%; font: 11px tahoma, arial, helvetica, sans-serif"><br>
                Add a Gene:<br>
                <input type="text" size="35" id="searchPathwaySNPViewer"
                       autocomplete="off"/> <input type="hidden"
                                                   id="selectedGenesAndIdSNPViewer"/></div>
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td valign='top' style='white-space: nowrap'>Selected SNPs:</td>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td><input type="text" size="35" id="selectedSNPs"
                   autocomplete="off"/></td>
    </tr>
</table>

<script type="text/javascript">
    showPathwaySearchBox('selectedGenesSNPViewer', 'selectedGenesAndIdSNPViewer', 'searchPathwaySNPViewer', 'divPathwaySNPViewer');
</script>