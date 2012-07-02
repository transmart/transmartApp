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

class ConceptCode {
		Long id
		String bioConceptCode
		String codeName
		String codeDescription
		String codeTypeName
 static mapping = {
	 table 'BIO_CONCEPT_CODE'
	 cache true
	 version false
	 id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
	 columns {
		id column:'BIO_CONCEPT_CODE_ID'
		bioConceptCode column:'BIO_CONCEPT_CODE'
		codeName column:'CODE_NAME'
		codeDescription column:'CODE_DESCRIPTION'
		codeTypeName column:'CODE_TYPE_NAME'
		}
	}
 static constraints = {
	bioConceptCode(nullable:true, maxSize:400)
	codeDescription(nullable:true, maxSize:2000)
	codeTypeName(nullable:true, maxSize:400)
	}
}
