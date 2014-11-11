package org.transmart

/**
 * @author $Author: mmcduffie $
 * $Id: ExperimentAnalysisFilter.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @version $Revision: 9178 $
 *
 */

public class ExperimentAnalysisFilter {

    String dataSource
    Long bioDiseaseId
    String species
    String expDesign
    String expType
    Long bioCompoundId
    String tissueType
    String cellLine
    String expDescrKeyword
    //String platformOrganism
    //Double foldChange = 1.2
    //Double pValue = 0.05
    Double foldChange
    Double pvalue

    def isUsed() {
        return validString(species) || validString(expDesign) || validString(expType) || validString(dataSource) || bioCompoundId != null || bioDiseaseId != null || foldChange != null || pvalue != null || validString(cellLine);
    }

    def filterFoldChange() {
        return foldChange != null && foldChange > 0;
    }

    def filterPValue() {
        return pvalue != null && pvalue > 0;
    }

    def filterDisease() {
        return bioDiseaseId != null && bioDiseaseId > 0;
    }

    def filterCompound() {
        return bioCompoundId != null && bioCompoundId > 0;
    }

    def filterSpecies() {
        return validString(species);
    }

    def filterExpDesign() {
        return validString(expDesign);
    }

    def filterExpType() {
        return validString(expType);
    }

    def filterDataSource() {
        return validString(dataSource)
    }

    def validString(String s) {
        return s != null && s.length() > 0;
    }
}
