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
  
package fm


import java.util.ArrayList;
import java.util.List;
import groovy.lang.Buildable;
import groovy.lang.GroovyObject;
import groovy.xml.StreamingMarkupBuilder;

class FmFile implements Buildable{
	
	Long id
	String displayName
	String originalName
	String fileVersion
	String description = "test description"
	Long fileSize	
	String fileType
	String filestoreLocation
	String filestoreName
	String linkUrl
	Date uploadDate = new Date()
	Boolean activeInd = Boolean.TRUE

	static mapping = {
		table 'fm_file'
		version false
		cache true
		sort "displayName"
		columns { id column:'file_id' }
	}
	
	static constraints = {
		displayName(maxSize:200)
		originalName(maxSize:200)
		filestoreLocation(nullable:true,maxSize:500)
		filestoreName(nullable:true,maxSize:50)
		
		}
	
	def void build(GroovyObject builder)
	{
		def fmFile = {
			
		}

		fmFile.delegate = builder
		fmFile()

	}
}
