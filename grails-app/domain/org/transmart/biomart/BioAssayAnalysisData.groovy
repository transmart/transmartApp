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
 * $Id: BioAssayAnalysisData.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */

package org.transmart.biomart

import org.transmart.biomart.BioMarker;
import org.transmart.biomart.Experiment;

import com.recomdata.util.IExcelProfile

class BioAssayAnalysisData implements IExcelProfile {
		String featureGroupName
		Experiment experiment
		BioAssayPlatform assayPlatform
		Double foldChangeRatio
		Double rawPvalue
	    Double adjustedPvalue
		Double preferredPvalue
	    Double rValue
		Double rhoValue
		BioAssayAnalysis analysis
		Double cutValue
		String resultsValue
		Long id
		String adjustedPValueCode
		Double numericValue
		String numericValueCode
		Double teaNormalizedPValue
		BioAssayFeatureGroup featureGroup
		static hasMany=[markers:BioMarker]
		static belongsTo=[BioMarker]

	static mapping = {
	 table 'BIO_ASSAY_ANALYSIS_DATA'
	 version false
	 id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
	 columns {
		featureGroupName column:'FEATURE_GROUP_NAME'
		experiment column:'BIO_EXPERIMENT_ID'
		assayPlatform column:'BIO_ASSAY_PLATFORM_ID'
		foldChangeRatio column:'FOLD_CHANGE_RATIO'
		rawPvalue column:'RAW_PVALUE'
		adjustedPvalue column:'ADJUSTED_PVALUE'
		preferredPvalue column:'PREFERRED_PVALUE'
		rValue column:'R_VALUE'
		rhoValue column:'RHO_VALUE'
		featureGroup column:'BIO_ASSAY_FEATURE_GROUP_ID'
		analysis column:'BIO_ASSAY_ANALYSIS_ID'
		cutValue column:'CUT_VALUE'
		resultsValue column:'RESULTS_VALUE'
		id column:'BIO_ASY_ANALYSIS_DATA_ID'
		adjustedPValueCode column:'ADJUSTED_P_VALUE_CODE'
		numericValue column:'NUMERIC_VALUE'
		numericValueCode column:'NUMERIC_VALUE_CODE'
		teaNormalizedPValue column:'TEA_NORMALIZED_PVALUE'
		markers joinTable:[name:'BIO_DATA_OMIC_MARKER', key:'BIO_DATA_ID']
		}
	}

	/**
	 * Get values to Export to Excel
	 */
	public List getValues() {
		return [featureGroupName, foldChangeRatio, rValue, rawPvalue, teaNormalizedPValue, adjustedPvalue, rhoValue, cutValue, resultsValue, numericValueCode, numericValue]
	}
}
