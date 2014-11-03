package org.transmart

/**
 * $Id: LiteratureFilter.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
class LiteratureFilter {

    String dataType

    // Reference
    Long bioDiseaseId
    Set diseaseSite = new HashSet()
    Set componentList = new HashSet()
    List pairCompList = new ArrayList()
    List pairGeneList = new ArrayList()

    // Alteration
    String mutationType
    String mutationSite
    String epigeneticType
    String epigeneticRegion
    LinkedHashMap alterationTypes = new LinkedHashMap()
    String moleculeType
    String regulation
    String ptmType
    String ptmRegion

    // Interaction
    String source
    String target
    String experimentalModel
    String mechanism

    // Inhibitor
    String trialType
    String trialPhase
    String inhibitorName
    String trialExperimentalModel

    LiteratureFilter() {

        alterationTypes.put("Epigenetic Event", true)
        alterationTypes.put("Expression", true)
        alterationTypes.put("Gene Amplification", true)
        alterationTypes.put("Genomic Level Change", true)
        alterationTypes.put("LOH", true)
        alterationTypes.put("Mutation", true)
        alterationTypes.put("PTM", true)

    }

    def hasDisease() {
        return bioDiseaseId != null && bioDiseaseId > 0
    }

    def hasDiseaseSite() {
        return diseaseSite != null && diseaseSite.size() > 0 && (diseaseSite.iterator().next() != "");
    }

    def hasComponent() {
        return componentList != null && componentList.size() > 0 && (componentList.iterator().next() != "");
    }

    def hasMutationType() {
        return mutationType != null && mutationType.length() > 0;
    }

    def hasMutationSite() {
        return mutationSite != null && mutationSite.length() > 0;
    }

    def hasEpigeneticType() {
        return epigeneticType != null && epigeneticType.length() > 0;
    }

    def hasEpigeneticRegion() {
        return epigeneticRegion != null && epigeneticRegion.length() > 0;
    }

    def hasAlterationType() {
        int count = 0
        for (type in alterationTypes) {
            if (type.value == true) {
                count++
            }
        }
        // NOTE: Only want to filter if any of the types are not selected.
        return count < alterationTypes.size();
    }

    def hasMoleculeType() {
        return moleculeType != null && moleculeType.length() > 0;
    }

    def hasRegulation() {
        return regulation != null && regulation.length() > 0;
    }

    def hasPtmType() {
        return ptmType != null && ptmType.length() > 0;
    }

    def hasPtmRegion() {
        return ptmRegion != null && ptmRegion.length() > 0;
    }

    def hasSource() {
        return source != null && source.length() > 0;
    }

    def hasTarget() {
        return target != null && target.length() > 0;
    }

    def hasExperimentalModel() {
        return experimentalModel != null && experimentalModel.length() > 0;
    }

    def hasMechanism() {
        return mechanism != null && mechanism.length() > 0;
    }

    def hasTrialType() {

        return trialType != null && trialType.length() > 0;
    }

    def hasTrialPhase() {
        return trialPhase != null && trialPhase.length() > 0;
    }

    def hasInhibitorName() {
        return inhibitorName != null && inhibitorName.length() > 0;
    }

    def hasTrialExperimentalModel() {
        return trialExperimentalModel != null && trialExperimentalModel.length() > 0;
    }

    /**
     * Return the set of alteration types that are selected in the filter
     *
     * @return set of alteration types that the user has selected
     */
    def getSelectedAlterationTypes() {
        Set returnSet = new HashSet()
        for (key in alterationTypes.keySet()) {
            if (alterationTypes.get(key) == true) {
                returnSet.add(key.toUpperCase().replace("_", " "))
            }
        }
        return returnSet
    }

    def parseDiseaseSite(list) {
        if (list != null) {
            if (list instanceof String) {
                diseaseSite.add(list)
            } else {
                for (item in list) {
                    diseaseSite.add(item)
                }
            }
        }
    }

    def parseComponentList(list) {
        pairCompList.clear()
        pairGeneList.clear()
        componentList.clear()
        if (list != null) {
            if (list instanceof String && list.trim().length() > 0) {
                componentList.add(list)
                def compArray = list.split(",")
                pairCompList.add(compArray[0].replace("[", "").trim())
                pairGeneList.add(compArray[1].replace("]", "").trim())
            } else {
                for (item in list) {
                    componentList.add(item)
                    def compArray = item.split(",")
                    pairCompList.add(compArray[0].replace("[", "").trim())
                    pairGeneList.add(compArray[1].replace("]", "").trim())
                }
            }
        }
    }
}
