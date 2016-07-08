<table id="queryTable" border="0">
    <tr>
        <td>Subset 1</td>
        <td>Subset 2</td>
    </tr>
    <!-- TODO This must be reactivated when support for encounter is proposed
    <tr>
        <td><div class="queryEncounter"><label for="queryEncounter_1"><input id="queryEncounter_1" type="checkbox"/>Only groups occurring within the same encounter</label></div></td>
        <td><div class="queryEncounter"><label for="queryEncounter_2"><input id="queryEncounter_2" type="checkbox"/>Only groups occurring within the same encounter</label></div></td>
    </tr>
    -->
    <tr>
        <td><!-- That's where our panels go --></td>
        <td><!-- That's where our panels go --></td>
    </tr>
</table>

<div style="text-align: center;">
    <button class="flatbutton" onclick="showSaveSubsetsDialog();">
        Save Comparison
    </button>
    <button class="flatbutton clearbutton" onclick="clearQuery();">
        Clear All Panels and Analysis
    </button>
    <br/>
    <br/>
</div>

<div id="panelModel" class="panelModel">
    <div class="panelBox">
        <div class="panelBoxListPlaceholder">
            <div class="wrap"><span class="holder">Drag your concepts here</span></div>
        </div>
        <div class="panelBoxList" id="panelBoxList_$n"></div>
        <div class="panelBoxDateSelector">
            <input type="text" id="panelBoxDateFrom_$n" placeholder="From">
            <input type="text" id="panelBoxDateTo_$n" placeholder="To">
        </div>
        <div class="panelBoxBar">
            <span class="panelRadio">
                <input type="radio" id="panelInclude_$n" name="panelRadio_$n" value="0" checked="checked"><label for="panelInclude_$n">Include</label>
                <input type="radio" id="panelExclude_$n" name="panelRadio_$n" value="1"><label for="panelExclude_$n">Exclude</label>
            </span>
            <span class="panelButtons">
                <!-- TODO This must be reactivated when support for dated query is proposed
                <input type="checkbox" id="panelDate_$n" class="panelDate" /><label for="panelDate_$n" class="flatbutton">By Dates</label>
                -->
                <button id="panelClear_$n" class="flatbutton clearbutton">Clear Panel</button>
            </span>
        </div>
    </div>
    <span class="panelFob">AND</span>
</div>

<div style="width: 100%; text-align: center; padding: 20px 0 30px 0;">
    <g:set var="projectName" value="${grailsApplication.config?.com?.recomdata?.projectName}"/>
    <g:set var="providerName" value="${grailsApplication.config?.com?.recomdata?.providerName}"/>
    <g:if test="${projectName}">
        <img src="${resource(dir: 'images', file: 'project_logo.png')}" alt="${projectName}" style="height:35px;vertical-align:middle;margin-bottom: 12px;" />
    </g:if>
    <g:if test="${projectName && providerName}">
        <span style="font-size:20px;display: inline-block;line-height: 35px; height: 35px;">&nbsp;+&nbsp;</span>
    </g:if>
    <g:if test="${providerName}">
        <a id="providerpowered" target="_blank" href="${grailsApplication.config?.com?.recomdata?.providerURL}"
           style="text-decoration: none;">
            <img src="${resource(dir: 'images', file: 'provider_logo.png')}" alt="${providerName}" style="height:35px;vertical-align:middle;margin-bottom: 12px;" />
        </a>
    </g:if>
</div>

<div id="hiddenDragDiv" style="display:none"></div>

<script type="text/javascript">
    prepareQueryPanels();
    setupDragAndDrop();
    if (GLOBAL.restoreSubsetId) {
        applySubsets(GLOBAL.restoreSubsetId);
    }
</script>
