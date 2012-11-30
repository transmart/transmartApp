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

class SearchKeyword {
		String keyword
		Long bioDataId
		String uniqueId
		Long id
		String dataCategory
		String dataSource
		String displayDataCategory
		Long ownerAuthUserId
		static hasMany = [terms:SearchKeywordTerm]

 static mapping = {
	 table 'SEARCH_KEYWORD'
	 version false
	 id generator:'sequence', params:[sequence:'SEQ_SEARCH_DATA_ID']
	 columns {
		keyword column:'KEYWORD'
		bioDataId column:'BIO_DATA_ID'
		uniqueId column:'UNIQUE_ID'
		id column:'SEARCH_KEYWORD_ID'
		dataCategory column:'DATA_CATEGORY'
		dataSource column:'SOURCE_CODE'
		displayDataCategory column:'DISPLAY_DATA_CATEGORY'
		ownerAuthUserId column:'OWNER_AUTH_USER_ID'
		terms column:'SEARCH_KEYWORD_ID'
		}
	}
 static constraints = {
	keyword(maxSize:400)
	bioDataId(nullable:true)
	uniqueId(nullable:true, maxSize:1000)
	dataCategory(nullable:true, maxSize:400)
	dataSource(nullable:true, maxSize:200)
	displayDataCategory(nullable:true, maxSize:400)
	ownerAuthUserId(nullable:true)
	}

	int hashCode() {
		// handle special case for TEXT SearchKeywords
		if (id == -1) {
			return keyword.hashCode();
		}
		return id
	}

	boolean equals(obj) {
		// handle special case for TEXT SearchKeywords
		if (id == -1) {
			return keyword == obj?.keyword
		}
		return id == obj?.id
	}
}
