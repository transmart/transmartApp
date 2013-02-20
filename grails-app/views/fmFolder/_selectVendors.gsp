<div id="vendorwrapper">
<g:select style="width: 400px" id="vendor" name="vendor" noSelection="${['null':'Select...']}" from="${vendors}" 
			onchange="${remoteFunction(action:'ajaxTechnologies', update: 'technologywrapper', params:'\'vendorName=\' + this.value' )};
					${remoteFunction(action:'ajaxMeasurements', update: 'measurementwrapper', params:'\'vendorName=\' + this.value' )}"/>
</div>