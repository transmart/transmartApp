/*
 * $Id$
 *
 * Ext.app.EditFiltersWindow extends Ext.Window to implement
 * a window for editing search filters.
 */

Ext.app.EditFiltersWindow = Ext.extend(Ext.Window, {

	id: "",
	loadIds: "",
	loadUrl: "",
	splitUrl: "",
	searchUrl: "",
	submitUrl: "",
	categoriesUrl: "",

  	constructor: function(config) {
		config = config || {};
		config.listeners = config.listeners || {};
		Ext.applyIf(config.listeners, {
			"show":  {
				fn: function(obj) {
					this.summaryPanel.body.dom.innerHTML = '<div class="loading-indicator">Loading...</div>';
					this.loadStore.load();
				},
				scope: this
			}
		});
		Ext.app.EditFiltersWindow.superclass.constructor.apply(this, arguments);
	},

	initComponent: function() {
		var reader = new Ext.data.JsonReader({
			root: "rows", id: "id"},
			[
			 	{name: "id"},
			 	{name: "source"},
			 	{name: "keyword"},
			 	{name: "synonyms"},
				{name: "category"},
				{name: "display"}
			]
		);

		var loadStore = new Ext.data.Store({
			proxy: new Ext.data.ScriptTagProxy({ url: this.initialConfig.loadUrl }),
			reader: reader,
			listeners: {
				"load" : {
					fn: function(obj, records, options) {
						this.filters = new Array();
						for (var j = 0; j < records.length; j++) {
							this.addFilter(records[j].data);
						}
						this.pickList.view.select(0);
					},
					scope: this
				}
			},
	        windowId: this.initialConfig.id
		});

		var splitStore = new Ext.data.Store({
			proxy: new Ext.data.ScriptTagProxy({ url: this.initialConfig.splitUrl }),
			reader: reader,
			listeners: {
				"load" : {
					fn: function(obj, records, options) {
						for (var j = 0; j < records.length; j++) {
							this.addFilter(records[j].data);
						}
					},
					scope: this
				}
			},
	        windowId: this.initialConfig.id
		});

		var searchStore = new Ext.data.Store({
			proxy: new Ext.data.ScriptTagProxy({ url: this.initialConfig.searchUrl }),
			reader: reader
		});

		var searchCombo = new Ext.app.SearchComboBox({
			hideTrigger: false,
			onSelect: function(record) {
				this.collapse();
				var win = Ext.getCmp(this.windowId);
				win.addFilter(record.data);
				this.clearValue();
			},
			onTriggerClick: function() {
				var record = this.getSelectedRecord();
				if (record != null) {
					this.collapse();
					var win = Ext.getCmp(this.windowId);
					win.addFilter(record);
					this.clearValue();
				}
			},
			searchUrl: this.initialConfig.searchUrl,
			submitUrl: "",
	        x: 0,
	        y: 18,
	        anchor: "100% 100%",
	        windowId: this.initialConfig.id,
	        pickList: pickList,
	        listeners: {
				"beforequery": {
					fn: function(queryEvent) {
			            this.pickList;
			            var rec = this.pickList.getSelectedRecord();
						if (rec != null) {
							queryEvent.query = rec.id + ":" + queryEvent.query;
						}
					},
					scope: this
				}
	        }	        
	    });

		var summaryPanel = new Ext.Panel({
	    	autoScroll: true,
	    	html: "",
	    	x: 0,
	    	y: 42,
	    	anchor: "100% 100%"
	    });
		
		var pickList = new Ext.app.PickList({
			id: "editcategories",
			storeUrl: this.initialConfig.categoriesUrl,
			label: "Category:&nbsp;",
			disabledClass: "picklist-disabled",
			combo: searchCombo,
			x: 0,
			y: 0,
			anchor: "100% 100%",
			onSelect: function(record) {
		        this.combo.focus();
		        if ((record.id != "all") || (record.id == "all" && this.combo.getRawValue().length > 0)) {
					this.combo.doQuery(this.combo.getRawValue(), true);
		        }
			}
		});

		var config = {
			// Ext.Window properties
			buttons: [{
				text: "Apply",
				handler: function() {
					var win = Ext.getCmp(this.windowId);
					var params = Ext.urlEncode({ids: win.formatIds(), texts: win.formatTexts()});
					win.hide();
					window.location = String.format(win.submitUrl + "?{0}", params);
				},
		        windowId: this.initialConfig.id
			}, {
				text: "Cancel",
				handler: function() {
					var win = Ext.getCmp(this.windowId);
					win.hide();
				},
		        windowId: this.initialConfig.id
			}],
			closeAction: "hide",
			height: 350,
			items: [ pickList, searchCombo, summaryPanel ],
			layout: "absolute",
			title: "Edit Filters",
			width: 510,

			// Ext.app.EditFiltersWindow properties
			filters: new Array(),
			searchCombo: searchCombo,
		    summaryPanel: summaryPanel,
		    pickList: pickList,
		    loadStore: loadStore,
		    splitStore: splitStore,
	        tools:[{
				id:'help',
				qtip:'Click for context sensitive help',
			    handler: function(event, toolEl, panel){
			    	D2H_ShowHelp("1020",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
			    }
	        }]
		};
		Ext.apply(this, config);
		Ext.apply(this.initialConfig, config);
		Ext.app.EditFiltersWindow.superclass.initComponent.apply(this);
	},

	indexOfFilter: function(record) {
		for (var i = 0; i < this.filters.length; i++) {
			var filter = this.filters[i];
			if (record.id != -1 && filter.id != -1 && record.id == filter.id) {
				return i;
			}
			if (record.id == -1 && filter.id == -1 && record.keyword == filter.keyword) {
				return i;
			}
		}
		return -1;
	},

	addFilter: function(record) {
		if (this.indexOfFilter(record) < 0) {
			this.filters.push(record);
    		this.renderSummary();
		}
	},

	removeFilter: function(record) {
		var index = this.indexOfFilter(record);
		if (index >= 0) {
			this.filters.splice(index, 1);
			this.renderSummary();
		}
	},

	splitFilter: function(record) {
		var index = this.indexOfFilter(record);
		if (index >= 0) {
			this.filters.splice(index, 1);
			this.summaryPanel.body.dom.innerHTML = '<div class="loading-indicator">Loading...</div>';
		}
		this.splitStore.load({ params: { id: record.id }});
	},

	renderSummary: function() {
		var values = new Object();
		var html = "";

		for (var i = 0; i < this.filters.length; i++) {
			var record = this.filters[i];

			if (values[record.category] == undefined) {
				values[record.category] = this.formatValue(record);
			} else {
				values[record.category] += " OR " + this.formatValue(record);
			}
		}

		if (values["GENE"] !== undefined) {
			html += this.formatSection("Gene", values["GENE"]);
		}

		if (values["PATHWAY"] !== undefined) {
			if (html.length > 0) {
				html += this.formatDivider("OR");
			}
			html += this.formatSection("Pathway", values["PATHWAY"]);
		}

		if (values["GENESIG"] !== undefined) {
			if (html.length > 0) {
				html += this.formatDivider("OR");
			}
			html += this.formatSection("Gene Signature", values["GENESIG"]);
		}

		if (values["GENELIST"] !== undefined) {
			if (html.length > 0) {
				html += this.formatDivider("OR");
			}
			html += this.formatSection("Gene List", values["GENELIST"]);
		}
		if (values["COMPOUND"] !== undefined) {
			if (html.length > 0) {
				html += this.formatDivider("AND");
			}
			html += this.formatSection("Compound", values["COMPOUND"]);
		}

		if (values["DISEASE"] !== undefined) {
			if (html.length > 0) {
				html += this.formatDivider("AND");
			}
			html += this.formatSection("Disease", values["DISEASE"]);
		}

		if (values["TRIAL"] !== undefined) {
			if (html.length > 0) {
				html += this.formatDivider("AND");
			}
			html += this.formatSection("Trial", values["TRIAL"]);
		}

		if (values["STUDY"] !== undefined) {
			if (html.length > 0) {
				html += this.formatDivider("AND");
			}
			html += this.formatSection("Study", values["STUDY"]);
		}

		if (values["TEXT"] !== undefined) {
			if (html.length > 0) {
				html += this.formatDivider("AND");
			}
			html += this.formatSection("Text", values["TEXT"]);
		}

		html = '<table class="search" width="100%">' + html + '</table>';
		this.summaryPanel.body.dom.innerHTML = html;
	},

	formatValue: function(record) {
		var label = record.keyword;
		
		//Escape any single quotes that are present because we use this value in a JS call.
		var escapedKeyword = record.keyword.replace("'","\\'");
		
		if (record.category == "TEXT") {
			label = "\"" + label + "\"";
		} else if (record.category == "PATHWAY") {
			if (record.source != '') {
				label = record.source.replace(">", "") + "-" + label;
			}
		}
		var cssClass = "filter-item filter-item-" + record.category.toLowerCase();
		var html = '<nobr><span class="' + cssClass + '">' + label + '</span>';
		if (record.category == "PATHWAY") {
			html += '<a href="#" class="filter-item filter-item-split" onclick="' +
				"var win=Ext.getCmp('" + this.id + "'); win.splitFilter({id:" + record.id + ",keyword:'" + escapedKeyword + "'});" +
				'"><img alt="split" src="../images/split.png" /></a>';
		}
		html += '<a href="#" class="filter-item filter-item-remove" onclick="' +
			"var win=Ext.getCmp('" + this.id + "'); win.removeFilter({id:" + record.id + ",keyword:'" + escapedKeyword + "'});" +
			'"><img alt="remove" src="../images/remove.png" /></a></nobr>';
		return html;
	},

	formatSection: function(category, valuesHTML) {
		var cssClass = "filter-item filter-item-" + category.toLowerCase();
		return '<tr><td class="' + cssClass + '" width="75px">' + category + '&gt;</td> ' +
			'<td class="filter-item">' + valuesHTML + '</td></tr>'
	},

	formatDivider: function(label) {
		var cssClass = "filter-divider filter-divider-" + label.toLowerCase();
		return '<tr><td colspan="2"><div class="' + cssClass + '"><span><p>' + label + '</p></span></div></td></tr>';
	},

	formatIds: function() {
		var ids = "";
		for (var i = 0; i < this.filters.length; i++) {
			var record = this.filters[i];
			if (record.id != -1) {
				if (ids != "") {
					ids += ",";
				}
				ids += record.id;
			}
		}
		return ids;
	},

	formatTexts: function() {
		var texts = "";
		for (var i = 0; i < this.filters.length; i++) {
			var record = this.filters[i];
			if (record.id == -1) {
				if (texts != "") {
					texts += ",";
				}
				texts += record.keyword;
			}
		}
		return texts;
	}


});