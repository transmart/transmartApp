import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import groovy.sql.Sql
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.querytool.ConstraintByOmicsValue

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

/**
 * Created by dverbeec on 18/03/2015.
 */
class OmicsQueryService {

    def dataSource
    def i2b2HelperService

    def getHighDimensionalConceptData(String concept_cd, String result_instance_id, omics_params) {
        log.info("Getting concept distribution data for high dimension concept code: $concept_cd and result_instance_id: " +
                "$result_instance_id and omics_params: $omics_params")
        List values = new ArrayList()

        String projection_type = omics_params.omics_projection_type.toUpperCase()
        try {
            ConstraintByOmicsValue.ProjectionType.valueOf(projection_type)
        }
        catch (IllegalArgumentException e) {
            log.error("An unknown projection type was passed to getHighDimensionalConceptData: $projection_type")
            return values
        }

        def info = ConstraintByOmicsValue.markerInfo[omics_params.omics_value_type]
        if (info == null) {
            log.error "Concept distribution not supported for omics_params: $omics_params"
            return values
        }

        if (info.filter_type == ConstraintByOmicsValue.FilterType.SINGLE_NUMERIC) {
            String sqlt;
            def sql_params = [omics_selector: omics_params.omics_selector,
                          concept_cd    : concept_cd,
                          result_instance_id: result_instance_id]

            String assay_id_subquery = "SELECT assay_id FROM deapp.de_subject_sample_mapping WHERE concept_code=:concept_cd"
            String ssm_subquery = "SELECT patient_id, assay_id FROM deapp.de_subject_sample_mapping WHERE concept_code=:concept_cd"
            if (result_instance_id != "") {
                assay_id_subquery += " AND patient_id IN (SELECT patient_num FROM i2b2demodata.qt_patient_set_collection WHERE result_instance_id=:result_instance_id)"
                ssm_subquery += " AND patient_id IN (SELECT patient_num FROM i2b2demodata.qt_patient_set_collection WHERE result_instance_id=:result_instance_id)"
            }


            if (patientIDPopulated(info.data_table)) {
                sqlt = """SELECT patient_id, AVG($omics_params.omics_projection_type) AS score FROM $info.data_table
                          WHERE $info.id_column IN (SELECT $info.annotation_id_column FROM $info.annotation_table WHERE $info.selector_column=:omics_selector)
                          AND assay_id IN ($assay_id_subquery)
                          GROUP BY patient_id"""
            } else {
                // patient id not populated in deapp data table, so we need to join with subject sample mapping table
                sqlt = """SELECT b.patient_id, AVG($omics_params.omics_projection_type) AS score FROM
                          (
                            SELECT * FROM $info.data_table WHERE $info.id_column IN (SELECT $info.annotation_id_column FROM $info.annotation_table WHERE $info.selector_column=:omics_selector)
                            AND assay_id IN ($assay_id_subquery)
                          ) a
                          INNER JOIN
                          (
                            $ssm_subquery
                          ) b
                          ON a.assay_id = b.assay_id
                          GROUP BY b.patient_id"""
            }
            log.info(sqlt)
            Sql sql = new Sql(dataSource)
            sql.eachRow(sqlt, sql_params, { row ->
                // can't add the rows directly, as they are not available anymore after the resultset is closed
                def thisrow = ["patient_id": row.patient_id, "score": row.score]
                values.add(thisrow)
            })
        }
        else if (info.filter_type == ConstraintByOmicsValue.FilterType.ACGH) {

        }
        else if (info.filter_type == ConstraintByOmicsValue.FilterType.VCF) {

        }

        return values;
    }

    def getConceptDistributionDataForHighDimensionConceptFromCode(String concept_cd, String result_instance_id, omics_params) {
        def rows = getHighDimensionalConceptData(concept_cd, result_instance_id, omics_params)
        ArrayList<Double> values = new ArrayList<Double>()

        rows.each { row ->
            if (row.score != null) {
                values.add(row.score);
            }
        }
        return values;
    }

