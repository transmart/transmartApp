package org.transmartfoundation.status

class GwavaStatus {

    def url
    def connected
    def lastProbe
    def enabled
    def errorMessage

    String toString() {
        return "GwavaStatus (" + url + ") - probe at: " + lastProbe
    }
}
