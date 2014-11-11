import org.transmart.biomart.BioMarker
import org.transmart.biomart.Compound
import org.transmart.searchapp.SearchKeyword


/*
 * $Id: DetailsController.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 */
/**
 * This controller is used to display details data for genes, pathways, and compounds. For each of these there
 * is a corresponsing action and view which displays a window with tabs, e.g. "gene" action and view. And then
 * if a summary is define a summary action and view, e.g. compoundSumary action and view.
 *
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
class DetailsController {

    def detailsService

    def gene = {
        def bioDataId = params?.id
        def altId = params?.altId
        def geneSymbol = ""
        def uniqueId = ""
        def geneId = ""
        if ((bioDataId == null || bioDataId.length() == 0) && (altId != null & altId.length() > 0)) {
            // TODO: Add type criteria
            def result = BioMarker.findByPrimaryExternalId(altId)
            if (result != null) {
                bioDataId = result.id.toString()
            }
        }
        if (bioDataId != null && bioDataId.length() > 0) {
            def marker = BioMarker.get(Long.valueOf(bioDataId))
            //	def searchKeyword = org.transmart.searchapp.SearchKeyword.findByBioDataId(Long.valueOf(bioDataId))
            //	if (searchKeyword != null) {
            //		geneSymbol = searchKeyword.keyword
            //		uniqueId = searchKeyword.uniqueId
            //		geneId = uniqueId.substring(uniqueId.indexOf(":") + 1)
            //	}
            if (marker != null) {
                geneSymbol = marker.name;
                geneId = marker.primaryExternalId;
            }
        }
        def hydraGeneID = detailsService.getHydraGeneID(bioDataId)

        //	println("hydraGene:"+hydraGeneID)
        render(view: "gene", model: [id: bioDataId, symbol: geneSymbol, geneId: geneId, hydraGeneID: hydraGeneID])
    }

    def pathway = {
        def bioDataId = params.id
        def pathwaySymbol = ""
        def uniqueId = ""
        def pathwayType = ""
        def searchKeyword = SearchKeyword.findByBioDataId(Long.valueOf(bioDataId))
        if (searchKeyword != null) {
            pathwaySymbol = searchKeyword.keyword
            uniqueId = searchKeyword.uniqueId
            pathwayType = uniqueId.substring(uniqueId.indexOf(":") + 1, uniqueId.lastIndexOf(":"))
        }
        render(view: "pathway", model: [id: bioDataId, symbol: pathwaySymbol, type: pathwayType])
    }

    def pathwaySummary = {
        def bioDataId = params.id
        def pathway = BioMarker.get(Long.valueOf(bioDataId))
        def genes
        if (pathway != null) {
            def query = "select k from org.transmart.searchapp.SearchKeyword k, org.transmart.biomart.BioDataCorrelation c where k.bioDataId=c.associatedBioDataId and c.bioDataId=?"
            genes = SearchKeyword.executeQuery(query, Long.valueOf(bioDataId))
        }
        render(view: "pathwaySummary", model: [pathway: pathway, genes: genes])
    }

    def compound = {
        def bioDataId = params.id
        def compoundSymbol = ""
        def uniqueId = ""
        def searchKeyword = SearchKeyword.findByBioDataId(Long.valueOf(bioDataId))
        if (searchKeyword != null) {
            compoundSymbol = searchKeyword.keyword
            uniqueId = searchKeyword.uniqueId
        }
        render(view: "compound", model: [id: bioDataId, symbol: compoundSymbol])
    }

    def compoundSummary = {
        def bioDataId = params.id
        def compound = Compound.get(Long.valueOf(bioDataId))
        render(view: "compoundSummary", model: [compound: compound])
    }
}
