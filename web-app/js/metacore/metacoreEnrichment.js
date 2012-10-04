function registerMetaCoreEnrichmentDragAndDrop()
{
	//Set up drag and drop for Dependent and Independent variables on the data association tab.
	//Get the Independent DIV
	var independentDiv = Ext.get("divIndependentVariableMetaCoreEnrichment");
	dtgI = new Ext.dd.DropTarget(independentDiv,{ddGroup : 'makeQuery'});
	dtgI.notifyDrop = dropOntoCategorySelection;
} 