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
  

package com.recomdata.transmart.plugin

import grails.test.*

import org.junit.Ignore

class PluginModuleControllerTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

//	@Ignore // JIRA: THRONE-106
    void testUpdate() {
		def pmc = new PluginModuleController()
		pmc.params.id = 3
		pmc.params.formPage = 'BoxPlot'
		//Note: See ANOVAData.build in the below string ... JSON.parse is throwing errors if there are new-line chars within ""
		pmc.params.paramsStr = """
		{
		"id":"boxPlot",
		"converter":{
			"R":[
				"source('||PLUGINSCRIPTDIRECTORY||Common/dataBuilders.R')",
				"source('||PLUGINSCRIPTDIRECTORY||Common/ExtractConcepts.R')",
				"source('||PLUGINSCRIPTDIRECTORY||Common/collapsingData.R')",
				"source('||PLUGINSCRIPTDIRECTORY||Common/BinData.R')",
				"source('||PLUGINSCRIPTDIRECTORY||ANOVA/BuildANOVAData.R')",
				"ANOVAData.build(input.dataFile = '||TEMPFOLDERDIRECTORY||Clinical/clinical.i2b2trans',concept.dependent='||DEPENDENT||',concept.independent='||INDEPENDENT||',binning.enabled='||BINNING||',binning.bins='||NUMBERBINS||',binning.type='||BINNINGTYPE||',binning.manual='||BINNINGMANUAL||',binning.binrangestring='||BINNINGRANGESTRING||',binning.variabletype='||BINNINGVARIABLETYPE||',binning.variable='||BINNINGVARIABLE||',flipimage=||FLIPIMAGE||,input.gexFile = '||TEMPFOLDERDIRECTORY||mRNA/Processed_Data/mRNA.trans',input.snpFile = '||TEMPFOLDERDIRECTORY||SNP/snp.trans',concept.dependent.type = '||TYPEDEP||',concept.independent.type = '||TYPEIND||',genes.dependent = '||GENESDEP||',genes.independent = '||GENESIND||',genes.dependent.aggregate = '||AGGREGATEDEP||',genes.independent.aggregate = '||AGGREGATEIND||',sample.dependent = '||SAMPLEDEP||',sample.independent = '||SAMPLEIND||',time.dependent = '||TIMEPOINTSDEP||',time.independent = '||TIMEPOINTIND||',snptype.dependent = '||SNPTYPEDEP||',snptype.independent = '||SNPTYPEIND||')"
				]
			},
		"name":"Box Plot with ANOVA",
		"dataFileInputMapping":{"MRNA.TXT":"mrnaData","CLINICAL.TXT":"TRUE","SNP.TXT":"snpData"},
		"dataTypes":{"subset1":["CLINICAL.TXT"]},
		"pivotData":false,
		"view":"BoxPlot",
		"processor":{
			"R":[
				"source('||PLUGINSCRIPTDIRECTORY||ANOVA/BoxPlotLoader.R')",
				"BoxPlot.loader(input.filename='outputfile',concept.dependent='||DEPENDENT||',concept.independent='||INDEPENDENT||',flipimage=||FLIPIMAGE||,concept.dependent.type = '||TYPEDEP||',concept.independent.type = '||TYPEIND||',genes.dependent = '||GENESDEPNAME||',genes.independent = '||GENESINDNAME||',binning.enabled='||BINNING||',binning.variable='||BINNINGVARIABLE||')"
				]
			},
		"renderer":{"GSP":"/boxPlot/boxPlotOut"},
		"variableMapping":{
			"||AGGREGATEIND||":"divIndependentVariableprobesAggregation",
			"||FLIPIMAGE||":"flipImage",
			"||BINNING||":"binning",
			"||GPLDEP||":"divDependentVariablegpls",
			"||BINNINGTYPE||":"binDistribution",
			"||TIMEPOINTSDEP||":"divDependentVariabletimepoints",
			"||BINNINGRANGESTRING||":"binRanges",
			"||RBMPANELDEP||":"divDependentVariablerbmPanels",
			"||TYPEIND||":"divIndependentVariableType",
			"||TYPEDEP||":"divDependentVariableType",
			"||SNPTYPEIND||":"divIndependentVariableprobesAggregation",
			"||GPLIND||":"divIndependentVariablegpls",
			"||RBMPANELIND||":"divIndependentVariablerbmPanels",
			"||SNPTYPEDEP||":"divDependentVariableSNPType",
			"||TIMEPOINTIND||":"divIndependentVariabletimepoints",
			"||DEPENDENT||":"dependentVariable",
			"||GENESDEPNAME||":"divDependentPathwayName",
			"||NUMBERBINS||":"numberOfBins",
			"||PLATFORMDEP||":"divDependentVariableplatforms",
			"||GENESIND||":"divIndependentVariablePathway",
			"||BINNINGVARIABLE||":"binVariable",
			"||SAMPLEIND||":"divIndependentVariablesamples",
			"||TISSUEDEP||":"divDependentVariabletissues",
			"||GENESDEP||":"divDependentVariablePathway",
			"||PLATFORMIND||":"divIndependentVariableplatforms",
			"||AGGREGATEDEP||":"divDependentVariableprobesAggregation",
			"||BINNINGVARIABLETYPE||":"variableType",
			"||SAMPLEDEP||":"divDependentVariablesamples",
			"||TISSUEIND||":"divIndependentVariabletissues",
			"||INDEPENDENT||":"independentVariable",
			"||GENESINDNAME||":"divIndependentPathwayName",
			"||BINNINGMANUAL||":"manualBinning"
			}
		}
		"""
		pmc.update()
		//println 'Render :: '+pmc.view
		println 'Message :: '+pmc.flash?.message
		
		def model = pmc.show()
		println model.paramsStr
    }
}
