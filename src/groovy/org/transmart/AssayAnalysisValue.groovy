package org.transmart
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
  

import org.transmart.biomart.BioAssayAnalysisData;

import org.transmart.biomart.BioMarker

/**
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 * $Id: AssayAnalysisValue.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 *
 */
public class AssayAnalysisValue implements Comparable {

	def analysisData
	BioMarker bioMarker

	// indicator for the up/down regulation (i.e. gene lists and signatures). If null implies
	// we don't care about the up/down regulation such as for a pathway
	Double valueMetric

	/**
	 * comparable interface implementation, sort on NPV
	 */
	public int compareTo(Object obj) {
		// verify correct object type
		if (!(obj instanceof AssayAnalysisValue)) return -1

		// compare objects
		AssayAnalysisValue compare = (AssayAnalysisValue) obj;
		Double thisScore = analysisData.teaNormalizedPValue
		Double compScore = compare.analysisData.teaNormalizedPValue

		// handle invalid values
		if(compScore==null && thisScore!=null) return 1;
		if(thisScore==null && compScore!=null) return -1;
		if(thisScore==null && compScore==null) return 0;

		return (thisScore.compareTo(compScore))
	}

}
