<div id="highdimension-filter-content">
    <g:if test="${error}">
        <div class="ui-state-error-text">
            ${error}
        </div>
    </g:if>
    <g:else>
        <table style="-webkit-border-horizontal-spacing: 10px; vertical-align: middle; width: 100%;">
            <tbody>
            <tr><td>Platform</td><td>${gpl_id}</td></tr>
            <tr><td>Search in</td>
                <td>
                <select id="highdimension-search-property" style="width: 100%">
                <g:each in="${searchable_properties.keySet().sort()}" var="prop_key">
                    <option value="${prop_key}">${searchable_properties.get(prop_key)}</option>
                </g:each>
                </select>
            </td></tr>
            <tr><td><label for="highdimension-filter-selector">Search term</label></td><td><input type="text" id="highdimension-filter-selector" style="width: 100%"/></td></tr>
            <tr><td>Query on</td>
                <td>
                    <select id="highdimension-filter-projection" style="width: 100%">
                        <g:each in="${projections.keySet().sort()}" var="projection">
                            <option value="${projection}">${projections.get(projection)}</option>
                        </g:each>
                    </select>
                </td></tr>

            </tbody>
        </table>

        <g:if test="${filter_type==org.transmartproject.core.querytool.HighDimensionFilterType.SINGLE_NUMERIC}">
            <table>
                <tbody>
                <tr id="highdimension-slider-row1"><td colspan="3">Number of subjects selected: <span id="highdimension-filter-subjectcount">0</span></td></tr>
                <tr id="highdimension-slider-row2"><td colspan="3">Select the range for expression values:</td></tr>
                <tr id="highdimension-slider-row3" style="display: none;">
                    <td><input type="text" size="2" id="highdimension-amount-min" style="color:#548cff; font-weight:bold;"></td>
                    <td><div id="highdimension-range"></div></td>
                    <td><input type="text" size="2" id="highdimension-amount-max" style="color:#548cff; font-weight:bold;"></td>
                </tr>
                <tr id="highdimension-slider-row4"><td colspan="3">Histogram:</td></tr>
                <tr><td colspan="3"><div id="highdimension-filter-histogram"></div></td></tr>
                <tr id="highdimension-slider-row6"><td>Bins:</td><td><div id="highdimension-bins"></div></td><td><input type="text" size="2" id="highdimension-amount-bins" readonly style="color:#548cff; font-weight:bold;"/></td></tr>
                </tbody>
            </table>
        </g:if>
        <g:elseif test="${filter_type==org.transmartproject.core.querytool.HighDimensionFilterType.ACGH}">
            <span>ACGH Data type</span>
        </g:elseif>
        <g:elseif test="${filter_type==org.transmartproject.core.querytool.HighDimensionFilterType.VCF}">

        </g:elseif>
    </g:else>
</div>
