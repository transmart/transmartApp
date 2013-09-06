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
  

STATE = {
		Dragging: false,
		Target: null,
		QueryRequestCounter: 0
}

function Concept(name, key, level, tooltip, tablename, dimcode, comment, normalunits, oktousevalues, value, nodeType)
{
	this.name=name;
	this.key=key;
	this.level=level;
	this.tooltip=tooltip;
	this.tablename=tablename;
	this.dimcode=dimcode;
	this.comment=comment;
	this.normalunits=normalunits;
	this.oktousevalues=oktousevalues;
	this.value=value;
	this.nodeType = nodeType
}

function Value(mode, operator, highlowselect, lowvalue, highvalue, units)
{
	if(typeof(mode)==undefined || mode==null)
		{this.mode="novalue"} //default to novalue
	else{
		this.mode=mode;
		}
		
	if(typeof(operator)==undefined || operator==null)
		{this.operator="LT"}
	else{
		this.operator=operator;
		}
	
	if(typeof(highlowselect)==undefined || highlowselect==null)
		{this.highlowselect="N"}
	else{
		this.highlowselect=highlowselect;
		}

	if(typeof(lowvalue)==undefined || lowvalue==null)
		{this.lowvalue=""}
	else{
		this.lowvalue=lowvalue;
		}

	if(typeof(highvalue)==undefined || highvalue==null)
		{this.highvalue=""}
	else{
		this.highvalue=highvalue;
		}
		
		if(typeof(units)==undefined || units==null)
		{this.units=""}
	else{
		this.units=units;
		}
} 

function convertNodeToConcept(node)
{
	var value=new Value();
	var level=node.attributes.level;
	var name=node.text;
	var key=node.id;
	var tooltip=node.attributes.qtip;
	var tablename=node.attributes.tablename;
	var dimcode=node.attributes.dimcode;
	var comment=node.attributes.comment;
	var normalunits=node.attributes.normalunits;
	var oktousevalues=node.attributes.oktousevalues;
	
	//Each node has a type (Categorical, Continuous, High Dimensional Data) that we need to populate. For now we will use the icon class.
	var nodeType = node.attributes.iconCls
	
	if(oktousevalues=="Y"){value.mode="novalue";} //default to novalue
	
	var myConcept=new Concept(name, key, level, tooltip, tablename, dimcode, comment, normalunits, oktousevalues, value, nodeType);
	return myConcept;
}
function createPanelItemNew(panel, concept)
{
	var li=document.createElement('div'); //was li
	//convert all object attributes to element attributes so i can get them later (must be a way to keep them in object?)
	li.setAttribute('conceptname',concept.name);
	li.setAttribute('conceptid', concept.key);
	li.setAttribute('conceptlevel',concept.level);
	li.setAttribute('concepttooltip', concept.tooltip);
	li.setAttribute('concepttablename',concept.tablename);
	li.setAttribute('conceptdimcode',concept.dimcode);
	li.setAttribute('conceptcomment', concept.comment);
	li.setAttribute('normalunits',concept.normalunits);
	li.setAttribute('setvaluemode',concept.value.mode);
	li.setAttribute('setvalueoperator',concept.value.operator);
	li.setAttribute('setvaluehighlowselect',concept.value.highlowselect);
	li.setAttribute('setvaluehighvalue',concept.value.highvalue);
	li.setAttribute('setvaluelowvalue',concept.value.lowvalue);
	li.setAttribute('setvalueunits',concept.value.units);
	li.setAttribute('oktousevalues',concept.oktousevalues);
	li.setAttribute('setnodetype',concept.nodeType);
	li.className="conceptUnselected";
	
	//Create a shortname
	var splits=concept.key.split("\\");
	var shortname="";
	if(splits.length>1)
	{
	shortname="...\\"+splits[splits.length-2]+"\\"+splits[splits.length-1];
	}
	else shortname=splits[splits.length-1];
	li.setAttribute('conceptshortname',shortname);
	
	//Create a setvalue description
	var valuetext="";
	if(typeof(concept.value.mode)!="undefined")
		{
		valuetext=getSetValueText(concept.value.mode, concept.value.operator, concept.value.highlowselect, concept.value.highvalue, concept.value.lowvalue, concept.value.units);
		li.setAttribute('conceptsetvaluetext',valuetext);
		}
	else
		{
		li.setAttribute('conceptsetvaluetext','');
		}	
	//Create the node
	var text=document.createTextNode(shortname+" "+valuetext); //used to be name
	li.appendChild(text);
	panel.appendChild(li);
	Ext.get(li).addListener('click',conceptClick);
	Ext.get(li).addListener('contextmenu',conceptRightClick);
	new Ext.ToolTip({ target:li, html:concept.key, dismissDelay:10000 });
	li.concept=concept;
	//return the node
	var subset=getSubsetFromPanel(panel);
	invalidateSubset(subset);
	return li;
}
function getSubsetFromPanel(panel)
{
return panel.id.substr(16,1);
}

function createPanelItem(subset,panelNumber, level, name, key, tooltip, tablename, dimcode, comment, normalunits, oktousevalues,
	setvaluemode, setvalueoperator, setvaluehighlowselect, setvaluelowvalue, setvaluehighvalue, setvalueunits)
{
var panel=document.getElementById("queryCriteriaDiv"+subset+"_"+panelNumber);
var li=document.createElement('div'); //was li
	//convert all object attributes to element attributes so i can get them later (must be a way to keep them in object?)
	li.setAttribute('conceptname',name);
	li.setAttribute('conceptid', key);
	li.setAttribute('conceptlevel',level);
	li.setAttribute('concepttooltip', tooltip);
	li.setAttribute('concepttablename',tablename);
	li.setAttribute('conceptdimcode',dimcode);
	li.setAttribute('conceptcomment', comment);
	li.setAttribute('normalunits',normalunits);
	if(typeof(setvaluemode)!="undefined")
		li.setAttribute('setvaluemode',setvaluemode);
	if(typeof(setvalueoperator)!="undefined")
		li.setAttribute('setvalueoperator',setvalueoperator);
	if(typeof(setvaluehighlowselect)!="undefined")
		li.setAttribute('setvaluehighlowselect',setvaluehighlowselect);
	if(typeof(setvaluehighvalue)!="undefined")
		li.setAttribute('setvaluehighvalue',setvaluehighvalue);
	if(typeof(setvaluelowvalue)!="undefined")
		li.setAttribute('setvaluelowvalue',setvaluelowvalue);
	if(typeof(setvalueunits)!="undefined")
		li.setAttribute('setvalueunits',setvalueunits);
	if(typeof(oktousevalues)!="undefined")
		li.setAttribute('oktousevalues', oktousevalues);
	li.className="conceptUnselected";
	
	//Create a shortname
	var splits=tooltip.split("\\");
	var shortname="";
	if(splits.length>1)
	{
	shortname="...\\"+splits[splits.length-2]+"\\"+splits[splits.length-1];
	}
	else shortname=splits[splits.length-1];
	li.setAttribute('conceptshortname',shortname);
	
	//Create a setvalue description
	var valuetext="";
	if(typeof(setvaluemode)!="undefined")
		{
		valuetext=getSetValueText(setvaluemode, setvalueoperator, setvaluehighlowselect, setvaluehighvalue, setvaluelowvalue, setvalueunits);
		li.setAttribute('conceptsetvaluetext',valuetext);
		}
	else
		{
		li.setAttribute('conceptsetvaluetext','');
		}	
	//Create the node
	var text=document.createTextNode(shortname+" "+valuetext); //used to be name
	li.appendChild(text);
	panel.appendChild(li);
	Ext.get(li).addListener('click',conceptClick);
	Ext.get(li).addListener('contextmenu',conceptRightClick);
	new Ext.ToolTip({ target:li, html:key, dismissDelay:10000 });
	invalidateSubset(subset);
	return li;
}	






function getSetValueText(mode, operator, highlowselect, highvalue, lowvalue, units)
{
var highlowselecttext;
switch(highlowselect)
	{
	case "H":
		highlowselecttext="HIGH";
		break;
	case "L":
		highlowselecttext="LOW";
		break;
	case "N":
		highlowselecttext="NORMAL";
		break;
	}
	
var text=" ";
	if(mode=='numeric')
		{
		if(operator!='BETWEEN')
		 {
			switch (operator)
			{
			case "LT":
			  text=text+"<";
			  break
			case "LE":
			 text=text+"<=";
			  break
			case "EQ":
			  text=text+"=";
			  break
			case "GT":
			  text=text+">";
			  break
			case "GE":
			 text=text+">=";
			  break
		 	}
		text=text+lowvalue;
		}
		else 
		{
		 text=text+"between "+lowvalue+" and "+highvalue
		}
	  }
	  else if(mode=='highlow')
	  	{
	  text=text+"High/Low-"+highlowselecttext;  
	  	}
	  else 
	  	{
	  	text="";
	  	}
	return text;
}


function resetQuery()
{
	for(var s=1;s<=GLOBAL.NumOfSubsets;s++)
	{
		for(var d=1;d<=GLOBAL.NumOfQueryCriteriaGroups;d++)
		{
 		clearGroup(s,d);
		}
	}
	hideCriteriaGroups();
}
function resetSelected()
{
	for(var s=1;s<=GLOBAL.NumOfSubsets;s++)
	{
		for(var d=1;d<=GLOBAL.NumOfQueryCriteriaGroups;d++)
		{
 		clearSelected(s,d);
		}
	}
}

function clearSelected(subset, panel)
{
	var sdiv=Ext.get("queryCriteriaDiv"+subset+"_"+panel);
	for(var i=0;i<sdiv.dom.childNodes.length;i++)
	{
  		sdiv.dom.childNodes[i].className="conceptUnselected";
	}
}





