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
class Patient {
		Long id
		String firstName
		String lastName
		String middleName
		Date birthDate
		String birthDateOrig
		String genderCode
		String raceCode
		String ethnicGroupCode
		String addressZipCode
		String countryCode
		String informedConsentCode
		Long bioExperimentId
		Long bioClinicalTrialPGroupId
 static mapping = {
	 table 'BIO_PATIENT'
	 version false
	 id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
	 columns {
		id column:'BIO_PATIENT_ID'
		firstName column:'FIRST_NAME'
		lastName column:'LAST_NAME'
		middleName column:'MIDDLE_NAME'
		birthDate column:'BIRTH_DATE'
		birthDateOrig column:'BIRTH_DATE_ORIG'
		genderCode column:'GENDER_CODE'
		raceCode column:'RACE_CODE'
		ethnicGroupCode column:'ETHNIC_GROUP_CODE'
		addressZipCode column:'ADDRESS_ZIP_CODE'
		countryCode column:'COUNTRY_CODE'
		informedConsentCode column:'INFORMED_CONSENT_CODE'
		bioExperimentId column:'BIO_EXPERIMENT_ID'
		bioClinicalTrialPGroupId column:'BIO_CLINICAL_TRIAL_P_GROUP_ID'
		}
	}
 static constraints = {
	firstName(nullable:true, maxSize:400)
	lastName(nullable:true, maxSize:400)
	middleName(nullable:true, maxSize:400)
	birthDate(nullable:true)
	birthDateOrig(nullable:true, maxSize:400)
	genderCode(nullable:true, maxSize:400)
	raceCode(nullable:true, maxSize:400)
	ethnicGroupCode(nullable:true, maxSize:400)
	addressZipCode(nullable:true, maxSize:400)
	countryCode(nullable:true, maxSize:400)
	informedConsentCode(nullable:true, maxSize:400)
	bioExperimentId(nullable:true)
	bioClinicalTrialPGroupId(nullable:true)
	}
}
