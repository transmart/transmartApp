import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.grails.web.json.JSONObject
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.querytool.ConstraintByValue
import org.transmartproject.core.querytool.Item
import org.transmartproject.core.querytool.Panel
import org.transmartproject.core.querytool.QueryDefinition
import org.transmartproject.db.i2b2data.ConceptDimension
import org.transmartproject.db.i2b2data.ObservationFact
import org.transmartproject.db.querytool.QtPatientSetCollection

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

/**
 * Created by dverbeec on 18/03/2015.
 */
class OmicsQueryService {

    def dataSource
    def i2b2HelperService

    /**
     * Finds all patients satisfying the omics filter constraint
     * @param studyid Study
     * @param operator
     * @param gene_symbol
     * @return
     */
    def getPatientIdsForOmicsFilter(String concept_code, String operator, String threshold, String gene_symbol) {
        def ordering = operator == "GT" ? "DESC" : "ASC"
        String sqlt = """select patient_id from
                        (
                        select patient_id, (percent_rank() over (order by avgz """ + ordering + """)) * 100 as pr
                        from
                        (
                        select c.patient_id, avg(zscore) avgz
                        from deapp.de_subject_microarray_data a
                             inner join deapp.de_mrna_annotation b ON a.probeset_id=b.probeset_id
                             inner join deapp.de_subject_sample_mapping c on a.assay_id = c.assay_id
                        where a.probeset_id in (
                           select probeset_id
                           from deapp.de_mrna_annotation
                           where gene_symbol=?)
                         and a.assay_id in (
                           select assay_id
                           from deapp.de_subject_sample_mapping
                           where concept_code=?
                         )
                        group by c.patient_id
                        ) q
                        ) q2"""
        def params = [gene_symbol, concept_code]
        if (threshold != "" && operator != "") {
            sqlt += " where pr < CAST(? as NUMERIC)"
            params.add(threshold)
        }

        Sql sql = new Sql(dataSource);
        def ids = [];
        sql.eachRow(sqlt, params, { row ->
            ids.add(row.patient_id)
        })
        return ids
    }

    def int keepPatientsInResultInstance(String rid, patientids) {
        if (patientids.size() == 0)
            return 0;
        String sqlt = """DELETE FROM qt_patient_set_collection
            WHERE result_instance_id=CAST(? AS NUMERIC)
                  AND patient_num NOT IN (""" + patientids.join(",") + ")" // TODO: better approach than join?

        Sql sql = new Sql(dataSource);
        return sql.executeUpdate(sqlt, [rid]); // returns the number of rows affected
    }

    def List<Long> getPatientIdsForResultInstance(String rid) {
        List<Long> ids = new ArrayList<Long>()
        String sqlt = """SELECT patient_num FROM qt_patient_set_collection
            WHERE result_instance_id=CAST(? AS NUMERIC)"""
        Sql sql = new Sql(dataSource)
        sql.eachRow(sqlt, [rid], { row ->
            ids.add(row.patient_num)
        })
        return ids
    }

    def updateRequestXML(Long resultId, String xmlRequest, String queryName) {

        def sourcexml
        try {
            sourcexml = new XmlSlurper().parse(new StringReader(xmlRequest))
        } catch (exception) {
            throw new InvalidRequestException('Malformed XML document: ' +
                    exception.message, exception)
        }

        def writer = new StringWriter()
        def targetxml = new MarkupBuilder(writer)

        targetxml.'qd:query_definition'('xmlns:qd': "http://www.i2b2" +
                ".org/xsd/cell/crc/psm/querydefinition/1.1/") {
            query_name queryName

            sourcexml.panel.collect { panelArg ->
                panel {
                    invert panelArg.invert
                    panelArg.item.each { itemArg  ->
                        item {
                            item_key itemArg.item_key

                            if (itemArg.constrain_by_omics_value.size()) {
                                constrain_by_omics_value {
                                    omics_value_operator itemArg.constrain_by_omics_value.omics_value_operator
                                    omics_value_constraint itemArg.constrain_by_omics_value.omics_value_constraint
                                    gene_symbol itemArg.constrain_by_omics_value.gene_symbol
                                }
                            }

                            if (itemArg.value_constraint.size()) {
                                value_constraint {
                                    value_operator itemArg.value_constraint.value_operator
                                    value_constraint itemArg.value_constraint.value_constraint
                                    value_type itemArg.value_constraint.value_type
                                }
                            }
                        }
                    }
                }
            }
        }

        String newXmlRequest = writer.toString()

        String sqlt = """UPDATE i2b2demodata.qt_query_master SET request_xml=?
                         WHERE query_master_id IN
                         (SELECT query_master_id
                          FROM i2b2demodata.qt_query_result_instance a INNER JOIN i2b2demodata.qt_query_instance b
                            ON a.query_instance_id=b.query_instance_id
                          WHERE result_instance_id=?)"""
        Sql sql = new Sql(dataSource)
        sql.executeUpdate(sqlt, [newXmlRequest, resultId])
    }

