<div id="omics-filter-content">
    <g:if test="${error}">
        <div class="ui-state-error-text">
            ${error}
        </div>
    </g:if>
    <g:else>
        <table style="-webkit-border-horizontal-spacing: 10px">
            <tbody>
            <tr><td>Platform: <span id="omics-filter-gplid">${gpl_id}</span></td>
                <td rowspan="2">
                    <g:set var="first_projection" value="${true}"/>
                    <g:each in="${projections}" var="projection">
                        <div><input type="radio" name="omics-filter-projection" value="${projection.key}" onclick="omicsProjectionChanged()"
                            <g:if test="${first_projection}">
                                checked
                                <g:set var="first_projection" value="${false}"/>
                            </g:if> >${projection.value}</div>
                    </g:each>
                </td></tr>
            <tr><td><label for="omics-filter-selector">${selector_name}: </label><input type="text" id="omics-filter-selector" size="5"/></td></tr>
            </tbody>
        </table>

        <g:if test="${filter_type=='numeric'}">
            <table>
                <tbody>
                <tr id="omics-slider-row1"><td colspan="3">Number of subjects selected: <span id="omics-filter-subjectcount">0</span></td></tr>
                <tr id="omics-slider-row2"><td colspan="3">Select the range for expression values:</td></tr>
                <tr id="omics-slider-row3" style="display: none;">
                    <td><input type="text" size="2" id="omics-amount-min" style="color:#548cff; font-weight:bold;"></td>
                    <td><div id="omics-range"></div></td>
                    <td><input type="text" size="2" id="omics-amount-max" style="color:#548cff; font-weight:bold;"></td>
                </tr>
                <tr id="omics-slider-row4"><td colspan="3">Histogram:</td></tr>
                <tr><td colspan="3"><div id="omics-filter-histogram"></div></td></tr>
                </tbody>
            </table>
        </g:if>
    </g:else>
</div>
