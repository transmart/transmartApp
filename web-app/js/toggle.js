function toggleFilter(e) {
	toggleVisible(e);
}

function showfilter(e) {
	var off_e = document.getElementById(e+'off');
	var on_e = document.getElementById(e+'on');
	if(on_e == null)
		on_e = document.getElementsByName(e+'on');
	var box_e = document.getElementById(e+'box');
	on_e.style.display = "none";
	box_e.style.display = "block";
	off_e.style.display = "inline";
	var result_e = document.getElementById(e+'result');
	if(result_e != null) {
		result_e.style.display="none";
	}
}

function hidefilter(e){
	var off_e = document.getElementById(e+'off');
	var on_e = document.getElementById(e+'on');
	if(on_e == null)
		on_e = document.getElementsByName(e+'on');
	var box_e = document.getElementById(e+'box');
	var result_e = document.getElementById(e+'result');
	on_e.style.display = "inline";
	box_e.style.display = "none";
	off_e.style.display = "none";
	if(result_e != null){
		result_e.style.display = "block";
	}
}

function toggleVisible(eleId) {
	var me = document.getElementById(eleId);
	if (me.style.display == "none") {
		me.style.display = "block";
	} else {
		me.style.display = "none";
	}
}

function toggleDetail(eleprefix){
	var fclose = eleprefix+"_fclose";
	var fopen = eleprefix+"_fopen";
	var detail = eleprefix+"_detail";
	toggleVisibility(fclose);
	toggleVisibility(fopen);
	toggleVisible(detail);
}

function toggleVisibility(eleId) {
	var me = document.getElementById(eleId);
	if (me.style.visibility == "hidden") {
		me.style.display = "inline";
		me.style.visibility = "visible";
	} else {
		me.style.visibility = "hidden";
		me.style.display = "none";
	}
}

function isEmptyHTML(html) {
	return html.replace(new RegExp('[ \t\r\n]+', 'g'), '').toLowerCase() == "";
}

function divIsEmpty(eleId){
	var ele = document.getElementById(eleId);

	//alert(isEmptyHTML(ele.innerHTML))
	return (isEmptyHTML(ele.innerHTML));
}

// for input pages with a select box with an 'other' option that needs an input prompt
function toggleOtherDiv(selectItem, otherDiv) {
	var otherDiv = document.getElementById(otherDiv);
	
	// hide/show other input
	if(selectItem.value==1 || selectItem.value=='other') 
		otherDiv.style.display='block';
	else 
		otherDiv.style.display='none';
}
