package com.recomdata.transmart.externaltool

import com.recomdata.export.IgvFiles
import org.transmartproject.core.exceptions.UnexpectedResultException;

class IgvDataService {

    def createJNLPasString(webRootDir, sessionFileURL) {
        // Create a new file instance
        def f = new File(webRootDir + "/files/" + "igv.jnlp")
        def ftext = f.text;

        StringBuilder s = new StringBuilder();

        // session file url
        s.append("\t<argument>")
        s.append(sessionFileURL)
        s.append("</argument>\n");

        s.append("</application-desc>")

        ftext = ftext.replaceAll("</application-desc>", s.toString())

        //println("jnlp file:"+ftext)
        return ftext;

    }

    def createSessionURL(IgvFiles igvFiles, String userName, String locus) {


        File sessionFile = igvFiles.getSessionFile();
        sessionFile << "<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n<Session genome='hg19'"
        if (locus != null) {
            sessionFile << " locus='" + locus + "' "
        }
        sessionFile << " version='4'>\n<Resources>\n";
        List<File> fileList = igvFiles.getDataFileList();

        /* sessionFile<<"<Resource path='http://localhost:8080/transmartApp/data/test.vcf'/>";
         session files need to be public available so that igv can check the idx file as well
         currently it's in the data folder under tomcat
         need to change to a conf location so that apache can manage the doc root
        */

        for (File file : fileList) {

            String fileUrl = igvFiles.getFileUrl(file);
            sessionFile << "<Resource path='" + fileUrl + "'/>\n";
            if (isVCFfile(file)) {
                createVCFIndexFile(file);
            }
        }

        sessionFile << "</Resources>\n</Session>";
        return igvFiles.getFileUrl(sessionFile);

    }

    def isVCFfile(File file) {
        println(file.getName())
        return file.getName().toLowerCase().endsWith("vcf");

    }


    def createVCFIndexFile(File vcfFile) {
        String[] argv = ["index", vcfFile.absolutePath]

        Class igvToolsClass
        try {
            igvToolsClass = Class.forName 'org.broad.igv.tools.IgvTools'
        } catch (e) {
            log.error 'Could not load IgvTools. The igvtools jar is not ' +
                    'bundled anymore. You will have to add it as a ' +
                    'dependency to the project'
            throw e
        }

        igvToolsClass.newInstance().run argv

        File idxFile = File(vcfFile.absolutePath + ".idx")

        idxFile.exists() || {
            throw new UnexpectedResultException('Could not create index ' +
                    "file for ${vcfFile.absolutePath}")
        }()

        idxFile
    }

}
