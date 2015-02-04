package transmartapp

import grails.converters.JSON
import org.transmart.biomart.Disease
import org.transmart.biomart.Observation

class DiseaseController {

    def diseaseService

    /**
     * Find the top 15 diseases with a case-insensitive LIKE
     */
    def extSearch = {
        def paramMap = params
        def value = params.term.toUpperCase();

        def observations = null;
        //eQTL requires just disease - GWAS types need diseases and observations
        def diseases = Disease.executeQuery("SELECT meshCode, disease FROM Disease d WHERE upper(d.disease) LIKE '%' || :term || '%'", [term: value], [max: 10]);
        if (!params.type.equals("eqtl")) {
            observations = Observation.executeQuery("SELECT code, name, codeSource FROM Observation o WHERE upper(o.name) LIKE '%' || :term || '%'", [term: value], [max: 10]);
        }

        def itemlist = [];
        for (disease in diseases) {
            itemlist.add([id: disease[0], keyword: disease[1], sourceAndCode: "MESH:" + disease[0], category: "DISEASE", display: "Disease"]);
        }
        for (observation in observations) {
            itemlist.add([id: observation[0], keyword: observation[1], sourceAndCode: observation[2] + ":" + observation[0], category: "OBSERVATION", display: "Observation"]);
        }

        render itemlist as JSON;
    }

    def getMeshLineage = {
        try {
            def paramMap = params
            def code = params.code
            def disease = Disease.findByMeshCode(code)
            def hierarchy = diseaseService.getMeshLineage(disease)

            //Return the list of disease names and codes, and a parsed lineage for convenience
            def diseases = [];
            def path = "";
            for (dis in hierarchy) {
                if (dis != null) {
                    diseases.add([code: dis.meshCode, name: dis.disease])
                }
            }
            def returnData = [diseases: diseases]
            render returnData as JSON
        }
        catch (Exception e) {
            e.printStackTrace()
            render(status: 500, text: e.getMessage())
        }
    }

}