function clearGroup(subset, panel)
{
	//clear button 
	var el=document.getElementById("btnExcludeGroup"+subset+"_"+panel);
	el.firstChild.nodeValue="Exclude";

	//clar the div
	var qc=Ext.get("queryCriteriaDiv"+subset+"_"+panel);
	for(var i=qc.dom.childNodes.length-1;i>=0;i--)
		{
		var child=qc.dom.childNodes[i];
		qc.dom.removeChild(child);
		}	
	//reset the class
	qc.dom.className="queryGroupInclude";
	invalidateSubset(subset);
}

function excludeGroup(btn,subset, panel)
{
var el=Ext.get("queryCriteriaDiv"+subset+"_"+panel);
var button=Ext.get("btnExcludeGroup"+subset+"_"+panel).dom;
if(el.dom.className=="queryGroupInclude")
	{
	el.dom.className="queryGroupExclude";
	button.firstChild.nodeValue="Include";
	}
	else
	{
	el.dom.className="queryGroupInclude";
	button.firstChild.nodeValue="Exclude";
	}
	invalidateSubset(subset);
}

function conceptClick(event)
{
selectConcept(this.dom);
}

function selectConcept(concept)
{
resetSelected(); //clear any selected concept in any panel in any subset
selectedConcept=concept; //select this one
selectedDiv=concept.parentNode;
selectedConcept.className="conceptSelected";
}

function conceptRightClick(event)
{
	var conceptnode=this.dom;
	selectConcept(conceptnode);
	var conceptid=this.dom.attributes.concepttooltip.nodeValue; //change to id later
	var comment=this.dom.attributes.conceptcomment.nodeValue;

	if (!this.contextMenuConcepts) {
	this.contextMenuConcepts = new Ext.menu.Menu({
	id: 'contextMenuConcepts',
	items: [{
	text: 'Delete', handler: function(){
										selectedDiv.removeChild(selectedConcept);
										invalidateSubset(getSubsetFromPanel(selectedDiv));
										
										}
	},{id: 'setvaluemenu', text: 'Set Value', handler:function(){showSetValueDialog();}},
	{
	text: 'Show Definition', handler:function(){ showConceptInfoDialog(conceptid, conceptid, comment);}
	}
	]
	}); 
	}
	var xy = event.getXY();
	this.contextMenuConcepts.showAt(xy);
	var m=Ext.getCmp('setvaluemenu');
	if(this.dom.attributes.oktousevalues.nodeValue!='Y')
		m.hide(); 
		//alert('you cant set value');
	else 
		m.show();
	return false;
}

function setValue(conceptnode, setvaluemode, setvalueoperator, setvaluehighlowselect, setvaluehighvalue, setvaluelowvalue, setvalueunits)
{
	conceptnode.setAttribute('setvaluemode',setvaluemode);
	conceptnode.setAttribute('setvalueoperator',setvalueoperator);
	conceptnode.setAttribute('setvaluehighlowselect',setvaluehighlowselect);
	conceptnode.setAttribute('setvaluehighvalue',setvaluehighvalue);
	conceptnode.setAttribute('setvaluelowvalue',setvaluelowvalue);
	conceptnode.setAttribute('setvalueunits',setvalueunits);
	var valuetext="";
	valuetext=getSetValueText(setvaluemode, setvalueoperator, setvaluehighlowselect, setvaluehighvalue, setvaluelowvalue, setvalueunits);
	conceptnode.setAttribute('conceptsetvaluetext',valuetext);
	var conceptshortname=conceptnode.getAttribute("conceptshortname");
	//alert(conceptshortname+" "+valuetext);
	Ext.get(conceptnode.id).update(conceptshortname+" "+valuetext);
	//conceptnode.update(conceptshortname+" "+valuetext);
	var subset=getSubsetFromPanel(conceptnode.parentNode);
	invalidateSubset(subset);
}

function showSetValueDialog()
{		
		var conceptnode=selectedConcept; //not dragging so selected concept is what im updating
		setvaluewin.setHeight(200); //set height back to old closed
		Ext.get("setvaluechartsPanel1").update("");
		Ext.get("setvaluechartsPanel2").update("");
        setvaluewin.show(viewport);
        var mode=conceptnode.getAttribute('setvaluemode');
        var test=document.getElementsByName("setValueMethod");
        if(mode!=null)
       		{
				setCheckedValue(test, mode)
        		setValueMethodChanged(mode);
        	}
        else //default to novalue
        {	
        	if(test.length>0)
        		{
				setCheckedValue(test, "novalue");
        		setValueMethodChanged("novalue");
        		}
        	}
        
        var highvalue=conceptnode.getAttribute('setvaluehighvalue');
        if(highvalue!=null)
        		document.getElementById("setValueHighValue").value=highvalue;
        else
        	document.getElementById("setValueHighValue").value="";
        		
        var lowvalue=conceptnode.getAttribute('setvaluelowvalue');
        var blah=document.getElementById("setValueLowValue");
        if(lowvalue!=null)
        		blah.value=lowvalue;
        else
        	blah.value="";
        		
        var units=conceptnode.getAttribute('setvalueunits');
        if(units!=null)
        		document.getElementById("setValueUnits").value=units;
        		
        var operator=conceptnode.getAttribute('setvalueoperator');
        if(operator!=null)
        		{
        		document.getElementById("setValueOperator").value=operator;
        		setValueOperatorChanged(operator);
        		}
        		
        else
        	{
        		document.getElementById("setValueOperator").value="LT";
        		setValueOperatorChanged("LT");
        		}
       
        var highlowselect=conceptnode.getAttribute('setvaluehighlowselect');
        if(highlowselect!=null)
        		document.getElementById("setValueHighLowSelect").value=highlowselect;
        		
      	var unitsinput=document.getElementById("setValueUnits");
      	var option = new Option(conceptnode.getAttribute('normalunits'),conceptnode.getAttribute('normalunits'));  
      	unitsinput.options[0]=option;   
}


function setValueDialogComplete(mode, operator, highlowselect, highvalue, lowvalue, units)
{
//alert(selectedConcept.id+" "+mode+" "+operator+" "+highlowselect+" "+highvalue+" "+lowvalue+" "+units);
var conceptnode=selectedConcept;
setValue(conceptnode, mode, operator, highlowselect, highvalue, lowvalue, units);
if(STATE.Dragging==true){
	STATE.Dragging=false;
	moveSelectedConceptFromHoldingToTarget();
	}
}

function moveSelectedConceptFromHoldingToTarget()
{
	var node=selectedConcept;
	STATE.Target.appendChild(node);
	var subset=STATE.Target.id.substr(16,1);
	invalidateSubset(subset);
	STATE.Target=null;	
}

function invalidateSubset(subset)
{
if(GLOBAL.CurrentSubsetIDs[subset]!=null) //check if its already been invalidated so i dont call again (otherwise I clear ap and grid too many times)
	{
	GLOBAL.CurrentSubsetIDs[subset]=null; //invalidate the subset
	clearAnalysisPanel();
	}
}

function clearAnalysisPanel()
{
var cleartxt="<div style='text-align:center;font:12pt arial;width:100%;height:100%;'><table style='width:100%;height:100%;'><tr><td align='center' valign='center'>Drag concepts to this panel to view a breakdown of the subset by that concept</td></tr></table></div>";
var cleartxt2="<div style='text-align:center;font:12pt arial;width:100%;height:100%;'><table style='width:100%;height:100%;'><tr><td align='center' valign='center'>Select Advanced->Haploview from the menu</td></tr></table></div>";
updateAnalysisPanel(cleartxt, false);
 var ag=Ext.getCmp("analysisGridPanel");
 ag.body.update("<div></div>");
 var aog=Ext.getCmp("analysisOtherPanel");
 if(aog) aog.body.update(cleartxt2);
 clearGrid(); 
}

function clearGrid()
{
Ext.Ajax.request(
    	    {
    	        url: pageInfo.basePath+"/chart/clearGrid",
    	        method: 'POST',                                       
    	        //success: function(result, request){showConceptDistributionHistogramComplete(result);},
    	        //failure: function(result, request){showConceptDistributionHistogramComplete(result);},
    	        timeout: '300000',
    	        params: Ext.urlEncode({charttype:"cleargrid"})
    	    });
    	    if(typeof(grid)!='undefined')
    	    { 
    	   	if(grid!=null){ 
    	    	grid.destroy();
    	    	grid=null;
    	    }
    	   }
}

function createNClusterSelector() {
	alert("Heatmap type: " + GLOBAL.HeatmapType);
	if (GLOBAL.HeatmapType == 'KMeans') {
		GLOBAL.nClusters = 2;
		var nclusters = new Ext.form.NumberField({
			allowDecimals: false,
			allowNegative: false,
			minValue: 1,
			maxValue: 100,
			name: "Number of clusters",
			value: 2,
			applyTo: 'nClusters'
		});
		nclusters.on('change', 
					 function(r) {
						GLOBAL.nClusters = r.getValue();
					 });
		nclusters.show();
		return nclusters;
	}
}

function createNClustersBox(){
	GLOBAL.nClusters = 2;
	var nclusters = new Ext.form.NumberField({
		allowDecimals: false,
		allowNegative: false,
		minValue: 1,
		maxValue: 100,
		name: "Number of clusters",
		value: 2,
		applyTo: 'nClusters',
		validator: function(value)
		{
	        if(value < 1) 
	        {
	            return 'You must choose at least 1 cluster.';
	        } 
	        else if(value > 100)
	        {
	        	return 'You must choose less than 100 clusters.';
	        }
	        else 
	        {
	            return true;
	        }
	    }
	});
	nclusters.on('change', 
			function(r) {
		GLOBAL.nClusters = r.getValue();
	});
	nclusters.show();
	
  	if (GLOBAL.HeatmapType != 'KMeans') {
		document.getElementById("divnclusters").style.display = "none";
  	}
}

