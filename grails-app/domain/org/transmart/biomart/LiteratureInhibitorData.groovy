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
 * $Id: LiteratureInhibitorData.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
package org.transmart.biomart
class LiteratureInhibitorData extends Literature {
	Long id
	LiteratureReferenceData reference
	String etlId
	String effectResponseRate
	String effectDownstream
	String effectBeneficial
	String effectAdverse
	String effectDescription
	String effectPharmacos
	String effectPotentials
	String trialType
	String trialPhase
	String trialStatus
	String trialExperimentalModel
	String trialTissue
	String trialBodySubstance
	String trialDescription
	String trialDesigns
	String trialCellLine
	String trialCellType
	String trialPatientsNumber
	String trialInclusionCriteria
	String inhibitor
	String inhibitorStandardName
	String casid
	String description
	String concentration
	String timeExposure
	String administration
	String treatment
	String techniques
	String effectMolecular
	String effectPercent
	String effectNumber
	String effectValue
	String effectSd
	String effectUnit
	static mapping = {
		table 'BIO_LIT_INH_DATA'
		version false
		id column:'BIO_LIT_INH_DATA_ID'
		id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
		columns {
			reference column:'BIO_LIT_REF_DATA_ID'
			etlId column:'ETL_ID'
			effectResponseRate column:'EFFECT_RESPONSE_RATE'
			effectDownstream column:'EFFECT_DOWNSTREAM'
			effectBeneficial column:'EFFECT_BENEFICIAL'
			effectAdverse column:'EFFECT_ADVERSE'
			effectDescription column:'EFFECT_DESCRIPTION'
			effectPharmacos column:'EFFECT_PHARMACOS'
			effectPotentials column:'EFFECT_POTENTIALS'
			trialType column:'TRIAL_TYPE'
			trialPhase column:'TRIAL_PHASE'
			trialStatus column:'TRIAL_STATUS'
			trialExperimentalModel column:'TRIAL_EXPERIMENTAL_MODEL'
			trialTissue column:'TRIAL_TISSUE'
			trialBodySubstance column:'TRIAL_BODY_SUBSTANCE'
			trialDescription column:'TRIAL_DESCRIPTION'
			trialDesigns column:'TRIAL_DESIGNS'
			trialCellLine column:'TRIAL_CELL_LINE'
			trialCellType column:'TRIAL_CELL_TYPE'
			trialPatientsNumber column:'TRIAL_PATIENTS_NUMBER'
			trialInclusionCriteria column:'TRIAL_INCLUSION_CRITERIA'
			inhibitor column:'INHIBITOR'
			inhibitorStandardName column:'INHIBITOR_STANDARD_NAME'
			casid column:'CASID'
			description column:'DESCRIPTION'
			concentration column:'CONCENTRATION'
			timeExposure column:'TIME_EXPOSURE'
			administration column:'ADMINISTRATION'
			treatment column:'TREATMENT'
			techniques column:'TECHNIQUES'
			effectMolecular column:'EFFECT_MOLECULAR'
			effectPercent column:'EFFECT_PERCENT'
			effectNumber column:'EFFECT_NUMBER'
			effectValue column:'EFFECT_VALUE'
			effectSd column:'EFFECT_SD'
			effectUnit column:'EFFECT_UNIT'
		}
	}
}
