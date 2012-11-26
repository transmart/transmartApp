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
import fm.FmFolderAssociation;

class FmFolder implements Buildable{
	
	Long id
	String folderName
	String folderFullName
	String folderTag
	Long folderLevel
	String objectUid
	String folderType
	Boolean activeInd = Boolean.TRUE
	
	

	static mapping = {
		table 'fm_folder'
		version false
		cache true
		sort "folderName"
		fmFiles joinTable: [name: 'fm_folder_file_association',  key:'folder_id', column: 'file_id'], lazy: false
		amTagTemplates joinTable: [name: 'am_template_association',  key:'object_uid', column: 'tag_template_id'], lazy: false
		
		columns { id column:'folder_id' }
	}
	
	static hasOne = [fmFolderAssociation: FmFolderAssociation]	
	static hasMany = [fmFiles: FmFile, amTagTemplates: AmTagTemplate]
	
	

	
	static constraints = {
		folderName(maxSize:200)
		folderFullName(maxSize:200)
		objectUid(maxSize:300)
		folderType(maxSize:50)
		folderTag(nullable:true,maxSize:50)
		}
	
	def void build(GroovyObject builder)
	{
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
/*		                 addresses {
		                     this.addresses.each{address ->
		                         out << address
		                     }
		                     *
		                 }
		                 */
             }
         }
		
         fmFolder.delegate = builder
         fmFolder()
	}
}
