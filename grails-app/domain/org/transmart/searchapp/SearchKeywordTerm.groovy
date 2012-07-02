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

class SearchKeywordTerm {
		Long ownerAuthUserId
		String keywordTerm
		SearchKeyword searchKeyword
		Long rank
		Long id
		Long termLength
		
		static belongsTo = [ searchKeyword:SearchKeyword ]
		
 static mapping = {
	 table 'SEARCH_KEYWORD_TERM'
	 version false
	 id generator:'sequence', params:[sequence:'SEQ_SEARCH_DATA_ID']
	 columns {
		ownerAuthUserId column:'OWNER_AUTH_USER_ID'
		keywordTerm column:'KEYWORD_TERM'
		searchKeyword column:'SEARCH_KEYWORD_ID'
		rank column:'RANK'
		id column:'SEARCH_KEYWORD_TERM_ID'
		termLength column:'TERM_LENGTH'
		}
	}
		
 static constraints = {
	ownerAuthUserId(nullable:true)
	keywordTerm(maxSize:200)
	rank(nullable:true)
	termLength(nullable:true)
	}
}