function createPathwaySearchBox(searchInputEltName, divName){
  	var ajaxurl;
  	var ds;
  	var resultTpl;
  	if (GLOBAL.searchType==='native'){
  		ajaxurl=pageInfo.basePath+'/analysis/ajaxGetPathwaySearchBoxData';  
  		ds =new Ext.data.Store({
			proxy: new Ext.data.ScriptTagProxy({
				url: ajaxurl}),
			reader: new Ext.data.JsonReader({
				root: 'rows',
				id: 'name'
				},[
					{name: 'uid'},
					{name: 'source'},
					{name: 'name'},
					{name: 'type'},
			   	]
			)
		});
		// Custom rendering Template
	    resultTpl = new Ext.XTemplate(
	        '<tpl for="."><div class="search-item">',
	            '<p width="430px" style="font: 11px verdana, arial, helvetica, sans-serif;"><span>{source}></span>&nbsp;<b>{name}</b></p>',
	        '</div></tpl>'
	    );
  	}else{
  		ajaxurl=pageInfo.basePath+'/search/loadSearchAnalysis';
  		ds =new Ext.data.Store({
			proxy: new Ext.data.ScriptTagProxy({
				url: ajaxurl}),
			reader: new Ext.data.JsonReader({
				root: "rows", id: "id"},
				[
				 	{name: "id"},
				 	{name: "source"},
				 	{name: "keyword"},
				 	{name: "synonyms"},
					{name: "category"},
					{name: "display"}
				]
			)
		});
		
		// Custom rendering Template
	    resultTpl = new Ext.XTemplate(
	        '<tpl for=".">',
	    		'<div class="search-item">',
	    			'<p>',
	    				'<span class="category-{display:lowercase}">{display}&gt;{source}</span>&nbsp;',
	    				'<b>{keyword}</b>&nbsp; {synonyms}',
	    			'</p>',
	    		'</div>',
	    	'</tpl>'
	    );
  	}
    var search = new Ext.form.ComboBox({
        store: ds,
        displayField:'title',
        typeAhead: false,
        loadingText: 'Searching...',
        width: 412,
        //listWidth: 430,
        listHeight:500,
        valueField:'naturalid',
        hideTrigger:true,
        //forceSelection:true,
        allowBlank:false,
        name:'searchText',
        mode:'remote',
        tpl: resultTpl,
        minChars:1,
        applyTo: searchInputEltName,
        //renderTo: 'search',
        itemSelector: 'div.search-item',
        onSelect: function(record){ // override default onSelect to do redirect
         	//alert(record.data.naturalid);
         	//compareStepPathwaySelection.hide();
             var sp=Ext.get(searchInputEltName);
             if (GLOBAL.searchType==='native'){
	             sp.dom.value=record.data.name;
	             GLOBAL.CurrentPathway=record.data.uid;
	             GLOBAL.CurrentPathwayName=record.data.name;
    		}else{
	             sp.dom.value=record.data.keyword;
	             GLOBAL.CurrentPathway=record.data.id;
	             GLOBAL.CurrentPathwayName=record.data.keyword;
    		}
             search.collapse();
             //compareSubsets(); 	
        }
    });
    search.on('focus', function(){
    								var sp=Ext.get(searchInputEltName);
    								sp.dom.value="";	
    								}, this);

  	if (GLOBAL.HeatmapType == 'Select' || GLOBAL.HeatmapType=='PCA') {
  		//Clear the pathway variable so we don't submit a value.
  		GLOBAL.CurrentPathway = '';
  		
  		//Remove the pathway box.
		document.getElementById(divName).style.display = "none";
  	}
}

function showPathwaySearchBox(selectedListEltName, pathwayAndIdEltName, searchInputEltName, divName){
  	var ajaxurl;
  	var ds;
  	var resultTpl;
  	var extSearchItemName = divName + "-search-item";
  	
  	if (GLOBAL.searchType==='native'){
  		ajaxurl=pageInfo.basePath+'/analysis/ajaxGetPathwaySearchBoxData';  
  		ds =new Ext.data.Store({
			proxy: new Ext.data.ScriptTagProxy({
				url: ajaxurl}),
			reader: new Ext.data.JsonReader({
				root: 'rows',
				id: 'name'
				},[
					{name: 'uid'},
					{name: 'source'},
					{name: 'name'},
					{name: 'type'},
			   	]
			)
		});
		// Custom rendering Template
	    resultTpl = new Ext.XTemplate(
	        '<tpl for="."><div class="' + extSearchItemName + '">',
	            '<p width="430px" style="font: 11px verdana, arial, helvetica, sans-serif;"><span>{source}></span>&nbsp;<b>{name}</b></p>',
	        '</div></tpl>'
	    );
  	}else{
  		ajaxurl=pageInfo.basePath+'/search/loadSearchAnalysis';
  		ds =new Ext.data.Store({
			proxy: new Ext.data.ScriptTagProxy({
				url: ajaxurl}),
			reader: new Ext.data.JsonReader({
				root: "rows", id: "id"},
				[
				 	{name: "id"},
				 	{name: "source"},
				 	{name: "keyword"},
				 	{name: "synonyms"},
					{name: "category"},
					{name: "display"}
				]
			)
		});
		
		// Custom rendering Template
	    resultTpl = new Ext.XTemplate(
	        	'<tpl for=".">',
	    		'<div class="' + divName + '-search-item">',
	    			'<p>',
	    				'<span class="category-{display:lowercase}">{display}&gt;{source}</span>&nbsp;',
	    				'<b>{keyword}</b>&nbsp; {synonyms}',
	    			'</p>',
	    		'</div>',
	    	'</tpl>'
	    );
  	}
    var search = new Ext.form.ComboBox({
        store: ds,
        displayField:'title',
        typeAhead: false,
        loadingText: 'Searching...',
        width: 412,
        listHeight:500,
        valueField:'naturalid',
        hideTrigger:true,
        name:'searchText',
        mode:'remote',
        tpl: resultTpl,
        minChars:1,
        applyTo: searchInputEltName,
        itemSelector: 'div.' + divName + '-search-item',
        onSelect: function(record){ // override default onSelect to do redirect
             var sp=Ext.get(searchInputEltName);
             if (GLOBAL.searchType==='native'){
	             sp.dom.value=record.data.name;
	             GLOBAL.CurrentPathway = record.data.uid;
    		}else {
	             var selectedGeneStr = record.data.display + '> ' + record.data.keyword;
	             var selectedListElt = Ext.get(selectedListEltName);
	             var selectedListText = selectedListElt.dom.value;
	             if (selectedListText && selectedListText.length != 0) {
	            	 selectedListText = selectedListText + ", " + selectedGeneStr;
	             }
	             else {
	            	 selectedListText = selectedGeneStr;
	             }
	             selectedListElt.dom.value = selectedListText;
	             // Set the cursor at the end of the text
	             if (selectedListElt.createTextRange) {
	            	 var rnage = selectedListElt.createTextRange();
	            	 range.move("character", selectedListText.length);
	            	 range.select();
	             }
	             else if (selectedListElt.selectionStart) {
	            	 selectedListElt.focus();
	            	 selectedListElt.setSelectionRange(selectedListText.length, selectedListText.length);
	             }
	             
	             //Put the gene display || transmart search_keyword id in selectedGenesAndIdSNPViewer hidden field, separated by |||
	             var geneAndIdStr = selectedGeneStr + '||' + record.data.id;
	             var geneAndIdElt = Ext.get(pathwayAndIdEltName);
	             var geneAndIdListText = geneAndIdElt.dom.value;
	             if (geneAndIdListText && geneAndIdListText.length != 0) {
	            	 geneAndIdListText = geneAndIdListText + "|||" + geneAndIdStr;
	             }
	             else {
	            	 geneAndIdListText = geneAndIdStr;
	             }
	             geneAndIdElt.dom.value = geneAndIdListText;
	             sp.dom.value="";	// Empty the search term input field, ready for user to type in next term.
    		}
             search.collapse();
        }
    });
    search.on('focus', function(){
    								var sp=Ext.get(searchInputEltName);
    								sp.dom.value="";	
    								}, this);
}

function createPathwaySearchBoxRBM(ajaxurl, boxwidth){
	var ds =new Ext.data.Store({
		proxy: new Ext.data.ScriptTagProxy({
			url: ajaxurl}),
		reader: new Ext.data.JsonReader({
			root: 'rows',
			id: 'name'
			},[
				{name: 'uid'},
				{name: 'source'},
				{name: 'name'},
				{name: 'type'}
		   	]
		)
	});
	
	// Custom rendering Template
    var resultTpl = new Ext.XTemplate(
        '<tpl for="."><div class="search-item">',
            '<p width="430px" style="font: 11px verdana, arial, helvetica, sans-serif;"><span>{source}></span>&nbsp;<b>{name}</b></p>',
        '</div></tpl>'
    );
    
    // add selector for k

	GLOBAL.nClusters = 2;
	var nclusters = new Ext.form.NumberField({
		allowDecimals: false,
		allowNegative: false,
		minValue: 1,
		maxValue: 100,
		name: "Number of clusters",
		value: 2,
		applyTo: 'nClustersRBM'
	});
	nclusters.on('change', 
			function(r) {
		GLOBAL.nClusters = r.getValue();
	});
	nclusters.show();
    
    var search = new Ext.form.ComboBox({
        store: ds,
        displayField:'title',
        typeAhead: false,
        loadingText: 'Searching...',
        width: boxwidth,
        listHeight:500,
        valueField:'naturalid',
        hideTrigger:true,
        allowBlank:false,
        name:'searchText',
        mode:'remote',
        tpl: resultTpl,
        minChars:1,
        applyTo: 'searchPathwayRBM',
        itemSelector: 'div.search-item',
        onSelect: function(record){ // override default onSelect to do redirect
             var sp=Ext.get("searchPathwayRBM");
             sp.dom.value=record.data.name;
             GLOBAL.CurrentPathway=record.data.uid;
             search.collapse();         	
        }
    });
    search.on('focus', function()	{
    	var sp=Ext.get("searchPathwayRBM");
    	sp.dom.value="";	
    }, this);
    return search;
}

