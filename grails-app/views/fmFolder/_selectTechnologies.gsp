
<g:select style="width: 400px" id="technology" name="technology" noSelection="${['null':'Select...']}" from="${technologies}" value="${technology}"
				onchange="${remoteFunction(action:'ajaxMeasurements', update: 'measurementwrapper', params:'\'technologyName=\' + this.value + \'&vendorName=\' + $F(\'vendor\') + \'&measurementName=\' + $F(\'measurement\')' )};
					${remoteFunction(action:'ajaxVendors', update: 'vendorwrapper', params:'\'technologyName=\' + this.value + \'&vendorName=\' + $F(\'vendor\') + \'&measurementName=\' + $F(\'measurement\')' )}"/>