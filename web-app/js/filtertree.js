// NOTE: This function relies on methods in ext/checkboxtree.js

function showTrialFilterTree(url) {

	var loader = new Ext.tree.TreeLoader({
		clearOnLoad: true,
		baseAttrs: { checked: true, iconCls: "blankNode" },
		dataUrl: url,
		preloadChildren: true
	});

	var tree = new Ext.tree.TreePanel({
		id:'trialfilter-tree',
		border:false,
		autoScroll: true,
		animate: true,
		lines: true,
		enableDD: false,
		containerScroll: true,
		rootVisible:false,
		loader: loader,
		root: {
			text: "Root",
			nodeType: "async",
			draggable: false,
			expanded: true,
			id: "source",
			checked: false
		}
	});

	tree.expandAll();
	tree.addListener('checkchange',treeCheckChange);
	tree.render("trialfilter-div");

}

function treeCheckChange(node, checked)	{
	node.getOwnerTree().suspendEvents();
	processTreeCheckChange(node,checked);
	node.getOwnerTree().resumeEvents();
}

function processTreeCheckChange(node,checked) {
	node.ui.toggleCheck(checked);
	if(!node.leaf) //if folder
		{
		for(var c=0; c<node.childNodes.length; c++)
			processTreeCheckChange(node.childNodes[c], checked);
		}
}

function getChecked(treeid) {
	return Ext.ComponentMgr.get(treeid + "-tree").getChecked("id");
}

function submitChecked(treeid) {
	  var etree = Ext.ComponentMgr.get(treeid + "-tree");
	  var etreeform = document.getElementById(treeid + "-form");
	  etreeform.checked.value = etree.getChecked("id");
	  etreeform.submit();
}

