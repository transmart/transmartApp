package com.recomdata.transmart.util

class UtilService {

    boolean transactional = false

    def toListString(List<Object> objList) {
        StringBuilder objToString = new StringBuilder()
        objList.each { obj ->
            if (obj && (obj?.toString())?.trim() != '') {
                if (obj instanceof String) {
                    objToString.append("'").append(obj.toString()).append("'")
                } else {
                    objToString.append(obj.toString())
                }
                objToString.append(",")
            }
        }

        if (objToString.length() > 1) objToString.delete(objToString.length() - 1, objToString.length())

        return objToString.toString()
    }

    def getActualPatientId(String sourceSystemCode) {
        def splitArr = sourceSystemCode.split(':')

        def actualPatientId = splitArr[splitArr.length - 1]

        return actualPatientId
    }
}
