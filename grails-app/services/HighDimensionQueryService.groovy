import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import grails.gorm.PagedResultList
import groovy.sql.Sql
import org.hibernate.criterion.Restrictions
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.querytool.ConstraintByOmicsValue
import org.transmartproject.db.dataquery.highdim.DeGplInfo
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.mirna.DeSubjectMirnaData
import org.transmartproject.db.dataquery.highdim.mrna.DeMrnaAnnotationCoreDb
import org.transmartproject.db.dataquery.highdim.mrna.DeSubjectMicroarrayDataCoreDb

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

/**
 * Created by dverbeec on 18/03/2015.
 */
class HighDimensionQueryService {

    def dataSource
    def i2b2HelperService
    def highDimensionResourceService

    def getHighDimensionalConceptSet(String result_instance_id1, String result_instance_id2) {
        def result = []

        if (result_instance_id1) result.addAll(getHighDimensionalConceptKeysInSubset(result_instance_id1));
        if (result_instance_id2) result.addAll(getHighDimensionalConceptKeysInSubset(result_instance_id2));

        result
    }

    /**
     *
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
                        constraint_params.omics_value_operator = i.constrain_by_omics_value.omics_value_operator.toString()
                        constraint_params.omics_value_constraint = i.constrain_by_omics_value.omics_value_constraint.toString()
                        constraint_params.omics_projection_type = i.constrain_by_omics_value.omics_projection_type.toString()
                        constraint_params.omics_property = i.constrain_by_omics_value.omics_property.toString()
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

        def concept_key = omics_constraint.concept_key
        def selector = omics_constraint.omics_selector
        String columnid = (concept_key + selector + omics_constraint.omics_projection_type).encodeAsSHA1()
        String columnname = selector + " in " + concept_key

        /*add the column to the table if its not there*/
        if (tablein.getColumn("subject") == null) {
            tablein.putColumn("subject", new ExportColumn("subject", "Subject", "", "string"));
        }
        if (tablein.getColumn(columnid) == null) {
            tablein.putColumn(columnid, new ExportColumn(columnid, columnname, "", "number"));
        }

        def resource = highDimensionResourceService.getHighDimDataTypeResourceFromConcept(concept_key)
        def concept_code = i2b2HelperService.getConceptCodeFromKey(concept_key)

        if (resource != null) {
            // this is an extra query, however if the cohort selection tab was used to generate the patient set,
            // this query was already executed to generate the histogram when specifying the limits, so the
            // results of this query should come from a cache
            def values = resource.getDistribution(
                    new ConstraintByOmicsValue(projectionType: ConstraintByOmicsValue.ProjectionType.forValue(omics_constraint.omics_projection_type),
                                           property      : omics_constraint.omics_property,
                                           selector      : omics_constraint.omics_selector),
                    concept_code,
                    Long.parseLong(result_instance_id))

            values.each { row ->
                /*If I already have this subject mark it in the subset column as belonging to both subsets*/
                String subject = row[0]
                Double value = row[1]
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
        }

        return tablein;
    }
}