    def applyOmicsFilters(Long resultId, String requestBody, String queryName) throws InvalidRequestException {
        def xml
        try {
            xml = new XmlSlurper().parse(new StringReader(requestBody))
        } catch (exception) {
            throw new InvalidRequestException('Malformed XML document: ' +
                    exception.message, exception)
        }
        def patient_ids_to_keep = []
        def filter_count = 0
        def panel_count = 0
        // first build a list of lists, each list corresponds to the patient ids to keep according to
        // data in the panel
        def result = new JSONObject()
        xml.panel.each { p ->
            def omics_filter_present = false
            def ids = []
            panel_count++
            p.item.each { i ->
                def c = i.constrain_by_omics_value
                if (c.size()) {
                    omics_filter_present = true
                    def conceptcode = i2b2HelperService.getConceptCodeFromKey(i.item_key.toString())
                    def pids = getPatientIdsForOmicsFilter(conceptcode,
                        c.omics_value_operator.toString(),
                        c.omics_value_constraint.toString(),
                        c.gene_symbol.toString())
                    def filter = new JSONObject()
                    filter.putAt("concept_code", conceptcode)
                    filter.putAt("operator", c.omics_value_operator.toString())
                    filter.putAt("threshold", c.omics_value_constraint.toString())
                    filter.putAt("gene_symbol", c.gene_symbol.toString())
                    filter.putAt("patient_ids_to_keep", pids)
                    result.putAt("panel" + panel_count + "_filter" + filter_count, filter)
                    ids.add(pids)
                    filter_count++
                }
            }

            if (!p.invert.equals("0")) {
                def all_ids = getPatientIdsForResultInstance(resultId.toString())
                all_ids.removeAll(ids)
                ids = all_ids
            }
            if (omics_filter_present) {
                patient_ids_to_keep.add(ids.flatten().unique())
            }
        }
        def removed = 0

        if (filter_count) {
            def keepers = []
            if (patient_ids_to_keep.size() > 1)
            // take the intersection of all lists (i.e. perform AND operation)
                keepers = patient_ids_to_keep.inject { list1, list2 -> list1.intersect(list2)}
            else
                keepers = patient_ids_to_keep.flatten()
            result.putAt("patient_ids_to_keep", keepers)
            removed = keepPatientsInResultInstance(resultId.toString(), keepers)
        }
        updateRequestXML(resultId, requestBody, queryName)
        result.putAt("removed_count", removed)
        result.putAt("filters_applied", filter_count)
        return result
    }

    def getConceptDistributionDataForHighDimensionConcept(String concept_cd, String gene_symbol) {
        log.info("Getting concept distribution data for high dimension concept code:" + concept_cd + " and gene_symbol: " + gene_symbol);
        ArrayList<Double> values = new ArrayList<Double>();
        Sql sql = new Sql(dataSource);
        String sqlt = """select avg(zscore) avgz
                        from deapp.de_subject_microarray_data a
                             inner join deapp.de_mrna_annotation b ON a.probeset_id=b.probeset_id
                             inner join deapp.de_subject_sample_mapping c on a.assay_id = c.assay_id
                        where a.probeset_id in (
                           select probeset_id
                           from deapp.de_mrna_annotation
                           where gene_symbol=?)
                         and a.assay_id in (
                           select assay_id
                           from deapp.de_subject_sample_mapping
                           where concept_code=?
                         )
                        group by c.patient_id"""
        sql.eachRow(sqlt, [
                gene_symbol,
                concept_cd
        ], { row ->
            if (row.avgz != null) {
                values.add(row.avgz);
            }
        })
        return values;
    }


