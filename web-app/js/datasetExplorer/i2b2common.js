STATE = {
    Dragging: false,
    Target: null,
    QueryRequestCounter: 0
};

// list of supported platform
// TODO : future refactoring should retrieve these values from gpl definitions in the database
var HIGH_DIMENSIONAL_DATA = {
    "mrna"          : {"platform" : "MRNA_AFFYMETRIX",  "type" : "Gene Expression"},
    "mirna_qpcr"    : {"platform" : "MIRNA_QPCR",       "type" : "MIRNA_QPCR"},
    "mirna_seq"     : {"platform" : "MIRNA_SEQ",        "type" : "MIRNA_SEQ"},
    "rbm"           : {"platform" : "RBM",              "type" : "RBM"},
    "proteomics"    : {"platform" : "PROTEIN",          "type" : "PROTEOMICS"},
    "snp"           : {"platform" : "SNP",              "type" : "SNP"},
    "rnaseq"        : {"platform" : "RNA_AFFYMETRIX",   "type" : "RNASEQ"},
    "metabolite"    : {"platform" : "METABOLOMICS",     "type" : "METABOLOMICS"}
};

// Check if current platform is supported
function isSupportedPlatform (currentPlatform) {
    for (var key in HIGH_DIMENSIONAL_DATA) {
        if (currentPlatform == HIGH_DIMENSIONAL_DATA[key].platform) return true;
    }
    return false;
}

// Check if current platform is RBM
function isRBMPlatform (currentPlatform) {
    return currentPlatform == HIGH_DIMENSIONAL_DATA["rbm"].platform ? true : false;
}

// toggle elements in the popup based on whether it is RBM or not
function toggleRBMDisplayElements (ele, eleGpl, eleTissue, eleRbmpanel, platform) {
    if (!isRBMPlatform(platform)) {
        ele.dom.style.display='';
        eleGpl.dom.style.display='';
        eleTissue.dom.style.display='';
        eleRbmpanel.dom.style.display='none';
    } else {
        ele.dom.style.display='none';
        eleGpl.dom.style.display='none';
        eleTissue.dom.style.display='none';
        eleRbmpanel.dom.style.display='';
    }
}

function Concept(name, key, level, tooltip, tablename, dimcode, comment, normalunits, oktousevalues, value, nodeType, visualattributes, applied_path, modifiedNode)
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
    this.nodeType = nodeType;
    this.visualattributes = visualattributes;
    this.applied_path    = applied_path || '@';
    this.modifiedNode    = modifiedNode;
}

function Value(mode, operator, highlowselect, lowvalue, highvalue, units)
{
    if (typeof(mode) == undefined || mode == null) {
        this.mode = "novalue";
    } //default to novalue
    else {
        this.mode = mode;
    }

    if (typeof(operator) == undefined || operator == null) {
        this.operator = "LT";
    }
    else {
        this.operator = operator;
    }

    if (typeof(highlowselect) == undefined || highlowselect == null) {
        this.highlowselect = "N";
    }
    else {
        this.highlowselect = highlowselect;
    }

    if (typeof(lowvalue) == undefined || lowvalue == null) {
        this.lowvalue = "";
    }
    else {
        this.lowvalue = lowvalue;
    }

    if (typeof(highvalue) == undefined || highvalue == null) {
        this.highvalue = "";
    }
    else {
        this.highvalue = highvalue;
    }

    if (typeof(units) == undefined || units == null) {
        this.units = "";
    }
    else {
        this.units = units;
    }
} 

