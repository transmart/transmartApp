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
		   var d = f.contentDocument;
		   if(d==null)
		   {
		    d = f.contentWindow.document;
		   }
		   var cv = d.getElementById("CrystalViewer");
		   if(cv == null){return}
		   if(typeof(cv) != 'undefined')
		   {
		   f.width = cv.clientWidth;
		   f.height = cv.clientHeight;
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
             var sp=Ext.get("searchUsers");
             sp.dom.value=record.data.name;
             var h=Ext.get("currentprincipalid");
             h.dom.value=record.data.uid;
             search.collapse();
            jQuery.ajax({
                url:          pageInfo.basePath + '/secureObjectAccess/listAccessForPrincipal',
                asynchronous: true,
                data: Recom.rc.serializeFormElements.call($('#accessform'),
                        ['searchtext', 'currentprincipalid', 'accesslevelid']),
                success: function (returnedData) {
                    jQuery('#permissions').html(returnedData);
                }
            });
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
             jQuery.ajax(	{	
		 			url:pageInfo.basePath+'/userGroup/searchGroupsWithoutUser/'+record.data.uid,
		 			asynchronous:true,
	 				data:{searchtext:jQuery('#searchtext').val()},
	 				success: function( returnedData ) {
	 					jQuery( '#groups' ).html( returnedData );
	 					}
	 				});             
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

Recom.rc.serializeFormElements = function(elements, form /* jquery el or undef */) {
    if (!form) {
        form = jQuery(this).closest('form');
        if (!form.length) {
            // if form is not set, search in the whole document
            form = jQuery(window.document);
        }
    }
    var data = {};
    elements.forEach(function(id) {
        var jQueryEl = form.find('#' + id);
        if (!jQueryEl.length) {
            form = form.find('[name=' +  id + ']');
            if (!jQueryEl.length) {
                console.error('Could not find element with id or name \'' +
                        id + '\' in form ' + form);
            }
        }

        data[id] = jQueryEl.val();
    });

    return jQuery.param(data, true);
};