    /**
     * Returns identifiers for a given platform that start with the given searchString
     * For gene expression, chromosomal and rnaseq platforms, gene symbols are returned
     * For proteomics platforms, uniprot_ids are returned
     * For mirna_qpcr platforms, mirna_ids are returned
     * @param searchString
     * @param gplid
     * @return
     */
    def getSearchResults(String searchString, String gplid) {
        def markersqlt = "SELECT  marker_type FROM deapp.de_gpl_info WHERE  LOWER(platform)=?"
        Sql markersql = new Sql(dataSource)
        List<String> markertypes = new ArrayList<String>()
        markersql.eachRow(markersqlt, [gplid.toLowerCase()], { row ->
            markertypes.add(row.marker_type)
        })

        if (markertypes.size() > 1) {
            log.warn "Search for markertype of platform $gplid returned ${markertypes.size()} results."
        }
        if (markertypes.size() == 0) {
            log.error "Search for markertype of platform $gplid returned no results."
            return new ArrayList<String>()
        }

        String markertype = markertypes.get(0)
        def info = ConstraintByOmicsValue.markerInfo[markertype]

        if (info == null) {
            log.error "Unsupported platform type queried: $markertype"
            return []
        }

        String sqlt = """SELECT DISTINCT $info.selector_column AS result, gpl_id
                      FROM $info.annotation_table
                      WHERE LOWER(gpl_id)=?
                      AND LOWER($info.selector_column) LIKE ?
                      ORDER BY $info.selector_column"""

        Sql sql = new Sql(dataSource)
        def results = []
        sql.eachRow(sqlt, [gplid.toLowerCase(), searchString.toLowerCase() + "%"], { row ->
            results.add([label: row.result])
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

        def info = ConstraintByOmicsValue.markerInfo[omics_constraint.omics_value_type]
        if (info == null) {
            log.error "Omics data type " + omics_constraint.omics_value_type + " invalid or not yet implemented."
            return tablein
        }
        else {
            if (!info.allowed_projections.collect {it.value.toLowerCase()}.contains(omics_constraint.omics_projection_type.toLowerCase())) {
                log.error "Unsupported projection type for $omics_constraint.omics_value_type: $omics_constraint.omics_projection_type, " +
                          "should be one of [" + info.allowed_projections.collect {it.value}.join(", ") + "]"
                return tablein
            }
        }

        def concept_key = omics_constraint.concept_key
        def gene_symbol = omics_constraint.omics_selector
        String columnid = (concept_key + gene_symbol + omics_constraint.omics_projection_type).encodeAsSHA1()
        String columnname = gene_symbol

        /*add the column to the table if its not there*/
        if (tablein.getColumn("subject") == null) {
            tablein.putColumn("subject", new ExportColumn("subject", "Subject", "", "string"));
        }
        if (tablein.getColumn(columnid) == null) {
            tablein.putColumn(columnid, new ExportColumn(columnid, columnname, "", "number"));
        }

        def values = getHighDimensionalConceptData(i2b2HelperService.getConceptCodeFromKey(concept_key), result_instance_id, omics_constraint)

        values.each { row ->
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
        }

        //pad all the empty values for this column
        for (ExportRowNew row : tablein.getRows()) {
            if (!row.containsColumn(columnid)) {
                row.put(columnid, "NULL");
            }
        }

        return tablein;
    }

    /**
     * Checks the given map for minimum required parameters for high-dimensional concepts
     * @param map
     * @return True if map contains required parameters, false otherwise
     */
    def hasRequiredParams(Map map) {
        map != null ? ["omics_value_type", "omics_projection_type", "omics_selector", "omics_platform"].inject(true) {result, key -> result && map.containsKey(key)} : false
    }

    private def patientIDPopulated(String table) {
        def sql = new Sql(dataSource)
        def sqlt = "SELECT COUNT(patient_id) AS count FROM " + table
        def row = sql.firstRow(sqlt)
        row.count > 0
    }
}