function createPlatformSearchBox(subsetId, applyToDivIdx){
	var applyToDivIdPrefix = 'platforms'; 
	var applyToDivId = applyToDivIdPrefix + applyToDivIdx;
	var ajaxurl;
	var ds;
	var resultTpl;
		ajaxurl=pageInfo.basePath+'/analysis/getCohortInformation';  
  		ds =new Ext.data.Store({
			proxy: new Ext.data.ScriptTagProxy({
				url: ajaxurl}),
			reader: new Ext.data.JsonReader({
				root: 'rows',
				id: 'platform'
				},[
					{name: 'platform'},
					{name: 'platformLabel'}
			   	]
			),
			listeners : {
        		beforeLoad : function(search) {
            		search.baseParams = {INFO_TYPE: 1, TRIAL : GLOBAL.DefaultCohortInfo.trials[subsetId-1]};
       			}
			}
		});

		// Custom rendering Template
	    resultTpl = new Ext.XTemplate(
	        '<tpl for="."><div class="search-item">',
	            '<p style="font: 11px verdana, arial, helvetica, sans-serif; width: 130px"><b>{platformLabel}</b></p>',
	        '</div></tpl>'
	    );
	    
	var onSelectFn=function(record)
	{
		var sp=Ext.get(applyToDivId);
        sp.dom.value=record.data.platformLabel;
        GLOBAL.CurrentPlatforms[subsetId-1]=record.data.platform;
        
	    var fields=['gpl'+applyToDivIdx, 'sample'+applyToDivIdx, 'tissue'+applyToDivIdx, 'timepoint'+applyToDivIdx, 'rbmpanel'+applyToDivIdx];
	    var globalValues=[GLOBAL.CurrentGpls, GLOBAL.CurrentSamples, GLOBAL.CurrentTissues, GLOBAL.CurrentTimepoints, GLOBAL.CurrentRbmpanels];
	    
        clearSelectionsOnSelect(fields, globalValues, applyToDivIdx);
        
		var ele=Ext.get('divsample'+applyToDivIdx);
		var eleGpl=Ext.get('divgpl'+applyToDivIdx);
		var eleTissue=Ext.get('divtissue'+applyToDivIdx);
		var eleRbmpanel=Ext.get('divrbmpanel'+applyToDivIdx);
		
		if((GLOBAL.CurrentPlatforms[applyToDivIdx-1]=='MRNA_AFFYMETRIX') || (GLOBAL.CurrentPlatforms[applyToDivIdx-1]=='SNP'))
		{
			ele.dom.style.display='';
			eleGpl.dom.style.display='';
			eleTissue.dom.style.display='';
			eleRbmpanel.dom.style.display='none';
		}
		else
		{
			ele.dom.style.display='none';
			eleGpl.dom.style.display='none';
			eleTissue.dom.style.display='none';
			eleRbmpanel.dom.style.display='';
		}
       
		//Toggle the High Dimensional Data elements after reseting the High Dim variable.
		if(GLOBAL.CurrentPlatforms[subsetId-1] == "SNP") GLOBAL.HighDimDataType = 'SNP'
		if(GLOBAL.CurrentPlatforms[subsetId-1] == "MRNA_AFFYMETRIX") GLOBAL.HighDimDataType = 'Gene Expression'

		toggleDataAssociationFields();
		
		this.collapse();
		
		if (!((!GLOBAL.CurrentPlatforms[0]) || (!GLOBAL.CurrentPlatforms[1]) || (GLOBAL.CurrentPlatforms[0]==GLOBAL.CurrentPlatforms[1]))){
			alert('Platforms do not match');
		}
	}
	GLOBAL.CurrentPlatforms[subsetId-1]=GLOBAL.DefaultCohortInfo.defaultPlatforms[subsetId-1];
	    createGenericSearchBox(applyToDivIdPrefix, subsetId, applyToDivIdx, ds, resultTpl, 200, 'remote', onSelectFn, 'platform', GLOBAL.DefaultCohortInfo.defaultPlatformLabels[subsetId-1]);
}

/**
 * onSelect function which provides multi-select functionality to different drop downs.
 * Also calls clearSelectionsOnSelect().
 * @param applyToDivIdPrefix
 * @param applyToDivIdx
 * @param globalArray
 * @param fields
 * @param globalValues
 * @return
 */
function createOnMultiSelectFn(subsetId, applyToDivIdPrefix, applyToDivIdx, globalArray, fields, globalValues){
	//This is the function that gets created on select.
	var onSelectFn = function(record)
	{
		//Form the name of the div.
		var applyToDivId=applyToDivIdPrefix + applyToDivIdx;
		
		//Get the div object.
		var sp=Ext.get(applyToDivId);
		
		//This is an array of values.
		var completeSelection = globalArray[subsetId-1];
		
		//This is the divs current value.
		var completeDisplaySelection = sp.dom.value;
		
		//This is the record we selected (Value).
		var selectedValue = record.data[applyToDivIdPrefix];
		
		//This is the display value of the selected item.
		var selectedDisplayValue = record.data[applyToDivIdPrefix+'Label'];
			
		//If we selected an item that isn't already in the global list, add it. Otherwise we remove that item.
		if (completeSelection.indexOf(selectedValue)==-1)
		{
			//Mark the item as selected or not using the background color.
			for (var i=0;i<this.innerList.dom.childNodes.length; i++)
			{
			  	if (this.store.data.items[i].id==selectedValue)
			  	{
			       	this.innerList.dom.childNodes[i].style.background='#DFE8F6';
			       	break;
			    }
			}
			
			//As we select items we add them to the value/display strings.
	    	if (completeSelection=='')
	    	{
	    		completeSelection=selectedValue;
	    		completeDisplaySelection=selectedDisplayValue;
	    	}else{
	    		completeSelection=completeSelection+','+selectedValue;
	    		completeDisplaySelection=completeDisplaySelection+','+selectedDisplayValue;
	    	}
	    	
		}else{
			
			//Remove the item if we are deselecting it.
			for (var i=0;i<this.innerList.dom.childNodes.length; i++){
			  	if (this.store.data.items[i].id==selectedValue){
			       	this.innerList.dom.childNodes[i].style.background='00000';
			       	break;
			       }
			}
			
			if(completeSelection.indexOf(selectedValue+',')>-1){
				completeSelection=completeSelection.replace(selectedValue+',','');
				completeDisplaySelection=completeDisplaySelection.replace(selectedDisplayValue+',','');
			}else if(completeSelection.indexOf(','+selectedValue)>-1){
				completeSelection=completeSelection.replace(','+selectedValue,'');
				completeDisplaySelection=completeDisplaySelection.replace(','+selectedDisplayValue,'');
			}else{
				completeSelection=completeSelection.replace(selectedValue,'');
				completeDisplaySelection=completeDisplaySelection.replace(selectedDisplayValue,'');
			}
			
		}
		
		sp.dom.value=completeDisplaySelection;
		globalArray[subsetId-1]=completeSelection;		
		
		clearSelectionsOnSelect(fields, globalValues, subsetId, applyToDivIdx);
	}
	return onSelectFn;
}

/**
 * To be called by onSelect functions of each drop down which wants to clear out drop downs below it.
 * @param fields
 * @param globalValues
 * @param applyToDivIdx
 * @return
 */
function clearSelectionsOnSelect(fields, globalValues, subsetId, applyToDivIdx){
	for (var i=0;i<fields.length;i++){
	    sp=Ext.get(fields[i]);
	    sp.dom.value='ALL';
	    globalValues[i][subsetId-1]='';
	}
}

/**
 * Displays calling fields only if the selected platform is MRNA,SNP or null.
 * @param applyToDivId
 * @param applyToDivIdx
 * @return
 */
function displayConditionally(applyToDivId, subsetId)
{
	if(GLOBAL.CurrentPlatforms[subsetId-1]!='MRNA_AFFYMETRIX' && GLOBAL.CurrentPlatforms[subsetId-1]!='SNP' && GLOBAL.CurrentPlatforms[subsetId-1]!=null)
	{
		var ele=Ext.get('div'+applyToDivId);
		ele.dom.style.display='none';
	}
}

/**
 * Displays calling fields only if the selected platform is RBM.
 * @param applyToDivId
 * @param applyToDivIdx
 * @return
 */
function displayWhenRBM(applyToDivId, subsetId){
	if(GLOBAL.CurrentPlatforms[subsetId-1]!='RBM'){
		var ele=Ext.get('div'+applyToDivId);
		ele.dom.style.display='none';
	}
}

function createGplSearchBox(subsetId, applyToDivIdx){
	var applyToDivIdPrefix='gpl';
	var applyToDivId=applyToDivIdPrefix + applyToDivIdx;
	var ajaxurl;
	var ds;
	var resultTpl;
	  	ajaxurl=pageInfo.basePath+'/analysis/getCohortInformation';  
  		ds =new Ext.data.Store({
			proxy: new Ext.data.ScriptTagProxy({
				url: ajaxurl}),
			reader: new Ext.data.JsonReader({
				root: 'rows',
				id: 'gpl'
				},[
					{name: 'gpl'},
					{name: 'gplLabel'}
			   	]
			),
			listeners : {
        		beforeLoad : function(search) {
            		search.baseParams = {INFO_TYPE: 5, PLATFORM : GLOBAL.CurrentPlatforms[subsetId-1], TRIAL : GLOBAL.DefaultCohortInfo.trials[subsetId-1]};
       			}
			}
		});
		
		// Custom rendering Template
	    resultTpl = new Ext.XTemplate(
	        '<tpl for="."><div class="search-item">',
	            '<p style="font: 11px verdana, arial, helvetica, sans-serif; width: 130px"><b>{gplLabel}</b></p>',
	        '</div></tpl>'
	    );
	    
	    //create onSelect handlers.
	    var fields=['sample'+applyToDivIdx, 'tissue'+applyToDivIdx, 'timepoint'+applyToDivIdx];
	    var globalValues=[GLOBAL.CurrentSamples, GLOBAL.CurrentTissues, GLOBAL.CurrentTimepoints];
	    var onSelectFn = createOnMultiSelectFn(subsetId, applyToDivIdPrefix, applyToDivIdx, GLOBAL.CurrentGpls, fields, globalValues);
	    
		GLOBAL.CurrentGpls[applyToDivIdx-1]=GLOBAL.DefaultCohortInfo.defaultGpls[applyToDivIdx-1];
	    createGenericSearchBox(applyToDivIdPrefix, subsetId, applyToDivIdx, ds, resultTpl, 200, 'remote', onSelectFn, 'gpl', GLOBAL.DefaultCohortInfo.defaultGplLabels[subsetId-1]);
	    
	   	//Display this select box only if Platform is MRNA or unselected.
	    displayConditionally(applyToDivId, subsetId);
}

