


/**
 * ExpStruct.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.recomdata.util.genego;

public class ExpStruct implements java.io.Serializable {
    private java.lang.String id;

    private double value;

    private double pvalue;

    public ExpStruct() {
    }

    public ExpStruct(
            java.lang.String id,
            double value,
            double pvalue) {
        this.id = id;
        this.value = value;
        this.pvalue = pvalue;
    }


    /**
     * Gets the id value for this ExpStruct.
     *
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this ExpStruct.
     *
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }


    /**
     * Gets the value value for this ExpStruct.
     *
     * @return value
     */
    public double getValue() {
        return value;
    }


    /**
     * Sets the value value for this ExpStruct.
     *
     * @param value
     */
    public void setValue(double value) {
        this.value = value;
    }


    /**
     * Gets the pvalue value for this ExpStruct.
     *
     * @return pvalue
     */
    public double getPvalue() {
        return pvalue;
    }


    /**
     * Sets the pvalue value for this ExpStruct.
     *
     * @param pvalue
     */
    public void setPvalue(double pvalue) {
        this.pvalue = pvalue;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ExpStruct)) return false;
        ExpStruct other = (ExpStruct) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
                ((this.id == null && other.getId() == null) ||
                        (this.id != null &&
                                this.id.equals(other.getId()))) &&
                this.value == other.getValue() &&
                this.pvalue == other.getPvalue();
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
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        _hashCode += new Double(getValue()).hashCode();
        _hashCode += new Double(getPvalue()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(ExpStruct.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("SOAP/MetaCore", "ExpStruct"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("", "id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("value");
        elemField.setXmlName(new javax.xml.namespace.QName("", "value"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pvalue");
        elemField.setXmlName(new javax.xml.namespace.QName("", "pvalue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
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
