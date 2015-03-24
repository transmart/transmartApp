function addPlatform(field) {
	//Gather the relevant elements
	var typeFieldName = '#' + field + 'Name';
	var typeField = $j(typeFieldName);
	var vendorField = $j('#' + field + 'Vendor');
	var selectField = $j('#' + field);
	var tagDiv = $j('#' + field + '-tags');

	//If we have a non-null platform ID, prepare to add it
	var platformId = typeField.val();
	var platformName = $j(typeFieldName + " option:selected").text();
	if (platformId == null || platformId == "null") { return; }
	
	//Stop if we already have this tag
	var existingPlatform = $j('option[value="' + platformId + '"]', selectField);
	if (existingPlatform.length > 0) { typeField.val(null); return; }
	
	//Get text and value to add
	var valueToAdd = platformId;
	var textToAdd = vendorField.val() + ": " + platformName;
	//Now add to the hidden select
	selectField.append($j('<option></option>').val(valueToAdd).text(textToAdd).attr('selected', 'selected'));
	
	//Create and add the visible tag to the page
	var newTag = $j('<span/>', {
		id: field + '-tag-' + valueToAdd,
		'class': 'tag',
		name: valueToAdd
	}).text(textToAdd);
	
	$j('#' + field + '-tags').append(newTag);
	newTag.hide().fadeIn('slow');
	typeField.val(null);
	
}

function loadPlatformTypes(field, type) {
	var targetField = $j('#' + field + 'Name');
	var sourceField = $j('#' + field + 'Vendor');
	var vendor = sourceField.val();
	fillSelectAjax(targetField, platformTypesUrl, {vendor:vendor, type:type});
}

function removeTag(fieldName, tag) {
	var escapedFieldName = fieldName.replace(".", "\\.");
	//Attribute selector here gets around spaces in ID, which shouldn't be allowed... but is
	$j('[id=\'' + escapedFieldName + '-tag-' + tag + "\']").remove();
    $j('[id=\'' + escapedFieldName + '-tag-' + tag + '-prefix' + "\']").remove();
    $j('[id=\'' + escapedFieldName + '-tag-' + tag + '-break' + "\']").remove();
    $j('#' + escapedFieldName + ' option[value="' + tag + '"]').remove();
}

$j('.tag').live('click', function(e) { removeTag($j(this).parent().attr('name'), $j(this).attr('name')); });