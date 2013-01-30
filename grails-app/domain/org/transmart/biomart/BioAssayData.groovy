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

import org.transmart.biomart.BioMarker;
import org.transmart.biomart.Compound;
import org.transmart.biomart.Disease;
import org.transmart.biomart.Experiment;

class BioAssayData {
		Long numericValue
		String textValue
		Double floatValue
		String featureGroupName
		Experiment experiment
		Long bioSampleId
		Long bioAssayId
		Long id
		Double log2Value
		Double log10Value
		Long bioAssayDatasetId
		static hasMany=[diseases:Disease, compounds:Compound, markers:BioMarker]

 static mapping = {
	 table 'BIO_ASSAY_DATA'
	 version false
	 id column:'BIO_ASSAY_DATA_ID'
	 id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
	 columns {
		numericValue column:'NUMERIC_VALUE'
		textValue column:'TEXT_VALUE'
		floatValue column:'FLOAT_VALUE'
		featureGroupName column:'FEATURE_GROUP_NAME'
		experiment column:'BIO_EXPERIMENT_ID'
		bioSampleId column:'BIO_SAMPLE_ID'
		bioAssayId column:'BIO_ASSAY_ID'
		log2Value column:'LOG2_VALUE'
		log10Value column:'LOG10_VALUE'
		bioAssayDatasetId column:'BIO_ASSAY_DATASET_ID'
		diseases joinTable:[name:'BIO_DATA_DISEASE', key:'BIO_DATA_ID', column:'BIO_DISEASE_ID']
		markers joinTable:[name:'BIO_DATA_OMIC_MARKER', key:'BIO_DATA_ID', column:'BIO_MARKER_ID']
		compounds joinTable:[name:'BIO_DATA_COMPOUND', key:'BIO_DATA_ID', column:'BIO_COMPOUND_ID']
		}
	}

}
