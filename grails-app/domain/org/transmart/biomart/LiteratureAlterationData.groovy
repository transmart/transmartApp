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
 * $Id: LiteratureAlterationData.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
package org.transmart.biomart
class LiteratureAlterationData extends Literature {
	Long id
	LiteratureReferenceData reference
	LiteratureModelData inVivoModel
	LiteratureModelData inVitroModel
	String etlId
	String alterationType
	String control
	String effect
	String description
	String techniques
	String patientsPercent
	String patientsNumber
	String popNumber
	String popInclusionCriteria
	String popExclusionCriteria
	String popDescription
	String popType
	String popValue
	String popPhase
	String popStatus
	String popExperimentalModel
	String popTissue
	String popBodySubstance
	String popLocalization
	String popCellType
	String clinSubmucosaMarkerType
	String clinSubmucosaUnit
	String clinSubmucosaValue
	String clinAsmMarkerType
	String clinAsmUnit
	String clinAsmValue
	String clinCellularSource
	String clinCellularType
	String clinCellularCount
	String clinPriorMedPercent
	String clinPriorMedDose
	String clinPriorMedName
	String clinBaselineVariable
	String clinBaselinePercent
	String clinBaselineValue
	String clinSmoker
	String clinAtopy
	String controlExpPercent
	String controlExpNumber
	String controlExpValue
	String controlExpSd
	String controlExpUnit
	String overExpPercent
	String overExpNumber
	String overExpValue
	String overExpSd
	String overExpUnit
	String lossExpPercent
	String lossExpNumber
	String lossExpValue
	String lossExpSd
	String lossExpUnit
	String totalExpPercent
	String totalExpNumber
	String totalExpValue
	String totalExpSd
	String totalExpUnit
	String glcControlPercent
	String glcMolecularChange
	String glcType
	String glcPercent
	String glcNumber
	String ptmRegion
	String ptmType
	String ptmChange
	String lohLoci
	String mutationType
	String mutationChange
	String mutationSites
	String epigeneticRegion
	String epigeneticType
	static hasMany = [assocMoleculeDetails:LiteratureAssocMoleculeDetailsData]
	static mapping = {
		table 'BIO_LIT_ALT_DATA'
		version false
		id column:'BIO_LIT_ALT_DATA_ID'
		id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
		columns {
			reference column:'BIO_LIT_REF_DATA_ID'
			inVivoModel column:'IN_VIVO_MODEL_ID'
			inVitroModel column:'IN_VITRO_MODEL_ID'
			assocMoleculeDetails joinTable:[name:'BIO_LIT_AMD_DATA', key:'BIO_LIT_ALT_DATA_ID', column:'BIO_LIT_AMD_DATA_ID']	
			etlId column:'ETL_ID'
			alterationType column:'ALTERATION_TYPE'
			control column:'CONTROL'
			effect column:'EFFECT'
			description column:'DESCRIPTION'
			techniques column:'TECHNIQUES'
			patientsPercent column:'PATIENTS_PERCENT'
			patientsNumber column:'PATIENTS_NUMBER'
			popNumber column:'POP_NUMBER'
			popInclusionCriteria column:'POP_INCLUSION_CRITERIA'
			popExclusionCriteria column:'POP_EXCLUSION_CRITERIA'
			popDescription column:'POP_DESCRIPTION'
			popType column:'POP_TYPE'
			popValue column:'POP_VALUE'
			popPhase column:'POP_PHASE'
			popStatus column:'POP_STATUS'
			popExperimentalModel column:'POP_EXPERIMENTAL_MODEL'
			popTissue column:'POP_TISSUE'
			popBodySubstance column:'POP_BODY_SUBSTANCE'
			popLocalization column:'POP_LOCALIZATION'
			popCellType column:'POP_CELL_TYPE'
			clinSubmucosaMarkerType column:'CLIN_SUBMUCOSA_MARKER_TYPE'
			clinSubmucosaUnit column:'CLIN_SUBMUCOSA_UNIT'
			clinSubmucosaValue column:'CLIN_SUBMUCOSA_VALUE'
			clinAsmMarkerType column:'CLIN_ASM_MARKER_TYPE'
			clinAsmUnit column:'CLIN_ASM_UNIT'
			clinAsmValue column:'CLIN_ASM_VALUE'
			clinCellularSource column:'CLIN_CELLULAR_SOURCE'
			clinCellularType column:'CLIN_CELLULAR_TYPE'
			clinCellularCount column:'CLIN_CELLULAR_COUNT'
			clinPriorMedPercent column:'CLIN_PRIOR_MED_PERCENT'
			clinPriorMedDose column:'CLIN_PRIOR_MED_DOSE'
			clinPriorMedName column:'CLIN_PRIOR_MED_NAME'
			clinBaselineVariable column:'CLIN_BASELINE_VARIABLE'
			clinBaselinePercent column:'CLIN_BASELINE_PERCENT'
			clinBaselineValue column:'CLIN_BASELINE_VALUE'
			clinSmoker column:'CLIN_SMOKER'
			clinAtopy column:'CLIN_ATOPY'
			controlExpPercent column:'CONTROL_EXP_PERCENT'
			controlExpNumber column:'CONTROL_EXP_NUMBER'
			controlExpValue column:'CONTROL_EXP_VALUE'
			controlExpSd column:'CONTROL_EXP_SD'
			controlExpUnit column:'CONTROL_EXP_UNIT'
			overExpPercent column:'OVER_EXP_PERCENT'
			overExpNumber column:'OVER_EXP_NUMBER'
			overExpValue column:'OVER_EXP_VALUE'
			overExpSd column:'OVER_EXP_SD'
			overExpUnit column:'OVER_EXP_UNIT'
			lossExpPercent column:'LOSS_EXP_PERCENT'
			lossExpNumber column:'LOSS_EXP_NUMBER'
			lossExpValue column:'LOSS_EXP_VALUE'
			lossExpSd column:'LOSS_EXP_SD'
			lossExpUnit column:'LOSS_EXP_UNIT'
			totalExpPercent column:'TOTAL_EXP_PERCENT'
			totalExpNumber column:'TOTAL_EXP_NUMBER'
			totalExpValue column:'TOTAL_EXP_VALUE'
			totalExpSd column:'TOTAL_EXP_SD'
			totalExpUnit column:'TOTAL_EXP_UNIT'
			glcControlPercent column:'GLC_CONTROL_PERCENT'
			glcMolecularChange column:'GLC_MOLECULAR_CHANGE'
			glcType column:'GLC_TYPE'
			glcPercent column:'GLC_PERCENT'
			glcNumber column:'GLC_NUMBER'
			ptmRegion column:'PTM_REGION'
			ptmType column:'PTM_TYPE'
			ptmChange column:'PTM_CHANGE'
			lohLoci column:'LOH_LOCI'
			mutationType column:'MUTATION_TYPE'
			mutationChange column:'MUTATION_CHANGE'
			mutationSites column:'MUTATION_SITES'
			epigeneticRegion column:'EPIGENETIC_REGION'
			epigeneticType column:'EPIGENETIC_TYPE'
		}
	}
}
