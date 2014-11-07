<g:javascript>
jQuery(document).ready(function() {

	var escapedFieldName = '${fieldName}'.replace(".", "\\.");
	jQuery("#" + escapedFieldName + "-input").autocomplete({
		source: function(request, response) {
		    jQuery.ajax({
                url: '${createLink([action: searchAction, controller: searchController])}',
		        type: 'POST',
		        data: {'term': request['term'], ${paramString ?: "'prm':'prm'"}},
                success: function(responseText) { response(responseText) },
                failure: function(xhr) { alert(xhr.responseText); response(responseText); }
		    });
		},
		minLength:0,
		select: function(event, ui) {
			var studyId = ui.item.id;
			var studyName = ui.item.keyword;
			jQuery("#" + escapedFieldName + "-input").val(studyName).attr('disabled', 'disabled').blur();
			jQuery("#" + escapedFieldName).val(studyId);
			updateStudyTable(studyId);
			return false;
		}
	}).data("autocomplete")._renderItem = function( ul, item ) {
		return jQuery('<li></li>')
		  .data("item.autocomplete", item )
		  .append('<a><span
        class="category-' + item.category.toLowerCase() + '">' + item.category + '&gt;</span>&nbsp;<b>' + item.keyword + '</b>
</a>')
		  .appendTo(ul);
	};
});

</g:javascript>
<g:textField name="${fieldName}" style="display: none" value="${value}"/>
<g:if test="${label}">
    <input id="${fieldName}-input" style="width: ${width}px" value="${label}" disabled="disabled"/>
</g:if>
<g:else>
    <input id="${fieldName}-input" style="width: ${width}px"/>
</g:else>