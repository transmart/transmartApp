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

import com.recomdata.util.IExcelProfile

class BioAssayAnalysisDataIdx implements IExcelProfile {
		
	Long id
	String ext_type
	String field_name
	Integer field_idx
	Integer display_idx
		
	static mapping = {
	 table name:'BIO_ASY_ANALYSIS_DATA_IDX', schema:'BIOMART'
	 version false
	 id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
	 columns {
		id column:'BIO_ASY_ANALYSIS_DATA_IDX_ID'
		ext_type column:'EXT_TYPE'
		field_name column:'FIELD_NAME'
		field_idx column:'FIELD_IDX'
		display_idx column:'DISPLAY_IDX'
		}
	}

	/**
	 * Get values to Export to Excel
	 */
	public List getValues() {
		return [ext_type,field_name,field_idx,display_idx]
	}
}