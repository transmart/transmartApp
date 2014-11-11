


/**
 * GeneGOPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.recomdata.util.genego;

public interface GeneGOPortType extends java.rmi.Remote {
    public java.lang.String login(java.lang.String login, java.lang.String password) throws java.rmi.RemoteException;

    public int logout(java.lang.String authKey) throws java.rmi.RemoteException;

    public java.lang.String getVersion(java.lang.String authKey) throws java.rmi.RemoteException;

    public SearchResult[] doRegulationSearch(java.lang.String authKey, java.lang.String str) throws java.rmi.RemoteException;

    public JobResult[] getJobs(java.lang.String authKey) throws java.rmi.RemoteException;

    public JobResult getJobStatus(java.lang.String authKey, java.lang.String jobID) throws java.rmi.RemoteException;

    public int deleteJob(java.lang.String authKey, java.lang.String jobID) throws java.rmi.RemoteException;

    public java.lang.String importExp(java.lang.String authKey, java.lang.String idType, java.lang.String expName, ExpStruct[] expData, int folderID) throws java.rmi.RemoteException;

    public java.lang.String getGenePageURL(java.lang.String authKey, java.lang.String id, java.lang.String idType) throws java.rmi.RemoteException;

    public MapResult[] getMapsByObject(java.lang.String authKey, java.lang.String id, java.lang.String idType) throws java.rmi.RemoteException;

    public MapResult[] getMapsByObjects(java.lang.String authKey, java.lang.String[] ids, java.lang.String idType) throws java.rmi.RemoteException;

    public java.lang.String getNetworkPageURL(java.lang.String authKey, java.lang.String id, java.lang.String idType) throws java.rmi.RemoteException;

    public java.lang.String getMainPageURL(java.lang.String authKey) throws java.rmi.RemoteException;

    public java.lang.String getPageURL(java.lang.String authKey, java.lang.String url) throws java.rmi.RemoteException;

    public java.lang.String getJobListPageURL(java.lang.String authKey) throws java.rmi.RemoteException;

    public LicenseResult[] getGrabbedLicenses(java.lang.String authKey) throws java.rmi.RemoteException;

    public java.lang.String getBaseURL(java.lang.String authKey) throws java.rmi.RemoteException;

    public int deactivateAllExps(java.lang.String authKey) throws java.rmi.RemoteException;

    public int activateExpsByJob(java.lang.String authKey, java.lang.String jobID) throws java.rmi.RemoteException;

    public int createExpFolder(java.lang.String authKey, java.lang.String folderName, int parentFolderID) throws java.rmi.RemoteException;

    public StoredNetworksListResult[] getStoredNetworksList(java.lang.String authKey) throws java.rmi.RemoteException;

    public java.lang.String getStoredNetworkXML(java.lang.String authKey, java.lang.String id) throws java.rmi.RemoteException;

    public GeneGoProcessesResult[] getGenegoProcessesByGenes(java.lang.String authKey, java.lang.String[] ids, java.lang.String idType) throws java.rmi.RemoteException;

    public InteractionResult[] getOutgoingRelationshipsByGenes(java.lang.String authKey, java.lang.String[] ids, java.lang.String idType) throws java.rmi.RemoteException;

    public InteractionResult[] getIncomingRelationshipsByGenes(java.lang.String authKey, java.lang.String[] ids, java.lang.String idType) throws java.rmi.RemoteException;

    public TransfactorResult[] getTransfactorsForGenes(java.lang.String authKey, java.lang.String[] ids, java.lang.String idType) throws java.rmi.RemoteException;

    public DrugResult[] getTherapeuticDrugsForGenes(java.lang.String authKey, java.lang.String[] ids, java.lang.String idType) throws java.rmi.RemoteException;

    public CompoundResult[] getSimilarEndogenousCompounds(java.lang.String authKey, java.lang.String geneId, java.lang.String geneIdType, java.lang.String chemId, java.lang.String chemIdType) throws java.rmi.RemoteException;
}
