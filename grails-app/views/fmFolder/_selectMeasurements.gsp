${measurement}
	<g:select style="width: 400px" id="measurement" name="measurement" noSelection="${['null':'Select...']}" from="${measurements}" value="${measurement}"
			onchange="${remoteFunction(action:'ajaxTechnologies', update: 'technologywrapper', params:'\'measurementName=\' + this.value + \'&technologyName=\' + $F(\'technology\') + \'&vendorName=\' + $F(\'vendor\')' )};
					${remoteFunction(action:'ajaxVendors', update: 'vendorwrapper', params:'\'measurementName=\' + this.value + \'&technologyName=\' + $F(\'technology\') + \'&vendorName=\' + $F(\'vendor\')' )}"/>
