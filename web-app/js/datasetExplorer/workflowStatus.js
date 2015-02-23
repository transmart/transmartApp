var thisJobName = null;
function setJobNameFromRun(jobNameFromRun) {
    thisJobName = jobNameFromRun;
}

function runWorkflowInBackground() {
    $j(this).dialog("option", "hide", 'slide');
    $j(this).dialog('close');
}

function cancelWorkflow() {
    $j.getJSON(pageInfo.basePath + "/asyncJob/canceljob"
        , {jobName: thisJobName}
        , function (json) {
            $j("#dialog-modal").dialog("option", "hide", 'slide');
            $j("#dialog-modal").dialog('close');
        }
    );
}

function createWorkflowStatus(parentElem, noTitleBar) {
    destroyWorkflowStatus();

    var maskDiv = $j(document.createElement('div')).attr({id: 'mask'});
    maskDiv.css('z-index', 10000);
    $j('#dataAssociationBody').append(maskDiv);


    //Add new modal-dialog
    var progressBarDiv = $j(document.createElement('div')).attr({id: 'progress-bar'});
    var progressStatusSpan = $j(document.createElement('span')).attr({id: 'progress-status'});
    progressStatusSpan.html('Running analysis');
    var progressStatusImg = $j(document.createElement('div')).attr({id: 'progress-img'});
    var progressTextDiv = $j(document.createElement('div')).attr({id: 'progress-text'});
    progressTextDiv.append(progressStatusImg);
    progressTextDiv.append(progressStatusSpan);

    var modalDialogDiv = $j(document.createElement('div')).attr({id: 'dialog-modal'});
    modalDialogDiv.append(progressBarDiv);
    modalDialogDiv.append(progressTextDiv);

    parentElem.append(modalDialogDiv);
    $j("#progress-img").attr('class', 'progress-spinner');

    $j("#mask").fadeTo(500, 0.25);

    var d = $j("#dialog-modal").dialog({
        height: 130, minHeight: 130, maxHeight: 130, width: 300, minWidth: 250, maxWidth: 350, closeOnEscape: false, show: { effect: 'drop', direction: "up" }, hide: { effect: 'fade', duration: 200 }, dialogClass: 'dialog-modal', title: 'Workflow Status', position: {
            my: 'left top',
            at: 'center',
            of: parentElem
        }, buttons: {
            "Stop Analysis": cancelWorkflow
        }
        //To hide the header of the dialog
        , create: function (event, ui) {
            if (noTitleBar) $j(".ui-widget-header", $(ui)).hide();
        }, close: function (event, ui) {
            $j("#mask").hide();
            $j("#mask").remove();
            $j("#dialog-modal").dialog('destroy');
            //$j('#mask').remove();
        }, zIndex: 10001
        //, modal: true
        , autoOpen: false
    });
    d.parent('.ui-dialog').appendTo($j('#dataAssociationBody'));
    $j("#dialog-modal").dialog('open');

    $j("#progress-bar").progressbar({
        value: 5
    });
}

function updateWorkflowStatus(jobStatusInfo) {
    if (jobStatusInfo != undefined) {
        var value = $j('#progress-bar').progressbar('option', 'value');
        var status = $j('#progress-status').html();

        if (jobStatusInfo.jobStatus == 'Error') {
            showWorkflowStatusError(jobStatusInfo);
            $j('#progress-img').attr('class', 'progress-error');
            return;
        }

        if ($j('#progress-status').html() != jobStatusInfo.jobStatus) {
            if (jobStatusInfo.jobStatus == 'Completed')    value = 100;
            else if (value < 100 && value + 10 < 100) value = value + 10;
            else if (value < 100 && value + 10 >= 100) value = value;
        }

        $j('#progress-bar').progressbar('option', 'value', value);
        $j('#progress-img').attr('class', 'progress-spinner');
        $j('#progress-status').html(jobStatusInfo.jobStatus);

        if (jobStatusInfo.jobStatus == 'Completed') {
            $j('#progress-img').attr('class', 'progress-completed');
            $j("#dialog-modal").dialog().dialog('close');
        }
    }
}

function showWorkflowStatusError(jobStatusInfo) {
    var jobStatusHeader = null;
    var jobStatusMsg = null;

    if (jobStatusInfo.errorType == 'data') {
        jobStatusHeader = 'Please verify the selected data';
        jobStatusMsg = 'Unable to complete job ' + jobStatusInfo.jobName + ': ' + jobStatusInfo.jobException;
    } else {
        jobStatusHeader = 'Error running job ' + jobStatusInfo.jobName + '.';
        jobStatusMsg = jobStatusInfo.jobException;
    }

    showWorkflowStatusErrorDialog(jobStatusHeader, jobStatusMsg);
}

function showWorkflowStatusErrorDialog(jobStatusHeader, jobStatusMsg) {
    var errorDialogDiv = $j(document.createElement('div')).attr({id: 'error-dialog-modal'});
    errorDialogDiv.html(jobStatusMsg);
    $j("#dialog-modal").parent().append(errorDialogDiv);

    var ed = $j("#error-dialog-modal").dialog({
        height: 100, minHeight: 90, maxHeight: 120, width: 300, minWidth: 250, maxWidth: 350, resizable: true, closeOnEscape: true, show: { effect: 'fade', duration: 200 }, hide: { effect: 'fade', duration: 200 }, dialogClass: 'dialog-modal', title: jobStatusHeader, position: {
            my: 'left top',
            at: 'center',
            of: $j("#dialog-modal").parent()
        }, create: function (event, ui) {
            /*setTimeout(function(){
             $j("#error-dialog-modal").dialog('close');
             $j("#dialog-modal").dialog('close');
             }, 10000);*/
            $j("#mask").hide();
            $j("#mask").remove();
        }, close: function (event, ui) {
            $j("#error-dialog-modal").dialog().dialog('close');
            $j("#dialog-modal").dialog().dialog('close');
        }, zIndex: 10002, autoOpen: false
    });

    ed.parent('.ui-dialog').appendTo($j('#dataAssociationBody'));
    $j("#error-dialog-modal").dialog('open');
}

function destroyWorkflowStatus() {
    //Remove existing error div element
    var errorDiv = $j('#error-dialog-modal');
    if (errorDiv != undefined) errorDiv.remove();

    //Remove existing mask div element
    var maskDiv = $j('#mask');
    if (maskDiv != undefined) maskDiv.remove();

    //Remove existing dialog-modal div element
    var existingModalDialogDiv = $j('#dialog-modal');
    if (existingModalDialogDiv != undefined) existingModalDialogDiv.remove();

    // a workaround for a flaw in the demo system (http://dev.jqueryui.com/ticket/4375), ignore!
    $j("#dialog:ui-dialog").dialog("destroy");
}
