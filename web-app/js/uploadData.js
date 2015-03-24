var UPLOAD_STUDY_TYPE = 'Experiment';

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
	$j('#dataFormTitle2').html('Upload ' + title + ' Data');
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

function validateFile(event) {

    //TODO Cheap Javascript validation in here - move to actual validator!
    $j('#studyErrors').empty();

    var errors = false;
    if ($j('#study').val() == null || $j('#study').val() == '') {
        $j('#studyErrors').html('<div class="fieldError">Please select a study</div>');
        errors = true;
    }

    if ($j('#uploadFile').val() == null || $j('#uploadFile').val() == '') {
        $j('#uploadFileErrors').html('<div class="fieldError">Please select a file to upload</div>');
        errors = true;
    }

    if (errors) {
        event.preventDefault();
        return false;
    }
    return true;
}

function showAnalysisForm() {
	$j('#formPage2').hide();
	$j('#formPage1').show();
}

function removeTag(fieldName, tag) {
	var escapedFieldName = fieldName.replace(".", "\\.");
	//Attribute selector here gets around spaces in ID, which shouldn't be allowed... but is
	$j('[id=\'' + escapedFieldName + '-tag-' + tag + "\']").remove();
    $j('[id=\'' + escapedFieldName + '-tag-' + tag + '-prefix' + "\']").remove();
    $j('[id=\'' + escapedFieldName + '-tag-' + tag + '-break' + "\']").remove();
    $j('#' + escapedFieldName + ' option[value="' + tag + '"]').remove();
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

    //Check for availability of study folder
    $j('#studyNoFolderMessage').hide();

    $j.ajax({
        url: studyHasFolderUrl,
        type: 'POST',
        data: {'accession': param},
        success: function(response) {
            if (!response.found) {
                $j('#studyNoFolderMessage').show();
                $j('#studyNoFolderMessage').html("Error retrieving folder for this study: " + response.message + ". Please contact the administrator using the button on the top right.");
            }
        },
        failure: function(xhr) {
            if (xhr.responseText != null && xhr.responseText != "") {
                $j('#studyNoFolderMessage').html("Error retrieving folder for this study: " + xhr.responseText + ". Please contact the administrator using the button on the top right.");
            }
        }
    });

	$j('#studyDiv').empty().hide().slideDown('slow').addClass('ajaxloading');
	
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
		
		$j('#studyDiv').html(msg).removeClass('ajaxloading');
	});
	
}


function generateBrowseWindow()
{
      //Grab the URL from a JS object.
      var URLtoUse = studyBrowseWindowUrl;
      jQuery('#divBrowseStudies').empty().addClass('ajaxloading');

      jQuery('#divBrowseStudies').dialog(
                  {
                        modal: false,
                        open: function()
                        {
                              jQuery(this).load(URLtoUse + "?type=" + UPLOAD_STUDY_TYPE).removeClass('ajaxloading');
                        },
                        height: 300,
                        width: 500,
                        title: "Studies in " + (UPLOAD_STUDY_TYPE == 'i2b2'?"Dataset Explorer":"faceted search"),
                        show: 'fade',
                        hide: 'fade',
                        buttons: {"Select" : applyStudyBrowse}
                  });
}

function downloadTemplate() {
	var type = $j('#dataType').val();
	if ($j('#dataType').val() == null || $j('#dataType').val() == "null" || $j('#dataType').val() == '') {
		$j('#dataTypeErrors').html('<div class="fieldError">Please select an analysis type</div>');
		//alert("Select data type!");
		}
	else
		window.location = templateDownloadUrl + "?type=" + type;
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
      
      //This destroys our popup window.
      jQuery(this).dialog("destroy");
});

//For all tags - when clicked, call the remove tag function (remove them from the DOM and underlying select list)
$j('.tag').live('click', function(e) { removeTag($j(this).parent().attr('name'), $j(this).attr('name')); });
}

//on check
function isDataSensitive() {
	if (jQuery('#sensitiveFlag').is(':checked')) {
	    jQuery("#sensitiveDesc").show();
		jQuery("#sensitiveFlag").val('1');
		} 
	else {
		jQuery("#sensitiveDesc").hide();
		jQuery("#sensitiveFlag").val('0');
		}
}

