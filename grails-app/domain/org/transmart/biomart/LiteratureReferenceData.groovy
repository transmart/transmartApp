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
 * $Id: LiteratureReferenceData.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
package org.transmart.biomart
class LiteratureReferenceData {
	Long id
	String etlId
	String component
	String componentClass
	String geneId
	String moleculeType
	String variant
	String referenceType
	String referenceId
	String referenceTitle
	String backReferences
	String studyType
	String disease
	String diseaseIcd10
	String diseaseMesh
	String diseaseSite
	String diseaseStage
	String diseaseGrade
	String diseaseTypes
	String diseaseDescription
	String physiology
	String statClinical
	String statClinicalCorrelation
	String statTests
	String statCoefficient
	String statPValue
	String statDescription
	static mapping = {
		table 'BIO_LIT_REF_DATA'
		version false
		id column:'BIO_LIT_REF_DATA_ID'
		id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
		columns {
			etlId column:'ETL_ID'
			component column:'COMPONENT'
			componentClass column:'COMPONENT_CLASS'
			geneId column:'GENE_ID'
			moleculeType column:'MOLECULE_TYPE'
			variant column:'VARIANT'
			referenceType column:'REFERENCE_TYPE'
			referenceId column:'REFERENCE_ID'
			referenceTitle column:'REFERENCE_TITLE'
			backReferences column:'BACK_REFERENCES'
			studyType column:'STUDY_TYPE'
			disease column:'DISEASE'
			diseaseIcd10 column:'DISEASE_ICD10'
			diseaseMesh column:'DISEASE_MESH'
			diseaseSite column:'DISEASE_SITE'
			diseaseStage column:'DISEASE_STAGE'
			diseaseGrade column:'DISEASE_GRADE'
			diseaseTypes column:'DISEASE_TYPES'
			diseaseDescription column:'DISEASE_DESCRIPTION'
			physiology column:'PHYSIOLOGY'
			statClinical column:'STAT_CLINICAL'
			statClinicalCorrelation column:'STAT_CLINICAL_CORRELATION'
			statTests column:'STAT_TESTS'
			statCoefficient column:'STAT_COEFFICIENT'
			statPValue column:'STAT_P_VALUE'
			statDescription column:'STAT_DESCRIPTION'
		}
	}
}
