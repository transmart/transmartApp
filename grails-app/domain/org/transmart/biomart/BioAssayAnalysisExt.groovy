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
  
package org.transmart.biomart

class BioAssayAnalysisExt {
	String vendor
	String vendorType
	String genomeVersion
	String tissue
	String cellType
	String population
	String researchUnit
	String sampleSize
	String modelName
	String modelDescription
	
	static belongsTo=[bioAssayAnalysis: BioAssayAnalysis]

	static mapping = {
		table 'BIO_ASSAY_ANALYSIS_EXT'
		version false
		cache usage:'read-only'
		id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
		columns {
			id column: 'BIO_ASSAY_ANALYSIS_EXT_ID'
			bioAssayAnalysis: 'BIO_ASSAY_ANALYSIS_ID'
			vendor column:'VENDOR'
			vendorType column:'VENDOR_TYPE'
			genomeVersion column:'GENOME_VERSION'
			tissue column:'TISSUE'
			cellType column:'CELL_TYPE'
			population column:'POPULATION'
			researchUnit column:'RESEARCH_UNIT'
			sampleSize column:'SAMPLE_SIZE'
			modelName column:'MODEL_NAME'
			modelDescription column:'MODEL_DESC'
		}
	}

	static constraints = {
		vendor(nullable: true)
		vendorType(nullable: true)
		genomeVersion(nullable: true)
		tissue(nullable: true)
		cellType(nullable: true)
		population(nullable: true)
		researchUnit(nullable: true)
		sampleSize(nullable: true)
		modelName(nullable: true)
		modelDescription(nullable: true)
	}
}