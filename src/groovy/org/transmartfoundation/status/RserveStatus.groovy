package org.transmartfoundation.status

import java.util.Date;

class RserveStatus {

    String url
    boolean connected
    boolean simpleExpressionOK
    boolean librariesOk
    String lastErrorMessage
    Date lastProbe

    String toString () {
        return "RserveStatus (" + url + ") - probe at: " + lastProbe
    }

}
