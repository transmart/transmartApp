package org.transmartproject.export

import org.gmock.GMockController
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.SampleType
import org.transmartproject.core.dataquery.assay.Timepoint
import org.transmartproject.core.dataquery.assay.TissueType
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.Platform

class MockTabularResultHelper {

    GMockController gMockController

    List<AssayColumn> createSampleAssays(int n) {
        (1..n).collect {
            createMockAssay(it, "assay_${it}", "sample_code_${it}",
                    "patient_${it}_subject_id", "sampletype_${it}",
                    "timepoint_${it}", "tissuetype_${it}", "" + it * 10)
        }
    }

    DataRow createRowForAssays(List<AssayColumn> assays,
                               List data,
                               String label) {
        createMockRow(
                dot(assays, data, { a, b -> [a, b] })
                        .collectEntries(Closure.IDENTITY),
                label)
    }

    List dot(List list1, List list2, function) {
        def res = []
        for (int i = 0; i < list1.size(); i++) {
            res << function(list1[i], list2[i])
        }
        res
    }

    List<Patient> createPatients(int n) {
        (1..n).collect {
            Patient p = mock(Patient)
            p.inTrialId.returns("subject id #$it".toString()).atLeastOnce()
            p
        }
    }

    List<String> createPatientRowLabels(int n) {
        (1..n).collect {
            "patient #$it" as String
        }
    }

    TabularResult<AssayColumn, DataRow> createMockTabularResult(Map params) {
        List<AssayColumn> sampleAssays = params.assays
        Map<String, List<Object>> labelToData = params.data
        String columnsDimensionLabel = params.columnsLabel
        String rowsDimensionLabel = params.rowsLabel

        def iterator = labelToData.collect { String label, List data ->
            createRowForAssays(sampleAssays, data, label)
        }.iterator()

        TabularResult highDimResult = mock TabularResult
        highDimResult.indicesList.returns(sampleAssays).stub()
        highDimResult.getRows().returns(iterator).stub()
        highDimResult.iterator().returns(iterator).stub()

        if (columnsDimensionLabel) {
            highDimResult.columnsDimensionLabel.returns columnsDimensionLabel
        }
        if (rowsDimensionLabel) {
            highDimResult.rowsDimensionLabel.returns rowsDimensionLabel
        }

        highDimResult
    }


    private AssayColumn createMockAssay(
            Long id = null, String label = null, String sampleCode = null,
            String patientInTrialId = null, String sampleTypeLabel = null,
            String timepointLabel = null, String tissueTypeLabel = null,
            String platformId = null
    ) {
        [
                getId              : { -> id },
                getLabel           : { -> label },
                getSampleCode      : { -> sampleCode },
                getPatientInTrialId: { -> patientInTrialId },
                getSampleType      : { ->
                    [
                            getLabel: { -> sampleTypeLabel }
                    ] as SampleType
                },
                getTimepoint       : { ->
                    [
                            getLabel: { -> timepointLabel }
                    ] as Timepoint
                },
                getTissueType      : { ->
                    [
                            getLabel: { -> tissueTypeLabel }
                    ] as TissueType
                },
                getPlatform        : { ->
                    [
                            getId: { -> platformId }
                    ] as Platform
                },
                equals             : { other -> delegate.is(other) },
                toString           : { -> "assay for $patientInTrialId" as String }
        ] as AssayColumn
    }

    private DataRow<AssayColumn, Object> createMockRow(Map<AssayColumn, Object> values,
                                                       String label) {
        DataRow row = mock(DataRow)
        row.label.returns(label).stub()

        values.eachWithIndex { entry, i ->
            row.getAt(i).returns(entry.value).stub()
        }
        values.keySet().each { column ->
            row.getAt(column).returns(values[column]).stub()
        }
        row.iterator().returns(values.values().iterator()).stub()

        row
    }

    private Object mock(Class clazz) {
        gMockController.mock clazz
    }
}
