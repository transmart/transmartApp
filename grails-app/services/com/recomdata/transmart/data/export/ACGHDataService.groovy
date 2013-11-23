package com.recomdata.transmart.data.export

import com.recomdata.transmart.data.export.util.FileWriterUtil
import groovy.transform.CompileStatic
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.acgh.AcghValues
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.db.dataquery.highdim.acgh.RegionRow

import javax.annotation.PostConstruct

class ACGHDataService {

    def queriesResourceService
    HighDimensionResource highDimensionResourceService
    HighDimensionDataTypeResource<RegionRow> acghResource
    def sessionFactory

    @PostConstruct
    void init() {
        /* No way to automatically inject the acgh resource in Spring?
         * Would be easy in CDI by having a producer of HighDimensionDataTypeResource
         * beans creating it on the fly by looking at the injection point */
        acghResource = highDimensionResourceService.getSubResourceForType 'acgh'
    }

    def writeRegions(String study,
                     File studyDir,
                     String fileName,
                     String jobName,
                     resultInstanceId) {
        def assayConstraints = [
                acghResource.createAssayConstraint(
                        AssayConstraint.TRIAL_NAME_CONSTRAINT,
                        name: study),
                acghResource.createAssayConstraint(
                        AssayConstraint.PATIENT_SET_CONSTRAINT,
                        result_instance_id: resultInstanceId as Long),
        ]
        def projection = acghResource.createProjection([:], 'acgh_values')

        def result,
            writerUtil;

        try {
            /* dataType == 'aCGH' => file created in a subdir w/ that name */
            writerUtil = new FileWriterUtil(studyDir, fileName, jobName, 'aCGH',
                    null, "\t" as char)
            result = acghResource.retrieveData assayConstraints, [], projection
            doWithResult(result, writerUtil)
        } finally {
            writerUtil?.finishWriting()
            result?.close()
        }
    }

    @CompileStatic
    private doWithResult(TabularResult<AssayColumn, RegionRow> regionResult,
                         FileWriterUtil writerUtil) {

        List<AssayColumn> assays = regionResult.indicesList
        String[] header = createHeader(assays)
        writerUtil.writeLine(header as String[])

        def templateArray = new String[header.size() + 1]
        //+1 b/c 1st row has no header
        Long i = 1; //for the first row
        for (Iterator<RegionRow> iterator = regionResult.rows; ; iterator.hasNext()) {
            RegionRow row = (RegionRow) iterator.next()
            if (!row) {
                break
            }
            String[] line = templateArray.clone()

            line[0] = i++ as String
            line[1] = row.chromosome as String
            line[2] = row.start as String
            line[3] = row.end as String
            line[4] = row.numberOfProbes as String
            line[5] = row.cytoband

            int j = 6
            PER_ASSAY_COLUMNS.each {k, Closure<AcghValues> value ->
                assays.each { AssayColumn assay ->
                    line[j++] = value(row[assay]) as String
                }
            }

            writerUtil.writeLine(line)
        }
    }

    private static final Map PER_ASSAY_COLUMNS = [
            chip:     { AcghValues v -> v.getChipCopyNumberValue() },
            flag:     { AcghValues v -> v.getCopyNumberState().getIntValue() },
            probloss: { AcghValues v -> v.getProbabilityOfLoss() },
            probnorm: { AcghValues v -> v.getProbabilityOfNormal() },
            probgain: { AcghValues v -> v.getProbabilityOfGain() },
            probamp:  { AcghValues v -> v.getProbabilityOfAmplification() },
    ]

    private String[] createHeader(List<AssayColumn> assays) {
        List<String> r = [
                'chromosome',
                'start',
                'end',
                'num.probes',
                'cytoband',
        ];

        PER_ASSAY_COLUMNS.keySet().each {String head ->
            assays.each { AssayColumn assay ->
                r << "${head}.${assay.patientInTrialId}".toString()
            }
        }

        r.toArray(new String[r.size()])
    }
}