function convertNodeToConcept(node)
{
    var value = new Value();
    var level = node.attributes.level;
    var name = jQuery('<span>' + node.text + '</span>').find("em").remove().end().html().trim();
    var key = node.id;
    var tooltip = node.attributes.qtip;
    var tablename = node.attributes.tablename;
    var dimcode = node.attributes.dimcode;
    var comment = node.attributes.comment;
    var normalunits = node.attributes.normalunits;
    var oktousevalues = node.attributes.oktousevalues;
    var visualattributes = node.attributes.visualattributes;
    var applied_path = node.attributes.applied_path;

    //Each node has a type (Categorical, Continuous, High Dimensional Data) that we need to populate. For now we will use the icon class.
    var nodeType = node.attributes.iconCls;
    var modifiedNode = {};
    
    modifiedNode.path = node.attributes.modifiedNodePath;
    modifiedNode.id = node.attributes.modifiedNodeId;
    modifiedNode.level = node.attributes.modifiedNodeLevel;

    if(oktousevalues === "Y") {
       value.mode="novalue";
    }

    return new Concept(name, key, level, tooltip, tablename, dimcode, comment, normalunits, oktousevalues, value, nodeType, visualattributes, applied_path, modifiedNode);
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
    li.setAttribute('visualattributes',concept.visualattributes);
    li.setAttribute('applied_path',concept.applied_path);
    li.setAttribute('modifiedNodePath',concept.modifiedNode.path);
    li.setAttribute('modifiedNodeId',concept.modifiedNode.id);
    li.setAttribute('modifiedNodeLevel',concept.modifiedNode.level);    
    li.className="panelBoxListItem x-tree-node-collapsed";
    
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

    //Find out the icon (this is a copy&paste from getTreeFromJSON
    var iconCls = false

    if (concept.oktousevalues != "N") {
        iconCls = "valueicon";
    }
    if (concept.visualattributes.indexOf('LEAF') != -1 ||
        concept.visualattributes.indexOf('MULTIPLE') != -1) {
        if (concept.oktousevalues == "N")
            iconCls = "alphaicon";
        // Yet another hack to get icon working seemlessly
        li.className += " x-tree-node-leaf"
    }
    if (concept.visualattributes.indexOf('HIGH_DIMENSIONAL') != -1) {
        iconCls = 'hleaficon';
    }
    if (concept.visualattributes.indexOf('EDITABLE') != -1) {
        iconCls = 'eleaficon';
    }
    if (concept.visualattributes.indexOf('PROGRAM') != '-1') {
        iconCls = "programicon";
    }
    if (concept.visualattributes.indexOf('STUDY') != '-1') {
        iconCls = "studyicon";
    }
    
    if(concept.visualattributes.indexOf('MODIFIER_LEAF') != -1)
    {
        iconCls            = "modifiericon";
        li.setAttribute('ismodifier',"Y");
        
        shortname = createShortNameFromPath(concept.modifiedNode.id);
        
        shortname += " [" + concept.name + "]"
    }
    else
    {
        shortname = concept.name;
    }
    li.setAttribute('conceptshortname',shortname);
    
    //Create the node
    var iconElem=document.createElement('span')
    var textElem=document.createElement('span')
    iconElem.className = "x-tree-node-icon " + (iconCls ? iconCls : '')
    textElem.appendChild(document.createTextNode(shortname+" "+valuetext)); //used to be name
    textElem.className = "concept-text"
    li.appendChild(iconElem);
    li.appendChild(textElem);
    panel.appendChild(li);
    Ext.get(li).addListener('click',conceptClick);
    Ext.get(li).addListener('contextmenu',conceptRightClick);
    new Ext.ToolTip({ target:li, html:concept.key, dismissDelay:10000 });
    li.concept=concept;
    //return the node

    invalidateSubset(jQuery('#' + panel.id).attr('subset'));

    return li;
}

function getSubsetFromPanel(panel)
{
    return jQuery('#' + panel.id).closest(".panelModel").attr('subset');
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
              text=text+" < ";
              break
            case "LE":
             text=text+" <= ";
              break
            case "EQ":
              text=text+" = ";
              break
            case "GT":
              text=text+" > ";
              break
            case "GE":
             text=text+" >= ";
              break
             }
        text=text+lowvalue;
        }
        else 
        {
         text=text+" BETWEEN "+lowvalue+" AND "+highvalue
        }
      }
      else if(mode=='highlow')
          {
      text=text+" "+highlowselecttext;
          }
      else 
          {
          text="";
          }
    return text.trim().length > 0 ? '<em> ' + text + '</em>' : '';
}

function resetSelected()
{
    selectedConcept = null
    jQuery(".panelBoxListItem").each(function (){
        jQuery(this).removeClass("selected")
    })
}

function conceptClick(event)
{
    selectConcept(this.dom);
    event.stopPropagation()
}

function selectConcept(concept)
{
    if (jQuery('#' + concept.getAttribute('id')).hasClass("selected")) {
        resetSelected()
        if (arguments && arguments.length > 1 && !arguments[1])
            return;
    }
    resetSelected()
    selectedConcept=concept; //select this one
    selectedDiv=concept.parentNode;
    selectedConcept.className=selectedConcept.className+" selected";
}

