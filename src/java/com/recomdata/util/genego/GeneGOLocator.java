


/**
 * GeneGOLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.recomdata.util.genego;

public class GeneGOLocator extends org.apache.axis.client.Service implements GeneGO {

    public GeneGOLocator() {
    }


    public GeneGOLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public GeneGOLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for GeneGOPort
    private java.lang.String GeneGOPort_address = "http://servername:port/api/soap.cgi";

    public java.lang.String getGeneGOPortAddress() {
        return GeneGOPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String GeneGOPortWSDDServiceName = "GeneGOPort";

    public java.lang.String getGeneGOPortWSDDServiceName() {
        return GeneGOPortWSDDServiceName;
    }

    public void setGeneGOPortWSDDServiceName(java.lang.String name) {
        GeneGOPortWSDDServiceName = name;
    }

    public GeneGOPortType getGeneGOPort() throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(GeneGOPort_address);
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getGeneGOPort(endpoint);
    }

    public GeneGOPortType getGeneGOPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            GeneGOBindingStub _stub = new GeneGOBindingStub(portAddress, this);
            _stub.setPortName(getGeneGOPortWSDDServiceName());
            return _stub;
        } catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setGeneGOPortEndpointAddress(java.lang.String address) {
        GeneGOPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (GeneGOPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                GeneGOBindingStub _stub = new GeneGOBindingStub(new java.net.URL(GeneGOPort_address), this);
                _stub.setPortName(getGeneGOPortWSDDServiceName());
                return _stub;
            }
        } catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("GeneGOPort".equals(inputPortName)) {
            return getGeneGOPort();
        } else {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("SOAP/MetaCore", "GeneGO");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("SOAP/MetaCore", "GeneGOPort"));
        }
        return ports.iterator();
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {

        if ("GeneGOPort".equals(portName)) {
            setGeneGOPortEndpointAddress(address);
        } else { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
