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

import org.transmart.biomart.BioSample;
import org.transmart.biomart.Experiment;

class BioAssayDataStatistics {
		Long id
		Long sampleCount
		Double quartile1
		Double quartile2
		Double quartile3
		Double maxValue
		Double minValue
		Double meanValue
		BioSample sample
		BioAssayDataset dataset
		Double stdDevValue
		String featureGroupName
		String valueNormalizeMethod
		Experiment experiment
		BioAssayFeatureGroup featureGroup
		//static hasMany=[markers:BioMarker]
		//static belongsTo=[BioMarker]

 static mapping = {
	 table 'BIO_ASSAY_DATA_STATS'
	 version false
	 id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_FACT_ID']
	 columns {
		id column:'BIO_ASSAY_DATA_STATS_ID'
		sampleCount column:'BIO_SAMPLE_COUNT'
		quartile1 column:'QUARTILE_1'
		quartile2 column:'QUARTILE_2'
		quartile3 column:'QUARTILE_3'
		maxValue column:'MAX_VALUE'
		minValue column:'MIN_VALUE'
		meanValue column:"MEAN_VALUE"
		stdDevValue column:'STD_DEV_VALUE'
		sample column:'BIO_SAMPLE_ID'
		featureGroupName column:'FEATURE_GROUP_NAME'
		valueNormalizeMethod column:'VALUE_NORMALIZE_METHOD'
		experiment column:'BIO_EXPERIMENT_ID'
		dataset column:'BIO_ASSAY_DATASET_ID'
		featureGroup column:'BIO_ASSAY_FEATURE_GROUP_ID'
	//	markers joinTable:[name:'BIO_DATA_OMIC_MARKER', key:'BIO_DATA_ID']

		}
	}

}
