<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Status Info</title>
</head>

<body>
<div class="body">
    <h1 class="status-title">Status of SOLR server</h1>
    <div class="solr-status-tag status-tag">${solrStatus.toString()}</div>
    <table class="solr-status-results-table status-results-table">
        <thead>
            <tr><th>Component</th><th>Status</th></tr>
        </thead>
        <tbody>
            <tr><td>Overall - is avaiable?</td><td>${solrStatus.connected}</td></tr>
            <tr><td>rwg core?</td><td>${solrStatus.rwgAvailable} (number of records: ${solrStatus.rwgNumberOfRecords})</td></tr>
            <tr><td>browse core?</td><td>${solrStatus.browseAvailable} (number of records: ${solrStatus.browseNumberOfRecords})</td></tr>
            <tr><td>sample core?</td><td>${solrStatus.sampleAvailable} (number of records: ${solrStatus.sampleNumberOfRecords})</td></tr>
        </tbody>
    </table>
</div>
</body>
</html>
