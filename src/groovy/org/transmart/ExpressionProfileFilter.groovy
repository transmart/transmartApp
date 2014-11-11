package org.transmart

/**
 * stores filter params for expression profile filter screen
 */

/**
 * @author $Auther$
 * $Id: ExpressionProfileFilter.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * $Revision: 9178 $
 *
 */
public class ExpressionProfileFilter {

    Long bioDiseaseId
    Long bioMarkerId
    String probeSet

    def filterDisease() {
        return bioDiseaseId != null && bioDiseaseId > 0;
    }

    def filterBioMarker() {
        return bioMarkerId != null && bioMarkerId > 0;
    }

    def filterProbeSet() {
        return probeSet != null && probeSet.length() > 0;

    }

    def reset() {
        bioDiseaseId = null;
        bioMarkerId = null;
        probeSet = null;
    }
}
