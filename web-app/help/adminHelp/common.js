
// Global -----------------------
var popupState = -1; // -1 - unknown; 0 - loading; 1 - loaded;
var popupDocument = null;
var popClientPos = null;
var popUpShown = null;
var nstextHeight = -1;
var _d2hInlinePopup = null;
var _d2hIndex = new d2hIndexTable();
var _d2hPopupMenu = null;
var _isPrinting = false;
var _isTopicOpenedFromTOC = false;
// ------------------------------

// Constants
var d2hNormalButton = 1;
var d2hSelectedButton = 2;
var d2hHoverButton = 3;
var d2hPassiveButton = 4;

// Border adjustment modes
var d2hRemoveRight = 1;
var d2hRemoveLeft = 2;
var d2hRestoreTopicLeft = 3;

// Variable names
var d2hLeftPaneCaption = "%LeftPaneCaption%";
var d2hTopicTitle = "%TopicTitle%";
var d2hTopicURL = "%TopicURL%";

function isSafari()
{
    var strUserAgent = navigator.userAgent.toLowerCase();
    var res = strUserAgent.indexOf("safari") == -1 || strUserAgent.indexOf("chrome") != -1 ? false : true;
    return res;
}

function isChrome() {
    var strUserAgent = navigator.userAgent.toLowerCase();
    var res = strUserAgent.indexOf("chrome") == -1 ? false : true;
    return res;
}

function isOpera()
{
    var strUserAgent = navigator.userAgent.toLowerCase();
    var res = strUserAgent.indexOf("opera") == -1 ? false : true;
    return res;
}

function getOperaVersionNumber()
{
    var strUserAgent = navigator.userAgent.toLowerCase();
    var indx = strUserAgent.indexOf("opera");
    if (indx < 0)
    {
        return 0;
    }
    var end = strUserAgent.indexOf(" ", indx + 6);
    var ver = strUserAgent.substring(indx + 6, end);
    return parseFloat(ver);
}

function isIE()
{
    var appname = navigator.appName.toLowerCase();
    return (appname == "microsoft internet explorer");
}

function getIEVersion()
{
    var ua = navigator.userAgent;
    var MSIEOffset = ua.indexOf("MSIE ");
    if (MSIEOffset == -1)
        return "0";
    else
        return ua.substring(MSIEOffset + 5, ua.indexOf(";", MSIEOffset));
}

function getIEVersionNumber()
{
    return parseFloat(getIEVersion());
}

function isNN()
{
    var ua = navigator.userAgent.toLowerCase();
    var bGecko = (ua.indexOf("gecko") > -1);
    if (bGecko)
    {
        return ua.indexOf("netscape") > -1;
    }
    return false;
}
function getNNVersionNumber( )
{
    var ua = navigator.userAgent.toLowerCase();
    var indx = ua.indexOf("netscape");
    if (indx == -1)
    {
        return 0;
    }
    else
    {
        var indxN = ua.indexOf("/", indx);
        if (indxN > -1)
        {
            indx = indxN + 1;
        }
        else
        {
            indx += 10;
        }
        var end = ua.indexOf(" ", indx);
        if (end == -1)
        {
            end = ua.length;
        }
        var ver = ua.substring(indx, end);
        indx = ver.indexOf(".");
        if (indx > -1)
        {
            var val = ver.substring(0, indx + 1);
            while (ver.indexOf(".", indx) > -1)
            {
                ver = ver.replace(".", "");
            }
            ver = val + ver.substring(indx);
        }
        return parseFloat(ver);
    }
}

function isMozilla()
{
    var ua = navigator.userAgent.toLowerCase();
    var bGecko = (ua.indexOf("gecko") > -1);
    if (bGecko)
    {
        return ua.indexOf("netscape") < 0;
    }
    return false;
}
function getMozillaVersionNumber( )
{
    var ua = navigator.userAgent.toLowerCase();
    var indx = ua.indexOf("rv:");
    if (indx == -1)
    {
        return 0;
    }
    else
    {
        indx += 3;
        var end = ua.indexOf(")", indx);
        if (end > -1)
        {
            return parseFloat(ua.substring(indx, end));
        }
        else
        {
            return parseFloat(ua.substring(indx));
        }
    }
}

function verifyBrowser()
{
    if (isOpera())
    {
        if (getOperaVersionNumber() < 7.54)
        {
            browserNotSupported();
            return false;
        }
    }
    else if (isNN())
    {
        if (getNNVersionNumber() < 6.23)
        {
            browserNotSupported();
            return false;
        }
    }
    else if (isMozilla())
    {
        if (getMozillaVersionNumber() < 1.2)
        {
            browserNotSupported();
            return false;
        }
    }
    return true;
}

function getSearchVal(strS, offset)
{
    var endstr = strS.indexOf('&', offset);
    if (endstr == -1)
    {
        endstr = strS.length + 1;
    }
    var val = strS.substring(offset, endstr);
    var test = val.replace('+', ' ');
    while (val.indexOf(test) == -1)
    {
        val = test;
        test = val.replace('+', ' ');
    }
    return decodeURIComponent(test);
}

function getQueryVal(strS, name)
{
    var arg = name + "=";
    var alen = arg.length;
    var clen = strS.length;
    var i = 0;
    while (i < clen)
    {
        var j = i + alen;
        if (strS.substring(i, j) == arg)
        {
            return getSearchVal(strS, j);
        }
        i = strS.indexOf("&", i) + 1;
        if (i == 0) break; 
    }
    return "";
} 

function getElemById(doc, id)
{
    try
    {
        if (typeof doc.all != "undefined")
            return doc.all(id);
        else
            return doc.getElementById(id);
    }
    catch(e)
    {
    }
    return null;
}

function getFrameByName(strName, wnd)
{
    var frm = null;
    try
    {
        if (typeof wnd == "undefined" || wnd == null)
            wnd = window;
        if (typeof wnd.parent.document.frames != "undefined")
            frm = wnd.parent.document.frames[strName];
        else
        {
            var arrFrames = wnd.parent.document.getElementsByName(strName);
            if (arrFrames == null || arrFrames.length == 0)
                return null;
            frm = arrFrames[0];
        }
    }
    catch(e)
    {
    }
    return frm;
}

function getIFrameById(strId)
{
    var frm;
    if (typeof document.frames != "undefined")
        frm = document.frames[strId];
    else
        frm = getElemById(document, strId);
    return frm;
}

function getFrameDocument(frm)
{
    if (!frm)
       return null;
    var doc = null;
    try
    {
        if (typeof frm.contentWindow != "undefined" && typeof frm.contentWindow.document != "undefined")
            doc = frm.contentWindow.document;
        else if (typeof frm.contentDocument != "undefined")
            doc = frm.contentDocument;
        else if (typeof frm.document != "undefined")
            doc = frm.document;
    }
    catch(e)
    {
    }
    return doc;
}

function getDocumentByFrameNameOrCurrentDocument(strName)
{
    var frm = getFrameByName(strName);
    if (frm == null)
        return document;
    else
        return getFrameDocument(frm);    
}


function getDoc(wnd)
{
    return getFrameDocument(wnd);
}


function getWindow(doc)
{
    var wnd = null;
    if (typeof doc.defaultView != "undefined")
        wnd = doc.defaultView;
    else if (typeof doc.contentWindow != "undefined")
        wnd = doc.contentWindow;
    else if (typeof doc.parentWindow != "undefined")
        wnd = doc.parentWindow;
    return wnd;
}

function d2hGetOwnerDocument(elem)
{
    var doc = null;
    if (typeof elem.document != "undefined")
        doc = elem.document;
    else if (typeof elem.ownerDocument != "undefined")
        doc = elem.ownerDocument;
    return doc;
}

function getNsTextHeight(doc)
{
    var text = getElemById(doc, "nstext");
    if (text == null)
        text = doc.body;
    var res = 16;
    var wnd = getWindow(doc);
    if (text.style.overflow == "auto")
    {
        if (typeof text.clientHeight != "undefined")
            res = text.clientHeight - 2;
        else
        {
            if (wnd != null && typeof wnd.getComputedStyle != "undefined")
                res = parseInt(wnd.getComputedStyle(text, '').getPropertyValue("height")) - 16;
            else
                res = parseInt(text.style.height) - 16;
        }
    }
    else
    {
        var topicBody = getElemById(doc, "D2HTopicOuterBody");
        if (topicBody && topicBody.style.overflow == "auto")
        {
            if (typeof topicBody.clientHeight != "undefined")
                res = topicBody.clientHeight - 2;
            else
            {
                if (wnd != null && typeof wnd.getComputedStyle != "undefined")
                    res = parseInt(wnd.getComputedStyle(topicBody, '').getPropertyValue("height")) - 16;
                else
                    res = parseInt(topicBody.style.height) - 16;
            }
        }
        else
        res = typeof doc.body.clientHeight != "undefined" ? (doc.body.clientHeight - 2) : (wnd.innerHeight - 16);
    }
   return res;
}

function getNsTextWidth(doc)
{
    var text = getElemById(doc, "nstext");
    if (text == null)
        text = doc.body;
    var res = 16;
    var wnd = getWindow(doc);
    if (text.style.overflow == "auto")
    {
        if (typeof text.clientWidth != "undefined")
            res = text.clientWidth - 2;
        else
        {
            if (wnd != null && typeof wnd.getComputedStyle != "undefined")
                res = parseInt(wnd.getComputedStyle(text, '').getPropertyValue("width")) - 16;
            else
                res = parseInt(text.style.width) - 16;
        }
    }
    else
    {
        var topicBody = getElemById(doc, "D2HTopicOuterBody");
        if (topicBody && topicBody.style.overflow == "auto")
        {
            if (typeof topicBody.clientWidth != "undefined")
                res = topicBody.clientWidth - 2;
            else
            {
                if (wnd != null && typeof wnd.getComputedStyle != "undefined")
                    res = parseInt(wnd.getComputedStyle(topicBody, '').getPropertyValue("width")) - 16;
                else
                    res = parseInt(topicBody.style.width) - 16;
            }
        }
        else
        res = typeof doc.body.clientWidth != "undefined" ? (doc.body.clientWidth - 2) : (wnd.innerWidth - 16);
    }
    return res;
}

function point(x, y)
{
    this.x = x;
    this.y = y;
}

function getNsTextLocation(doc)
{
    if (doc == null)
        doc = document;
    var text = getElemById(doc, "nstext");
    if (text == null)
        text = doc.body;
    if (text.style.overflow == "auto")
        return new point(text.offsetLeft, text.offsetTop);
    else
        return new point(0, 0);
}

function getMouseAtNsText(doc, pop, evt)
{
    if (doc == null)
        doc = document;
    var pt = getNsTextLocation(doc);
    var ptRes;
    if (pop != null)
    {
        if (typeof evt.pageX != "undefined")
            ptRes = new point(parseInt(evt.pageX) + parseInt(pop.offsetLeft), parseInt(evt.pageY) + parseInt(pop.offsetTop));
        else
            ptRes = new point(parseInt(evt.offsetX) + parseInt(pop.offsetLeft), parseInt(evt.offsetY) + parseInt(pop.offsetTop));
        
        if (typeof doc.body.scrollLeft != "undefined")
        {
            ptRes.x -= parseInt(doc.body.scrollLeft) + parseInt(document.body.scrollLeft);
            ptRes.y -= parseInt(doc.body.scrollTop) + parseInt(document.body.scrollTop);
        }
        else if (typeof window.pageXOffset != "undefined") 
        {
            var wnd = getWindow(doc);
            if (wnd != null)
            {
                ptRes.x -= parseInt(wnd.pageXOffset);
                ptRes.y -= parseInt(wnd.pageYOffset);
            }
            wnd = getWindow(document);
            if (wnd != null)
            {
                ptRes.x -= parseInt(wnd.pageXOffset);
                ptRes.y -= parseInt(wnd.pageYOffset);
            }
        }
    }
    else
        ptRes = new point(evt.x ? evt.x : evt.clientX, evt.y ? evt.y : evt.clientY);
    ptRes.x -= pt.x;
    ptRes.y -= pt.y;
    return ptRes;
}

function rectangle(x, y, width, height)
{
    this.x = x;
    this.y = y;
    this.height = height;
    this.width = width;    
}

function getPopup(doc, popDoc, frm, pt, clientPt, initialWidth)
{
    if (doc == null)
        doc = document;
    var w = getNsTextWidth(doc);
    if (initialWidth > w)
        initialWidth = w;
    var pop = getElemById(doc, "d2h_popupFrameWnd");
    var delta = 0;
    if (clientPt.x + initialWidth > w)
        delta = clientPt.x + initialWidth - w;
    if (pt.x < delta)
    {
        initialWidth += pt.x - delta;
        pt.x = 0;
    }
    else
        pt.x -= delta;
    if (pop != null)
        pop.style.width = initialWidth;
    var rect = new rectangle(pt.x, 0, initialWidth, 0);
    var h = getNsTextHeight(doc);
    var initialHeigth = getAvailableHeight(frm, popDoc);
    delta = (2*h)/3;
    var bDown = delta > clientPt.y;
    var popH;
    if (bDown)
        popH = h - clientPt.y;
    else
        popH = clientPt.y;
    if (initialHeigth > popH)
    {
        if (initialHeigth > h)
            initialHeigth = h > 20 ? h - 20 : h / 2;
        if (clientPt.y + initialHeigth >= h)
        {
            pt.y -= clientPt.y + initialHeigth - h;
            initialHeigth -= 20;
        }
        rect.y = pt.y;
        rect.height = initialHeigth;
    }
    else
    {
        rect.height = initialHeigth + 1;
        if (bDown)
            rect.y = pt.y + 15;
        else
        {
            rect.y = pt.y - 15;
            rect.y -= rect.height;
        }
    }
    return rect; 
}

function getPopCoords(bPopupOpen, doc, evt)
{
    if (doc == null)
        doc = document;
    var elem = (evt.target) ? evt.target : evt.srcElement;
    var nstx = getElemById(doc, "nstext");
    if (nstx == null)
        nstx = doc.body;
    var topicBody = getElemById(doc, "D2HTopicOuterBody");
    var pt = new point(0, 0);
    var pop = getElemById(doc, "d2h_popupFrameWnd");
    var isOp = false;
    if (isOpera() && typeof nstx.offsetLeft != "undefined")
    {
        isOp = true;
        pt.x = evt.x;
        pt.y = evt.y;
        if (!bPopupOpen)
        {
            pt.x += doc.body.scrollLeft;
            pt.y += doc.body.scrollTop;
            if (nstx && nstx.style.overflow == "auto")
                pt.y += nstx.scrollTop;
            if (topicBody)
                pt.y += topicBody.scrollTop;
        }
    }
    else if (typeof evt.layerY != "undefined")
    {
        pt.x = evt.pageX;
        pt.y = evt.pageY;
    }
    else
    {
        if (bPopupOpen)
        {
            pt.x = evt.offsetX;
            pt.y = evt.offsetY;
        }
        else
        {
            pt.x = evt.x + doc.body.scrollLeft;
            pt.y = evt.y + doc.body.scrollTop;
            if (nstx)
                pt.y -= nstx.offsetTop - nstx.scrollTop;
            if (topicBody)
                pt.y -= topicBody.offsetTop - topicBody.scrollTop;
        }
    }
    if (bPopupOpen)
    {
        pt.x += pop.offsetLeft;
        pt.y += pop.offsetTop
        if (!isOp)
        {
            if (typeof document.body.scrollLeft != "undefined")
            {
                pt.x -= document.body.scrollLeft;
                pt.y -= document.body.scrollTop;
            }
            else if (typeof window.pageXOffset != "undefined") 
            {
                pt.x -= window.pageXOffset;
                pt.y -= window.pageYOffset;
            }
        }
    }
    return pt;
}

function getAvailableHeight(frm, popDoc)
{
    var hgt = 140;
    if (frm != null && typeof frm.self != "undefined" && typeof frm.self.document.body.scrollHeight != "undefined")
    {
        hgt = frm.self.document.body.scrollHeight;
        if (isOpera())
            hgt += 7;
    }
    else
    {
        if (typeof popDoc.body.scrollHeight != "undefined")
            hgt = popDoc.body.scrollHeight;    
        else if (typeof popDoc.body.offsetHeight != "undefined")
        {
            hgt = popDoc.body.offsetHeight;    
            hgt = hgt + 18;
        }
    }
    return hgt;
}

