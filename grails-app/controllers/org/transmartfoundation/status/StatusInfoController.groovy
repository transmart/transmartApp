package org.transmartfoundation.status

class StatusInfoController {

    SolrStatusService solrStatusService
    RserveStatusService rserveStatusService

    def index() {

        SolrStatus solrStatus = solrStatusService.getStatus()
        RserveStatus rserveStatus = rserveStatusService.getStatus()

        [solrStatus: solrStatus, rserveStatus: rserveStatus]
    }
}