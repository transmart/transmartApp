package transmartapp

import grails.converters.JSON
import org.transmart.searchapp.AuthUser

class OntologyService {

    boolean transactional = true

    def i2b2HelperService
    def springSecurityService

    def searchOntology(searchtags, searchterms, tagsearchtype, returnType, accessionsToInclude, searchOperator) {

        def concepts = [];
        def myNodes;

        if (searchterms?.size() == 0) {
            searchterms = null;
        }
        log.trace("searching for:" + searchtags + " of type" + tagsearchtype + "with searchterms:" + searchterms?.join(","))
        def myCount = 0;
        def allSystemCds = []
        def visualAttrHiddenWild = '%H%';

        //Build queries for search terms and accessions to include
        def searchtermstring = ""
        for (searchterm in searchterms) {
            searchterm = searchterm?.trim();
            if (searchterm) {
                if (!searchtermstring.equals("")) {
                    searchtermstring += " " + searchOperator + " "
                }
                def searchtermWild = '%' + searchterm.toLowerCase().replace("'", "''") + '%';
                searchtermstring += "lower(o.name) like '" + searchtermWild + "' "
            }
        }
        if (!searchtermstring) {
            searchtermstring = "2=1"; //No free-text search terms, so this section of the query is always false
        }

        def accessionSearchString = ""
        if (accessionsToInclude) {
            accessionSearchString += " OR (o.visualattributes = 'FAS' AND o.sourcesystemcd IN ("
            accessionSearchString += "'" + accessionsToInclude.join("','") + "'"
            accessionSearchString += "))"
        }

        if (tagsearchtype == 'ALL') {
            def countQuery = "SELECT COUNT(DISTINCT o.id) from i2b2.OntNode o WHERE (_searchterms_) _accessionSearch_ AND o.visualattributes NOT like '" + visualAttrHiddenWild + "'"
            def nodeQuery = "SELECT o from i2b2.OntNode o WHERE (_searchterms_) _accessionSearch_ AND o.visualattributes NOT like '" + visualAttrHiddenWild + "'"

            countQuery = countQuery.replace("_searchterms_", searchtermstring).replace("_accessionSearch_", accessionSearchString)
            nodeQuery = nodeQuery.replace("_searchterms_", searchtermstring).replace("_accessionSearch_", accessionSearchString)

            log.debug(nodeQuery)

            myCount = i2b2.OntNode.executeQuery(countQuery)[0]
            myNodes = i2b2.OntNode.executeQuery(nodeQuery)

        } else {

            def cdQuery = "SELECT DISTINCT o.sourcesystemcd FROM i2b2.OntNode o JOIN o.tags t WHERE t.tag IN (:tagArg) AND t.tagtype =:tagTypeArg"
            allSystemCds = i2b2.OntNode.executeQuery(cdQuery, [tagArg: searchtags, tagTypeArg: tagsearchtype], [max: 800])

            def countQuery = "SELECT COUNT(DISTINCT o.id) from i2b2.OntNode o WHERE o.sourcesystemcd IN (:scdArg) AND (_searchterms_) AND o.visualattributes NOT like '" + visualAttrHiddenWild + "'"
            countQuery = countQuery.replace("_searchterms_", searchtermstring)
            myCount = i2b2.OntNode.executeQuery(countQuery, [scdArg: allSystemCds])[0]

            def nodeQuery = "SELECT o from i2b2.OntNode o WHERE o.sourcesystemcd IN (:scdArg) AND (_searchterms_) AND o.visualattributes NOT like '" + visualAttrHiddenWild + "'"
            nodeQuery = nodeQuery.replace("_searchterms_", searchtermstring)
            myNodes = i2b2.OntNode.executeQuery(nodeQuery, [scdArg: allSystemCds])
        }
        //}

        //check the security
        def keys = [:]
        myNodes.each { node ->
            //keys.add("\\"+node.id.substring(0,node.id.indexOf("\\",2))+node.id)
            keys.put(node.id, node.securitytoken)
            log.trace(node.id + " security token:" + node.securitytoken)
        }
        def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        def access = i2b2HelperService.getAccess(keys, user);
        log.trace(access as JSON)

        if (returnType.equals("JSON")) {
            //build the JSON for the client
            myNodes.each { node ->
                log.trace(node.id)
                def level = node.hlevel
                def key = "\\" + node.id.substring(0, node.id.indexOf("\\", 2)) + node.id
                def name = node.name
                def synonym_cd = node.synonymcd
                def visualattributes = node.visualattributes
                def totalnum = node.totalnum
                def facttablecolumn = node.facttablecolumn
                def tablename = node.tablename
                def columnname = node.columnname
                def columndatatype = node.columndatatype
                def operator = node.operator
                def dimcode = node.dimcode
                def comment = node.comment
                def tooltip = node.tooltip
                def metadataxml = i2b2HelperService.metadataxmlToJSON(node.metadataxml)
                concepts.add([level: level, key: key, name: name, synonym_cd: synonym_cd, visualattributes: visualattributes, totalnum: totalnum, facttablecolumn: facttablecolumn, tablename: tablename, columnname: columnname, columndatatype: columndatatype, operator: operator, dimcode: dimcode, comment: comment, tooltip: tooltip, metadataxml: metadataxml, access: access[node.id]])

            }
            def resulttext;
            if (myCount < 100) {
                resulttext = "Found " + myCount + " results."
            } else {
                resulttext = "Returned first 100 of " + myCount + " results."
            }

            def result = [concepts: concepts, resulttext: resulttext]
            log.trace(result as JSON)

            return result
        } else if (returnType.equals("accession")) {
            def accessions = []
            myNodes.each { node ->
                if (!accessions.contains(node.sourcesystemcd)) {
                    accessions.add(node.sourcesystemcd)
                }
            }
            return accessions
        } else if (returnType.equals("path")) {
            def ids = []

            myNodes.each { node ->
                def key = "\\" + node.id.substring(0, node.id.indexOf("\\", 2)) + node.id // ?!
                if (!ids.contains(key)) {
                    ids.add(key)
                }
            }
            return ids
        }
    }

    def checkSubjectLevelData(accession) {

        def nodes = i2b2.OntNode.createCriteria().list {
            eq('sourcesystemcd', accession?.toUpperCase())
            maxResults(1)
        }

        return (nodes.size() > 0)
    }

    def getPathForAccession(accession) {
        def node = i2b2.OntNode.createCriteria().get {
            eq('sourcesystemcd', accession.toUpperCase())
            eq('hlevel', 1L)
        }

        return ("\\" + node.id.substring(0, node.id.indexOf("\\", 2)) + node.id).replace("\\", "\\\\")
    }
}
