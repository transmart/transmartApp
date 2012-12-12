	function getTooltipOffset(e, tooltipDivId)  {
	    var xOffset;
	    var yOffset;		
		
	    var dw = jQuery(document).width();
	    var dh = jQuery(document).height();
	
	    var w = jQuery("#" + tooltipDivId).width() + 10;
	    var h = jQuery("#" + tooltipDivId).height() + 10;

	    
    	// will popup fit on right
    	if ((e.pageX + w) < dw)  {
    		xOffset = 10;	    
    	}
	    else {
		    // will popup fit to left	    
		    if ((e.pageX - w) > 0)  {
		    	xOffset = -1 * w
		    }
	    	else {
	    		xOffset = -1 * e.pageX;
	    	}
	    }

	    // will popup fit above
	    if ((e.pageY - h) > 0)  {
	    	yOffset = h
	    }
	    else {
	    	// will popup fit below
	    	if ((e.pageY + h) < dh)  {	    		
	    		yOffset = 10;
	    	}
	    	else {
	    		yOffset = e.pageY;
	    	}
	    }
	    
	    return {xOffset:xOffset, yOffset:yOffset};
	    
	}
	    	
