


package com.recomdata.export;

import java.io.File;

public class PlinkFiles {

    protected File pedFile;
    protected File mapFile;
    protected File famFile;
    protected File phenoFile;

    public PlinkFiles() throws java.io.IOException {
        // put files in a directory
        File tmpdir = new File(System.getProperty("java.io.tmpdir")
                + File.separator + "datasetexplorer");

        if (!tmpdir.exists()) {
            tmpdir.mkdir();
        }
        this.pedFile = File.createTempFile("plink_", ".ped", tmpdir);
        this.mapFile = File.createTempFile("plink_", ".map", tmpdir);
        this.famFile = File.createTempFile("plink_", ".fam", tmpdir);
        this.phenoFile = File.createTempFile("plink_", ".peno", tmpdir);
    }

    public File getPedFile() {
        return this.pedFile;
    }

    public File getMapFile() {
        return this.mapFile;
    }

    public File getFamFile() {
        return this.famFile;
    }

    public File getPhenoFile() {
        return this.phenoFile;
    }
}