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
	 Long userId   // would like to reference the AuthUser class, but can't because it is in default package -- for now just reference the id until we move to Grails 2
	 String name
	 String description
	 String criteria
	 
	 static mapping = {
		 table 'SAVED_FACETED_SEARCH'
		 version false
		 id generator:'sequence', params:[sequence:'SEQ_SAVED_FACETED_SEARCH_ID']
		 columns {
			id column:'SAVED_FACETED_SEARCH_ID'
			userId column: "USER_ID"
			name column: 'NAME'
			description column:'DESCRIPTION'
			criteria column:'CRITERIA'
		 }
	 }
	 
	 static constraints = {
		 userId(nullable:false)
		 name(nullable:false,maxSize:50)
		 description(nullable:false,maxSize:1000)
		 criteria(nullable:false,maxSize:4000)
		 name (unique:'userId')
	 }
		
 }
