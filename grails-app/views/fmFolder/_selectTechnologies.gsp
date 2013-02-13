<g:select style="width: 400px" id="technology" name="technology" noSelection="${['null':'Select...']}" from="${technologies}" 
				onchange="${remoteFunction(action:'ajaxMeasurements', update: 'measurement', params:'\'technologyName=\' + this.value' )};
					${remoteFunction(action:'ajaxVendors', update: 'vendor', params:'\'technologyName=\' + this.value' )}"/>