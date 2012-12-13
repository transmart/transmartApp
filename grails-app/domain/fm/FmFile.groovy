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

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

class FmFile {
	
	Long id
	String displayName
	String originalName
	Long fileVersion = 1l
	String fileType
	Long fileSize
	String filestoreLocation
	String filestoreName
	String linkUrl
	Boolean activeInd = Boolean.TRUE
	Date createDate = new Date()
	Date updateDate = new Date()

	static mapping = {
		table 'fm_file'
		version false
		cache true
		sort "displayName"
		id generator: 'sequence', params:[sequence:'seq_fm_id']
		columns {
			id column:'file_id'
		}
	}

	static contraints = {
		displayName(maxSize:1000)
		orginalName(maxSize:1000)
		fileType(nullable: true, maxSize:100)
		fileSize(nullable: true)
		filestoreLocation(nullable: true, maxSize:1000)
		filestoreName(nullable: true, maxSize:1000)
		linkUrl(nullable: true, maxSize:1000)
	}
	
}