function conceptRightClick(event)
{
    var conceptnode=this.dom;
    selectConcept(conceptnode, true);
    var conceptid=this.dom.attributes.conceptid.nodeValue;
    var comment=this.dom.attributes.conceptcomment.nodeValue;

    if (!this.contextMenuConcepts) {
    this.contextMenuConcepts = new Ext.menu.Menu({
        id: 'contextMenuConcepts',
        items: [{
            text: 'Delete', handler: function() {
                selectedDiv.removeChild(selectedConcept);
                removeUselessPanels();
                invalidateSubset(getSubsetFromPanel(selectedDiv));
            }
        },{
            id: 'setvaluemenu',
            text: 'Set Value',
            handler:function() {
                showSetValueDialog();
            }
        }, {
            id: 'geneexprfiltermenu',
            text: 'Set Filter',
            handler:function() {
                // create mock node object for highDimensionalConceptDropped
                var node = {id: selectedConcept.attributes.conceptid.nodeValue}
                highDimensionalConceptDropped(node, true);
            }
        }, {
            text: 'Show Definition',
            handler: function() {
                showConceptInfoDialog(conceptid, conceptid, comment);}
            }]
        });
    }
    var xy = event.getXY();
    this.contextMenuConcepts.showAt(xy);
    var m=Ext.getCmp('setvaluemenu');
    var o=Ext.getCmp('geneexprfiltermenu');
    m.hide();
    o.hide();
    if(this.dom.attributes.oktousevalues.nodeValue=='Y')
        m.show();
    else if (this.dom.attributes.oktousevalues.nodeValue == 'H')
        o.show();
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
    var conceptname=conceptnode.getAttribute("conceptname");
    jQuery('#' + conceptnode.id + " .concept-text").html(conceptname + " " + valuetext)
    var subset=getSubsetFromPanel(conceptnode.parentNode);
    invalidateSubset(subset);
}

function applySetValueDialog(validation) {

    var mode = getSelected(document.getElementsByName("setValueMethod"))[0].value;
    var highvalue = document.getElementById("setValueHighValue").value;
    var lowvalue = document.getElementById("setValueLowValue").value;
    var units = document.getElementById("setValueUnits").value;
    var operator = document.getElementById("setValueOperator").value;
    var highlowselect = document.getElementById("setValueHighLowSelect").value;

    // make sure that there is a value set
    if (validation && mode=="numeric" && operator == "BETWEEN" && (highvalue == "" || lowvalue== "")){
        alert('You must specify a low and a high value.');
    } else if (validation && mode=="numeric" && lowvalue == "") {
        alert('You must specify a value.');
    } else {

        if (validation)
            setValueDialogComplete(mode, operator, highlowselect, highvalue, lowvalue, units);
        else
            setValueDialogComplete(setvaluewin.mode, setvaluewin.operator, setvaluewin.highlowselect, setvaluewin.highvalue, setvaluewin.lowvalue, setvaluewin.units);

        if (STATE.Dragging) {
            jQuery('#' + selectedConcept.id).remove()
            removeUselessPanels()
        }

        setvaluewin.hide();
    }
}

function showSetValueDialog()
{
        var conceptnode=selectedConcept; //not dragging so selected concept is what im updating
        setvaluewin.setHeight(180); //set height back to old closed

        var top = jQuery("#resultsTabPanel").offset().top + jQuery("#resultsTabPanel").height() / 2 - setvaluewin.height / 1.5
        var left = jQuery("#resultsTabPanel").offset().left + jQuery("#resultsTabPanel").width() / 2 - setvaluewin.width / 2

        setvaluewin.setPosition(left, top);
        Ext.get("setvaluechartsPanel1").update("");
        Ext.get("setvaluechartsPanel2").update("");
        setvaluewin.show(viewport);
        var mode=conceptnode.getAttribute('setvaluemode');
        var test=document.getElementsByName("setValueMethod");
        if(mode!=null) {
            setCheckedValue(test, mode);
            setValueMethodChanged(mode);
        } else {
            if(test.length>0) {
                setCheckedValue(test, "numeric"); //numeric
                setValueMethodChanged("numeric");
            }
        }

        var highvalue = conceptnode.getAttribute('setvaluehighvalue');
        if (highvalue != null) {
                document.getElementById("setValueHighValue").value=highvalue;
        } else {
            document.getElementById("setValueHighValue").value="";
        }

        var lowvalue=conceptnode.getAttribute('setvaluelowvalue');
        var blah=document.getElementById("setValueLowValue");
        if (lowvalue != null) {
            blah.value=lowvalue;
        } else {
            blah.value = "";
        }

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

    setvaluewin.mode = mode;
    setvaluewin.operator = operator;
    setvaluewin.highlowselect = highlowselect;
    setvaluewin.highvalue = highvalue;
    setvaluewin.lowvalue = lowvalue;
    setvaluewin.units = units;

    setValueDialogComplete('novalue', operator, highlowselect, highvalue, lowvalue, units)
}


