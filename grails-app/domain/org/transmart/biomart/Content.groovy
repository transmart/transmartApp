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
class Content {
	Long id
	String name
	ContentRepository repository
	String location
	String title
	String contentAbstract
	String type

def getAbsolutePath() {
	String root = repository.location == null ? "" : repository.location
	String path = location == null ? "" : location
	String file = name == null ? "" : name
	return root + java.io.File.separator + path + java.io.File.separator + file
}

def getLocationType() {
	return repository.locationType
}

static mapping = {
 table 'BIO_CONTENT'
 version false
 cache usage:'read-only'
 id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
 columns {
	id column:'BIO_FILE_CONTENT_ID'
	name column:'FILE_NAME'
	repository column:'REPOSITORY_ID'
	location column:'LOCATION'
	title column:'TITLE'
	contentAbstract column:'ABSTRACT'
	type column:'FILE_TYPE'
	}
}
static constraints = {
name(nullable:true, maxSize:2000)
location(nullable:true, maxSize:800)
title(nullable:true, maxSize:2000)
contentAbstract(nullable:true, maxSize:4000)
type(maxSize:400)
}

}
