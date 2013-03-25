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
  


Ext.ux.OntologyTreeLoader = Ext.extend(Ext.tree.TreeLoader, {

requestData : function(node, callback){
        if(this.fireEvent("beforeload", this, node, callback) !== false){
        var getChildrenRequest=getONTRequestHeader()+'<ns4:get_children blob="true" max="1000" synonyms="false" hiddens="false">';
        getChildrenRequest=getChildrenRequest+"<parent>"+node.id+"</parent></ns4:getchildren>"+getONTRequestFooter();
        
            this.transId = Ext.Ajax.request({
                url: pageInfo.basePath+"/proxy?url="+GLOBAL.ONTUrl+"getChildren",
    	        method: 'POST',
    	        xmlData: getChildrenRequest,  
                success: this.handleResponse,
                failure: this.handleFailure,
                scope: this,
                argument: {callback: callback, node: node},
                timeout: '120000', //2 minutes
                params: { }
            });
        }else{
            // if the load is cancelled, make sure we notify
            // the node that we are done
            if(typeof callback == "function"){
                callback();
            }
        }
  },

processResponse:function (response, node, callback) {
 //	if(GLOBAL.Debug){alert(response.responseText)};
 	try{
 	//response.responseText.evalJSON();
 	}
 	catch(e){}
	//var nodes = this.parseXml(response);
	node.beginUpdate();
	//node.appendChild(nodes);
	this.parseXml(response, node);
	getChildConceptPatientCounts(node);
	node.endUpdate();
	if (typeof callback == "function") {
		callback(this, node);
	}
}, 

parseXml:function (response, node) {
 // shorthand
    var Tree = Ext.tree;
    
 var concept=null;
 var concepts=response.responseXML.selectNodes('//concept');
	
	for(i=0;i<concepts.length;i++)
	  {
   		 var c=getTreeNodeFromXMLNode(concepts[i]);
   		 if(c.attributes.id.indexOf("SECURITY")>-1){continue;}
   		 node.appendChild(c);
   	 }
	
}});

function getConceptPatientCount(node)
{
Ext.Ajax.request(
    	    {
    	        url: pageInfo.basePath+"/chart/conceptPatientCount",
    	        method: 'POST',                                       
    	        success: function(result, request){getConceptPatientCountComplete(result, node);},
    	        failure: function(result, request){getConceptPatientCountComplete(result, node);},
    	        timeout: '300000',
    	        params: Ext.urlEncode({charttype:"conceptpatientcount",
    	        		 			   concept_key: node.attributes.id})
    	    });   
}

function getConceptPatientCountComplete(result, node)
{
node.setText(node.text+" <b>("+result.responseText+")</b>");
}

function getChildConceptPatientCounts(node)
{
Ext.Ajax.request(
    	    {
    	        url: pageInfo.basePath+"/chart/childConceptPatientCounts",
    	        method: 'POST',                                       
    	        success: function(result, request){   	        	
    	        	getChildConceptPatientCountsComplete(result, node);
    	        },
    	        failure: function(result, request){    	        	
    	        	// getChildConceptPatientCountsComplete(result, node);
    	        },
    	        timeout: '300000',
    	        params: Ext.urlEncode({charttype:"childconceptpatientcounts",
    	        		 			   concept_key: node.attributes.id})
    	    });   
}

function getChildConceptPatientCountsComplete(result, node)
{
/* eval the response and look up in loop*/
var childaccess=Ext.util.JSON.decode(result.responseText).accesslevels;
var childcounts=Ext.util.JSON.decode(result.responseText).counts;

//var mobj=result.responseText.evalJSON();
//var childaccess=mobj.accesslevels;
//var childcounts=mobj.counts;
/*var cca=new Array();
var size=childcounts.size();
for(var i=0;i<size;i++)
{
	cca[childcounts[i].concept]=childcounts[i].count;
}*/

node.beginUpdate();
var children=node.childNodes;
var size2=children.length;
for (var i=0;i<size2;i++)
{
  var key=children[i].attributes.id;
  var fullname=key.substr(key.indexOf("\\",2), key.length);
  var count=childcounts[fullname];
  var access=childaccess[fullname];
  var child=children[i];
  if(count!=undefined)
  {
   child.setText(child.text+" ("+count+")");
  }
  
  if((access!=undefined && access!='Locked') || GLOBAL.IsAdmin) //if im an admin or there is an access level other than locked leave node unlocked
  {
  	//leave node unlocked must have some read access
  }
  else
  {
  	//default node to locked
  	//child.setText(child.text+" <b>Locked</b>");
  	child.attributes.access='locked';
  	child.disable();
  	child.on('beforeload', function(node){alert("Access to this node has been restricted. Please contact your administrator for access."); return false}); 
  }
}
node.endUpdate();
}

