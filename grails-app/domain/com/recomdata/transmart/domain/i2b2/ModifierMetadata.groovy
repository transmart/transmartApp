package com.recomdata.transmart.domain.i2b2;

class ModifierMetadata {

    String id
    String valtypeCd
    String stdUnits
    String visitInd

    static mapping = {
        table 'I2B2DEMODATA.MODIFIER_METADATA'
        version false

        id column: 'MODIFIER_CD'
        valtypeCd column: 'VALTYPE_CD'
        stdUnits column: 'STD_UNITS'
        visitInd column: 'VISIT_IND'
    }


}