function d2hCreatePopupIFrame(doc)
{
    var frm = null;
    var nstx = getElemById(doc, "nstext");
    if (nstx == null)
        nstx = doc.body;
    if (!isOpera() && typeof doc.body.insertAdjacentHTML != "undefined")
    {
        nstx.insertAdjacentHTML("BeforeEnd", "<div id='popupDiv'></div>")
        var div = getElemById(document, "popupDiv");
        if (div == null)
	        return null;
        div.innerHTML = "<iframe id=\"d2h_popupFrameWnd\" name=\"d2h_popupFrameWnd\" frameborder=\"no\" height=\"0px\" width=\"0px\" style=\"VISIBILITY: hidden; position: absolute;\"></iframe>";
        frm = getElemById(doc, "d2h_popupFrameWnd");
        if (typeof div.removeNode != "undefined")
            div.removeNode(false);
    }
    else
    {
        frm = doc.createElement("iframe");
        frm.id = "d2h_popupFrameWnd";
        frm.name = "d2h_popupFrameWnd";
        frm.frameBorder = "no";
        frm.height = "0px"
        frm.width = "0px"
        frm.style.visibility = "hidden";
        frm.style.position = "absolute";
        nstx.appendChild(frm);
    }
    frm.style.margin = "0pt";
    frm.style.padding = "0pt";
    return frm;
} 


function dhtml_popup(evt, url)
{
	ANCHOR = "";
	var mainDoc, pop, main, body, x, y, pt;

	// no url? then hide the popup
	if (url == null || url.length == 0)
	{
		pop = getElemById(document, "d2h_popupFrameWnd");
		if (pop != null)
		{
			pop.style.display = "none";
			pop.left = 0;
			pop.top = 0;
			pop.width = 0;
			pop.height = 0; 
			pop.style.visibility = "hidden";
			pop.style.border = "";
                        pop.setAttribute("src", "about:blank");
		}
		return;
	}
    popUpShown = false;
	// if the popup frame is already open, close it first
	if (dhtml_popup_is_open())
	{
	    popUpShown = true;
		// the main window is the parent of the popup frame
		main = window.parent;
		mainDoc = getDoc(main);
		body = main.document.body;
		pop = getElemById(main.document, "d2h_popupFrameWnd");

		pt = getPopCoords(popUpShown, mainDoc, evt);
		x = pt.x;
		y = pt.y;
    	main.popClientPos = getMouseAtNsText(mainDoc, pop,  evt);

		// hide the popup frame
		if (!isNN())
		    pop.style.display = "none";
		pop.style.visibility = "hidden";
	}
	else
	{
		// the main window is the current window
		main = window;
		mainDoc = getDoc(main);
		body = document.body;
		pop = getElemById(document, "d2h_popupFrameWnd");

		// use the event coordinates for positioning the popup
		pt = getPopCoords(popUpShown, mainDoc, evt);
		x = pt.x;
		y = pt.y;
    	popClientPos = getMouseAtNsText(mainDoc, null,  evt);

		// get the popup frame, creating it if needed
		if (pop == null)
		    pop = d2hCreatePopupIFrame(document); 
	}
	if (pop == null)
	{
	    d2hwindow(url, "d2hPopup");
	    return;
	}

	// get frame style
	var sty = pop.style;

	// load url into frame
	var anchorIndex = url.indexOf("#", 0);
	setPopupState(-1, popUpShown);
	pop.setAttribute("src", d2hGetRelativePath(document, "_d2hblank" + d2hDefaultExtension));
	pop.style.display = "block";
	setPopupState(0, popUpShown);
	var strUrl;
	if (anchorIndex >= 0)
	{
		ANCHOR = url.substr(anchorIndex + 1);
		//workaround to reset current src
		strUrl = url.substr(0, anchorIndex);
	}
	else
	    strUrl = url;
    if (popUpShown)
        open(strUrl, isOpera() ? "_self" : "d2h_popupFrameWnd");
    else
    {
        if (isOpera() && getOperaVersionNumber() >= 9.0)
            open(strUrl, "d2h_popupFrameWnd");
        else
            pop.src = strUrl;
    }
	// initialize frame size/position
	sty.border    = "1px solid #cccccc";
	sty.left = x - 30000;
	sty.top = y - 30000;
	var wid = getInsideWindowWidth(mainDoc) - 60;
	if (wid < 10)
	    wid += 60;
	sty.width  = (wid > 500)? wid * 0.6: wid;
	sty.height = 0;

	// wait until the document is loaded to finish positioning
	main.setTimeout("dhtml_popup_position()", 100);
}

function dhtml_popup_is_open()
{
	return window.name == "d2h_popupFrameWnd";
}

function popDocIsLoad()
{
    if (window.popupState == 1 || window.g_d2hIterationCount == 100)
    {
        // g_d2hIterationCount is used for Opera 8.0 and higher, where OnLoad is not fired if the window is invisible (Opera bug)
        window.g_d2hIterationCount = 0;
        return true;
    }
    if (typeof window.g_d2hIterationCount == "undefined")
        window.g_d2hIterationCount = 1;
    else
        window.g_d2hIterationCount++;
    return false;
}

function dhtml_popup_position()
{
    if (!popDocIsLoad())
    {
        window.setTimeout("dhtml_popup_position()", 100);
        return;
    }

    // get frame
    var pop = getElemById(document, "d2h_popupFrameWnd");
    var frm = getIFrameById("d2h_popupFrameWnd");
    var sty = pop.style;
    if (!popupDocument)
        popupDocument = getDoc(frm);
	    
    if (popupDocument)
    {
        if (getAllElements(popupDocument).length == 0)
            //if frame is empty, it contains its document, workaround must be applied
            d2h_set_popup_html(popupDocument);
        if (ANCHOR != "")
             //for non-splitting mode topics that are not needed must be hidden
             d2h_hide_unused_elements(popupDocument);

        // hide navigation/nonscrolling elements, if present
        dhtml_popup_elements(popupDocument);
        var popWnd = getWindow(popupDocument);
        if (popWnd)
        {
            popWnd.d2hPrepareAnchors4Popup();
            popWnd.g_bMainWnd = false;
        }
    }
    var popDoc = popupDocument;

	// get containing element (scrolling text region or document body)
    var body = getElemById(document, "nstext");
    var poptext = getElemById(popDoc, "nstext");
    var nsbanner = getElemById(popDoc, "_d2hTitleNavigator");
    d2hStandardizePopupMargin(popDoc.body, poptext, nsbanner);
    if (body == null)
        body = document.body;

    sty.visibility = "visible";
    setPopupState(-1, popUpShown);

    // make content visible
    var pt = new point(parseInt(sty.left) + 30000, parseInt(sty.top) + 30000);
    var rect = getPopup(document, popDoc, frm, pt, popClientPos, parseInt(sty.width));
    sty.left = rect.x;
    sty.top = rect.y;
    sty.width = rect.width;
    sty.height = rect.height;
}

