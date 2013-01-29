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
  

package com.recomdata.util;

import static org.junit.Assert.*;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.recomdata.dataexport.util.ExportUtil;

class RandomTest extends GroovyTestCase {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	void testConceptPath() {
		/*def conceptPathWithoutSampleInfo = ''
		def conceptPath = '\\Public Studies\\Acute_Physiologic_Hyperinsulinemia_Coletta_GSE9105\\Biomarker Data\\Gene Expression\\Affymetrix GeneChip Human Genome U133 Array Set HGU133A\\Vastus Lateralis Muscle\\030 Minutes after Insulin Infusion\\'
		//def conceptPath = '\\Public Studies\\XDP_Tamiya_GSE3064\\Biomarker Data\\Gene Expression\\Affymetrix Human HG-Focus Target Array\\XDP Nucleus Accumbens\\'
		def arr = StringUtils.split(conceptPath, "\\")
		def valList = []
		def removalArr = ['240 Minutes after Insulin Infusion', null, 'Vastus Lateralis Muscle']
		
		println ExportUtil.getShortConceptPath(conceptPath, removalArr)
		
		println StringUtils.join(["abc", "def"], "/")*/
		
		println """
{"id":"boxPlot","converter":{"R":["source('||PLUGINSCRIPTDIRECTORY||Common/dataBuilders.R')","source('||PLUGINSCRIPTDIRECTORY||Common/ExtractConcepts.R')","source('||PLUGINSCRIPTDIRECTORY||Common/collapsingData.R')","source('||PLUGINSCRIPTDIRECTORY||Common/BinData.R')","source('||PLUGINSCRIPTDIRECTORY||ANOVA/BuildANOVAData.R')","\t\t\t\t\t\tANOVAData.build(input.dataFile = '||TEMPFOLDERDIRECTORY||Clinical/clinical.i2b2trans',\n\t\t\t\t\t\t\t\t\t\tconcept.dependent='||DEPENDENT||',\n\t\t\t\t\t\t\t\t\t\tconcept.independent='||INDEPENDENT||',\n\t\t\t\t\t\t\t\t\t\tbinning.enabled='||BINNING||',\n\t\t\t\t\t\t\t\t\t\tbinning.bins='||NUMBERBINS||',\n\t\t\t\t\t\t\t\t\t\tbinning.type='||BINNINGTYPE||',\n\t\t\t\t\t\t\t\t\t\tbinning.manual='||BINNINGMANUAL||',\n\t\t\t\t\t\t\t\t\t\tbinning.binrangestring='||BINNINGRANGESTRING||',\n\t\t\t\t\t\t\t\t\t\tbinning.variabletype='||BINNINGVARIABLETYPE||',\n\t\t\t\t\t\t\t\t\t\tbinning.variable='||BINNINGVARIABLE||',\n\t\t\t\t\t\t\t\t\t\tflipimage=||FLIPIMAGE||,\n\t\t\t\t\t\t\t\t\t\tinput.gexFile = '||TEMPFOLDERDIRECTORY||mRNA/Processed_Data/mRNA.trans',\n\t\t\t\t\t\t\t\t\t\tinput.snpFile = '||TEMPFOLDERDIRECTORY||SNP/snp.trans',\n\t\t\t\t\t\t\t\t\t\tconcept.dependent.type = '||TYPEDEP||',\n\t\t\t\t\t\t\t\t\t\tconcept.independent.type = '||TYPEIND||',\n\t\t\t\t\t\t\t\t\t\tgenes.dependent = '||GENESDEP||',\n\t\t\t\t\t\t\t\t\t\tgenes.independent = '||GENESIND||',\n\t\t\t\t\t\t\t\t\t\tgenes.dependent.aggregate = '||AGGREGATEDEP||',\n\t\t\t\t\t\t\t\t\t\tgenes.independent.aggregate = '||AGGREGATEIND||',\n\t\t\t\t\t\t\t\t\t\tsample.dependent = '||SAMPLEDEP||',\n\t\t\t\t\t\t\t\t\t\tsample.independent = '||SAMPLEIND||',\n\t\t\t\t\t\t\t\t\t\ttime.dependent = '||TIMEPOINTSDEP||',\n\t\t\t\t\t\t\t\t\t\ttime.independent = '||TIMEPOINTIND||',\n\t\t\t\t\t\t\t\t\t\tsnptype.dependent = '||SNPTYPEDEP||',\n\t\t\t\t\t\t\t\t\t\tsnptype.independent = '||SNPTYPEIND||')\n\t\t\t\t\t"]},"name":"Box Plot with ANOVA","dataFileInputMapping":{"MRNA.TXT":"mrnaData","CLINICAL.TXT":"TRUE","SNP.TXT":"snpData"},"dataTypes":{"subset1":["CLINICAL.TXT"]},"pivotData":false,"view":"BoxPlot","processor":{"R":["source('||PLUGINSCRIPTDIRECTORY||ANOVA/BoxPlotLoader.R')","\t\t\t\t\t\t\tBoxPlot.loader(input.filename='outputfile',\n\t\t\t\t\t\t\t\t\t\t\tconcept.dependent='||DEPENDENT||',\n\t\t\t\t\t\t\t\t\t\t\tconcept.independent='||INDEPENDENT||',\n\t\t\t\t\t\t\t\t\t\t\tflipimage=||FLIPIMAGE||,\n\t\t\t\t\t\t\t\t\t\t\tconcept.dependent.type = '||TYPEDEP||',\n\t\t\t\t\t\t\t\t\t\t\tconcept.independent.type = '||TYPEIND||',\n\t\t\t\t\t\t\t\t\t\t\tgenes.dependent = '||GENESDEPNAME||',\n\t\t\t\t\t\t\t\t\t\t\tgenes.independent = '||GENESINDNAME||',\n\t\t\t\t\t\t\t\t\t\t\tbinning.enabled='||BINNING||',\n\t\t\t\t\t\t\t\t\t\t\tbinning.variable='||BINNINGVARIABLE||')\n\t\t\t\t\t"]},"renderer":{"GSP":"/boxPlot/boxPlotOut"},"variableMapping":{"||AGGREGATEIND||":"divIndependentVariableprobesAggregation","||FLIPIMAGE||":"flipImage","||BINNING||":"binning","||GPLDEP||":"divDependentVariablegpls","||BINNINGTYPE||":"binDistribution","||TIMEPOINTSDEP||":"divDependentVariabletimepoints","||BINNINGRANGESTRING||":"binRanges","||RBMPANELDEP||":"divDependentVariablerbmPanels","||TYPEIND||":"divIndependentVariableType","||TYPEDEP||":"divDependentVariableType","||SNPTYPEIND||":"divIndependentVariableprobesAggregation","||GPLIND||":"divIndependentVariablegpls","||RBMPANELIND||":"divIndependentVariablerbmPanels","||SNPTYPEDEP||":"divDependentVariableSNPType","||TIMEPOINTIND||":"divIndependentVariabletimepoints","||DEPENDENT||":"dependentVariable","||GENESDEPNAME||":"divDependentPathwayName","||NUMBERBINS||":"numberOfBins","||PLATFORMDEP||":"divDependentVariableplatforms","||GENESIND||":"divIndependentVariablePathway","||BINNINGVARIABLE||":"binVariable","||SAMPLEIND||":"divIndependentVariablesamples","||TISSUEDEP||":"divDependentVariabletissues","||GENESDEP||":"divDependentVariablePathway","||PLATFORMIND||":"divIndependentVariableplatforms","||AGGREGATEDEP||":"divDependentVariableprobesAggregation","||BINNINGVARIABLETYPE||":"variableType","||SAMPLEDEP||":"divDependentVariablesamples","||TISSUEIND||":"divIndependentVariabletissues","||INDEPENDENT||":"independentVariable","||GENESINDNAME||":"divIndependentPathwayName","||BINNINGMANUAL||":"manualBinning"}}
		"""
	}
	
}
