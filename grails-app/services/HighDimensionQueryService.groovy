import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import groovy.sql.Sql
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.querytool.ConstraintByOmicsValue

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess
/**
 * Author: Denny Verbeeck (dverbeec@its.jnj.com)
 */
class HighDimensionQueryService {

    def dataSource
    def i2b2HelperService
    def highDimensionResourceService
    def springSecurityService

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
                xml = new XmlSlurper().parse(new StringReader(clobToString(row.request_xml)))
            } catch (exception) {
                throw new InvalidRequestException('Malformed XML document: ' +
                        exception.message, exception)
            }
            xml.panel.each { p ->
                p.item.each { i ->
                    def constraint_params = [:]
                    if (i.constrain_by_omics_value?.size()) {
                        constraint_params.concept_key = i.item_key.toString()
                        constraint_params.omics_selector = i.constrain_by_omics_value.omics_selector.toString()
                        constraint_params.omics_value_type = i.constrain_by_omics_value.omics_value_type.toString()
                        constraint_params.omics_value_operator = i.constrain_by_omics_value.omics_value_operator.toString()
                        constraint_params.omics_value_constraint = i.constrain_by_omics_value.omics_value_constraint.toString()
                        constraint_params.omics_projection_type = i.constrain_by_omics_value.omics_projection_type.toString()
                        constraint_params.omics_property = i.constrain_by_omics_value.omics_property.toString()
                    }
                    else if (i.item_key) {
                        // high dimensional concept but no value filter
                        constraint_params.concept_key = i.item_key.toString()
                    }
                    concepts.add(constraint_params)
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
        def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)

        if (!i2b2HelperService.isValidOmicsParams(omics_constraint)) {
            return i2b2HelperService.addConceptDataToTable(tablein, omics_constraint.concept_key, result_instance_id, user)
        }

        def concept_key = omics_constraint.concept_key
        def selector = omics_constraint.omics_selector
        def projection_type = omics_constraint.omics_projection_type
        String columnname =  "${projection_type}: ${selector} in ${concept_key}"
        String columnid = columnname.encodeAsSHA1()

        /*add the column to the table if its not there*/
        if (tablein.getColumn("subject") == null) {
            tablein.putColumn("subject", new ExportColumn("subject", "Subject", "", "string"));
        }
        if (tablein.getColumn(columnid) == null) {
            tablein.putColumn(columnid, new ExportColumn(columnid, columnname, "", "number"));
        }

        def resource = highDimensionResourceService.getHighDimDataTypeResourceFromConcept(concept_key)

        if (resource != null) {

            def data = resource.getDistribution(
                    new ConstraintByOmicsValue(projectionType: omics_constraint.omics_projection_type,
                            property      : omics_constraint.omics_property,
                            selector      : omics_constraint.omics_selector),
                    concept_key,
                    (result_instance_id == "" ? null : result_instance_id as Long))

            data.each { s, v ->
                String subject = s.toString() // this is a Long
                String value = v.toString() // this is a Double
                /*If I already have this subject mark it in the subset column as belonging to both subsets*/
                if (tablein.containsRow(subject)) /*should contain all subjects already if I ran the demographics first*/ {
                    tablein.getRow(subject).put(columnid, value);
                } else
                /*fill the row*/ {
                    ExportRowNew newrow = new ExportRowNew();
                    newrow.put("subject", subject);
                    newrow.put(columnid, value);
                    tablein.putRow(subject, newrow);
                }
            }
        }

        return tablein;
    }

    /**
     * Converts a clob to a string for retuirned Oracle columns
     */
    def String clobToString(clob) {
        if (clob == null) {
            return ""
        };
        if (clob instanceof String) {
            // postgres schema uses strings in some places oracle uses clobs
            return clob
        }
        def buffer = new byte[1000];
        def num = 0;
        def inStream = clob.asciiStream;
        def out = new ByteArrayOutputStream();
        while ((num = inStream.read(buffer)) > 0) {
            out.write(buffer, 0, num);
        }
        return new String(out.toByteArray());
    }

}
