var CTXT_DISPLAY_FULLHELP = 1;
var CTXT_DISPLAY_TOPICONLY = 2;

function D2H_ShowHelp(contextID, mainURL, wndName, uCommand)
{
	var indx = mainURL.lastIndexOf("\\");
	var indx1 = mainURL.lastIndexOf("/");

	if (indx1 > indx)
		indx = indx1;
	var url = "";
	if (indx > 0)
		url = mainURL.substring(0, indx+1);
	url += "_d2h_ctxt_help.htm?contextID=" + contextID + "&mode=" + ((uCommand == CTXT_DISPLAY_TOPICONLY) ? "0" : "1");
	var wnd = window.open(url, wndName);
	wnd.focus();
}