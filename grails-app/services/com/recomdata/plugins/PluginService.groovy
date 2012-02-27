/*************************************************************************
  * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
package com.recomdata.plugins

import com.recomdata.plugins.PluginDescriptor;

class PluginService {

	def grailsApplication
	
    boolean transactional = true

    def Map register() {
		
		//Get the location of the R Scripts from the config file.
		String pluginScriptDirectory = grailsApplication.config.com.recomdata.plugins.pluginScriptDirectory
		String tempFolderDirectory = grailsApplication.config.com.recomdata.plugins.tempFolderDirectory
		
		Map pluginsMap = new HashMap<String,PluginDescriptor>();
		PluginDescriptor plugin = registerPlugin(
			[
				"R":
					[
						"source('${pluginScriptDirectory}LineGraph"+File.separator+"BuildLinearData.R')",
						"LinearData.build(input.dataFile = '||TEMPFOLDERDIRECTORY||Clinical"+File.separator+"clinical.i2b2trans',"+
							"concept.dependent='||DEPENDENT||', concept.group='||GROUPBY||')"
					]
			],
			[
					"R":
						[
							"source('${pluginScriptDirectory}LineGraph"+File.separator+"LineGraphLoader.r')",
							"LineGraph.loader(input.filename='outputfile.txt', graphType='||GRAPHTYPE||')"
						]
			],
			"LineGraph",
			[
				"CLINICAL"
			],
			"Line Graph",
			"lineGraph",
			[
				"GSP":"/lineGraph/lineGraphOutput"
			],
			[
				"||DEPENDENT||":"dependentVariable",
				"||INDEPENDENT||":"independentVariable",
				"||GROUPBY||":"groupByVariable",
				"||GRAPHTYPE||":"graphType"
			],false);	
		pluginsMap.put(plugin.getId(), plugin)
		
		//Correlation Analysis
		plugin = registerPlugin(
			[
				"R":
					[
						"source('${pluginScriptDirectory}Correlation"+File.separator+"BuildCorrelationData.R')",
						"CorrelationData.build(input.dataFile = '||TEMPFOLDERDIRECTORY||Clinical"+File.separator+"clinical.i2b2trans',"+
							"concept.variables='||CURRENTVARIABLES||',correlation.by='||CORRELATIONBY||')"
					]
			],
			[
					"R":
						[
							"source('${pluginScriptDirectory}Correlation"+File.separator+"CorrelationLoader.r')",
							"Correlation.loader(input.filename='outputfile.txt',correlation.by='||CORRELATIONBY||',correlation.method='||CORRELATIONTYPE||')"
						]
			],
			"CorrelationAnalysis",
			[
				"CLINICAL"
			],
			"Correlation Analysis",
			"correlationAnalysis",
			[
				"GSP":"/correlationAnalysis/correlationAnalysisOutput"
			],
			[
				"||CURRENTVARIABLES||":"variablesConceptPaths",
				"||CORRELATIONBY||":"correlationBy",
				"||CORRELATIONTYPE||":"correlationType"
			],false);
		pluginsMap.put(plugin.getId(), plugin)
		
		//Scatter Plot
		plugin = registerPlugin(
			[
				"R":
					[
						"source('${pluginScriptDirectory}ScatterPlot"+File.separator+"BuildScatterData.R')",
						"ScatterData.build(input.dataFile = '||TEMPFOLDERDIRECTORY||Clinical"+File.separator+"clinical.i2b2trans',concept.dependent='||DEPENDENT||',concept.independent='||INDEPENDENT||')"
					]
			],
			[
					"R":
						[
							"source('${pluginScriptDirectory}ScatterPlot"+File.separator+"ScatterPlotLoader.R')",
							"ScatterPlot.loader(input.filename='outputfile.txt',concept.dependent='||DEPENDENT||',concept.independent='||INDEPENDENT||')"
						]
				],
			"ScatterPlot",
			[
				"CLINICAL"
			],
			"Scatter Plot with Linear Regression",
			"scatterPlot",
			[
				"GSP":"/scatterPlot/scatterPlotOut"
			],
			[
				"||DEPENDENT||":"dependentVariable",
				"||INDEPENDENT||":"independentVariable"
			],
			false)
		pluginsMap.put(plugin.getId(), plugin)
		
		return pluginsMap;
    }
	
	def PluginDescriptor registerPlugin(convertor, processor, view, dataTypes, name, id,renderer,variableMapping,pivotData){
		PluginDescriptor plugin = new PluginDescriptor();
		plugin.setConverter(convertor);
		plugin.setProcessor(processor);
		plugin.setView(view);
		plugin.setDataTypes(dataTypes);
		plugin.setName(name);
		plugin.setId(id);
		plugin.setRenderer(renderer)
		plugin.setVariableMapping(variableMapping);
		plugin.setPivotData(pivotData);
		return plugin
	}
}
