/** Add the timeout dialog for the session timeout */ 
function addTimeoutDialog(heartbeatURL, logoutURL)	{
	// Function to create the session timeout dialog
    // setup the dialog
    jQuery("#timeout-div").dialog({
    	autoOpen: false,
        modal: true,
        width: 400,
        height: 200,
        closeOnEscape: false,
        draggable: false,
        resizable: false,
        buttons: {
        	'Yes, Keep Working': function(){
        		// Just close the dialog. We pass a reference to this
        		// button during the init of the script, so it'll automatically
        		// resume once clicked
        		jQuery(this).dialog('close');
        	},
            'No, Logoff': function(){
            	jQuery.idleTimeout.options.onTimeout.call(this); // call logoutURL
            }
        }
    });
        
    // This is the idle timeout plugin
    jQuery.idleTimeout('#timeout-div', 'div.ui-dialog-buttonpane button:first', {
    	idleAfter: 1500,	 				// user is considered idle after 25 minutes of no movement (1500 seconds)
        pollingInterval: 600,  				// how often we check for heartbeat (10 minutes - 600 seconds) to ensure the server session does not timeout
        keepAliveURL: heartbeatURL,			// this URL will respond with the text shown below
        serverResponseEquals: 'ALIVE',  	// the response from heartbeat must equal this text
        onTimeout: function(){
        	window.location = logoutURL;	// redirect the user when they timeout.     
        },
        onIdle: function()	{
            jQuery(this).dialog("open");	// show the dialog when the user idles         
        },
        onCountdown: function(counter){
            jQuery("#timeout-countdown").html(counter); // update the counter span inside the dialog during each second of the countdown         
        },
            onResume: function(){         
            // the dialog is closed by a button in the dialog
            // no need to do anything else         
        }
    });	
}
	