<g:javascript>
jQuery(document).ready(function() {	
	
	var escapedFieldName = '${fieldName}'.replace(".", "\\.");
	jQuery("#" + escapedFieldName + "-input").autocomplete({
		source: '${createLink([action:searchAction,controller:searchController,params:[category:'GENE_OR_SNP']])}',
		minLength:0,
		select: function(event, ui) {
			var id = ui.item.id;
			var name = ui.item.label;
			jQuery("#" + escapedFieldName + "-input").val(name).attr('disabled', 'disabled').blur();
			jQuery("#" + escapedFieldName).val(id);
			return false;
		}
	}).data("autocomplete")._renderItem = function( ul, item ) {
		return jQuery('<li></li>')
		  .data("item.autocomplete", item )
		  .append('<a><span class="category-' + item.category.toLowerCase() + '">' + item.category + '&gt;</span>&nbsp;<b>' + item.label + '</b> ' + item.synonyms + '</a>')
		  .appendTo(ul);
	};
});

function changeField(field, valueField) {
	var escapedFieldName = valueField.replace(".", "\\.");

	jQuery('#' + escapedFieldName).val('');
	jQuery('#' + escapedFieldName + "-input").val('').removeAttr('disabled').focus();
}

</g:javascript>
<g:textField name="${fieldName}" style="display: none" value="${value}"/>
<g:if test="${label}">
	<input id="${fieldName}-input" style="width: ${width}px" value="${label}" disabled="disabled"/>
</g:if>
<g:else>
	<input id="${fieldName}-input" style="width: ${width}px"/>
</g:else>
