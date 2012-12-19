/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  

if (window.addEventListener)
{
	// Mozilla, Netscape, Firefox
	window.addEventListener("load", d2ht_LoadPage, false);
	window.addEventListener("unload", d2ht_Window_Unload, false);
}
else 
{
	// IE
	window.attachEvent("onload", d2ht_LoadPage);
	window.attachEvent("onunload", d2ht_Window_Unload);
}

// Initialize array of section states

var sectionStates = new Array();
var sectionStatesInitialized = false;
var gSecStates = new Array();

var _toggleSwitch = "d2ht_toggleSwitch";
var _collapseAllImage = "d2ht_collapseAllImage";
var _toggleAllImage = "d2ht_toggleAllImage";
var _expandAllImage = "d2ht_expandAllImage";
var _collapseAllLabel = "d2ht_collapseAllLabel";
var _expandAllLabel = "d2ht_expandAllLabel";
var _collapseImage = "d2ht_collapseImage";
var _expandImage = "d2ht_expandImage";

var _collapsibleSectionStates = "d2ht_CollapsibleSectionStates";

function d2ht_InitSectionStates()
{
	sectionStatesInitialized = true;
    
    // SectionStates has the format:
    //
    //     firstSectionCaption:state;secondSectionCaption:state;thirdSectionCaption:state; ... ;lastSectionCaption:state
    //
    // where state is either "e" (expanded) or "c" (collapsed)
	
	d2ht_LoadStateFromCookie();
    
	var imgElements = document.getElementsByName(_toggleSwitch);
	for (var i = 0; i < imgElements.length; ++i)
	{
        sectionStates[imgElements[i].id] = d2ht_GetInitialSectionState(imgElements[i]);
		
		if (sectionStates[imgElements[i].id] == "c")
			d2ht_CollapseSection(imgElements[i]);
		else
			d2ht_ExpandSection(imgElements[i]);
	}
	d2ht_SetCollapseAll();	
}

function d2ht_GetInitialSectionState(item)
{      
    if (sectionStates[item.id] == null) 
		return item.getAttribute('d2ht_state') == "c" ? "c" : "e";
	else
		return sectionStates[item.id];
}

var noReentry = false;

function d2ht_OnLoadImage(eventObj)
{
    if (noReentry) return;
    
    if (!sectionStatesInitialized) 
	    d2ht_InitSectionStates(); 
   
    var elem;
    if(document.all) elem = eventObj.srcElement;
    else elem = eventObj.target;
        
    if ((sectionStates[elem.id] == "e"))
		d2ht_ExpandSection(elem);
	else
		d2ht_CollapseSection(elem);
}

function d2ht_LoadPage()
{
    if (!sectionStatesInitialized) 
	    d2ht_InitSectionStates(); 
		
	d2ht_SetCollapseAll();
}

function d2ht_Window_Unload()
{
	d2ht_SaveStateToCookie();
}

/*	
**********
**********   Begin Expand/Collapse
**********
*/

// expand or collapse a section
function d2ht_ExpandCollapse(imageItem)
{
	if (sectionStates[imageItem.id] == "e")
		d2ht_CollapseSection(imageItem);
	else
		d2ht_ExpandSection(imageItem);
	
	d2ht_SetCollapseAll();
}

// expand or collapse all sections
function d2ht_ExpandCollapseAll(imageItem)
{
    var collapseAllImage = document.getElementById(_collapseAllImage);
    var expandAllImage = document.getElementById(_expandAllImage);
    if (imageItem == null || collapseAllImage == null || expandAllImage == null) return;
    noReentry = true; // Prevent entry to OnLoadImage
    
	var imgElements = document.getElementsByName(_toggleSwitch);
	var i;
	var collapseAll = (imageItem.src == collapseAllImage.src);
	if (collapseAll)
	{
		imageItem.src = expandAllImage.src;
		imageItem.alt = expandAllImage.alt;
		imageItem.title = expandAllImage.alt;

		for (i = 0; i < imgElements.length; ++i)
		{
			d2ht_CollapseSection(imgElements[i]);
		}
	}
	else
	{
		imageItem.src = collapseAllImage.src;
		imageItem.alt = collapseAllImage.alt;
		imageItem.title = collapseAllImage.alt;

		for (i = 0; i < imgElements.length; ++i)
		{
			d2ht_ExpandSection(imgElements[i]);
		}
	}
	d2ht_SetAllSectionStates(collapseAll);
	d2ht_SetToggleAllLabel(collapseAll);
	
	noReentry = false;
}

function d2ht_ExpandCollapseCheckKey(imageItem, eventObj)
{
	if(eventObj.keyCode == 13 && !isOpera())
		d2ht_ExpandCollapse(imageItem);
}

function d2ht_ExpandCollapseAllCheckKey(imageItem, eventObj)
{
	if(eventObj.keyCode == 13 && !isOpera())
		d2ht_ExpandCollapseAll(imageItem);
}

