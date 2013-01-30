function createAutoSearchBox(ajaxurl, searchUrl, boxwidth){
	var ds =new Ext.data.Store({
		proxy: new Ext.data.ScriptTagProxy({
			url: ajaxurl
			}),
			reader: new Ext.data.JsonReader({
			root: 'rows',
			id: 'gid'
			}, [
			{name: 'gid'},
			{name: 'source'},
			{name: 'name'},
			{name: 'synonyms'},
			{name: 'type'}
			])
			});
	 // Custom rendering Template
    var resultTpl = new Ext.XTemplate(
        '<tpl for="."><div class="search-item">',
            '<p><span>{type}>{source}</span>&nbsp;<b>{name}</b>&nbsp; {synonyms}</p>',
        '</div></tpl>'
    );
    var search = new Ext.form.ComboBox({
        store: ds,
        displayField:'title',
        typeAhead: false,
        loadingText: 'Searching...',
        width: boxwidth,
        valueField:'gid',
        hideTrigger:true,
        forceSelection:false,
        allowBlank:false,
        name:'searchText',
        mode:'remote',
        tpl: resultTpl,
        minChars:1,
        shadow:'drop',
        applyTo: 'search',
        itemSelector: 'div.search-item',
        onSelect: function(record){ // override default onSelect to do redirect
    	    search.collapse();
            window.location =
                String.format(searchUrl+'?searchText={0}', record.data.gid);
        }
    });

}
