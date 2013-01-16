var POPUP_COLOR = "LightYellow";
var POPUP_REPEAT = "no-repeat";
var POPUP_IMAGE = "";
var IMAGE_OPEN = "Images/Theme/ButtonsAndIcons/open_vista.gif";
var IMAGE_CLOSE = "Images/Theme/ButtonsAndIcons/closed_vista.gif";
var IMAGE_TOPIC = "Images/Theme/ButtonsAndIcons/topic_vista.gif";
var INDEX_SELECTED = "Images/Theme/ButtonsAndIcons/index_vista.gif";
var INDEX_UNSELECTED = "Images/Theme/ButtonsAndIcons/index_vista.gif";
var CONTENTS_SELECTED = "Images/Theme/ButtonsAndIcons/contents_vista.gif";
var CONTENTS_UNSELECTED = "Images/Theme/ButtonsAndIcons/contents_vista.gif";
var FAVORITES_SELECTED = "Images/Theme/ButtonsAndIcons/favorites_vista.gif";
var FAVORITES_UNSELECTED = "Images/Theme/ButtonsAndIcons/favorites_vista.gif";
var SEARCH_SELECTED = "Images/Theme/ButtonsAndIcons/search_vista.gif";
var SEARCH_UNSELECTED = "Images/Theme/ButtonsAndIcons/search_vista.gif";
var SYNCTOC_SELECTED = "Images/Theme/ButtonsAndIcons/sync_vista.gif";
var SYNCTOC_UNSELECTED = "Images/Theme/ButtonsAndIcons/sync_vista.gif";
var ANCHOR = "";
var CLASS_ITEMOVER = "clsMouseOver";
var CLASS_ITEMOUT = "";
var CLASS_ITEMCURSEL = "clsCurrentTocItem";
var MSG_BROWSERNOTSUPPORTED = "<h2>Search engine does not support this browser</h2><hr />Search engine requires one of the following browsers:<ul><li>Internet Explorer 6.0 or higher</li><li>Netscape 6.2.3 or higher</li><li>Firefox</li><li>Mozilla 1.2 or higher</li><li>Opera 1.54 or higher</li></ul>";
var MSG_SEARCHENGINENOTLOADED = "<h2>Search engine failed to initialize</h2><hr /> Error: Java is disabled or not installed, or Java applet cannot initialize for other reasons.<br />Client-side search requires a Java-enabled browser.</p><p>If Java is not installed on your computer and you can't switch to using server-side search, we recommend installing JRE from the Sun Microsystems web site.<br /><a href=\"http://java.sun.com/products/plugin/index.jsp\">Download JRE</a></p>";
var MSG_SEARCHENGINENOTLOADED_PATHCONTAINSNONASCII = "<h2>Search engine failed to initialize</h2><hr noshadow />Error: Path to NetHelp contains non-ASCII characters.<br />Move NetHelp to a folder with only ASCII characters in directory names.";
var MSG_ZERO_TOPICS_FOUND = "No topics found.";
var MSG_MANY_TOPICS_FOUND = "%d topics found.";
var ALT_CLOSED_BOOK_NO_TOPIC = "Closed book without topic";
var ALT_CLOSED_BOOK_TOPIC = "Closed book with topic";
var ALT_OPEN_BOOK_NO_TOPIC = "Open book without topic";
var ALT_OPEN_BOOK_TOPIC = "Open book with topic";
var ALT_TOPIC = "Topic";
var TITLE_HOT_SPOT_JUMP = "link";
var USE_SECTION_508 = "no";
var g_DefaultURL = "default.htm";
var g_TopicMargin = 7;
var g_BorderMargin = 2;

var g_ScrollNavigator = false;

var d2hDefaultExtension = ".htm";

