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
  

function getSelectedAsCommaSeparatedList(ob)
 {
 var selected = new Array();
 	for (var i = 0; i < ob.options.length; i++)
 	if (ob.options[ i ].selected)
	selected.push(ob.options[ i ].value);
	return selected.join(", ");
 }

 		function sizeToFit(f)
		{
		   var d=f.contentDocument
		   if(d==null)
		   {
		    d=f.contentWindow.document
		   }
		   var cv=d.getElementById("CrystalViewer");
		   if(cv==null){return}
		   if(typeof(cv)!='undefined')
		   {
		   f.width=cv.clientWidth;
		   f.height=cv.clientHeight;
		  }
	}

  function pleaseWait() {
  	hideBody();
    ProgressImage = document.getElementById("progress_image");
    document.getElementById("progress").style.display = "block";
    setTimeout("ProgressImage.src = ProgressImage.src",100);
    return true;
    }

   function hideBody()
   {
   document.getElementById("bodytohide").style.display="none";
   }


   function createUserSearchBox(ajaxurl, boxwidth, ivalue){
	var ds =new Ext.data.Store({
		proxy: new Ext.data.ScriptTagProxy({
			url: ajaxurl
			}),
			reader: new Ext.data.JsonReader({
			root: 'rows',
			id: 'name'
			}, [
			{name: 'uid'},
			{name: 'name'},
			{name: 'username'},
			{name: 'description'},
			{name: 'type'},
			])
			});
	 // Custom rendering Template
    var resultTpl = new Ext.XTemplate(
        '<tpl for="."><div class="search-item">',
            '<p width="430px" style="font: 11px verdana, arial, helvetica, sans-serif;">',
            	'<span>{type}></span>&nbsp;<span class="dohighlight">{name}</span> - {username}',
            '</p>',
        '</div></tpl>'
    );
    var search = new Recom.rc.ComboBox({
        store: ds,
        displayField:'title',
        typeAhead: false,
        loadingText: 'Searching...',
        width: boxwidth,
        //listWidth: 430,
        listHeight:500,
        valueField:'uid',
        hideTrigger:true,
        //forceSelection:true,
        allowBlank:false,
        name:'searchText',
        mode:'remote',
        value:ivalue,
        tpl: resultTpl,
        minChars:1,
        applyTo: 'searchUsers',
        //renderTo: 'search',
        itemSelector: 'div.search-item',
        onSelect: function(record){ // override default onSelect to do redirect
         	//alert(record.data.uid);
             var sp=Ext.get("searchUsers");
             sp.dom.value=record.data.name;
             var h=Ext.get("currentprincipalid");
             h.dom.value=record.data.uid;
             //alert(h.dom.value);
             search.collapse();
             new Ajax.Updater({success:'permissions'}, pageInfo.basePath+'/secureObjectAccess/listAccessForPrincipal/'+record.data.uid,{asynchronous:true,evalScripts:true,parameters:$('searchtext').serialize()});
             //compareSubsets();
        }
    });
    search.on('focus', function(){
    								var sp=Ext.get("searchUsers");
    								sp.dom.value="";
    								var h=Ext.get("currentprincipalid");
             						h.dom.value="";
    								}, this);
    //var sp=Ext.get("searchPathway");
   // sp.on('keypress', AlertSpecialKey, this);
    return search;
}


 function createUserSearchBox2(ajaxurl, boxwidth){
	var ds =new Ext.data.Store({
		proxy: new Ext.data.ScriptTagProxy({
			url: ajaxurl
			}),
			reader: new Ext.data.JsonReader({
			root: 'rows',
			id: 'name'
			}, [
			{name: 'uid'},
			{name: 'name'},
			{name: 'username'},
			{name: 'description'},
			{name: 'type'},
			])
			});
	 // Custom rendering Template
    var resultTpl = new Ext.XTemplate(
        '<tpl for="."><div class="search-item">',
            '<p width="430px" style="font: 11px verdana, arial, helvetica, sans-serif;">',
            	'<span>{type}></span>&nbsp;<span class="dohighlight">{name}</span> - {username}',
            '</p>',
        '</div></tpl>'
    );
    var search = new Recom.rc.ComboBox({
        store: ds,
        displayField:'title',
        typeAhead: false,
        loadingText: 'Searching...',
        width: boxwidth,
        //listWidth: 430,
        listHeight:500,
        valueField:'uid',
        hideTrigger:true,
        //forceSelection:true,
        allowBlank:false,
        name:'searchText',
        mode:'remote',
        tpl: resultTpl,
        minChars:1,
        applyTo: 'searchUsers',
        //renderTo: 'search',
        itemSelector: 'div.search-item',
        onSelect: function(record){ // override default onSelect to do redirect
         	//alert(record.data.uid);
             var sp=Ext.get("searchUsers");
             sp.dom.value=record.data.name;
             var h=Ext.get("currentprincipalid");
             h.dom.value=record.data.uid;
             //alert(h.dom.value);
             search.collapse();
             new Ajax.Updater({success:'groups'}, pageInfo.basePath+'/userGroup/searchGroupsWithoutUser/'+record.data.uid,{asynchronous:true,evalScripts:true,parameters:$('searchtext').serialize()});
             //compareSubsets();
        }
    });
    search.on('focus', function(){
    								var sp=Ext.get("searchUsers");
    								sp.dom.value="";
    								var h=Ext.get("currentprincipalid");
             						h.dom.value="";
    								}, this);
    //var sp=Ext.get("searchPathway");
   // sp.on('keypress', AlertSpecialKey, this);


    return search;
}

