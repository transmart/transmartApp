<g:javascript>
Ext.onReady(function(){	
	
	var escapedFieldName = '${fieldName}'.replace(".", "\\.");
	
	var combo = new Ext.app.SearchComboBox({
		id: "${fieldName}-combobox",
		renderTo: "${fieldName}-input",
		searchUrl: "${createLink([action:searchAction,controller:searchController])}",
		submitFn: function(param, text) {
			var combo = Ext.getCmp("${fieldName}-combobox");
			combo.setDisabled(true);
			$j('#' + escapedFieldName + '-combobox').addClass('locked');
			combo.setRawValue(text);
			$j('#' + escapedFieldName).val(param);
			updateStudyTable(param);
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
	
	combo.setRawValue('${label}');
	if (combo.getRawValue() != "") {
		combo.setDisabled(true);
		$j('#' + escapedFieldName + '-combobox').addClass('locked');
	}
});
</g:javascript>
<g:textField name="${fieldName}" style="display: none" value="${value}"/>
<div id="${fieldName}-input" style="float: left"></div>
 <a id="${fieldName}ChangeButton" class="upload" onclick="$j('#studyDiv').empty().slideUp('slow'); changeField('${fieldName}-combobox', '${fieldName}')">Change</a>