


/**
 * $Id: SnpViewerFiles.java 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 *
 */
package com.recomdata.export;

import java.io.File;

public class SnpViewerFiles {

    protected File sampleFile;
    protected File dataFile;

    public SnpViewerFiles() throws java.io.IOException {
        // put files in a directory
        File tmpdir = new File(System.getProperty("java.io.tmpdir") + File.separator + "datasetexplorer");

        if (!tmpdir.exists()) {
            tmpdir.mkdir();
        }
        this.sampleFile = File.createTempFile("gp_df_", ".sample.txt", tmpdir);
        this.dataFile = File.createTempFile("gp_df_", ".xcn", tmpdir);
    }

    public File getSampleFile() {
        return this.sampleFile;
    }

    public File getDataFile() {
        return this.dataFile;
    }

}