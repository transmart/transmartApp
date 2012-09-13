function changeField(field, valueField) {
	var escapedFieldName = valueField.replace(".", "\\.");
	
	var combo = Ext.getCmp(field);
	combo.setDisabled(false);
	combo.setRawValue('');
	$j('#' + escapedFieldName).val('');
	combo.focus();
}

function ieReadFileHeader(filename) 
{
	/* Disabled instant check for now
    try
    {
        var fso  = new ActiveXObject("Scripting.FileSystemObject"); 
        var fh = fso.OpenTextFile(filename, 1); 
        var contents = fh.ReadLine(); 
        fh.Close();
        return contents;
    }
    catch (e)
    {
        alert ("Could not open file: " + e.message);
    }
    */
}

//TODO Get required columns from model
var GWAS_COLUMNS = ['rs_id', 'p-value'];
var EQTL_COLUMNS = ['p value', 'RS ID', 'gene'];
var GWASM_COLUMNS = ['RS #', 'p-value'];

function verifyHeader() {
	$j('#columnsAll').html("Columns found: ");
	$j('#columnsNotFound').empty();
	
	var filepath = document.getElementById('file').value;
	var header = ieReadFileHeader(filepath);
	var columns = header.split(",");
	if (columns.length == 0) {
		$j('#columnsNotFound').html('Could not parse the file!');
	}
	else {

		var requiredColumns
		//TODO Get required columns from model
		if ($j('#analysisType').val() == 'gwas') {
			requiredColumns = GWAS_COLUMNS;
		}
		else if ($j('#analysisType').val() == 'eqtl') {
			requiredColumns = EQTL_COLUMNS;
		}
		else {
			requiredColumns = GWASM_COLUMNS;
		}
		 
		
		
		acknowledgeColumn(columns[0], requiredColumns);
		for (var i = 1; i < columns.length; i++) {
			$j('#columnsAll').append(", ");
			acknowledgeColumn(columns[i], requiredColumns);
		}

		if (requiredColumns.length > 0) {
			$j('#columnsNotFound').append('Columns missing: ' + requiredColumns[0]);
			for (var i = 1; i < requiredColumns.length; i++) {
				$j('#columnsNotFound').append(', ' + requiredColumns[i]);
			}
		}
	}				
}

function acknowledgeColumn(column, requiredColumns) {
	$j('#columnsAll').append(column);
	//If this column is in the required list, remove it from the list
	var arrayIndex = $j.inArray(column, requiredColumns);
	if (arrayIndex > -1) {
		requiredColumns.splice(arrayIndex, 1);
	}
}

function showDataUploadForm() {
	
	$j('#studyErrors').empty();
	$j('#analysisNameErrors').empty();
	//TODO Quick and nasty Javascript validation in here - move to actual validator!
	var errors = false;
	if ($j('#study\\.id').val() == null || $j('#study\\.id').val() == '') {
		$j('#studyErrors').html('<div class="fieldError">Please select a study</div>');
		errors = true;
	}
	if ($j('#analysisName').val() == null || $j('#analysisName').val() == '') {
		$j('#analysisNameErrors').html('<div class="fieldError">Please enter an analysis name</div>');
		errors = true;
	}
	if (errors) {
		return;
	}
	
	var type = $j('#dataType').val();
	var title = $j('#dataType option:selected').text();
	if (IS_EDIT) {
		$j('#dataFormTitle2').html('Edit ' + title + ' metadata')
	}
	else {
		$j('#dataFormTitle2').html('Upload ' + title + ' Data')
	}
	if (type == 'eqtl') {
		$j('#tagsLabel').html('Disease:');
		$j('#platformLabel').html('Genotype Platform:');
		$j('#expressionPlatformRow').show();
	}
	else {
		$j('#tagsLabel').html('Phenotype:');
		$j('#platformLabel').html('Platform:');
		$j('#expressionPlatformRow').hide();
	}
	$j('#formPage1').hide();

	//Gets around oddity with Ext combo box layout on display:none divs
	$j('#formPage2').css('visibility', 'visible');
	$j('#formPage2').hide();
	$j('#formPage2').show();
}

function showAnalysisForm() {
	$j('#formPage2').hide();
	$j('#formPage1').show();
}

function removeTag(fieldName, tag) {
	var escapedFieldName = fieldName.replace(".", "\\.");
	$j('#' + escapedFieldName + '-tag-' + tag).remove();
	$j('#' + escapedFieldName + ' option[value="' + tag + '"]').remove();
}

function addPlatform(field) {
	//Gather the relevant elements
	var typeField = $j('#' + field + 'Name');
	var vendorField = $j('#' + field + 'Vendor');
	var selectField = $j('#' + field);
	var tagDiv = $j('#' + field + '-tags');

	//If we have a non-null platform ID, prepare to add it
	var platformId = typeField.val();
	if (platformId == null || platformId == "null") { return; }
	
	//Stop if we already have this tag
	var existingPlatform = $j('option[value=' + platformId + ']', selectField);
	if (existingPlatform.length > 0) { typeField.val(null); return; }
	
	//Get text and value to add
	var valueToAdd = platformId;
	var textToAdd = vendorField.val() + ": " + platformId;

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

function fillSelectAjax(element, url, params) {
	
	request = $j.ajax({
		url: url,
		type: 'POST',
		data: params
	});
	
	request.fail(function(jqXHR, textStatus) {
		if (jqXHR.responseText) {
			alert(jqXHR.responseText);
		}
	});
	
	request.done(function(msg) {
		element.empty();
		var rows = msg['rows'];

		var newOpt = $j('<option/>', {
			value: 'null'
		}).text('Select...');
		element.append(newOpt);
		
		for (var i = 0; i < rows.length; i++) {
			var row = msg['rows'][i];
			newOpt = $j('<option/>', {
				value: row['title'] //Using platform name as the identifier!
			}).text(row['title']);
			element.append(newOpt);
		}
	});
	
}

$j('.tag').live('click', function(e) { removeTag($j(this).parent().attr('name'), $j(this).attr('name')); });