function setValueDialogComplete(mode, operator, highlowselect, highvalue, lowvalue, units)
{
    var conceptnode=selectedConcept;
    setValue(conceptnode, mode, operator, highlowselect, highvalue, lowvalue, units);
    if(STATE.Dragging==true){
        STATE.Dragging=false;
        moveSelectedConceptFromHoldingToTarget();
    }
}

function moveSelectedConceptFromHoldingToTarget()
{
    var subset=jQuery('#' + selectedConcept.id).closest(".panelModel").attr("subset")
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
                defaultHeaders: { 'Content-Type': 'text/plain' },
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
                    {name: 'type'}
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
          ajaxurl=pageInfo.basePath+'/search/loadSearchPathways';
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
                    {name: 'type'}
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
                 
                 //Put the gene display || transmart search_keywork id in selectedGenesAndIdSNPViewer hidden field, separated by |||
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
                 sp.dom.value="";    // Empty the search term input field, ready for user to type in next term.
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
    search.on('focus', function()    {
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

        toggleRBMDisplayElements(ele, eleGpl, eleTissue, eleRbmpanel, GLOBAL.CurrentPlatforms[applyToDivIdx-1]);

        toggleDataAssociationFields();
        
        this.collapse();
        
        if (!((!GLOBAL.CurrentPlatforms[0]) || (!GLOBAL.CurrentPlatforms[1]) || (GLOBAL.CurrentPlatforms[0]==GLOBAL.CurrentPlatforms[1]))){
            alert('Platforms do not match');
        }
    };
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
    };
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
function hideWhenRBM(applyToDivId, subsetId)
{

    if (isRBMPlatform(GLOBAL.CurrentPlatforms[subsetId-1])) {
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
function hideWhenNotRBM(applyToDivId, subsetId){
    if(!isRBMPlatform(GLOBAL.CurrentPlatforms[subsetId-1])){
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
        hideWhenRBM(applyToDivId, subsetId);
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
        hideWhenNotRBM(applyToDivId, subsetId);
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
        hideWhenRBM(applyToDivId, subsetId);
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
        hideWhenRBM(applyToDivId, subsetId);
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
    
        if(!value_in || value_in=="") value_in = "ALL";
    
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
                height:370,
                //autoHeight: true,
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

    toggleRBMDisplayElements(ele, eleGpl, eleTissue, eleRbmpanel, GLOBAL.CurrentPlatforms[0]);

    ele=(Ext.get('divsample2')) ? Ext.get('divsample2') : Ext.get('divsample4');
    eleGpl=(Ext.get('divgpl2')) ? Ext.get('divgpl2') : Ext.get('divgpl4');
    eleTissue=(Ext.get('divtissue2')) ? Ext.get('divtissue2') : Ext.get('divtissue4');
    eleRbmpanel=(Ext.get('divrbmpanel2')) ? Ext.get('divrbmpanel2') : Ext.get('divrbmpanel4')

    toggleRBMDisplayElements(ele, eleGpl, eleTissue, eleRbmpanel, GLOBAL.CurrentPlatforms[1]);

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
                height:370,
                //autoHeight: true,
                closable: false,
                plain: true,
                modal: true,
                border:false,
                //autoScroll: true,
                buttons: [
                        {
                            id: 'compareStepPathwaySelectionOKButtonRBM',
                            text: 'OK',
                            handler: function()    {                                                                              
                                compareStepPathwaySelectionRBM.hide();
                                var setid1 = GLOBAL.CurrentSubsetIDs[1];
                                      var setid2 = GLOBAL.CurrentSubsetIDs[2];
                                      var setname1 = "subset1";
                                   var setname2 = "subset2";
                                   var sp=Ext.get("searchPathwayRBM");
                                   if (sp.dom.disabled == true)    {
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

function isSubsetEmpty(subset)
{
    var flag = true
    jQuery(".panelModel[subset='" + subset + "']").each(function () {
        if (jQuery(this).find(".panelBoxList").html().trim() != '' && flag)
            flag = false
    })
        
    return flag;
}

function showConceptDistributionHistogram(){

    var concept_key = selectedConcept.getAttribute('conceptid');

    Ext.Ajax.request(
        {
            url: pageInfo.basePath+"/chart/conceptDistribution",
            method: 'POST',
            success: function(result, request){showConceptDistributionHistogramComplete(result);},
            failure: function(result, request){showConceptDistributionHistogramComplete(result);},
            timeout: '300000',
            params: Ext.urlEncode({concept_key: concept_key})
        });
}

function showConceptDistributionHistogramComplete(result)
{
    setvaluewin.setHeight(370);

    if (result == null)
        return Ext.get("setvaluechartsPanel1").update("<div class='x-mask-loading'><div class='conceptDistributionPlaceholder'/></div>");

    Ext.get("setvaluechartsPanel1").update(result.responseText);
}

function showConceptDistributionHistogramForSubset()
{
    var concept_key = selectedConcept.getAttribute('conceptid');

    Ext.Ajax.request(
        {
            url: pageInfo.basePath+"/chart/conceptDistributionForSubset",
            method: 'POST',
            success: function(result, request){showConceptDistributionHistogramForSubsetComplete(result);},
            failure: function(result, request){showConceptDistributionHistogramForSubsetComplete(result);},
            timeout: '300000',
            params: Ext.urlEncode({concept_key: concept_key, result_instance_id1: GLOBAL.CurrentSubsetIDs[getSubsetFromPanel(selectedDiv)]})
        });
}

function showConceptDistributionHistogramForSubsetComplete(result)
{
    setvaluewin.setHeight(370);

    if (result == null)
        return Ext.get("setvaluechartsPanel2").update("<div class='x-mask-loading'><div class='conceptDistributionPlaceholder'/></div>");

    Ext.get("setvaluechartsPanel2").update(result.responseText);
}

function getTreeNodeFromJsonNode(concept)
{
        var Tree = Ext.tree;
        
         var level                =    null;
         var name                =    null;
         var tablename            =    null;
         var tooltip                =    null;
         var key                    =    null;
         var dimcode                =    null;
         var newnode                =    null;
         var leaf                =    false;
         var draggable            =    true;
         var comment                =    null;
         var normalunits            =    null;
         var commentnode            =    null;
         var normalunitsnode     =     null;
         var oktousevaluesnode    =     null;
         var oktousevalues        =    null;
        var visualattributes    =   null;
        var applied_path        =    '@';
         var modifierId            =    null;
         var constraint_data_type =    null;
         
    level                = concept.level;
    key                    = concept.key;
    name                = concept.name;
    tooltip                = concept.tooltip;
    dimcode                = concept.dimensionCode;
    tablename            = concept.dimensionTableName;
    visualattributes    = concept.visualAttributes;

    comment                = ''; //XXX
    normalunits            = concept.metadata && concept.metadata.unitValues
                              ? concept.metadata.unitValues.normalUnits
                              : '';
    oktousevalues        =    concept.metadata
                              ? (concept.metadata.okToUseValues ? 'Y' : 'N')
                              : 'N'
constraint_data_type = concept.metadata ? concept.metadata.dataType : '';
        
        //We need to replace the < signs with &lt;
        name = name.replace(/</gi,"&lt;");
        
        var iconCls    =    null;
        var cls        =    null;
        var tcls     =    null;
        
    if (oktousevalues != "N") {
            iconCls="valueicon";
        }


    if (visualattributes.indexOf('LEAF') != -1 ||
        visualattributes.indexOf('MULTIPLE') != -1) {
        leaf = true;
        if (oktousevalues == "N")
            iconCls="alphaicon";
        /* otherwise false; see init */
            }
    if (visualattributes.indexOf('CONTAINER') != -1) {
        draggable=false;
        /* otherwise true; see init */
        }    

    if (visualattributes.indexOf('HIGH_DIMENSIONAL') != -1) {
        iconCls = 'hleaficon';
        tcls = 'hleafclass';
        oktousevalues = 'H';
    } else if (visualattributes.indexOf('EDITABLE') != -1) {
        iconCls = 'eleaficon';
        tcls = 'eleafclass';
    }

    if (visualattributes.indexOf('PROGRAM') != '-1') {
        iconCls="programicon";
    }

    if (visualattributes.indexOf('STUDY') != '-1') {
        iconCls="studyicon";
    }

    if(visualattributes.indexOf('MODIFIER_LEAF') != -1)
    {
        leaf            = true;
        iconCls            = "modifiericon";
        applied_path     = concept.applied_path;
        modifierId         = concept.key;
    }
    else if(visualattributes.indexOf('MODIFIER_CONTAINER') != -1)
    {
        leaf            = false;
        draggable        = false;
        iconCls            = "modifierfoldericon";
        applied_path     = concept.applied_path;
        modifierId         = concept.key;        
    }

    if(oktousevalues != "N" && !leaf)
    {
        iconCls="foldernumericicon";
    }
    
        //set whether expanded or not.
        var autoExpand=false;
        // Crude string check to bold this node if it's appeared as an actual search result (leaf)
        var isSearchResult = (GLOBAL.PathToExpand.indexOf(key + ",") > -1);
        if (isSearchResult) {
            tcls += ' searchResultNode';
        }
        //And another to highlight if it's the default passed-in path (only do this once)
        var isDefaultPath = (GLOBAL.DefaultPathToExpand.indexOf(key) > -1);
        if (isDefaultPath) {
            tcls += ' defaultPathNode';
        }
        GLOBAL.DefaultPathToExpand = '';
        
        if(GLOBAL.PathToExpand.indexOf(key)>-1 && GLOBAL.UniqueLeaves.indexOf(key + ",")==-1) autoExpand=true;
        
        var expand=((contains(dseOpenedNodes, key)) || autoExpand) && (!contains(dseClosedNodes, key));
        
        // set the root node
        newnode = new Tree.AsyncTreeNode({
            text: name,
            draggable: draggable,
            leaf: leaf,
            id: key,
            comment: comment,
            qtip: tooltip,
            iconCls:iconCls,
            cls: tcls,
            level: level,  //extra attribute for storing level in hierarchy access through node.attributes.level
            dimcode: dimcode,
            tablename: tablename,
            normalunits: normalunits,
            oktousevalues: oktousevalues,
            expanded: expand,
            visualattributes : visualattributes,
            applied_path: applied_path,
               modifierId: modifierId,
               constraint_data_type : constraint_data_type
            });
            newnode.addListener('contextmenu',ontologyRightClick);
    return newnode;
    }

function climbTreeBuildName(baseNode)
{
    var nodeNameString = "\\";
    
    while(baseNode.parentNode != null)
    {
        if(baseNode.attributes.text  && baseNode.attributes.text != "root") nodeNameString = "\\" + baseNode.attributes.text + nodeNameString;
        
        baseNode = baseNode.parentNode;
    }
    
    return nodeNameString;
}


function toggleNodeDraggingState()
{
    if(STATE.Dragging==true)
    {
        STATE.Dragging=false;
        moveSelectedConceptFromHoldingToTarget();
    }
    
}

function nodeType(method, object)
{
    if(method=='visualattributes_modifier')
    {
        if(object.indexOf('MODIFIER_LEAF') != -1) 
        {
            return "LEAF"
        }
        else if(object.indexOf('MODIFIER_CONTAINER') != -1) 
        {
            return "CONTAINER"
        }
        else 
        {
            return ""
        }
    }
    
    if(method=='iconCls_modifier')
    {
        if(object == 'modifiericon') 
        {
            return "LEAF"
        }
        else if(object == 'modifierfoldericon') 
        {
            return "CONTAINER"
        }
        else 
        {
            return ""
        }
    }    
}

function createShortNameFromPath(pathToShorten)
{
    var splits=pathToShorten.split("\\");
    
    return splits[splits.length-1] || splits[splits.length-2];
}