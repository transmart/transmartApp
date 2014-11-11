


/**
 * $Id: SurvivalAnalysisFiles.java 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 *
 */
package com.recomdata.export;

import java.io.File;

public class SurvivalAnalysisFiles {

    protected File clsFile;
    protected File dataFile;

    public SurvivalAnalysisFiles() throws java.io.IOException {
        // put files in a directory
        File tmpdir = new File(System.getProperty("java.io.tmpdir") + File.separator + "datasetexplorer");

        if (!tmpdir.exists()) {
            tmpdir.mkdir();
        }
        this.clsFile = File.createTempFile("gp_df_", ".cls", tmpdir);
        this.dataFile = File.createTempFile("gp_df_", ".txt", tmpdir);
    }

    public File getClsFile() {
        return this.clsFile;
    }

    public File getDataFile() {
        return this.dataFile;
    }

}