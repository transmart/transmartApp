package org.transmartfoundation.status

import grails.util.Holders
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection

class GwavaStatusService {

    def errorMessage = ""

    def getStatus() {

        def urlString = Holders.config.com.recomdata.rwg.webstart.codebase
        def enabled = isGwavaEnabled()
        def canConnect = canConnect(urlString);

        def settings = [
                'url'                   : urlString,
                'enabled'               : enabled,
                'connected'             : canConnect,
                'errorMessage'          : errorMessage,
                'lastProbe'             : new Date()
        ]

        GwavaStatus status = new GwavaStatus(settings)
        return status
    }

    def isGwavaEnabled() {
        return !!Holders.config.com.recomdata.rwg.webstart.transmart.url;
    }

    def canConnect(urlString) {
        errorMessage = "URL did not respond"
        boolean sawText = false
        URL gwava
        try {
            gwava = new URL(urlString)
            gwava.eachLine {line ->
                errorMessage = ""
                if (line.contains("GWAVA QuickStart")) {
                    sawText = true;
                }
            }
        } catch (MalformedURLException e) {
            errorMessage = "MalformedURLException: " + e.message
        } catch (IOException e1) {
            errorMessage = "IOException: " + e1.message
        } catch (Exception ex) {
            errorMessage = "Unexpected error: " + ex.message
        }
        return sawText
    }
}
