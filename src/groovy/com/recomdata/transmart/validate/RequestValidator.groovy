/**
 *
 */
package com.recomdata.transmart.validate

import org.apache.log4j.Logger;

/**
 * @author SMunikuntla
 *
 */
abstract class RequestValidator {

    def static log = Logger.getLogger(RequestValidator.class)
    /**
     * Helper method to return null from Javascript calls
     *
     * @param inputArg - the input arguments
     * @return null or the input argument if it is not null (or empty or undefined)
     */
    public static String nullCheck(inputArg) {
        log.debug("Input argument to nullCheck: ${inputArg}")
        if (inputArg == "undefined" || inputArg == "null" || inputArg == "") {
            log.debug("Returning null in nullCheck")
            return null
        }
        return inputArg
    }

}
