<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
 
-->

<p><font color='red'>${warningMsg}</font></p>
<table class="searchform">
<tr><td style='white-space: nowrap'>SNP Datasets in Subset 1: </td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td>${snpDatasetNum_1}</td></tr>
<tr><td style='white-space: nowrap'>SNP Datasets in Subset 2: </td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td>${snpDatasetNum_2}</td></tr>
<tr><td>&nbsp;</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td>&nbsp;</td></tr>
<tr><td valign='top' style='white-space: nowrap'>Select Chromosomes:</td>
<td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td>
<g:select name="snpViewChroms" id="snpViewChroms" from="${chroms}" value="${chromDefault}" multiple="multiple"  size="5"></g:select>
</td>
</tr>
<tr><td valign='top' style='white-space: nowrap'>Selected Genes:</td>
<td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><input type="text"  size="35" id="selectedGenesSNPViewer" autocomplete="off" />
			  	<div id="divPathwaySNPViewer" style="width:100%; font:11px tahoma, arial, helvetica, sans-serif"><br>Add a Gene:<br>
			  		<input type="text"  size="35" id="searchPathwaySNPViewer" autocomplete="off" />
			  		<input type="hidden" id="selectedGenesAndIdSNPViewer"/>
			  	</div>
</td>
</tr>
<tr><td>&nbsp;</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td>&nbsp;</td></tr>
<tr><td valign='top' style='white-space: nowrap'>Selected SNPs:</td>
<td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><input type="text"  size="35" id="selectedSNPs" autocomplete="off" /></td>
</tr>
</table>

<script type="text/javascript">
	showPathwaySearchBox('selectedGenesSNPViewer', 'selectedGenesAndIdSNPViewer', 'searchPathwaySNPViewer', 'divPathwaySNPViewer');
</script>