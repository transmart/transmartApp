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
    <h1 class="status-title">Status of R server (Rserve)</h1>
    <div class="rserve-status-tag status-tag">${rserveStatus.toString()}</div>
    <table class="rserve-status-results-table status-results-table">
        <thead>
            <tr><th>Component</th><th>Status</th></tr>
        </thead>
        <tbody>
            <tr><td>Overall - is avaiable?</td><td>${rserveStatus.connected}</td></tr>
            <tr><td>working</td><td>${rserveStatus.simpleExpressionOK}</td></tr>
            <tr><td>necessary libraries</td><td>${rserveStatus.librariesOk}</td></tr>
            <tr><td>error message (if any)</td><td>${rserveStatus.lastErrorMessage}</td></tr>
        </tbody>
    </table>
    <h1 class="status-title">Status of connection to gwava.war</h1>
    <g:if test="${gwavaStatus.enabled}">
        <div class="gwava-status-tag status-tag">${gwavaStatus.toString()}</div>
        <table class="gwava-status-results-table status-results-table">
            <thead>
                <tr><th>Component</th><th>Status</th></tr>
            </thead>
            <tbody>
                <tr><td>Overall - is avaiable?</td><td>${gwavaStatus.connected}</td></tr>
                <tr><td>error message (if any)</td><td>${gwavaStatus.errorMessage}</td></tr>
            </tbody>
        </table>
    </g:if>
    <g:else>
        <div class="gwava-status-tag status-tag">The GWAS option appears not to be enabled. See ~/.grails/transmartConfig/Config.groovy</div>
    </g:else>
</div>
</body>
</html>
