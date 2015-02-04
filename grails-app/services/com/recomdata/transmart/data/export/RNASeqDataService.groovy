package com.recomdata.transmart.data.export

import com.recomdata.transmart.data.export.util.FileWriterUtil
import groovy.transform.CompileStatic
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.rnaseq.RnaSeqValues

import javax.annotation.PostConstruct

class RNASeqDataService {

    HighDimensionResource highDimensionResourceService
    HighDimensionDataTypeResource<RegionRow> rnaSeqResource

    @PostConstruct
    void init() {
        /* No way to automatically inject the acgh resource in Spring?
         * Would be easy in CDI by having a producer of HighDimensionDataTypeResource
         * beans creating it on the fly by looking at the injection point */
        rnaSeqResource = highDimensionResourceService.getSubResourceForType 'rnaseq'
    }

    def writeRegions(String study,
                     File studyDir,
                     String fileName,
                     String jobName,
                     resultInstanceId) {
        def assayConstraints = [
                rnaSeqResource.createAssayConstraint(
                        AssayConstraint.TRIAL_NAME_CONSTRAINT,
                        name: study),
                rnaSeqResource.createAssayConstraint(
                        AssayConstraint.PATIENT_SET_CONSTRAINT,
                        result_instance_id: resultInstanceId as Long),
        ]

        def projection = rnaSeqResource.createProjection([:], 'rnaseq_values')

        def result,
            writerUtil;

        try {
            /* dataType == 'RNASeq' => file created in a subdir w/ that name */
            writerUtil = new FileWriterUtil(studyDir, fileName, jobName, 'RNASeq',
                    null, "\t" as char)
            result = rnaSeqResource.retrieveData assayConstraints, [], projection
            doWithResult(result, writerUtil)
        } finally {
            writerUtil?.finishWriting()
            result?.close()
        }
    }

    @CompileStatic
    private doWithResult(TabularResult<AssayColumn, RegionRow> rnaseqRegionResult,
                         FileWriterUtil writerUtil) {

        List<AssayColumn> assays = rnaseqRegionResult.indicesList
        String[] header = createHeader(assays)
        writerUtil.writeLine(header as String[])

        def templateArray = new String[header.size() + 1]
        //+1 b/c 1st row has no header
        Long i = 1; //for the first row

        for (Iterator<RegionRow> iterator = rnaseqRegionResult.rows; iterator.hasNext();) {
            RegionRow row = (RegionRow) iterator.next()

            String[] line = templateArray.clone()

            line[0] = i++ as String
            line[1] = row.name as String
            line[2] = row.chromosome as String
            line[3] = row.start as String
            line[4] = row.end as String
            line[5] = row.numberOfProbes as String
            line[6] = row.cytoband

            int j = 7
            PER_ASSAY_COLUMNS.each { k, Closure<RnaSeqValues> value ->
                assays.each { AssayColumn assay ->
                    line[j++] = value(row.getAt(assay)) as String
                }
            }

            writerUtil.writeLine(line)
        }
    }

    private static final Map PER_ASSAY_COLUMNS = [
            readcount: { RnaSeqValues v -> v.getReadCount() },
    ]

    private String[] createHeader(List<AssayColumn> assays) {
        List<String> r = [
                'regionname',
                'chromosome',
                'start',
                'end',
                'num.probes',
                'cytoband',
        ];

        PER_ASSAY_COLUMNS.keySet().each { String head ->
            assays.each { AssayColumn assay ->
                r << "${head}.${assay.patientInTrialId}".toString()
            }
        }

        r.toArray(new String[r.size()])
    }
}
