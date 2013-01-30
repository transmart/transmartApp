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
  

/**
 * $Id: LiteratureModelData.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
package org.transmart.biomart
class LiteratureModelData {
	Long id
	String etlId
	String modelType
	String description
	String stimulation
	String controlChallenge
	String challenge
	String sentization
	String zygosity
	String experimentalModel
	String animalWildType
	String tissue
	String cellType
	String cellLine
	String bodySubstance
	String component
	String geneId
	static mapping = {
		table 'BIO_LIT_MODEL_DATA'
		version false
		id column:'BIO_LIT_MODEL_DATA_ID'
		id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
		columns {
			etlId column:'ETL_ID'
			modelType column:'MODEL_TYPE'
			description column:'DESCRIPTION'
			stimulation column:'STIMULATION'
			controlChallenge column:'CONTROL_CHALLENGE'
			challenge column:'CHALLENGE'
			sentization column:'SENTIZATION'
			zygosity column:'ZYGOSITY'
			experimentalModel column:'EXPERIMENTAL_MODEL'
			animalWildType column:'ANIMAL_WILD_TYPE'
			tissue column:'TISSUE'
			cellType column:'CELL_TYPE'
			cellLine column:'CELL_LINE'
			bodySubstance column:'BODY_SUBSTANCE'
			component column:'COMPONENT'
			geneId column:'GENE_ID'
		}
	}
}
