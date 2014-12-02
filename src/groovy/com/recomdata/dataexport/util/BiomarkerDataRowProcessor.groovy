package com.recomdata.dataexport.util

/**
 * This interface provides a method to write a data object to a file.
 * @author MMcDuffie
 *
 */
interface BiomarkerDataRowProcessor {
    void processDataRow(row, resultingObject)
}
