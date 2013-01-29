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
  

package com.recomdata.dataexport.util

import org.apache.commons.lang.StringUtils;

class ExportUtil {

	public static String getShortConceptPath(String conceptPath, removalArr) {
		def arr = StringUtils.split(conceptPath, "\\")
		def valList = []
		//Remove upto Study-name and any string values specified in the removalArr
		if (arr.length > 2) arr.eachWithIndex { val, i ->
			def valShouldBeRemoved = false
			removalArr.each { removalVal -> if (StringUtils.equalsIgnoreCase(removalVal, val)) {
				valShouldBeRemoved = true
				return
			} }
			  
			if (i > 1 && !valShouldBeRemoved) { 
				valList.add(val)
			} else if (valShouldBeRemoved) {
				def j = i
				while (j < arr.length) arr[j++] = ''
			}
		}
		
		def shortenedConceptPath = StringUtils.join(valList, '\\')
		shortenedConceptPath = StringUtils.leftPad(shortenedConceptPath, shortenedConceptPath.length()+1, '\\')
		
		return shortenedConceptPath
	}
	
	public static String getSampleValue(String value, String sampleType, String timepoint, String tissueType) {
		def retVal = null;
		if (StringUtils.equalsIgnoreCase(value, "E") || StringUtils.equalsIgnoreCase(value, "normal")) {
			def retVals = []
			if (null != sampleType && StringUtils.isNotEmpty(sampleType)) retVals.add(sampleType)
			if (null != timepoint && StringUtils.isNotEmpty(timepoint)) retVals.add(timepoint)
			if (null != tissueType && StringUtils.isNotEmpty(tissueType)) retVals.add(tissueType)
			retVal = StringUtils.join(retVals, "/")
		} else {
			retVal = value
		}
		return retVal
	}
}
