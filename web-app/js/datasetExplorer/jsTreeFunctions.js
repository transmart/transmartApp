function drawHardcodedJQueryTree()
{

    //Create a toolbar that has a text field to search with and a context specific help link.
    var helpToolBar = new Ext.Toolbar([
        new Ext.form.TextField({id:"txtCrossSearchBox"}),
        {
            id:'searchButton',
            handler: function(event, toolEl, panel){

                //Render the concepts that match the search term.
                drawjQueryTree("#pnlMainItems",pageInfo.basePath + "/crossTrial/generateTree",Ext.get("txtCrossSearchBox").getValue(),null)

                Ext.getCmp("pnlRelatedItems").show()

                //Render the related tree.
                drawjQueryTree("#" + Ext.getCmp("pnlRelatedItems").body.id,pageInfo.basePath + "/crossTrial/generateTree",Ext.get("txtCrossSearchBox").getValue(),"related")

                //Size the panel since the search results bar is visible
                sizeTreePanelRelated("pnlMainItems", "pnlRelatedItems");
            },
            text:'Search'
        },
        {
            id:'clearButton',
            handler: function(event, toolEl, panel){

                //Reload the tree.
                drawjQueryTree("#pnlMainItems",pageInfo.basePath + "/crossTrial/generateTree",null,null)

                //Hide the panel that shows the search result count.
                Ext.getCmp("lowerCrossSearchToolbar").hide()

                //Clear the text out of the search bar.
                Ext.getCmp("txtCrossSearchBox").setValue("")

                //Hide the panel that holds our related search results.
                Ext.getCmp("pnlRelatedItems").hide()

                //Size the panel since the search results bar is not visible
                sizeTreePanel("pnlMainItems");

            },
            text:'Clear'
        },
        '->',
        {
            id:'contextHelp-button',
            handler: function(event, toolEl, panel){
                D2H_ShowHelp("1319",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
            },
            iconCls: "contextHelpBtn"
        }
    ]);

    //This toolbar will be used to show the counts for items found in a search.
    var toolbar2 = new Ext.Toolbar({id : "lowerCrossSearchToolbar"});

    toolbar2.hide();

    //Create another tab in the ontology tree tabpanel. Include the above toolbar in the panel.
    acrossTrialTreePanel = new Ext.Panel(
        {
            id : 'acrossTrialTreePanel',
            title : 'Across Trial',
            autoScroll : false,
            listeners :
            {
                activate: function(p){

                    sizeTreePanel("pnlMainItems");

                    $j(window).resize(function() {
                        ontTabPanel.doLayout();
                        sizeTreePanel("pnlMainItems");

                        if ($j("#pnlRelatedItems").is(":visible")){
                            sizeTreePanelRelated("pnlMainItems", "pnlRelatedItems");
                        };
                    });

                }
            }
        }
    );

    //Create a panel which will be our tree/immediate search results.
    var mainTreePanel = new Ext.Panel(
        {
            id : 'pnlMainItems'
        }
    );

    //Add a new panel to the across trial tab which will hold "Related Items" that are displayed when searching.
    var relatedItemsPanel = new Ext.Panel(
        {
            id : 'pnlRelatedItems',
            title : 'Related Items'
        }
    );

    relatedItemsPanel.hide()

    acrossTrialTreePanel.add(helpToolBar)
    acrossTrialTreePanel.add(toolbar2)
    acrossTrialTreePanel.add(mainTreePanel)
    acrossTrialTreePanel.add(relatedItemsPanel)

    //Add the new tab with the tree DIV to the tab panel object.
    ontTabPanel.add(acrossTrialTreePanel);
    ontTabPanel.doLayout();

    drawjQueryTree("#pnlMainItems",pageInfo.basePath + "/crossTrial/generateTree",null,null);
}



function drawAcrossTrialTree_OLD()
{
	//Create a toolbar that has a text field to search with and a context specific help link.
    var helpToolBar = new Ext.Toolbar([
                           		new Ext.form.TextField({id:"txtCrossSearchBox"}),
                           		{
                           			id:'searchButton',
                           			handler: function(event, toolEl, panel){
                           				
	                           			//Render the concepts that match the search term.
	                  			   		drawjQueryTree("#pnlMainItems",pageInfo.basePath + "/crossTrial/generateTree",Ext.get("txtCrossSearchBox").getValue(),null)
	                          			
	                  			   		Ext.getCmp("pnlRelatedItems").show()
	                  			   		
	                  			   		//Render the related tree.
	                  			   		drawjQueryTree("#" + Ext.getCmp("pnlRelatedItems").body.id,pageInfo.basePath + "/crossTrial/generateTree",Ext.get("txtCrossSearchBox").getValue(),"related")
                           			
	                  			   		//Size the panel since the search results bar is visible
	                  			   		sizeTreePanelRelated("pnlMainItems", "pnlRelatedItems");
                           			},
                           			text:'Search'
                           		},
                          		{
                          			id:'clearButton',
                          			handler: function(event, toolEl, panel){
                          				
                          				//Reload the tree.
                          				drawjQueryTree("#pnlMainItems",pageInfo.basePath + "/crossTrial/generateTree",null,null)
                          				
                          				//Hide the panel that shows the search result count.
                          				Ext.getCmp("lowerCrossSearchToolbar").hide()
                          				
                          				//Clear the text out of the search bar.
                          				Ext.getCmp("txtCrossSearchBox").setValue("")
                          				
                          				//Hide the panel that holds our related search results.
                          				Ext.getCmp("pnlRelatedItems").hide()
                          				
                          				//Size the panel since the search results bar is not visible
                          				sizeTreePanel("pnlMainItems");
                          				
                          			},
                          			text:'Clear'
                          		},                                 	  	
                          		'->',
                          		{
                          			id:'contextHelp-button',
                          			handler: function(event, toolEl, panel){
                          				D2H_ShowHelp("1319",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
                          			},
                          			iconCls: "contextHelpBtn"  
                          		}                           		
                               ]);	
    
	//This toolbar will be used to show the counts for items found in a search.
	var toolbar2 = new Ext.Toolbar({id : "lowerCrossSearchToolbar"});
    
	toolbar2.hide();
	
	//Create another tab in the ontology tree tabpanel. Include the above toolbar in the panel.
	acrossTrialTreePanel = new Ext.Panel(
			{
				id : 'acrossTrialTreePanel',
				title : 'Across Trial',
				autoScroll : false,
				listeners :
                {
					activate: function(p){
						
						sizeTreePanel("pnlMainItems");
						
						$j(window).resize(function() {
							ontTabPanel.doLayout();
							sizeTreePanel("pnlMainItems");
							
							if ($j("#pnlRelatedItems").is(":visible")){
								sizeTreePanelRelated("pnlMainItems", "pnlRelatedItems");
							};
						});
	
                	}
                }
			}
	);	
	
	//Create a panel which will be our tree/immediate search results.
	var mainTreePanel = new Ext.Panel(
			{
				id : 'pnlMainItems'
			}
	);	
	
	//Add a new panel to the across trial tab which will hold "Related Items" that are displayed when searching.
	var relatedItemsPanel = new Ext.Panel(
			{
				id : 'pnlRelatedItems',
				title : 'Related Items'
			}
	);	
	
	relatedItemsPanel.hide()
	
	// Removing across trial tree search, replace with integrated search
	// acrossTrialTreePanel.add(helpToolBar)
	acrossTrialTreePanel.add(toolbar2)
	acrossTrialTreePanel.add(mainTreePanel)
	acrossTrialTreePanel.add(relatedItemsPanel)
	
	//Add the new tab with the tree DIV to the tab panel object.
	ontTabPanel.add(acrossTrialTreePanel);
	ontTabPanel.doLayout();

	drawjQueryTree("#pnlMainItems",pageInfo.basePath + "/crossTrial/generateTree",null,null);
}

//Setup the across trial tree for scrolling. We need to wrap the UL element in a DIV and setup scrolling and height
function sizeTreePanel(panelId)
{
	
	var selfPanel = $j("#" + panelId);
	
   	var parentHeight = selfPanel.parent().innerHeight();
   	var otherHeight = 0;
   	
   	$j("#" + panelId).siblings().filter(":visible").each( function() {
   		otherHeight += $j(this).outerHeight();
   	}); 
	
	$j("#" + panelId).css("overflow", "auto").css("height", parentHeight - otherHeight );

}

function sizeTreePanelRelated(panelId, panelRelatedId)
{
	//This differs from sizeTreePanel because we know we have related items panel.
	//So now we will split the main panel in half and give each the same height with scroll.

	var selfPanel = $j("#" + panelId);
	var relatedPanel = $j("#" + panelRelatedId);
	
   	var parentHeight = selfPanel.parent().innerHeight();
   	var otherHeight = 0;
   	
   	selfPanel.siblings().not("#" + panelRelatedId).filter(":visible").each( function() {
   		otherHeight += $j(this).outerHeight();
   	}); 
   	
   	var newHeight = (parentHeight - 23 - otherHeight) / 2;

   	selfPanel.css("overflow", "auto").css("height", newHeight );
   	relatedPanel.css("height", newHeight );
   	
   	var relatedPanelHeight = newHeight - relatedPanel.children(".x-panel-header").outerHeight();

   	var childEl = relatedPanel.find(".x-panel-body");
   	
   	childEl.css("overflow", "auto").css("height", relatedPanelHeight );

}

function drawjQueryTree(div,url,searchTerm,treeType)
{	
	//Make the new DIV into a tree.
	jQuery(div).jstree({
		"treeType" : treeType,
		"json_data" : {
			"ajax" : {
				"url" : url,
				"data" : function (nodeToOpen) { 
					
						//This is the javascript object we load up to pass back to the server.
						var returnObject = {};
						
						//Get the type of tree (related or null. null for a typical tree or search tree)
						returnObject["treeType"] = this.get_settings().treeType ? this.get_settings().treeType : ""
						
						//If the tree is initially loading, hard code some parameters.
						if(nodeToOpen == -1)
						{
							returnObject["nodeToOpen"] = -1;
							returnObject["nodeLevel"] = -1;
							returnObject["searchTerm"] = searchTerm ? searchTerm : ""
						}
						else
						{
							//Gather some data when we are opening a node in the tree.
							returnObject["nodeToOpen"] = nodeToOpen.data().dimcode;
							returnObject["nodeLevel"] = nodeToOpen.data().level;
							returnObject["visitInd"] = nodeToOpen.data().visitInd;
							returnObject["inOutCode"] = nodeToOpen.data().inOutCode;
							
							//The related tree needs to include the search term when opening a node.
							if(!returnObject["treeType"])
							{
								returnObject["searchTerm"] = ""
							}
							else
							{
								returnObject["searchTerm"] = searchTerm ? searchTerm : ""
							}
						}
							
						return returnObject;
					},
				"success": function (x) {
					
					if(searchTerm)
					{
						//Show the search bar that has the count of search results.
			   			Ext.getCmp("lowerCrossSearchToolbar").show()

			   			//If the panel that we draw the result counts in isn't there, add it.
			   			if(!Ext.getCmp("searchCrossResultsPanel"))
			   			{
			   				var resultsTextPanel = new Ext.Panel({id : 'searchCrossResultsPanel'});
			   				Ext.getCmp("lowerCrossSearchToolbar").add(resultsTextPanel)
			   			}
			   			
			   			//Fill the toolbar which shows the result counts.
			   			Ext.getCmp("searchCrossResultsPanel").setBody("Found " + x.size() + " result(s)");
					}
		   			
		   			acrossTrialTreePanel.doLayout();
		   			
		   			if (treeType == "related")
		   			{
		   				//Size the panel since the search results bar is visible
      			   		sizeTreePanelRelated("pnlMainItems", "pnlRelatedItems");
		   			}
		   			
				    return x;
				}
			}
		},
		"themes" : {
			"theme" : "classic",
			"dots" : true,
			"icons" : true
		},
		"dnd" : {
			"drop_finish" : function (data) { 
				//We have a different drop function depending on what we are dropping into.
				if(jQuery("#" + data.e.currentTarget.id).hasClass("results_analysis"))
				{
					dropOntoResultsAnalysisCrossTrial(data, 'stats');
				}
				else if(jQuery("#" + data.e.currentTarget.id).hasClass("results_grid"))
				{
					dropOntoResultsAnalysisCrossTrial(data, 'grid');
				}				
				else
				{
					preDropOntoCategorySelectionCrossTrial(data);
				}
			},
			"drop_check"  : function (data) 
			{
				if(data.o.data("level") == "0"){
					return false;
				}				
				
				return true; 
			},
			"drag_check" : function (data) {
				if(data.r.attr("id") == "divTimeVariable") {
					return false;
				}

				return { 
					after : false, 
					before : false, 
					inside : true 
				};
			}
		},
		"types" : {
			"valid_children" : [ "T","N" ],
			"types" : {
				"T" : {
					"icon" : {
						"image" : "../images/alpha.gif"
					}
				},
				"N" : {
					"icon" : {
						"image" : "../images/numeric.gif"
					}					
				}
			}
		},
		"crrm" : {
	        "move" : {
	          "check_move" : function (m) {
	            return false;
	          }
	        }
	      },	
		"plugins" : [ "crrm", "themes", "json_data", "ui", "dnd", "types" ]
	})	
}

function convertCrossNodeToConcept(node, showValueConstraintPopup)
{
	var value			=	new Value();
	var level			=	node.data("level");
	var name			=	node.text;
	var key				=	node.data("id");
	var tooltip			=	node.data("qtip");
	var tablename		=	node.data("tablename");
	var dimcode			=	node.data("dimcode");
	var comment			=	node.data("comment");
	var normalunits		=	node.data("normalunits");
	var oktousevalues	=	node.data("oktousevalues");
	var timingLevel		=	node.data("timingLevel");
	var leafNode		=	node.hasClass("jstree-leaf");

    //for future i2b2 native modifier support
    var ismodifier = false;
    var modifierappliedpath = null;
    var modifiername = null;
    var modifierkey = null;

    /*JMI the following modifier code works for extjs tree not the jquery tree used here, need to port this code in this location:
    //var ismodifier = node.attributes.ismodifier;
    /*if(ismodifier)
    {
        modifierappliedpath = node.attributes.appliedpath;
        modifiername =node.text;
        modifierkey=node.id;
        node=node.parentNode; //swap out for rest of properties;
    } */



	//This is the text from the visit dimension that describes this particular encounter. This applies to time series data.
	var inOutCode		=	node.data("inOutCode");

	//Each node has a type (Categorical, Continuous, High Dimensional Data) that we need to populate. For now we will use the icon class.
	var nodeType = node.data("iconCls")
	
	if(!nodeType || nodeType == "T") nodeType = ""
	
	if(nodeType == "N") 
	{
		nodeType = "valueicon"
		
		//This will set flags that force a popup when a user drags an item into a DIV.
		if(showValueConstraintPopup) oktousevalues = "Y"
	}
	
	//Default the mode if it's okay to use value constraints.
	if(oktousevalues=="Y") value.mode="numeric"; 
	
	//If we haven't specified that it's okay to use values, set it to a character string here.
	if(!oktousevalues) oktousevalues = "N"
	
	//If this is a leafnode, set the level to be leaf.
	if(leafNode) level = "leaf"
	
	var myConcept=new Concept(name, key, level, tooltip, tablename, dimcode, comment, normalunits, oktousevalues, value, nodeType, ismodifier, modifiername, modifierappliedpath, modifierkey, inOutCode, timingLevel);
	return myConcept;
}

function preDropOntoCategorySelectionCrossTrial(droppedNodeData)
{
	var leafNodesOnly = jQuery("#" + droppedNodeData.e.currentTarget.id).hasClass("leafNodesOnly");
	var targetDiv = droppedNodeData.e.currentTarget.id;
	var dropObject = droppedNodeData.o;
	
	//If we dragged in a folder that hasn't been opened yet (no kids) we need to open it before calling the drop function.
	if(droppedNodeData.o.hasClass("jstree-leaf") == false && leafNodesOnly && droppedNodeData.o.hasClass("jstree-closed") && droppedNodeData.o.find("li").size() == 0) 
	{
			//Fire off the opening of the tree node.
			jQuery("#pnlMainItems").jstree("open_node",droppedNodeData.o, 
					function()
						{
							dropOntoCategorySelectionCrossTrial(targetDiv, dropObject); 
						})
	
	}
	else
	{
		dropOntoCategorySelectionCrossTrial(targetDiv, dropObject)
	}
	
	return true;
}

////////////////////////////
//These are the jQuery versions of these functions
//This function fires when an item is dropped onto an input box.
////////////////////////////
function dropOntoCategorySelectionCrossTrial(targetDiv, dropObject)
{
	var targetdiv = Ext.get(targetDiv)
	var leafNodesOnly = jQuery("#" + targetDiv).hasClass("leafNodesOnly");
	var shortNameDepth = 3;
	
	if(jQuery("#" + targetDiv).attr("shortNameLevel"))
	{
		shortNameDepth = jQuery("#" + targetDiv).attr("shortNameLevel")
	}
	
	//Node must be folder so use children leafs
	if(dropObject.hasClass("jstree-leaf") == false && leafNodesOnly) 
	{
		
		//Keep track of whether the folder has any leaves.
		var foundLeafNode = false
		
		dropObject.find("li").each(function (idx, listItem)
		{
			//Grab the child node.
			var child=jQuery(listItem);
			
			//If this is a leaf node, add it.
			if(child.hasClass("jstree-leaf")==true)
			{
				//Handle the panel timing flag.
				toggleSameEventPanelTiming(child.data("timingLevel"),targetdiv);
				
				//Add the item to the input.
				var concept = createPanelItemNew(targetdiv, convertCrossNodeToConcept(child), shortNameDepth);
				
				//Set the flag indicating we had a leaf node.
				foundLeafNode = true;
							
			}
			
		})
		
		//If no leaf nodes found, alert the user.
		if(!foundLeafNode)
		{
			Ext.Msg.alert('No Nodes in Folder','When dragging in a folder you must select a folder that has leaf nodes directly under it.');
		}				
	}
	else 
	{
		//This tells us whether it is a numeric or character node.
		var numericNode = false;
		
		//This class tells us that the current DIV doesn't allow a popup to specify value constraints.
		var showValueConstraintPopup = !jQuery("#" + targetDiv).hasClass("excludeValuePopup");
		
		if(dropObject.data("iconCls") == "N") numericNode = true
		
		//Handle the panel timing flag.
		toggleSameEventPanelTiming(dropObject.data("timingLevel"),targetdiv);
		
		if(!(numericNode && showValueConstraintPopup))
		{
			//took this 3 params method(createPanelItemNew) from FDA code and changed the name to 'createPanelItemNewWithShortName' to overcome method overloading issue in javascript
            var concept = createPanelItemNewWithShortName(targetdiv, convertCrossNodeToConcept(dropObject, showValueConstraintPopup), shortNameDepth);
		}
		else
		{
			//Add the item to the input.
			var concept = createPanelItemNew(Ext.get("hiddenDragDiv"), convertCrossNodeToConcept(dropObject, showValueConstraintPopup), shortNameDepth);			
		}
		
    	//This sets some values that the SetValueDialog function uses.
    	selectConcept(concept);
		
		//If this is a numeric node, show the dialog to set it.
        if(numericNode && showValueConstraintPopup)
        {
        	//Set the state of the application to dragging until the user finalizes from the value popup.
            STATE.Dragging = true;
            
            //Set the target of the finished value criteria when the user finishes in the popup.
            STATE.Target = targetdiv;
            
            //Popup the value criteria menu.
            showSetValueDialog();
        }			

	}
	return true;
} 

//This function will toggle a css class on and off a div based on the timing level.
function toggleSameEventPanelTiming(panelTiming, divObject)
{
	//If the node the user dragged in is Subject level (data.node.attributes.timingLevel = "S") and there are no other nodes, we need to set the .css class of this box to be not the same event.
	if(panelTiming == "S" && divObject.dom.firstChild == null)
	{
		divObject.removeClass("queryGroupSAMEEVENT")
	}
	
	//If the node the user dragged in is event level (data.node.attributes.timingLevel == "E") set the flag to be same event.
	if(panelTiming == "E")
	{
		divObject.addClass("queryGroupSAMEEVENT")
	}
	
	//If the node the user dragged in is a mixed node and there isn't a node in there already, remove the same event class.
	if(panelTiming != "E" && divObject.dom.firstChild == null)
	{
		divObject.removeClass("queryGroupSAMEEVENT")
	}		
}

function dropOntoResultsAnalysisCrossTrial(data, dropType)
{
	//This sets a value indicating whether this is a value icon or not.
	var isNumeric = data.o.data("iconCls") == "N"
	
	//Build a concept from the dragged in node.
	var concept = convertCrossNodeToConcept(data.o, isNumeric)
	
	buildAnalysisCrossTrial(concept.dimcode, concept.key, concept.level, concept.oktousevalues, dropType, concept.inOutCode)
}

////////////////////////////

