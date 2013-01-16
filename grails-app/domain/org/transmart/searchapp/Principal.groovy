package org.transmart.searchapp
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
class Principal {
	static transients = ['principalNameWithType']

	Long id
	boolean enabled
	String type
	String name
	String uniqueId =''
	Date dateCreated
	Date lastUpdated
	String description = ''
	String principalNameWithType

	static mapping = {
		table 'SEARCH_AUTH_PRINCIPAL'
		tablePerHierarchy false
		version false
		id generator:'assigned'
		columns
		{
			id column:'ID'
			uniqueId column:'UNIQUE_ID'
			name column:'NAME'
			description column:'DESCRIPTION'
			enabled column:'ENABLED'
			type column:'PRINCIPAL_TYPE'
			dateCreated column:'DATE_CREATED'
			lastUpdated column:'LAST_UPDATED'
		}

	}
	static constraints = {
		//enabled()
		type(nullable:false)
		description(nullable:true, maxSize:255)
		uniqueId(nullable:true)
	}

	def beforeInsert = {
		uniqueId = type+" "+id;
	}

	public String getPrincipalNameWithType(){
		return type+' - '+name;
	}

	public void setPrincipalNameWithType(String n){

	}
}