function createRbmPanelSearchBox(subsetId, applyToDivIdx){
	var applyToDivIdPrefix='rbmpanel';
	var applyToDivId=applyToDivIdPrefix + applyToDivIdx;
	var ajaxurl;
	var ds;
	var resultTpl;
	  	ajaxurl=pageInfo.basePath+'/analysis/getCohortInformation';  
  		ds =new Ext.data.Store({
			proxy: new Ext.data.ScriptTagProxy({
				url: ajaxurl}),
			reader: new Ext.data.JsonReader({
				root: 'rows',
				id: 'rbmpanel'
				},[
					{name: 'rbmpanel'},
					{name: 'rbmpanelLabel'}
			   	]
			),
			listeners : {
        		beforeLoad : function(search) {
            		search.baseParams = {INFO_TYPE: 7, PLATFORM : GLOBAL.CurrentPlatforms[subsetId-1], TRIAL : GLOBAL.DefaultCohortInfo.trials[subsetId-1]};
       			}
			}
		});
		
		// Custom rendering Template
	    resultTpl = new Ext.XTemplate(
	        '<tpl for="."><div class="search-item">',
	            '<p style="font: 11px verdana, arial, helvetica, sans-serif; width: 130px"><b>{rbmpanelLabel}</b></p>',
	        '</div></tpl>'
	    );
	    
	    //create onSelect handlers.
	    var fields=['timepoint'+applyToDivIdx];
	    var globalValues=[GLOBAL.CurrentTimepoints];
	    var onSelectFn = createOnMultiSelectFn(subsetId, applyToDivIdPrefix, applyToDivIdx, GLOBAL.CurrentRbmpanels, fields, globalValues);
	    
		GLOBAL.CurrentRbmpanels[subsetId-1]=GLOBAL.DefaultCohortInfo.defaultRbmpanels[subsetId-1];
	    createGenericSearchBox(applyToDivIdPrefix, subsetId, applyToDivIdx, ds, resultTpl, 200, 'remote', onSelectFn, 'rbmpanel', GLOBAL.DefaultCohortInfo.defaultRbmpanelLabels[subsetId-1]);

	   	//Display this select box only if Platform is RBM.
	    displayWhenRBM(applyToDivId, subsetId);
}

function createTimePointsSearchBox(subsetId, applyToDivIdx){
	
	var applyToDivIdPrefix='timepoint';
	var applyToDivId=applyToDivIdPrefix + applyToDivIdx;
	var ajaxurl;
	var ds;
	var resultTpl;
	  	ajaxurl=pageInfo.basePath+'/analysis/getCohortInformation';  
  		ds =new Ext.data.Store({
			proxy: new Ext.data.ScriptTagProxy({
				url: ajaxurl}),
			reader: new Ext.data.JsonReader({
				root: 'rows',
				id: 'timepoint'
				},[
					{name: 'timepoint'},
					{name: 'timepointLabel'}
			   	]
			),
			listeners : {
        		beforeLoad : function(search) {
            		search.baseParams = {INFO_TYPE: 3, PLATFORM : GLOBAL.CurrentPlatforms[subsetId-1], TRIAL : GLOBAL.DefaultCohortInfo.trials[subsetId-1],
            		SAMPLES: GLOBAL.CurrentSamples[subsetId-1], GPL: GLOBAL.CurrentGpls[subsetId-1], TISSUE: GLOBAL.CurrentTissues[subsetId-1], RBMPANEL: GLOBAL.CurrentRbmpanels[subsetId-1]};
       			}
			}
		});
		
		// Custom rendering Template
	    resultTpl = new Ext.XTemplate(
	        '<tpl for="."><div class="search-item">',
	            '<p style="font: 11px verdana, arial, helvetica, sans-serif; width: 130px"><b>{timepointLabel}</b></p>',
	        '</div></tpl>'
	    );
	    
	    //create onSelect handlers.
	    var fields=[];
	    var globalValues=[];
	    var onSelectFn = createOnMultiSelectFn(subsetId, applyToDivIdPrefix, applyToDivIdx, GLOBAL.CurrentTimepoints, fields, globalValues);
	    
		GLOBAL.CurrentTimepoints[subsetId-1]=GLOBAL.DefaultCohortInfo.defaultTimepoints[subsetId-1];
	    createGenericSearchBox(applyToDivIdPrefix, subsetId, applyToDivIdx, ds, resultTpl, 200, 'remote', onSelectFn, 'timepoint', GLOBAL.DefaultCohortInfo.defaultTimepointLabels[subsetId-1]);
}

function createSamplesSearchBox(subsetId, applyToDivIdx){
	var applyToDivIdPrefix='sample';
	var applyToDivId=applyToDivIdPrefix + applyToDivIdx;
	var ajaxurl;
	var ds;
	var resultTpl;
	  	ajaxurl=pageInfo.basePath+'/analysis/getCohortInformation';  
  		ds =new Ext.data.Store({
			proxy: new Ext.data.ScriptTagProxy({
				url: ajaxurl}),
			reader: new Ext.data.JsonReader({
				root: 'rows',
				id: 'sample'
				},[
					{name: 'sample'},
					{name: 'sampleLabel'}
			   	]
			),
			listeners : {
        		beforeLoad : function(search) {
            		search.baseParams = {INFO_TYPE: 4, PLATFORM : GLOBAL.CurrentPlatforms[subsetId-1], GPL: GLOBAL.CurrentGpls[subsetId-1], TRIAL : GLOBAL.DefaultCohortInfo.trials[subsetId-1]};
       			}
			}
		});
		// Custom rendering Template
	    resultTpl = new Ext.XTemplate(
	        '<tpl for="."><div class="search-item">',
	            '<p style="font: 11px verdana, arial, helvetica, sans-serif; width: 130px"><b>{sampleLabel}</b></p>',
	        '</div></tpl>'
	    );
	    
	    //create onSelect handlers.
	    var fields=['tissue'+applyToDivIdx, 'timepoint'+applyToDivIdx];
	    var globalValues=[GLOBAL.CurrentTissues, GLOBAL.CurrentTimepoints];
	    var onSelectFn = createOnMultiSelectFn(subsetId, applyToDivIdPrefix, applyToDivIdx, GLOBAL.CurrentSamples, fields, globalValues);
		
		GLOBAL.CurrentSamples[subsetId-1]=GLOBAL.DefaultCohortInfo.defaultSamples[subsetId-1];
	    createGenericSearchBox(applyToDivIdPrefix, subsetId, applyToDivIdx, ds, resultTpl, 200, 'remote', onSelectFn, 'sample', GLOBAL.DefaultCohortInfo.defaultSampleLabels[subsetId-1]);
	    
	   	//Display this select box only if Platform is MRNA or unselected.
	    displayConditionally(applyToDivId, subsetId);
}

function createTissueSearchBox(subsetId, applyToDivIdx){
	var applyToDivIdPrefix='tissue';
	var applyToDivId=applyToDivIdPrefix + applyToDivIdx;
	var ajaxurl;
	var ds;
	var resultTpl;
	  	ajaxurl=pageInfo.basePath+'/analysis/getCohortInformation';  
  		ds =new Ext.data.Store({
			proxy: new Ext.data.ScriptTagProxy({
				url: ajaxurl}),
			reader: new Ext.data.JsonReader({
				root: 'rows',
				id: 'tissue'
				},[
					{name: 'tissue'},
					{name: 'tissueLabel'}
			   	]
			),
			listeners : {
        		beforeLoad : function(search) {
            		search.baseParams = {INFO_TYPE: 6, PLATFORM : GLOBAL.CurrentPlatforms[subsetId-1], GPL: GLOBAL.CurrentGpls[subsetId-1], 
            				SAMPLE: GLOBAL.CurrentSamples[subsetId-1], TRIAL : GLOBAL.DefaultCohortInfo.trials[subsetId-1]};
       			}
			}
		});
		// Custom rendering Template
	    resultTpl = new Ext.XTemplate(
	        '<tpl for="."><div class="search-item">',
	            '<p style="font: 11px verdana, arial, helvetica, sans-serif; width: 130px"><b>{tissueLabel}</b></p>',
	        '</div></tpl>'
	    );
	    
	    //create onSelect handlers.
	    var fields=['timepoint'+applyToDivIdx];
	    var globalValues=[GLOBAL.CurrentTimepoints];
	    var onSelectFn = createOnMultiSelectFn(subsetId, applyToDivIdPrefix, applyToDivIdx, GLOBAL.CurrentTissues, fields, globalValues);
	    
		GLOBAL.CurrentTissues[subsetId-1]=GLOBAL.DefaultCohortInfo.defaultTissues[subsetId-1];
	    createGenericSearchBox(applyToDivIdPrefix, subsetId, applyToDivIdx, ds, resultTpl, 200, 'remote', onSelectFn, 'tissue', GLOBAL.DefaultCohortInfo.defaultTissueLabels[subsetId-1]);
	    
	   	//Display this select box only if Platform is MRNA or unselected.
	    displayConditionally(applyToDivId, subsetId);
}

