/**
 * $Id$
 *
 * Extended Panel which displays horizontal pick list.
 */
Ext.app.PickList = Ext.extend(Ext.Panel, {

	initComponent: function() {
		var reader = new Ext.data.JsonReader(
			{ root: "rows", id: "value" },
			[
				{name: "value"},
				{name: "label"}
			]
		);
		
		var store = new Ext.data.Store({
			autoLoad: true,
			proxy: new Ext.data.ScriptTagProxy({ url: this.initialConfig.storeUrl }),
			reader: reader,
			listeners: {
				"load" : {
					fn: function(obj, records, options) {
						this.view.select(0);
					},
					scope: this
				}
			}			
		});

		var tpl = new Ext.XTemplate(
			'<tpl for="."><tpl if="xindex &gt; 1"> | </tpl><span class="x-view-item">{label}</span></tpl>'
		);
		
		var label = new Ext.Panel({
			id: this.initialConfig.id + "-label",
			border: false,
			autoHeight: true,
			width: 62,
			html: this.initialConfig.label
		});
		
		var view = new Ext.DataView({
			id: this.initialConfig.id + "-view",
			store: store,
			tpl: tpl,
			singleSelect: true,
			autoHeight: true,
			columnWidth: 1.0,
			emptyText: "No categories to display",
			itemSelector: "span.x-view-item",
			overClass: "x-view-over",
			listeners: {
			    "click": {
				    fn: function(obj, index, node, e) {
						this.onSelect(obj.store.getAt(index));
					},
					scope: this
				},
	    		"containerclick": {
					fn: function(obj, event) {
						return false; // prevent deselection of items
					},
					scope: this
				}
			}
		});
		
		var config = {
			border: false,
			autoHeight: true,
			layout: "column",
			view: view,
			items: [ label, view ]
		};
		
		Ext.apply(this, config);
		Ext.apply(this.initialConfig, config);
		Ext.app.PickList.superclass.initComponent.apply(this);
	},
	
	onSelect: function(value) {
	},
	
	getSelectedRecord: function() {
		var recs = this.view.getSelectedRecords();
		if (recs.length > 0) {
			return recs[0];
		}
		return null;
	}

});