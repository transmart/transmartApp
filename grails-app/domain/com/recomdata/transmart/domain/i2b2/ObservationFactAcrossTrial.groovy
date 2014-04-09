package com.recomdata.transmart.domain.i2b2

import org.apache.commons.lang.builder.EqualsBuilder
import org.apache.commons.lang.builder.HashCodeBuilder

/**
 * Created with IntelliJ IDEA.
 * User: hbangalore
 * Date: 11/18/13
 * Time: 4:30 PM
 * To change this template use File | Settings | File Templates.
 */

class ObservationFactAcrossTrial implements Serializable {

    String       encounterNum
    String       conceptCd
    String       providerId
    Date         startDate
    String       modifierCd
    Long         instanceNum
    String       patientNum
    String       valtypeCd //starting here they're nullable
    String       tvalChar
    BigDecimal   nvalNum
    String       valueflagCd
    BigDecimal   quantityNum
    String       unitsCd
    Date         endDate
    String       locationCd
    String       observationBlob
    BigDecimal   confidenceNum
    Date         updateDate
    Date         downloadDate
    Date         importDate
    String       sourcesystemCd
    BigDecimal   uploadId

    ConceptVisit conceptVisit

    static mapping = {
        table   name: 'observation_fact', schema: 'I2B2DEMODATA'

        version false
        conceptVisit	column: 'CONCEPT_CD',		type: 'string', insertable: false, updateable: false
    }

    static constraints = {
        conceptCd         maxSize:    50
        providerId        maxSize:    50
        modifierCd        maxSize:    100
        valtypeCd         nullable:   true,   maxSize:   50
        tvalChar          nullable:   true
        nvalNum           nullable:   true,   scale:     5
        valueflagCd       nullable:   true,   maxSize:   50
        quantityNum       nullable:   true,   scale:     5
        unitsCd           nullable:   true,   maxSize:   50
        endDate           nullable:   true
        locationCd        nullable:   true,   maxSize:   50
        observationBlob   nullable:   true
        confidenceNum     nullable:   true,   scale:     5
        updateDate        nullable:   true
        downloadDate      nullable:   true
        importDate        nullable:   true
        sourcesystemCd    nullable:   true,   maxSize:   50
        uploadId          nullable:   true
    }

    int hashCode() {
        def builder = new HashCodeBuilder()
        builder.append encounterNum
        builder.append conceptCd
        builder.append providerId
        builder.append startDate
        builder.append modifierCd
        builder.append instanceNum
        builder.toHashCode()
    }

    boolean equals(other) {
        if (other == null) return false
        def builder = new EqualsBuilder()
        builder.append encounterNum, other.encounterNum
        builder.append conceptCd,    other.conceptCd
        builder.append providerId,   other.providerId
        builder.append startDate,    other.startDate
        builder.append modifierCd,   other.modifierCd
        builder.append instanceNum,  other.instanceNum
        builder.isEquals()
    }
}