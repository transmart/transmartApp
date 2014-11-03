<g:javascript>

jQuery(document).ready(function() {	
	
	var escapedFieldName = '${fieldName}'.replace(".", "\\.");
	jQuery("#" + escapedFieldName + "-input").autocomplete({
		source: function( request, response ) {
			jQuery.ajax({
				url: '${createLink([action: searchAction, controller: searchController])}',
				data: {
					term: request.term,
					codeTypeName: '${codeTypeName}'
				},
				success: function( data ) {
					response( jQuery.map( data, function(item) {
						return {
							category: item.category,
							keyword: item.keyword,
							sourceAndCode: item.sourceAndCode,
							id: item.id,
							display: item.display
						}
					}));
				}
			});
		},
			
		minLength:0,
		
		select: function(event, ui) {
			var sourceAndCode = ui.item.sourceAndCode;
			var diseaseName = ui.item.keyword;
			jQuery("#" + escapedFieldName + "-input").val('').focus();
			$j('#' + escapedFieldName).append($j('<option></option>').val(sourceAndCode).text(diseaseName).attr('selected', 'selected'));
			var newTag = $j('<span/>', {
				id: '${fieldName}-tag-' + sourceAndCode,
				'class': 'tag',
				name: sourceAndCode
			}).text(diseaseName);
			$j('#' + escapedFieldName + '-tags').append(newTag);
			newTag.hide().fadeIn('slow');
			
			return false;
		}
	}).data("autocomplete")._renderItem = function( ul, item ) {
	
		var resulta = '<a><span
        class="category-' + item.category.toLowerCase() + '">' + item.category + '&gt;</span>&nbsp;<b>' + item.keyword + '</b>&nbsp;';
if (item.synonyms != null) {
resulta += (item.synonyms + '</a>');
		}
		else {
			resulta += '</a>';
		}
		
		var returnElement = jQuery('<li></li>')		
		  .data("item.autocomplete", item )
		  .append(resulta)
		  .appendTo(ul);
		  
		ul.css('height', '');
		
		//If this addition would expand the list off the screen, limit its height and add a scrollbar
		var windowHeight = jQuery(window).height();
		var inputPosition = jQuery("#${fieldName}-input").offset().top;
		var ulHeight = ul.height();
		var bottomY = inputPosition + ulHeight + 20;
		
		if (bottomY > windowHeight) {
			ul.height(windowHeight - inputPosition - 30);
			ul.css('overflow', 'auto');
		}
		  
		return returnElement;
	};
});
</g:javascript>
<%-- Tag box (visual display of tags) --%>
<div id="${fieldName}-tags" class="tagBox" name="${fieldName}">
    <g:each in="${values}" var="value">
        <span class="tag" id="${fieldName}-tag-${value.uniqueId}" name="${value.uniqueId}">${value.codeName}</span>
    </g:each>
</div>

<%-- Hidden select field, keeps actual selected values --%>
<select id="${fieldName}" name="${fieldName}" multiple="multiple" style="display: none;">
    <g:each in="${values}" var="value">
        <option selected="selected" value="${value.uniqueId}">${value.codeName}</option>
    </g:each>
</select>

<%-- Visible input --%>
<div style="background-color: #E4E4E4; float:left; padding: 8px; border-radius: 8px;">
    <div style="float: left; line-height: 24px; font-style: italic; margin-right: 8px;">Add new:</div>
    <input id="${fieldName}-input" style="float: left; width: 600px;"/>
</div>