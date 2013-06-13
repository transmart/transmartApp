package com.recomdata.transmart.data.export

import com.recomdata.transmart.data.export.util.FileWriterUtil
import groovy.transform.CompileStatic
import org.transmartproject.core.dataquery.acgh.ACGHValues
import org.transmartproject.core.dataquery.acgh.RegionResult
import org.transmartproject.core.dataquery.acgh.RegionRow
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.constraints.ACGHRegionQuery
import org.transmartproject.core.dataquery.constraints.CommonHighDimensionalQueryConstraints
import org.transmartproject.core.dataquery.acgh.Region



class ACGHDataService {

    def dataQueryResourceNoGormService
    def queriesResourceService
    def sessionFactory

    def writeRegions(String study,
                     File studyDir,
                     String fileName,
                     String jobName,
                     resultInstanceId) {
        def q = new ACGHRegionQuery(
                common: new CommonHighDimensionalQueryConstraints(
                        studies: [study],
                        patientQueryResult: queriesResourceService.
                                getQueryResultFromId(resultInstanceId as Long)
                )
        );
        def session,
            result,
            writerUtil;

        try {
            /* dataType == 'aCGH' => file created in a subdir w/ that name */
            writerUtil = new FileWriterUtil(studyDir, fileName, jobName, 'aCGH',
                    null, "\t" as char)
            session = sessionFactory.openStatelessSession()
            result = dataQueryResourceNoGormService.runACGHRegionQuery(q, session)

            doWithResult(result, writerUtil)
        } finally {
            writerUtil?.finishWriting()
            result?.close()
            session?.close()
        }
    }

    @CompileStatic
    private doWithResult(RegionResult regionResult,
                         FileWriterUtil writerUtil) {

        List<Assay> assays = regionResult.indicesList
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
            Region region = row.region

            line[0] = i++ as String
            line[1] = region.chromosome as String
            line[2] = region.start as String
            line[3] = region.end as String
            line[4] = region.numberOfProbes as String

            int j = 5
            PER_ASSAY_COLUMNS.each {k, Closure<ACGHValues> value ->
                assays.each {Assay assay ->
                    line[j++] = value(row.getRegionDataForAssay(assay)) as String
                }
            }

            writerUtil.writeLine(line)
        }
    }

    private static final Map PER_ASSAY_COLUMNS = [
            chip: {ACGHValues v -> v.getChipCopyNumberValue()},
            flag: {ACGHValues v -> v.getCopyNumberState().getIntValue()},
            probloss: {ACGHValues v -> v.getProbabilityOfLoss()},
            probnorm: {ACGHValues v -> v.getProbabilityOfNormal()},
            probgain: {ACGHValues v -> v.getProbabilityOfGain()},
            probamp: {ACGHValues v -> v.getProbabilityOfAmplification()},
    ]

    private String[] createHeader(List<Assay> assays) {
        List<String> r = [
                'chromosome',
                'start',
                'end',
                'num.probes',
        ];

        PER_ASSAY_COLUMNS.keySet().each {String head ->
            assays.each {Assay assay ->
                /* FIXME: subjectId is not public */
                r << "${head}.${assay.subjectId}".toString()
            }
        }

        r.toArray(new String[r.size()])
    }
}
