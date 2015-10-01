Ext.ux.OntologyTreeLoader = Ext.extend(Ext.tree.TreeLoader, {

    requestData: function (node, callback) {
        if (this.fireEvent("beforeload", this, node, callback) !== false) {
            
            node.isModifier = false;
            if (nodeType('iconCls_modifier',node.attributes.iconCls) != ""){
                node.isModifier = true;
            }
            
            //TODO: This is fugly.
            if(!node.isModifier) {
                this.transId = Ext.Ajax.request({
                    method: 'GET',
                    url: pageInfo.basePath + "/concepts/getChildren",
                    params: { concept_key: node.id },
                    success: this.handleResponse,
                    failure: this.handleFailure,
                    scope: this,
                    argument: {callback: callback, node: node},
                    timeout: '120000' //2 minutes
                });
                
            } else {
                this.transId = Ext.Ajax.request({
                    method: 'GET',
                    url: pageInfo.basePath + "/concepts/getModifierChildren",
                    params: { modifier_key: node.id,
                                applied_path: node.attributes.applied_path,
                                qualified_term_key: node.attributes.qualified_term_key},
                    success: this.handleResponse,
                    failure: this.handleFailure,
                    scope: this,
                    argument: {callback: callback, node: node},
                    timeout: '120000' //2 minutes
                });                
            }
        } else {
            // if the load is cancelled, make sure we notify
            // the node that we are done
            if (typeof callback == "function") {
                callback();
            }
        }
    },

    processResponse: function (response, node, callback) {
        node.beginUpdate();
        this.parseJson(response, node);
        getChildConceptPatientCounts(node);
        node.endUpdate();
        if (typeof callback == "function") {
            callback(this, node);
        }
    },

    parseJson: function (response, node) {
        // shorthand
        var Tree = Ext.tree;

        var concepts = Ext.decode(response.responseText);

        var matchList = GLOBAL.PathToExpand.split(",");
        for (i = 0; i < concepts.length; i++) {
            var c = getTreeNodeFromJsonNode(concepts[i]);
            if(c.attributes.id.indexOf("SECURITY")>-1) {continue;}
            //For search results - if the node level is 1 (study) or below and it doesn't appear in the search results, filter it out.
            if(c.attributes.level <= '1' && GLOBAL.PathToExpand != '' && GLOBAL.PathToExpand.indexOf(c.attributes.id) == -1) {
                //However, don't filter studies/top folders out if a higher-level match exists
                var highLevelMatchFound = false;
                for (var j = 0; j < matchList.length-1; j++) { //-1 here - leave out last result (trailing comma)
                    if (c.id.startsWith(matchList[j]) && c.id != matchList[j]) {
                        highLevelMatchFound = true;
                        break;
                    }
                }
                if (!highLevelMatchFound) {
                    continue;
                }
            }
            
            if(nodeType("visualattributes_modifier", c.attributes.visualattributes) != "") {
                c = extendTreeNodeForModifier(c, concepts[i]);
            }
            
            //If the node has been disabled, ignore all children
            if (!node.disabled) {
                node.appendChild(c);
            }
        }

    }});

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
	
    var params = Ext.urlEncode({
        charttype: "childconceptpatientcounts",
        concept_key: node.attributes.id
    });

    // Ext AJAX has intermittent failure to pass parameters when many AJAX requests are made in a short space of time - switched to jQuery here
    jQuery.ajax({
        url: pageInfo.basePath + "/chart/childConceptPatientCounts",
        method: 'POST',
        success: function(result){getChildConceptPatientCountsComplete(result, node);},
        data: {charttype: "childconceptpatientcounts", concept_key: node.attributes.id}
    });
}

function getChildConceptPatientCountsComplete(result, node) {
    var mobj = result;
    var childaccess = mobj.accesslevels;
    var childcounts = mobj.counts;
    node.beginUpdate();
    var children = node.childNodes;
    var size2 = children.length;
    for (var i = 0; i < size2; i++) {
        var key = children[i].attributes.id;
        var fullname = key.substr(key.indexOf("\\", 2), key.length);
        var count = childcounts != null ? childcounts[fullname] : '';
        var access = childaccess != null ? childaccess[fullname] : '';
        var child = children[i];
        if (count != undefined) {
            child.setText(child.text + "<em> (" + count + ")</em>");
        }

        if ((access != undefined && access != 'Locked') ||
                key.indexOf('\\\\xtrials\\') === 0 || // across trials node should never be locked
                GLOBAL.IsAdmin) //if im an admin or there is an access level other than locked leave node unlocked
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
                return false;
            });
        }
    }
    node.endUpdate();
}

