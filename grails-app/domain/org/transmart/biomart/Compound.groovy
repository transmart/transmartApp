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
  

package org.transmart.biomart

class Compound {
	Long id
	String cntoNumber
	String number
	String casRegistry
	String codeName
	String genericName
	String brandName
	String chemicalName
	String mechanism
	String productCategory
	String description
	String sourceCode
	
	static hasMany=[experiments:Experiment, literatures:Literature]
	
	def getName(){
		if(genericName!=null)
			return genericName;
		if(brandName!=null)
			return brandName;
		if(number!=null)
			return number;
		if(cntoNumber!=null)
			return cntoNumber;
	}
	
	static mapping = {
		table 'BIO_COMPOUND'
		version false
		cache usage:'read-only'
		id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
		columns {
			id column:'BIO_COMPOUND_ID'
			cntoNumber column:'CNTO_NUMBER'
			number column:'JNJ_NUMBER'
			casRegistry column:'CAS_REGISTRY'
			codeName column:'CODE_NAME'
			genericName column:'GENERIC_NAME'
			brandName column:'BRAND_NAME'
			chemicalName column:'CHEMICAL_NAME'
			mechanism column:'MECHANISM'
			productCategory column:'PRODUCT_CATEGORY'
			description column:'DESCRIPTION'
			sourceCode column:'SOURCE_CD'
			experiments joinTable:[name:'BIO_DATA_COMPOUND', key:'BIO_COMPOUND_ID']
			literatures joinTable:[name:'BIO_DATA_COMPOUND', key:'BIO_COMPOUND_ID']
		}
	}
	
	static constraints = {
		cntoNumber(nullable:true, maxSize:400)
		number(nullable:true, maxSize:400)
		casRegistry(nullable:true, maxSize:800)
		codeName(nullable:true, maxSize:400)
		genericName(nullable:true, maxSize:400)
		brandName(nullable:true, maxSize:400)
		chemicalName(nullable:true, maxSize:800)
		mechanism(nullable:true, maxSize:800)
		productCategory(nullable:true, maxSize:400)
		description(nullable:true, maxSize:2000)
		sourceCode(nullable:true, maxSize:100)
	}
	
}
