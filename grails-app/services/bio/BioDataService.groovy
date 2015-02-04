package bio

import org.transmart.biomart.BioData

class BioDataService {

    boolean transactional = true

    def getBioDataObject(String uid) {
        def bioDataObject
        def bioData = BioData.findByUniqueId(uid)
        log.info "bioData = " + bioData
        if (bioData != null) {
            Class clazz = grailsApplication.getDomainClass().clazz
            log.info "clazz = " + clazz
            bioDataObject = clazz.findByObjectUid(folder.getUniqueId())
            log.info "bioDataObject = " + bioDataObject
        }

        return bioDataObject
    }
}
