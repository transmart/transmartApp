package transmartapp

import grails.converters.JSON
import org.transmart.biomart.Experiment
import org.transmart.searchapp.*

class ExperimentController {

    def SpringSecurityService;
    def gwasWebService;

    /**
     * Find the top 20 experiments with a case-insensitive LIKE
     */
    def extSearch = {
        def paramMap = params
        def value = params.term.toUpperCase();
        def studyType = params.studyType?.toUpperCase();

        def experiments = Experiment.executeQuery("SELECT accession, title FROM Experiment e WHERE upper(e.title) LIKE '%' || :term || '%' AND upper(e.type) = :studyType", [term: value, studyType: studyType], [max: 20]);

        def category = "STUDY"
        def categoryDisplay = "Study"
        if (studyType.equals('I2B2')) {
            category = "i2b2"
            categoryDisplay = "i2b2"
        }
        def itemlist = [];
        for (exp in experiments) {
            itemlist.add([id: exp[0], keyword: exp[1], category: category, display: categoryDisplay]);
        }

        render itemlist as JSON;
    }

    /**
     * This will display a list of the available studies in the system to the user. The user will only be able to select one item from the dropdown.
     */
    def browseExperimentsSingleSelect = {

        def experiments
        def user=AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        def secObjs= getExperimentSecureStudyList()

        if (params.type) {
            experiments = Experiment.findAllByType(params.type)
            experiments = getSortedList(experiments)
        } else {
            experiments = Experiment.list()
            experiments = getSortedList(experiments)
        }

        experiments=experiments.findAll{!secObjs.containsKey(it.accession) || !gwasWebService.getGWASAccess(it.accession, user).equals("Locked") }
        render(template: 'browseSingle', model: [experiments: experiments])
    }

    /**
     *
     * Get list of secured (i.e. private) experiment.
     *
     */

    def getExperimentSecureStudyList(){  
		
        StringBuilder s = new StringBuilder();
        s.append("SELECT so.bioDataUniqueId, so.bioDataId FROM SecureObject so Where so.dataType='Experiment'")
        def t=[:];
        //return access levels for the children of this path that have them
        def results = SecureObject.executeQuery(s.toString());
        for (row in results){
            def token = row[0];
            def dataid = row[1];
            token=token.replaceFirst("EXP:","")
            log.info(token+":"+dataid);
            t.put(token,dataid);
        }
        return t;
    }
	
    /**
     * This will render a UI where the user can pick an experiment from a list of all the experiments in the system. Selection of multiple studies is allowed.
     */
    def browseExperimentsMultiSelect = {

        def experiments

        if (params.type) {
            experiments = Experiment.findAllByType(params.type)
            experiments = getSortedList(experiments)
        } else {
            experiments = Experiment.list()
            experiments = getSortedList(experiments)
        }

        render(template: 'browseMulti', model: [experiments: experiments])
    }

    def getSortedList(experiments) {

        experiments.sort({ a, b ->
            return a.title.trim().compareToIgnoreCase(b.title.trim());
        })

        return experiments
    }

}
