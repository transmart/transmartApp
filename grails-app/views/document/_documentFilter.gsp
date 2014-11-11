<g:form controller="document" action="filterDocument" name="documentfilters">
    <table class="jubfilter" style="width:500px">
        <tr>
            <th colspan=2 style="align:right">
                <span class="button">
                    <g:actionSubmit class="search" action="filterDocument" value="Filter Results"
                                    onclick="return validateDocumentFilters();"/>&nbsp;
                </span>
            </th>
        </tr>
        <tr>
            <td colspan=2 style="border-right:0px solid #ccc">
                <table class="jubfiltersection">
                    <tr>
                        <td style="vertical-align:top; border-left:0; font-weight:bold; padding-top:7px;" width="110"
                            nowrap="nowrap">
                            Repository
                        </td>
                        <td style="vertical-align:top; line-height:normal;">
                            <g:each in="${repositories.keySet()}" status="i" var="repository">
                                <g:checkBox name="repository_${repository.toLowerCase().replace(' ', '_')}"
                                            value="${filter.repositories.get(repository)}"/>
                                ${repository}<br/>
                            </g:each>
                        </td>
                    </tr>
                    <tr>
                        <td style="vertical-align:top; border-left:0; font-weight:bold; padding-top:7px;" width="110"
                            nowrap="nowrap">
                            Path
                        </td>
                        <td style="line-height:normal;">
                            <g:textField name="path" value="${filter.path}" style="width:250px;"/>
                        </td>
                    </tr>
                    <tr>
                        <td style="vertical-align:top; border-left:0; font-weight:bold; padding-top:7px;" width="110"
                            nowrap="nowrap">
                            Document Type
                        </td>
                        <td style="vertical-align:top; line-height:normal;">
                            <g:checkBox name="type_excel" value="${filter.type_excel}"/>Excel<br/>
                            <g:checkBox name="type_html" value="${filter.type_html}"/>HTML<br/>
                            <g:checkBox name="type_pdf" value="${filter.type_pdf}"/>PDF<br/>
                            <g:checkBox name="type_powerpoint" value="${filter.type_powerpoint}"/>PowerPoint<br/>
                            <g:checkBox name="type_text" value="${filter.type_text}"/>Text<br/>
                            <g:checkBox name="type_word" value="${filter.type_word}"/>Word<br/>
                            <g:checkBox name="type_other" value="${filter.type_other}"/>Other
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</g:form>