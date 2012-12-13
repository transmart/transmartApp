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

import groovy.xml.StreamingMarkupBuilder
import java.util.ArrayList;
import java.util.List;
import am.AmTagTemplate;

class FmFolder implements Buildable{
	
	Long id
	String folderName
	String folderFullName
	Long folderLevel
	String objectUid
	String folderType
	String folderTag
	Boolean activeInd = true

	static mapping = {
		table 'fm_folder'
		version false
		cache true
		sort "folderName"
		id generator: 'sequence', params:[sequence:'seq_fm_id']
		fmFiles joinTable: [name: 'fm_folder_file_association',  key:'folder_id', column: 'file_id'], lazy: false
		amTagTemplates joinTable: [name: 'am_template_association',  key:'object_uid', column: 'tag_template_id'], lazy: false
		columns {
			id column:'folder_id'
		}
	}
	
	static hasMany = [fmFiles: FmFile, amTagTemplates: AmTagTemplate]
	
	static constraints = {
		folderName(maxSize:1000)
		folderFullName(maxSize:1000)
		objectUid(maxSize:300)
		folderType(maxSize:100)
		folderTag(nullable: true, maxSize:20)
	}
	
	def void build(GroovyObject builder)
	{
		def fmFolder = {
			folderDefinition(id:this.id) {
				folderName(this.folderName)
				folderFullName(this.folderFullName)
				folderLevel(this.folderLevel)
				folderType(this.folderType)
//				String[] subFolders=folderDefinitionthis.folderFullName.split(File.pathSeparator);
//				def folders = FolderDefinition.list()
//				folders = FolderDefinition.findAllByFolderName(subFolders[1])

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
	
}
