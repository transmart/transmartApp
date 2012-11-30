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
X * $Id: ClinicalTrial.groovy 10184 2011-10-24 21:43:59Z jliu $
 * @author $Author: jliu $
 * @version $Revision: 10184 $
 */

package org.transmart.biomart

import java.util.List;

import org.transmart.biomart.Experiment;

class ClinicalTrial extends Experiment {
	String trialNumber
	String studyOwner
	String studyPhase
	String blindingProcedure
	String studyType
	Long durationOfStudyWeeks
	Long numberOfPatients
	Long numberOfSites
	String routeOfAdministration
	String dosingRegimen
	String groupAssignment
	String typeOfControl
	String primaryEndPoints
	String secondaryEndPoints
	String inclusionCriteria
	String exclusionCriteria
	String subjects
	String genderRestrictionMfb
	Long minAge
	Long maxAge
	String secondaryIds
	//Long id
	static mapping = {
		table 'BIO_CLINICAL_TRIAL'
		version false
		//id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
		cache usage:'read-only'
		columns {
			id 	column:'BIO_EXPERIMENT_ID'
			trialNumber column:'TRIAL_NUMBER'
			studyOwner column:'STUDY_OWNER'
			studyPhase column:'STUDY_PHASE'
			blindingProcedure column:'BLINDING_PROCEDURE'
			studyType column:'STUDYTYPE'
			durationOfStudyWeeks column:'DURATION_OF_STUDY_WEEKS'
			numberOfPatients column:'NUMBER_OF_PATIENTS'
			numberOfSites column:'NUMBER_OF_SITES'
			routeOfAdministration column:'ROUTE_OF_ADMINISTRATION'
			dosingRegimen column:'DOSING_REGIMEN'
			groupAssignment column:'GROUP_ASSIGNMENT'
			typeOfControl column:'TYPE_OF_CONTROL'
			primaryEndPoints column:'PRIMARY_END_POINTS'
			secondaryEndPoints column:'SECONDARY_END_POINTS'
			inclusionCriteria type:'text', column:'INCLUSION_CRITERIA'
			exclusionCriteria type:'text', column:'EXCLUSION_CRITERIA'
			subjects column:'SUBJECTS'
			genderRestrictionMfb column:'GENDER_RESTRICTION_MFB'
			minAge column:'MIN_AGE'
			maxAge column:'MAX_AGE'
			secondaryIds column:'SECONDARY_IDS'
		}
	}
	
	/**
	 * Get values to Export to Excel
	 */
	public List getValues() {
		return [title, trialNumber, primaryInvestigator, description, studyPhase, studyType, design, blindingProcedure, durationOfStudyWeeks,
		completionDate, inclusionCriteria, exclusionCriteria, dosingRegimen, typeOfControl, genderRestrictionMfb,
		groupAssignment, primaryEndPoints, secondaryEndPoints, routeOfAdministration, secondaryIds, subjects, maxAge, minAge,
		numberOfPatients, numberOfSites, super.getCompoundNames(), super.getDiseaseNames()]
	}
	
	
}
