
package annotation

class AmTagTemplateService {

    boolean transactional = true

    def serviceMethod() {

    }

    def getTemplate(String key) {

        log.info "Searching amTagTemplateAssociation for " + key

        def amTagTemplateAssociation
        def amTagTemplate

        if (key) {
            amTagTemplateAssociation = AmTagTemplateAssociation.findByObjectUid(key)
            log.info "amTagTemplateAssociation = " + amTagTemplateAssociation + " for key = " + key
        } else {
            log.error "Unable to retrieve an AmTagTemplateAssociation with a null key value"
        }

        if (amTagTemplateAssociation) {
            log.info "Searching amTagTemplate"
            amTagTemplate = AmTagTemplate.get(amTagTemplateAssociation.tagTemplateId)
            log.info "amTagTemplate = " + amTagTemplate.toString()
            log.info "amTagTemplate.tagItems = " + amTagTemplate.amTagItems

        } else {
            log.error "AmTagTemplate is null for tag template association = " + key
        }

        return amTagTemplate
    }

}
