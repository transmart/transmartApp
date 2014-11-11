package org.transmart

/**
 * $Id: GeneExprFilter.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 * */
class GeneExprFilter {
    String cellline
    String disease

    def hasCellline() {
        return cellline != null && cellline.length() > 0;
    }

    def hasDisease() {
        return disease != null && disease.length() > 0;
    }
}
