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
  

package de
class DeVariantSubjectDetail {
		Long id
		String chromosome 
		Long	position
		String rsID
		String ref
		String alt
		String quality
		String filter 
		String info
		String format
		String dataset
		String variant
	

 static mapping = {
	 table 'DE_VARIANT_SUBJECT_DETAIL'
	 version false	
	 id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
	 columns {
		id column:'VARIANT_SUBJECT_DETAIL_ID'
		dataset column:'DATASET_ID'
		chromosome column:'CHR'
		position column:'POS'
		rsID column:'RS_ID'
		ref column:'REF'
		alt  column:'ALT'
		quality column:'QUAL'
		filter column:'FILTER'
		info column:'INFO'
		format column:'FORMAT'
		variant column:'VARIANT_VALUE', sqlType:'clob'
		}
	}

}