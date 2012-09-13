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
   
package search

class SavedFacetedSearch {
	  
  	 Long id
	 Long userId   // would like to reference the AuthUser class, but can't because it is in default package -- for now just reference the id 
	 String name
	 String criteria
	 Date createDt
	 Date modifiedDt
	 
	 static mapping = {
		 table 'SAVED_FACETED_SEARCH'
		 version false
		 id generator:'sequence', params:[sequence:'SEQ_SAVED_FACETED_SEARCH_ID']
		 columns {
			id column:'SAVED_FACETED_SEARCH_ID'
			userId column: "USER_ID"
			name column: 'NAME'
			criteria column:'CRITERIA'
			createDt column:"CREATE_DT"
			modifiedDt column:"MODIFIED_DT"
		 }
	 }
	 
	 static constraints = {
		 userId(nullable:false)
		 name(nullable:false,blank: false,maxSize:100)
		 criteria(nullable:false,blank: false,maxSize:4000)
		 name (unique:'userId')
		 createDt(nullable:true)
		 modifiedDt(nullable:true)
	 }
		
 }
