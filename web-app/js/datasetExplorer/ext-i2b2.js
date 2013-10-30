/*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.    You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.    You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************/



Ext.ux.OntologyTreeLoader = Ext.extend(Ext.tree.TreeLoader, {

    requestData: function (node, callback) {
        if (this.fireEvent("beforeload", this, node, callback) !== false) {
         this.getChildModifiers(node, callback);
        } else {
            // if the load is cancelled, make sure we notify
            // the node that we are done
            if (typeof callback == "function") {
                callback();
            }
        }
    },

    processResponse: function (response, node, callback) {
        //	if(GLOBAL.Debug){alert(response.responseText)};
        try {
            //response.responseText.evalJSON();
        }
        catch (e) {
        }
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

    parseXml: function (response, node) {
        // shorthand
        var Tree = Ext.tree;

        var concept = null;
        var concepts = response.responseXML.selectNodes('//concept');

        var matchList = GLOBAL.PathToExpand.split(",");

        for(i=0;i<concepts.length;i++)
        {
            var c = getTreeNodeFromXMLNode(concepts[i]);
            if(c.attributes.id.indexOf("SECURITY")>-1) {continue;}
            //For search results - if the node level is 1 (study) or below and it doesn't appear in the search results, filter it out.
            if(c.attributes.level <= '1' && GLOBAL.PathToExpand != '' && GLOBAL.PathToExpand.indexOf(c.attributes.id) == -1) {
                //However, don't filter studies/top folders out if a higher-level match exists
                var highLevelMatchFound = false;
                for (var j = 0; j < matchList.size()-1; j++) { //-1 here - leave out last result (trailing comma)
                    if (c.id.startsWith(matchList[j]) && c.id != matchList[j]) {
                        highLevelMatchFound = true;
                        break;
                    }
                }
                if (!highLevelMatchFound) {
                    continue;
                }
            }

            //If the node has been disabled, ignore all children
            if (!node.disabled) {
                node.appendChild(c);
            }
        }

    },

    processModifierResponse: function (response, node, callback) {
        //	if(GLOBAL.Debug){alert(response.responseText)};
        try {
            //response.responseText.evalJSON();
        }
        catch (e) {
        }
        //var nodes = this.parseXml(response);
        node.beginUpdate();
        //node.appendChild(nodes);
        this.parseModifierXml(response, node);
        node.endUpdate();
       /* if (typeof callback == "function") {
            callback(this, node);
        }  */
    },

    parseModifierXml: function (response, node) {
        // shorthand
        var Tree = Ext.tree;

        var concept = null;
        var concepts = response.responseXML.selectNodes('//modifier');

        for (i = 0; i < concepts.length; i++) {
            var c = getTreeNodeFromXMLNode(concepts[i]);
            if (c.attributes.id.indexOf("SECURITY") > -1) {
                continue;
            }
            node.appendChild(c);
        }

    },
    handleModifierResponse: function (response) {
        this.transId = false;
        var a = response.argument;
        this.processModifierResponse(response, a.node, a.callback);
        //chain to get children
        this.getChildConcepts(a.node, a.callback);
    },

    getChildModifiers: function (node, callback) {
        var getModifierRequest = getONTRequestHeader() + '<ns4:get_modifiers synonyms="false" hiddens="false">';
        getModifierRequest = getModifierRequest + "<self>" + node.id + "</self></ns4:get_modifiers>" + getONTRequestFooter();

        this.transId = Ext.Ajax.request({
            url: pageInfo.basePath + "/proxy?url=" + GLOBAL.ONTUrl + "getModifiers",
            method: 'POST',
            xmlData: getModifierRequest,
            success: this.handleModifierResponse,
            failure: this.handleFailure,
            scope: this,
            argument: {callback: callback, node: node},
            timeout: '120000', //2 minutes
            params: { }
        });
    },

    getChildConcepts: function (node, callback) {
        var getChildrenRequest = getONTRequestHeader() + '<ns4:get_children blob="true" max="1000" synonyms="false" hiddens="false">';
        getChildrenRequest = getChildrenRequest + "<parent>" + node.id + "</parent></ns4:getchildren>" + getONTRequestFooter();

        this.transId = Ext.Ajax.request({
            url: pageInfo.basePath + "/proxy?url=" + GLOBAL.ONTUrl + "getChildren",
            method: 'POST',
            xmlData: getChildrenRequest,
            success: this.handleResponse,
            failure: this.handleFailure,
            scope: this,
            argument: {callback: callback, node: node},
            timeout: '120000', //2 minutes
            params: { }
        });
    }

});


function getConceptPatientCount(node) {
    Ext.Ajax.request(
        {
            url: pageInfo.basePath + "/chart/conceptPatientCount",
            method: 'POST',
            success: function (result, request) {
                getConceptPatientCountComplete(result, node);
            },
            failure: function (result, request) {
                getConceptPatientCountComplete(result, node);
            },
            timeout: '300000',
            params: Ext.urlEncode({charttype: "conceptpatientcount",
                concept_key: node.attributes.id})
        });
}

function getConceptPatientCountComplete(result, node) {
    node.setText(node.text + " <b>(" + result.responseText + ")</b>");
}

function getChildConceptPatientCounts(node) {


    //For some reason this was causing the 2nd and later requests to have content type of xml...causing the parameters to be null. Fixed with jQuery
    jQuery.ajax({
        url: pageInfo.basePath + "/chart/childConceptPatientCounts",
        type: 'POST',
        success: function (result, request) {
            getChildConceptPatientCountsComplete(result, node);
        },
        failure: function (result, request) {
            getChildConceptPatientCountsComplete(result, node);
        },
        timeout: '300000',
        data: Ext.urlEncode({charttype: "childconceptpatientcounts",
            concept_key: node.attributes.id})
    })



    /*Ext.Ajax.request(
        {
            url: pageInfo.basePath + "/chart/childConceptPatientCounts",
            method: 'POST',
            headers: {"Content-Type":"application/x-www-form-urlencoded;charset=UTF-8"},
            success: function (result, request) {
                getChildConceptPatientCountsComplete(result, node);
            },
            failure: function (result, request) {
                getChildConceptPatientCountsComplete(result, node);
            },
            timeout: '300000',
            params: Ext.urlEncode({charttype: "childconceptpatientcounts",
                concept_key: node.attributes.id})
        });  */
}

function getChildConceptPatientCountsComplete(result, node) {
    /* eval the response and look up in loop*/
//var childaccess=Ext.util.JSON.decode(result.responseText).accesslevels;
//var childcounts=Ext.util.JSON.decode(result.responseText).counts;
    //var mobj = result.responseText.evalJSON();
    var mobj = result;
    var childaccess = mobj.accesslevels;
    var childcounts = mobj.counts;
    /*var cca=new Array();
     var size=childcounts.size();
     for(var i=0;i<size;i++)
     {
     cca[childcounts[i].concept]=childcounts[i].count;
     }*/
    var blah = node;
    node.beginUpdate();
    var children = node.childNodes;
    var size2 = children.size()
    for (var i = 0; i < size2; i++) {
        var key = children[i].attributes.id;
        var fullname = key.substr(key.indexOf("\\", 2), key.length);
        var count = childcounts[fullname];
        var access = childaccess[fullname];
        var child = children[i];
        if (count != undefined) {
            child.setText(child.text + " (" + count + ")");
        }

        if ((access != undefined && access != 'Locked') || GLOBAL.IsAdmin) //if im an admin or there is an access level other than locked leave node unlocked
        {
            //leave node unlocked must have some read access
        }
        else {
            //default node to locked
            //child.setText(child.text+" <b>Locked</b>");
            child.attributes.access = 'locked';
            child.disable();
            child.on('beforeload', function (node) {
                alert("Access to this node has been restricted. Please contact your administrator for access.");
                return false
            });
        }
    }
    node.endUpdate();
}

