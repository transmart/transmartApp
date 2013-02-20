<g:javascript>

jQuery(document).ready(function() {	
	
	var escapedFieldName = '${fieldName}'.replace(".", "\\.");
	jQuery("#" + escapedFieldName + "-input").autocomplete({
		source: function( request, response ) {
		
			jQuery.ajax({
				url: '${createLink([action:searchAction,controller:searchController])}',
				data: {
					term: request.term,
					vendor:jQuery("#vendor").val(),
					measurement:jQuery('#measurement').val(),
					technology:jQuery('#technology').val(),
				},
				success: function( data ) {
					response( jQuery.map( data, function(item) {
						return {
							category: item.category,
							label: item.label,
							sourceAndCode: item.id,
							id: item.id,
							synonyms: item.synonyms,
							display: item.display
						}
					}));
				}
			});
		},
			
		minLength:0,
		
		select: function(event, ui) {
			var sourceAndCode = ui.item.sourceAndCode;
			 
			var split = ui.item.label.split('--');
			var displayName = split[0]
			jQuery("#" + escapedFieldName + "-input").val('').focus();
			$j('#' + escapedFieldName).append($j('<option></option>').val(sourceAndCode).text(displayName).attr('selected', 'selected'));
			var newTag = $j('<span/>', {
				id: '${fieldName}-tag-' + sourceAndCode,
				'class': 'tag',
				name: sourceAndCode
			}).text(displayName);
			$j('#' + escapedFieldName + '-tags').append(newTag);
			newTag.hide().fadeIn('slow');
			
			return false;
		}
	}).data("autocomplete")._renderItem = function( ul, item ) {
		return jQuery('<li></li>')		
		  .data("item.autocomplete", item )
		  .append('<a><span class="category-' + item.category.toLowerCase() + '">' + item.category + '&gt;</span>&nbsp;<b>' + item.label + '</b></a>')
		  .appendTo(ul);
	};
});
</g:javascript>

<%! import bio.* %>  


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
	<div style="float: left; margin-right: 8px">
	<div style="fixed: left; line-height: 24px; font-style: italic; margin-right: 8px;">Filter on: </div>
	<div style="float: left; margin-right: 8px">
	
	
	<div class="textsmaller">Measurement</div>
	<div id="measurementwrapper">
	<g:select style="width: 400px" id="measurement" name="measurement" noSelection="${['null':'Select...']}" from="${measurements}"
			onchange="${remoteFunction(action:'ajaxTechnologies', update: 'technologywrapper', params:'\'queryType=measurement&measurementName=\' + this.value')}"/>
	</div>
	</div>
	
	<div style="float: left; margin-right: 8px">
	<div class="textsmaller">Technology</div>
	<div id="technologywrapper">
	<g:select style="width: 400px" id="technology" name="technology" noSelection="${['null':'Select...']}" from="${technologies}" 
			onchange="${remoteFunction(action:'ajaxVendors', update: 'vendorwrapper', params:'\'queryType=technology&measurementName=\'+document.getElementById(\'measurement\').value+\'&technologyName=\' + this.value' )}"/>
	</div>
	</div>
	
	<div style="float: left; margin-right: 8px">
	<div id="technologywrapper">
	<div class="textsmaller">Vendor</div>
		<g:select style="width: 400px" id="vendor" name="vendor" noSelection="${['null':'Select...']}" from="${vendors}" />
	</div>
	</div>
	</div>
	<div style="float: left; margin-right: 8px">
	<div style="fixed: left; line-height: 24px; font-style: italic; margin-right: 8px;">Add new platform: </div>
	<input id="${fieldName}-input" style="float: left; width: 600px;"/>
	</div>
</div>