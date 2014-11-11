<div style="background-color:white;height:100%;font: 12px tahoma,verdana,helvetica;text-align:center;">

    <br/>
    <br/>

    <span style="font: 14px tahoma,verdana,helvetica;color:#800080;font-weight:bold;">Search Filters</span>

    <br/>
    <br/>

    <table style="width:100%;text-align:left;padding:0;">
        <g:each var="term" in="${termsMap}">
            <tr>
                <td style="vertical-align:top;">
                    <g:render template="categoryListWithCheckBoxes"
                              model="[termName: term.key, termDisplayName: term.value.displayName, termList: term.value.counts, JSONData: JSONData]"/>
                </td>
            </tr>
            <tr>
                <td>
                    &nbsp;
                </td>
            </tr>
        </g:each>
    </table>
</div>