import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import groovy.sql.Sql
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.exceptions.InvalidRequestException

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

/**
 * Created by dverbeec on 18/03/2015.
 */
class OmicsQueryService {

    def dataSource
    def i2b2HelperService
    def conceptsResourceService

    def getConceptDistributionDataForHighDimensionConcept(String concept_cd, omics_params) {
        log.info("Getting concept distribution data for high dimension concept code: $concept_cd and omics params: $omics_params");
        ArrayList<Double> values = new ArrayList<Double>();
        Sql sql = new Sql(dataSource);
        String sqlt = """select b.patient_id, avg($omics_params.omics_projection_type) as score from
                                (
                                    select * from deapp.de_subject_microarray_data
                                    where probeset_id in
                                    (
                                        select probeset_id from deapp.de_mrna_annotation
                                        where gene_symbol='$omics_params.omics_selector'
                                    )
                                    and assay_id in
                                    (
                                        select assay_id from deapp.de_subject_sample_mapping where concept_code='$concept_cd'
                                    )
                                ) a
                                inner join
                                (
                                  select patient_id, assay_id from deapp.de_subject_sample_mapping where concept_code='$concept_cd'
                                ) b
                                on a.assay_id = b.assay_id
                                group by b.patient_id"""
        sql.eachRow(sqlt, { row ->
            if (row.score != null) {
                values.add(row.score);
            }
        })
        return values;
    }


    def getConceptDistributionDataForHighDimensionConceptFromCode(String concept_cd, String result_instance_id, omics_params) {
        log.info("Getting concept distribution data for high dimension concept code: $concept_cd and result_instance_id: " +
                "$result_instance_id and omics_params: $omics_params")
        ArrayList<Double> values = new ArrayList<Double>();
        if (result_instance_id.trim() == "") {
            log.info("getConceptDistributionDataForHighDimensionConceptFromCode called with no result_istance_id")
            return getConceptDistributionDataForHighDimensionConcept(concept_cd, omics_params)
        }
        Sql sql = new Sql(dataSource)
        String sqlt = """select b.patient_id, avg($omics_params.omics_projection_type) as score from
                                (
                                    select * from deapp.de_subject_microarray_data
                                    where probeset_id in
                                    (
                                        select probeset_id from deapp.de_mrna_annotation
                                        where gene_symbol='$omics_params.omics_selector'
                                    )
                                    and assay_id in
                                    (
                                        select assay_id from deapp.de_subject_sample_mapping where patient_id in
                                        (
                                          select patient_num from i2b2demodata.qt_patient_set_collection where result_instance_id=$result_instance_id
                                        )
                                        and concept_code='$concept_cd'
                                    )
                                ) a
                                inner join
                                (
                                  select patient_id, assay_id from deapp.de_subject_sample_mapping where patient_id in
                                  (
                                    select patient_num from i2b2demodata.qt_patient_set_collection where result_instance_id=$result_instance_id
                                  )
                                  and concept_code='$concept_cd'
                                ) b
                                on a.assay_id = b.assay_id
                                group by b.patient_id"""
        sql.eachRow(sqlt, { row ->
            if (row.score != null) {
                values.add(row.score);
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

    def getSearchResultsForGene(String searchString, String gplid) {
        def sqlt = """select distinct gene_symbol, gpl_id
                      from deapp.de_mrna_annotation
                      where lower(gpl_id)=?
                      and lower(gene_symbol) like ?
                      order by gene_symbol"""
        Sql sql = new Sql(dataSource)
        def results = []
        sql.eachRow(sqlt, [gplid.toLowerCase(), searchString.toLowerCase() + "%"], { row ->
            results.add([label: row.gene_symbol])
        })
        results
    }

    def getHighDimensionalConceptSet(String result_instance_id1, String result_instance_id2) {
        def result = []

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
    def getHighDimensionalConceptKeysInSubset(String resultInstanceId) {
        Sql sql = new Sql(dataSource)
        String sqlt = """SELECT REQUEST_XML FROM QT_QUERY_MASTER c INNER JOIN QT_QUERY_INSTANCE a
		    ON a.QUERY_MASTER_ID=c.QUERY_MASTER_ID INNER JOIN QT_QUERY_RESULT_INSTANCE b
		    ON a.QUERY_INSTANCE_ID=b.QUERY_INSTANCE_ID WHERE RESULT_INSTANCE_ID = ?""";

        String xmlrequest = "";
        def concepts = []
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
                        def constraint_params = [:]
                        constraint_params.concept_key = i.item_key.toString()
                        constraint_params.omics_selector = i.constrain_by_omics_value.omics_selector.toString()
                        constraint_params.omics_value_type = i.constrain_by_omics_value.omics_value_type.toString()
                        constraint_params.omics_projection_type = i.constrain_by_omics_value.omics_projection_type.toString()
                        concepts.add(constraint_params)
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
    def ExportTableNew addHighDimConceptDataToTable(ExportTableNew tablein, omics_constraint, String result_instance_id) {
        checkQueryResultAccess result_instance_id

        if (omics_constraint.omics_value_type == 'GENE_EXPRESSION') {
            if (! ['raw_intensity','log_intensity','zscore'].contains(omics_constraint.omics_projection_type.toLowerCase())) {
                log.error "Unsupported projection type: " + omics_constraint.omics_projection_type
                return tablein
            }
            def concept_key = omics_constraint.concept_key
            def gene_symbol = omics_constraint.omics_selector
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

            String sqlt = """select b.patient_id, avg($omics_constraint.omics_projection_type) as score from
                                (
                                select * from deapp.de_subject_microarray_data
                                where probeset_id in
                                (
                                select probeset_id from deapp.de_mrna_annotation where gpl_id=
                                (
                                  select gpl_id from deapp.de_subject_sample_mapping where patient_id in
                                  (select patient_num from i2b2demodata.qt_patient_set_collection where result_instance_id=$result_instance_id)
                                  limit 1
                                )
                                and gene_symbol='$gene_symbol'
                                )
                                ) a

                                inner join
                                (
                                  select patient_id, assay_id from deapp.de_subject_sample_mapping where patient_id in
                                  (select patient_num from i2b2demodata.qt_patient_set_collection where result_instance_id=$result_instance_id)
                                  and concept_code=
                                  (
                                    select concept_cd from i2b2demodata.concept_dimension where concept_path='$concept_key'
                                  )
                                ) b
                                on a.assay_id = b.assay_id
                                group by b.patient_id"""

            sql.eachRow(sqlt, { row ->
                /*If I already have this subject mark it in the subset column as belonging to both subsets*/
                String subject = row.patient_id
                Double value = row.score
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
        }
        else {
            log.error "Omics data type " + omics_constraint.omics_value_type + " not yet implemented."
        }
        return tablein;
    }

    def getPlatform(String concept_key) {
        List<Patient> patientList = conceptsResourceService.getByKey(concept_key).getPatients()
        if (patientList.isEmpty())
            return null
        Set<Assay> assayList = patientList[0].getAssays()
        if (assayList.isEmpty())
            return null
        return assayList[0].getPlatform()
    }
}
