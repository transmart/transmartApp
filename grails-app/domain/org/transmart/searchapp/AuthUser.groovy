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
class AuthUser extends Principal {
	static transients = ['pass', 'accountExpired', 'accountLocked', 'passwordExpired']
	static hasMany = [authorities: Role, groups:UserGroup]
	static belongsTo = [Role,UserGroup]

	String username
	String userRealName
	String passwd
	String email
	boolean emailShow

	/** plain password to create a MD5 password */
	String pass = '[secret]'
	
	// Used by Security Plugin
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
	
	static mapping = {
		table 'SEARCH_AUTH_USER'
		version false
		columns{
			id column:'ID'
			username column:'USERNAME'
			userRealName column:'USER_REAL_NAME'
			passwd column:'PASSWD'
			email column:'EMAIL'
		    emailShow column:'EMAIL_SHOW'
            authorities joinTable:[name:'SEARCH_ROLE_AUTH_USER', key:'AUTHORITIES_ID', column:'PEOPLE_ID']
			groups joinTable: [name:'SEARCH_AUTH_GROUP_MEMBER', column:'AUTH_GROUP_ID', key: 'AUTH_USER_ID']
		}
	}

	static constraints = {
		username(blank: false, unique: true)
		userRealName(blank: false)
		passwd(blank: false)
		email(nullable:true, maxSize:255)
	}

	def String toString(){
		return userRealName+" - "+username;
	}

	public AuthUser(){
		this.type ='USER';
	}

	def beforeInsert  ={
		if(name==null)	{
			name = userRealName
		}
	}
	
	def beforeUpdate =	{
		name = userRealName
	}
	
	/**
	 * is this user an Admin
	 */
	def isAdmin() {
		def bAdmin = false;
		authorities.each { if(it.authority==Role.ADMIN_ROLE) bAdmin = true; }
		return bAdmin;
	}
}
