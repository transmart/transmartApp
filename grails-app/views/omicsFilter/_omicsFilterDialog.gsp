<div id="omics-filter-content">
    <g:if test="${error}">
        <div class="ui-state-error-text">
            ${error}
        </div>
    </g:if>
    <g:else>
        <div>Platform: <span id="omics-filter-gplid">${gpl_id}</span></div>

        <g:if test="${filter_type==org.transmartproject.core.querytool.ConstraintByOmicsValue.FilterType.SINGLE_NUMERIC}">
            <table style="-webkit-border-horizontal-spacing: 10px">
                <tbody>
                <tr><td><label for="omics-filter-selector">${selector_name}: </label><input type="text" id="omics-filter-selector" size="5"/></td>
                    <td>
                        <g:set var="first_projection" value="${true}"/>
                        <g:each in="${projections}" var="projection">
                            <div><input type="radio" name="omics-filter-projection" value="${projection.key}" onclick="omicsProjectionChanged()"
                                <g:if test="${first_projection}">
                                    checked
                                    <g:set var="first_projection" value="${false}"/>
                                </g:if> >${projection.value}</div>
                        </g:each>
                    </td></tr>

                </tbody>
            </table>
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
                <tr id="omics-slider-row6"><td>Bins:</td><td><div id="omics-bins"></div></td><td><input type="text" size="2" id="omics-amount-bins" readonly style="color:#548cff; font-weight:bold;"/></td></tr>
                </tbody>
            </table>
        </g:if>
        <g:elseif test="${filter_type==org.transmartproject.core.querytool.ConstraintByOmicsValue.FilterType.ACGH}">
            <span>ACGH Data type</span>
        </g:elseif>
        <g:elseif test="${filter_type==org.transmartproject.core.querytool.ConstraintByOmicsValue.FilterType.VCF}">
            <span>VCF Data type</span>
        </g:elseif>
    </g:else>
</div>
