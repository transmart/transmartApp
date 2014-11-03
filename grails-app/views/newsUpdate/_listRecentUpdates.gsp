<g:each var="update" in="${updates}" status="iterator">
    ${update.rowsAffected} records for the ${update.dataSetName} Data Set were ${update.operation} <a href="#"
                                                                                                      onClick="alert('clicky')">More information</a> <br/>
</g:each>