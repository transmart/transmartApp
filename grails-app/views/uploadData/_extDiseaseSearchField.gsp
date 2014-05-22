<g:javascript>

jQuery(document).ready(function() {

	var escapedFieldName = '${fieldName}'.replace(".", "\\.");

    <g:each in="${values}" var="phenotype">
        <g:if test="${phenotype.value.type == 'DISEASE'}">
            $j.ajax({
                url: '${createLink([action:'getMeshLineage',controller:'disease'])}',
                data: {
                    code: '${phenotype.value.code}'
                },
                success: function(json) {
                    addDiseaseTag(json.diseases, 'MESH:' + '${phenotype.value.code}', escapedFieldName);
                }
        });
        </g:if>
        <g:else>
            addObservationTag('${phenotype.value.code}', '${phenotype.key}', escapedFieldName);
        </g:else>
    </g:each>

    jQuery("#" + escapedFieldName + "-input").autocomplete({
        source: function( request, response ) {
            jQuery.ajax({
                url: '${createLink([action:searchAction,controller:searchController])}',
				data: {
					type: ANALYSIS_TYPE,
					term: request.term
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
			//Create a space for the new tag while we're working out the hierarchy
			if (ui.item.category == "DISEASE") {
                $j.ajax({
                    url: '${createLink([action:'getMeshLineage',controller:'disease'])}',
                    data: {
                        code: ui.item.id
                    },
                    success: function(json) {
                        addDiseaseTag(json.diseases, sourceAndCode, escapedFieldName);
                    },
                    error: function(xhr) {
                        alert(xhr.responseText);
                    }
                });
            }
            else { //For observations
                addObservationTag(diseaseName, sourceAndCode, escapedFieldName);
            }
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
</div>

<%-- Hidden select field, keeps actual selected values --%>
<select id="${fieldName}" name="${fieldName}" multiple="multiple" style="display: none">
</select>

<%-- Visible input --%>
<div style="background-color: #E4E4E4; float:left; padding: 8px; border-radius: 8px;">
    <div style="float: left; line-height: 24px; font-style: italic; margin-right: 8px;">Add new: </div>
    <input id="${fieldName}-input" style="float: left; width: 600px;"/>
</div>