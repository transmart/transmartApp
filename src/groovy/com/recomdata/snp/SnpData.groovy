package com.recomdata.snp

import com.recomdata.dataexport.util.BiomarkerDataRowProcessor

/**
 * This class will represent a row of SNP data we are exporting to a file.
 * @author MMcDuffie
 *
 */
class SnpData implements BiomarkerDataRowProcessor {
    //The file format will be PATIENT_NUM,GENE,PROBE_ID,GENOTYPE,COPYNUMBER
    void processDataRow(row, snpOutputFile) {
        //Construct the line of data for the file.
        String lineToWrite = "${row.patientNum}\t${row.geneName}\t${row.probeName}\t${row.genotype}\t${row.copyNumber}"

        //Add the filtering data.
        lineToWrite += "\t${row.sample}"
        lineToWrite += "\t${row.timepoint}"
        lineToWrite += "\t${row.tissue}"
        lineToWrite += "\t${row.gplId}"

        //If the search keyword is not null, add it.
        if (row.searchKeywordId != null) lineToWrite += "\t${row.searchKeywordId}"

        //Add the line separator.
        lineToWrite += System.getProperty("line.separator")

        //Write our SNP data row to the file.
        snpOutputFile.write(lineToWrite)
    }

}
