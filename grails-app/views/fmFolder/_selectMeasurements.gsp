	<g:select style="width: 400px" id="measurement" name="measurement" noSelection="${['null':'Select...']}" from="${measurements}"
			onchange="${remoteFunction(action:'ajaxTechnologies', update: 'technology', params:'\'measurementName=\' + this.value' )};
					${remoteFunction(action:'ajaxVendors', update: 'vendor', params:'\'measurementName=\' + this.value' )}"/>
