<g:select style="width: 400px" id="vendor" name="vendor" noSelection="${['null':'Select...']}" from="${vendors}" 
			onchange="${remoteFunction(action:'ajaxTechnologies', update: 'technology', params:'\'vendorName=\' + this.value' )};
					${remoteFunction(action:'ajaxMeasurements', update: 'measurement', params:'\'vendorName=\' + this.value' )}"/>