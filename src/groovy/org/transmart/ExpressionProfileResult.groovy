package org.transmart

/**
 * stores results for expression profile 
 */

/**
 * $Id: ExpressionProfileResult.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class ExpressionProfileResult {

    // genes retrieved from a search
    def genes = []

    // diseases retrieved from search or gene change
    def diseases = []

    // probesets
    def probeSets = []

    // box plot URL
    def graphURL = null

    // dataset items associated with box plot
    def datasetItems = []

    // experiment count from search
    def profCount = 0
}
