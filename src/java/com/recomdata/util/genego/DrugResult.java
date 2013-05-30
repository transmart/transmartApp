/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  

/**
 * DrugResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.recomdata.util.genego;

public class DrugResult  implements java.io.Serializable {
    private java.lang.String target;

    private java.lang.String drug_name;

    private java.lang.String drug_type;

    private java.lang.String smiles;

    private java.lang.String iupac_name;

    private java.lang.String casnum;

    public DrugResult() {
    }

    public DrugResult(
           java.lang.String target,
           java.lang.String drug_name,
           java.lang.String drug_type,
           java.lang.String smiles,
           java.lang.String iupac_name,
           java.lang.String casnum) {
           this.target = target;
           this.drug_name = drug_name;
           this.drug_type = drug_type;
           this.smiles = smiles;
           this.iupac_name = iupac_name;
           this.casnum = casnum;
    }


    /**
     * Gets the target value for this DrugResult.
     * 
     * @return target
     */
    public java.lang.String getTarget() {
        return target;
    }


    /**
     * Sets the target value for this DrugResult.
     * 
     * @param target
     */
    public void setTarget(java.lang.String target) {
        this.target = target;
    }


    /**
     * Gets the drug_name value for this DrugResult.
     * 
     * @return drug_name
     */
    public java.lang.String getDrug_name() {
        return drug_name;
    }


    /**
     * Sets the drug_name value for this DrugResult.
     * 
     * @param drug_name
     */
    public void setDrug_name(java.lang.String drug_name) {
        this.drug_name = drug_name;
    }


    /**
     * Gets the drug_type value for this DrugResult.
     * 
     * @return drug_type
     */
    public java.lang.String getDrug_type() {
        return drug_type;
    }


    /**
     * Sets the drug_type value for this DrugResult.
     * 
     * @param drug_type
     */
    public void setDrug_type(java.lang.String drug_type) {
        this.drug_type = drug_type;
    }


    /**
     * Gets the smiles value for this DrugResult.
     * 
     * @return smiles
     */
    public java.lang.String getSmiles() {
        return smiles;
    }


    /**
     * Sets the smiles value for this DrugResult.
     * 
     * @param smiles
     */
    public void setSmiles(java.lang.String smiles) {
        this.smiles = smiles;
    }


    /**
     * Gets the iupac_name value for this DrugResult.
     * 
     * @return iupac_name
     */
    public java.lang.String getIupac_name() {
        return iupac_name;
    }


    /**
     * Sets the iupac_name value for this DrugResult.
     * 
     * @param iupac_name
     */
    public void setIupac_name(java.lang.String iupac_name) {
        this.iupac_name = iupac_name;
    }


    /**
     * Gets the casnum value for this DrugResult.
     * 
     * @return casnum
     */
    public java.lang.String getCasnum() {
        return casnum;
    }


    /**
     * Sets the casnum value for this DrugResult.
     * 
     * @param casnum
     */
    public void setCasnum(java.lang.String casnum) {
        this.casnum = casnum;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DrugResult)) return false;
        DrugResult other = (DrugResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.target==null && other.getTarget()==null) || 
             (this.target!=null &&
              this.target.equals(other.getTarget()))) &&
            ((this.drug_name==null && other.getDrug_name()==null) || 
             (this.drug_name!=null &&
              this.drug_name.equals(other.getDrug_name()))) &&
            ((this.drug_type==null && other.getDrug_type()==null) || 
             (this.drug_type!=null &&
              this.drug_type.equals(other.getDrug_type()))) &&
            ((this.smiles==null && other.getSmiles()==null) || 
             (this.smiles!=null &&
              this.smiles.equals(other.getSmiles()))) &&
            ((this.iupac_name==null && other.getIupac_name()==null) || 
             (this.iupac_name!=null &&
              this.iupac_name.equals(other.getIupac_name()))) &&
            ((this.casnum==null && other.getCasnum()==null) || 
             (this.casnum!=null &&
              this.casnum.equals(other.getCasnum())));
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
        if (getTarget() != null) {
            _hashCode += getTarget().hashCode();
        }
        if (getDrug_name() != null) {
            _hashCode += getDrug_name().hashCode();
        }
        if (getDrug_type() != null) {
            _hashCode += getDrug_type().hashCode();
        }
        if (getSmiles() != null) {
            _hashCode += getSmiles().hashCode();
        }
        if (getIupac_name() != null) {
            _hashCode += getIupac_name().hashCode();
        }
        if (getCasnum() != null) {
            _hashCode += getCasnum().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DrugResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("SOAP/MetaCore", "DrugResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("target");
        elemField.setXmlName(new javax.xml.namespace.QName("", "target"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("drug_name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "drug_name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("drug_type");
        elemField.setXmlName(new javax.xml.namespace.QName("", "drug_type"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("smiles");
        elemField.setXmlName(new javax.xml.namespace.QName("", "smiles"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("iupac_name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "iupac_name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("casnum");
        elemField.setXmlName(new javax.xml.namespace.QName("", "casnum"));
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
          new  org.apache.axis.encoding.ser.BeanSerializer(
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
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
