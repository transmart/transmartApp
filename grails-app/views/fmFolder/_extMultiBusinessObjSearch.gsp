<g:javascript>

jQuery(document).ready(function() {	
	
	var escapedFieldName = '${fieldName}'.replace(".", "\\.");
	jQuery("#" + escapedFieldName + "-input").autocomplete({
		source: function( request, response ) {
			jQuery.ajax({
				url: '${createLink([action:searchAction,controller:searchController])}',
				data: {
					term: request.term,
				},
				success: function( data ) {
					response( jQuery.map( data, function(item) {
						return {
							category: item.category,
							label: item.label,
							sourceAndCode: item.id,
							id: item.id,
							display: item.display,
							synonyms: item.synonyms
						}
					}));
				}
			});
		},
			
		minLength:0,
		
		select: function(event, ui) {
			var sourceAndCode = ui.item.sourceAndCode;
			var displayName = ui.item.label;
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
		  .append('<a><span class="category-' + item.category.toLowerCase() + '">' + item.category + '&gt;</span>&nbsp;<b>' + item.label + '</b>&nbsp;' + item.synonyms + '</a>')
		  .appendTo(ul);
	};
});
</g:javascript>

<%-- Visible input --%>
<div style="width: 100%" id="genotypePlatform-tags" class="tagBox" name="genotypePlatform">
	<g:each in="${genotypePlatforms}" var="value">
		<span class="tag" id="genotypePlatform-tag-${value.key}" name="${value.key}">${value.value}</span>
	</g:each>
</div>
							
<div style="background-color: #E4E4E4; float:left; padding: 8px; border-radius: 8px;">
	<div style="float: left; line-height: 24px; font-style: italic; margin-right: 8px;">Add new: </div>
	<input id="${fieldName}-input" style="float: left; width: 600px;"/>
</div>

<div style="float: left; margin-right: 8px">
	<div class="textsmaller">Measurement</div>
	<g:select style="width: 400px" name="measurement" noSelection="${['null':'Select...']}" from="${measurements}" onChange="loadPlatformTypes('genotypePlatform', 'SNP')"/>
</div>
<div style="float: left; margin-right: 8px">
	<div class="textsmaller">Technology</div>
	<g:select style="width: 400px" name="technology" noSelection="${['null':'Select...']}" from="${technology}" onChange="loadPlatformTypes('genotypePlatform', 'SNP')"/>
</div>
<div style="float: left; margin-right: 8px">
	<div class="textsmaller">Vendor</div>
	<g:select style="width: 400px" name="vendor" noSelection="${['null':'Select...']}" from="${vendors}" onChange="loadPlatformTypes('genotypePlatform', 'SNP')"/>
</div>

<div style="float: left">
	<div class="textsmaller">Platform</div>
	<g:select style="width: 200px" name="genotypePlatformName" onchange="addPlatform('genotypePlatform')"/>
	<select id="genotypePlatform" name="genotypePlatform" multiple="multiple" style="display: none;">
		<g:each in="${platforms}" var="value">
			<option selected="selected" value="${value.key}">${value.value}</option>
		</g:each>
	</select>
</div>
