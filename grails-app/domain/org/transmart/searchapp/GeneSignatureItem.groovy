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
  

package org.transmart.searchapp

import org.transmart.biomart.BioAssayFeatureGroup;
import org.transmart.biomart.BioData;

import org.transmart.biomart.BioMarker

/**
 * domain class for a gene signature item
 */
class GeneSignatureItem {

	Long id
	GeneSignature geneSignature
	BioMarker bioMarker
	BioAssayFeatureGroup probeset
	String bioDataUniqueId
	Double foldChgMetric

	static belongsTo = [ geneSignature:GeneSignature ]

	static mapping = {
		table 'SEARCH_GENE_SIGNATURE_ITEM'
		version false
		id generator:'sequence', params:[sequence:'SEQ_SEARCH_DATA_ID']
		columns {
			id column:'ID'
			geneSignature column:'SEARCH_GENE_SIGNATURE_ID'
			bioMarker column:'BIO_MARKER_ID'
			probeset column:'BIO_ASSAY_FEATURE_GROUP_ID'
			foldChgMetric column:"FOLD_CHG_METRIC"
			bioDataUniqueId column:'BIO_DATA_UNIQUE_ID'
		}
	}

	static constraints = {
		foldChgMetric(nullable:true)
		bioDataUniqueId(nullable:true)
		bioMarker(nullable:true)
		probeset(nullable:true)
	}
}