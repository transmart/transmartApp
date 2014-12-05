function extendTreeNodeForModifier(existingTreeNode, responseModifierObject)
{
    existingTreeNode.attributes.qualified_term_key = responseModifierObject.qualified_term_key;
    existingTreeNode.attributes.applied_path = responseModifierObject.applied_path;
    
    return existingTreeNode;
}

function prepareDroppedModifier(node, modifierElementId)
{     
    //We keep track of the node this modifier modifies. This is needed to build the query later.
    var modifiedNode = createModifiedNode(node);
    var modifierAsConcept = convertNodeToConcept(node);
    var draggedModifier = new modifierNode();
    
     modifierAsConcept.key = node.attributes.modifierId;     
 
    draggedModifier.initializeFromNode(node)
    
     if(draggedModifier.needsValuePopup)
     {
         var concept = createPanelItemNew(Ext.get(modifierElementId), modifierAsConcept, true);
        selectConcept(concept);
        
        STATE.Dragging = true;
        STATE.Target = modifierElement;         
         
        var newModifierPopupWindow = new modifierInfoScreen()
        newModifierPopupWindow.generateDialog(draggedModifier);
     }
     else
     {
         var concept = createPanelItemNew(Ext.get(modifierElementId).dom, modifierAsConcept);
         selectConcept(concept);
     }
     
}

function createModifiedNode(node)
{
    var modifiedNode;
    
    //Check our parent, if it's a modifier container than check its parent to find out the node we are modifying.
    if(node.parentNode.attributes.applied_path != null && node.parentNode.attributes.applied_path != "@")
    {
         modifiedNode = node.parentNode.parentNode;
    }
    else
    {
         modifiedNode = node.parentNode;
    }

    node.attributes.modifiedNodePath  = modifiedNode.attributes.id;
    node.attributes.modifiedNodeId    = climbTreeBuildName(modifiedNode);
    node.attributes.modifiedNodeLevel = modifiedNode.attributes.level;     
    
    return modifiedNode;
}

function modifierNode()
{
     
     this.needsValuePopup     = false
     
     this.modifierValuesHashmap = {};

     this.initializeFromNode = function(modifierNode)
     {          
          this.oktousevalues = modifierNode.attributes.oktousevalues;

          //Get the Data Type field out of the XML so we know what to expect in the rest of the response.
          this.modifierValueType = modifierNode.constraint_data_type;
          
          if(this.oktousevalues != null && this.oktousevalues == "Y")
          {
               this.needsValuePopup = true;
               
               if(this.modifierValueType == 'Enum') this.__initializeEnumValues();
          }

     }
     
     this._initializeEnumValues = function()
     {
         //If the Data Type is Enum we need to pull the list of values and draw a popup with a select list.
         var modifierEnumValues = [""];
         
         //this.modifierValuesHashmap["TEST1"] = "TEST1";
     }

}

function modifierInfoScreen()
{
     this.draggedModifier = null;
     
     this._createDialog = function() 
     {
          jQuery( "#modifierValueDiv" ).dialog(
                    {
                         show: 
                              {
                                   effect: "fade",
                                   duration: 100
                              },
                        hide: 
                             {
                                 effect: "fade",
                                 duration: 100
                             },
                        dialogClass:'modifierValueDialog no-close',
                        maxHeight: 600,
                        minHeight: 300,
                        minWidth: 150,
                        maxWidth:300,
                        modal:true
                   });          
     }
     
     this._enumWindowFinished = function()
     {     
          var valuetext="";
          
          if(jQuery("#chkEnableModifierValues")[0].checked) valuetext = "In List (" + jQuery("#modifierValueList").val() + ")"
          
          applyValueText(selectedConcept, valuetext, "Enum");

          invalidateSubset(getSubsetFromPanel(selectedConcept.parentNode));
          
          toggleNodeDraggingState();
          
          jQuery( "#divModifierEnum").hide();
          jQuery('#modifierValueDiv').dialog('close');

     }
     
     this._setupEnumInput = function(draggedModifier)
     {
          jQuery( "#divModifierEnum").show();
          
          jQuery('#modifierValueList').find('option').remove().end();
          
          jQuery.each(draggedModifier.modifierValuesHashmap, 
          function(key, value) 
          {
               jQuery('#modifierValueList').append("<option value='" + key + "'>" + value + "</option>");
          });
     
          jQuery( "#btnModifierValuesDone" ).unbind("click").bind("click", this._enumWindowFinished);     
     }
     
     this._floatWindowFinished = function()
     {
          var valuetext="";
          
          if(jQuery('input[name=valueMethod]:checked').val() != "novalue") valuetext = getSetValueText("numeric", jQuery("#valueOperator").val(),"",jQuery("#highValueModifier").val(),jQuery("#lowValueModifier").val(),"")
          
          applyValueText(selectedConcept, valuetext, "Float");
          
          jQuery( "#divModifierFloat").hide();
          jQuery('#modifierValueDiv').dialog('close');
     }
     
     this._setupFloatInput = function()
     {
          jQuery( "#divModifierFloat").show();
          jQuery( "#btnModifierValuesDone" ).unbind("click").bind("click", this._floatWindowFinished);
          valueMethodChanged(jQuery("#valueMethod").val());
     }
     
     //Build the Modifier select list.
     this.generateDialog = function(draggedModifier)
     {
          this.draggedModifier = draggedModifier;
          
          if(draggedModifier.modifierValueType == "Enum")  this._setupEnumInput(draggedModifier);
          if(draggedModifier.modifierValueType == "Float") this._setupFloatInput();  
          
          this._createDialog();
     }

}

function applyValueText(conceptNode, valuetext, valueMode)
{
     conceptNode.setAttribute('conceptsetvaluetext',valuetext);
     conceptNode.setAttribute('setvaluemode',valueMode);
     
     var conceptshortname = conceptNode.getAttribute("conceptshortname");

     Ext.get(conceptNode.id).update(conceptshortname+" "+valuetext);          
}

