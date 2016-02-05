package org.transmartfoundation.status

class StatusInfoController {

    SolrStatusService solrStatusService

    def index() {

        SolrStatus solrStatus = solrStatusService.getStatus()

        [solrStatus: solrStatus]
    }
}