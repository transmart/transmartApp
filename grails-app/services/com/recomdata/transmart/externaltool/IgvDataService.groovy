package com.recomdata.transmart.externaltool;

import com.recomdata.export.IgvFiles;

class IgvDataService {
	
	def createJNLPasString(webRootDir, sessionFileURL){
		// Create a new file instance
		def f = new File (webRootDir + "/files/" + "igv.jnlp")
		def ftext = f.text;
	
		StringBuilder s = new StringBuilder();
		
		// session file url
		s.append("\t<argument>")
		s.append(sessionFileURL)
		s.append("</argument>\n");
		
		s.append("</application-desc>")
		
		ftext= ftext.replaceAll("</application-desc>",s.toString())
		
		
		println("jnlp file:"+ftext)
		return ftext;
		
	}
	
	def createSessionURL(IgvFiles igvFiles, String userName){
		
		File sessionFile = igvFiles.getSessionFile();
		sessionFile << "<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n<Session genome='hg19' version='3'>\n<Resources>\n";
		List<File> fileList = igvFiles.getDataFileList();
		for (File file : fileList) {
			String fileUrl = igvFiles.getFileUrlWithSecurityToken(file, userName);
			sessionFile << "<Resource path='" + fileUrl + "'/>\n";
		}
		sessionFile << "</Resources>\n</Session>";
		return igvFiles.getFileUrlWithSecurityToken(sessionFile, userName);

	}
}