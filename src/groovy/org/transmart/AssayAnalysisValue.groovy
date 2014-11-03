package org.transmart


import org.transmart.biomart.BioMarker

/**
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 * $Id: AssayAnalysisValue.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 *
 */
public class AssayAnalysisValue implements Comparable {

    def analysisData
    BioMarker bioMarker

    // indicator for the up/down regulation (i.e. gene lists and signatures). If null implies
    // we don't care about the up/down regulation such as for a pathway
    Double valueMetric

    /**
     * comparable interface implementation, sort on NPV
     */
    public int compareTo(Object obj) {
        // verify correct object type
        if (!(obj instanceof AssayAnalysisValue)) return -1

        // compare objects
        AssayAnalysisValue compare = (AssayAnalysisValue) obj;
        Double thisScore = analysisData.teaNormalizedPValue
        Double compScore = compare.analysisData.teaNormalizedPValue

        // handle invalid values
        if (compScore == null && thisScore != null) return 1;
        if (thisScore == null && compScore != null) return -1;
        if (thisScore == null && compScore == null) return 0;

        return (thisScore.compareTo(compScore))
    }

}
