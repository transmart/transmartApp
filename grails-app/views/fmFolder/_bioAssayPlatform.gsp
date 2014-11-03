<%! import org.transmart.biomart. * %>

<g:javascript>
function addToPlatformField() {
	var platformId = jQuery('#platform').val();
	var platformText = jQuery('#platform option:selected').text();
	
	$j('#${fieldName}').append($j('<option></option>').val(platformId).text(platformText).attr('selected', 'selected'));
	var newTag = $j('<span/>', {
		id: '${fieldName}-tag-' + platformId,
		'class': 'tag',
		name: platformId
	}).text(platformText);
	$j('#${fieldName}-tags').append(newTag);
	newTag.hide().fadeIn('slow');
	jQuery('#platform').val(0);
}

function updatePlatforms() {
	jQuery('#platformwrapper').empty();
	jQuery.ajax({
		url: ajaxPlatformsURL,
		data: {
			vendorName:jQuery("#vendor").val(),
			measurementName:jQuery('#measurement').val(),
			technologyName:jQuery('#technology').val(),
		},
		success: function(response) {
			jQuery('#platformwrapper').html(response);
		}
	});
}
</g:javascript>

<%-- Tag box (visual display of tags) --%>
<div id="${fieldName}-tags" class="tagBox" name="${fieldName}">
    <g:each in="${values}" var="value">
        <span class="tag" id="${fieldName}-tag-${value.uniqueId}" name="${value.uniqueId}">${value.displayValue}</span>
    </g:each>
</div>

<%-- Hidden select field, keeps actual selected values --%>
<select id="${fieldName}" name="${fieldName}" multiple="multiple" style="display: none;">
    <g:each in="${values}" var="value">
        <option selected="selected" value="${value.uniqueId}">${value.displayValue}</option>
    </g:each>
</select>

<%-- Visible input --%>

<div style="background-color: #E4E4E4; float:left; padding: 8px; border-radius: 8px;">
    <div style="margin-right: 8px">
        <div style="fixed: left; line-height: 24px; font-style: italic; margin-right: 8px;">Filter on:</div>

        <div style="margin-right: 8px">

            <div class="textsmaller">Measurement</div>

            <div id="measurementwrapper">
                <tmpl:selectMeasurements measurements="${measurements}"/>
            </div>
        </div>

        <div style="margin-right: 8px">
            <div class="textsmaller">Technology</div>

            <div id="technologywrapper">
                <tmpl:selectTechnologies technologies="${technologies}"/>
            </div>
        </div>

        <div style="margin-right: 8px">
            <div class="textsmaller">Vendor</div>

            <div id="vendorwrapper">
                <tmpl:selectVendors vendors="${vendors}"/>
            </div>
        </div>
    </div>

    <div style="margin-right: 8px">
        <div style="fixed: left; line-height: 24px; font-style: italic; margin-right: 8px;">Add new platform:</div>

        <div id="platformwrapper">
            <tmpl:selectPlatforms/>
        </div>
    </div>
</div>