function dhtml_popup_elements(doc)
{
    d2hShowTopicTitleInPopup(doc);
    d2hHideBreadcrumbs(doc);

	// set popup background style
	doc.body.style.backgroundColor = POPUP_COLOR;
	doc.body.style.backgroundImage = "url('" + d2hGetRelativePath(doc, POPUP_IMAGE) + "')";
	doc.body.style.backgroundRepeat = POPUP_REPEAT;
	doc.body.style.overflow = "auto";
	// reset background image/color of topic, if present
	var outerBody = getElemById(doc, "D2HTopicOuterBody");
	if (outerBody != null)
	{
		outerBody.style.backgroundImage = "none";
		outerBody.style.backgroundColor = "transparent";
		outerBody.style.overflow = "visible";
		outerBody.style.width = "";
		outerBody.style.height = "";
	}
	
	// reset background image/color of scrolling text region, if present
	var nstx = getElemById(doc, "nstext");
	if (nstx != null)
	{
		nstx.style.overflow = "visible";
		nstx.style.backgroundImage = "none";
		nstx.style.backgroundColor = "transparent";
	}
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// d2h functions: browser-independent
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function d2hie()
{
    if (isOpera() || isNN() || isMozilla())
        return false;
    else
        return isIE();
}

function d2hpopup(evt, url)
{
    evt = (evt) ? evt : ((window.event) ? event : null);
    if (_d2hInlinePopup != null)
    {
        var elem = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
        if (elem == null || !d2hElementInContainer(elem, _d2hInlinePopup))
        {
            d2hHideInline(_d2hInlinePopup);
            _d2hInlinePopup = null;
        }
    }

    d2hClosePopupMenu(evt);
    // use dhtml if we can
    dhtml_popup(evt, url);
    return false;
}

function d2hwindow(url, name)
{
    if (name != 'main')
    {
        var wnd = window.open(url, name, 'scrollbars=1,resizable=1,toolbar=0,directories=0,status=0,location=0,menubar=0,height=300,width=400');
        wnd.focus();
        return false;
    }
    return true;
}

function d2hcancel(msg, url, line)
{
	return true;
}

function d2hload()
{
    window.focus();
    window.onerror = d2hcancel;
    setPopupState(1, true);
    d2hPrepareAnchors4Popup();
}

function setPopupState(state, fromPop)
{
    if (fromPop)
    {
        if (window.parent)
        {
            if (state == 1)
                window.parent.popupDocument = document;
            else
                window.parent.popupDocument = null;
            window.parent.popupState = state;
        }
    }
    else
    {
        if (state == 1)
            popupDocument = document;
        else
            popupDocument = null;
        popupState = state;
    }
}

function d2hframeload()
{
	// for compatibility with HTML generated by earlier versions
}

function d2hGetCurrentStyleAttribute(elem, name)
{
    if (typeof elem.currentStyle != "undefined" && typeof elem.currentStyle.getAttribute != "undefined")
        return elem.currentStyle.getAttribute(name);
    else
    {
        var doc = d2hGetOwnerDocument(elem);
        var wnd = doc ? getWindow(doc) : null;
        if (doc && wnd && typeof wnd.getComputedStyle != "undefined")
            return wnd.getComputedStyle(elem, '').getPropertyValue(name);
        else
           elem.style.getAttribute(name);    
    }    
}

function evalHideTocElem(elem)
{
    var id = elem.id;
    if (id == undefined || id == null)
        return true;
    var res = true;
    var id1 = id.substring(0, 1);
    var id2 = id.substring(0, 2);
    if (id1 == "c")
    {
        elem.style.display = "none";
        res = false;
    }
    else if (id1 == "a")
    {
        //fill book/topic properties
        elem.onmouseover = d2hItemOver;
        elem.onmouseout = d2hItemOut;
        elem.onfocus = d2hItemOver;
        elem.onblur = d2hItemOut;
        if (USE_SECTION_508 != "yes")
            elem.hideFocus = true;
        elem.title = getInnerText(elem);
        if (id2 == "am")
            elem.onkeypress = d2hclick; 
        else if (id2 == "at")
        {
            elem.href = "javascript:void(0)";
            elem.onkeypress = d2hclick; 
        }
        else
            elem.onclick = d2hLinkClick;
    }
    else if (id1 == "i")
    {
        //fill book/topic image properties
        elem.align="absMiddle";
        elem.border = "0"; 
        elem.hspace = "0";
        elem.vspace = "0";
        if (id2 == "im" || id2 == "it")
            d2himage(elem, IMAGE_CLOSE, USE_SECTION_508, false);
        else
            d2himage(elem, IMAGE_TOPIC, USE_SECTION_508);
    }
    return res;
}

function reverseVisibileTocElem(elem, img, mode)
{
    if (elem.style.display == "none")
    {
        if (mode != 2)
        {
            elem.style.display = "";
            d2himage(img, IMAGE_OPEN, USE_SECTION_508, true);
        }
    }
    else
    {
        if (mode != 1)
        {
            elem.style.display = "none";
            d2himage(img, IMAGE_CLOSE, USE_SECTION_508, false);
        }
    }
}

function getAllElements(doc)
{
    if (typeof doc.all != "undefined")
        return doc.all;
    else
        return doc.getElementsByTagName("*");
}

function d2htocload()
{
    var elem = getElemById(document, "c-_root");
    if (elem)
        d2hPrepareTocChildNodes(elem);
}

function d2hPrepareTocChildNodes(parent)
{
    if (d2hGetAttribute(parent, "done") == "1")
        return;
    d2hTraverseElements(parent, evalHideTocElem, false, true);
    parent.setAttribute("done", "1");
}

function d2hclick(evt)
{
    evt = (evt) ? evt : ((typeof window.event != "undefined") ? event : null);
    if (evt)
    {
        var elem = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
        if (!elem)
        {
            return true;
        }
        if (elem.nodeType == 3)
            elem = elem.parentNode;
        if (elem.tagName.toLowerCase() == "span")
            elem = elem.parentNode;
        var id = elem.id;
        if (id == "D2HSyncToc")
        {
            return true;
        }
        var expand = false, shrink = false, needToRevert = false;
        var charCode = (evt.charCode) ? evt.charCode : ((evt.which) ? evt.which : evt.keyCode);
        if (evt.type == "keypress" && charCode < 13)
            return;
        expand = charCode == 43;
        shrink = charCode == 45;
        var isClick = evt.type == "click" || charCode == 13;
        needToRevert = expand || shrink || (id.substring(0, 2) == "at" && isClick);
        var sub = id.substring(2);
        var elt = getElemById(document, "c" + sub);
        var img = getElemById(document, ((id.substring(1, 2) == "m") ? "im" : "it") + sub);
        if (elem.tagName.toLowerCase() == "a" && !needToRevert)
        {
            _isTopicOpenedFromTOC = true;
            if (elt != null)
            {
                //open book's tree if user pressed link
                d2hPrepareTocChildNodes(elt);
                reverseVisibileTocElem(elt, img, 1);
            }
            showSelection(document, elem);
            return true;
        }
        if (id.substring(1, 2) != "m" && id.substring(1, 2) != "t")
            return true;
        if (elt != null)
        {
            d2hPrepareTocChildNodes(elt);
            reverseVisibileTocElem(elt, img, expand ? 1 : (shrink ? 2 : 0));
            cancelEvent(evt);
            return false;
        } 
    }
    return true;
}

function d2hItemSelect(evt)
{
    evt = (evt) ? evt : ((typeof window.event != "undefined") ? event : null);
    if (evt)
    {
        var elem = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
        if (!elem)
        {
            return;
        }
        if (elem.nodeType == 3)
            elem = elem.parentNode;
        var id = elem.id;
        if (elem.tagName.toLowerCase() == "a")
            showSelection(document, elem);
    }
}

function d2hSearchItemSelect(href, evt)
{
    d2hSetClickState("onresult");
    
    d2hItemSelect(evt);
    d2hInitSecondaryWindows();
    var lowhref = href.toLowerCase();
    var wnd = _d2hSecondaryWindowsByTopics[lowhref];
    if (typeof (wnd) == "undefined" || wnd == null || wnd == "")
        return true;
    else
    {
		var sWndFeatures = "";
		if (typeof d2hInitWindowParams != "undefined")
		{
			d2hInitWindowParams();
			if (_d2hWindowParamsByWindows[wnd])
				sWndFeatures = _d2hWindowParamsByWindows[wnd];
		}
        var hwnd = window.open(href, wnd, sWndFeatures);
        hwnd.focus();
    }
    return false;
}

function d2hItemOver(evt)
{
    evt = (evt) ? evt : ((window.event) ? event : null);
    if (evt)
    {
        var elem = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
        if (!elem)
        {
            return;
        }
        if (elem.nodeType == 3)
            elem = elem.parentNode;
        elem = findActualElementOver(elem);
        elem.className  = CLASS_ITEMOVER;
    }
}
function d2hItemOut(evt)
{
    evt = (evt) ? evt : ((window.event) ? event : null);
    if (evt)
    {
        var elem = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
        if (!elem)
        {
            return;
        }
        if (elem.nodeType == 3)
            elem = elem.parentNode;
        elem = findActualElementOver(elem); 
        var curSel = getElemById(document, "curSel");
        if (curSel != null && curSel.value == elem.id)
	    elem.className = CLASS_ITEMCURSEL;
        else
            hideSelection(elem);
     }
}
function hideSelection(elem)
{
    elem.className = CLASS_ITEMOUT;
}
function showSelection(doc, elem)
{
    var curSel = getElemById(doc, "curSel");
    if (!curSel)
    {
        return;
    }
    if (curSel.value != "")
    {
        var old = getElemById(doc, curSel.value);
        if (old)
            hideSelection(old);
    }
    curSel.value = elem.id;
    elem.className = CLASS_ITEMCURSEL;
}

//if element has special ID and special SPAN inside, this SPAN is returned
function findActualElementOver(element)
{
    var foundElem = null;
    var id = element.id.toLowerCase();
    if (id.substring(0, 1) == "i")
    {
        foundElem = d2hGetParentElement(element);
        id = foundElem.id.toLowerCase();
    }
    if (id.substring(0, 2) == "at")
    {
        var spanID = "st" + id.substring(2);
        foundElem = getElemById(document, spanID);
    }
    return foundElem == null ? element : foundElem;
}

// Sets a specified relative URL of image to specified HTML element
function d2himage(element, image, apply508, isOpen)
{
	if (element != null)
	{
		// Hide element if image is missing
		if (image == "")
			element.style.visibility = "hidden";

		// Sets the specified image to element and displays it
		else
		{
			element.src = d2hGetRelativePath(d2hGetOwnerDocument(element), image);
			element.style.visibility = "visible";
			if (apply508 == "yes")
            {
                var id = element.id.substring(0, 2);
                if (id == "im")
                    element.alt = isOpen ? ALT_OPEN_BOOK_TOPIC : ALT_CLOSED_BOOK_TOPIC;
                else if (id == "it") 
                    element.alt = isOpen ? ALT_OPEN_BOOK_NO_TOPIC : ALT_CLOSED_BOOK_NO_TOPIC;
                else
                    element.alt = ALT_TOPIC;
                element.title = element.alt;
            }
            if (apply508 == "no")
                element.alt = ""
			
		}	
	}
}

function d2hnsresize(evt)
{
    var ps = false;
    try
    {
        if (typeof window.parent.popupState != "undefined" && window.parent.popupState == 0)
            ps = true;
    }
    catch(e)
    {
    }
    if (window.parent && ps)
        setPopupState(1, true);
    else
    {
        if (d2hie())
            dhtml_nonscrolling_resize();
        else
            dhtml_NNscrollingStyle();
    }
}

function d2h_before_print(doc)
{
    if (_isPrinting)
        return;
    if (!doc)
        doc = document;
    doc.body.style.overflow = "visible";
    doc.body.style.margin = "0pt";
	var oText = getElemById(doc, "D2HTopicOuterBody");
	if (oText != null)
	{
		oText.style.overflow = "visible";
		oText.style.width = "100%";
	}
	var nsText = getElemById(doc, "nstext");
	if (nsText != null)
	{
		nsText.style.overflow = "visible";
		nsText.style.width = "100%";
	}
	var nav = getElemById(doc, "ienav");
	if (nav != null)
		nav.style.display = "none";
	var oBanner = getElemById(doc, "nsbanner");
	if (oBanner != null)
	{
		oBanner.style.borderBottom = "0px";
		oBanner.style.margin = "0px 0px 0px 0px";
		oBanner.style.width = "100%";
	}
}

function d2h_after_print(doc)
{
    if (!doc)
        doc = document;
	doc.location.reload();
}

function d2h_set_popup_html(doc)
{
	doc.body.innerHTML = document.body.innerHTML;
	var frame = getFrameById("d2h_popupFrameWnd");
	if (frame != null)
		frame.removeNode(true);
	var nst = getElemById(doc, "nstext");
	if (nst != null)
	{
		nst.style.paddingTop = "0px";
		nst.style.paddingLeft = "10px";
		nst.style.removeAttribute("top", false);
		nst.style.removeAttribute("width", false);
		nst.style.removeAttribute("height", false);
	}
	var elt, i;
	//need to reset onclick event to prevent script error
	//because scripts don't work when body is copied from document to frame
    var listObj = doc.getElementsByTagName("a");

	for (i = 0; i < listObj.lenght; i++)
	{
		elt = listObj.item(i);
		elt.onclick = "";
	}
}

function d2h_hide_unused_elements(doc)
{
	var title = getElemById(doc, "TitleRow");
	if (title != null)
		title.style.display = "none";
	var nsb = getElemById(doc, "nsbanner");
	if (nsb != null)
		nsb.style.display = "none";

    var listObj = getAllElements(doc);
	var count = listObj.length;
	var show = false, inTopic = false, id, elt, i;
	for (i = 0; i < count; i++)
	{
		elt = listObj.item(i);
		id = elt.id;
		if (!inTopic && (id.length > 10) && (id.substring(0, 10) == "_D2HTopic_"))
			inTopic = true;
		if (elt.className == "_D2HAnchor")
			show = (elt.name == ANCHOR);
               	if (inTopic && !show)
			elt.style.display = "none";
	}
}

function expandTreeNode(doc, elem)
{
    if (elem.id == "")
        return;
    var imgId = null;
    if (elem.id.substring(0, 1) == "a")
    {
        imgId = elem.id.substring(2);
    }
    expandNodes(doc, elem, imgId);
}

function expandNodes(doc, elem, selID)
{
    if (typeof elem.tagName == "undefined" || elem.tagName.toLowerCase == "body" || elem.id == "c-_root")
        return;
    if (elem.parentNode)
        expandNodes(doc, elem.parentNode, selID);
    elem.style.display = "";
    if (elem.id == "")
        return;
    var imgId = null;
    if (elem.id.substring(0, 1) == "c")
    {
        d2hPrepareTocChildNodes(elem);
        imgId = elem.id.substring(2);
    }
    else if (elem.id.substring(0, 1) == "a")
        imgId = elem.id.substring(2);

    if (imgId)
    {
        if (selID == imgId)
            return;
        var imgId1 = "im-" + imgId;
        var img = getElemById(doc, imgId1);
        if (!img)
        {
            imgId1 = "it-" + imgId;
            img = getElemById(doc, imgId1);
        }
        if (img)
            d2himage(img, IMAGE_OPEN, USE_SECTION_508, true);
    }
}

function getTocAnchorByhRef(doc, linkHref)
{
    var listObj = doc.getElementsByTagName("a");
	var strHref;
	var curLink = null;
	var safari = isSafari();
	for (var i = 0; (i < listObj.length) && curLink == null; i++)
	{
	    if (listObj[i].href)
	    {
	        strHref = listObj[i].href.toLowerCase();
        	if (safari)
	            strHref = unescape(strHref);			
	        if (strHref == linkHref)
	            curLink = listObj[i];
	     }
	}
	return curLink;
}

function d2hSyncTocMouseDown(evt)
{
    var elem = getElemById(document, "D2HSyncToc");
    if (elem != null)
    {
        elem.className = "syncTocSelected";
        var img = getElemById(document, elem.id + "Image");
        if (img != null)
            d2himage(img, SYNCTOC_SELECTED);
    }
}

function d2hSyncTocMouseUp(evt)
{
    var elem = getElemById(document, "D2HSyncToc");
    if (elem != null)
    {
        elem.className = "syncToc";
        var img = getElemById(document, elem.id + "Image");
        if (img != null)
            d2himage(img, SYNCTOC_UNSELECTED);
    }
}

function d2hSyncTocMouseOut(evt)
{
    d2hSyncTocMouseUp(evt);
}

function d2hSyncTocMouseMove(evt)
{
    d2hSyncTocMouseUp(evt);
}

function SyncTocBody(linkHref, scrollByHorizontal, selectAndExpand)
{
    var frm = getFrameByName("left");
    if (frm && !_isTopicOpenedFromTOC)
    {
        var doc = getFrameDocument(frm);
        if (doc != null)
    	{
            if (doc.forms && doc.forms.length == 0)
                return;
            linkHref = linkHref.toLowerCase();
           
            var anchorIndex = linkHref.indexOf("#", 0);
            if (anchorIndex >= 0)
                linkHref = linkHref.substr(0, anchorIndex);
            var curLink = getTocAnchorByhRef(doc, linkHref);
            if (curLink == null)
                return;
            if (selectAndExpand)
            {
                expandTreeNode(doc, curLink);
                showSelection(doc, curLink);
            }
            if (USE_SECTION_508 == "yes")
                curLink.focus();
            if (scrollByHorizontal)
            {
                var ie = isIE();
                if (isOpera() || ie)
                    curLink.scrollIntoView();
                else if (typeof(doc.location.hash) != "undefined")
                    doc.location.hash = curLink.id;
            }
            else
            {
                var scrollArea = getElemById(doc, "scrollArea");
                scrollArea.scrollTop = curLink.offsetTop;
            }
        }
    }
    _isTopicOpenedFromTOC = false;
}

function d2hSyncDynamicToc(scrollByHorizontal)
{
    var frm = getFrameByName("right");
    if (frm)
    {
        var doc = getFrameDocument(frm);
        if (doc != null)
        {
            var loc = doc.location.href;
            SyncTocBody(loc, scrollByHorizontal, true);
        }
    }
    return false;
}

function d2hSyncStaticToc(scrollByHorizontal)
{
    var frm = getFrameByName("right");
    if (frm)
    {
        var doc = getFrameDocument(frm);
        if (doc != null)
        {
            var loc = doc.location.href;
            SyncTocBody(loc, scrollByHorizontal, false);
        }
    }
    return false;
}

function getInsideWindowWidth(doc)
{
    var wid = 0;
    if (doc == null)
        doc = document;
    if (typeof doc.body.clientWidth != "undefined")
	    wid = doc.body.clientWidth;
	else if (typeof doc.body.parentElement != "undefined" && typeof doc.body.parentElement.clientWidth != "undefined")
	    wid = doc.body.parentElement.clientWidth;
    else
    {
        var wnd = getWindow(doc);
        if (wnd != null && typeof wnd.innerWidth != "undefined")
	        wid = wnd.innerWidth;
	}
    return wid;
}

function getInsideWindowHeight(doc)
{
    var hgt = 0;
    if (doc == null)
        doc = document;
    if (typeof doc.body.clientHeight != "undefined")
	    hgt = doc.body.clientHeight;
	else if (typeof doc.body.parentElement != "undefined" && typeof doc.body.parentElement.clientHeight != "undefined")
	    hgt = doc.body.parentElement.clientHeight;
    else
    {
        var wnd = getWindow(doc);
        if (wnd != null && typeof window.innerHeight != "undefined")
	        hgt = wnd.innerHeight;
	}
    return hgt;
}

function isServerSide()
{
    var str = document.location.toString().toLowerCase();
    return !(str.length > 7 && str.substring(0, 7) == "file://");
}

function getDocHeight(doc)
{
    if (typeof doc.body.offsetHeight != "undefined")
        return doc.body.offsetHeight;
    else if (typeof doc.body.scrollHeight != "undefined")
        return doc.body.scrollHeight;
    return doc.body.style.height;
}

function d2hGetRelativePath(doc, path)
{
	if (path.length >= 0 && doc != null)
	{
		var relPart = d2hGetAttribute(doc.body, "relPart");
		if (relPart == null)
			relPart = "";
		return relPart  + path;
	}
	else
		return "";
}

function d2hHideInline(elem)
{
    if (elem != null)
    {
        elem.style.position = "absolute";
        elem.style.visibility = "hidden";
        if (typeof elem.style.display != "undefined")
            elem.style.display = "none";
    }
}

function d2hShowInline(elem)
{
    if (elem != null)
    {
        elem.style.position = "";
        elem.style.visibility = "visible";
        if (typeof elem.style.display != "undefined")
            elem.style.display = "";
    }
}

function d2hInitInlineDropdown(elemId)
{
    var elem = getElemById(document, elemId);
    d2hHideInline(elem);
}

function d2hInitInlineExpand(elemId)
{
    var elem = getElemById(document, elemId);
    d2hHideInline(elem);
}

function d2hInitInlinePopup(elemId)
{
    var elem = getElemById(document, elemId);
    if (elem != null)
    {
        d2hHideInline(elem);
        elem.style.backgroundColor = POPUP_COLOR;
        elem.style.backgroundImage = "url('" + d2hGetRelativePath(document, POPUP_IMAGE) + "')";
        elem.style.backgroundRepeat = POPUP_REPEAT;
        elem.style.border = "1px solid #cccccc";
    }
}

function d2hInlineExpand(evt, elemId)
{
    var elem = getElemById(document, elemId);
    if (elem != null)
    {
        if (elem.style.visibility == "hidden")
            d2hShowInline(elem);
        else
            d2hHideInline(elem);
    }
    return false;
}

function d2hInlineDropdown(evt, elemId)
{
    var elem = getElemById(document, elemId);
    if (elem != null)
    {
        if (elem.style.visibility == "hidden")
            d2hShowInline(elem);
        else
            d2hHideInline(elem);
    }
    return false;
}

function d2hInlinePopup(evt, elemId)
{
    var elem = getElemById(document, elemId);
    if (elem != null)
    {
        if (elem.style.visibility == "hidden")
        {
            if (d2hNeedSendToBody(elem))
                d2hSend2Body(elem);
            if (typeof elem.style.display != "undefined")
                elem.style.display = "";
            elem.style.width = "auto";
            elem.style.height = "auto";
            var pt = d2hGetInlinePosition(evt);
            elem.style.visibility = "visible";
            setInlinePopup2Pos(elem, pt.x, pt.y);
            _d2hInlinePopup = elem;
        }
        else
        {
            d2hHideInline(elem);
            elem.style.left = 0;
            elem.style.top = 0;
        }
    }
    return false;
}

function setInlinePopup2Pos(popupElem, x, y)
{
    var nstext = getElemById(document, "nstext");
    if (nstext == null)
        nstext = document.getElementsByTagName((document.compatMode && document.compatMode == "CSS1Compat") ? "HTML" : "BODY")[0];
    d2hStandardizePopupMargin(popupElem);
    var w_width = nstext.clientWidth ? nstext.clientWidth + nstext.scrollLeft : window.innerWidth + window.pageXOffset;
    var w_height = nstext.clientHeight ? nstext.clientHeight + nstext.scrollTop : window.innerHeight + window.pageYOffset;
    popupElem.style.width = "auto";
    popupElem.style.height = "auto";
    var textWidth = popupElem.offsetWidth;
    var w = (w_width > 300)? w_width * 0.6: w_width;
    if (textWidth > w)
        textWidth = w;
    popupElem.style.width = textWidth + "px";
    var t_width = popupElem.offsetWidth;
    var t_height = popupElem.offsetHeight;
    textWidth = Math.sqrt(16*t_width*t_height/9);
    popupElem.style.width = Math.round(textWidth) + "px";
    t_width = popupElem.offsetWidth;
    t_height = popupElem.offsetHeight;
    popupElem.style.left = x + 8 + "px";
    popupElem.style.top = y + 8 + "px";
    var x_body_bottom = (document.body.clientWidth ? document.body.clientWidth : window.innerWidth) + document.body.scrollLeft;
    var y_body_bottom = (document.body.clientHeight ? document.body.clientHeight : window.innerHeight) + document.body.scrollTop;
    if (x + t_width > x_body_bottom)
        popupElem.style.left = x_body_bottom - t_width + "px";
    if (y + t_height > y_body_bottom)
        popupElem.style.top = y_body_bottom - t_height + "px";
}

function getInnerText(elem)
{
    if (typeof elem.innerText != "undefined")
        return elem.innerText;
    else if (typeof elem.textContent != "undefined")
        return elem.textContent; 
    else
        return elem.text; 
}

function setInnerText(elem, text)
{
    if (typeof elem.innerText != "undefined")
        elem.innerText = text;
    else if (typeof elem.textContent != "undefined")
        elem.textContent = text; 
}

function d2hIndex(val)
{
    this._key = getInnerText(val).toLowerCase();
    this._elem = val;
}

function d2hIndexArray()
{
    this._indexes = new Array();
    
    d2hIndexArray.prototype.Add = function(val)
    {
        this._indexes[this._indexes.length] = new d2hIndex(val);
    }
    
    d2hIndexArray.prototype.IndexKeyComparer = function(a, b)
    {
        if (a._key < b._key)
            return -1;
        if (a._key > b._key)
            return 1;
        return 0;
    }
    
    d2hIndexArray.prototype.sort = function()
    {
        this._indexes.sort(this.IndexKeyComparer);
    }

    d2hIndexArray.prototype.length = function()
    {
        return this._indexes.length;
    }

    d2hIndexArray.prototype.SearchStringComparer = function(a, b, prune)
    {
        var stra, strb;
        if ((prune & 7) != 0)
        {
            if (a.length > b.length)
            {
                if ((prune & 7) == 1)
                    return 1;
                stra = a.substring(0, b.length);
                strb = b;
            }
            else
            {
                if ((prune & 7) == 2)
                    return -1;
                stra = a;
                strb = b.substring(0, a.length);
            }
        }
        else
        {
            stra = a;
            strb = b;
        }
        
        if (stra < strb)
            return -1;
        if (stra > strb)
            return 1;
        return 0;
    }

    d2hIndexArray.prototype.find = function(findKey, prevIndex)
    {
        if (findKey == null || findKey.length == 0)
            return null;
        findKey = findKey.toLowerCase();
        var low, hight;
        if (prevIndex == null)
        {
            low = 0;
            high = this._indexes.length - 1;
        }
        else
        {
            var prevKey = this._indexes[prevIndex]._key;
            var cmp = this.SearchStringComparer(findKey, prevKey, 0);
            if (cmp < 0)
            {
                low = 0;
                high = prevIndex;
            }
            else if (cmp > 0)
            {
                low = prevIndex;
                high = this._indexes.length - 1;
            }
            else
                return prevIndex;
        }
        var Ki;
        var K = findKey;
        var c;
        var j = null;
        for (;(high > low);)
        {
            j = Math.floor((high+low)/2);
            Ki = this._indexes[j]._key;
            if (K < Ki)
                high = j - 1;
            else if (K > Ki)
                low = j + 1;
            else
                return j;
        }
        c = this.SearchStringComparer(K, this._indexes[low]._key, 3);
        if (c < 0)
            return low - 1;
        else if (high > -1)
        {
            c = this.SearchStringComparer(K, this._indexes[high]._key, 3);
            if (c == 0)
                return high;
            else if (c > 0)
                return high + 1;
        }
        return low;
    }
}

function d2hIndexTable()
{
    this._table = new Array();
    this._first = null;
    this._singleLetterIndex = null;
    this._timerID = null;
    this._textControl = null;
    this._onTextChanged = null;
    this._timeout = null;
    this._currText = "";
    this._prevIndex = null;
    this._capitalLetter = null;
    this._prevCapitalIndex = null;

    d2hIndexTable.prototype.LoadAllIndexCapitalLetter = function()
    {
        if (this._capitalLetter != null)
            return;
        this._capitalLetter = new d2hIndexArray();
        var arr = document.getElementsByName("_d2h_capitalletter");
        for (var i = arr.length - 1; i >= 0; i--)
            this._capitalLetter.Add(arr[i]);
        this._capitalLetter.sort();
    }
    
    d2hIndexTable.prototype.GetAllIndexesWithFirstLetter = function(firstLetter)
    {
        var indx;
        if (this._table[firstLetter] != null)
            indx = this._table[firstLetter];
        else
        {
            var name = "_d2h_" + firstLetter.toString();
            indx = new d2hIndexArray();
            var arr = document.getElementsByName(name);
            for (var i = arr.length - 1; i >= 0; i--)
                indx.Add(arr[i]);
            indx.sort();
            this._table[firstLetter] = indx;
        }
        return indx;
    }
    
    d2hIndexTable.prototype.GetIndex = function(key)
    {
        if (key == null || key.length == 0)
            return null;
        this.LoadAllIndexCapitalLetter();
        var code = key.substring(0, 1);
        var prevCapital = null;
        if (this._capitalLetter.length() == 0)
            return null;
        if (this._prevCapitalIndex != null)
            prevCapital = this._capitalLetter._indexes[this._prevCapitalIndex]._key;
        var indx;
        if (prevCapital != code)
        {
            indx = this._capitalLetter.find(code, this._prevCapitalIndex);
            if (indx == -1)
                indx = 0;
            else if (indx > this._capitalLetter.length() - 1)
                return null;
            this._prevCapitalIndex = indx;
        }
        else
            indx = this._prevCapitalIndex;
        
        code = this._capitalLetter._indexes[indx]._key;
        var ch = code.charCodeAt(0);
        var prevIndex = null;
        if (this._first == null || ch != this._first)
        {
            this._first = ch;
            this._singleLetterIndex = this.GetAllIndexesWithFirstLetter(ch);
        }
        else
            prevIndex = this._prevIndex;
        if (this._singleLetterIndex == null || this._singleLetterIndex.length() == 0)
            return null;
        indx = this._singleLetterIndex.find(key, prevIndex);
        if (indx < 0)
        {
            if (this._prevCapitalIndex == 0)
                return this._singleLetterIndex._indexes[0]._elem;
            else
            {
                code = this._capitalLetter._indexes[this._prevCapitalIndex - 1]._key;
                ch = code.charCodeAt(0);
                var arr = this.GetAllIndexesWithFirstLetter(ch)
                return arr._indexes[arr.length() - 1]._elem;
            }
        }
        else if (indx > this._singleLetterIndex.length() - 1)
            return this._singleLetterIndex._indexes[this._singleLetterIndex.length() - 1]._elem;
        else
        {
            this._prevIndex = indx;
            return this._singleLetterIndex._indexes[indx]._elem;
        }
    }
    
    d2hIndexTable.prototype.FindIfTextChanged = function()
    {
        if (this._textControl == null)
            return;
        var curr = this._textControl.value.toLowerCase();
        if (this._currText != curr)
        {
            this.SuspendIndexSearchListener();
            this._currText = curr
            var obj = this.GetIndex(curr);
            if (obj != null)
                this._onTextChanged(obj);
                
            this.ResumeIndexSearchListener();
        }
    }

    d2hIndexTable.prototype.Find = function(key)
    {
        if (this._textControl == null)
            return null;
        var curr = key.toLowerCase();
        this.SuspendIndexSearchListener();
        this._currText = curr
        var obj = this.GetIndex(curr);
        this.ResumeIndexSearchListener();
        return obj;
    }
        
    d2hIndexTable.prototype.StartIndexSearchListener = function(textControl, timeout, handler)
    {
        this._textControl = textControl;
        this._onTextChanged = handler;
        this._timeout = timeout;
        this.SuspendIndexSearchListener();
        this.ResumeIndexSearchListener();
    }
    d2hIndexTable.prototype.StopIndexSearchListener = function()
    {
        this.SuspendIndexSearchListener();
    }
    
    d2hIndexTable.prototype.SuspendIndexSearchListener = function()
    {
        if (this._timerID != null)
        {
            clearInterval(this._timerID);
            this._timerID = null;
        }
    }

    d2hIndexTable.prototype.ResumeIndexSearchListener = function()
    {
        if (this._timerID == null)
            this._timerID = setInterval("_d2hIndex.FindIfTextChanged()", this._timeout);
    }
}

function d2hAdjustFrameHeigth(framesetId)
{
    var layout = getElemById(window.parent.document, framesetId);
    var nn = isNN();
    if (layout != null && ((nn && getNNVersionNumber() >= 6.23) || !nn))
    {
        var h = document.body.scrollHeight;
        if (nn && (typeof g_BorderMargin != "undefined"))
            h = h + g_BorderMargin; 
        layout.rows = h.toString() + "px, 100%";
    }
}

function d2hGetIndexTextProvider()
{
    var frm = getFrameByName("textprovider");
    if (frm != null)
    {
        var doc = getFrameDocument(frm);
        if (doc != null)
            return getElemById(doc, "incrsText");
    }
    return null;
}

function d2hAddIndexSearchHandler()
{
   var ctext = d2hGetIndexTextProvider();
   _d2hIndex.StartIndexSearchListener(ctext, 100, d2hOnIndexFind);
}

function d2hRemoveIndexSearchHandler()
{
    _d2hIndex.StopIndexSearchListener();
}

function d2hOnIndexFind(obj)
{
    obj.scrollIntoView();
    showSelection(document, obj);
}

function d2hGo2Index(key)
{
    var frm = getFrameByName("indexlist");
    if (frm != null)
    {
        var doc = getFrameDocument(frm);
        if (doc != null)
        {
            var wnd = getWindow(doc);
            var obj = wnd._d2hIndex.Find(key);
            if (obj != null && obj != "")
            {
                d2hOnIndexFind(obj);
                var f = obj.onclick;
                var evt = new Object();
                evt.target = obj;
                obj.setAttribute("indx", "1");
                var x = obj.offsetLeft;
                var y = obj.offsetTop;
                if (typeof obj.scrollLeft != "undefined" && !isOpera())
                {
                    var p = getElemById(doc, "scrollArea");
                    if (p)
                    {
                        x -= p.scrollLeft;
                        y -= p.scrollTop;
                    }
                }
                evt.pageX = evt.clientX = x;
                if (obj.offsetHeight)
                    y += obj.offsetHeight;
                evt.pageY = evt.clientY = y;
                f(evt);
            }
        }
    }
}

function d2hIncrsText_onkeyup(evt)
{
    if (evt.keyCode == 13)
    {
        var textElem = getElemById(document, "incrsText");
        if (textElem != null)
        {
            d2hGo2Index(textElem.value.toString());
            textElem.focus();
        }
    }
}

function d2hSetFocusTo(elemId)
{
    var elem = getElemById(document, elemId);
    if (elem != null)
    {
        if (typeof elem.focus != "undefined")
            elem.focus();
    }
}

function d2hAdjustFrameAndSetFocus(framesetid, setFocusTo)
{
    d2hAdjustFrameHeigth(framesetid);
    setTimeout("d2hSetFocusTo(\"" + setFocusTo + "\")", 40);
}

function d2hIndexLookup_onload()
{
    d2hAdjustFrameAndSetFocus('indexlayout', "incrsText");
}

function d2hSearchLookup_onload()
{
    d2hAdjustFrameAndSetFocus('searchlayout', "query");

    cUseHighlight = getElemById(document, "useHighlight" );
    if (cUseHighlight)
        d2hUpdateLastHighlightState(cUseHighlight);
}

function d2hIsTopicTitle(elem)
{
    if (elem.nodeType != 1)
        return false;
    var tagName = elem.tagName;
    tagName = tagName.substring(0, 1).toLowerCase();
    if (tagName == "h" || tagName == "p")
        return true;
    return false;
}

function d2hTraverseElements(elem, func, execForCurrentElem, useFuncResultToBreak)
{
    if (execForCurrentElem)
        if (useFuncResultToBreak)
        {
            if (!func(elem))
                return;
        }
        else
            func(elem);
    var c = elem.firstChild;
    while (c != null)
    {
        if (c.nodeType == 1)
        {
            var res = func(c);
            if (useFuncResultToBreak)
            {
                if (res)
                    d2hTraverseElements(c, func, true, useFuncResultToBreak);
            }
            else
                d2hTraverseElements(c, func, true, useFuncResultToBreak);
        }
        c = c.nextSibling;
    }
}

function d2hSetZeroMargin(elem)
{
    elem.style.margin = "0pt";
    elem.style.padding = "0pt";
}

function d2hGetFirstChildElement(parent)
{
    var c = parent.firstChild;
    while (c != null && c.nodeType != 1)
        c = c.nextSibling;
    if (c != null && c.nodeType == 1)
        return c;
    return null;
}

function d2hStandardizePopupMargin(elem, marginpading2zeroElem, ienav)
{   
    elem.style.margin = "0pt";
    elem.style.padding = marginpading2zeroElem ? "0pt" : "6pt";
    var h = null;
    var contents;
    if (typeof marginpading2zeroElem != "undefined" && marginpading2zeroElem != null)
    {
        d2hSetZeroMargin(marginpading2zeroElem);
        marginpading2zeroElem.style.padding = "6pt";
        contents = marginpading2zeroElem;
        if (typeof ienav != "undefined" && ienav != null)
        {
            ienav.className = "";
            d2hTraverseElements(ienav, d2hSetZeroMargin, true, false);
            ienav.style.padding = "6pt";
        }
    }
    else
        contents = elem;
    if (contents != null)
        h = d2hGetFirstChildElement(contents);
    if (h != null)
    {
        var tagName = h.tagName.toLowerCase();
        if (tagName == "div")
        {
            d2hSetZeroMargin(h);
            h = d2hGetFirstChildElement(h);
        }
        if (h != null && d2hIsTopicTitle(h))
            d2hSetZeroMargin(h);
    }
}

function d2hGetParentElement(elem)
{
    var parent = null;
    if (typeof elem != "undefined" && elem != null)
    {
        if (typeof elem.parentElement != "undefined")
            parent = elem.parentElement;
        else
        {
            parent = elem.parentNode;
            if (parent != null && parent.nodeType != 1)
            {
                parent = parent.parentNode;
                if (parent != null && parent.nodeType != 1)
                    parent = null;
            }
        }
    }
    return parent;
}

function d2hGetParentByTagName(elem, tagName)
{
    if (elem.tagName.toLowerCase() == tagName)
        return elem;
    else if (elem.tagName.toLowerCase() == "body" || elem.tagName.toLowerCase() == "head" || elem.tagName.toLowerCase() == "html")
        return null;
    else
        return d2hGetParentByTagName(d2hGetParentElement(elem), tagName);    
}


function d2hGetSelectedCell(evt)
{
    evt = (evt) ? evt : ((window.event) ? event : null);
    if (!evt)
        return;
    var elem = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
    if (!elem)
        return;
    if (elem.tagName.toLowerCase() == "td")
        return elem;
    var result = d2hGetParentElement(elem);
    while (result.tagName.toLowerCase() != "td" && result.tagName.toLowerCase() != "body")
        result = d2hGetParentElement(result);
    if (result.tagName.toLowerCase() == "body")
        return elem;
    else
        return result;    
}

function d2hShowTopicTitleInPopup(doc)
{
    var nav = getElemById(doc, "nsbanner");
    if (nav == null)
        return;
    var title = getElemById(doc, "_d2hTitleNavigator");
    if (title == null)
    {
        nav.style.display = "none";
        return;
    }
    var parent = d2hGetParentElement(nav);
    if (parent == null)
        return;
    var objTitle = null;
    if (typeof title.removeNode != "undefined")
        objTitle = title.removeNode(true);
    else
        objTitle = title.cloneNode(true);
    parent.replaceChild(objTitle, nav);
}

function d2hHideBreadcrumbs(doc)
{
    var breadcrumbs = getElemById(doc, "d2h_breadcrumbs");
    if (breadcrumbs != null)
        breadcrumbs.style.display = "none";
}

function d2hNeedSendToBody(elem)
{
    var p = d2hGetParentElement(elem);
    return !(p == null || p == document.body);
}

function d2hMoveToEnd(elem, newParent)
{
    var obj = null;
    if (typeof elem.removeNode != "undefined")
        obj = elem.removeNode(true);
    else
    {
        var parent = d2hGetParentElement(elem);
        obj = parent.removeChild(elem);
    }
    newParent.appendChild(obj);
}

function d2hSend2Body(elem)
{
    var body = document.body;
    d2hMoveToEnd(elem, body);
}

function d2hGetInlinePosition(evt)
{
	var pt;
	if (evt.pageX)
		pt = new point(evt.pageX, evt.pageY);
	else
		pt = new point(evt.clientX + document.body.scrollLeft, evt.clientY + document.body.scrollTop);
    return pt;
}

function d2hGetRoot(strPath)
{
    var i, ch;
    for (i = strPath.length - 1; i > 0; i--)
    {
        ch = strPath.substring(i - 1, i); 
        if (ch == '\\' || ch == '/')
            return strPath.substring(0, i - 1);
    }
    return "";
}

function d2hIsAscii(charCode)
{
    return (charCode > 7 && charCode < 127);
}

function d2hIsAsciiOnly(str)
{
    for (var i = 0; i < str.length; i++)
    {
        if (!d2hIsAscii(str.charCodeAt(i)))
            return false;
    }
    return true;
}

function d2hGetServerType()
{
	if (isServerSide())
	{
	    var def = "jsp";
	    var platform =  d2hServerPlatform ? d2hServerPlatform.toLowerCase() : def;
	    if (platform != "asp" && platform != "jsp")
	        platform = def;
	    return platform;
	}
	else
        {
            var ext = d2hDefaultExtension;
            if (ext.length > 0 && ext.substring(0, 1) == ".")
                ext = ext.substring(1);
	    return ext;
        }
}

function d2hGetMainThemeWnd(wnd)
{
    if (!wnd)
        return null;
    var nm = wnd.name.toLowerCase();
    if (wnd.g_bMainWnd == true || nm == "" || nm == "right")
        return wnd;
    return d2hGetMainThemeWnd(wnd.parent);
}

function d2hGetMainLayoutWnd(wnd, considerTopWndAsMain)
{
    if (!wnd)
        return null;
    if (wnd.g_mainLayout == true)
        return wnd;
    if (wnd == wnd.parent)
        return considerTopWndAsMain ? wnd : null;
    return d2hGetMainLayoutWnd(wnd.parent);
}

function d2hGetHRefWithoutHash(href)
{
    var indx = href.indexOf("#");
    if (indx > -1)
        href = href.substring(0, indx);
    return href;
}

function IsScriptHref(href)
{
    var l = href.length;
    if (l >= 8)
    {
        var protocol = href.substring(0, 8);
        if (protocol == "jscript:")
            return true;
        if (l >= 9)
        {
            protocol = href.substring(0, 9);
            if (protocol == "vbscript:")
                return true;
            if (l >= 11)
            {
                protocol = href.substring(0, 11);
                if (protocol == "javascript:")
                    return true;
            }
        }
    }
    return false;
}

function d2hPrepareAnchors4Popup()
{
    var hRefCurWnd = d2hGetHRefWithoutHash(window.location.href).toLowerCase();
    var doc = getDoc(window)
    var listObj = doc.getElementsByTagName("a");
    var elem, hr;
    for (i = 0; i < listObj.length; i++)
    {
        elem = listObj.item(i);
        hr = d2hGetHRefWithoutHash(elem.href).toLowerCase();
        if (hr.length != 0 && !elem.onclick && elem.target.length == 0 && !IsScriptHref(hr) && hr != hRefCurWnd)
            elem.onclick = d2hOnClickAInPopupWnd;
    }    
}

function d2hOnClickAInPopupWnd(evt)
{
    var mainWnd = d2hGetMainThemeWnd(window);
    if (!mainWnd)
        return true;
    evt = (evt) ? evt : ((typeof window.event != "undefined") ? event : null);
    if (evt)
    {
        var elem = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
        if (elem)
        {
            if (elem.nodeType == 3)
                elem = elem.parentNode;
            if (elem.tagName.toLowerCase() == "a")
            {
                if (typeof evt.cancelBubble != "undefined")
                    evt.cancelBubble = true;
                if (typeof evt.preventDefault != "undefined" && typeof evt.stopPropagation != "undefined")
                {
                    evt.preventDefault();
                    evt.stopPropagation();
                }
                var h = elem.href;
                if (window.parent)
                    window.parent.dhtml_popup();
                mainWnd.location.href = h;
                return false;
            }
        }
    }
    return true;
}

function d2hProcessTopicLinksForCSH()
{
	if (window.g_bCSHTopic || (typeof window.g_bCSHTopic == "undefined" && d2hIsCSH()))
	{
		var wnd = d2hGetMainThemeWnd(window);
		var listObj = document.getElementsByTagName("a");
		for (i = 0; i < listObj.length; i++)
			d2hProcessLinkElemForCSH(listObj.item(i), wnd.name);
		listObj = document.getElementsByTagName("button");
		for (i = 0; i < listObj.length; i++)
			d2hProcessLinkElemForCSH(listObj.item(i), wnd.name);		
		listObj = document.getElementsByTagName("area");
		for (i = 0; i < listObj.length; i++)
			d2hProcessLinkElemForCSH(listObj.item(i), wnd.name);		
	}
}

function d2hProcessLinkElemForCSH(elem, mainWnd)
{
	var t = d2hGetAttribute(elem, "target");
	if (t == "right")
		elem.setAttribute("target", mainWnd);
	var elemTarget = d2hGetAttribute(elem, "href");
	if (!elemTarget)
		return;
	var hr = d2hGetHRefWithoutHash(elemTarget);
	if (hr.length > 0)
	{
		var hash = "";
		if (hr.length < elemTarget.length)
			hash = elemTarget.substring(hr.length, elemTarget.length);
		if (hr.indexOf("?") == -1)
			hr += "?csh=1";
		else
			hr += "&csh=1";
		hr += hash;
		elem.setAttribute("href", hr);
	}
}

function d2hGetAttribute(elem, name)
{
	if (elem == null)
		return null;
	var res = null;
	if (typeof elem.getAttribute != "undefined")
		res = elem.getAttribute(name);
	if (typeof res != "undefined" && res != "")
		return res;
	if (typeof elem.outerHTML != "undefined")
	{
		var elemBody = elem.outerHTML;
		elemBody = elemBody.substring(elem.tagName.length + 1, elemBody.length);
		var indx = elemBody.indexOf(">");
		if (indx > -1)
		{
			elemBody = elemBody.substring(0, indx);
			var eBodyLow = elemBody.toLowerCase()
			name = name.toLowerCase() + "=";
			indx = eBodyLow.indexOf(name);
			if (indx > -1)
			{
				elemBody = elemBody.substring(indx + name.length + 1, elemBody.length);
				indx = elemBody.indexOf('"');
				if (indx > -1)
					res = elemBody.substring(0, indx);
				else
					res = elemBody;
			}
		}
	}
	return res;
}

function d2hProcessTopicNavForCSH()
{
    if (d2hIsCSH())
    {
        d2hHideMainNav();
        window.g_bCSHTopic = true;
        window.self.g_bMainWnd = true;
    }
    d2hProcessTabParam();
}

function d2hProcessTabParam()
{
    var mainWnd = d2hGetMainLayoutWnd(window, false);
    if (mainWnd && mainWnd.tabProcessed) return;
    var tabId = d2hGetSearchVal(document, "tab");
    if (!tabId || tabId.length == 0)
    {
        if (mainWnd && mainWnd != window)
        {
            var doc = getDoc(mainWnd);
            if (doc)
                tabId = d2hGetSearchVal(doc, "tab");
        }
    }
    if (tabId && tabId.length > 0 && (tabId = GetButtonId(tabId)) != null)
    {
        d2hCommand(tabId);
        mainWnd.tabProcessed = true;
    }
}

function d2hGetSearchVal(doc, query)
{
    var strSearch = doc.location.search;
    strSearch = strSearch.substring(1, strSearch.length);
    return getQueryVal(strSearch, query);
}

function GetButtonId(tabId)
{
    var buttonID = null;
    if (tabId == "0")
        buttonID = 'D2HContents';
    else if (tabId == "1")
        buttonID = 'D2HIndex';
    else if (tabId == "2")
        buttonID = 'D2HSearch';
    else if (tabId == "3")
        buttonID = 'D2HFavorites';
    return buttonID;
}

function d2hHideMainNav()
{
	var nav = getElemById(document, "_d2hNavigatorLinks");
	if (nav)
	{
		nav.style.visibility = "hidden";
		nav.style.display = "none";
	}
}

function d2hIsCSH()
{
	var strSearch = document.location.search;
	strSearch = strSearch.substring(1, strSearch.length);
	var cshVal = getQueryVal(strSearch, "csh");
	return cshVal == "1";
}

function d2hButtonClick(evt)
{
	d2hLinkClick(evt);
}

function d2hLinkClick(evt)
{
	evt = (evt) ? evt : ((typeof window.event != "undefined") ? event : null);
	if (evt)
	{
		var elem = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
		if (elem)
		{
			if (elem.nodeType == 3)
				elem = elem.parentNode;
			var tag = elem.tagName.toLowerCase();
			if (tag != "a" && tag != "button" && tag != "area")
				elem = elem.parentNode;
			var target = d2hGetAttribute(elem, "target");
			var sWndFeatures = "";
			var isMenuItem = elem.getAttribute("mnui") == "1";
			var isIndexItem = elem.getAttribute("indx") == "1";
			if (typeof d2hInitWindowParams != "undefined")
			{
				d2hInitWindowParams();
				if (_d2hWindowParamsByWindows[target])
					sWndFeatures = _d2hWindowParamsByWindows[target];
				else if (elem.tagName.toLowerCase() == "a" && !isMenuItem && !isIndexItem)
					return true;
			}
			var href = d2hGetAttribute(elem, "href");
			var wnd = null;
			var w = isMenuItem ? window.parent : window;
			if (href && target)
			{
				if (isOpera())
				{
					var doc = getDoc(w);
					wnd = w.open(d2hGetRelativePath(doc, "_d2hblank" + d2hDefaultExtension), target, sWndFeatures);
					wnd.location.href = href;
				}
				else
					wnd = w.open(href, target, sWndFeatures);
			}
			else if (href)
			    wnd = w.open(href, "_self");
			if (wnd && typeof wnd.focus != "undefined")
				wnd.focus();
			return wnd == null;
		}
	}
	return true;
}

function d2hRegisterEventHandler(obj, altObj, eventName, handler)
{
	var o = altObj;
	var oldHandler = o[eventName.toLowerCase()];
	// Safari and Chrome return null while IE, Opera, Firefox return undefined value
	if (typeof oldHandler == "undefined" || (isSafari() && oldHandler == null))
	{
		o = obj;
		oldHandler = o[eventName.toLowerCase()];
	}
	if (typeof oldHandler == "undefined")
	{
		if (typeof document.scripts != "undefined")
		{
			var objName = typeof obj.open != "undefined" ? "window" : obj.tagName.toLowerCase();
			var altName = typeof altObj.open != "undefined" ? "window" : altObj.tagName.toLowerCase();
			for (var i = 0; i < document.scripts.length; i++) 
			{
				var script = document.scripts[i];
				if ((script.htmlFor == obj.id || script.htmlFor == objName || script.htmlFor == altObj.id || script.htmlFor == altName) && script.event == eventName)
				{
					oldHandler = script.innerHTML;
					break;
				}
			}
		}
	}
	else
	{
		var defFunc = "";
		if (oldHandler)
			defFunc = oldHandler.toString();
		var beg = defFunc.indexOf('{');
		var end = defFunc.lastIndexOf('}');
		if (beg > 0 || end > beg)
			oldHandler = defFunc.substring(beg + 1, end - 1);
	}
	if (oldHandler && oldHandler.indexOf(handler) >= 0)
		return;
	var newHandler = oldHandler;
	if (oldHandler == null || newHandler.length == 0)
		newHandler = handler;
	else
		newHandler += "; " + handler;
	o[eventName.toLowerCase()] = new Function("event", newHandler);
}

function d2hInitMainThemeHandlers(prev, next)
{
    var wnd = d2hGetMainWindow();
    if (isChrome() && window.location.protocol.toLowerCase() == "file:" && (wnd == null || wnd.g_mainLayout == undefined))
        alert("Due to security limitations, this version of Chrome browser does not work correctly with NetHelp stored in local files on your computer. You can use this Chrome version to view NetHelp deployed on the web without limitations, but for local files please use a different browser.");
	d2hRegisterEventHandler(window, document.body, "onload", "d2hnsresize(event);d2hSetNavigatorState(" + prev + "," + next + ");d2hProcessTopicLinksForCSH();d2hProcessHighlight();");
	d2hRegisterEventHandler(window, document.body, "onmousedown", "d2hpopup(event);");
}

function d2hInitSecThemeHandlers()
{
	d2hRegisterEventHandler(window, document.body, "onload", "d2hload();d2hProcessHighlight();");
	d2hRegisterEventHandler(window, document.body, "onmousedown", "d2hpopup(event);");
}

function d2hGetMenuPanel(doc)
{
	var mnuId = "_d2h_popup_menu"
	var mnu = _d2hPopupMenu ? _d2hPopupMenu : getElemById(doc, mnuId);
	if (mnu == null)
	{
        mnu = doc.createElement("div");
        mnu.id = mnuId;
        mnu.height = "0px"
        mnu.width = "0px"
        mnu.className = "d2hPopupMenu";
        mnu.style.zIndex = 1000;
        mnu.noWrap = true;
        d2hHidePopupMenu(mnu);
        doc.body.appendChild(mnu);
    }
    else
    {
		d2hHidePopupMenu(mnu);
		mnu.innerHTML = "";
        mnu.height = "0px"
        mnu.width = "0px"		
	}
    return mnu;
} 

function d2hSetPopupMenuPos(doc, popupElem, x, y)
{
	var nstext = getElemById(doc, "nstext");
	var wnd = getWindow(doc);
	if (nstext == null)
		nstext = doc.getElementsByTagName((doc.compatMode && doc.compatMode == "CSS1Compat") ? "HTML" : "BODY")[0];
	var w_width = nstext.clientWidth ? nstext.clientWidth + nstext.scrollLeft : wnd.innerWidth + wnd.pageXOffset;
	var w_height = nstext.clientHeight ? nstext.clientHeight + nstext.scrollTop : wnd.innerHeight + wnd.pageYOffset;
	popupElem.style.width = "auto";
	popupElem.style.height = "auto";
	var textWidth = popupElem.offsetWidth;
	if (textWidth < 100)
		textWidth += 30;
	popupElem.style.width = textWidth + "px";
	var t_width = popupElem.offsetWidth;
	var t_height = popupElem.offsetHeight;
	popupElem.style.left = x + "px";
	popupElem.style.top = y + "px";
	var x_body_bottom = (doc.body.clientWidth ? doc.body.clientWidth : wnd.innerWidth) + doc.body.scrollLeft;
	var y_body_bottom = (doc.body.clientHeight ? doc.body.clientHeight : wnd.innerHeight) + doc.body.scrollTop;
	if (x + t_width > x_body_bottom)
	{
		x_body_bottom = x_body_bottom - t_width;
		if (x_body_bottom < 0)
			x_body_bottom = 0;
		popupElem.style.left = x_body_bottom + "px";
	}
	if (y + t_height > y_body_bottom)
	{
		y_body_bottom = y_body_bottom - t_height;
		if (y_body_bottom < 0)
			y_body_bottom = 0;
		popupElem.style.top = y_body_bottom + "px";
	}
}

function d2hShowPopupMenu(evt, doc, menu, arrLinks)
{
	if (typeof menu.style.display != "undefined")
		menu.style.display = "";

	var pt = d2hGetInlinePosition(evt);
	menu.style.width = "auto";
	menu.style.height = "auto";
	d2hSetPopupMenuPos(doc, menu, pt.x, pt.y);
	var isPopupObj = typeof window.createPopup != "undefined";
	var ie = isIE();
	if (arrLinks != null)
	{
		for (var i = 0; i < arrLinks.length; i++)
		{
			if (ie)
			{
				arrLinks[i].style.width = "100%";
				arrLinks[i].tabIndex = -1;
			}
			arrLinks[i].setAttribute("mnui", "1");
			if (isPopupObj)
				arrLinks[i].onclick = "d2hMenuItemClick(event)";
			else
				arrLinks[i].onclick = d2hMenuItemClick;
		}
	}
	
	if (!isPopupObj)
	{
		menu.style.visibility = "visible";
		return;
	}
	
	var popDoc = null;
	if (typeof menu.g_popupLayer == "undefined")
	{
		menu.g_popupLayer = window.createPopup();
		popDoc = getDoc(menu.g_popupLayer);
		var relpath = d2hGetAttribute(doc.body, "relPart");
		if (!relpath)
			relpath = "";
		popDoc.createStyleSheet(relpath + "Theme/popupmenu.css");
		d2hLoadScript(popDoc, "", relpath + "linker.js");
		d2hLoadScript(popDoc, "", relpath + "special.js");
		d2hLoadScript(popDoc, "", relpath + "common.js");
		var wnd = getWindow(popDoc);
		if (typeof d2hInitWindowParams != "undefined")
		{
			d2hInitWindowParams();
			wnd.d2hInitWindowParams = new Function();
			wnd._d2hWindowParamsByWindows = _d2hWindowParamsByWindows;
		}
	}
	else
		popDoc = getDoc(menu.g_popupLayer);
	var popup = menu.g_popupLayer;
	if (popDoc)
	{
		var oPopBody = popDoc.body;
		oPopBody.innerHTML = "";
		oPopBody.innerHTML = menu.outerHTML;
		var popMenu = getElemById(popDoc, "_d2h_popup_menu");
		if (popMenu)
		{
			popMenu.style.position = "";
			popMenu.style.left = "0";
			popMenu.style.top = "0";
			popMenu.style.visibility = "visible";
			popup.show(evt.clientX, evt.clientY, menu.offsetWidth, menu.offsetHeight, doc.body);
			return;
		}
	}
	menu.style.visibility = "visible";
}

function d2hHidePopupMenu(menu)
{
	if (!menu)
		return;
	if (typeof menu.g_popupLayer != "undefined")
		menu.g_popupLayer.hide();
	d2hHideInline(menu);
}

function d2hClosePopupMenu(evt, force)
{
    if (_d2hPopupMenu != null)
    {
		if (!force)
		{
			var pt = d2hGetInlinePosition(evt);
			var l = parseInt(_d2hPopupMenu.style.left);
			var t = parseInt(_d2hPopupMenu.style.top);
			var isLeftButton = true;
			var w3DOM = !(isIE() || (isOpera() && getOperaVersionNumber() < 8.0 /* In Opera, before 8.0: event.button == 1 for left mouse button, in 8.0 and higher: event.button == 0 */));
			if (typeof evt.button != "undefined")
			{
				if (evt.button != 1 && !w3DOM)
					isLeftButton = false;
				else if (evt.button != 0 && w3DOM)
					isLeftButton = false;
			}
			if (isLeftButton && pt.x > l && pt.y > t && pt.x <= l + _d2hPopupMenu.offsetWidth && pt.y <= t + _d2hPopupMenu.offsetHeight)
				return; 
		}
        d2hHidePopupMenu(_d2hPopupMenu);
        _d2hPopupMenu = null;
    }
}

function d2hLoadKeywordList()
{
    var strSearch = document.location.search;
    if (strSearch == null || strSearch.length == 0)
        return;
    strSearch = strSearch.substring(1, strSearch.length);
    if (strSearch == null || strSearch.length == 0)
        return;
    var linkID = getQueryVal(strSearch, "id");
    if (linkID == null || linkID.length == 0)
        return;
    window.g_d2hMenuItemsLoaded = d2hShowKeywordList;
    d2hLoadMenuItems(linkID, "AKLinks/" + linkID + '.js', true);
}

function d2hShowKeywordList(items)
{
    var arr = items;
    if (arr == undefined)
        return true;
    var header = getElemById(document, 'd2hKeywordTitle');
    if (header == null)
        return true;
    var list = getElemById(document, 'd2hKeywordList');
    if (list == null)
        return true;
    header.innerHTML = MSG_MANY_TOPICS_FOUND.replace('%d', arr.length);
    for (var i = 0; i < arr.length; i++)
    {
        var text = '<a id="d2hKeywordLink_' + i + '" href="' + d2hGetRelativePath(document, arr[i][0]) + '" title="' + TITLE_HOT_SPOT_JUMP + '"' + ((arr[i][1] != null) ? ' target = "' + arr[i][1] + '"' : '') + '>' + 
        '<img border=0  align=absMiddle src=' + d2hGetRelativePath(document, IMAGE_TOPIC) + ' alt="">' + arr[i][2] + '</a>';
        var s = document.createElement("p");
        s.innerHTML = text;
        list.appendChild(s); 
    }
    var firstLink = getElemById(document, 'd2hKeywordLink_0');
    if (firstLink != null)
        firstLink.focus();
    else
        document.focus();
    return true;
}

function d2hMenuItemClick(evt)
{
	d2hHidePopupMenu(_d2hPopupMenu);
	return d2hLinkClick(evt);
}

function d2hPopulateMenu(doc, menu, arr)
{
	var mnuItem, a, arrLinks;
	arrLinks = new Array();
	var relpath = d2hGetAttribute(doc.body, "relPart");
	if (!relpath)
		relpath = "";
	for (var i = 0; i < arr.length; i++)
	{
		mnuItem = doc.createElement("div");
		a = doc.createElement("a");
		a.setAttribute("href", relpath + arr[i][0]);
		if (arr[i][1] != null)
			a.setAttribute("target", arr[i][1]);
		a.innerHTML = arr[i][2];
		mnuItem.appendChild(a);
		arrLinks[arrLinks.length] = a;
		menu.appendChild(mnuItem);
	}
	return arrLinks;
}

function d2hPopupMenu(evt, arg)
{
	evt = (evt) ? evt : ((typeof window.event != "undefined") ? event : null);
	if (!evt)
		return false;
	var arr = d2hGetMenuItems(arg);
	var elem = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
	var doc = d2hGetOwnerDocument(elem);
	if (arr == null || arr.length == 0)
		alert(MSG_ZERO_TOPICS_FOUND);
	else if (arr.length == 1 || USE_SECTION_508 == "yes")
	{
		if (elem)
		{
			if (elem.nodeType == 3)
				elem = elem.parentNode;
			var tag = elem.tagName.toLowerCase();
			if (tag != "a" && tag != "button" && tag != "area")
				elem = elem.parentNode;
			var relpath = d2hGetAttribute(doc.body, "relPart");
			if (!relpath)
				relpath = "";
    		if (USE_SECTION_508 == "yes" && arr.length > 1)
                {
    			elem.setAttribute("href", relpath + "_d2h_keyword_links.htm?id=" + arg);
    			if (getFrameByName("right", d2hGetMainWindow()) != null)
		    	    elem.setAttribute("target", "right");
                }
    		else	
    		{
    			elem.setAttribute("href", relpath + arr[0][0]);
	        	if (arr[0][1] != null)
			    	elem.setAttribute("target", arr[0][1]);
			}
			return d2hLinkClick(evt);
		}
	}
	else
	{
            _d2hPopupMenu = d2hGetMenuPanel(doc);
            arr = d2hPopulateMenu(doc, _d2hPopupMenu, arr);
            d2hShowPopupMenu(evt, doc, _d2hPopupMenu, arr);
	}
	return false;
}

function d2hLoadScript(doc, elemId, src)
{
	var elem = doc.createElement("script");
	elem.setAttribute("language", "javascript");
	if (elemId != "")
		elem.id = elemId;
    elem.src = src;
    var listObj = doc.getElementsByTagName("head");
    var head = null;
    if (listObj && listObj.length > 0)
        head = listObj[0];
    else
        head = doc.body;
	head.appendChild(elem);
}

function Load2MenuBag(bag, doc, key, src)
{
	bag._files[key] = src;
	d2hLoadScript(doc, "_d2hscr_" + key, src);
}

function d2hGetMenuStorageWindow(wnd)
{
	if (!wnd)
        wnd =  window;
    if (!wnd.parent || wnd == wnd.parent)
        return wnd;
    return d2hGetMenuStorageWindow(wnd.parent);
}

function d2hGetMenuItems(key)
{
	var wnd = d2hGetMenuStorageWindow();
	return wnd.g_d2hMenuBag._items[key];
}

function d2hStoreMenuItems(key, items)
{
	var wnd = d2hGetMenuStorageWindow();
	wnd.g_d2hMenuBag._items[key] = items;
	if (typeof window.g_d2hMenuItemsLoaded != "undefined")
	    if (window.g_d2hMenuItemsLoaded(items))
	        window.g_d2hMenuItemsLoaded = null;
}
	
function d2hLoadMenuItems(key, datafile, reload)
{
	var wnd = d2hGetMenuStorageWindow();
	if (wnd.g_d2hMenuBag == null)
	{
		wnd.g_d2hMenuBag = new Object();
		wnd.g_d2hMenuBag._files = new Array();
		wnd.g_d2hMenuBag._items = new Array();
	}
	if ((typeof reload == "undefined" || !reload) && wnd.g_d2hMenuBag._files[key] != null)
		return;
	Load2MenuBag(wnd.g_d2hMenuBag, document, key, datafile);
}

function d2hLoadWindows()
{
	var windows = d2hGetRelativePath(document, "windows.js");
	if (typeof g_hubProject != "undefined" && g_hubProject.length > 0 && windows[0] != '/')
		windows = '/' + windows;
	windows = g_hubProject + windows;
	d2hLoadScript(document, "", windows);
}

function d2hLoadNavUrls()
{
	var urls = d2hGetRelativePath(document, "urls.js");
	if (typeof g_hubProject != "undefined" && g_hubProject.length > 0 && urls[0] != '/')
		urls = '/' + urls;
	urls = g_hubProject + urls;
	d2hLoadScript(document, "", urls);
}

function d2hCoupleUrl(href, doc)
{
	var hubPrj = null;
	if (doc)
	{
		var wnd = getWindow(doc);
		if (typeof wnd.g_hubProject != "undefined")
			hubPrj = wnd.g_hubProject;
	}
	if (hubPrj == null && typeof g_hubProject != "undefined")
		hubPrj = g_hubProject;
	if (hubPrj == null)
		return href;
	if (hubPrj.length > 0 && href[0] != '/')
		href = '/' + href;
	href = hubPrj + href;
	return href;
}

function d2hCoupleINav(evt)
{
	var elem = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
	if (elem)
	{
		if (elem.nodeType == 3)
			elem = elem.parentNode;
		var tag = elem.tagName.toLowerCase();
		if (tag != "a")
			elem = elem.parentNode;
		if (d2hGetAttribute(elem, "joined") == "1")
			return;
		var href = null;
		if (elem.id == 'D2HContents')
			href = window.g_ContentsURL;
		else if (elem.id == 'D2HIndex')
			href = window.g_IndexURL;
		else if (elem.id == 'D2HSearch')
			href = window.g_SearchURL;
		else if (elem.id == 'D2HFavorites')
			href = window.g_FavoritesURL;
		if (href)
		{
			elem.setAttribute("joined", "1");
			elem.setAttribute("href", d2hCoupleUrl(d2hGetRelativePath(document, href)));
		}
	}
}

function d2hGetMainWindow()
{
    return d2hGetMainLayoutWnd(window, true);
}

function d2hUpdateLastHighlightState(checkbox)
{
    var wndMain = d2hGetMainWindow();
    if (typeof wndMain.useHighlight == "undefined")
        d2hSetLastHighlightState(checkbox.checked);
    else
        checkbox.checked = wndMain.useHighlight;
}

function d2hSetLastHighlightState(state)
{
    d2hGetMainWindow().useHighlight = state;
}

function d2hGetSearchFrameDocument()
{
    var wndMain = window;
    if (window.name == "right")
        wndMain = window;
    else if (window.name == "searchlist")
        wndMain = window.parent;
    else if (dhtml_popup_is_open())
        wndMain = window.parent;
    else if (window.opener)
        wndMain = !window.opener.closed ? window.opener.parent : d2hGetMainWindow();
    else if (window.name == "textprovider")
        return document;

    var frame1 = getFrameByName("left", wndMain);
    if (!frame1)
        return null;
        
    var docFrame1 = getFrameDocument(frame1);
    if (!docFrame1)
        return null;

    var frame2 = getElemById(docFrame1, "textprovider");
    if (!frame2)
        return null;
        
    var docFrame2 = getFrameDocument(frame2);
    if (!docFrame2)
        return null;

    return docFrame2;
}

function d2hGetRightFrameDocument()
{
    var frame1 = getFrameByName("right", d2hGetMainWindow());
    if (!frame1)
        return null;

    var docFrame1 = getFrameDocument(frame1);
    if (!docFrame1)
        return null;

    return docFrame1;
}

function d2hSetClickState(state)
{
    var frmSearch = d2hGetSearchFrameDocument();
    if (!frmSearch)
        return;

    var hClickState = getElemById(frmSearch, "clickStatus");
    if (hClickState)
        hClickState.value = state;

}

function d2hGetClickState(state)
{
    var frmSearch = d2hGetSearchFrameDocument();
    if (!frmSearch)
        return;

    var hClickState = getElemById(frmSearch, "clickStatus");
    if (hClickState)
        return hClickState.value;

    return null;
}

function d2hIsUseHighlight()
{
    var frmSearch = d2hGetSearchFrameDocument();
    if (!frmSearch)
        return;

    var cUseHighlight = getElemById(frmSearch, "useHighlight");
    if (cUseHighlight)
        return cUseHighlight.checked;
        
    return true;
}

function d2hProcessHighlightRightFrame()
{
    try
    {
        var frmRight = d2hGetRightFrameDocument();
        if (!frmRight)
            return;
        d2hSetClickState("onresult");
        d2hProcessHighlight(frmRight);
    }
    catch(ex)
    {}
}

function addSpace(str, words, startIndex, checkForEastern)
{
    for (var i = startIndex; i < words.length; i++)
    {
        var pos = str.indexOf(" ", 0);
        if (pos != -1)
            return addSpace(str.substring(0, pos), words, i, checkForEastern) + " " + addSpace(str.substring(pos + 1), words, i, checkForEastern);
        else if (pos == -1 && (!checkForEastern || isEasternLanguage(str)))
        {
            pos = str.indexOf(words[i], 0);
            if (pos != -1)
            {
                var left = addSpace(str.substring(0, pos), words, i, checkForEastern);
                var right = addSpace(str.substring(pos + words[i].length), words, i, checkForEastern);
                if (left && left != "\"")
                    left += " ";
                if (right && right != "\"")
                    right = " " + right;
                var newStr = words[i];
                if (left)
                    newStr = left + newStr;
                if (right)
                    newStr += right;
                return newStr;
            }
        }
    }    
    return str;
}

function addSpacesBetweenWords(elem)
{
    var query = elem.value;
    query = query.replace(/\u0009|\u000a|\u000b|\u000c|\u000d|\u00a0|\u1680|\u180e|\u2000|\u2001|\u2002|\u2003|\u2004|\u2005|\u2006|\u2007|\u2008|\u2009|\u200a|\u2028|\u2029|\u202f|\u205f|\u3000/g," ");
    query = query.replace(/\s+/g, " ").replace(/^\s+/g, "").replace(/\s+$/g, "");
    var mainWindow = d2hGetMainWindow();
    var words = mainWindow.getWords();  
    var res = addSpace(query, words, 0, true);
    elem.value = res;
    return res;
}

function isEasternLanguage(s)
{
    var codes = [[0x2E80,0x9FFF],[0xA000,0xA63F],[0xA6A0,0xA71F],[0xA800,0xA82F],[0xA840,0xD7FF],[0xF900,0xFAFF],[0xFE30,0xFE4F]];
    for (var i = 0; i < s.length; i++)
    {
        var code = s.charCodeAt(i);
        if (code < codes[0][0])
            continue;
        for (var j = 0; j < codes.length; j++)
            if (code >= codes[j][0] && code <= codes[j][1])
                return true;
    }
    return false;
}

function d2hProcessHighlight(doc)
{
    try
    {
        if (typeof doc == "undefined" || doc == null)
            doc = document;
                
        var frmSearch = d2hGetSearchFrameDocument();
        if (!frmSearch)
            return;

        if (d2hGetClickState() != "onresult")
            return;
        d2hSetClickState("");

        if (!d2hIsUseHighlight())
            return;

        var eQuery = getElemById(frmSearch, "query");
        if (!eQuery)
            return;

        var strRequest = eQuery.value;

        var listSearch = d2hSplitRequest(strRequest);

        d2hHighlightNode(doc.body, listSearch, false, true);
    }
    catch(ex)
    {}
}

function getWildcardRegexp(wildcard)
{
	wildcard = wildcard
		.replace(/[-[\]{}()+.,\\^$|]/g, '\\$&')
		.replace(/\*+/g, '\\w*')
		.replace(/\?/g, '\\w')
		.replace(/\s+/g, '\\s+');
    if (wildcard === '\\w*') {
        wildcard = '\\w+';
    }
    if (!isEasternLanguage(wildcard)) {
        var l = wildcard.length,
            startIsWordChar = /^(\w|\.|\\w)/,
            endIsWordChar = /(\w|\.|\*|\\w\+)$/;
        wildcard = (startIsWordChar.test(wildcard) ? '\\b' : '') + 
            wildcard +
            (endIsWordChar.test(wildcard) ? '\\b' : '');
    }
    return wildcard;
}

var style_name = "d2hHlt";

function d2hHighlightNode(node, request, caseSensitive, wholeWord)
{
    if (!request || node.childNodes.length == 0)
        return;

    var mainWindow = d2hGetMainWindow();
    for (var i = 0; i < request.length; i++)
    {
        if (!caseSensitive)
            request[i] = request[i].toLowerCase();
        if (mainWindow && mainWindow.aliasesHT[request[i]] && !mainWindow.isWildcard(request[i]))
        {
            var aliasesRows = mainWindow.aliasesHT[request[i]];
            for (var j = 0; j < aliasesRows.length; j++)
            {
                var words = mainWindow.g_sAliases[aliasesRows[j]]
                for (var w = 0; w < words.length; w++)
                    if (w == 0)
                        request[i] = getWildcardRegexp(words[w]);
                    else
                        request[i] += "|" + getWildcardRegexp(words[w]);
            }
        }
        else if (mainWindow && mainWindow.isWildcard(request[i]))
        {
            var words = mainWindow.getWordsFromIndex(request[i]);
            if (words.length > 0)
            {
                request[i] = getWildcardRegexp(words[0]);
                for (var j = 1; j < words.length; j++)
                    request[i] += "|" + getWildcardRegexp(words[j]);
            }
        }        
        else
            request[i] = getWildcardRegexp(request[i]);
    }

    var regexpRequest = new RegExp(request.join("|"), caseSensitive ? "" : "i");

    var nodeproc = function(node)
    {
        var match = regexpRequest.exec(node.data);
        if (match)
        {
            var val = match[0];
            var node2 = node.splitText(match.index);
            var node3 = node2.splitText(val.length);
            var span = node.ownerDocument.createElement('span');
            node.parentNode.replaceChild(span, node2);
            span.className = style_name;
            span.appendChild(node2);
            return span;
        }

        return node;
    };

    walkNodes(node.childNodes[0], 1, nodeproc);
}

function d2hRemoveHighlightRightFrame()
{
    try
    {
        var frmRight = d2hGetRightFrameDocument();
        if (!frmRight)
            return;
        d2hRemoveHighlightNode(frmRight.body);
    }
    catch(ex)
    {}
}

function d2hRemoveHighlightNode(node)
{
    var nodeproc = function(node)
    {
        if(node.parentNode.className == style_name)
            node.parentNode.className = "";
        return node;
    };

    walkNodes(node.childNodes[0], 1, nodeproc);
}

function d2hSplitRequest(request)
{
    var arr = new Array();
    var i = 0;    
    var j = 0;
    var l = -1;
    while (j < request.length)    
    {
        if (request.charAt(j) == '\"')
        {
            if (l == -1)
                l = j;
            else
            {
                arr[i] = request.substring(l+1, j);
                if (isEasternLanguage(arr[i]))
                    arr[i] = arr[i].replace(/\s+/gi, "?");                
                request = request.substring(0, l) + request.substring(j+1, request.length);                
                i++;
                j = l - 1;
                l = -1;
            }
        }
        j++;
    }  
    var mainWindow = d2hGetMainWindow();    
    request = request.replace(mainWindow.searchAndInSpaces, " ")
        .replace(mainWindow.searchOrInSpaces, " ")
        .replace(mainWindow.searchNotInSpaces, " ");
    var words = request.split(/\s+/g);
    for (var j = 0; j < words.length; j++)
    {
        if (!words[j])
            continue;
        arr[i++] = words[j];
    }
    return arr;
}

function walkNodes(node, depth, nodeproc)
{
    var check_time_nodes = 1000;
    var replace_weight = 20;
    var max_highlight_time = 2000;

    var regSkipTag = /^(script|style|textarea)/i;
    var count = 0;
    var timeBegin = new Date();
    while (node && depth > 0)
    {
        count++;
        if (count > check_time_nodes)
        {
            count = 0;
            if (new Date() - timeBegin > max_highlight_time)
                return;
                
        }

        if (node.nodeType == 1)
        {
            if (!regSkipTag.test(node.tagName) && node.childNodes.length > 0)
            {
                node = node.childNodes[0];
                depth ++;
                continue;
            }
        }
        else if (node.nodeType == 3)
        {
            var new_node = nodeproc(node);
            
            if (node != new_node)
            {
                node = new_node;
                count += replace_weight;
            }
        }

        if (node.nextSibling)
        {
            node = node.nextSibling;
        }
        else
        {
            while (depth > 0)
            {
                node = node.parentNode;
                depth--;
                if (node.nextSibling)
                {
                    node = node.nextSibling;
                    break;
                }
            }
        }
    }
}

function d2hElementInContainer(elem, container)
{
    do
    {
        if (elem == container)
            return true;
    }
    while ((elem = d2hGetParentElement(elem)) != null)
    return false;
}
function cancelEvent(evt)
{
    if (!evt)
        return;
    if (evt.preventDefault)
        evt.preventDefault();
    else
        evt.returnValue = false;
}

function d2hLoadFavorites()
{
    var cookie, favorites, pair, title, url, i;
    cookie = d2hGetCookie("Favorites");
    if (!cookie)
        return ;
    favorites = new Array();
    favorites = cookie.split("||");
    if (favorites)
    {
        for (i = 0; i < favorites.length; i++)
        {
            pair = favorites[i].split("|");
            if (!pair)
                continue;
            title = pair[0];
            url = pair[1];
        }
    }
    return favorites;
}

function d2hAddToFavorites(title, url)
{
    var cookie, favorites, pair, title, url, expires, i;
    cookie = d2hGetCookie("Favorites");
    if (!cookie)
        cookie = "";
    favorites = new Array();
    favorites = cookie.split("||");
    if (favorites)
    {
        for (i = 0; i < favorites.length; i++)
        {
            pair = favorites[i].split("|");
            if (pair && title == pair[0])
                return
        }
    }
    if (cookie)
        cookie += "||";
    else
        cookie = "";
    cookie += title + "|" + url;
	expires = new Date();
	expires = new Date(expires.getTime() + (1000 * 60 * 60 * 24 * 365));
    d2hSetCookie("Favorites", cookie, null, expires.toGMTString(), null, null);
}

function d2hRemoveFromFavorites(title)
{
    var cookie, start, end, expires;
    title = d2hReplaceAll(title, "|", "'");
    cookie = d2hGetCookie("Favorites");
    if (!cookie)
        return;
    start = cookie.indexOf("||" + title + "|");
    if (start == -1)
    {
        if (cookie.indexOf(title + "|") == 0)
            start = 0;
        else
            return;
    }
    
    end = cookie.indexOf("|", start + 3 + title.length);
    if (end == -1)
        end = cookie.length;
    cookie = cookie.substring(0, start) + cookie.substring(end);
    if (cookie.indexOf("||") == 0)
        cookie = cookie.substring(2);
	expires = new Date();
	expires = new Date(expires.getTime() + (1000 * 60 * 60 * 24 * 365));
    d2hSetCookie("Favorites", cookie, null, expires.toGMTString(), null, null);
}

function d2hSetCookie(name, value, path, expires, domain, secure) 
{
  var wnd = d2hGetMainWindow();
  var curCookie = name + "=" + escape(value) +
    ((expires) ? "; expires=" + expires : "") +
    ((path) ? "; path=" + path : "; path=/") +
    ((domain) ? "; domain=" + domain : "") +
    ((secure) ? "; secure" : "");
  wnd.document.cookie = curCookie;
}

function d2hGetCookie(name) 
{
    var wnd = d2hGetMainWindow();
  var prefix = name + "=";
  var cookieStartIndex = wnd.document.cookie.indexOf(prefix);
  if(cookieStartIndex == -1) return null;
  var cookieEndIndex = wnd.document.cookie.indexOf(";", cookieStartIndex + prefix.length);
  if(cookieEndIndex == -1) cookieEndIndex = wnd.document.cookie.length;
  return unescape(wnd.document.cookie.substring(cookieStartIndex + prefix.length, cookieEndIndex));
}

function d2hDelCookie(name, path, domain) 
{
  if(getCookie(name)) {
    document.cookie = name + "=" + 
    ((path) ? "; path=" + path : "; path=/") +
    ((domain) ? "; domain=" + domain : "") +
    "; expires=Thu, 01-Jan-70 00:00:01 GMT";
  }
}

function d2hReplaceAll(string, what, to)
{
    var res = string.slice(0); //copy string to result
    if (what == to)
        return res;
    var whatLen = what.length;
    var toLen = to.length;
    var index = res.indexOf(what);
    while (index >= 0)
    {
        res = res.slice(0, index) + to + res.slice(index + whatLen);
        index = res.indexOf(what, index + toLen);
    }
    return res;
}

/////////////////////////////////////////////////////////////////////
//
//      Navigator functions
//
/////////////////////////////////////////////////////////////////////

//initializes prev/next button state; called from topic page
function d2hSetNavigatorState(prev, next)
{
    var elemPrev = d2hFindButtonByID('D2HPrevious');
    var elemNext = d2hFindButtonByID('D2HNext');
    if (elemPrev)
        d2hSetButtonState(elemPrev, prev ? d2hNormalButton : d2hPassiveButton);
    if (elemNext)
        d2hSetButtonState(elemNext, next ? d2hNormalButton : d2hPassiveButton);
}

//finds a button in three toolbars
function d2hFindButtonByID(id)
{
    var frames = ["nav_top", "nav_bottom", "right"];
    for (i = 0; i < frames.length; i++) 
    {
        var doc = getDocumentByFrameNameOrCurrentDocument(frames[i]);
        if (doc)
        {
            var elem = getElemById(doc, id);
            if (elem)
                return elem;
        }
    }
    return null;
}

//returns SPAN element inside a button
function d2hGetButtonTextElem(cell)
{
    if (cell && cell.nodeType == 1)
    {
        if (cell.tagName.toLowerCase() != "span")
        {
            var elem = cell.firstChild;
            while (elem != null)
            {
                var res = d2hGetButtonTextElem(elem);
                if (res)
                    return res;
                elem = elem.nextSibling;
            }
            return null;
        }
        else
            return cell;
    }
    else
        return null;
}

//initializes navigator buttons
function d2hOnLoadToolbar(evt)
{
    var navigator = getElemById(document, "tblNavigator");
    if (navigator != null)
    {
        d2hMaybeCorrectDivWidth(navigator);
        var anchors = navigator.getElementsByTagName("a");
        for (var i = 0; i < anchors.length; i++)
            d2hInitButton(anchors[i]);
    }
}

//fixes an IE bug: if div's margin-left is set, div's right side goes beyond the screen limit; fixed by setting width to 100%
function d2hMaybeCorrectDivWidth(elem, id)
{
    if (!isIE())
        return;
    if (!elem)
        elem = getElemById(document, id);    
    if (!elem)
        return;
    var div = d2hGetParentByTagName(elem, "div");
    if (div != null)
        div.style.width = "100%";    
}

//initializes a button
function d2hInitButton(elem)
{
    var button = d2hGetParentElement(elem);
    var caption = d2hGetAttribute(button, "caption");
    if (caption || caption == "")
    {
        if (caption)
            d2hRegisterCaption(button, caption);
        button.style.cursor = "default";
    }
    else
    {
        elem.hideFocus = USE_SECTION_508 != "yes";
        elem.style.textDecoration = "none";
        elem.style.padding = "1px 3px 1px 3px";
        elem.onfocus = d2hSButtonMOver;
        elem.onblur = d2hSButtonMOut;
        var id = button.id;
        button.onfocus = d2hSButtonMOver;
        button.onblur = d2hSButtonMOut;
        button.onmouseover = d2hSButtonMOver;
        button.onmouseout = d2hSButtonMOut;
        button.onmousedown = d2hSButtonMDown;
        if (!d2hIsSwitchCommand(id))
            button.onmouseup = d2hSButtonMUp;
    }
    d2hSetButtonImageProperties(button);
    var mode = (d2hIsSwitchCommand(id) && d2hIsButtonSelected(id)) ? d2hSelectedButton : d2hNormalButton;
    var doc = d2hGetOwnerDocument(elem);
    if (doc == null)
        return;
    var wnd = getWindow(doc);
    if (wnd == null)
        return;
    var textElem = d2hGetButtonTextElem(elem);   
    if (textElem != null)
        textElem.style.verticalAlign = "middle";
    d2hSetButtonState(button, d2hGetDefaultButtonMode(button), doc, wnd, true);
    d2hUpdateCaptions();
}

//initializes button images
function d2hSetButtonImageProperties(elem)
{
    id = elem.id;
    var listImg = elem.getElementsByTagName("img");
    for (var i = 0; i < listImg.length; i++)
    {
    	var img = listImg[i];
        img.style.verticalAlign = "middle";
        img.border = "0"; 
        img.setAttribute("normalSrc", d2hGetAttribute(img, "src"));        
    }
}

//returns false if the button must not change appearance because it is passive
function d2hCanChangeButtonState(elem)
{
    if (elem == null)
        return false;
    var id = elem.id;
    if (d2hIsNavigationCommand(id) && !d2hIsNavigationButtonEnabled(elem))
       return false;
    return true;
}

//sets visual mode (normal/selected/passive/hover) for a button
function d2hSetButtonState(elem, mode, doc, wnd, initOnly)
{
    if (elem == null)
        return;
    if (!mode)
        mode = d2hGetDefaultButtonMode(elem);
    if (!doc)
        doc = d2hGetOwnerDocument(elem);
    if (doc == null)
        return;
    if (!wnd)
        var wnd = getWindow(doc);
    if (wnd == null)
        return;
    if (!wnd.USE_FLAT_BUTTONS)
    {
        if (mode == d2hHoverButton || mode == d2hSelectedButton)
            d2hSelectButton(elem, doc, wnd, mode == d2hHoverButton);
        else
            d2hUnselectButton(elem, doc, wnd);
    }
    else if (initOnly)
        d2hUnselectButton(elem, doc, wnd);
    d2hSetButtonBackground(elem, mode, wnd);
    d2hSetButtonPicture(elem, mode);    
    var textElem = d2hGetButtonTextElem(elem);
    if (textElem == null)
        return;
    var id = elem.id;
    if (mode == d2hPassiveButton)
        textElem.className = id + "Disabled";
    else if (mode == d2hHoverButton)
        textElem.className = id + "Rollover";
    else if (mode == d2hSelectedButton && !d2hIsNavigationCommand(id))
        textElem.className = id + "Selected";
    else
    {
        if (d2hIsNavigationCommand(id))
            textElem.className = id + "Enabled";
        else    
            textElem.className = id + "Unselected";
    }
}

//sets background (normal/selected/passive) for a visual mode
function d2hSetButtonBackground(elem, mode, wnd)        
{
    if (mode == d2hNormalButton || mode == d2hPassiveButton)
    {
        if (!wnd.IS_MODERN)
            elem.style.backgroundColor = "";    
        else
            elem.style.backgroundImage = "none";
    }
    else if (mode == d2hSelectedButton)
    {
        if (!wnd.IS_MODERN)
            elem.style.backgroundColor = wnd.BTN_SELECTED;    
        else
            elem.style.backgroundImage = "url('" + wnd.BTN_SELECTED + "')";
    }
    else if (mode == d2hHoverButton)
    {
        if (!wnd.IS_MODERN)
            elem.style.backgroundColor = wnd.BTN_HOVER;    
        else
            elem.style.backgroundImage = "url('" + wnd.BTN_HOVER + "')";
    }
}

//sets picture (normal/selected/passive) for a visual mode
function d2hSetButtonPicture(elem, mode)
{
    id = elem.id;
    if (mode == d2hHoverButton || mode == d2hSelectedButton && d2hIsNavigationCommand(id))
        return;
    var useNormal = mode == d2hNormalButton;
    var listImg = elem.getElementsByTagName("img");
    if (listImg.length == 0)
       return;
  	var img = listImg[0];
  	var normalSrc = d2hGetAttribute(img, "normalSrc");
  	var altSrc = d2hGetAttribute(img, "altSrc");
  	if (useNormal && normalSrc)
  	   img.src = normalSrc;
  	else if (altSrc)
  	   img.src = altSrc;
  	else if (normalSrc)
  	   img.src = normalSrc;
}    

//returns default visual mode for a button (normal, selected or passive)
function d2hGetDefaultButtonMode(elem)
{
    var id = elem.id;
    if (d2hIsSwitchCommand(id) && d2hIsButtonSelected(id)) 
        return d2hSelectedButton;
    else if (d2hIsNavigationCommand(id) && !d2hIsNavigationButtonEnabled(elem)) 
        return d2hPassiveButton;
    else    
        return d2hNormalButton;
}

//returns false if a prev/next button should not be used for the current topic
function d2hIsNavigationButtonEnabled(button)
{
    doc = getDocumentByFrameNameOrCurrentDocument("right");
    if (!doc)
        return false;
    var wnd = getWindow(doc);
    if (wnd)
        return (button.id == "D2HNext" && wnd.next) || (button.id == "D2HPrevious" && wnd.prev);
}

//returns true if the id of a toggle button is the same as the id for the active left pane
function d2hIsButtonSelected(id)
{
    var res = d2hActivePaneID() == id;
    var wnd = d2hGetMainLayoutWnd(window);
    if (wnd && res && wnd.g_Manager && wnd.g_Manager.Navigator)
        res = id != wnd.g_Manager.Navigator._prevActivePane;
    return res;
}

//returns the ID of a toggle button for the current left pane
function d2hActivePaneID()
{
    var pane = d2hactivepane();
    if (pane == "indexlayout")
        pane = "D2HIndex";
    else if (pane == "searchlayout")
        pane = "D2HSearch";
    return pane;
}

//sets visual effects for pushed/hover states
function d2hSelectButton(elem, doc, wnd, up)
{
    //todo: support modern style
    if (wnd.IS_MODERN)
        return;
    if (elem != null)
    {
        var colorTopLeft = up ? "white" : "gray";
        var colorBottomRight = up ? "gray" : "white";
        elem.style.borderLeft = colorTopLeft + " 1px solid";
        elem.style.borderTop = colorTopLeft + " 1px solid";
        elem.style.borderBottom = colorBottomRight + " 1px solid";
        elem.style.borderRight = colorBottomRight + " 1px solid";
        elem.style.padding = "0px 2px 0px 2px";
    }
}

//removes visual effects for pushed/hover states
function d2hUnselectButton(elem, doc, wnd)
{
    //todo: support modern style
    if (wnd.IS_MODERN)
    {
        elem.style.border = "none";
        return;
    }
    if (!wnd.USE_FLAT_BUTTONS)
    {
    	var navigator = getElemById(doc, "nsbanner");
        if (navigator == null)
           navigator = doc.body;
        var color = "";   
        if (navigator != null)
        {
            elem.style.border = "none";
            elem.style.padding = "1px 3px 1px 3px";
        }
        if (wnd.USE_FLAT_BUTTONS && wnd.BTN_BORDER_COLOR)
            elem.style.borderRight = wnd.BTN_BORDER_COLOR + " 1px solid";
    }        
    var table = d2hGetParentByTagName(elem, "table");
    if (table == null)
        return;
    var color = d2hGetAttribute(table, "useBorder");
    var borderType = d2hGetAttribute(table, "borderType");
    if (color != null && borderType != null && color != "")
    {
        color = color + " 1px solid";
        if (borderType == "Right")
            elem.style.borderRight = color;
        else if (borderType == "Left")
            elem.style.borderLeft = color;
        else if (borderType == "Outer")
        {
            elem.style.borderRight = color;
            if (elem.cellIndex == 0)
                elem.style.borderLeft = color;
        }
        else if (borderType == "Inner")
        {
            if (elem.cellIndex > 0)
                elem.style.borderLeft = color;
        }
    }
}

//returns true if the command is a left pane switch
function d2hIsSwitchCommand(id)
{
    return (id == "D2HContents" || id == "D2HIndex" || id == "D2HSearch" || id == "D2HFavorites");
}

//returns true if the command is prev/next
function d2hIsNavigationCommand(id)
{
    return (id == "D2HPrevious" || id == "D2HNext");
}

//returns true if the command is SyncTOC
function d2hIsSyncTOCCommand(id)
{
    return (id == "D2HSyncTOC");
}

//returns true if the command requires special processing
function d2hIsSpecialCommand(id)
{
    return (id == "D2HEmail" || id == "D2HPrint" || id == "D2HFavoritesAdd");
}

function d2hIsHideNavPaneCommand(id)
{
    return id == "D2HHideNavigationPane";
}

//returns the URL given an id of a switch command
function d2hGetSwitchCommandURL(id)
{
    if (id == "D2HContents")
        return g_ContentsURL;
    else if (id == "D2HIndex")
        return g_IndexURL;
    else if (id == "D2HSearch")
        return g_SearchURL;
    else if (id == "D2HFavorites")
        return g_FavoritesURL;
}

// switches left pane and toggles button state
function d2hSwitchPane(id)
{
    //load pane URL
    var destDoc = getDocumentByFrameNameOrCurrentDocument("left");
    if (destDoc != null)
    {
        var wnd = getWindow(destDoc);
        if (wnd != null)
        {
            var href = d2hGetSwitchCommandURL(id); 
            if (href)
            {
                if (typeof g_hubProject != "undefined" && g_hubProject.length > 0 && href[0] != '/')
                {
                    href = '/' + href;
                    href = g_hubProject + href;
                }
                wnd.location.href = d2hGetRelativePath(document, href);
            }
        }
    }
    d2hPressPaneButton(id);
}

function d2hPressPaneButton(id)
{
    var arrSwitch = ["D2HContents", "D2HIndex", "D2HSearch", "D2HFavorites"];
    var i, elem;
    for (i = 0; i < arrSwitch.length; i++)
    {
        elem = d2hFindButtonByID(arrSwitch[i]);
        if (elem != null)
        {
            d2hSetButtonState(elem, (elem.id == id) ? d2hSelectedButton : d2hNormalButton);
            if (elem.id == id)
                d2hUpdateCaptions(elem, d2hLeftPaneCaption)
        }
    }
}

// goes to the prev/next topic
function d2hNavigateTopic(id)
{
    var doc = getDocumentByFrameNameOrCurrentDocument("right");
    if (!doc)
        return;
    wnd = getWindow(doc);
    if (wnd == null)
        return;
    elem = getElemById(doc, id + "Anchor");
	if (!elem || typeof elem.href == "undefined")
        return;
	var href = elem.href;
	if (href)
		wnd.location.href = href;
    
}

function d2hSpecialCommand(id)
{
    var frm, wnd, doc;
    var doc = getDocumentByFrameNameOrCurrentDocument("right");
    wnd = getWindow(doc);
    if (id == "D2HEmail")
    {
	
        var topicURL;
        if (wnd)
            topicURL = wnd.location.href;
        else
            topicURL = location.href;
        var href = "mailto:" + d2hGetEMailAddress() + "?subject=" + d2hEncodeURIComponent(d2hGetEMailSubject(doc.title, topicURL)) + "&body=" + d2hEncodeURIComponent(topicURL);
        wnd.location.href = href;
    }
    else if (id == "D2HPrint")
    {
        if (isIE() || isOpera())
            wnd.focus();
        d2h_before_print(doc); 
        _isPrinting = true;   
        wnd.print();
        d2h_after_print(doc);
        _isPrinting = false;   
    }
    else if (id == "D2HFavoritesAdd")
    {
        var topicURL = wnd.d2hGetTopicRelativePath();
        var topicTitle = doc.title;
        var mainLayout = d2hGetMainLayoutWnd(window);
        if (!mainLayout.g_Manager)
            mainLayout.g_Manager = new Object();
        if (!mainLayout.g_Manager.Favorites)
            mainLayout.g_Manager.Favorites = new d2hFavorites(d2hGetProjectID());
        mainLayout.g_Manager.Favorites.Window = window;
        if (!mainLayout.g_Manager.Favorites.HasProjectID())
        {
            mainLayout.g_Manager.Favorites.SetProjectID(d2hGetProjectID());
            mainLayout.g_Manager.Favorites.Load();
        }
        var handler = null;
        var selhandler = null;
        var isFavoritesTabOpen = d2hactivepane() == "D2HFavorites";
        if (isFavoritesTabOpen)
        {
            doc = getDocumentByFrameNameOrCurrentDocument("left");
            if (doc != null)
            {
                wnd = getWindow(doc);
                handler = wnd.d2hFavoritesAdd;
                selhandler = wnd.d2hSelectRow;
            }
        }
        mainLayout.g_Manager.Favorites.Add(topicTitle, topicURL, handler, selhandler);
        if (!isFavoritesTabOpen)
            d2hCommand("D2HFavorites");
    }
}

//returns the active pane body id
function d2hactivepane()
{
	var id = "D2HContents";
	try
	{
	    var frms = window.parent.frames;
	    if (frms.length < 2)
		    return id;
	    var frm = frms["left"];
	    if (frm == null)
		    return id;

	    var body = frm.document.body;
	    if (body != null)		
		    id = body.id;
    }
    catch(e)
    {
    }
	return id;				
}

///////////////// button events /////////////////////

//on click, main method for executing built-in commands
function d2hCommand(id, checkButton)
{
    //check if button exists in current document
    if (checkButton && !getElemById(document, id))
        return;
    var mainLayout = d2hGetMainLayoutWnd(window);
    if (d2hIsSwitchCommand(id))
    {
        d2hSwitchPaneChanging(id);
        setTimeout("d2hSwitchPane('" + id + "')", 100);
        if (mainLayout && mainLayout.g_Manager && mainLayout.g_Manager.Navigator)
            mainLayout.g_Manager.Navigator.ShowNavigationPane();
    }
    else if (d2hIsSpecialCommand(id))
        d2hSpecialCommand(id);
    else if (d2hIsNavigationCommand(id))
        setTimeout("d2hNavigateTopic('" + id + "')", 100);
    else if (d2hIsSyncTOCCommand(id) && mainLayout && mainLayout.g_Manager && mainLayout.g_Manager.Navigator)
    {
        var leftDoc = getFrameDocument(mainLayout.g_Manager.Navigator.Left)
        if (d2hIsTOCPane(leftDoc))
            mainLayout.g_Manager.Navigator.SyncTOC(true);
        else
        {
            mainLayout.g_Manager.Navigator.NeedSyncTOC(true);
            d2hCommand("D2HContents", false);
        }
    }
    else if (d2hIsHideNavPaneCommand(id) && mainLayout)
    {
      if (mainLayout.g_Manager && mainLayout.g_Manager.Navigator)
        mainLayout.g_Manager.Navigator.HideNavigationPane();
    }
}

// on mouseover and focus
function d2hSButtonMOver(evt)	
{
    var elem = d2hGetSelectedCell(evt);
    if (!d2hCanChangeButtonState(elem))
        return;
    d2hSetButtonState(elem, d2hHoverButton);
    cancelEvent(evt);
}

// on mouseout and blur
function d2hSButtonMOut(evt)
{
    var elem = d2hGetSelectedCell(evt);
    if (!d2hCanChangeButtonState(elem))
        return;
    d2hSetButtonState(elem);
    cancelEvent(evt);
}

//on mousedown
function d2hSButtonMDown(evt)
{
    var elem = d2hGetSelectedCell(evt);
    if (!d2hCanChangeButtonState(elem))
        return;
    d2hSetButtonState(elem, d2hSelectedButton);
    cancelEvent(evt);
}

//on mouseup
function d2hSButtonMUp(evt)
{
    var elem = d2hGetSelectedCell(evt);
    if (!d2hCanChangeButtonState(elem))
        return;
    d2hSetButtonState(elem);
    d2hSetButtonState(elem, d2hHoverButton);
    cancelEvent(evt);
}

function d2hIsTOCPane(doc)
{
    return doc && doc.body && doc.body.id == "D2HContents";
}

function d2hIsTopWindow()
{
    return d2hGetMainLayoutWnd(window, true) == window;
}

function d2hIsSecondaryWindow()
{
    return window.name != "" && window.name != "right";
}

function d2hGetFullNetHelpPath()
{
    var res = d2hGetRelativePath(document, window.g_DefaultURL);
    if (typeof g_hubProject != "undefined" && g_hubProject.length > 0 && res[0] != '/')
        res = g_hubProject + '/' + res;
    return res;
}

function d2hGetTopicRelativePath()
{
    var url = location.href;
    var indx = url.indexOf('?');
    if (indx == -1)
	    indx = url.length;
    var relPart = d2hGetAttribute(document.body, "relPart");
    if (relPart == null)
        relPart = "";
    var len = relPart.split("/").length;
    if (typeof g_hubProject != "undefined" && g_hubProject.length > 0)
        len += g_hubProject.split("/").length;
    var tmp;
    for (; len > 0; len --)
    {
        tmp = url.lastIndexOf("/", indx - 1);
        if (tmp < 1)
            len = 0;
        else
            indx = tmp;
    }
    url = url.substring(indx + 1, url.length);
    return url;
}

function d2hOpenTopicInFullNetHelp()
{
    var href = d2hGetFullNetHelpPath();
    var topic = d2hGetTopicRelativePath();
    href += "?turl=" + d2hEncodeURIComponent(topic);
    location.href = href;
}

function d2hTopicPreOpen()
{
    if (d2hIsCSH() || !d2hIsTopWindow() || d2hIsSecondaryWindow())
        return;
    d2hOpenTopicInFullNetHelp();
}

function d2hEncodeURIComponent(str)
{
    try
    {
        if (encodeURIComponent)
            return encodeURIComponent(str);
    }
    catch (e)
    {}
    return escape(str);
}

function d2hDecodeURIComponent(str)
{
    try
    {
        if (decodeURIComponent)
            return decodeURIComponent(str);
    }
    catch(e)
    {}
    return unescape(str);
}

///////////////// general methods for handling variables /////////////////////

// create variable
function d2hVariable(id, text)
{
    this.id = id;
    this.text = text;
}

// add variable to top manager
function d2hRegisterCaption(elem, caption)
{
    //check if caption contains variables
    if (caption.indexOf(d2hLeftPaneCaption) < 0)
        return;
    var mainLayout = d2hGetMainLayoutWnd(window);
    if (mainLayout && !mainLayout.g_Manager)
        mainLayout.g_Manager = new Object();
    if (mainLayout && !mainLayout.g_Manager.Variables)
        mainLayout.g_Manager.Variables = new Array();
    if (mainLayout)
        mainLayout.g_Manager.Variables[mainLayout.g_Manager.Variables.length] = new d2hVariable(elem.id, caption);
}

// updape captions with variables
function d2hUpdateCaptions(elem, varName)
{
    var mainLayout = d2hGetMainLayoutWnd(window);
    if (!mainLayout || !mainLayout.g_Manager || !mainLayout.g_Manager.Variables)
        return;
    for (var i = 0; i < mainLayout.g_Manager.Variables.length; i++)
        d2hUpdateVariable(mainLayout.g_Manager.Variables[i], elem, varName);
}

// update a caption with variables
function d2hUpdateVariable(variable, sender, varName)
{
    var elem = d2hFindButtonByID(variable.id);
    if (!elem)
        return;
    var textElem = d2hGetButtonTextElem(elem);
    if (!textElem)
        return;
    var text = variable.text;
    //process variables
    text = d2hUpdateLeftPaneCaption(text, sender, varName);
    setInnerText(textElem, text);
}

///////////////// specific methods for handling variables /////////////////////

// LeftPaneCaption variable
function d2hUpdateLeftPaneCaption(text, sender, varName)
{
    var linkedElement = (varName && varName == d2hLeftPaneCaption) ? sender : d2hGetLeftPaneActiveButton();
    if (linkedElement == null)
        return;
    return d2hReplaceAll(text, d2hLeftPaneCaption, getInnerText(linkedElement));
}

// Find active toggle button by current left pane content
function d2hGetLeftPaneActiveButton()
{
    var buttonID = null;
    var destDoc = getDocumentByFrameNameOrCurrentDocument("left");
    if (destDoc != null)
    {
        var wnd = getWindow(destDoc);
        if (wnd != null)
        {
            var href = wnd.location.href;
            // check whether g_ContentsURL is undefined is necessary only for Safari, otherwise it can raise errors
            if (href && typeof(g_ContentsURL) != 'undefined')
            {
                var len = href.length;
                if (len >= g_ContentsURL.length && href.substr(len - g_ContentsURL.length) == g_ContentsURL)
                    buttonID = 'D2HContents';
                else if (len >= g_IndexURL.length && href.substr(len - g_IndexURL.length) == g_IndexURL)
                    buttonID = 'D2HIndex';
                else if (len >= g_SearchURL.length && href.substr(len - g_SearchURL.length) == g_SearchURL)
                    buttonID = 'D2HSearch';
                else if (len >= g_FavoritesURL.length && href.substr(len - g_FavoritesURL.length) == g_FavoritesURL)
                    buttonID = 'D2HFavorites';
            }
        }
    }
    if (!buttonID)
        buttonID = 'D2HContents';
    return d2hFindButtonByID(buttonID);
}

function d2hGetEMailSubject(titleVal, urlVal)
{
    var template = d2hGetEMailSubjectTemplate();
    template = d2hReplaceAll(template, d2hTopicTitle, titleVal);
    return d2hReplaceAll(template, d2hTopicURL, urlVal);
}

///////////////// methods for handling borders /////////////////////

//main method for adjusting borders
function d2hAdjustBorders(doc, mode)
{
    if (!doc)
        doc = document;
    if (isOpera() || (typeof g_TopicMargin == "undefined") || g_TopicMargin == 0)
        return;
    if (mode == d2hRemoveRight)   
        doc.body.style.marginRight = "0px";
    else if (mode == d2hRemoveLeft)   
        doc.body.style.marginLeft = "0px";
    else if (mode == d2hRestoreTopicLeft)   
        doc.body.style.marginLeft = g_TopicMargin + "px";
}

//removes left topic border if not in Opera
function d2hRemoveRightBorder()
{
    d2hAdjustBorders(document, d2hRemoveRight);
}

//removes left topic border if not in Opera
function d2hAdjustTopicBorders(doc)
{
    if (!doc)
        doc = getDocumentByFrameNameOrCurrentDocument("right");
    if (!doc)
        return;
    var wnd = getWindow(doc);
    if (!wnd)
        return;
    if (!getFrameByName("right", wnd))
        return;
    if (window.g_bCSHTopic || (typeof window.g_bCSHTopic == "undefined" && d2hIsCSH()))
        return;
	d2hAdjustBorders(doc, d2hRemoveLeft);
}

//if a classic toolbar has margins and border, increase body margins for non-IE browser to compensate for inner DIV size
function d2hCorrectToolbarBorders()
{
    if (!isIE() && (typeof HAS_MARGIN_CLASSIC_BORDER != "undefined") && HAS_MARGIN_CLASSIC_BORDER)
    {
        var marginBottom = parseInt(d2hGetCurrentStyleAttribute(document.body, "margin-bottom"));
        var marginRight = parseInt(d2hGetCurrentStyleAttribute(document.body, "margin-right"));
        document.body.style.marginBottom = (marginBottom + 2) + "px";
        document.body.style.marginRight = (marginRight + 2) + "px";
    }
}