/*************************************************************************   
* Copyright 2008-2012 Janssen Research & Development, LLC.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************/

function generatePdfFromHTML(domObjectId, pdfFileName) {
	var body = Ext.getBody();
	try {
		Ext.destroy(Ext.get('pdfForm'));
		Ext.destroy(Ext.get('iframe2'));
	} catch(e) {}
	
	var frame = body.createChild({tag:'iframe',cls:'x-hidden',id:'iframe2',name:'iframe2'});
    var form = body.createChild({tag:'form',cls:'x-hidden',id:'pdfForm',
    	action:pageInfo.basePath+'/PDF/generatePDF', method:'post', 
        target:'iframe2'
    });

    var htmlObj = Ext.getDom(domObjectId);
    var tempObj = htmlObj.cloneNode(true);
    handleSelectBoxes(tempObj.getElementsByTagName('select'));
    handleInputs(tempObj.getElementsByTagName('input'));
    
	if (tempObj != null && tempObj.innerHTML != null) {
		
		var objectToExtractFrom = tempObj
		
		var innerHtmlString = ""
		
		//IE needs some additional REGEX to get the attribute names in quotes.
		if(navigator.appName == "Microsoft Internet Explorer")
		{
			innerHtmlString = ieInnerHTML(tempObj,false)
		}
		else
		{
			innerHtmlString = tempObj.innerHTML
		}

		var htmlString = innerHtmlString.replace(/\<input([^\>]*)\>/g, "<input$1/>");
		htmlString = htmlString.replace(/\<hr([^\>]*)\>/g, "");
		htmlString = htmlString.replace(/\<br([^\>]*)\>/g, "<br$1/>");
		htmlString = htmlString.replace(/\<meta([^\>]*)\>/g, "<meta$1/>");
		htmlString = htmlString.replace(/\<link([^\>]*)\>/g, "<link$1/>");
		
		htmlString = htmlString.replace(/\<img([^\>]*)\>/g, "<img$1/>");
		
		htmlString = htmlString.replace(/\<area([^\>]*)\>/g, "<area />");
		htmlString = htmlString.replace(/&nbsp;/gi," ");
		htmlString = htmlString.replace(/subsetPanel.html/gi," ");

		//IE Needs some tags removed.
		if(navigator.appName == "Microsoft Internet Explorer")
		{
			htmlString = htmlString.replace(/\<\/input\>/gi," ");
			htmlString = htmlString.replace(/\<\/hr\>/gi," ");
			htmlString = htmlString.replace(/\<form([^\>]*)>/gi," ");
			htmlString = htmlString.replace(/\<\/form>/gi," ");
			htmlString = htmlString.replace(/\<tbody([^\>]*)>/gi," ");
			htmlString = htmlString.replace(/\<\/tbody>/gi," ");			
			htmlString = htmlString.replace(/setvaluehighvalue/gi," ");
			htmlString = htmlString.replace(/setvaluelowvalue/gi," ");
			htmlString = htmlString.replace(/setvalueunits/gi," ");
			htmlString = htmlString.replace(/conceptsetvaluetext/gi," ");
			htmlString = htmlString.replace(/normalunits\=\".*\"/gi," ");
			htmlString = htmlString.replace(/normalunits/gi," ");
			htmlString = htmlString.replace(/src\=\"http:\/\/([^<]+)\/transmart\/images\/tempImages/gi,"src=\"/transmart/images/tempImages");
		}
		
		var htmlStr = new Ext.form.Field({
	    	fieldLabel: '', id:'htmlStr', name:'htmlStr', labelSeparator: ' ', boxLabel:'', hidden: true, 
	    	value: htmlString, renderTo: form
    	});
		var filename = new Ext.form.Field({
	    	fieldLabel: '', id:'filename', name:'filename', labelSeparator: ' ', boxLabel:'', hidden: true, 
	    	value: pdfFileName, renderTo: form
    	});
		form.dom.submit();
	}
}


function getSelectedOptionHTML(element) {
    if (!(element = $(element))) return;
    var actualElement = Ext.getDom(element.id);
    var index = actualElement.selectedIndex;
    return index >= 0 ? actualElement.options[index].innerHTML : undefined;
}

function handleSelectBoxes(selectBoxes) {
	for (var i = 0; i < selectBoxes.length; i++) {
    	var selectBox = selectBoxes[i];
    	var selectedText = getSelectedOptionHTML(selectBox);
    	selectBox.insertAdjacentHTML("afterEnd", (selectedText != undefined) ? "<span>"+selectedText+"</span>" : "");
	}
    while (selectBoxes.length > 0) {
    	var selectBox = selectBoxes[0];
    	selectBox.parentNode.removeChild(selectBox);
    }
}

function handleInputs(inputs) {
	for (var i = 0; i < inputs.length; i++) {
    	var input = inputs[i];
    	var inputValue;
    	if (input.type == 'text' || input.type == 'textarea') {
    		inputValue = input.value;
    		input.insertAdjacentHTML("afterEnd", (inputValue != undefined) ? "<span>"+inputValue+"</span>" : "")
    	} else if (input.type == 'checkbox' || input.type == 'radio') {
    		inputValue = input.checked;
    		input.insertAdjacentHTML("afterEnd", (inputValue != undefined && inputValue) ? "<span>"+inputValue+"</span>" : "<span>false</span>");
    	}
	}
    while (inputs.length > 0) {
    	var input = inputs[0];
    	input.parentNode.removeChild(input);
    }
}

function ieInnerHTML(obj, convertToLowerCase) {
	 var zz = obj.innerHTML
	     ,z = zz.match(/<\/?\w+((\s+\w+(\s*=\s*(?:".*?"|'.*?'|[^'">\s]+))?)+\s*|\s*)\/?>/g);

	  if (z){
	    for (var i=0;i<z.length;i++){
	      var y
	          , zSaved = z[i]
	          , attrRE = /\=[a-zA-Z\-\.\:\[\]_\(\)\&\$\%#\@\!0-9]+[?\s+|?>]/g;
	      z[i] = z[i]
	              .replace(/(<?\w+)|(<\/?\w+)\s/,function(a){return a.toLowerCase();});
	      y = z[i].match(attrRE);//deze match

	       if (y){
	        var j = 0
	            , len = y.length
	        while(j<len){
	          var replaceRE = /(\=)([a-zA-Z\-\.\:\[\]_\(\)\&\$\%#\@\!0-9]+)?([\s+|?>])/g
	              , replacer = function(){
	                  var args = Array.prototype.slice.call(arguments);
	                  return '="'+(convertToLowerCase ? args[2].toLowerCase() : args[2])+'"'+args[3];
	                };
	          z[i] = z[i].replace(y[j],y[j].replace(replaceRE,replacer));
	          j++;
	        }
	       }
	       zz = zz.replace(zSaved,z[i]);
	     }
	   }
	  return zz;
	 }