function addResearchUnit(field) {
	//Gather the relevant elements
	var typeFieldName = '#' + field + 'Name';
	var typeField = $j(typeFieldName);
	var selectField = $j('#' + field);
	var tagDiv = $j('#' + field + '-tags');

	//If we have a non-null platform ID, prepare to add it
	var unitName = typeField.val();
	if (unitName == null || unitName == "null") { return; }
	
	//Stop if we already have this tag
	var existingunitName = $j('option[value="' + unitName + '"]', selectField);
	if (existingunitName.length > 0) { typeField.val(null); return; }
	var valueToAdd = unitName;
	var textToAdd = unitName;
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

addDiseaseTag = function(diseaseHierarchy, sourceAndCode, escapedFieldName) {
    var diseasePath = "";
    var diseases = diseaseHierarchy;
    var diseasePathName = "";
    var diseaseName = "";

    for (var i = 0; i < diseases.length; i++) {
        var disease = diseases[i];
        if (i != diseases.length-1) { //We don't want to include the last disease name, as this is in the tag!
            diseasePathName += disease.name + " > ";
        }
        else {
            diseaseName = disease.name;
        }
        diseasePath += disease.code + "/";
    }

    jQuery("#" + escapedFieldName + "-input").val('').focus();

    //Check for supersets - confirm and remove
    var conflicts = [];
    var subsets = [];
    var existingDiseases = $j('#' + escapedFieldName + ' option');
    for (var i = 0; i < existingDiseases.length; i++) {
        var pathToExamine = $j(existingDiseases[i]).text();
        if (pathToExamine.startsWith(diseasePath) || diseasePath.startsWith(pathToExamine)) {
            conflicts.push(i);
        }
    }

    //If we have conflicts, remove the originals by index.
    if (conflicts.length > 0) {
        var i = 0;
        var tags = $j('#' + escapedFieldName + '-tags .tag');
        for (i = 0; i < tags.length; i++) {
            if ($j.inArray(i, conflicts) > -1) {
                var tagToRemove = $j(tags[i]);
                removeTag(tagToRemove.parent().attr('name'), tagToRemove.attr('name'));
            }
        }
    }

    //Update hidden select box
    $j('#' + escapedFieldName).append($j('<option></option>').val(sourceAndCode).text(diseasePath).attr('selected', 'selected'));

    //Draw on-screen representation
    var newTagPrefix = $j('<span/>', {
        id: escapedFieldName + '-tag-' + sourceAndCode + '-prefix',
        'class': 'tagPrefixSpan',
        name: sourceAndCode + "-prefix"
    }).text(diseasePathName);

    var newTag = $j('<span/>', {
        id: escapedFieldName + '-tag-' + sourceAndCode,
        'class': 'tag',
        name: sourceAndCode
    }).text(diseaseName);

    var tagBreak = $j('<br/>', { id: escapedFieldName + '-tag-' + sourceAndCode + '-break' });

    if (newTagPrefix.text() != "") {
        $j('#' + escapedFieldName + '-tags').append(newTagPrefix);
    }

    $j('#' + escapedFieldName + '-tags').append(newTag).append(tagBreak);
    newTag.hide().fadeIn('slow');

    return false;
};

addObservationTag = function(observationName, sourceAndCode, escapedFieldName) {
    jQuery("#" + escapedFieldName + "-input").val('').focus();
    $j('#' + escapedFieldName).append($j('<option></option>').val(sourceAndCode).text(observationName).attr('selected', 'selected'));
    var newTag = $j('<span/>', {
        id: escapedFieldName + '-tag-' + sourceAndCode,
        'class': 'tag observationtag',
        name: sourceAndCode
    }).text(observationName);
    var tagBreak = $j('<br/>', { id: escapedFieldName + '-tag-' + sourceAndCode + '-break' });

    $j('#' + escapedFieldName + '-tags').append(newTag).append(tagBreak);
    newTag.hide().fadeIn('slow');

    return false;
};

jQuery(document).ready(function() {

    jQuery('#uploadFilePane').hide();
    jQuery('#uploadFileButton').hide();

    jQuery('body').on('click', '.sidebarRadio', function() {
        jQuery('.sidebarRadio').removeClass('selected');
        jQuery(this).addClass('selected');

        if (jQuery(this).attr('id') == 'uploadAnalysisRadio') {
            UPLOAD_STUDY_TYPE = 'Experiment';
            jQuery('#uploadAnalysisPane').show();
            jQuery('#enterMetadataButton').show();
            jQuery('#uploadFileButton').hide();
            jQuery('#uploadFilePane').hide();
            jQuery('#studyDiv').empty().slideUp('slow');
            changeField('study-combobox', 'study');
            jQuery('#formPage2').hide();
            jQuery('#formPage1').show();
            jQuery('.dataFormTitle').text("Upload Analysis Data");
        }
        else {
            if (jQuery(this).attr('id') == 'uploadFileDatasetExplorerRadio') {
                UPLOAD_STUDY_TYPE = 'i2b2';
                jQuery('.dataFormTitle').text("Upload File to Dataset Explorer");
            }
            else if (jQuery(this).attr('id') == 'uploadFileRadio') {
                UPLOAD_STUDY_TYPE = 'Experiment';
                jQuery('.dataFormTitle').text("Upload File to Faceted Search");
            }
            jQuery('#uploadAnalysisPane').hide();
            jQuery('#enterMetadataButton').hide();
            jQuery('#uploadFileButton').show();
            jQuery('#uploadFilePane').show();
            jQuery('#studyDiv').empty().slideUp('slow');
            changeField('study-combobox', 'study');
            jQuery('#formPage2').hide();
            jQuery('#formPage1').show();

        }
    });
});