    def getConceptDistributionDataForHighDimensionConceptFromCode(String concept_cd, String result_instance_id, String gene_symbol) {
        log.info("Getting concept distribution data for high dimension concept code:" + concept_cd + " and result_instance_id: " +
                result_instance_id + " and gene_symbol: " + gene_symbol)
        ArrayList<Double> values = new ArrayList<Double>();
        if (result_instance_id.trim() == "") {
            log.info("getConceptDistributionDataForHighDimensionConceptFromCode called with no result_istance_id")
            return getConceptDistributionDataForHighDimensionConcept(concept_cd, gene_symbol)
        }
        Sql sql = new Sql(dataSource)
        String sqlt = """select avg(zscore) avgz
                        from deapp.de_subject_microarray_data a
                             inner join deapp.de_mrna_annotation b ON a.probeset_id=b.probeset_id
                             inner join deapp.de_subject_sample_mapping c on a.assay_id = c.assay_id
                        where a.probeset_id in (
                           select probeset_id
                           from deapp.de_mrna_annotation
                           where gene_symbol=?)
                         and a.assay_id in (
                           select assay_id
                           from deapp.de_subject_sample_mapping
                           where concept_code=?
                         )
                         and c.patient_id in (
                           select patient_num
                           from i2b2demodata.qt_patient_set_collection
                           where result_instance_id=CAST(? as NUMERIC)
                         )
                        group by c.patient_id
                        order by avgz asc"""
        sql.eachRow(sqlt, [
                gene_symbol,
                concept_cd,
                result_instance_id
        ], { row ->
            if (row.avgz != null) {
                values.add(row.avgz);
            }
        })
        return values;
    }

    def String getGeneSymbolForGeneId(String geneId) {
        def sqlt = """SELECT DISTINCT gene_symbol
                      FROM deapp.de_mrna_annotation
                      WHERE gene_id=CAST(? AS NUMERIC)"""
        Sql sql = new Sql(dataSource)
        List<String> results = new ArrayList<String>()
        sql.eachRow(sqlt, [geneId], { row ->
            results.add(row.gene_symbol)
        })
        if (results.size() > 1) {
            log.warn "Different gene symbols found for gene-id " + geneId + ", returning the first one."
        }
        return results[0]
    }

    def getSearchResultsForGene(String searchString, String concept_key) {
        def concept_code = i2b2HelperService.getConceptCodeFromKey(concept_key)
        def searchstr = "'" + searchString.toLowerCase() + "%'"
        def sqlt = """select distinct gene_symbol, gpl_id from deapp.de_mrna_annotation where gpl_id=
                        (
                        select distinct gpl_id
                        from (select * from i2b2demodata.concept_dimension where concept_cd=?) cd
                             inner join deapp.de_subject_sample_mapping ssm
                             on cd.concept_cd=ssm.concept_code
                             )
                        and lower(gene_symbol) like """ + searchstr + """
                        order by gene_symbol"""
        Sql sql = new Sql(dataSource)
        def results = []
        sql.eachRow(sqlt, [concept_code], { row ->
            results.add([label: row.gene_symbol])
        })
        results
    }

    def Set<String> getHighDimensionalConceptSet(String result_instance_id1, String result_instance_id2) {
        Set<String> result = new HashSet<String>();

        if (result_instance_id1) {
            result.addAll(getHighDimensionalConceptKeysInSubset(result_instance_id1));
        }

        if (result_instance_id2) {
            result.addAll(getHighDimensionalConceptKeysInSubset(result_instance_id2));
        }

        result
    }

