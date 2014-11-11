import org.transmart.searchapp.GeneSignature
import org.transmart.searchapp.SearchKeyword

/**
 * $Id: SearchHelpController.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * $Revision: 9178 $
 */

public class SearchHelpController {

    // service injections
    def springSecurityService
    def geneSignatureService

    def list = {

    }

//	def listAllPathways = {
//
//		def map = [:]
//		def results = SearchKeyword.findAllByDataCategory("PATHWAY",[sort:"keyword", cache:'read-only'])
//			SearchKeyword.executeQuery("SELECT s FROM SearchKeyword s WHERE s.dataCategory='PATHWAY' ORDER BY s.keyword")
//		for (keyword in results) {
//			if (map.containsKey(keyword.dataSource)) {
//				map[keyword.dataSource].add(keyword)
//			} else {
//				def list = []
//				list.add(keyword)
//				map[keyword.dataSource] = list
//			}
//		}
//		List sources = []
//		sources.addAll(map.keySet());
//
//		render(view:'pathwayhelp', model:[pathways:map, sources:sources])
//	}

    def loadPathways = {

        def step = params.step ? params.step : "A-C"
        def datasources = SearchKeyword.executeQuery("select distinct k.dataSource from org.transmart.searchapp.SearchKeyword k where k.dataCategory='PATHWAY' order by upper(k.dataSource)", [], [cache: 'read-only']);
        def defaultsource = datasources.size() > 0 ? datasources[0] : 'GeneGo'
        def dataSource = params.datasource ? params.datasource : defaultsource;
        def sql = "select k from org.transmart.searchapp.SearchKeyword k where dataSource='" + dataSource + "' "
        if ("Other".equals(step)) {
            sql += "and upper(substr(k.keyword, 1, 1)) not between 'A' and 'Z' "
        } else {
            sql += "and upper(substr(k.keyword, 1, 1)) between '" + step.substring(0, 1) + "' and '" + step.substring(step.length() - 1) + "' "
        }
        sql += "order by upper(k.keyword)"
        def pathways = SearchKeyword.executeQuery(sql, [], [cache: 'read-only'])
        render(view: 'pathwayhelp', model: [pathways: pathways, datasources: datasources, selecteddatasource: dataSource])
    }

    def listAllPathways = {
        //	def dataSource = params.datasource ? params.datasource : "GeneGO"
        def step = params.step ? params.step : "A-C"
        //def datasources = SearchKeyword.executeQuery("select distinct k.dataSource from org.transmart.searchapp.SearchKeyword k where k.dataCategory='PATHWAY' order by upper(k.dataSource)", [], [cache:'read-only']);
        def datasources = SearchKeyword.executeQuery("select distinct k.dataSource from org.transmart.searchapp.SearchKeyword k where k.dataCategory='PATHWAY' order by upper(k.dataSource)");
        def defaultsource = datasources.size() > 0 ? datasources[0] : 'GeneGo'
        def dataSource = params.datasource ? params.datasource : defaultsource;

        def sql = "select k from org.transmart.searchapp.SearchKeyword k where dataSource='" + dataSource + "' "
        if ("Other".equals(step)) {
            sql += "and upper(substr(k.keyword, 1, 1)) not between 'A' and 'Z' "
        } else {
            sql += "and upper(substr(k.keyword, 1, 1)) between '" + step.substring(0, 1) + "' and '" + step.substring(step.length() - 1) + "' "
        }
        sql += "order by upper(k.keyword)"
        //def pathways = SearchKeyword.executeQuery(sql, [], [cache:'read-only'])
        def pathways = SearchKeyword.executeQuery(sql)
        render(view: 'pathwayhelp', model: [pathways: pathways, datasources: datasources, selecteddatasource: dataSource])
    }

    def listAllTrials = {
        def all = SearchKeyword.executeQuery("SELECT s, e FROM SearchKeyword s, Experiment e WHERE s.dataCategory='TRIAL' AND s.bioDataId=e.id ORDER BY s.keyword")
        render(view: 'trialhelp', model: [trials: all])
    }

    def listAllDiseases = {
        //def all = SearchKeyword.executeQuery("SELECT s, d FROM SearchKeyword s, bio.Disease d WHERE s.dataCategory='DISEASE' AND s.bioDataId=d.id ORDER BY s.keyword")

        def all = SearchKeyword.findAllByDataCategory("DISEASE", [sort: "keyword", cache: 'read-only'])
        //SearchKeyword.executeQuery("SELECT s FROM SearchKeyword s WHERE s.dataCategory='DISEASE' ORDER BY s.keyword")
        render(view: 'diseasehelp', model: [diseases: all])
    }

    def listAllCompounds = {
        def all = SearchKeyword.executeQuery("SELECT s, c FROM SearchKeyword s, Compound c WHERE s.dataCategory='COMPOUND' AND s.bioDataId=c.id ORDER BY s.keyword")
        render(view: 'compoundhelp', model: [compounds: all])
    }

    /**
     * list all gene signatures and gene list versions user has permission to use in search
     */
    def listAllGeneSignatures = {

        // logged in user
        def user = springSecurityService.getPrincipal()
        def bAdmin = user.isAdmin()

        // signatures user has search access
        def signatures = geneSignatureService.listPermissionedGeneSignatures(user.id, bAdmin)

        def mapKeywordsGS = new HashMap()
        def mapKeywordsGL = new HashMap()
        def keyword

        // keyword maps
        signatures.each {
            // gene sig keyword map
            keyword = SearchKeyword.findByUniqueId(it.uniqueId)
            mapKeywordsGS.putAt(it.id, keyword)

            // gene list keyword map
            if (it.foldChgMetricConceptCode != GeneSignatureService.METRIC_CODE_GENE_LIST) {
                keyword = SearchKeyword.findByUniqueId(GeneSignature.DOMAIN_KEY_GL + ":" + it.id)
                mapKeywordsGL.putAt(it.id, keyword)
            }
        }

        render(view: 'geneSigHelp', model: [signatures: signatures, gsMap: mapKeywordsGS, glMap: mapKeywordsGL])
    }

}
