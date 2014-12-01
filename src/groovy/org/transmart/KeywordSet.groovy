package org.transmart

/**
 * $Id: KeywordSet.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class KeywordSet extends LinkedHashSet {

    def getKeywordUniqueIds() {
        def uidlist = []
        for (keyword in this) {
            uidlist.add(keyword.uniqueId)
        }
        return uidlist
    }

    def getKeywordDataIds() {
        def bioids = []
        for (keyword in this) {
            bioids.add(keyword.bioDataId)
        }
        return bioids
    }

    def getKeywordDataIdString() {
        StringBuilder s = new StringBuilder()

        for (keyword in this) {
            if (s.length() > 0) {
                s.append(", ")
            }
            s.append(keyword.bioDataId)
        }
        return s.toString()
    }

    def removeKeyword(keyword) {
        for (k in this) {
            if (k.uniqueId.equals(keyword.uniqueId)) {
                return this.remove(k)
            }
        }
    }

}
