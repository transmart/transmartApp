package org.transmart

/**
 * $Id: TrialFilter.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 * */
class TrialFilter {

    def selectedtrials = []
    boolean newFilter = true;

    String platform
    //Double foldChange = 1.2
    //Double pValue = 0.05
    Double foldChange
    Double pvalue
    Double rvalue
    Long bioDiseaseId
    Long bioCompoundId
    String phase
    String studyType
    String studyDesign
    String status

    def hasSelectedTrials() {
        return selectedtrials.size() > 0;
    }

    def createTrialInclause() {
        def s = new StringBuilder()
        for (n in selectedtrials) {
            if (s.length() > 0) {
                s.append(",");
            }
            s.append("'").append(n).append("'");
        }
        return s.toString()
    }

    def hasPlatform() {
        return (platform != null && platform.length() > 0);
    }

    def hasFoldChange() {
        return (foldChange != null && foldChange > 0);
    }

    def hasPValue() {
        return (pvalue != null && pvalue > 0);
    }

    def hasRValue() {
        return (rvalue != null);

    }

    def hasDisease() {
        return (bioDiseaseId != null && bioDiseaseId > 0);
    }

    def hasCompound() {
        return (bioCompoundId != null && bioCompoundId > 0);
    }

    def hasPhase() {
        return (phase != null && phase.length() > 0);
    }

    def hasStudyType() {
        return (studyType != null && studyType.length() > 0);
    }

    def hasStudyDesign() {
        return (studyDesign != null && studyDesign.length() > 0);
    }

//	def createStudyTypeInValues(){
//		return createMultipleStringInValues(studyTypes)
//	}
//
//	def createStudyDesignInValues(){
//		return createMultipleStringInValues(studyDesigns)
//	}
//
//	def createMultipleStringInValues(stringList){
//		def s = new StringBuilder()
//		int i = 0;
//		for(n in stringList){
//			if(i>0)
//				s.append(",")
//			s.append("'")
//			s.append(n.toString())
//			s.append("'")
//			i++;
//		}
//		return s.toString()
//	}

    def createListTrialInclause() {
        def s = []
        // int i = 0;
        for (n in selectedtrials) {
            s.add(n.toString())
        }
        return s
    }

    def marshal() {

    }
}
