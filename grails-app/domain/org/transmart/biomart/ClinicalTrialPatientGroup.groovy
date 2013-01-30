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
class ClinicalTrialPatientGroup {
		ClinicalTrial clinicalTrial
		Long id
		String name
		String description
		Long numberOfPatients
		String patientGroupTypeCode
 static mapping = {
	 table 'BIO_CLINICAL_TRIAL_PT_GROUP'
	 version false
	 id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
	 columns {
		clinicalTrial column:'BIO_EXPERIMENT_ID'
		id column:'BIO_CLINICAL_TRIAL_P_GROUP_ID'
		name column:'NAME'
		description column:'DESCRIPTION'
		numberOfPatients column:'NUMBER_OF_PATIENTS'
		patientGroupTypeCode column:'PATIENT_GROUP_TYPE_CODE'
		}
	}

		static constraints = {
			name(nullable:true, maxSize:1020)
			description(nullable:true, maxSize:2000)
			numberOfPatients(nullable:true)
			patientGroupTypeCode(nullable:true, maxSize:400)
			}
}
