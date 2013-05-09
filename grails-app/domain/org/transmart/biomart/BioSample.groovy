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

import org.transmart.biomart.CellLine;

class BioSample {
		Long id
		String type
		String characteristics
		String sourceCode
		Experiment experiment
		CellLine cellLine
		BioSubject bioSubject
		String source
		Long bioBankId
		Long bioPatientEventId
		String name

 static mapping = {
	 table 'BIO_SAMPLE'
	 version false
	 id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
	 columns {
		id column:'BIO_SAMPLE_ID'
		type column:'BIO_SAMPLE_TYPE'
		characteristics column:'CHARACTERISTICS'
		sourceCode column:'SOURCE_CODE'
		experiment column:'EXPERIMENT_ID'
		bioSubject column:'BIO_SUBJECT_ID'
		source column:'SOURCE'
		bioBankId column:'BIO_BANK_ID'
		bioPatientEventId column:'BIO_PATIENT_EVENT_ID'
		name column:'BIO_SAMPLE_NAME'
		cellLine column:'BIO_CELL_LINE_ID'
		}
	}
 static constraints = {
	type(maxSize:400)
	characteristics(nullable:true, maxSize:2000)
	sourceCode(nullable:true, maxSize:400)
	source(nullable:true, maxSize:400)
	bioBankId(nullable:true)
	bioPatientEventId(nullable:true)
	}
}
