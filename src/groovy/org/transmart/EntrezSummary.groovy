package org.transmart

/**
 * $Id: EntrezSummary.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
class EntrezSummary {
    String GeneID
    String Name
    String Description
    String Orgname
    String OtherAliases
    String Mim
    String Summary
    String NomenclatureStatus

    def getOMIMID() {
        def retValue = null
        if (Mim != null) {
            retValue = Mim.split(":")
        }
        if (retValue != null && retValue.length > 1) {
            retValue = retValue[1]
        }
        return retValue
    }

    def getAliases() {
        def retValue = null
        if (OtherAliases != null) {
            retValue = OtherAliases.split(",")
        }
        return retValue
    }
}
