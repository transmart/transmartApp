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
class CellLine {
		String disease
		String primarySite
		String metastaticSite
		String species
		String attcNumber
		String cellLineName
		Long id
		Long bioDiseaseId
		String origin
		String description
		String diseaseStage
		String diseaseSubtype
 static mapping = {
	 table 'BIO_CELL_LINE'
	 version false
	 cache true
	 id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
	 columns {
		disease column:'DISEASE'
		primarySite column:'PRIMARY_SITE'
		metastaticSite column:'METASTATIC_SITE'
		species column:'SPECIES'
		attcNumber column:'ATTC_NUMBER'
		cellLineName column:'CELL_LINE_NAME'
		id column:'BIO_CELL_LINE_ID'
		bioDiseaseId column:'BIO_DISEASE_ID'
		origin column:'ORIGIN'
		description column:'DESCRIPTION'
		diseaseStage column:'DISEASE_STAGE'
		diseaseSubtype column:'DISEASE_SUBTYPE'
		}
	}

}
