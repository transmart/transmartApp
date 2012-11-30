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
 * $Id: LiteratureInteractionData.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
package org.transmart.biomart
class LiteratureInteractionData extends Literature {
	Long id
	LiteratureReferenceData reference
	LiteratureModelData inVivoModel
	LiteratureModelData inVitroModel
	String etlId
	String sourceComponent
	String sourceGeneId
	String targetComponent
	String targetGeneId
	String interactionMode
	String regulation
	String mechanism
	String effect
	String localization
	String region
	String techniques
	static mapping = {
		table 'BIO_LIT_INT_DATA'
		version false
		id column:'BIO_LIT_INT_DATA_ID'
		id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
		columns {
			reference column:'BIO_LIT_REF_DATA_ID'
			inVivoModel column:'IN_VIVO_MODEL_ID'
			inVitroModel column:'IN_VITRO_MODEL_ID'
			etlId column:'ETL_ID'
			sourceComponent column:'SOURCE_COMPONENT'
			sourceGeneId column:'SOURCE_GENE_ID'
			targetComponent column:'TARGET_COMPONENT'
			targetGeneId column:'TARGET_GENE_ID'
			interactionMode column:'INTERACTION_MODE'
			regulation column:'REGULATION'
			mechanism column:'MECHANISM'
			effect column:'EFFECT'
			localization column:'LOCALIZATION'
			region column:'REGION'
			techniques column:'TECHNIQUES'
		}
	}
}