function createUserSearchBox3(ajaxurl, boxwidth){
	var ds =new Ext.data.Store({
		proxy: new Ext.data.ScriptTagProxy({
			url: ajaxurl
			}),
			reader: new Ext.data.JsonReader({
			root: 'rows',
			id: 'name'
			}, [
			{name: 'uid'},
			{name: 'name'},
			{name: 'username'},
			{name: 'description'},
			{name: 'type'},
			])
			});
	 // Custom rendering Template
    var resultTpl = new Ext.XTemplate(
        '<tpl for="."><div class="search-item">',
            '<p width="430px" style="font: 11px verdana, arial, helvetica, sans-serif;">',
            	'<span>{type}></span>&nbsp;<span class="dohighlight">{name}</span> - {username} - {clinic}',
            '</p>',
        '</div></tpl>'
    );
    var search = new Recom.rc.ComboBox({
        store: ds,
        displayField:'title',
        typeAhead: false,
        loadingText: 'Searching...',
        width: boxwidth,
        //listWidth: 430,
        listHeight:500,
        valueField:'uid',
        hideTrigger:true,
        //forceSelection:true,
        allowBlank:false,
        name:'searchText',
        mode:'remote',
        tpl: resultTpl,
        minChars:1,
        applyTo: 'searchUsers',
        //renderTo: 'search',
        itemSelector: 'div.search-item',
        onSelect: function(record){ // override default onSelect to do redirect
             var sp=Ext.get("searchUsers");
             sp.dom.value=record.data.name;
             var h=Ext.get("currentprincipalid");
             h.dom.value=record.data.uid;
             search.collapse();
        }
    });
    search.on('focus', function(){
    								var sp=Ext.get("searchUsers");
    								sp.dom.value="";
    								var h=Ext.get("currentprincipalid");
             						h.dom.value="";
    								}, this);
    return search;
}








Ext.namespace('Recom.rc');
Recom.rc.ComboBox = function(config) {

    // call parent constructor
    Recom.rc.ComboBox.superclass.constructor.call(this, config);

}; // end of Ext.ux.IconCombo constructor



Ext.extend(Recom.rc.ComboBox, Ext.form.ComboBox, {
	afterRefresh: function(){
      var i=this.view.el.dom;
	  doHighlight(i, this.store.baseParams.query, '<b>', '</b>');
	},
	initList: function(){
		Recom.rc.ComboBox.superclass.initList.call(this, arguments);
		this.mon(this.view, 'refresh', this.afterRefresh, this);
		}
    });

var originalRefresh=Ext.DataView.prototype.refresh;
Ext.override(Ext.DataView, {

	refresh: function(){
	originalRefresh.call(this, arguments);
	this.fireEvent("refresh", this);
	}

});


function doHighlight(rootelement, searchTerm, highlightStartTag, highlightEndTag)
{
	var elements=Ext.query(".dohighlight");
	for(var j=0;  j<elements.length;  j+=1)
	{
		highlightTextNodes(elements[j], searchTerm);
	}
}

function highlightTextNodes(element, searchTerm) {
	  var tempinnerHTML = element.innerHTML;
	  // Do regex replace
	  //var regex = new RegExp(">([^<]*)?("+searchTerm+")([^>]*)?<","ig");
	  var insensitive=true;
	 var regex = new RegExp('(<[^>]*>)|('+ searchTerm.replace(/([-.*+?^${}()|[\]\/\\])/g,"\\$1") +')', insensitive ? 'ig' : 'g');
	  // Inject span with class of 'highlighted termX' for google style highlighting
	  var a= tempinnerHTML.replace(regex,'<b>'+searchTerm+'</b>');
	  element.innerHTML =a;
	}

