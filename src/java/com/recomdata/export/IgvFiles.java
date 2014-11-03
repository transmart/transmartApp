


/**
 * $Id: IgvFiles.java 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 *
 */
package com.recomdata.export;

import java.io.File;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class IgvFiles {
    protected File tmpDir;
    protected String fileAccessUrl;

    protected File sampleFile;
    protected List<File> dataFileList;
    protected String genomeVersion = "hg19";

    protected File sessionFile;

    public IgvFiles(String gpFileDirName, String gpFileAccessUrl) throws java.io.IOException {
        fileAccessUrl = gpFileAccessUrl;
        // put files in a directory
        tmpDir = new File(gpFileDirName);

        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }

        dataFileList = new ArrayList<File>();
    }

    public File getSampleFile() throws java.io.IOException {
        if (sampleFile == null)
            sampleFile = File.createTempFile("igv_df_", ".sample.txt", tmpDir);
        return this.sampleFile;
    }

    public File createCopyNumberFile() throws java.io.IOException {

        return File.createTempFile("igv_df_", ".cn", tmpDir);
    }

    public File createVCFFile() throws java.io.IOException {
        return File.createTempFile("igv_vcf_", ".cvf", tmpDir);
    }

    public void addFile(File file) {
        dataFileList.add(file);
    }


    public File getSessionFile() throws java.io.IOException {
        if (sessionFile == null)
            sessionFile = File.createTempFile("igv_df_", ".xml", tmpDir);
        return this.sessionFile;
    }

    public static String getFileSecurityHash(File file, String userName) throws Exception {
        System.out.println("datafile length:" + file.length());
        String hashWord = userName + Long.toString(file.length());
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.reset();
        md5.update(hashWord.getBytes());
        //return md5.digest().toString();
        return hashWord;
    }

    public String getFileUrlWithSecurityToken(File file, String userName) throws Exception {
        String hashStr = getFileSecurityHash(file, userName);
        // The URL in the XML document need to have & escaped by &amp;
        // IGV openSession routine uses the extension of a file or a URL to determine the file type. Put the file name at the end of URL.
        return fileAccessUrl + "?user=" + userName +
                "&amp;hash=" + URLEncoder.encode(hashStr, "UTF-8") + "&amp;file=" + URLEncoder.encode(file.getName(), "UTF-8");
    }

    public String getFileUrl(File file) throws Exception {
        return fileAccessUrl + "/" + URLEncoder.encode(file.getName(), "UTF-8");
    }

    List<File> getCopyNumberFileList() {
        return dataFileList;
    }

    List<File> getDataFileList() {
        return dataFileList;
    }
}