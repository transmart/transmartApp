Ext.tree.CheckboxUI = Ext.extend(Ext.tree.TreeNodeUI, {
	renderElements : function(n, a, targetNode, bulkRender){
        // add some indent caching, this helps performance when rendering a large tree
        this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent() : '';

        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node"><div ext:tree-node-id="',n.id,'" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<span class="x-tree-node-indent">',this.indentMarkup,"</span>",
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" />',
             '<span></span>',
            cb ? ('<input class="x-tree-node-cb" type="checkbox" ' + (a.checked ? 'checked="checked" />' : '/>')) : '',
            '<a hidefocus="on" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a></div>",
            '<ul class="x-tree-node-ct" style="display:none;"></ul>',
            "</li>"].join('');

        var nel;
        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }

        this.elNode = this.wrap.childNodes[0];
        this.ctNode = this.wrap.childNodes[1];
        var cs = this.elNode.childNodes;
        this.indentNode = cs[0];
        this.ecNode = cs[1];
        this.iconNode = cs[2];
        var index = 3;
        if(cb){
            this.checkbox = cs[3];
			// fix for IE6
			this.checkbox.defaultChecked = this.checkbox.checked;
            index++;
        }
        this.anchor = cs[index];
        this.textNode = cs[index].firstChild;
    }

});


function createTree(treeid, dataURL) {

	  var treediv = treeid+"-div";
		var root = new Ext.tree.AsyncTreeNode({
			text: 'Root',
			draggable:false,
			id:'source'
			});

		// construct treepanel with root node (and without loader)
	  var tree = new Ext.tree.TreePanel({
			id:treeid,
			renderTo:treediv,
			xtype:'treepanel',
			autoScroll:true,
			border:false,
			rootVisible:false,
			root:root,
		loader: (new Ext.tree.TreeLoader({dataUrl:dataURL, requestMethod : "GET",preloadChildren:true}))
		});
		   root.expand();
		   tree.addListener('checkchange',treeCheckChange);
		  // Ext.ComponentMgr.register(tree);
		   return tree;
		}


function createGlobalFilterTree(treeid, dataURL) {
	// set the root node
	var root = new Ext.tree.AsyncTreeNode({
	text: 'Root',
	draggable:false,
	id:'source'
	});

	// construct treepanel with root node (and without loader)
	var tree = new Ext.tree.TreePanel({
		id:treeid,
		region:'west',
	    title:'Global Filter',
	    split:true,
	    width: 200,
		xtype:'treepanel',
		autoScroll:true,
		border:false,
		rootVisible:false,
		root:root,
		collapsible:true,
		collapsed:true,
	    margins:'0 0 5 5',
	    cmargins:'0 5 5 5',
		loader: (new Ext.tree.TreeLoader({dataUrl:dataURL,requestMethod : "GET"}))
	});
	   root.expand();
	   tree.addListener('checkchange',treeCheckChange);
	//   Ext.ComponentMgr.register(tree);
	   return tree;
	}


	function treeCheckChange(node, checked)
	{
	node.getOwnerTree().suspendEvents();
	processTreeCheckChange(node,checked);
	node.getOwnerTree().resumeEvents();
	}


	function processTreeCheckChange(node,checked)
	{
	node.ui.toggleCheck(checked);
	if(!node.leaf) //if folder
		{
		for(var c=0; c<node.childNodes.length; c++)
			processTreeCheckChange(node.childNodes[c], checked);
		}
	}
	function getChecked(treeid)
	{
	return Ext.ComponentMgr.get(treeid).getChecked("id");
	}


	function submitChecked(treeid){

	  var etree = Ext.ComponentMgr.get(treeid);
	  var etreeform = document.getElementById(treeid+'form');
	  etreeform.checked.value = etree.getChecked("id");
	  etreeform.submit();

  }

