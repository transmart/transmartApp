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
  

package i2b2
class OntNode {
		Long hlevel
		String id
		//String fullname
		String name
		String synonymcd
		String visualattributes
		Long   totalnum
		String basecode
		String metadataxml
		String facttablecolumn
		String tablename
		String columnname
		String columndatatype
		String operator
		String dimcode
		String comment
		String tooltip
		Date updatedate
		Date downloaddate
		Date importdate
		String sourcesystemcd
		String valuetypecd
		String securitytoken
		List tags=[]
		static hasMany =[tags:OntNodeTag]
 static mapping = {
	 table 'I2B2_SECURE'
	 version false
	 id column: 'C_FULLNAME'
	 columns {
		hlevel column:'C_HLEVEL'
		//fullname column:'C_FULLNAME'
		name column:'C_NAME'
		synonymcd column:'C_SYNONYM_CD'
		visualattributes column:'C_VISUALATTRIBUTES'
		totalnum column:'C_TOTALNUM'
		basecode column:'C_BASECODE'
		metadataxml column:'C_METADATAXML'
		facttablecolumn column:'C_FACTTABLECOLUMN'
		tablename column:'C_TABLENAME'
		columnname column:'C_COLUMNNAME'
		columndatatype column:'C_COLUMNDATATYPE'
		operator column:'C_OPERATOR'
		dimcode column:'C_DIMCODE'
		comment column:'C_COMMENT'
		tooltip column:'C_TOOLTIP'
		updatedate column:'UPDATE_DATE'
		downloaddate column:'DOWNLOAD_DATE'
		importdate column:'IMPORT_DATE'
		sourcesystemcd column:'SOURCESYSTEM_CD'
		valuetypecd column:'VALUETYPE_CD'
		securitytoken column:'SECURE_OBJ_TOKEN'
		//tags joinTable:[name:'I2B2_TAGS', key:'PATH', column: 'TAG_ID']
		tags column: 'PATH' //joinTable: false
		}
	}
 static constraints = {
	hlevel(nullable:true)
	//fullname(nullable:false, maxSize:900)
	name(nullable:true, maxSize:2000)
	synonymcd(nullable:true, maxSize:1)
	visualattributes(nullable:true, maxSize:3)
	totalnum(nullable:true)
	basecode(nullable:true, maxSize:450)
	metadataxml(nullable:true, maxSize:4000)
	facttablecolumn(nullable:true, maxSize:50)
	tablename(nullable:true, maxSize:50)
	columnname(nullable:true, maxSize:50)
	columndatatype(nullable:true, maxSize:50)
	operator(nullable:true, maxSize:10)
	dimcode(nullable:true, maxSize:900)
	comment(nullable:true, maxSize:4000)
	tooltip(nullable:true, maxSize:900)
	updatedate(nullable:true)
	downloaddate(nullable:true)
	importdate(nullable:true)
	sourcesystemcd(nullable:true, maxSize:50)
	valuetypecd(nullable:true, maxSize:50)
	securitytoken(nullable:true, maxSize:50)
	}
}