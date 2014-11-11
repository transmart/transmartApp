


/**
 * $Id: GwasFiles.java 10098 2011-10-19 18:39:32Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 10098 $
 *
 */
package com.recomdata.export;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class GwasFiles {
    File tmpDir;
    String fileAccessUrl;
    String fileNameRoot;

    File mapFile;
    File pedFile;
    File assocFile;
    File reportFile;
    File sessionFile;

    List<Integer> patientCountList;
    List<Integer> datasetCountList;
    List<String> chromList;

    public GwasFiles(String gpFileDirName, String gpFileAccessUrl) throws java.io.IOException {
        if (gpFileDirName.endsWith(File.separator) == false) {
            gpFileDirName += File.separator;
        }

        fileAccessUrl = gpFileAccessUrl;
        // put files in a directory
        tmpDir = new File(gpFileDirName);

        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
        reportFile = File.createTempFile("gwas_", ".report.htm", tmpDir);
        String reportFileName = reportFile.getName();
        int idx = reportFileName.indexOf(".");
        fileNameRoot = reportFileName.substring(0, idx);
        mapFile = new File(gpFileDirName + fileNameRoot + ".map");
        pedFile = new File(gpFileDirName + fileNameRoot + ".ped");
        sessionFile = new File(gpFileDirName + fileNameRoot + ".session.xml");

        patientCountList = new ArrayList<Integer>();
        datasetCountList = new ArrayList<Integer>();
        chromList = new ArrayList<String>();
    }

    public File getMapFile() {
        return mapFile;
    }

    public File getPedFile() {
        return pedFile;
    }

    public File getReportFile() {
        return reportFile;
    }

    public File getAssocFile() {
        return assocFile;
    }

    public File getSessionFile() throws java.io.IOException {
        return sessionFile;
    }

    public List<Integer> getPatientCountList() {
        return patientCountList;
    }

    public List<Integer> getDatasetCountList() {
        return datasetCountList;
    }

    public List<String> getChromList() {
        return chromList;
    }

    public void setChromList(List<String> chromList) {
        this.chromList = chromList;
    }

    public String getFileUrlWithSecurityToken(File file, String userName) throws Exception {
        String hashStr = userName + Long.toString(file.length());
        // The URL in the XML document need to have & escaped by &amp;
        // IGV openSession routine uses the extension of a file or a URL to determine the file type. Put the file name at the end of URL.
        return fileAccessUrl + "?user=" + userName +
                "&amp;hash=" + URLEncoder.encode(hashStr, "UTF-8") + "&amp;file=" + URLEncoder.encode(file.getName(), "UTF-8");
    }


}