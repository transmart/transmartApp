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
