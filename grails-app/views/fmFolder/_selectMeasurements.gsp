	<g:select style="width: 400px" id="measurement" name="measurement" noSelection="${['null':'Select...']}" from="${measurements}"
			onchange="${remoteFunction(action:'ajaxTechnologies', update: 'technologywrapper', params:'\'measurementName=\' + this.value' )};
					${remoteFunction(action:'ajaxVendors', update: 'vendorwrapper', params:'\'measurementName=\' + this.value' )}"/>
