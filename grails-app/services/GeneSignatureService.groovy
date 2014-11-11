import com.recomdata.genesignature.FileSchemaException
import com.recomdata.search.query.Query
import org.hibernate.Hibernate
import org.springframework.web.multipart.MultipartFile
import org.transmart.biomart.BioAssayDataAnnotation
import org.transmart.biomart.BioData
import org.transmart.biomart.BioMarker
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.GeneSignature
import org.transmart.searchapp.GeneSignatureItem
import org.transmart.searchapp.SearchKeyword

import static org.transmartproject.db.support.DatabasePortabilityService.DatabaseType.ORACLE

/**
 * Service class for Gene Signature functionality
 * $Id: GeneSignatureService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class GeneSignatureService {

    // fold change metric codes
    static def METRIC_CODE_TRINARY = "TRINARY"
    static def METRIC_CODE_ACTUAL = "ACTUAL"
    static def METRIC_CODE_GENE_LIST = "NOT_USED"

    // probably not needed but makes all methods transactional
    static transactional = true

    // service injections
    def searchKeywordService
    def springSecurityService
    def sessionFactory
    def databasePortabilityService

    /**
     * verify file matches indicated schema
     */
    def verifyFileFormat(MultipartFile file, Long schemaColCt, String metricType) throws FileSchemaException {
        log.info "verifyFileFormat() called with schemaColCt: " + schemaColCt + "; metricType: " + metricType
        BufferedReader br = null;

        // check column count
        int colCount = schemaColCt;
        if (metricType == METRIC_CODE_GENE_LIST) colCount = colCount - 1;
        def origFile = file.getOriginalFilename()

        try {
            // establish a reader
            InputStream is = file.getInputStream()
            br = new BufferedReader(new InputStreamReader(is))

            // parse file (read first three lines only)
            String record = null
            int i = 0;
            StringTokenizer st = null;
            while (br.ready() && i < 3) {
                i++;
                record = br.readLine().trim();
                println("Line " + i + ": " + record)
                if (record == "") continue;

                List items = new ArrayList();
                st = new StringTokenizer(record, "\t")

                // parse into tokens
                while (st.hasMoreTokens()) {
                    items.add(st.nextToken())
                }

                // check column count
                if (items.size() != colCount) throw new FileSchemaException("Invalid number of columns, please check file:'" + origFile + "' settings and/or correct usage of tab delimiter")

                // check metric code
                String foldChgTest
                switch (metricType) {

                    case METRIC_CODE_TRINARY:
                        foldChgTest = (String) items.get(items.size() - 1)
                        int triFoldChg;

                        try {
                            triFoldChg = Integer.parseInt(foldChgTest)
                            if (triFoldChg != -1 && triFoldChg != 0 && triFoldChg != 1) throw new FileSchemaException("ERROR: Fold-change value (" + triFoldChg + ") in file:'" + origFile + "' did not match one of the trinary indicators (i.e. -1,0,1)!")
                        } catch (NumberFormatException e) {
                            throw new FileSchemaException("Invalid fold-change in file:'" + origFile + "' for Metric indicator: " + METRIC_CODE_TRINARY + " (" + foldChgTest + ")", e)
                        }

                        break;

                    case METRIC_CODE_ACTUAL:
                        foldChgTest = (String) items.get(items.size() - 1)
                        double actFoldChg
                        try {
                            actFoldChg = Double.parseDouble(foldChgTest)
                            if (actFoldChg == -1 || actFoldChg == 0 || actFoldChg == 1) throw new FileSchemaException("Fold-change value (" + foldChgTest + ")  in file:'" + origFile + "' appears to be trinary instead of an actual fold change!")
                        } catch (NumberFormatException e) {
                            throw new FileSchemaException("Invalid fold-change in file:'" + origFile + "' for Metric indicator: " + METRIC_CODE_ACTUAL + " (" + foldChgTest + ")", e)
                        }

                        break;

                    case METRIC_CODE_GENE_LIST:
                        break;
                }
            }
        } finally {
            br.close();
        }
    }

    /**
     * parse file and create associated gene sig item records
     */
    def loadGeneSigItemsFromFile(MultipartFile file, String organism, String metricType, String fileSchemaName, GeneSignature gs) throws FileSchemaException {
        BufferedReader br = null;
        List<GeneSignatureItem> gsItems = new ArrayList();
        SortedSet invalidSymbols = new TreeSet();
        def origFile = file.getOriginalFilename()

        // metric type
        log.debug("\nINFO: Parsing: " + file.originalFilename + " for organism: " + organism + " [Type: " + metricType + "]")

        try {
            // establish a reader
            InputStream is = file.getInputStream()
            br = new BufferedReader(new InputStreamReader(is))

            // parse file (read first three lines only)
            String record = null
            int i = 0;
            StringTokenizer st = null;

            while (br.ready()) {
                //while(br.ready() && i < 5) {
                i++;
                record = br.readLine().trim();
                println("Line " + i + ": " + record)
                if (record == "") continue;

                List items = new ArrayList();
                st = new StringTokenizer(record, "\t")

                // parse into tokens
                while (st.hasMoreTokens()) {
                    items.add(st.nextToken())
                }

                String geneSymbol = (String) items.get(0);
                String foldChgTest = (String) items.get(items.size() - 1)
                Double foldChg = null;

                // parse fold change metric for non gene lists
                if (metricType != METRIC_CODE_GENE_LIST) {
                    // check valid fold change
                    if (foldChgTest != "") {
                        try {
                            foldChg = Double.parseDouble(foldChgTest)
                        } catch (NumberFormatException e) {
                            log.error "invalid number format detected in file (" + foldChgTest + ")", e
                            throw new FileSchemaException("Invalid fold-change number detected in file:'" + origFile + "', please correct (" + foldChgTest + ")", e)
                        }
                    }
                }

                // lookup gene symbol or probeset id
                def marker
                if (fileSchemaName.toUpperCase() =~ /GENE /) {
                    marker = lookupBioAssociations(geneSymbol, organism)
                    if (marker == null || marker.size() == 0) {
                        println("WARN: invalid gene sybmol: " + geneSymbol)
                        invalidSymbols.add(geneSymbol);
                        continue;
                    }

                    def bioMarkerId = marker.getAt(0);
                    def uniqueId = marker.getAt(1)
                    println(">> Gene lookup: 1) marker id: " + bioMarkerId + "; 2) uniqued id: " + uniqueId)

                    // create item instance
                    GeneSignatureItem item = new GeneSignatureItem(bioMarker: BioMarker.read(bioMarkerId), bioDataUniqueId: uniqueId, foldChgMetric: foldChg);
                    gsItems.add(item);

                } else if (fileSchemaName.toUpperCase() =~ /PROBESET /) {
                    // geneSymbol ==> probeset id
                    marker = lookupProbesetBioAssociations(geneSymbol, gs.techPlatform.accession)

                    if (marker == null || marker.isEmpty()) {
                        log.warn("WARN: invalid probe set id: " + geneSymbol + " for platform " + gs.techPlatform.accession)
                        invalidSymbols.add(geneSymbol);
                        continue;
                    }

                    def probesetId = marker.getAt(0);
                    println(">> Probeset lookup: 1) probeset id: " + probesetId)

                    GeneSignatureItem item = new GeneSignatureItem(probesetId: probesetId, foldChgMetric: foldChg);
                    gsItems.add(item);
                } else {
                    marker = null
                }

            }

            // check for invalid symbols
            if (invalidSymbols.size() > 0) FileSchemaException.ThrowInvalidGenesFileSchemaException(invalidSymbols);
            log.info "created (" + gsItems.size() + ") GeneSignatureItem records"
            return gsItems;
        } finally {
            br.close();
        }
    }

    /**
     * mark specified instance public to user community
     */
    def makePublic(GeneSignature gs, boolean publicFlag) {
        gs.publicFlag = publicFlag;
        def savedInst = gs.save()

        // update search link for both GS and GL version
        searchKeywordService.updateGeneSignatureLink(savedInst, GeneSignature.DOMAIN_KEY, true)
        searchKeywordService.updateGeneSignatureLink(savedInst, GeneSignature.DOMAIN_KEY_GL, true)
    }

    /**
     * mark specified instance as deleted
     */
    def delete(GeneSignature gs) {
        gs.deletedFlag = true;
        def savedInst = gs.save()

        // update search link
        searchKeywordService.updateGeneSignatureLink(savedInst, GeneSignature.DOMAIN_KEY, true)
        searchKeywordService.updateGeneSignatureLink(savedInst, GeneSignature.DOMAIN_KEY_GL, true)
    }

    /**
     * delete the indicated items for the gene signature
     */
    def deleteGeneSigItems(GeneSignature gs, List<String> delItems) {

        // construct delete clause
        String inClause = delItems.toString()
        inClause = inClause.replace('[', '')
        inClause = inClause.replace(']', '')
        log.info "in clause: " + inClause

        // get one record to get gs id
        //GeneSignatureItem gsi = GeneSignatureItem.get(delItems[0])
        //def gs = gsi.geneSignature
        log.info "associated GeneSignature id " + gs.id

        // delete tagged items
        GeneSignatureItem.executeUpdate("delete GeneSignatureItem i where i.id IN (" + inClause + ") and i.geneSignature.id = " + gs.id)

        // load fresh gs and modify
        gs = GeneSignature.get(gs.id)
        gs.modifiedByAuthUser = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        gs.lastUpdated = new Date()
        gs.validate()
        def saved = gs
        if (!gs.hasErrors()) saved = gs.save()
        return saved
    }

    /**
     * add gene signature items
     */
    def addGenSigItems(GeneSignature gs, List<String> geneSymbols, List<String> probes, List<Double> valueMetrics) {

        log.debug " service addGenSigItems() called >> gs: " + gs + "; symbols: " + geneSymbols + "; metrics: " + valueMetrics
        def symbol
        def foldChgMetric

        def metricItems
        if (valueMetrics) metricItems = valueMetrics.toArray()

        def marker
        def organism = gs.techPlatform?.organism
        def fileSchemaId = gs.fileSchema?.id
        def invalidSymbols = []
        def gsi = []
        def i = 0;
        def gsItems = []

        // iterate symbols
        Iterator iter
        if (geneSymbols) iter = geneSymbols.iterator()
        if (probes) iter = probes.iterator()

        while (iter.hasNext()) {
            symbol = iter.next()
            foldChgMetric = (valueMetrics != null) ? metricItems[i] : null;
            i++
            log.info "[iter:" + i + "] trying to add gene sybmol: " + symbol + " with foldChgMetric: " + foldChgMetric

            // check for invalid symbols
            if (fileSchemaId != 3) marker = lookupBioAssociations(symbol, organism)
            if (fileSchemaId == 3) marker = lookupProbesetBioAssociations(symbol, gs.techPlatform.accession)

            if (marker == null || marker.size() == 0) {
                println("WARN: invalid gene sybmol: " + symbol)
                invalidSymbols.add(symbol)
                continue
            }

            if (fileSchemaId != 3) {
                def bioMarkerId = marker.getAt(0);
                def uniqueId = marker.getAt(1)
                println(">> Gene lookup: 1) marker id: " + bioMarkerId + "; 2) uniqued id: " + uniqueId + "; 3) FoldChgMetric: " + foldChgMetric)

                // create item instance
                GeneSignatureItem item = new GeneSignatureItem(bioMarker: BioMarker.get(bioMarkerId), bioDataUniqueId: uniqueId, foldChgMetric: foldChgMetric);
                gsItems.add(item)
            }

            if (fileSchemaId == 3) {
                // create item instance
                def annot = (de.DeMrnaAnnotation.find("from DeMrnaAnnotation as a where a.probesetId=? ", [marker.getAt(0)])).probesetId;
                if (annot != null) {
                    GeneSignatureItem item = new GeneSignatureItem(probesetId: annot, foldChgMetric: foldChgMetric);
                    gsItems.add(item)
                }
            }

        }

        // check for invalid gene symbols
        if (invalidSymbols.size() > 0) FileSchemaException.ThrowInvalidGenesFileSchemaException(invalidSymbols);

        // modify gs and add new items
        gs.modifiedByAuthUser = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        gs.lastUpdated = new Date()

        // add new items
        gsItems.each { gs.addToGeneSigItems(it) }
        log.info "added (" + gsItems.size() + ") GeneSignatureItem records"

        gs.validate()
        def saved = gs
        if (!gs.hasErrors()) saved = gs.save()
        return saved
    }

    /**
     * create new GeneSignature and all dependendant objects from wizard
     */
    def saveWizard(GeneSignature gs, MultipartFile file) {

        def metricType = gs.foldChgMetricConceptCode?.bioConceptCode
        println "metrictype " + metricType
        def organism = gs.techPlatform?.organism
        def fileSchemaName = gs.fileSchema?.name

        // load gs items (could be from a cloned object)
        if (file != null) {
            def gsItems = loadGeneSigItemsFromFile(file, organism, metricType, fileSchemaName, gs);
            gsItems.each { gs.addToGeneSigItems(it) }
        }

        // save gs, items, and search link
        def savedInst = gs.save(flush: true)
        def nsave = savedInst;
        if (savedInst.uniqueId == null || savedInst.uniqueId == "") {
            // need to refresh this object
            savedInst.updateUniqueId();
            //nsave = savedInst.save(flush:true)
        }

        // set bio_marker_id and bio_data_unique_id for probeset
        //if(nsave) updateGenSigItems(nsave)

        // link objects to search
        searchKeywordService.updateGeneSignatureLink(nsave, GeneSignature.DOMAIN_KEY_GL, true)
        searchKeywordService.updateGeneSignatureLink(nsave, GeneSignature.DOMAIN_KEY, true)

        return nsave;
    }

    /**
     *  fill bio_marker_id and bio_data_unique_id for newly loaded probesets
     */

    def updateGenSigItems(GeneSignature gs) {
        def gsi = GeneSignatureItem.findAllWhere(geneSignature: gs)
        gsi.each { gsItem ->
            def bioMkr = BioAssayDataAnnotation.findAllWhere(probeset: gsItem.probeset)
            if (bioMkr) {
                bioMkr.each {
                    def b1 = BioData.find("from BioData as b where b.id=?", [it.bioMarker.id])
                    gsItem.bioDataUniqueId = b1.uniqueId
                    gsItem.bioMarker = it.bioMarker
                    gsItem.save(flush: true)
                }
            }
        }
    }

    /**
     * update GeneSignature and all dependant objects from  wizard
     */
    def updateWizard(GeneSignature gs, MultipartFile file) {

        // load new items if file present
        if (file != null && file.getOriginalFilename() != "") {
            def metricType = gs.foldChgMetricConceptCode?.bioConceptCode
            def organism = gs.techPlatform?.organism
            def fileSchemaName = gs.fileSchema?.name

            // parse items
            def gsItems = loadGeneSigItemsFromFile(file, organism, metricType, fileSchemaName, gs);

            // delete current items
            log.info "deleting original items"
            GeneSignatureItem.executeUpdate("delete GeneSignatureItem i where i.geneSignature.id = :currentId", [currentId: gs.id])
            gs.geneSigItems = []

            // add new items
            gsItems.each { gs.addToGeneSigItems(it) }
        }

        // update gs, refresh items, and search link

        def savedInst = gs.save()
        searchKeywordService.updateGeneSignatureLink(savedInst, GeneSignature.DOMAIN_KEY, true)
        searchKeywordService.updateGeneSignatureLink(savedInst, GeneSignature.DOMAIN_KEY_GL, true)
    }

    /**
     * clone items from a parent onto a clone
     */
    def cloneGeneSigItems(GeneSignature parent, GeneSignature clone) {

        GeneSignatureItem item = null;
        parent.geneSigItems.each {
            if (it.bioMarker) {
                item = new GeneSignatureItem(bioMarker: BioMarker.get(it.bioMarker.id), bioDataUniqueId: it.bioDataUniqueId, foldChgMetric: it.foldChgMetric);
            } else {
                item = new GeneSignatureItem(foldChgMetric: it.foldChgMetric, probesetId: it.probesetId);
            }
            clone.addToGeneSigItems(item);
        }
    }

    /**
     * match up the uploaded gene sybmol with our internal bio_marker & bio_data_uid tables
     */
    def lookupBioAssociations(String geneSymbol, String organism) {
        def query = new Query(mainTableAlias: "bd");
        query.addTable("org.transmart.biomart.BioMarker bm")
        query.addTable("org.transmart.biomart.BioData bd")
        query.addCondition("bm.id=bd.id")
        query.addCondition("bm.bioMarkerType='GENE'")
        query.addCondition("bm.organism='" + organism.toUpperCase() + "'")
        query.addCondition("UPPER(bm.name) ='" + geneSymbol.toUpperCase() + "'")
        query.addCondition("bd.type='BIO_MARKER.GENE'")
        query.addSelect("bm.id")
        query.addSelect("bd.uniqueId")

        def qBuf = query.generateSQL();
        //log.debug "Lookup query: "+qBuf

        //def markers = BioMarker.executeQuery(qBuf);
        def markers = BioData.executeQuery(qBuf);

        // try ext code lookup if necessary
        //println(markers)

        if (markers == null || markers.size() == 0 || markers.size() > 1) {
            query = new Query(mainTableAlias: "bm");
            query.addTable("org.transmart.biomart.BioDataExternalCode ext")
            query.addTable("org.transmart.biomart.BioMarker bm")
            query.addTable("org.transmart.biomart.BioData bd")
            query.addCondition("ext.bioDataId=bm.id")
            query.addCondition("bm.id=bd.id")
            query.addCondition("UPPER(ext.code) = '" + geneSymbol.toUpperCase() + "'")
            query.addCondition("bm.bioMarkerType='GENE'")
            query.addCondition("bm.organism='" + organism.toUpperCase() + "'")
            query.addCondition("bd.type='BIO_MARKER.GENE'")
            query.addSelect("bm.id")
            query.addSelect("bd.uniqueId")

            qBuf = query.generateSQL();
            log.info "Ext Bio Marker lookup query: " + qBuf
            markers = BioMarker.executeQuery(qBuf)

            // check for none or ambiguity
            if (markers == null || markers.size() > 1) return null;
        }
        return markers[0];
    }

    /**
     * match up the uploaded probeset id with our internal bio_assay_feature_group & bio_data_uid tables
     */
    def lookupProbesetBioAssociations(String probeset, String platform) {
        def query = new Query(mainTableAlias: "bf");

        query.addTable("de.DeMrnaAnnotation a")
        query.addCondition("a.gplId='" + platform + "'")
        query.addCondition("a.probeId ='" + probeset.replace(" ", "") + "'")
        query.addSelect("a.probesetId")

        def qBuf = query.generateSQL();

        def marker = de.DeMrnaAnnotation.executeQuery(qBuf).asList();

        return marker;
    }

    /**
     * gets a lit of permissioned gene signature records the user is allowed to view. The returned
     * items are list of domain objects
     */
    def listPermissionedGeneSignatures(Long userId, boolean bAdmin) {
        def permCriteria = (bAdmin) ? "(1=1)" : "(gs.createdByAuthUser.id=" + userId + " or gs.publicFlag=true)"
        def qBuf = "from GeneSignature gs where " + permCriteria + " and gs.deletedFlag=false order by gs.name"
        return GeneSignature.findAll(qBuf);
    }

    /**
     * creates a Map of the gene counts per gene signature including up and down regulation counts for those
     * signatures the user has permission to view
     */
    def getPermissionedCountMap(Long userId, boolean bAdmin) {

        /*
        // this code only gets the gene count per gene sig, could not use HQL using aggregate function to count up and down regulation

        def permCriteria = (bAdmin) ? "(1=1)" : "(i.geneSignature.createdByAuthUser.id="+userId+" or i.geneSignature.publicFlag=true)"
        // 1) total gene count
        def selectItems = "i.geneSignature.id"
        StringBuffer qBuf = new StringBuffer();
        qBuf.append("select ").append(selectItems).append(", count(*) as GeneCount ")
        //qBuf.append("select ").append(selectItems).append(", count(*) as GeneCount, sum(if(i.foldChgMetric is not null and i.foldChgMetric>0,1,0)) as UpCount, sum(if(i.foldChgMetric is not null and i.foldChgMetric<0,1,0)) as DownCount ")
        qBuf.append("from GeneSignatureItem i ")
        qBuf.append("where "+permCriteria+" and i.geneSignature.deletedFlag=false ")
        qBuf.append("group by ").append(selectItems)

        def query = qBuf.toString();
        def results = GeneSignature.executeQuery(query);

        // create lookup map on id
        Map countMap = new HashMap();
        results.each { countMap.put(it.getAt(0), it) }
        */

        def permCriteria = (bAdmin) ? "(1=1)" : "(gs.CREATED_BY_AUTH_USER_ID=" + userId + " or gs.PUBLIC_FLAG=${databasePortabilityService.databaseType == ORACLE ? '1' : 'true'})"
        StringBuffer nativeSQL = new StringBuffer();
        nativeSQL.append("select gsi.SEARCH_GENE_SIGNATURE_ID as id, count(*) Gene_Ct, sum(CASE WHEN gsi.FOLD_CHG_METRIC>0 THEN 1 ELSE 0 END) Up_Ct, sum(CASE WHEN gsi.FOLD_CHG_METRIC<0 THEN 1 ELSE 0 END) Down_Ct ");
        nativeSQL.append("from SEARCH_GENE_SIGNATURE_ITEM gsi join SEARCH_GENE_SIGNATURE gs on gsi.search_gene_signature_id=gs.search_gene_signature_id ");
        nativeSQL.append("where " + permCriteria + " and gs.DELETED_FLAG=${databasePortabilityService.databaseType == ORACLE ? '0' : 'false'} ");
        nativeSQL.append("group by gsi.SEARCH_GENE_SIGNATURE_ID");

        // execute native sql on hibernate session
        def countMap = new HashMap();
        def session = sessionFactory.getCurrentSession()

        //def trans = session.beginTransaction();
        def hqlQuery = session.createSQLQuery(nativeSQL.toString())
        hqlQuery.addScalar("id", Hibernate.LONG)
        hqlQuery.addScalar("Gene_Ct", Hibernate.LONG)
        hqlQuery.addScalar("Up_Ct", Hibernate.LONG)
        hqlQuery.addScalar("Down_Ct", Hibernate.LONG)
        def results = hqlQuery.list()
        // stuff results into a lookup map
        results.each { countMap.put(it.getAt(0), it); }

        return countMap
    }

    def getGeneSigGMTContent(geneSigId) {
        // get domain object
        def gs = GeneSignature.get(geneSigId)

        //write gene-sig items into the GMT file
        def sbuf = new StringBuilder()
        sbuf.append((gs?.name) ? gs?.name : '').append('\t')
        sbuf.append((gs?.description) ? gs?.description : '').append('\t')

        for (geneSigItem in gs.geneSigItems) {
            sbuf.append((geneSigItem?.geneSymbol) ? geneSigItem?.geneSymbol.join("/") : '').append('\t')
        }

        sbuf.append('\n')

        return sbuf.toString()
    }

    def expandGeneList(geneListUid) {

        def genesList = []


        def geneSig = GeneSignature.findByUniqueId(geneListUid)
        def geneKeywords = GeneSignatureItem.createCriteria().list {
            eq('geneSignature', geneSig)
        }

        // loop through each keyword for the gene list items and add to list
        geneKeywords.each {
            // don't add duplicates
            def symbol = it.geneSymbol
            for (g in symbol) {
                def bioId = lookupBioAssociations(g, geneSig.techPlatform?.organism)[1]
                if (bioId && genesList.indexOf(bioId) < 0) {
                    genesList.add(bioId)
                }
            }
        }


        return genesList

    }

    def expandPathway(pathwayUid) {

        def genesList = []

        def geneKeywords = SearchKeyword.executeQuery("""
		   				select k_gene.uniqueId
						from org.transmart.searchapp.SearchKeyword k_pathway, org.transmart.biomart.BioMarkerCorrelationMV b,
						org.transmart.searchapp.SearchKeyword k_gene
						where b.correlType = 'PATHWAY GENE'
						and b.bioMarkerId = k_pathway.bioDataId
						and k_pathway.dataCategory = 'PATHWAY'
						and b.assoBioMarkerId = k_gene.bioDataId
						and k_gene.dataCategory = 'GENE'
						and k_pathway.uniqueId = :pathwayUid """, [pathwayUid: pathwayUid])

        // loop through each keyword for the gene list items and add to list
        geneKeywords.each {
            // don't add duplicates
            if (genesList.indexOf(it) < 0) {
                genesList.add it
            }
        }

        return genesList
    }

}
