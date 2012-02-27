<div style="background-color:white;height:100%;width:100%;">
	<br />
	<br />
	<font style="font: 12px tahoma,verdana,helvetica;color:#800080;font-weight:bold;">Recent Updates (click update for more info)</font>
	<br />
	<hr />
	<br />
	
	<g:each var="newsUpdate" in="${newsUpdates}" status="iterator">
	
		<a href="#" onClick="showNewsUpdateDetail('${newsUpdate.id}')">
			Data Set <i>${newsUpdate.dataSetName}</i> modified on <i><g:formatDate format="yyyy-MM-dd" date="${newsUpdate.updateDate}"></g:formatDate></i>  
		</a>
		
		<br />
		<br />
		
	</g:each>
	
	<br />
	<br />

	
</div>