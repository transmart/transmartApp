
<g:select style="width: 400px" id="technology" name="technology" noSelection="${['null':'Select...']}" from="${technologies}" 
				onchange="${remoteFunction(action:'ajaxMeasurements', update: 'measurementwrapper', params:'\'technologyName=\' + this.value' )};
					${remoteFunction(action:'ajaxVendors', update: 'vendorwrapper', params:'\'technologyName=\' + this.value' )}"/>