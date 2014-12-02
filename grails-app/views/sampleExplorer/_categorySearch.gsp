<div style="background-color:white;height:100%;width:100%;">
    <br/>
    <br/>
    <span style="font: 12px tahoma,verdana,helvetica;color:#800080;font-weight:bold;">Recent Updates (click update for more info)</span>
    <br/>
    <hr/>
    <br/>

    <g:if test="${newsUpdates != null && !newsUpdates.isEmpty()}">
        <g:each var="newsUpdate" in="${newsUpdates}" status="iterator">

            <a href="#" onClick="showNewsUpdateDetail('${newsUpdate.id}')">
                Data Set <i>${newsUpdate.dataSetName}</i> modified on <i><g:formatDate format="yyyy-MM-dd"
                                                                                       date="${newsUpdate.updateDate}"></g:formatDate></i>
            </a>

            <br/>
            <br/>
        </g:each>
    </g:if>
    <g:else>
        No updates available.
    </g:else>

    <br/>
    <br/>

</div>