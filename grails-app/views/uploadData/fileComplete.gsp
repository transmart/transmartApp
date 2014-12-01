</head>
<body>
<div id="header-div">
    <g:render template="/layouts/commonheader" model="['app': 'uploaddata']"/>

    <br/><br/>

    <div class="uploadwindow">

        <div>The file was uploaded successfully and has been added to the study.
        </div>
        <br/>
        <a href="${createLink([action: 'index', controller: 'uploadData'])}">Upload another file</a>
        <br/><br/>
        <a href="${createLink([action: 'index', controller: 'RWG'])}">Return to the search page</a>
    </div>

</div>

</body>
</html>