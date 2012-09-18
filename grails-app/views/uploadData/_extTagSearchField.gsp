<g:javascript>

jQuery(document).ready(function() {	
	
	var escapedFieldName = '${fieldName}'.replace(".", "\\.");
	jQuery("#" + escapedFieldName + "-input").autocomplete({
		source: '${createLink([action:searchAction,controller:searchController])}',
		minLength:0,
		select: function(event, ui) {
			var diseaseId = ui.item.id;
			var diseaseName = ui.item.keyword;
			jQuery("#" + escapedFieldName + "-input").val('').focus();
			$j('#' + escapedFieldName).append($j('<option></option>').val(diseaseId).text(diseaseName).attr('selected', 'selected'));
			var newTag = $j('<span/>', {
				id: '${fieldName}-tag-' + diseaseId,
				'class': 'tag',
				name: diseaseId
			}).text(diseaseName);
			$j('#' + escapedFieldName + '-tags').append(newTag);
			newTag.hide().fadeIn('slow');
			
			return false;
		}
	}).data("autocomplete")._renderItem = function( ul, item ) {
		return jQuery('<li></li>')		
		  .data("item.autocomplete", item )
		  .append('<a><span class="category-' + item.category.toLowerCase() + '">' + item.category + '&gt;</span>&nbsp;<b>' + item.keyword + '</b></a>')
		  .appendTo(ul);
	};
});
</g:javascript>
<%-- Tag box (visual display of tags) --%>
<div id="${fieldName}-tags" class="tagBox" name="${fieldName}">
	<g:each in="${values}" var="value">
		<span class="tag" id="${fieldName}-tag-${value.key}" name="${value.key}">${value.value}</span>
	</g:each>
</div>

<%-- Hidden select field, keeps actual selected values --%>
<select id="${fieldName}" name="${fieldName}" multiple="multiple" style="display: none;">
	<g:each in="${values}" var="value">
		<option selected="selected" value="${value.key}">${value.value}</option>
	</g:each>
</select>

<%-- Visible input --%>
<div style="background-color: #E4E4E4; float:left; padding: 8px; border-radius: 8px;">
	<div style="float: left; line-height: 24px; font-style: italic; margin-right: 8px;">Add new: </div>
	<input id="${fieldName}-input" style="float: left; width: 600px;"/>
</div>