<p><font color='red'>${warningMsg}</font></p>
<table class="searchform">
<tr><td style='white-space: nowrap'>PLINK Datasets in Subset 1: </td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td>${snpDatasetNum_1}</td></tr>
<tr><td style='white-space: nowrap'>PLINK Datasets in Subset 2: </td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td>${snpDatasetNum_2}</td></tr>
<tr><td>&nbsp;</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td>&nbsp;</td></tr>
<tr><td valign='top' style='white-space: nowrap'>Select Chromosomes:</td>
<td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td>
<g:select name="plinkChroms" id="plinkChroms" from="${chroms}" value="${chromDefault}" multiple="multiple"  size="5"></g:select>
</td>
</tr>

<!-- 
<tr>
  <td valign='top' style='white-space: nowrap'>Selected Genes:</td>
  <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
  <td><input type="text"  size="35" id="selectedGenesPlink" autocomplete="off" />
	 <div id="divPathwayPlink" style="width:100%; font:11px tahoma, arial, helvetica, sans-serif"><br>Add a Gene:<br>
		<input type="text"  size="35" id="searchPathwayPlink" autocomplete="off" />
		<input type="hidden" id="selectedGenesAndIdPlink"/>
	</div>
  </td>
</tr>
-->

<!--   
<tr><td>&nbsp;</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td>&nbsp;</td></tr>
<tr><td valign='top' style='white-space: nowrap'>Selected SNPs:</td>
<td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><input type="text"  size="35" id="selectedSNPsPlink" autocomplete="off" /></td>
</tr>
-->

</table>

<script type="text/javascript">
	showPathwaySearchBox('selectedGenesPlink', 'selectedGenesAndIdPlink', 'searchPathwayPlink', 'divPathwayPlink');
</script>