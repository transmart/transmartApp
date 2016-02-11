package org.transmartfoundation.status

class StatusInfoController {

    SolrStatusService solrStatusService
    RserveStatusService rserveStatusService
    GwavaStatusService gwavaStatusService

    def index() {

        SolrStatus solrStatus = solrStatusService.getStatus()
        RserveStatus rserveStatus = rserveStatusService.getStatus()
        GwavaStatus gwavaStatus = gwavaStatusService.getStatus()

        [solrStatus: solrStatus, rserveStatus: rserveStatus, gwavaStatus: gwavaStatus]
    }
}