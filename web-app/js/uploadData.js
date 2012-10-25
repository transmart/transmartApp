function changeField(field, valueField) {
	var escapedFieldName = valueField.replace(".", "\\.");

	$j('#' + escapedFieldName).val('');
	$j('#' + escapedFieldName + "-input").val('').removeAttr('disabled').focus();
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

/*
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
*/

function acknowledgeColumn(column, requiredColumns) {
	$j('#columnsAll').append(column);
	//If this column is in the required list, remove it from the list
	var arrayIndex = $j.inArray(column, requiredColumns);
	if (arrayIndex > -1) {
		requiredColumns.splice(arrayIndex, 1);
	}
}

function showDataUploadForm() {
	
	//TODO Cheap Javascript validation in here - move to actual validator!
	$j('#studyErrors').empty();
	$j('#dataTypeErrors').empty();
	$j('#analysisNameErrors').empty();
	
	var errors = false;
	if ($j('#study').val() == null || $j('#study').val() == '') {
		$j('#studyErrors').html('<div class="fieldError">Please select a study</div>');
		errors = true;
	}
	if ($j('#dataType').val() == null || $j('#dataType').val() == "null" || $j('#dataType').val() == '') {
		$j('#dataTypeErrors').html('<div class="fieldError">Please select an analysis type</div>');
		errors = true;
	}
	if ($j('#analysisName').val() == null || $j('#analysisName').val() == '') {
		$j('#analysisNameErrors').html('<div class="fieldError">Please enter an analysis name</div>');
		errors = true;
	}
	if (errors) {
		return;
	}
	
	ANALYSIS_TYPE = $j('#dataType').val();
	var title = $j('#dataType option:selected').text();
	if (IS_EDIT) {
		$j('#dataFormTitle2').html('Edit ' + title + ' metadata')
	}
	else {
		$j('#dataFormTitle2').html('Upload ' + title + ' Data')
	}
	if (ANALYSIS_TYPE == 'EQTL') {
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
	//$j('#formPage2').css('visibility', 'visible');
	//$j('#formPage2').hide();
	$j('#formPage2').show();
}

function showAnalysisForm() {
	$j('#formPage2').hide();
	$j('#formPage1').show();
}

function removeTag(fieldName, tag) {
	var escapedFieldName = fieldName.replace(".", "\\.");
	//Attribute selector here gets around spaces in ID, which shouldn't be allowed... but is
	$j('[id=\'' + escapedFieldName + '-tag-' + tag + "\']").remove();
	$j('#' + escapedFieldName + ' option[value="' + tag + '"]').remove();
}

function addPlatform(field) {
	//Gather the relevant elements
	var typeFieldName = '#' + field + 'Name';
	var typeField = $j(typeFieldName);
	var vendorField = $j('#' + field + 'Vendor');
	var selectField = $j('#' + field);
	var tagDiv = $j('#' + field + '-tags');

	//If we have a non-null platform ID, prepare to add it
	var platformId = typeField.val();
	var platformName = $j(typeFieldName + " option:selected").text()
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
				value: row['accession'] //Using platform accession as the identifier!
			}).text(row['title']);
			element.append(newOpt);
		}
	});
	
}

function updateStudyTable(param) {
	
	$j('#studyDiv').empty().hide();
	
	request = $j.ajax({
		url: studyDetailUrl,
		type: 'POST',
		data: {'accession': param}
	});
	
	request.fail(function(jqXHR, textStatus) {
		if (jqXHR.responseText) {
			alert(jqXHR.responseText);
		}
	});
	
	request.done(function(msg) {
		
		$j('#studyDiv').html(msg).slideDown('slow');
	});
	
}


function generateBrowseWindow(nodeClicked)
{
      //Grab the URL from a JS object.
      var URLtoUse = studyBrowseWindowUrl
      
      jQuery('#divBrowseStudies').dialog(
                  {
                        modal: false,
                        open: function()
                        {
                              jQuery(this).load(URLtoUse)
                        },
                        height: 300,
                        width: 500,
                        title: nodeClicked,
                        show: 'fade',
                        hide: 'fade',
                        buttons: {"Select" : applyStudyBrowse}
                  })
}

function downloadTemplate() {
	var type = $j('#dataType').val();
	window.location = templateDownloadUrl + "?type=" + type;
}

function loadPlatformTypes(field, type) {
	var targetField = $j('#' + field + 'Name');
	var sourceField = $j('#' + field + 'Vendor');
	var vendor = sourceField.val();
	fillSelectAjax(targetField, platformTypesUrl, {vendor:vendor, type:type});
}


//After the user clicks select on the study popup, transfer the study ID and title and update the study table
function applyStudyBrowse() {
      //Loop through all the selected items.
      jQuery(".studyBrowseRow.selected").each(function(i, selected){
    	  var studyId = $j(this).attr('name');
    	  var studyName = $j('#studyBrowseName' + studyId).text();
    	  $j('#study-input').val(studyName).attr('disabled', 'disabled');
    	  $j('#study').val(studyId);
          updateStudyTable(studyId);
      })
      
      //This destroys our popup window.
      jQuery(this).dialog("destroy")
}

//For all tags - when clicked, call the remove tag function (remove them from the DOM and underlying select list)
$j('.tag').live('click', function(e) { removeTag($j(this).parent().attr('name'), $j(this).attr('name')); });
