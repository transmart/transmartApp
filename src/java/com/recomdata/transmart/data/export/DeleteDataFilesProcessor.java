


package com.recomdata.transmart.data.export;

import com.recomdata.transmart.data.export.util.FTPUtil;
import grails.util.Holders;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Map;


public class DeleteDataFilesProcessor {
    private static org.apache.log4j.Logger log = Logger
            .getLogger(DeleteDataFilesProcessor.class);

    @SuppressWarnings("rawtypes")
    private static final Map config = Holders.getFlatConfig();
    private static final String TEMP_DIR = (String) config.get("com.recomdata.plugins.tempFolderDirectory");

    public boolean deleteDataFile(String fileToDelete, String directoryToDelete) {
        boolean fileDeleted = false;
        try {
            if (StringUtils.isEmpty(fileToDelete) || StringUtils.isEmpty(directoryToDelete)) {
                throw new Exception("Invalid file or directory name. Both are needed to delete data for an export job");
            }
            String dirPath = TEMP_DIR + File.separator + directoryToDelete;
            @SuppressWarnings("unused")
            boolean directoryDeleted = deleteDirectoryStructure(new File(dirPath));

            fileDeleted = FTPUtil.deleteFile(fileToDelete);
            //If the file was not found at the FTP location try to delete it from the server Temp dir
            if (!fileDeleted) {
                String filePath = TEMP_DIR + File.separator + fileToDelete;
                File jobZipFile = new File(filePath);
                if (jobZipFile.isFile()) {
                    jobZipFile.delete();
                    fileDeleted = true;
                }
            }
        } catch (Exception e) {
            log.error("Failed to delete the data for job " + directoryToDelete);
            log.error(e.getMessage());
        }
        return fileDeleted;
    }

    private boolean deleteDirectoryStructure(File directory) {
        if (directory.exists()) {
            File[] dirChildren = directory.listFiles();
            for (int i = 0; i < dirChildren.length; i++) {
                if (dirChildren[i].isDirectory()) {
                    deleteDirectoryStructure(dirChildren[i]);
                } else {
                    dirChildren[i].delete();
                }
            }
        }
        return directory.delete();
    }

}
