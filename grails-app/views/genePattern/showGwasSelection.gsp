<p><span style="color: red; ">${warningMsg}</span></p>
<table class="searchform">
    <tr><td style='white-space: nowrap'>SNP Datasets in Subset 1:</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td>${snpDatasetNum_1}</td>
    </tr>
    <tr><td style='white-space: nowrap'>SNP Datasets in Subset 2:</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td>${snpDatasetNum_2}</td>
    </tr>
    <tr><td>&nbsp;</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td>&nbsp;</td></tr>
    <tr><td valign='top' style='white-space: nowrap'>Select Chromosomes:</td>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td>
        <g:select name="gwasChroms" id="gwasChroms" from="${chroms}" value="${chromDefault}" multiple="multiple"
                  size="5"></g:select>
    </td>
</table>
