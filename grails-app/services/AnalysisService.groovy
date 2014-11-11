/**
 * This class contains methods that will help us ran our analysis components like the modules in the advanced workflow menu.
 * @author MMcDuffie
 *
 */
class AnalysisService {

    def dataSource;

    /**
     * Get a list of genes from the database for a given list of Sample Ids. We access the haploview_data table to get this information.
     * @return
     */
    def getGenesForHaploviewFromSampleId(result) {
        //This will be a list of all the distinct genes for the selected patients.
        def genes = [];

        //Use this datasource to get the genes.
        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

        //Query the Haploview table directly to get the genes. The Sample ID should be the "i2b2_id" or patient_num
        String sqlt = """	SELECT	DISTINCT gene
							FROM	haploview_data HD
							WHERE	HD.I2B2_ID IN (?) order by gene asc"""

        //We get a distinct list that covers all the subsets.
        result.each
                {
                    currentSampleList ->

                        String[] currentStringArray = (String[]) currentSampleList.value

                        sql.eachRow(sqlt, [quoteCSV(currentStringArray.join(","))],
                                {
                                    row ->
                                        if (!genes.get(row.gene)) genes.add(row.gene);
                                })
                }
        return genes;
    }

    def String quoteCSV(String val) {
        String[] inArray;
        StringBuilder s = new StringBuilder();

        if (val != null && val.length() > 0) {
            inArray = val.split(",");
            s.append("'" + inArray[0] + "'");
            for (int i = 1; i < inArray.length; i++) {
                s.append(",'" + inArray[i] + "'");
            }
        }
        return s.toString();
    }

}
