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
	String uniqueId
	
	static hasMany = [folders: FmFolder] //Should probably only have one, but Grails doesn't allow join table on one-many

	static belongsTo = FmFolder
	
	static transients = ['folder', 'uniqueId']

	static mapping = {
		table 'fm_file'
		version false
		cache true
		sort "displayName"
		id column:'file_id', generator: 'sequence', params:[sequence:'seq_fm_id']
		folders joinTable: [name: 'fm_folder_file_association',  key:'file_id', column: 'folder_id']
	}
	
	static constraints = {
		displayName(maxSize:1000)
		originalName(maxSize:1000)
		fileType(nullable:true, maxSize:100)
		fileSize(nullable:true)
		filestoreLocation(nullable:true, maxSize:1000)
		filestoreName(nullable:true, maxSize:1000)
		linkUrl(nullable:true, maxSize:1000)
	}

	/**
	 * Gets file's associated folder.
	 * @return
	 */
	FmFolder getFolder() {
		if (folders != null && !folders.isEmpty()) {
			return folders.iterator().next();
		}
		return null;
	}
	
	/**
	 * Sets file's associated folder.
	 * @param folder
	 */
	def setFolder(FmFolder folder) {
		this.addToFolders(folder);
	}
	
	/**
	 * Use transient property to support unique ID for folder.
	 * @return folder's uniqueId
	 */
	String getUniqueId() {
		if (uniqueId == null) {
			FmData data = FmData.get(id);
			if (data != null) {
				uniqueId = data.uniqueId
				return data.uniqueId;
			}
			return null;
		}
		return uniqueId;
	}

	/**
	 * Find file by its uniqueId
	 * @param uniqueId
	 * @return file with matching uniqueId or null, if match not found.
	 */
	static FmFile findByUniqueId(String uniqueId) {
		FmFile file;
		FmData data = FmData.findByUniqueId(uniqueId);
		if (data != null) {
			file = FmFile.get(data.id);
		}
		return file;
	}

}
