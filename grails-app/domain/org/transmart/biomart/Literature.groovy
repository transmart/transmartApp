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
 * $Id: Literature.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
package org.transmart.biomart
class Literature {
	Long id
	LiteratureReferenceData reference
	Long bioCurationDatasetId
	String statement
	String statementStatus
	String dataType
	static hasMany = [diseases:Disease, compounds:Compound, markers:BioMarker, files:ContentReference]
	static belongsTo = [Disease, Compound, BioMarker, ContentReference]		
	static mapping = {
		table 'BIO_DATA_LITERATURE'
		tablePerHierarchy false
		version false
		id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
		columns {
			id column:'BIO_DATA_ID'
			reference column: 'BIO_LIT_REF_DATA_ID'
			bioCurationDatasetId column:'BIO_CURATION_DATASET_ID'
			statement column:'STATEMENT'
			statementStatus column:'STATEMENT_STATUS'
			dataType column:'DATA_TYPE'
			diseases joinTable:[name:'BIO_DATA_DISEASE', key:'BIO_DATA_ID']
			markers joinTable:[name:'BIO_DATA_OMIC_MARKER', key:'BIO_DATA_ID']
			compounds joinTable:[name:'BIO_DATA_COMPOUND', key:'BIO_DATA_ID']
			files joinTable:[name:'BIO_CONTENT_REFERENCE', key:'BIO_DATA_ID', column:'BIO_CONTENT_REFERENCE_ID']
		}
	}
}
