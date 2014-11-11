package com.recomdata.dataexport.dao

import com.recomdata.transmart.data.export.util.FileWriterUtil
import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationContext
import org.transmart.biomart.ClinicalTrial
import org.transmart.biomart.Compound
import org.transmart.biomart.Experiment
import org.transmart.biomart.Taxonomy

/**
 * This class has been replaced with MetadataService.
 * This class will provide access to the study metadata
 */
@Deprecated
public class StudyDao {
    ApplicationContext ctx = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getAttribute(org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes.APPLICATION_CONTEXT)
    def dataSource = ctx.getBean('dataSource')
    def springSecurityService = ctx.getBean('springSecurityService')
    def dataTypeName = "Study"
    def dataTypeFolder = null
    def char separator = '\t'
    private static final log = LogFactory.getLog('grails.app.' + StudyDao.class.name)

    //This is the list of parameters passed to the SQL statement.
    ArrayList parameterList = new ArrayList();

    /**
     * This method will gather study data and write it to a file.
     *  The file will contain basic study metadata
     * @param fileName
     * @param jobName
     * @param studyAccessions
     */
    public void getData(File studyDir, String fileName, String jobName, List<String> studyAccessions) {
        //Log the action of data access.
        //def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"i2b2DAO - getData", eventmessage:"RID:"+result_instance_ids.toString()+" Concept:"+conceptCodeList.toString(), accesstime:new java.util.Date())
        //al.save()

        log.info("loading study metadata for " + studyAccessions)
        // try to find it by Clinical Trial
        def studiesMap = [:]
        studyAccessions.each { studyUid ->
            def isTrial = true;
            // work around to fix the lazy loading issue - we don't have full transaction support there
            def exp = ClinicalTrial.findByTrialNumber(studyUid);
            //def exp =	ClinicalTrial.executeQuery("SELECT DISTINCT ct FROM ${ClinicalTrial.name} ct LEFT JOIN FETCH ct.organisms LEFT JOIN FETCH ct.compounds LEFT JOIN FETCH ct.diseases WHERE ct.trialNumber=?",studyUid);
            //def exp =	ClinicalTrial.find(" FROM ClinicalTrial as ct WHERE ct.trialNumber = :uid",[uid:studyUid]);
            //def exp =	ClinicalTrial.executeQuery("SELECT DISTINCT ct.trialNumber, ct.organisms, ct.compounds, ct.diseases FROM ${ClinicalTrial.name} ct LEFT JOIN FETCH ct.organisms LEFT JOIN FETCH ct.compounds LEFT JOIN FETCH ct.diseases WHERE ct.trialNumber = :uid",[uid:studyUid]);

            if (exp == null) {
                exp = Experiment.findByAccession(studyUid);
                //exp = Experiment.executeQuery("SELECT DISTINCT ct FROM org.transmart.biomart.Experiment ct LEFT JOIN FETCH ct.organisms LEFT JOIN FETCH ct.compounds LEFT JOIN FETCH ct.diseases");
                isTrial = false;
            }
            def organisms = Taxonomy.findAll(new Taxonomy(experiments: [exp]))
            def compounds = Compound.findAll(new Compound(experiments: [exp]))

            //exp.compounds; exp.organisms; exp.diseases;

            if (exp != null) {
                studiesMap.put(studyUid, getStudyData(exp, organisms, compounds))
            }
        }
        writeData(getStudyColumns(), studiesMap, studyDir, fileName, jobName)
    }

    private String writeData(String[] studyCols, Map studiesMap, File studyDir, String fileName, String jobName) {
        if (!studiesMap.isEmpty()) {

            def dataTypeName = null
            def dataTypeFolder = null
            def char separator = '\t'
            FileWriterUtil writerUtil = new FileWriterUtil(studyDir, fileName, jobName, dataTypeName, dataTypeFolder, separator)

            studyCols.eachWithIndex { studyCol, i ->
                def lineVals = [];
                lineVals.add(studyCol)
                studiesMap.each { key, studyData ->
                    lineVals.add(studyData[i])
                }
                writerUtil.writeLine(lineVals as String[])
            }

            writerUtil.finishWriting()
        }
    }

    protected String[] getStudyColumns() {

        /*def headers1=["Title" , "Trial Number", "Owner", "Description", "Study Phase", "Study Type", "Study Design", "Blinding procedure",
            "Duration of study (weeks)", "Completion date", "Inclusion Criteria", "Exclusion Criteria", "Dosing Regimen",
            "Type of Control", "Gender restriction mfb", "Group assignment", "Primary endpoints", "Secondary endpoints",
            "Route of administration", "Secondary ids", "Subjects", "Max age", "Min age", "Number of patients", "Number of sites", "Compounds", "Diseases", "Organisms"];
        */
        def headers1 = ["Title",
                        "Date",
                        "Owner",
                        "Institution",
                        "Country",
                        "Description",
                        "Access Type",
                        "Phase",
                        "Objective",
                        "BioMarker Type",
                        "Compound",
                        "Design Factor",
                        "Number of Patients",
                        "Organism",
                        "Target/Pathways"
        ]
        return headers1;
    }

    protected String[] getStudyData(Experiment exp, organisms, compounds) {
        def data = [];
        if (exp instanceof ClinicalTrial) {
            data.add(exp.title)
            data.add(exp.completionDate)
            data.add(exp.studyOwner)
            data.add(exp.institution)
            data.add(exp.country)
            data.add(exp.description)
            data.add(exp.accessType)
            data.add(exp.studyPhase)
            data.add(exp.design)
            data.add(exp.bioMarkerType)
            data.add(getCompoundNames(compounds))
            data.add(exp.overallDesign)
            data.add(exp.numberOfPatients)
            data.add(getOrganismNames(organisms))
            data.add(exp.target)
        } else {
            data.add(exp.title)
            data.add(exp.completionDate)
            data.add(exp.primaryInvestigator)
            data.add(exp.institution)
            data.add(exp.country)
            data.add(exp.description)
            data.add(exp.accessType)
            data.add("")
            data.add(exp.design)
            data.add(exp.bioMarkerType)
            data.add(getCompoundNames(compounds))
            data.add(exp.overallDesign)
            data.add("")
            data.add(getOrganismNames(organisms))
            data.add(exp.target)
        }

        return data as String[]
    }


    def getCompoundNames(compounds) {
        StringBuilder compoundNames = new StringBuilder()
        compounds.each {
            if (it.getName() != null) {
                if (compoundNames.length() > 0) {
                    compoundNames.append("; ")
                }
                compoundNames.append(it.getName())
            }
        }
        return compoundNames.toString()
    }

    def getOrganismNames(organisms) {
        StringBuilder taxNames = new StringBuilder()
        organisms.each {
            if (it.label != null) {
                if (taxNames.length() > 0) {
                    taxNames.append("; ")
                }
                taxNames.append(it.label)
            }
        }
        return taxNames.toString()
    }

}
