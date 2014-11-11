<style type="text/css">
.studyBrowseRow.selected {
    border: 1px solid #AED0EA;
    background-color: #D7EBF9;
}

table.studyBrowseTable {
    border-collapse: collapse;
}

table.studyBrowseTable > tbody > tr {
    cursor: pointer;
    padding: 4px;
}

table.studyBrowseTable > tbody > tr > td {
    padding: 4px 0px;
    font-size: 10px;
}
</style>
<script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/ui.multiselect.js')}"></script>
<script type="text/javascript">
    $j = jQuery.noConflict();
    $j(document).ready(function () {
        $j(".studyBrowseRow").click(function () {
            $j('.studyBrowseRow').removeClass('selected');
            $j(this).addClass('selected');
        });
    });

</script>
<table class="studyBrowseTable"><thead><tr><th>Accession</th><th>Name</th></tr></thead>
    <tbody>
    <g:each in="${experiments}" var="experiment">
        <tr class="studyBrowseRow" name="${experiment.accession}"><td>${experiment.accession}</td><td
                id="studyBrowseName${experiment.accession}">${experiment.title}</td></tr>
    </g:each>
    </tbody>
</table>
