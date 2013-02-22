
<g:select style="width: 400px" id="vendor" name="vendor" noSelection="${['null':'Select...']}" from="${vendors}" value="${vendor}"
			onchange="${remoteFunction(action:'ajaxTechnologies', update: 'technologywrapper', params:'\'vendorName=\' + this.value + \'&technologyName=\' + $F(\'technology\') + \'&measurementName=\' + $F(\'measurement\')' )};
					${remoteFunction(action:'ajaxMeasurements', update: 'measurementwrapper', params:'\'vendorName=\' + this.value + \'&technologyName=\' + $F(\'technology\') + \'&measurementName=\' + $F(\'measurement\')' )}"/>
