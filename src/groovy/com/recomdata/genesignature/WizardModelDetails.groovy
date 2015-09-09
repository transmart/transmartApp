/**
 * model details class for the create/edit wizard in the gene signature module
 */
package com.recomdata.genesignature

import com.recomdata.util.ModelDetails
import org.transmart.biomart.ConceptCode

/**
 * @author jspencer
 * @version
 */
public class WizardModelDetails extends ModelDetails {
    // wizard tyes
    static def WIZ_TYPE_CREATE = 0
    static def WIZ_TYPE_EDIT = 1
    static def WIZ_TYPE_CLONE = 2

    // default is create
    def wizardType = WIZ_TYPE_CREATE

    // pick lists
    def sources
    def owners
    def species
    def mouseSources
    def tissueTypes
    def expTypes
    def analyticTypes
    def normMethods
    def analysisMethods
    def schemas
    def pValCutoffs
    def foldChgMetrics
    def platforms
    def compounds

    // domain class
    def geneSigInst

    // id of domain being edited
    def editId
    def cloneId

    /**
     * add an empty other ConceptCode item
     */
    public static void addOtherItem(List<ConceptCode> items, String optionId) {
        if (optionId == null) optionId = "other"
        items.add(new ConceptCode(bioConceptCode: optionId, codeName: "other"))
    }

}