    /**
     * This will add the gene_symbol to the concept key, e.g. ...\Biomarker data\Genome platform\TNF
     * @param resultInstanceId
     * @return
     */
    def List<String> getHighDimensionalConceptKeysInSubset(String resultInstanceId) {
        ArrayList<String> concepts = new ArrayList<String>();
        Sql sql = new Sql(dataSource)
        String sqlt = """SELECT REQUEST_XML FROM QT_QUERY_MASTER c INNER JOIN QT_QUERY_INSTANCE a
		    ON a.QUERY_MASTER_ID=c.QUERY_MASTER_ID INNER JOIN QT_QUERY_RESULT_INSTANCE b
		    ON a.QUERY_INSTANCE_ID=b.QUERY_INSTANCE_ID WHERE RESULT_INSTANCE_ID = ?""";

        String xmlrequest = "";
        sql.eachRow(sqlt, [resultInstanceId], { row ->
            def xml
            try {
                xml = new XmlSlurper().parse(new StringReader(row.request_xml))
            } catch (exception) {
                throw new InvalidRequestException('Malformed XML document: ' +
                        exception.message, exception)
            }
            xml.panel.each { p ->
                p.item.each { i ->
                    if (i.constrain_by_omics_value.size()) {
                        concepts.add(i.item_key.toString() + i.constrain_by_omics_value.gene_symbol.toString() + "\\")
                    }
                }
            }
        })
        log.info "High dimensional concepts found: " + concepts
        concepts
    }

    /**
     * Adds a column of high dimensional data to the grid export table
     */
    def ExportTableNew addHighDimConceptDataToTable(ExportTableNew tablein, String concept_key, String result_instance_id, String gene_symbol) {
        checkQueryResultAccess result_instance_id

        String columnid = (concept_key + gene_symbol).encodeAsSHA1()
        String columnname = gene_symbol

        /*add the column to the table if its not there*/
        if (tablein.getColumn("subject") == null) {
            tablein.putColumn("subject", new ExportColumn("subject", "Subject", "", "string"));
        }
        if (tablein.getColumn(columnid) == null) {
            tablein.putColumn(columnid, new ExportColumn(columnid, columnname, "", "number"));
        }


        /* get the data, we assume expression values will always be numeric */
        String concept_cd = i2b2HelperService.getConceptCodeFromKey(concept_key);
        Sql sql = new Sql(dataSource)

        log.info("Getting concept distribution data for high dimension concept code:" + concept_cd + " and result_instance_id: " +
                result_instance_id + " and gene_symbol: " + gene_symbol)
        ArrayList<Double> values = new ArrayList<Double>();

        String sqlt = """select avg(zscore) avgz, c.patient_id
                        from deapp.de_subject_microarray_data a
                             inner join deapp.de_mrna_annotation b ON a.probeset_id=b.probeset_id
                             inner join deapp.de_subject_sample_mapping c on a.assay_id = c.assay_id
                        where a.probeset_id in (
                           select probeset_id
                           from deapp.de_mrna_annotation
                           where gene_symbol=?)
                         and a.assay_id in (
                           select assay_id
                           from deapp.de_subject_sample_mapping
                           where concept_code=?
                         )
                         and c.patient_id in (
                           select patient_num
                           from i2b2demodata.qt_patient_set_collection
                           where result_instance_id=CAST(? as NUMERIC)
                         )
                        group by c.patient_id
                        order by avgz asc"""

        sql.eachRow(sqlt, [
                gene_symbol,
                concept_cd,
                result_instance_id
        ], { row ->
            /*If I already have this subject mark it in the subset column as belonging to both subsets*/
            String subject = row.patient_id
            Double value = row.avgz
            if (tablein.containsRow(subject)) /*should contain all subjects already if I ran the demographics first*/ {
                tablein.getRow(subject).put(columnid, value.toString());
            } else
            /*fill the row*/ {
                ExportRowNew newrow = new ExportRowNew();
                newrow.put("subject", subject);
                newrow.put(columnid, value.toString());
                tablein.putRow(subject, newrow);
            }
        })

        //pad all the empty values for this column
        for (ExportRowNew row : tablein.getRows()) {
            if (!row.containsColumn(columnid)) {
                row.put(columnid, "NULL");
            }
        }

        return tablein;
    }

    def decodeHighDimConceptKey(String concept_key) {
        def matcher = concept_key =~ /^(.*\\)([^\\]+)\\$/
        return [concept : matcher[0][1], gene_symbol : matcher[0][2]]
    }
}
