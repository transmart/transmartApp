package org.transmart


import org.transmart.searchapp.SearchKeyword

/**
 * @author $Author: mmcduffie $
 * $Id: HeatmapFilter.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @version $Revision: 9178 $
 *
 */
public class HeatmapFilter {

    String datatype
    String heatmapfiltertype
    SearchKeyword searchTerm


    def reset = {
        datatype = null
        heatmapfiltertype = null
        searchTerm = null
    }
}
