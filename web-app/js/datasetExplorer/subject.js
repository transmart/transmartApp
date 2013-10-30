GLOBAL_selectedObservations = null;
function displayProfile(subjectId, patientId, trial){
	var windowWidth = jQuery(window).width();
	var dialogWidth = windowWidth*.8;
	
	var windowHeight = jQuery(window).height();
	var dialogHeight = windowHeight*.8;
	
	jQuery.ajax({
		  url: pageInfo.basePath + '/subject/profile',
		  success:function(data){
			  						jQuery( "#subjectProfile" ).remove();
			  						jQuery( "#subjectProfileWrapper" ).html(data);
			  						jQuery( "#subjectProfile" ).dialog({
			  							title:"Subject Profile View",
			  							minHeight:dialogHeight,
			  							minWidth:dialogWidth,
			  							modal:true,
			  							open: function( event, ui ) {
			  								jQuery(document).ready(function(){
				  								jQuery("#selectedObservations").multiselect();
				  								//jQuery("#selectedTimepointData").multiselect();
				  								plotGraph();
				  								setSelectedObservation(subjectId);// set all the currently selected items from the drop down list to a global variable				  								
			  								});
			  							},
			  							close: function( event, ui ) {
			  								jQuery( "#subjectProfile" ).remove();
			  							}
			  						});
			  					},
		  failure:function(data){alert("Server error getting subject profile");},
		  data: {subjectId:subjectId, patientId:patientId, trial:trial}
		});
}

function getObservations(subjectId){
	var selectedObservations = jQuery("#selectedObservations").multiselect("getChecked");
	var selectedObservationValues = "";
	for(var i = 0; i<selectedObservations.length; i++){
		if(i==0){
			selectedObservationValues+=selectedObservations[i].value;
		}else{
			selectedObservationValues+="|"+selectedObservations[i].value;
		}
	}
	
	jQuery.ajax({
		url: pageInfo.basePath + '/subject/observations',
		success:function(data){
			jQuery("#observationDetails").html(data);
		},
		failure:function(data){alert("Server error getting observation data");},
		data:{subjectId: subjectId, selectedObservationValues: selectedObservationValues}
	})
}

function plotGraph(){
	var plotData = [{label:"Subject Characteristic", data:[[0, 0], [1, 1]] }];
	var plotOptions = {
		yaxis: { max: 5 }
	};  
	
	jQuery.plot(jQuery("#placeholder"), plotData, plotOptions);
}//

function spNavigation(currentSubjectId,currentPatientId, currentTrial,isNext){
	
	var subjectId;
	var patientId;
	
	var gridViewTable = jQuery('#gridViewTable').dataTable(); 
 
	var subjectIdArray = gridViewTable.fnGetColumnData(0,false,false,false);
	var patientIdArray = gridViewTable.fnGetColumnData(1,false,false,false);	
	var trialArray = gridViewTable.fnGetColumnData(3,false,false,false);
	
	
	if(isNext){
		subjectId =	getNextId(subjectIdArray, currentSubjectId.toString());
		patientId =	getNextId(patientIdArray, currentPatientId.toString());
		trial = getNextTrialId(trialArray, patientIdArray, currentPatientId.toString());
	}else{
		subjectId = getPreviousId(subjectIdArray, currentSubjectId.toString());
		patientId = getPreviousId(patientIdArray, currentPatientId.toString());
		trial = getPreviousTrialId(trialArray, patientIdArray, currentPatientId.toString());
	}			
	 
	if(subjectId == null || patientId == null ){
		alert('Reached end of the list');		
	}else{
			//set the global object with selected observations 
			getSelectedObservation(currentSubjectId);// get all the currently selected items in the drop down list			
			displayProfile(subjectId, patientId, trial);		
		}
}//

//Special case for trial, because trial number are similar
function getNextTrialId(array,patArray, currPatId){
	if (patArray.indexOf(currPatId) < patArray.length){
		return array[patArray.indexOf(currPatId) + 1] ;
	}else if (patArray.indexOf(currPatId) == patArray.length){
		return array[patArray.indexOf(patArray.length - 1)];
	}else{
		return null;
	}
}//

//Special case for trial, because trial number are similar
function getPreviousTrialId(array,patArray, currPatId){
	if (patArray.indexOf(currPatId) <= patArray.length && patArray.indexOf(currPatId) != 0 ){
		return array[patArray.indexOf(currPatId) - 1] ;
	}else if (patArray.indexOf(currPatId) == 0){
		return null;
	}
}//
	

function getNextId(array,id){
	if (array.indexOf(id) < array.length){
		return array[array.indexOf(id) + 1] ;
	}else if (array.indexOf(id) == array.length){
		return array[array.indexOf(array.length - 1)];
	}else{
		return null;
	}	
}//

function getPreviousId(array,id){
	if (array.indexOf(id) <= array.length && array.indexOf(id) != 0 ){
		return array[array.indexOf(id) - 1] ;
	}else if (array.indexOf(id) == 0){
		return null;
	}
}//


function getSelectedObservation(subjectId){
	var selectedObservations = jQuery("#selectedObservations").multiselect("getChecked");
	var selectedObservationValues = "";
	GLOBAL_selectedObservations = new Array();
	for(var i = 0; i<selectedObservations.length; i++){
		if(i==0){
			GLOBAL_selectedObservations.push(selectedObservations[i].value);
		}else{
			GLOBAL_selectedObservations.push(selectedObservations[i].value);			
		}	
	}
}//	
	
function setSelectedObservation(subjectId){	
		if (GLOBAL_selectedObservations == null) return;
		var options = jQuery('#selectedObservations option');
		options.each(function(index,option){
				//alert(option.value);		
			for(var i = 0; i < GLOBAL_selectedObservations.length; i++ ){	
				if (option.value == GLOBAL_selectedObservations[i]){				
					option.defaultSelected = true;
				}
			}
		 }													
		);	
		jQuery("#selectedObservations").multiselect('refresh');
		getObservations(subjectId); // run the submit button to get all the selected observations
		GLOBAL_selectedObservations = null; // reset the selection to null
	}//
