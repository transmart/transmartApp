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
class Role {
	// role types
	static def ADMIN_ROLE = "ROLE_ADMIN"
	static def STUDY_OWNER_ROLE = "ROLE_STUDY_OWNER"
	static def SPECTATOR_ROLE = "ROLE_SPECTATOR"
	static def DS_EXPLORER_ROLE = "ROLE_DATASET_EXPLORER_ADMIN"
	static def PUBLIC_USER_ROLE ="ROLE_PUBLIC_USER"
	static def TRAINING_USER_ROLE ="ROLE_TRAINING_USER"

	static hasMany = [people: AuthUser]

	String description
	String authority

	static mapping = {
		table 'SEARCH_ROLE'
		people joinTable:[name:'SEARCH_ROLE_AUTH_USER', key:'PEOPLE_ID',column:'AUTHORITIES_ID']
	}
	
	static constraints = {
		authority(blank: false, unique: true)
		description()
	}
}
