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
* $Id
* @author $Author
* @version $Revision
*
*/

package i2b2

class SubjectSnpDataset {

	public static String SAMPLE_TYPE_NORMAL = "NORMAL";
	public static String SAMPLE_TYPE_DISEASE = "DISEASE";

	Long id;
	String datasetName;

	String conceptCd;

	String platformName;
	String trialName;
	Long patientNum;
	String patientGender;
	String timePoint;
	String subjectId;

	/* This is used to organized paired datasets in GenePattern sample info text file
	 The value is stored in database table "de_subject_snp_dataset", column "sample_type" */
	String sampleType;

	Long pairedDatasetId;


	static mapping = {
		table 'DE_SUBJECT_SNP_DATASET'
		version false
		columns{
			id column:'SUBJECT_SNP_DATASET_ID'
			datasetName column:'DATASET_NAME'
			conceptCd column:'CONCEPT_CD'
			platformName column:'PLATFORM_NAME'
			trialName column:'TRIAL_NAME'
			patientNum column:'PATIENT_NUM'
			patientGender column:'PATIENT_GENDER'
			timePoint column:'TIMEPOINT'
			subjectId column:'SUBJECT_ID'
			sampleType column:'SAMPLE_TYPE'
			pairedDatasetId column:'PAIRED_DATASET_ID'
		}
	}
}

