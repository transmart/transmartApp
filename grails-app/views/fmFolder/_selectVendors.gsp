<g:setProvider library="prototype"/>
<g:select style="width: 400px" id="vendor" name="vendor"
          noSelection="${['null': 'Select...']}" from="${vendors}"
          value="${vendor}"
          onchange="${remoteFunction(action: 'ajaxTechnologies', update: 'technologywrapper', onSuccess: 'updatePlatforms()', params: '\'vendorName=\' + this.value + \'&technologyName=\' + $F(\'technology\') + \'&measurementName=\' + $F(\'measurement\')')};
                    ${remoteFunction(action: 'ajaxMeasurements', update: 'measurementwrapper', onSuccess: 'updatePlatforms()', params: '\'vendorName=\' + this.value + \'&technologyName=\' + $F(\'technology\') + \'&measurementName=\' + $F(\'measurement\')')}"/>