function d2ht_SetAllSectionStates(collapsed)
{
    for (var sectionId in sectionStates) 
        sectionStates[sectionId] = collapsed ? "c" : "e";
}

function d2ht_ExpandSection(imageItem)
{
	d2ht_SetSectionState(imageItem, "e");
}

function d2ht_CollapseSection(imageItem)
{
	d2ht_SetSectionState(imageItem, "c");
}

function d2ht_SetSectionState(imageItem, sectionState)
{
    noReentry = true; // Prevent re-entry to OnLoadImage
    try
    {
        var image = document.getElementById(sectionState == "c" ? _expandImage : _collapseImage);
		
		//FireFox 3.0 loads images asynchronously, in a sepatate thread, and the noReentry flag does not work. 
		//Set imageItem properties only if they changed, so OnLoad event does not cause infinte recursion.
		if (imageItem.src != image.src)
		{
			imageItem.src = image.src;
			imageItem.alt = image.alt;
			imageItem.title = image.alt;
		}
		
	    imageItem.parentNode.parentNode.nextSibling.style.display = sectionState == "c" ? "none" : "";
	    sectionStates[imageItem.id] = sectionState;
    }
    catch (e)
    {
    }
    noReentry = false;
}

function d2ht_AllCollapsed()
{
	var imgElements = document.getElementsByName(_toggleSwitch);
	var allCollapsed = true;
	var i;
		
	for (i = 0; i < imgElements.length; i++) 
		allCollapsed = allCollapsed && (sectionStates[imgElements[i].id] == "c");
	
	return allCollapsed;
}

function d2ht_SetCollapseAll()
{
	var imageElement = document.getElementById(_toggleAllImage);
	if (imageElement == null) 
		return;
	
	var allCollapsed = d2ht_AllCollapsed();
	var image = document.getElementById(allCollapsed ? _expandAllImage : _collapseAllImage);
	if (image == null) 
		return;
		
	imageElement.src = image.src;
	imageElement.alt = image.alt;	
	imageElement.title = image.alt;

	d2ht_SetToggleAllLabel(allCollapsed);
}

function d2ht_SetToggleAllLabel(allCollapsed)
{
	var collapseLabelElement = document.getElementById(_collapseAllLabel);
	var expandLabelElement = document.getElementById(_expandAllLabel);
	
	if (collapseLabelElement == null || expandLabelElement == null) return;
	
	collapseLabelElement.style.display = allCollapsed ? "none" : "inline";
	expandLabelElement.style.display = allCollapsed ? "inline" : "none";	
}

/*	
**********
**********   End Expand/Collapse
**********
*/

function d2ht_RemoveSpaces(value)
{
	if (value == null || value.length == 0)
		return value;
		
	var res = "";
	var i = 0;
	while (i < value.length)
	{
		if (value.charAt(i) != ' ')
			res = res + value.charAt(i);		
		++i;
	}
	return res;
}

function d2ht_LoadStateFromCookie()
{
	var i1 = document.cookie.indexOf(_collapsibleSectionStates, 0);
	if (i1 == -1)
		return;
		
	var i2 = document.cookie.indexOf(";", i1);
	if (i2 == -1)
		i2 = document.cookie.length;
	
	var states = document.cookie.substring(i1 +_collapsibleSectionStates.length + 1, i2);
	d2ht_StringToStatesArray(states);
	
	var imgElements = document.getElementsByName(_toggleSwitch);
    for (var key in gSecStates)
	{
		for (i = 0; i < imgElements.length; ++i)
		{
			if (key == d2ht_RemoveSpaces(imgElements[i].parentNode.parentNode.innerText))
			{
				sectionStates[imgElements[i].id] = gSecStates[key];
				break;
			}
		}
	}
}

function d2ht_SaveStateToCookie()
{
	var imgElements = document.getElementsByName(_toggleSwitch);
	for (var i = 0; i < imgElements.length; ++i)
		gSecStates[d2ht_RemoveSpaces(imgElements[i].parentNode.parentNode.innerText)] = sectionStates[imgElements[i].id];
		
	document.cookie = _collapsibleSectionStates + "=" + d2ht_StatesArrayToString() + "; path=/";
}

function d2ht_StatesArrayToString()
{  
	var states = "";   
    for (var key in gSecStates) 
		states += key + ":" + gSecStates[key] + "|";
	return states.substring(0, states.length - 1);
}

function d2ht_StringToStatesArray(str)
{
    var start = 0, end;
    var key;
	if (str != null && str.length != 0)
	{
        while (start < str.length)
        {
            end = str.indexOf(":", start);
            
            key = str.substring(start, end);
            
            start = end + 1;
            end = str.indexOf("|", start);
            if (end == -1) 
				end = str.length;
            gSecStates[key] = str.substring(start, end);
    	    start = end + 1;
        }
	}
}