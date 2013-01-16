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
 * $Id: BioMarker.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
 
package org.transmart.biomart

import org.transmart.biomart.BioAssayAnalysisData;
import org.transmart.biomart.BioAssayDataStatistics;
import org.transmart.biomart.BioDataCorrelation;

import com.recomdata.util.IExcelProfile

class BioMarker implements IExcelProfile {
		Long id
		String name
		String description
		String organism
		String primarySourceCode
		String primaryExternalId
		String bioMarkerType
		static hasMany=[correlations:BioDataCorrelation,
		                associatedCorrels:BioDataCorrelation,
		                assayAnalysisData:BioAssayAnalysisData,
		                literatures:Literature,
		                assayDataStats:BioAssayDataStatistics]

		def isGene(){
			return "GENE".equalsIgnoreCase(bioMarkerType)
		}
		def isPathway(){
			return "PATHWAY".equalsIgnoreCase(bioMarkerType)
		}

 static mapping = {
	 table 'BIO_MARKER'
	 version false

//	 id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
	 columns {
		id column:'BIO_MARKER_ID'
		name column:'BIO_MARKER_NAME'
		description column:'BIO_MARKER_DESCRIPTION'
		organism column:'ORGANISM'
		primarySourceCode column:'PRIMARY_SOURCE_CODE'
		primaryExternalId column:'PRIMARY_EXTERNAL_ID'
		bioMarkerType column:'BIO_MARKER_TYPE'
		correlations joinTable:[name:'BIO_DATA_CORRELATION', key:'BIO_DATA_ID',column:'BIO_DATA_CORREL_ID']
		associatedCorrels joinTable:[name:'BIO_DATA_CORRELATION', key:'ASSO_BIO_DATA_ID', column:'BIO_DATA_CORREL_ID']
		assayAnalysisData joinTable:[name:'BIO_DATA_OMIC_MARKER', key:'BIO_MARKER_ID']
		literatures joinTable:[name:'BIO_DATA_OMIC_MARKER', key:'BIO_MARKER_ID']
		assayDataStats joinTable:[name:'BIO_DATA_OMIC_MARKER', key:'BIO_MARKER_ID']
 }
	}
		static constraints = {
			primaryExternalId(nullable:true, maxSize:400)
			bioMarkerType(maxSize:400)
			name(nullable:true, maxSize:400)
			description(nullable:true, maxSize:2000)
			organism(nullable:true, maxSize:400)
			primarySourceCode(nullable:true, maxSize:400)
			}

    /**
	 * Get values to Export to Excel
	 */
	public List getValues() {	     
		return [name, description, organism]
	}
}
