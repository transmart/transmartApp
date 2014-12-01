import grails.util.Holders

/**
 * $Id: GlobalFilterService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 *
 */
public class GlobalFilterService {

/**
 *
 */
    //def createGlobalFilterCriteria(GlobalFilter gfilter,
    //		Query query){
    //return createGlobalFilterCriteria(gfilter, query, false)

    //}

/**
 *
 */

/*	def createGlobalFilterCriteria(GlobalFilter gfilter,
			Query query,
			boolean expandBioMarkers){
		StringBuilder s = new StringBuilder();
		// biomarkers
		def biomarkerFilters = gfilter.getBioMarkerFilters()
		if(!biomarkerFilters.isEmpty()){
			def markerAlias = query.mainTableAlias+"_bm"
			def markerTable =query.mainTableAlias+".markers "+markerAlias;
			query.addTable(" JOIN "+markerTable)
			if(expandBioMarkers){
				query.addCondition(markerAlias+".id IN ("+createExpandBioMarkerSubQuery(biomarkerFilters.getKeywordDataIdString())+") ")
			}else{
			query.addCondition(markerAlias+".id IN ("+biomarkerFilters.getKeywordDataIdString()+") ")
			}
		}
		// disease
		if(!gfilter.getDiseaseFilters().isEmpty()){
			def dAlias = query.mainTableAlias+"_dis"
			def dtable =query.mainTableAlias+".diseases "+dAlias;
			query.addTable(" JOIN "+dtable)
			query.addCondition(dAlias+".id IN ("+gfilter.getDiseaseFilters().getKeywordDataIdString()+") ")
			}
		// compound
		if(!gfilter.getCompoundFilters().isEmpty()){
				def dAlias = query.mainTableAlias+"_cpd"
				def dtable =query.mainTableAlias+".compounds "+dAlias;

				query.addTable(" JOIN "+dtable)
			query.addCondition(dAlias+".id IN ("+gfilter.getCompoundFilters().getKeywordDataIdString()+") ")
		}
		// trials
		if(!gfilter.getTrialFilters().isEmpty()){
			if(query.allowExperimentFilter()){
				query.addCondtion(mainTableAlias+".experimentId IN ("+gfilter.getTrialFilters().getKeywordDataIdString()+")")
			}else {
				query.addCondition(" 1 = 0 ")
			}
		}

	}
	*/

/**
 *
 */

    /* def createExpandBioMarkerSubQuery(ids){
        StringBuilder s = new StringBuilder()
        s.append("SELECT DISTINCT marker.id FROM org.transmart.biomart.BioMarker marker ")
        s.append(" LEFT JOIN marker.associatedCorrels marker_cor")
        s.append(" WHERE marker_cor.bioDataId IN (").append(ids).append(")")
        s.append (" AND marker_cor.correlationDescr.correlation='PATHWAY GENE'")
        return s.toString()
       }
   */


    def createPagingParamMap(params) {
        def paramMap = [:]
        def max = params.max
        def offset = params.offset
        if (max == null)
            max = Holders.config.com.recomdata.search.paginate.max
        if (offset == null)
            offset = 0
        // dynamic typing sucks here..
        if (max != null)
            paramMap["max"] = Integer.valueOf(String.valueOf(max))
        if (offset != null)
            paramMap["offset"] = Integer.valueOf(String.valueOf(offset))
        return paramMap;
    }

}