Ext.ux.TransmartComboBox = Ext.extend(Ext.form.ComboBox, {
	onLoad:function(){
		Ext.ux.TransmartComboBox.superclass.onLoad.call(this);
	    var completeSelection ='';
	    if(this.name.indexOf('timepoint')>-1){
	    	completeSelection=GLOBAL.CurrentTimepoints[this.name.substring(this.name.length-1)-1];
	    }else if(this.name.indexOf('platform')>-1){
	    	completeSelection=GLOBAL.CurrentPlatforms[this.name.substring(this.name.length-1)-1];
	    }else if(this.name.indexOf('sample')>-1){
	    	completeSelection=GLOBAL.CurrentSamples[this.name.substring(this.name.length-1)-1];
	    }else if(this.name.indexOf('gpl')>-1){
	    	completeSelection=GLOBAL.CurrentGpls[this.name.substring(this.name.length-1)-1];
	    }else if(this.name.indexOf('tissue')>-1){
	    	completeSelection=GLOBAL.CurrentTissues[this.name.substring(this.name.length-1)-1];
	    }else if(this.name.indexOf('rbmpanel')>-1){
	    	completeSelection=GLOBAL.CurrentRbmpanels[this.name.substring(this.name.length-1)-1];
	    }
	    if(completeSelection && (completeSelection!='')){
	    	var selectionsArray = completeSelection.split(',');
			for (var i=0;i<this.innerList.dom.childNodes.length; i++){
				for(var j=0;j<selectionsArray.length; j++){
				  	if (this.store.data.items[i].id==selectionsArray[j]){
				       	this.innerList.dom.childNodes[i].style.background='#DFE8F6';
				       	break;
				    }else{
				       	this.innerList.dom.childNodes[i].style.background='00000';
				    }
				}
			}
	    }
	}
});
Ext.reg("transmartcombobox", Ext.ux.TransmartComboBox);


function createGenericSearchBox(applyToDivIdPrefix, subsetId, applyToDivIdx, ds, resultTpl, boxwidth, mode_in, onSelectFn, displayField_in, value_in){
	
		if(!value_in || value_in=="") value_in = "ALL"
	
	    var search = new Ext.ux.TransmartComboBox({
        store: ds,
        displayField:displayField_in,
        typeAhead: true,
        triggerAction: 'all',
        loadingText: 'Searching...',
        width: boxwidth,
        listHeight:500,
        valueField:'naturalid',
        hideTrigger:false,
        allowBlank:true,
        name:applyToDivIdPrefix + subsetId, // Only here we add subsetId as postfix, instead of applyToDivIdx
        mode:mode_in,
        tpl: resultTpl,
        minChars:0,
        applyTo: applyToDivIdPrefix + applyToDivIdx,
        itemSelector: 'div.search-item',
        onSelect: onSelectFn,
        editable: false,
        value: value_in,
        listeners: {
        	beforequery: function(qe){
            	delete qe.combo.lastQuery;
        	}
        }
    });
}

function AlertSpecialKey(event, field){
	//alert(event.getKey());
	var key=event.getKey();
	if(key=='13' ||  key=='undefined')
		{
		event.stopPropagation();
		return false;
		}
		return true;
	}
	
