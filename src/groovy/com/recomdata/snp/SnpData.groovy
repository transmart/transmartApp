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
  

package com.recomdata.snp

import com.recomdata.dataexport.util.BiomarkerDataRowProcessor

/**
 * This class will represent a row of SNP data we are exporting to a file.
 * @author MMcDuffie
 *
 */
class SnpData implements BiomarkerDataRowProcessor 
{
	//The file format will be PATIENT_NUM,GENE,PROBE_ID,GENOTYPE,COPYNUMBER
	void processDataRow(row,snpOutputFile)
	{
		//Construct the line of data for the file.
		String lineToWrite = "${row.patientNum}\t${row.geneName}\t${row.probeName}\t${row.genotype}\t${row.copyNumber}"
		
		//Add the filtering data.
		lineToWrite += "\t${row.sample}"
		lineToWrite += "\t${row.timepoint}"
		lineToWrite += "\t${row.tissue}"
		lineToWrite += "\t${row.gplId}"
		
		//If the search keyword is not null, add it.
		if(row.searchKeywordId != null) lineToWrite += "\t${row.searchKeywordId}"
		
		//Add the line separator.
		lineToWrite += System.getProperty("line.separator")
		
		//Write our SNP data row to the file.
		snpOutputFile.write(lineToWrite)
	}
	
}
