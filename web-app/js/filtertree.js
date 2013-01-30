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

