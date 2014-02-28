
function popupWindow(mylink, windowname)
{
	if (!window.focus)	{
		return true;
	}
	
	var href;
	if (typeof(mylink) == 'string')	
		href = mylink;
	else 
		href = mylink.href;

	var w = window.open(href, windowname, 'width=800,height=800,scrollbars=yes');
	w.focus();
	return false;
}