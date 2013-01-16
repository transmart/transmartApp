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
  

/**
 * $Id$
 *
 * Extended ComboBoz which handles selection of search terms.
 */
Ext.app.SearchComboBox = Ext.extend(Ext.form.ComboBox, {
	id: "",
	searchUrl: "",
	submitUrl: "",
    width: 470,
    hideTrigger: true,

    constructor: function(config) {
		config = config || {};
		config.listeners = config.listeners || {};
		Ext.applyIf(config.listeners, {
			"specialkey":  {
				fn: function(combo, eventObject) {
					if (eventObject.getKey() == Ext.EventObject.ENTER) {
						var param = combo.getSelectedParam();
						if (param != null) {
							if (combo.submitFn != null) {
								combo.submitFn(param, param); 
							} else {
								window.location = String.format(combo.submitUrl + "?id={0}", param);
							}
						}
					}
				},
				scope: this
			}
		});
		Ext.app.SearchComboBox.superclass.constructor.apply(this, arguments);
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

		var searchStore = new Ext.data.Store({
			proxy: new Ext.data.ScriptTagProxy({ url: this.initialConfig.searchUrl }),
			reader: reader,
			listeners: {
				"load" : {
					fn: function(obj, records, options) {
						if (records.length > 0) {
							this.select(-1);
						}
					},
					scope: this
				}
			}		
		});

		var config = {
	        allowBlank: true,
	        displayField: "title",
	        forceSelection: false,
	        itemSelector: "div.search-item",
	        loadingText: "Searching...",
	        minChars: 1,
	        mode: "remote",
	        shadow: "drop",
	        store: searchStore,
	        tpl: new Ext.XTemplate(
	        	'<tpl for=".">',
	        		'<div class="search-item">',
	        			'<p>',
	        				'<span class="category-{display:lowercase}">{display}&gt;{source}</span>&nbsp;',
	        				'<b>{keyword}</b>&nbsp; {synonyms}',
	        			'</p>',
	        		'</div>',
	        	'</tpl>'
		    ),
		    triggerClass: "x-form-add-trigger",
	        typeAhead: false,
	        valueField: "id"
		};
		Ext.apply(this, config);
		Ext.apply(this.initialConfig, config);
		Ext.app.SearchComboBox.superclass.initComponent.apply(this);
	},

	getSelectedParam: function() {
		var value = this.getRawValue().replace(/^\s+|\s+$/g, '');
		value = value.replace(/,+/g, ' ');
		return value;
	},

	getSelectedRecord: function() {
		var value = this.getRawValue().replace(/^\s+|\s+$/g, '');
		value = value.replace(/,+/g, ' ');
		var record = null;
		if (value != "") {
			record = {id:-1, source:"", keyword:value, synonyms:"", category:"TEXT", display:"Text"};
		}
		return record;
	}

});