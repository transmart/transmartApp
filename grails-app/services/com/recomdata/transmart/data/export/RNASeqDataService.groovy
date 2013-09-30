package com.recomdata.transmart.data.export

import com.recomdata.transmart.data.export.util.FileWriterUtil
import groovy.transform.CompileStatic
import org.transmartproject.core.dataquery.rnaseq.RNASEQValues
import org.transmartproject.core.dataquery.rnaseq.RegionRNASeqResult
import org.transmartproject.core.dataquery.rnaseq.RegionRNASeqRow
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.constraints.ACGHRegionQuery
import org.transmartproject.core.dataquery.constraints.CommonHighDimensionalQueryConstraints
import org.transmartproject.core.dataquery.acgh.Region



class RNASeqDataService {

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
            /* dataType == 'RNASeq' => file created in a subdir w/ that name */
            writerUtil = new FileWriterUtil(studyDir, fileName, jobName, 'RNASeq',
                    null, "\t" as char)
            session = sessionFactory.openStatelessSession()
            result = dataQueryResourceNoGormService.runRNASEQRegionQuery(q, session)
            doWithResult(result, writerUtil)
        } finally {
            writerUtil?.finishWriting()
            result?.close()
            session?.close()
        }
    }

    @CompileStatic
    private doWithResult(RegionRNASeqResult regionRNASeqResult,
                         FileWriterUtil writerUtil) {

        List<Assay> assays = regionRNASeqResult.indicesList
        String[] header = createHeader(assays)
        writerUtil.writeLine(header as String[])

        def templateArray = new String[header.size() + 1]
        //+1 b/c 1st row has no header
        Long i = 1; //for the first row
        for (Iterator<RegionRNASeqRow> iterator = regionRNASeqResult.rows; ; iterator.hasNext()) {
            RegionRNASeqRow row = (RegionRNASeqRow) iterator.next()
            if (!row) {
                break
            }
            String[] line = templateArray.clone()
            Region region = row.region

            line[0] = i++ as String
            line[1] = region.name as String
            line[2] = region.chromosome as String
            line[3] = region.start as String
            line[4] = region.end as String
            line[5] = region.numberOfProbes as String
            line[6] = region.cytoband

            int j = 7
            PER_ASSAY_COLUMNS.each {k, Closure<RNASEQValues> value ->
                assays.each {Assay assay ->
                    line[j++] = value(row.getRegionDataForAssay(assay)) as String
                }
            }

            writerUtil.writeLine(line)
        }
    }

    private static final Map PER_ASSAY_COLUMNS = [
            readcount: {RNASEQValues v -> v.getReadCountValue()},
    ]

    private String[] createHeader(List<Assay> assays) {
        List<String> r = [
                'regionname',
                'chromosome',
                'start',
                'end',
                'num.probes',
                'cytoband',
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
