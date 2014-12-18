import annotation.AmTagItem
import fm.FmFolderAssociation
import grails.converters.JSON
import i2b2.OntNode
import org.transmart.biomart.Experiment
import org.transmart.searchapp.AuthUser

class OntologyController {

    def index = {}
    def i2b2HelperService
    def springSecurityService
    def ontologyService
    def amTagTemplateService
    def amTagItemService

    def showOntTagFilter = {
        def tagtypesc = []
        tagtypesc.add("ALL")
        def tagtypes = i2b2.OntNodeTag.executeQuery("SELECT DISTINCT o.tagtype FROM i2b2.OntNodeTag as o order by o.tagtype")
        tagtypesc.addAll(tagtypes)
        def tags = i2b2.OntNodeTag.executeQuery("SELECT DISTINCT o.tag FROM i2b2.OntNodeTag o order by o.tag")
        /*WHERE o.tagtype='"+tagtypesc[0]+"'*/
        log.trace "${tags as JSON}"
        render(template: 'filter', model: [tagtypes: tagtypesc, tags: tags])
    }

    def ajaxGetOntTagFilterTerms = {
        def tagtype = params.tagtype
        log.trace("calling search for tagtype:" + tagtype)
        def tags = i2b2.OntNodeTag.executeQuery("SELECT DISTINCT o.tag FROM i2b2.OntNodeTag o WHERE o.tagtype='" + tagtype + "' order by o.tag")
        log.trace "${tags as JSON}"
        render(template: 'depSelectTerm', model: [tagtype: tagtype, tags: tags])
    }

    def ajaxOntTagFilter =
            {
                log.trace("called ajaxOntTagFilter")
                log.trace("tagterm:" + params.tagterm)
                def tagterm = params.tagterm
                def ontsearchterm = params.ontsearchterm
                def tagtype = params.tagtype
                def result = ontologyService.searchOntology(tagterm, [ontsearchterm], tagtype, 'JSON')
                render result as JSON
            }


    def getInitialSecurity =
            {
                def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
                def result = i2b2HelperService.getAccess(i2b2HelperService.getRootPathsWithTokens(), user);
                render result as JSON
            }
    def sectest =
            {
                log.trace("KEYS:" + params.keys)
                def keys = params.keys.toString().split(",");
                def paths = [];
                def access;
                if (params.keys != "") {
                    keys.each { key ->
                        log.debug("in LOOP")
                        paths.add(i2b2HelperService.keyToPath(key))
                    }
                    def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)


                    access = i2b2HelperService.getConceptPathAccessCascadeForUser(paths, user)
                }
                log.trace(access as JSON)
            }

    def showConceptDefinition = {
        def conceptPath = i2b2HelperService.keyToPath(params.conceptKey)
        def node = OntNode.get(conceptPath)

        //Check for study by visual attributes
        if (node.visualattributes.contains("S")) {
            def accession = node.sourcesystemcd
            def study = Experiment.findByAccession(accession?.toUpperCase())
            if (study) {
                def folder = FmFolderAssociation.findByObjectUid(study.uniqueId?.uniqueId)?.fmFolder
                if (folder) {
                    def amTagTemplate = amTagTemplateService.getTemplate(folder.uniqueId)
                    List<AmTagItem> metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate?.id)
                    render(template: 'showStudy', model: [folder: folder, bioDataObject: study, metaDataTagItems: metaDataTagItems])
                }
            }
        }
        render(template: 'showDefinition', model: [tags: node.tags])
    }

}