function showCompareStepPathwaySelection()
{
	if(!this.compareStepPathwaySelection)
	{
		compareStepPathwaySelection = new Ext.Window({
                id: 'compareStepPathwaySelectionWindow',
                title: 'Compare Subsets-Pathway Selection',
            	layout:'fit',
                width:450,
                // height:250,
                autoHeight: true,
                closable: false,
                plain: true,
                modal: true,
                border:false,
                //autoScroll: true,
                buttons: [
                		{
                            id: 'compareStepPathwaySelectionOKButton',
                            text: 'Run Workflow',
                            handler: function(){       
                            	
                            	//If we come from the sample side we handle the code a little different.
				   				if(GLOBAL.Explorer == "SAMPLE")
			   					{
				   					finalAdvancedMenuValidationSample();
				   					return;
			   					}
				   				finalAdvancedMenuValidation();

                            }
                        },
                		{
                            id: 'dataAssociationApplyButton',
                            text: 'Apply Selections',
                            handler: function(){       
			   					applyToForm();
			   					return;
			   				}
                        },
                        {
                            text: 'Cancel',
                            handler: function() {
                            	compareStepPathwaySelection.hide();
                            }
                         }],
	            resizable: false,
	            autoLoad:
	            {
	            	url: pageInfo.basePath+'/panels/compareStepPathwaySelection.html',
	            	scripts: true,
	               	nocache:true, 
	               	discardUrl:true,
	               	method:'POST',
	               	callback: toggleDataAssociationFields
	            },
		        tools:[{
					id:'help',
					qtip:'Click for context sensitive help',
				    handler: function(event, toolEl, panel){
				    	D2H_ShowHelp('1126', helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
				    }
		        }]
            });
        }else{
        	resetCohortInfoValues();
        	toggleDataAssociationFields();
        }

		compareStepPathwaySelection.show(viewport); 

		// toggle display of "k" selector for k-means clustering
		if (document.getElementById("divnclusters") != null) {
			if (GLOBAL.HeatmapType == 'KMeans') {
				document.getElementById("divnclusters").style.display = "";
			} else {
				document.getElementById("divnclusters").style.display = "none";
			}
		} else if (document.getElementById("divnclusters1") != null) {
			if (GLOBAL.HeatmapType == 'KMeans') {
				document.getElementById("divnclusters1").style.display = "";
			} else {
				document.getElementById("divnclusters1").style.display = "none";
			}
		}
		
		// toggle display of Gene/Pathway selector
		if (document.getElementById("divpathway") != null) {
			if (GLOBAL.HeatmapType == 'Select' || GLOBAL.HeatmapType=='PCA') 
			{
				//Clear the pathway variable so we don't submit a value.
				GLOBAL.CurrentPathway = '';
				
				//Hide the pathway box.
				document.getElementById("divpathway").style.display = "none";
			} else {
				document.getElementById("divpathway").style.display = "";
			}
		} else if (document.getElementById("divpathway1") != null) {
			if (GLOBAL.HeatmapType == 'Select' || GLOBAL.HeatmapType=='PCA') {
				document.getElementById("divpathway1").style.display = "none";
			} else {
				document.getElementById("divpathway1").style.display = "";
			}
		}
}

//this function has been modified to accomodate the fields in the new panel for data-export
//There should be a better way of doing this instead of adding code for new fields like below ... what if there are new panels with similar fields :( 
function resetCohortInfoValues(){
	if (Ext.get('platforms1') && Ext.get('platforms2')) { 
		Ext.get('platforms1').dom.value=(!GLOBAL.DefaultCohortInfo.defaultPlatforms[0] || GLOBAL.DefaultCohortInfo.defaultPlatforms[0] == "" ? 'ALL' :GLOBAL.DefaultCohortInfo.defaultPlatformLabels[0]);
		Ext.get('platforms2').dom.value=(!GLOBAL.DefaultCohortInfo.defaultPlatforms[1] || GLOBAL.DefaultCohortInfo.defaultPlatforms[1] == "" ? 'ALL' :GLOBAL.DefaultCohortInfo.defaultPlatformLabels[1]);
	} else if (Ext.get('platforms3') && Ext.get('platforms4')) { 
		Ext.get('platforms3').dom.value=(!GLOBAL.DefaultCohortInfo.defaultPlatforms[0] || GLOBAL.DefaultCohortInfo.defaultPlatforms[0] == "" ? 'ALL' :GLOBAL.DefaultCohortInfo.defaultPlatformLabels[0]);
		Ext.get('platforms4').dom.value=(!GLOBAL.DefaultCohortInfo.defaultPlatforms[1] || GLOBAL.DefaultCohortInfo.defaultPlatforms[1] == "" ? 'ALL' :GLOBAL.DefaultCohortInfo.defaultPlatformLabels[1]);
	}
	GLOBAL.CurrentPlatforms[0]=GLOBAL.DefaultCohortInfo.defaultPlatforms[0];
	GLOBAL.CurrentPlatforms[1]=GLOBAL.DefaultCohortInfo.defaultPlatforms[1];

	if (Ext.get('timepoint1') && Ext.get('timepoint2')) {
		Ext.get('timepoint1').dom.value=(GLOBAL.DefaultCohortInfo.defaultTimepointLabels[0] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultTimepointLabels[0]);
		Ext.get('timepoint2').dom.value=(GLOBAL.DefaultCohortInfo.defaultTimepointLabels[1] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultTimepointLabels[1]);
	} else if (Ext.get('timepoint3') && Ext.get('timepoint4')) {
		Ext.get('timepoint3').dom.value=(GLOBAL.DefaultCohortInfo.defaultTimepointLabels[0] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultTimepointLabels[0]);
		Ext.get('timepoint4').dom.value=(GLOBAL.DefaultCohortInfo.defaultTimepointLabels[1] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultTimepointLabels[1]);
	}
	GLOBAL.CurrentTimepoints[0]=GLOBAL.DefaultCohortInfo.defaultTimepoints[0];
	GLOBAL.CurrentTimepoints[1]=GLOBAL.DefaultCohortInfo.defaultTimepoints[1];
	
	if (Ext.get('sample1') && Ext.get('sample2')) {
		Ext.get('sample1').dom.value=(GLOBAL.DefaultCohortInfo.defaultSampleLabels[0] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultSampleLabels[0]);
		Ext.get('sample2').dom.value=(GLOBAL.DefaultCohortInfo.defaultSampleLabels[1] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultSampleLabels[1]);
	} else if (Ext.get('sample3') && Ext.get('sample4')) {
		Ext.get('sample3').dom.value=(GLOBAL.DefaultCohortInfo.defaultSampleLabels[0] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultSampleLabels[0]);
		Ext.get('sample4').dom.value=(GLOBAL.DefaultCohortInfo.defaultSampleLabels[1] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultSampleLabels[1]);
	}
	GLOBAL.CurrentSamples[0]=GLOBAL.DefaultCohortInfo.defaultSamples[0];
	GLOBAL.CurrentSamples[1]=GLOBAL.DefaultCohortInfo.defaultSamples[1];
	
	if (Ext.get('gpl1') && Ext.get('gpl2')) {
		Ext.get('gpl1').dom.value=(GLOBAL.DefaultCohortInfo.defaultGplLabels[0] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultGplLabels[0]);
		Ext.get('gpl2').dom.value=(GLOBAL.DefaultCohortInfo.defaultGplLabels[1] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultGplLabels[1]);
	} else if (Ext.get('gpl3') && Ext.get('gpl4')) {
		Ext.get('gpl3').dom.value=(GLOBAL.DefaultCohortInfo.defaultGplLabels[0] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultGplLabels[0]);
		Ext.get('gpl4').dom.value=(GLOBAL.DefaultCohortInfo.defaultGplLabels[1] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultGplLabels[1]);
	}
	GLOBAL.CurrentGpls[0]=GLOBAL.DefaultCohortInfo.defaultGpls[0];
	GLOBAL.CurrentGpls[1]=GLOBAL.DefaultCohortInfo.defaultGpls[1];
	
	if (Ext.get('tissue1') && Ext.get('tissue2')) {
		Ext.get('tissue1').dom.value=(GLOBAL.DefaultCohortInfo.defaultTissueLabels[0] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultTissueLabels[0]);
		Ext.get('tissue2').dom.value=(GLOBAL.DefaultCohortInfo.defaultTissueLabels[1] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultTissueLabels[1]);
	} else if (Ext.get('tissue3') && Ext.get('tissue4')) {
		Ext.get('tissue3').dom.value=(GLOBAL.DefaultCohortInfo.defaultTissueLabels[0] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultTissueLabels[0]);
		Ext.get('tissue4').dom.value=(GLOBAL.DefaultCohortInfo.defaultTissueLabels[1] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultTissueLabels[1]);
	}
	GLOBAL.CurrentTissues[0]=GLOBAL.DefaultCohortInfo.defaultTissues[0];
	GLOBAL.CurrentTissues[1]=GLOBAL.DefaultCohortInfo.defaultTissues[1];
	
	if (Ext.get('rbmpanel1') && Ext.get('rbmpanel2')) {
		Ext.get('rbmpanel1').dom.value=(GLOBAL.DefaultCohortInfo.defaultRbmpanelLabels[0] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultRbmpanelLabels[0]);
		Ext.get('rbmpanel2').dom.value=(GLOBAL.DefaultCohortInfo.defaultRbmpanelLabels[1] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultRbmpanelLabels[1]);
	} else if (Ext.get('rbmpanel3') && Ext.get('rbmpanel4')) {
		Ext.get('rbmpanel3').dom.value=(GLOBAL.DefaultCohortInfo.defaultRbmpanelLabels[0] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultRbmpanelLabels[0]);
		Ext.get('rbmpanel4').dom.value=(GLOBAL.DefaultCohortInfo.defaultRbmpanelLabels[1] == "" ? "ALL" : GLOBAL.DefaultCohortInfo.defaultRbmpanelLabels[1]);
	}
	GLOBAL.CurrentRbmpanels[0]=GLOBAL.DefaultCohortInfo.defaultRbmpanels[0];
	GLOBAL.CurrentRbmpanels[1]=GLOBAL.DefaultCohortInfo.defaultRbmpanels[1];
	
	var ele=(Ext.get('divsample1')) ? Ext.get('divsample1') : Ext.get('divsample3');
	var eleGpl=(Ext.get('divgpl1')) ? Ext.get('divgpl1') : Ext.get('divgpl3');
	var eleTissue=(Ext.get('divtissue1')) ? Ext.get('divtissue1') : Ext.get('divtissue3');
	var eleRbmpanel=(Ext.get('divrbmpanel1')) ? Ext.get('divrbmpanel1') : Ext.get('divrbmpanel3')
	if(GLOBAL.CurrentPlatforms[0]=='MRNA_AFFYMETRIX' || GLOBAL.CurrentPlatforms[0]=='SNP' || GLOBAL.CurrentPlatforms[0]==null){
		ele.dom.style.display='';
		eleGpl.dom.style.display='';
		eleTissue.dom.style.display='';
		eleRbmpanel.dom.style.display='none'
	}else{
		ele.dom.style.display='none';
		eleGpl.dom.style.display='none';
		eleTissue.dom.style.display='none';
		eleRbmpanel.dom.style.display='';
	}
	ele=(Ext.get('divsample2')) ? Ext.get('divsample2') : Ext.get('divsample4');
	eleGpl=(Ext.get('divgpl2')) ? Ext.get('divgpl2') : Ext.get('divgpl4');
	eleTissue=(Ext.get('divtissue2')) ? Ext.get('divtissue2') : Ext.get('divtissue4');
	eleRbmpanel=(Ext.get('divrbmpanel2')) ? Ext.get('divrbmpanel2') : Ext.get('divrbmpanel4')
	if(GLOBAL.CurrentPlatforms[1]=='MRNA_AFFYMETRIX' || GLOBAL.CurrentPlatforms[1]==null){
		ele.dom.style.display='';
		eleGpl.dom.style.display='';
		eleTissue.dom.style.display='';
		eleRbmpanel.dom.style.display='none';
	}else{
		ele.dom.style.display='none';
		eleGpl.dom.style.display='none';
		eleTissue.dom.style.display='none';
		eleRbmpanel.dom.style.display='';
	}
	
	//Clear out the pathway/aggregation input.
	document.getElementById("probesAggregation").checked=false;
	document.getElementById("searchPathway").value = "";
	
}

function showCompareStepPathwaySelectionRBM()
{
if(!this.compareStepPathwaySelectionRBM)
{
	compareStepPathwaySelectionRBM = new Ext.Window({
                id: 'compareStepPathwaySelectionWindowRBM',
                title: 'Compare Subsets-Pathway Selection for RBM Platform',
            	layout:'fit',
                width:450,
                // height:250,
                autoHeight: true,
                closable: false,
                plain: true,
                modal: true,
                border:false,
                //autoScroll: true,
                buttons: [
                		{
                            id: 'compareStepPathwaySelectionOKButtonRBM',
                            text: 'OK',
                            handler: function()	{							                                                  
                            	compareStepPathwaySelectionRBM.hide();
	                            var setid1 = GLOBAL.CurrentSubsetIDs[1];
   	   							var setid2 = GLOBAL.CurrentSubsetIDs[2];
   	   							var setname1 = "subset1";
   								var setname2 = "subset2";
   								var sp=Ext.get("searchPathwayRBM");
   								if (sp.dom.disabled == true)	{
   									GLOBAL.CurrentPathway="SHOWALLANALYTES";
   								}   								
                            	showHeatMap(setid1, setname1, setid2, setname2,
   									GLOBAL.CurrentPathway, GLOBAL.CurrentDataType, 
   									GLOBAL.HeatmapType, GLOBAL.resulttype, GLOBAL.nClusters);
   								sp.dom.value = "";
   							}
                        }
                        ,{
                            text: 'Cancel',
                            handler: function() {
                            	compareStepPathwaySelectionRBM.hide();
                            	var sp=Ext.get("searchPathwayRBM");
                            	sp.dom.value = "";
                            }
                         }],
                resizable: false,
                autoLoad:
            {
               url: pageInfo.basePath+'/panels/compareStepPathwaySelectionRBM.html',
               scripts: true,
               	nocache:true, 
			discardUrl:true,
			method:'POST'
            }
            });
        }

		compareStepPathwaySelectionRBM.show(viewport); 

		// toggle display of "k" selector for k-means clustering
		if (document.getElementById("divnclustersRBM") != null) {
			if (GLOBAL.HeatmapType == 'KMeans') {
				document.getElementById("divnclustersRBM").style.display = "";
			} else {
				document.getElementById("divnclustersRBM").style.display = "none";
			}
		}
               
}

function getQuerySummary(subset)
{
var query="";
//Interate over the criteria groups
for(var i=1;i<=GLOBAL.NumOfQueryCriteriaGroups;i++)
	{
		
		var qcd=Ext.get("queryCriteriaDiv"+subset+'_'+i.toString());
		if(qcd.dom.childNodes.length>0)
		{
			var panel=getQuerySummaryPanel(qcd.dom, i);
			if(query!="" && panel!="")
			{
				query=query+"<br>AND<br>";
			}
			query=query+panel;
		}
	}
return query;
}

//takes actual dom element
function getQuerySummaryPanel(qd, number) 
{
var panel="";
//set the invert
var invert=0;
if(qd.className=="queryGroupExclude")
	invert=1;
//set the occurs (later)
var occurences=1;

if(invert==1)
	{
	panel=panel+"<b>NOT (</b>";
	}
else
	{
	panel=panel+"<b>(</b>";
	}
for(var i=0;i<qd.childNodes.length;i++)
{
	if(i>0)
		{
		panel=panel+'<br><b>OR</b><br>';
		}
	var itemel=qd.childNodes[i];
	panel=panel+getQuerySummaryItem(itemel);
}
return panel+"<b>)</b>";
}
                  

function getQuerySummaryItem(el){
 	var item=el.getAttribute("conceptdimcode")+" "+
 			getSetValueText(
        				el.getAttribute('setvaluemode'),
						el.getAttribute('setvalueoperator'),
						el.getAttribute('setvaluehighlowselect'),
						el.getAttribute('setvaluehighvalue'),
						el.getAttribute('setvaluelowvalue'),
						el.getAttribute('setvalueunits'));
return item;
}

function myNullCallback()
{
 //alert('you have hit my callback');
}

function isSubsetEmpty(subset)
{
	for(var d=1;d<=GLOBAL.NumOfQueryCriteriaGroups;d++)
	{
		var queryDiv=Ext.get("queryCriteriaDiv"+subset+'_'+d);
		if(queryDiv.dom.childNodes.length>0)
		{ return false;}	
	}
		
	return true;
}

function showConceptDistributionHistogram(){
var conceptnode=selectedConcept; 
/*var cdhwindow=Ext.getCmp('cdhwindow');
if(cdhwindow==null)
{
cdhwindow = new Ext.Window({
                id: 'cdhwindow',
                title: 'Histogram',
            	layout:'fit',
                width:250,
                height:180,
                closable: true,
     			closeAction:'hide',  
                plain: true,
                modal: false,
                border:false,
                resizable: false
            });
}
     cdhwindow.show();
     cdhwindow.el.alignTo(setvaluewin.el, "tl-bl");
     cdhwindow.body.update("");*/
     //*run the current query
     var concept_key=conceptnode.getAttribute('conceptid');
     Ext.Ajax.request(
    	    {
    	        url: pageInfo.basePath+"/chart/conceptDistribution",
    	        method: 'POST',                                       
    	        success: function(result, request){showConceptDistributionHistogramComplete(result);},
    	        failure: function(result, request){showConceptDistributionHistogramComplete(result);},
    	        timeout: '300000',
    	        params: Ext.urlEncode({charttype:"conceptdistribution",
    	        		 			   concept_key: concept_key})
    	    });   
}

function showConceptDistributionHistogramComplete(result)
{
/*Ext.getCmp("cdhwindow").body.update(result.responseText);*/
setvaluewin.setHeight(390);
Ext.get("setvaluechartsPanel1").update(result.responseText);
}


function showConceptDistributionHistogramForSubset()
{
/*var cdhswindow=Ext.getCmp('cdhswindow');
if(cdhswindow==null)
{
cdhswindow = new Ext.Window({
                id: 'cdhswindow',
                title: 'Histogram for Subset',
            	layout:'fit',
                width:250,
                height:180,
                closable: true,
     			closeAction:'hide',  
                plain: true,
                modal: false,
                border:false,
                resizable: false
            });
}
     cdhswindow.show();
     cdhswindow.el.alignTo(setvaluewin.el, "tr-br");
     cdhswindow.body.update("");*/

var conceptnode=selectedConcept; 
var concept_key=conceptnode.getAttribute('conceptid');

var subset;
if(conceptnode.parentNode.id=="hiddenDragDiv") //was dragging so get target panel
	{
	 subset=getSubsetFromPanel(STATE.Target);
	 }
else{subset=getSubsetFromPanel(conceptnode.parentNode);} //wasn't dragging so get selected panel
var result_instance_id1=GLOBAL.CurrentSubsetIDs[subset];

Ext.Ajax.request(
    	    {
    	        url: pageInfo.basePath+"/chart/conceptDistributionForSubset",
    	        method: 'POST',                                       
    	        success: function(result, request){showConceptDistributionHistogramForSubsetComplete(result);},
    	        failure: function(result, request){showConceptDistrubutionHistogramForSubsetComplete(result);},
    	        timeout: '300000',
    	        params: Ext.urlEncode({ charttype: "conceptdistributionforsubset",
    	        		  				concept_key: concept_key, 
    	        		  				result_instance_id1: result_instance_id1})
    	    }); 
}
function showConceptDistributionHistogramForSubsetComplete(result)
{
/*Ext.getCmp("cdhswindow").body.update(result.responseText);*/
setvaluewin.setHeight(390);
Ext.get("setvaluechartsPanel2").update(result.responseText);
}

function getShortNameFromKey(concept_key)
{
//Create a shortname
	var splits=concept_key.split("\\");
	var shortname="";
	if(splits.length>1)
	{
	shortname="...\\"+splits[splits.length-2]+"\\"+splits[splits.length-1];
	}
	else shortname=splits[splits.length-1];
	return shortname;
}

function getCategoryFromKey(concept_key)
{
//Create a shortname
	var splits=concept_key.split("\\");
	var category="";
	category=splits[2];
	return category;
}

function doLogin()
{
window.location.href=pageInfo.basePath;
}

function getTreeNodeFromJsonNode(concept)
{
    var Tree = Ext.tree;

    var level				=	null;
    var name				=	null;
    var tablename			=	null;
    var tooltip				=	null;
    var key					=	null;
    var dimcode				=	null;
    var newnode				=	null;
    var leaf				=	false;
    var draggable			=	true;
    var comment				=	null;
    var normalunits			=	null;
    var commentnode			=	null;
    var normalunitsnode 	= 	null;
    var oktousevaluesnode	= 	null;
    var oktousevalues		=	null;

    level				= concept.level;
    key					= concept.key;
    name				= concept.name;
    tooltip				= concept.tooltip;
    dimcode				= concept.dimensionCode;
    tablename			= concept.dimensionTableName;
    visualattributes	= concept.visualAttributes;

    comment				= ''; //XXX
    normalunits			= concept.metadata
                              ? concept.metadata.unitValues.normalUnits
                              : '';
    oktousevalues		=	concept.metadata
                              ? (concept.metadata.okToUseValues ? 'Y' : 'N')
                              : 'N'

    //We need to replace the < signs with &lt;
    name = name.replace(/</gi, "&lt;");

    var iconCls = null;
    var cls = null;
    var tcls = null;

    if (oktousevalues != "N") {
        iconCls = "valueicon";
    }


    if (visualattributes.indexOf('LEAF') != -1 ||
        visualattributes.indexOf('MULTIPLE') != -1) {
        leaf = true;
        /* otherwise false; see init */
    }
    if (visualattributes.indexOf('CONTAINER') != -1) {
        draggable = false;
        /* otherwise true; see init */
    }

    if (visualattributes.indexOf('HIGH_DIMENSIONAL') != -1) {
        iconCls = 'hleaficon';
        tcls = 'hleafclass';
    } else if (visualattributes.indexOf('EDITABLE') != -1) {
        iconCls = 'eleaficon';
        tcls = 'eleafclass';
    }


    //set whether expanded or not.
    var autoExpand = false;
    //var pathToExpand="\\\\Clinical Trials\\Clinical Trials\\C-2006-004\\Subjects\\Demographics\\Race\\";
    if (GLOBAL.PathToExpand.indexOf(key) > -1)
        autoExpand = true;

    // set the root node

    newnode = new Tree.AsyncTreeNode({
        text          : name,
        draggable     : draggable,
        leaf          : leaf,
        id            : key,
        comment       : comment,
        qtip          : tooltip,
        iconCls       : iconCls,
        cls           : tcls,
        level         : level,  //extra attribute for storing level in hierarchy access through node.attributes.level
        dimcode       : dimcode,
        tablename     : tablename,
        normalunits   : normalunits,
        oktousevalues : oktousevalues,
        expanded      : autoExpand
    });
    newnode.addListener('contextmenu', ontologyRightClick);
    return newnode;
}


function getTreeNodeFromJSON(concept)
{
		var Tree = Ext.tree;
         var level = concept.hlevel;
         //alert(concept.id);
         var key=concept.key
         var name = concept.name;
         var tooltip = concept.tooltip;
         var dimcode = concept.dimcode;
         var visualattributes = concept.visualattributes;
         var tablename = concept.tablename;
         var metadataxml=concept.metadataxml;
         var oktousevalues=metadataxml.oktousevalues;
         var normalunits=metadataxml.normalunits;
         var access=concept.access;
         var comment=concept.comment;
         // get type of node
         var nodetype = visualattributes.substr(0, 1);
         var nodestatus = visualattributes.substr(1, 1);
         // A = active I = inactive H = hidden
         if(nodetype == 'F') // folder - dragable
         {
            leaf = false;
            draggable = true;
         }
         else if(nodetype == 'C') // folder - dragable
         {
            leaf = false;
            draggable = false;
         }
         else if(nodetype == 'L' || nodetype == 'M') // leaf - dragable
         {
            leaf = true;
            draggable = true;
         }
         var newnode = new Tree.AsyncTreeNode(
         {
            text : name,
            draggable : draggable,
            leaf : leaf,
            id : key,
            qtip : tooltip,
            level : level,  // extra attribute for storing level in hierarchy access through node.attributes.level
            dimcode : dimcode,
            tablename : tablename,
            normalunits : normalunits,
            oktousevalues: oktousevalues,
            comment: comment
         }
         );
  		   newnode.addListener('contextmenu',ontologyRightClick);
  		   return newnode;
  }



function setTreeNodeSecurity(newnode, access)
{
if(access!=undefined)
  			{
  			if(access=='Locked')
  			{
  			//newnode.setText(child.text+" <b>Locked</b>");
  			newnode.attributes.access='locked';
  			newnode.disable();
  			newnode.on('beforeload', function(node){alert("Access to this node has been restricted. Please contact your administrator for access."); return false});		
  			}
  		   }
}



function getValue(node, defaultvalue)
{
 	var result=defaultvalue;
	if(node!=null && node!=undefined)
		{
		if(node.firstChild!=null && node.firstChild!=undefined)
			{
			result=node.firstChild.nodeValue;
			}
	}
return result;
}


function showInfo(url) {
	showInfoInner(url, 600, 500);
}


function showInfoInner(url, w, h)
{
	
   if( ! this.infowin)
   {
       infowin = new Ext.Window(
      {
         id : 'infowin',
         //title : title
         layout : 'fit',
         width : w,
         height : h,
         closable : false,
         plain : true,
         modal : true,
         border : true,
         autoScroll: true,
         buttons : [
         {
            text : 'Close',
            handler : function()
            {
               infowin.hide();
            }
         }
         ],
         resizable : false
      }
      );
   }

   infowin.show();
   infowin.load({
    url: pageInfo.basePath+"/"+url,
    //params: {param1: "foo", param2: "bar"}, // or a URL encoded string
    //callback: yourFunction,
    //scope: yourObject, // optional scope for the callback
    discardUrl: true,
    nocache: true,
    text: "Loading...",
    timeout: 30,
    scripts: false
});
}

	  
function hideCriteriaGroups()
{
	var qp=Ext.get("queryPanel").dom.firstChild.firstChild;
	qp.style.background="#eee";
	Ext.get("subsetdivider").dom.rowSpan="4";
for(i=GLOBAL.NumOfQueryCriteriaGroupsAtStart+1;i<=GLOBAL.NumOfQueryCriteriaGroups;i++)
	{
    var e=document.getElementById("qcr"+i);
    e.style.display="none";
	}
	
}

function showCriteriaGroup(i)
{
	var e=document.getElementById("qcr"+i);
    e.style.display="";
	if(i>3){Ext.get("subsetdivider").dom.rowSpan=i+1;}
}


