package com.recomdata.dataexport.util

import org.apache.commons.lang.StringUtils;

class ExportUtil {

    public static String getShortConceptPath(String conceptPath, removalArr) {
        def arr = StringUtils.split(conceptPath, "\\")
        def valList = []
        //Remove upto Study-name and any string values specified in the removalArr
        if (arr.length > 2) arr.eachWithIndex { val, i ->
            def valShouldBeRemoved = false
            removalArr.each { removalVal ->
                if (StringUtils.equalsIgnoreCase(removalVal, val)) {
                    valShouldBeRemoved = true
                    return
                }
            }

            if (i > 1 && !valShouldBeRemoved) {
                valList.add(val)
            } else if (valShouldBeRemoved) {
                arr[i] = ''
            }
        }

        def shortenedConceptPath = StringUtils.join(valList, '\\')
        shortenedConceptPath = StringUtils.leftPad(shortenedConceptPath, shortenedConceptPath.length() + 1, '\\')

        return shortenedConceptPath
    }

    public static String getSampleValue(String value, String sampleType, String timepoint, String tissueType) {
        def retVal = null;
        if (StringUtils.equalsIgnoreCase(value, "E") || StringUtils.equalsIgnoreCase(value, "normal")) {
            def retVals = []
            if (null != sampleType && StringUtils.isNotEmpty(sampleType)) retVals.add(sampleType)
            if (null != timepoint && StringUtils.isNotEmpty(timepoint)) retVals.add(timepoint)
            if (null != tissueType && StringUtils.isNotEmpty(tissueType)) retVals.add(tissueType)
            retVal = StringUtils.join(retVals, "/")
        } else {
            retVal = value
        }
        return retVal
    }
}
