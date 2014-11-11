package com.recomdata.transmart.data.export.util

/**
 * This interface provides a method to write a data object to a file.
 * @author MMcDuffie
 *
 */
interface BiomarkerDataRowProcessor {
    void processDataRow(row, resultingObject)
}
