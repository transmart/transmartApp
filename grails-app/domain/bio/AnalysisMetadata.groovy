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

package bio

class AnalysisMetadata {
	Long id
	Experiment study
	String dataType
	String analysisName
	String description
	String phenotypeIds
	Long population
	String tissue
	String genomeVersion
	String genotypePlatformIds
	String expressionPlatformIds
	String statisticalTest
	String researchUnit
	String sampleSize
	String cellType
	String modelName
	String modelDescription
	Double pValueCutoff
	Date etlDate
	Date processDate
	String filename
	String status = "NEW"

	static mapping = {
		table 'LZ_SRC_ANALYSIS_METADATA'
		
		id column: 'ETL_ID', generator:'sequence', params:[sequence:'SEQ_ETL_ID']
		
		version false
		columns {					        //3456789012345678901234567890
			study 					column:'STUDY_ID'
			dataType				column:'DATA_TYPE'
			analysisName			column:'ANALYSIS_NAME'
			description				column:'DESCRIPTION'
			phenotypeIds			column:'PHENOTYPE_IDS'
			population				column:'POPULATION'
			tissue					column:'TISSUE'
			genomeVersion			column:'GENOME_VERSION'
			genotypePlatformIds		column:'GENOTYPE_PLATFORM_IDS'
			expressionPlatformIds	column:'EXPRESSION_PLATFORM_IDS'
			statisticalTest			column:'STATISTICAL_TEST'
			researchUnit			column:'RESEARCH_UNIT'
			sampleSize				column:'SAMPLE_SIZE'
			cellType				column:'CELL_TYPE'
			pValueCutoff			column:'PVALUE_CUTOFF'
			etlDate					column:'ETL_DATE'
			filename				column:'FILENAME'
			status					column:'STATUS'
			processDate				column:'PROCESS_DATE'
			modelName				column:'MODEL_NAME'
			modelDescription		column:'MODEL_DESC'
		}
	}
	
	static constraints = {
		dataType(nullable:true)
		analysisName(unique: true, maxSize:50)
		description(nullable:true, maxSize:4000)
		phenotypeIds(nullable:true)
		population(nullable:true)
		tissue(nullable:true)
		genomeVersion(nullable:true)
		genotypePlatformIds(nullable:true)
		expressionPlatformIds(nullable:true)
		statisticalTest(nullable:true)
		researchUnit(nullable:true)
		sampleSize(nullable:true)
		cellType(nullable:true)
		pValueCutoff(nullable:true)
		etlDate(nullable:true)
		filename(nullable:true)
		status(nullable:true)
		processDate(nullable:true)
		modelName(nullable:true)
		modelDescription(nullable:true)
	}
}