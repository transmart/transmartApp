import org.transmart.biomart.BioDataExternalCode


/**
 * $Id: DetailsService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 */
//import org.dom4j.Document;
//import org.dom4j.Element;
//import org.dom4j.DocumentException;
//import org.dom4j.io.SAXReader;

/**
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
class DetailsService {


    def getHydraGeneID(id) {

        def query = "SELECT DISTINCT bec.code FROM org.transmart.biomart.BioDataExternalCode bec WHERE bec.bioDataId=? AND bec.codeType='HYDRA_GENE_ID'";
        def result = BioDataExternalCode.executeQuery(query, Long.valueOf(String.valueOf(id)));
        if (result != null && result.size() > 0)
            return result[0]
        else
            return ""
    }
}
