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

import com.recomdata.util.FolderType

class FmFolder implements Buildable {
	
	Long id
	String folderName
	String folderFullName
	Long folderLevel
	String folderType
	String folderTag
	String description
	Boolean activeInd = Boolean.TRUE
	String uniqueId
	String pluralFolderTypeName

	static belongsTo = [parent: FmFolder]
	
	static hasMany = [fmFiles: FmFile, children: FmFolder]
	
	static transients = ['uniqueId', 'pluralFolderTypeName']
	
	static mapping = {
		table 'fm_folder'
		version false
		sort "folderName"
		id column:'folder_id', generator: 'sequence', params:[sequence:'seq_fm_id']
		fmFiles joinTable: [name: 'fm_folder_file_association',  key:'folder_id', column: 'file_id'], lazy: false, cascade: "all-delete-orphan"
	}
	
	static constraints = {
		folderName(blank:false, maxSize:1000)
		folderFullName(nullable: true, maxSize:1000)
		folderType(blank:false, maxSize:100)
		folderTag(nullable: true, maxSize:50)
		description(blank:false, maxSize: 2000)
		parent(nullable: true)
	}

	/**
	 * Use transient property to support unique ID for folder.
	 * @return folder's uniqueId
	 */
	String getUniqueId() {
		if (uniqueId == null) {
			if(id)
			{
				FmData data = FmData.get(id);
				if (data != null) {
					uniqueId = data.uniqueId
					return data.uniqueId;
				}
				return null;
			}
			else
			{
				return null;
			}
		}  
		return uniqueId;
	}
	
	String getPluralFolderTypeName()
	{
		if(this.folderType == FolderType.ANALYSIS.name())
		{
			return "ANALYSES"
		}
		
		if(this.folderType == FolderType.PROGRAM.name() || this.folderType == FolderType.ASSAY.name() || this.folderType == FolderType.FOLDER.name())
		{
			return this.folderType + "S"
		}

		if(this.folderType == FolderType.STUDY.name())
		{
			return "STUDIES"
		}

	}
		
	/**
	 * Find folder by its uniqueId
	 * @param uniqueId
	 * @return folder with matching uniqueId or null, if match not found.
	 */
	static FmFolder findByUniqueId(String uniqueId) {
		FmFolder folder;
		FmData data = FmData.findByUniqueId(uniqueId);
		if (data != null) {
			folder = FmFolder.get(data.id);
		}
		return folder;
	}
	
	
	/**
	 * Return true if this folder has any folders that name it as a parent
	 * @return true if this folder has children, false otherwise
	 */
	boolean hasChildren() {
		def children = FmFolder.createCriteria().list {
			eq('parent', this)
			eq('activeInd', true)
		}
		if (children) { return true }
		return false
	}
	
	def void build(GroovyObject builder) {
        def fmFolder = {
             folderDefinition(id:this.id){
				 folderName(this.folderName)
				 folderFullName(this.folderFullName)
				 folderLevel(this.folderLevel)
				 folderType(this.folderType)
				 
				 List<FmFolder> subFolderList = FmFolder.findAll("from FmFolder as fd where fd.folderFullName like :fn and fd.folderLevel = :fl",
				 [fn:this.folderFullName+"%", fl: (this.folderLevel + 1)])

				 unescaped << '<fmFolders>'
				 subFolderList.each {					 
					 	println it
						out << it
				 	}
				 unescaped << '</fmFolders>'
             }
         }
		
         fmFolder.delegate = builder
         fmFolder()

	}
	
	/**
	* override display
	*/
   public String toString() {
	   StringBuffer sb = new StringBuffer();
	   sb.append("ID: ").append(this.id).append(", Folder Name: ").append(this.folderName);
	   sb.append(", Folder Full Name: ").append(this.folderFullName).append(", Folder Level: ").append(this.folderLevel);
	   sb.append(", Folder Type: ").append(this.folderType).append(", uniqueId: ").append(this.uniqueId).append(", Description: ").append(this.description);
	   return sb.toString();
   }
   
   

}
