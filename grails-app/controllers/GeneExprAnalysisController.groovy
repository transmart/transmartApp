/**
 * $Id: GeneExprAnalysisController.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 */
/**
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
class GeneExprAnalysisController {

    def findgenerifs = {
        // use session object to render null or generifs
        def openrifs = session["opengenerifs"];
        if (openrifs == null) {
            openrifs = [:]
            session["opengenerifs"] = openrifs
        }

        def search = true;
        if (openrifs[params.id] != null) {
            openrifs.remove(params.id)
            search = false;
        } else {
            openrifs[params.id] = "y"
            if (session["details"] != null)
                session["details"].remove(params.id)
        }

        if (search) {
            def geneExprAnalysis = GeneExprAnalysis.get(params.id)
            def rifs = GeneRifs.findAllByGeneSymbolLike(geneExprAnalysis.geneSymbol);
            render(template: "generifs", model: [generifs: rifs])
        } else {
            render(template: "emptyTemplate")
        }
    }

    def detail = {
        def details = session["details"];
        if (details == null) {
            details = [:]
            session["details"] = details
        }

        def search = true;
        if (details[params.id] != null) {
            details.remove(params.id)
            search = false;
        } else {
            details[params.id] = "y"
            if (session["opengenrifs"] != null)
                session["opengenerifs"].remove(params.id)
        }
        if (search) {
            def geneExprAnalysis = GeneExprAnalysis.get(params.id)

            if (!geneExprAnalysis) {
                flash.message = "GeneExprAnalysis not found with id ${params.id}"

            }
            //  println("here")
            //  render(template:"emptyTemplate")
            render(template: "/geneExprAnalysis/detail", model: [geneExprAnalysis: geneExprAnalysis])
        } else {
            render(template: "emptyTemplate")
        }
    }


    def noResult = {
        render(view: 'noresult')
    }
}