function dhtml_nonscrolling_resize()
{
    if (!document.body.clientWidth)
        return;
    var oText= getElemById(document, "nstext");
    if (oText == null)
        return;
    var oBanner= getElemById(document, "nsbanner");
    if (oBanner != null)
    {
        d2hSetTopicTextRightIndent(oText);
        var h = 0;
        if (document.body.clientHeight > oBanner.offsetHeight)
            h = document.body.clientHeight - oBanner.offsetHeight - g_TopicMargin * 2;
        oText.style.height = h > 0 ? h : 0;
    }
    //fixes an IE bug: if div's margin-left is set, div's right side goes beyond the screen limit; fixed by setting width to 100%
    oText.style.width = "100%";

    d2hRegisterEventHandler(window, document.body, "onresize", "d2hnsresize(event);");
    d2hRegisterEventHandler(window, document.body, "onbeforeprint", "d2h_before_print();");
    d2hRegisterEventHandler(window, document.body, "onafterprint", "d2h_after_print();");
} 

function dhtml_NNscrollingStyle()
{
    var oBanner= getElemById(document, "nsbanner");
    var oText= getElemById(document, "nstext");
    
    if (oText == null)
        return;
    
    if (oBanner != null)
    {
        var frm = getFrameByName("right");
        var h;
        if (frm)
        {
            h = getDocHeight(document);
            var wnd = getWindow(document);
            var h1 = wnd.innerHeight;
            if (h1 > h)
                h = h1;
            h = h - oBanner.offsetHeight - 16;
        }
        else
            h = window.innerHeight - oBanner.offsetHeight - 16;
        h = h - g_TopicMargin * 2;    
        if (h < 0)
            h = 0;
        if (!g_ScrollNavigator && (isMozilla() || isNN()))
        {
            oText.style.height = null;
            if (oText.offsetHeight < h)    
                oText.style.height = h; 
        }
        else
            oText.style.height = h; 
    }
    try
    {
        d2hRegisterEventHandler(window, document.body, "onresize", "d2hnsresize();");
        d2hRegisterEventHandler(window, document.body, "onbeforeprint", "d2h_before_print();");
        d2hRegisterEventHandler(window, document.body, "onafterprint", "d2h_after_print();");
    }
    catch(e)
    {
    }
}

function d2hSetTopicTextRightIndent(elem)
{
    if (_needIndentation)
        elem.style.paddingRight = "20px";
}
function d2hSyncTOC(scrollByHorizontal)
{
	var frm = getFrameByName("right");
	var next = true;
	if (frm)
	{
		var doc = getFrameDocument(frm);
		if (doc && doc.location.href != "_d2hblank.htm")
			next = false;
	}
	if (next)
	{
		setTimeout("d2hSyncTOC(" + (scrollByHorizontal ? "true" : "false") + ")", 100);
		return;
	}

d2hSyncDynamicToc(scrollByHorizontal);

}

function d2hTrySyncTOC()
{
    var mainLayout = d2hGetMainLayoutWnd(window);
    if (!mainLayout || !mainLayout.g_Manager || !mainLayout.g_Manager.Navigator)
        return;
    var leftDoc = getFrameDocument(mainLayout.g_Manager.Navigator.Left)
    if (d2hIsTOCPane(leftDoc))
        mainLayout.g_Manager.Navigator.SyncTOC(false);
    else
        mainLayout.g_Manager.Navigator.NeedSyncTOC(true);
}

function d2hSwitchPaneChanging(id)
{


    var mainLayout = d2hGetMainLayoutWnd(window);
    if (id == "D2HContents" && mainLayout && mainLayout.g_Manager && mainLayout.g_Manager.Navigator)
        mainLayout.g_Manager.Navigator.NeedSyncTOC(true);


}

function d2hGetEMailAddress()
{
    return "support@mycompany.com";
}

function d2hGetEMailSubjectTemplate()
{
    return "RE: \"%TopicTitle%\"";
}

function d2hGetProjectID()
{
    return "{6D5E6BE8-C4CB-461A-BBA6-1B8FB7971C45}";
}
