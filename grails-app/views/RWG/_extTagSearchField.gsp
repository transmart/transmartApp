<g:javascript>

jQuery(document).ready(function() {	
	
	var escapedFieldName = '${fieldName}'.replace(".", "\\.");
	jQuery("#" + escapedFieldName + "-input").autocomplete({
		source: '${createLink([action: searchAction, controller: searchController, params: [category: 'GENE_OR_SNP']])}',
		minLength:0,
		
		select: function(event, ui) {
			var id = ui.item.id;
			var name = ui.item.label;
			jQuery("#" + escapedFieldName + "-input").val('').focus();
			jQuery('#' + escapedFieldName).append(jQuery('<option></option>').val(id).text(name).attr('selected', 'selected'));
			var newTag = jQuery('<span/>', {
				id: '${fieldName}-tag-' + id,
				'class': 'tag',
				name: id
			}).text(name);
			jQuery('#' + escapedFieldName + '-tags').append(newTag);
			newTag.hide().fadeIn('slow');
			return false;
		}
	}).data("uiAutocomplete")._renderItem = function( ul, item ) {
		return jQuery('<li></li>')		
		  .data("item.autocomplete", item )
		  .append('<a><span class="category-' + item.category.toLowerCase() + '">' + item.category + '&gt;</span>&nbsp;<b>' + item.label + '</b> ' + item.synonyms + '</a>')
		  .appendTo(ul);
	};
});
    function removeTag(fieldName, tag) {
        var escapedFieldName = fieldName.replace(".", "\\.");
        //Attribute selector here gets around spaces in ID, which shouldn't be allowed... but is
        jQuery('[id=\'' + escapedFieldName + '-tag-' + tag + "\']").remove();
        jQuery('#' + escapedFieldName + ' option[value="' + tag + '"]').remove();
    }
    //For all tags - when clicked, call the remove tag function (remove them from the DOM and underlying select list)
jQuery('.tag').live('click', function(e) { removeTag(jQuery(this).parent().attr('name'), jQuery(this).attr('name')); });
</g:javascript>
<%-- Tag box (visual display of tags) --%>
<div id="${fieldName}-tags" class="tagBox">
    <g:each in="${values}" var="value">
        <span class="tag" id="${fieldName}-tag-${value.key}">${value.value}</span>
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
    <div style="float: left; line-height: 24px; font-style: italic; margin-right: 8px;">Add new:</div>
    <input id="${fieldName}-input" style="float: left; width: 600px;"/>
</div>
