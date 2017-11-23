</head>
<body>
<div id="header-div" class="header-div">
    <g:render template="/layouts/commonheader" model="['app': 'uploaddata']"/>

    <br/><br/>

    <div class="uploadwindow">
        <g:if test="${result.success == true}">
            <div>The file was uploaded successfully and has been submitted to the queue for processing.
                <br/>
                This analysis will be processed overnight and should be available by tomorrow.
            </div>
            <br/>
            <a href="${createLink([action: 'index', controller: 'uploadData'])}">Upload another file</a>
        </g:if>
        <g:else>
            <div>The metadata has been saved, but there was a problem uploading the file:</div>

            <div class="uploaderror">${result.error}</div>
            <g:if test="${result.requiredFields}">
                <table class="uploadfieldtable">
                    <tr>
                        <td class="datalabel">Fields in file</td>
                        <td>${result.providedFields.join(", ")}</td>
                    </tr>
                    <tr>
                        <td class="datalabel">Required fields</td>
                        <td>${result.requiredFields.join(", ")}</td>
                    </tr>
                    <tr>
                        <td class="datalabel">Missing fields</td>
                        <td class="uploaderror">${result.missingFields.join(", ")}</td>
                    </tr>
                </table>
            </g:if>
            <br/>
            <a href="${createLink([action: 'edit', controller: 'uploadData', id: uploadDataInstance.id])}">Edit/resubmit this upload</a>
        </g:else>
        <br/><br/>
        <a href="${createLink([action: 'index', controller: 'RWG'])}">Return to the search page</a>
    </div>

</div>

</body>
</html>
