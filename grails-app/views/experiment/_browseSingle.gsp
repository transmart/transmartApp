<style type="text/css">
.studyBrowseRow.selected {
	background-color: #FFC;
}

table.studyBrowseTable {
	border-collapse: collapse;
}
table.studyBrowseTable > tbody > tr {
	font-size: 8pt;
	cursor: pointer;
}
table.studyBrowseTable > tbody > tr > td {
	padding: 8px 0px;
}
</style>
<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/ui.multiselect.js')}"></script>
<script type="text/javascript">
$j = jQuery.noConflict();
$j(document).ready(function(){
    $j(".studyBrowseRow").click(function(){
    	 $j('.studyBrowseRow').removeClass('selected');
         $j(this).addClass('selected');
    });
});

</script>
<table class="studyBrowseTable"><thead><tr><th>Accession</th><th>Name</th></tr></thead>
	<tbody>
	<g:each in="${experiments}" var="experiment">
		<tr class="studyBrowseRow" name="${experiment.id}"><td>${experiment.accession}</td><td id="studyBrowseName${experiment.id}">${experiment.title}</td></tr>
	</g:each>
	</tbody>
</table>
