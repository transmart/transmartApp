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
class BioDataExternalCode {
		Long id
		Long bioDataId
		String code
		String codeSource
		String codeType
		String bioDataType
 static mapping = {
	 table 'BIO_DATA_EXT_CODE'
	 version false
	 id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
	 columns {
		id column:'BIO_DATA_EXT_CODE_ID'
		bioDataId column:'BIO_DATA_ID'
		code column:'CODE'
		codeSource column:'CODE_SOURCE'
		codeType column:'CODE_TYPE'
		bioDataType column:'BIO_DATA_TYPE'
		}
	}
 static constraints = {
	code(maxSize:500)
	codeSource(nullable:true, maxSize:400)
	codeType(nullable:true, maxSize:400)
	bioDataType(nullable:true, maxSize:100)
	}
}
