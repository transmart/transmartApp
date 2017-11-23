</head>
<body>
<div id="header-div" class="header-div">
    <g:render template="/layouts/commonheader" model="['app': 'uploaddata']"/>

    <br/><br/>

    <div class="uploadwindow" style="width: 95%">
        <div>List of the 20 most recent data uploads
        </div>
        <table class="uploadfieldtable" style="margin:0px">
            <thead>
            <tr>
                <th>id</th>
                <th>study</th>
                <th>dataType</th>
                <th>analysisName</th>
                <th>phenotypeIds</th>
                <th>genotypePlatformIds</th>
                <th>expressionPlatformIds</th>
                <th>statisticalTest</th>
                <th>researchUnit</th>
                <th>sampleSize</th>
                <th>cellType</th>
                <th>modelName</th>
                <th>pValueCutoff</th>
                <th>etlDate</th>
                <th>processDate</th>
                <th>filename</th>
                <th>status</th>
                <th>sensitiveFlag</th>
                <th>sensitiveDesc</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${uploads}" var="upload">
                <tr>
                    <td>${upload.id}</td>
                    <td>${upload.study}</td>
                    <td>${upload.dataType}</td>
                    <td>${upload.analysisName}</td>
                    <td><g:each in="${upload.phenotypeIds?.split(';')}" var="me">${me}<br/></g:each></td>
                    <td>${upload.genotypePlatformIds}</td>
                    <td>${upload.expressionPlatformIds}</td>
                    <td>${upload.statisticalTest}</td>
                    <td>${upload.researchUnit}</td>
                    <td>${upload.sampleSize}</td>
                    <td>${upload.cellType}</td>
                    <td>${upload.modelName}</td>
                    <td>${upload.pValueCutoff}</td>
                    <td>${upload.etlDate}</td>
                    <td>${upload.processDate}</td>
                    <td>${upload.filename}</td>
                    <td>${upload.status}</td>
                    <td>${upload.sensitiveFlag}</td>
                    <td>${upload.sensitiveDesc}</td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

</div>

</body>
</html>
