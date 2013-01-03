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
  

package transmartapp

import fm.FmFile

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream

class FileExportController {

    def add = {
		def paramMap = params
		def idList = params.id.split(',')
		
		def exportList = session['export']
		
		if (exportList == null) {
			exportList = [];
		}
		for (id in idList) {
			if (id && !exportList.contains(id)) {
				exportList.push(id)
			}
		}
		session['export'] = exportList;
		
		//Render back the number to display
		render (status: 200, text: exportList.size())
	}
	
	def remove = {
		def idList = params.list('id')
		
		def exportList = session['export']
		
		if (exportList == null) {
			exportList = [];
		}
		for (id in idList) {
			if (id && exportList.contains(id)) {
				exportList.remove(id)
			}
		}
		session['export'] = exportList;
		
		//Render back the number to display
		render (status: 200, text: exportList.size())
	}
	
	def view = {
		
		def exportList = session['export']
		def files = []
		for (id in exportList) {
			FmFile f = FmFile.get(id)
			if (f) {
				files.push([id: f.id, fileType: f.fileType, displayName: f.displayName, folder: f.fmFolder.folderFullName[0]])
			}
		}
		files.sort { a, b ->
			if (!a.folder.equals(b.folder)) {
				return a.folder.compareTo(b.folder)
			}
			return a.displayName.compareTo(b.displayName)
		}
		
		render(template: '/RWG/export', model: [files: files])
	}
	
	def export = {
		
		try {
			response.setHeader('Content-disposition', 'attachment; filename=export.zip')
			response.contentType = 'application/zip'
			
			//File fileZip = new File();
			
			//Final export list comes from selected checkboxes
			def exportList = params.id.split(",")
			
			//FileOutputStream fwZip = new FileOutputStream(fileZip);
			def zipStream = new ZipOutputStream(response.outputStream);
			
			for (f in exportList) {
				FmFile fmFile = FmFile.get(f)
				File file = new File(fmFile.filestoreLocation + "/" + fmFile.filestoreName)
				if (file.exists()) {
					def fileEntry = new ZipEntry(fmFile.displayName)
					zipStream.putNextEntry(fileEntry)
					file.withInputStream({is -> zipStream << is})
					zipStream.closeEntry()
				}
			}
			
			zipStream.flush();
			zipStream.close();
			
			//byte[] fileBytes = fileZip.getBytes();
	
			//response.outputStream = zipStream
		}
		catch (Exception e) {
			render(status: 500, text: e.getMessage())
		}
	}
}
