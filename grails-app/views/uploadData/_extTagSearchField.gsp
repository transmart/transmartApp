<g:javascript>
Ext.onReady(function(){

	var escapedFieldName = '${fieldName}'.replace(".", "\\.");
	
	var combo = new Ext.app.SearchComboBox({
		id: "${fieldName}-combobox",
		renderTo: "${fieldName}-input",
		searchUrl: "${createLink([action:searchAction,controller:searchController])}",
		submitFn: function(param, text) {
			var combo = Ext.getCmp("${fieldName}-combobox");
			combo.setRawValue('');
			
			$j('#' + escapedFieldName).append($j('<option></option>').val(param).text(text).attr('selected', 'selected'));
			
			var newTag = $j('<span/>', {
				id: '${fieldName}-tag-' + param,
				'class': 'tag',
				name: param
			}).text(text);
			$j('#' + escapedFieldName + '-tags').append(newTag);
		},
		value: "",
		width: 470,
        onSelect: function(record) {
			this.collapse();
			if (record != null) {
				this.submitFn(record.data.id, record.data.keyword);
			}
		}
	});
});
</g:javascript>
<div id="${fieldName}-tags" class="tagBox" name="${fieldName}">
	<g:each in="${values}" var="value">
		<span class="tag" id="${fieldName}-tag-${value.key}" name="${value.key}">${value.value}</span>
	</g:each>
</div>
<select id="${fieldName}" name="${fieldName}" multiple="multiple" style="display: none;">
	<g:each in="${values}" var="value">
		<option selected="selected" value="${value.key}">${value.value}</option>
	</g:each>
</select>

<div style="background-color: #E4E4E4; float:left; padding: 8px; border-radius: 8px;">
	<div style="float: left; line-height: 24px; font-style: italic; margin-right: 8px;">Add new: </div>
	<div id="${fieldName}-input" style="float: left;"></div>
</div>