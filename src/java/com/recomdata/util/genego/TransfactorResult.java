


/**
 * TransfactorResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.recomdata.util.genego;

public class TransfactorResult implements java.io.Serializable {
    private java.lang.String transfac;

    private java.lang.String target;

    private java.lang.String effect;

    private java.lang.String[] pubmeds;

    public TransfactorResult() {
    }

    public TransfactorResult(
            java.lang.String transfac,
            java.lang.String target,
            java.lang.String effect,
            java.lang.String[] pubmeds) {
        this.transfac = transfac;
        this.target = target;
        this.effect = effect;
        this.pubmeds = pubmeds;
    }


    /**
     * Gets the transfac value for this TransfactorResult.
     *
     * @return transfac
     */
    public java.lang.String getTransfac() {
        return transfac;
    }


    /**
     * Sets the transfac value for this TransfactorResult.
     *
     * @param transfac
     */
    public void setTransfac(java.lang.String transfac) {
        this.transfac = transfac;
    }


    /**
     * Gets the target value for this TransfactorResult.
     *
     * @return target
     */
    public java.lang.String getTarget() {
        return target;
    }


    /**
     * Sets the target value for this TransfactorResult.
     *
     * @param target
     */
    public void setTarget(java.lang.String target) {
        this.target = target;
    }


    /**
     * Gets the effect value for this TransfactorResult.
     *
     * @return effect
     */
    public java.lang.String getEffect() {
        return effect;
    }


    /**
     * Sets the effect value for this TransfactorResult.
     *
     * @param effect
     */
    public void setEffect(java.lang.String effect) {
        this.effect = effect;
    }


    /**
     * Gets the pubmeds value for this TransfactorResult.
     *
     * @return pubmeds
     */
    public java.lang.String[] getPubmeds() {
        return pubmeds;
    }


    /**
     * Sets the pubmeds value for this TransfactorResult.
     *
     * @param pubmeds
     */
    public void setPubmeds(java.lang.String[] pubmeds) {
        this.pubmeds = pubmeds;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TransfactorResult)) return false;
        TransfactorResult other = (TransfactorResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
                ((this.transfac == null && other.getTransfac() == null) ||
                        (this.transfac != null &&
                                this.transfac.equals(other.getTransfac()))) &&
                ((this.target == null && other.getTarget() == null) ||
                        (this.target != null &&
                                this.target.equals(other.getTarget()))) &&
                ((this.effect == null && other.getEffect() == null) ||
                        (this.effect != null &&
                                this.effect.equals(other.getEffect()))) &&
                ((this.pubmeds == null && other.getPubmeds() == null) ||
                        (this.pubmeds != null &&
                                java.util.Arrays.equals(this.pubmeds, other.getPubmeds())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getTransfac() != null) {
            _hashCode += getTransfac().hashCode();
        }
        if (getTarget() != null) {
            _hashCode += getTarget().hashCode();
        }
        if (getEffect() != null) {
            _hashCode += getEffect().hashCode();
        }
        if (getPubmeds() != null) {
            for (int i = 0;
                 i < java.lang.reflect.Array.getLength(getPubmeds());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPubmeds(), i);
                if (obj != null &&
                        !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(TransfactorResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("SOAP/MetaCore", "TransfactorResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("transfac");
        elemField.setXmlName(new javax.xml.namespace.QName("", "transfac"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("target");
        elemField.setXmlName(new javax.xml.namespace.QName("", "target"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("effect");
        elemField.setXmlName(new javax.xml.namespace.QName("", "effect"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pubmeds");
        elemField.setXmlName(new javax.xml.namespace.QName("", "pubmeds"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
            java.lang.String mechType,
            java.lang.Class _javaType,
            javax.xml.namespace.QName _xmlType) {
        return
                new org.apache.axis.encoding.ser.BeanSerializer(
                        _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
            java.lang.String mechType,
            java.lang.Class _javaType,
            javax.xml.namespace.QName _xmlType) {
        return
                new org.apache.axis.encoding.ser.BeanDeserializer(
                        _javaType, _xmlType, typeDesc);
    }

}
