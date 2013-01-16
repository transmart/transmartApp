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
 * $Id: JubilantResNetService.groovy 10098 2011-10-19 18:39:32Z mmcduffie $
 */
import com.recomdata.util.ariadne.Attr
import com.recomdata.util.ariadne.Batch
import com.recomdata.util.ariadne.Control
import com.recomdata.util.ariadne.Controls
import com.recomdata.util.ariadne.Link
import com.recomdata.util.ariadne.Node
import com.recomdata.util.ariadne.Nodes
import com.recomdata.util.ariadne.Properties
import com.recomdata.util.ariadne.Resnet
import groovy.xml.MarkupBuilder
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.transmart.SearchResult;

/**
 * ResNetService that will provide an .rnef file for Jubilant data
 * 
 * @author $Author: mmcduffie $
 * @version $Revision: 10098 $
 */
class JubilantResNetService {       
    boolean transactional = false  // No need for this to be part of a transaction
    
    def searchFilter			// Passed from the JubilantController   
    def misses = [] as Set		// Set that contains the proteins that could not be found
    def literatureQueryService	// Make our own calls to query for proteins and interactions    
    
    def createResNet() {               
        misses.clear()			// Clear the misses with each run
                                    
        def sResult = new SearchResult()
        def reset = true
        
        Batch batch = null;
	
        if (literatureQueryService.litJubOncIntCount(searchFilter) > 0) {                		
            sResult.result = literatureQueryService.litJubOncIntData(searchFilter, null)            
            batch = new Batch()
            batch.getResnet().add(processInteractionData(sResult, "Onc"))            
        }
        
        if (literatureQueryService.litJubAsthmaIntCount(searchFilter) > 0)	{               
            sResult.result = literatureQueryService.litJubAsthmaIntData(searchFilter, null)            
            if (batch == null)	{
                batch = new Batch()
            }	
            batch.getResnet().add(processInteractionData(sResult, "Asthma"))
        }
            
        if (literatureQueryService.litJubOncAltCount(searchFilter) > 0)	{            
            sResult.result = literatureQueryService.litJubOncAltData(searchFilter, null)
            if (batch == null)	{
                batch = new Batch()
            }
            batch.getResnet().add(processAlterationData(sResult, "Onc"))
        }
         
        if (literatureQueryService.litJubAsthmaAltCount(searchFilter) > 0)	{
            sResult.result = literatureQueryService.litJubAsthmaAltData(searchFilter, null)
            if (batch == null)	{
                batch = new Batch()
            }
            batch.getResnet().add(processAlterationData(sResult, "Asthma"))
        }

        if (literatureQueryService.litJubOncInhCount(searchFilter) > 0)	{            
            sResult.result = literatureQueryService.litJubOncInhData(searchFilter, null)
            if (batch == null)	{
                batch = new Batch()
            }
            batch.getResnet().add(processInhibitorData(sResult, "Onc"))
        }        
        return batch;
    }    
 
    def processInteractionData(SearchResult searchResults, String domain)	{
        def intMisses = [] as Set  // Interaction misses
        def nodeMap = [:]     	  // Map of nodes where we have already found the URN
        def refCountMap = [:]	  // Map of ref counts for the index attribute
        int refCount = 0
        def random = new Random()
        def RLIMIT = 10000
        
        Resnet resnet = new Resnet()
        resnet.setName(searchFilter.searchText + "-Int-" + domain + "-" + new Date())
        resnet.setType("Pathway")        

        Properties pResNet = new Properties()
        
        Attr att = new Attr()
        att.setName("Source")
        att.setValue("Jubilant")
        pResNet.getAttr().add(att)

        Nodes nodes = new Nodes()
        Controls controls = new Controls()
        
        for (searchResult in searchResults.result) {
            if (searchResult.sourceComponent != null  && searchResult.targetComponent != null)	{
                String source = searchResult.sourceComponent.trim()
                String target = searchResult.targetComponent.trim()
                if (intMisses.contains(source) || intMisses.contains(target))	{
                    log.info("Source: " + source + " or target: " + target + " is missing...")
                    continue;
                }
                
                Node sourceNode = null                
                String srcURN = ""
                
                if (nodeMap.containsKey(source))	{
                    sourceNode = nodeMap.get(source)
                } else	{
                    sourceNode = new Node()
                    sourceNode.setLocalId("N" + random.nextInt(RLIMIT))
			        srcURN = literatureQueryService.findGeneURN(source)
			        if (srcURN == null)	{        			    
			            log.info("Missed Source: " + source) 
				        intMisses.add(source)
				        continue;
				    }				    
                    sourceNode.setUrn(srcURN)					
					
					att = new Attr()
					att.setName("NodeType")
					att.setValue("Protein")
					sourceNode.getAttr().add(att)
					att = new Attr()
					att.setName("Name")
					att.setValue(source)
					sourceNode.getAttr().add(att)
					nodeMap.put(source, sourceNode)
					nodes.getNode().add(sourceNode)
                }
                
                Node targetNode = null                
                String tgtURN = ""
                
                if (nodeMap.containsKey(target))	{
                    targetNode = nodeMap.get(target)
                } else	{
                    targetNode = new Node()
                    targetNode.setLocalId("N" + random.nextInt(RLIMIT))
				    tgtURN = literatureQueryService.findGeneURN(target)
				    if (tgtURN == null)	{        			    
				        log.info("Missed Target: " + target) 
				        intMisses.add(target)
				        continue;
				    }				   
					targetNode.setUrn(tgtURN)
					
					att = new Attr()
					att.setName("NodeType")
					att.setValue("Protein")
					targetNode.getAttr().add(att)
					att = new Attr()
					att.setName("Name")
					att.setValue(target)
					targetNode.getAttr().add(att)
            		nodeMap.put(target, targetNode)
					nodes.getNode().add(targetNode)
                }
                
                boolean isBinding = false
                
                Control control = new Control()
                control.setLocalId("L" + + random.nextInt(RLIMIT))
                
                if (searchResult.mechanism != null)	{
    				if ("Complex".compareToIgnoreCase(searchResult.mechanism) == 0 ||
    				        "Interaction".compareToIgnoreCase(searchResult.mechanism) == 0 ||
    				        "Dissociation".compareToIgnoreCase(searchResult.mechanism) == 0 ||
    				        "Binding".compareToIgnoreCase(searchResult.mechanism) == 0 ||
    				        "Association".compareToIgnoreCase(searchResult.mechanism) == 0 ||
    				        "Heterodimerization".compareToIgnoreCase(searchResult.mechanism) == 0 ||
    				        "Recruitment".compareToIgnoreCase(searchResult.mechanism) == 0)	{
    				    createAtt(control, "ControlType", "Binding")    	                
    	                isBinding = true
    				} else if ("Degradation".compareToIgnoreCase(searchResult.mechanism) == 0)   {
    				    createAtt(control, "ControlType", "Expression")
    				    createAtt(control, "Effect", "negative")
    				    createAtt(control, "Mechanism", "Degradation")    				    
    				} else if ("Accumulation".compareToIgnoreCase(searchResult.mechanism) == 0)  {
    				    createAtt(control, "ControlType", "Expression")
    				    createAtt(control, "Effect", "positive")
    				    createAtt(control, "Mechanism", "Accumulation")
    				} else if ("Stabilization".compareToIgnoreCase(searchResult.mechanism) == 0)  {
    				    createAtt(control, "ControlType", "Expression")
    				    createAtt(control, "Effect", "positive")
    				    createAtt(control, "Mechanism", "Stabilization")
    				} else if ("Downregulation".compareToIgnoreCase(searchResult.mechanism) == 0 ||
    				           "Inhibition".compareToIgnoreCase(searchResult.mechanism)	== 0)	{
    				    createAtt(control, "ControlType", "Regulation")
    				    createAtt(control, "Effect", "negative")
    				} else if ("Activation".compareToIgnoreCase(searchResult.mechanism) == 0 ||
    				           "Assembly".compareToIgnoreCase(searchResult.mechanism) == 0 ||
    				           "Upregulation".compareToIgnoreCase(searchResult.mechanism) == 0)	{
    				    createAtt(control, "ControlType", "Regulation")
    				    createAtt(control, "Effect", "positive")
    				} else if ("Cleavage".compareToIgnoreCase(searchResult.mechanism) == 0)  {
    				    createAtt(control, "ControlType", "Expression")
    				    createAtt(control, "Effect", "negative")
    				    createAtt(control, "Mechanism", "Cleavage")
    				} else if ("Synthesis".compareToIgnoreCase(searchResult.mechanism) == 0)  {
    				    createAtt(control, "ControlType", "MolSynthesis")
    				} else if ("Translocation".compareToIgnoreCase(searchResult.mechanism) == 0)  {
    				    createAtt(control, "ControlType", "MolTransport")
    				} else if ("Release".compareToIgnoreCase(searchResult.mechanism) == 0)	{
    				    createAtt(control, "ControlType", "MolTransport")
    				    createAtt(control, "Mechanism", "Release")
    				} else if ("Secretion".compareToIgnoreCase(searchResult.mechanism) == 0)	{
    				    createAtt(control, "ControlType", "MolTransport")
    				    createAtt(control, "Mechanism", "Secretion")
    				} else if ("Splicing".compareToIgnoreCase(searchResult.mechanism) == 0)	{
    				    createAtt(control, "ControlType", "Expression")
    				    createAtt(control, "Mechanism", "Splicing")
    				} else  {
    				    log.warn("Unknown interaction mechanism found: " + searchResult.mechanism)
       				    createAtt(control, "ControlType", searchResult.mechanism)    				}
				} else	{
				    log.warn("Interaction mechanism is null, mapping relationship type to unknown")
				    createAtt(control, "ControlType", "Unknown")     				
				}
                
                Link l = new Link()
                if (isBinding)	{
                    l.setType("in-out")
                    l.setRef(sourceNode.getLocalId())
                    control.getLink().add(l)
                    
                    l = new Link()
                    l.setType("in-out")
                    l.setRef(targetNode.getLocalId())
                    control.getLink().add(l)
                } else	{
                    l.setType("in")
                    l.setRef(sourceNode.getLocalId())
                    control.getLink().add(l)
                    
                    l = new Link()
                    l.setType("out")
                    l.setRef(targetNode.getLocalId())
                    control.getLink().add(l)
                }
                
                createAtt(control, "X-Mode", searchResult.interactionMode)
                createAtt(control, "X-Region", searchResult.region)
                createAtt(control, "X-Regulation", searchResult.regulation)
                createAtt(control, "X-Effect", searchResult.effect)
                createAtt(control, "X-Technique", searchResult.techniques)
                createAtt(control, "X-Localization", searchResult.localization)

                if (!refCountMap.containsKey(searchResult.reference.referenceId))	{
                    refCount = 1
                    refCountMap.put(searchResult.reference.referenceId, new Integer(refCount))
                } else	{                       
                    refCount = (new Integer(refCountMap.get(searchResult.reference.referenceId))).intValue()
                    refCount++
                    refCountMap.put(searchResult.reference.referenceId, new Integer(refCount))
                }
                createAtt(control, "mref", searchResult.reference.referenceId, refCount)
                createAtt(control, "msrc", searchResult.reference.referenceTitle, refCount)
                createAtt(control, "Disease", searchResult.reference.disease)
                createAtt(control, "X-Disease Site", searchResult.reference.diseaseSite)
                createAtt(control, "X-Disease Types", searchResult.reference.diseaseTypes)
                createAtt(control, "X-Disease Stage", searchResult.reference.diseaseStage)
                createAtt(control, "X-Disease Description", searchResult.reference.diseaseDescription)
                createAtt(control, "X-Physiology", searchResult.reference.physiology)
                createAtt(control, "X-Clinical Statistics", searchResult.reference.statClinical)
                createAtt(control, "X-Clinical Correlation", searchResult.reference.statClinicalCorrelation)
                createAtt(control, "X-Statistical Tests", searchResult.reference.statTests)
                createAtt(control, "X-Coefficient", searchResult.reference.statCoefficient)
                createAtt(control, "X-P Value", searchResult.reference.statPValue)
                createAtt(control, "X-Statistical Description", searchResult.reference.statDescription)
                
				if (searchResult.inVivoModel != null)	{
				    createAtt(control, "X-In Vivo Model Type", searchResult.inVivoModel.modelType)
				    createAtt(control, "X-In Vivo Description", searchResult.inVivoModel.description)
				    createAtt(control, "X-In Vivo Stimulation", searchResult.inVivoModel.stimulation)
				    createAtt(control, "X-In Vivo Control Challenge", searchResult.inVivoModel.controlChallenge)
				    createAtt(control, "X-In Vivo Challenge", searchResult.inVivoModel.challenge)
				    createAtt(control, "X-In Vivo Sentization", searchResult.inVivoModel.sentization)
				    createAtt(control, "X-In Vivo Zygosity", searchResult.inVivoModel.zygosity)
				    createAtt(control, "X-In Vivo Experimental Model", searchResult.inVivoModel.experimentalModel)
				    createAtt(control, "X-In Vivo Animal Wild Type", searchResult.inVivoModel.animalWildType)
				    createAtt(control, "X-In Vivo Tissue", searchResult.inVivoModel.tissue)
				    createAtt(control, "X-In Vivo Cell Type", searchResult.inVivoModel.cellType)
				    
				    if (searchResult.inVivoModel.cellLine != null)	{
				        String fullCellLine = searchResult.inVivoModel.cellLine
				        def cellLineArray = fullCellLine.split(",")
				        int count = 1
				        for (cellLine in cellLineArray)	{
				            createAtt(control, "X-In Vivo Cell Line " + count, cellLine)
				            count++
				        }
				    } 
				    createAtt(control, "X-In Vivo Body Substance", searchResult.inVivoModel.bodySubstance)
				}
                				
				if (searchResult.inVitroModel != null)	{
				    createAtt(control, "X-In Vitro Model Type", searchResult.inVitroModel.modelType)
				    createAtt(control, "X-In Vitro Description", searchResult.inVitroModel.description)
					createAtt(control, "X-In Vitro Stimulation", searchResult.inVitroModel.stimulation)
					createAtt(control, "X-In Vitro Control Challenge", searchResult.inVitroModel.controlChallenge)
					createAtt(control, "X-In Vitro Challenge", searchResult.inVitroModel.challenge)
					createAtt(control, "X-In Vitro Sentization", searchResult.inVitroModel.sentization)
					createAtt(control, "X-In Vitro Zygosity", searchResult.inVitroModel.zygosity)
					createAtt(control, "X-In Vitro Experimental Model", searchResult.inVitroModel.experimentalModel)
					createAtt(control, "X-In Vitro Animal Wild Type", searchResult.inVitroModel.animalWildType)
					createAtt(control, "X-In Vitro Tissue", searchResult.inVitroModel.tissue)
					createAtt(control, "X-In Vitro Cell Type", searchResult.inVitroModel.cellType)
					if (searchResult.inVitroModel.cellLine != null)	{
					    String fullCellLine = searchResult.inVitroModel.cellLine
				        def cellLineArray = fullCellLine.split(",")
				        int count = 1
				        for (cellLine in cellLineArray)	{
				            createAtt(control, "X-In Vitro Cell Line " + count, cellLine)
				            count++
				        }
				    }
				    createAtt(control, "X-In Vitro Body Substance", searchResult.inVitroModel.bodySubstance)
				}					
                controls.getControl().add(control)                                                                   
            }
        }
        
        misses.addAll(intMisses)
        
        att = new Attr()
        att.setName("Notes")
        att.setValue(intMisses.toString())
        pResNet.getAttr().add(att)
                        
        resnet.setProperties(pResNet)
        resnet.setNodes(nodes)
        resnet.setControls(controls)

        return resnet
   }
        
   def processAlterationData(SearchResult searchResults, String domain)	{        
       def altMisses = [] as Set  // Alteration misses
       def nodeMap = [:]     	  // Map of nodes where we have already found the URN
       def refCountMap = [:]	  // Map of ref counts for the index attribute
       int refCount = 0				  
       def random = new Random()
       def RLIMIT = 10000
        
       Resnet resnet = new Resnet()
       resnet.setName(searchFilter.searchText + "-Alt-" + domain + "-" + new Date())
       resnet.setType("Pathway")        

       Properties pResNet = new Properties()
       
       Attr att = new Attr()
       att.setName("Source")
       att.setValue("Jubilant")
       pResNet.getAttr().add(att)

       Nodes nodes = new Nodes()
       Controls controls = new Controls()
        
       for (searchResult in searchResults.result) {           
           if (searchResult.reference != null)	{                
               if (searchResult.reference.component != null  && searchResult.reference.disease != null)	{                   
                   String component = searchResult.reference.component.trim()
                   String disease = searchResult.reference.disease.trim()
                   if (altMisses.contains(component) || altMisses.contains(disease))	{
                       log.info("Component: " + component + " or disease: " + disease + " is missing...")
                       continue; 
                   }
                   
                   Node componentNode = null                
                   String cmpURN = ""
                   
                   if (nodeMap.containsKey(component))	{
                       componentNode = nodeMap.get(component) 
                   } else	{
                       componentNode = new Node()
                       componentNode.setLocalId("N" + random.nextInt(RLIMIT))
                       cmpURN = literatureQueryService.findGeneURN(component)
   				       if (cmpURN == null)	{        			    
   				           log.info("Missed Component: " + component) 
   				           altMisses.add(component)
   				           continue;
   				       }
                       componentNode.setUrn(cmpURN)                       
                       
                       att = new Attr()
                       att.setName("NodeType")
                       att.setValue("Protein")
                       componentNode.getAttr().add(att)
                       att = new Attr()
                       att.setName("Name")
                       att.setValue(component)
                       componentNode.getAttr().add(att)
                       nodeMap.put(component, componentNode)
                       nodes.getNode().add(componentNode)                       
                   }
                                    
                   Node diseaseNode = null                
                   String disURN = null
                   
                   if (nodeMap.containsKey(disease))	{
                       diseaseNode = nodeMap.get(disease)
                   } else	{
                       diseaseNode = new Node()
                       diseaseNode.setLocalId("N" + random.nextInt(RLIMIT))
                       if (searchResult.reference.diseaseMesh != null)	{
                           disURN = literatureQueryService.findDiseaseURN(searchResult.reference.diseaseMesh.trim())
                       } else	{
                           log.info("DiseaseMesh is null, trying disease...")
                       }
                       if (disURN == null)	{                       
                           disURN = literatureQueryService.findDiseaseURN(disease)                    
                           if (disURN == null)	{
                               log.info("Missed Disease: " + disease) 
                               altMisses.add(disease)
                               continue
                           }
                       }
                       diseaseNode.setUrn(disURN)
                       
                       att = new Attr()
   					   att.setName("NodeType")
   					   att.setValue("Disease")
   					   diseaseNode.getAttr().add(att)
   					   att = new Attr()
   					   att.setName("Name")
   					   att.setValue(disease)
   					   diseaseNode.getAttr().add(att)
   					   nodeMap.put(disease, diseaseNode)
   					   nodes.getNode().add(diseaseNode)      					
                   }
                   
                   Control control = new Control()
                   control.setLocalId("L" + + random.nextInt(RLIMIT))
                   
                   if (searchResult.alterationType != null)	{
                       createAtt(control, "ControlType", "StateChange")
   				       if ("Expression".compareToIgnoreCase(searchResult.alterationType) == 0 ||
   				               "Epigenetic Event".compareToIgnoreCase(searchResult.alterationType) == 0 ||
   				               "Mutation".compareToIgnoreCase(searchResult.alterationType) == 0)	{
   				           createAtt(control, "Mechanism", searchResult.alterationType)   				           
   				       } else if ("Genomic Level Change".compareToIgnoreCase(searchResult.alterationType) == 0)	{
   				           createAtt(control, "Mechanism", searchResult.alterationType)
   				           createAtt(control, "Effect", "unknown")   				                   
   				       } else if ("Gene Amplification".compareToIgnoreCase(searchResult.alterationType) == 0)	{
   				           createAtt(control, "Mechanism", searchResult.alterationType)
   				           createAtt(control, "Effect", "positive")   				        				        
   				       } else if ("LOH".compareToIgnoreCase(searchResult.alterationType) == 0) {
   				           createAtt(control, "Mechanism", "Loss of heterozygosity")    				        
   				       } else if ("PTM".compareToIgnoreCase(searchResult.alterationType) == 0)	{
   				           createAtt(control, "Mechanism", "Posttranslational modification")
   				       } else	{
   				           log.warn("Unknown alteration type: " + searchResult.alterationType)
   				           createAtt(control, "ControlType", searchResult.alterationType)
   				       }
   				   } else	{
   				       log.warn("Alteration type is null, mapping relationship type to unknown")
   				       createAtt(control, "ControlType", "Unknown")   				       
   				   }
                   
                   Link l = new Link()
                   l.setType("out")
                   l.setRef(componentNode.getLocalId())
                   control.getLink().add(l)
                   
                   l = new Link()
                   l.setType("in")
                   l.setRef(diseaseNode.getLocalId())
                   control.getLink().add(l)
             
                   createAtt(control, "X-Control", searchResult.control)
                   createAtt(control, "X-Effect", searchResult.effect)
                   createAtt(control, "X-Description", searchResult.description)
                   createAtt(control, "X-Techniques", searchResult.techniques)
                   createAtt(control, "X-PatientsPercent", searchResult.patientsPercent)
                   createAtt(control, "X-PatientsNumber", searchResult.patientsNumber)
                   createAtt(control, "X-PopNumber", searchResult.popNumber)
                   createAtt(control, "X-PopIncCriteria", searchResult.popInclusionCriteria)
                   createAtt(control, "X-PopExcCriteria", searchResult.popExclusionCriteria)
                   createAtt(control, "X-PopDescription", searchResult.popDescription)
                   createAtt(control, "X-PopType", searchResult.popType)
                   createAtt(control, "X-PopValue", searchResult.popValue)
                   createAtt(control, "X-PopPhase", searchResult.popPhase)
                   createAtt(control, "X-PopStatus", searchResult.popStatus)
                   createAtt(control, "X-PopExpModel", searchResult.popExperimentalModel)
                   createAtt(control, "X-PopTissue", searchResult.popTissue)
                   createAtt(control, "X-PopBodySubstance", searchResult.popBodySubstance)
                   createAtt(control, "X-PopLocalization", searchResult.popLocalization)
                   createAtt(control, "X-PopCellType", searchResult.popCellType)
                   createAtt(control, "X-ClinSubmucosaMarkerType", searchResult.clinSubmucosaMarkerType)
                   createAtt(control, "X-ClinSubmucosaUnit", searchResult.clinSubmucosaUnit)
                   createAtt(control, "X-ClinSubmucosaValue", searchResult.clinSubmucosaValue)
                   createAtt(control, "X-ClinAsmMarkerType", searchResult.clinAsmMarkerType)
                   createAtt(control, "X-ClinAsmUnit", searchResult.clinAsmUnit)
                   createAtt(control, "X-ClinAsmValue", searchResult.clinAsmValue)
                   createAtt(control, "X-ClinCellularSource", searchResult.clinCellularSource)
                   createAtt(control, "X-ClinCellularType", searchResult.clinCellularType)
                   createAtt(control, "X-ClinCellularCount", searchResult.clinCellularCount)
                   createAtt(control, "X-ClinPriorMedPercent", searchResult.clinPriorMedPercent)
                   createAtt(control, "X-ClinPriorMedDose", searchResult.clinPriorMedDose)
                   createAtt(control, "X-ClinPriorMedName", searchResult.clinPriorMedName)
                   createAtt(control, "X-ClinBaselineVariable", searchResult.clinBaselineVariable)
                   createAtt(control, "X-ClinBaselinePercent", searchResult.clinBaselinePercent)
                   createAtt(control, "X-ClinBaselineValue", searchResult.clinBaselineValue)
                   createAtt(control, "X-ClinSmoker", searchResult.clinSmoker)
                   createAtt(control, "X-ClinAtopy", searchResult.clinAtopy)
                   createAtt(control, "X-ControlExpPercent", searchResult.controlExpPercent)
                   createAtt(control, "X-ControlExpNumber", searchResult.controlExpNumber)
                   createAtt(control, "X-ControlExpValue", searchResult.controlExpValue)
                   createAtt(control, "X-ControlExpSd", searchResult.controlExpSd)
                   createAtt(control, "X-ControlExpUnit", searchResult.controlExpUnit)
                   createAtt(control, "X-OverExpPercent", searchResult.overExpPercent)
                   createAtt(control, "X-OverExpNumber", searchResult.overExpNumber)
                   createAtt(control, "X-OverExpValue", searchResult.overExpValue)
                   createAtt(control, "X-OverExpSd", searchResult.overExpSd)
                   createAtt(control, "X-OverExpUnit", searchResult.overExpUnit)
                   createAtt(control, "X-LossExpPercent", searchResult.lossExpPercent)
                   createAtt(control, "X-LossExpNumber", searchResult.lossExpNumber)
                   createAtt(control, "X-LossExpValue", searchResult.lossExpValue)
                   createAtt(control, "X-LossExpSd", searchResult.lossExpSd)
                   createAtt(control, "X-LossExpUnit", searchResult.lossExpUnit)
                   createAtt(control, "X-TotalExpPercent", searchResult.totalExpPercent)
                   createAtt(control, "X-TotalExpNumber", searchResult.totalExpNumber)
                   createAtt(control, "X-TotalExpValue", searchResult.totalExpValue)
                   createAtt(control, "X-TotalExpSd", searchResult.totalExpSd)
                   createAtt(control, "X-TotalExpUnit", searchResult.totalExpUnit)                	
                   createAtt(control, "X-GlcControlPercent", searchResult.glcControlPercent)
                   createAtt(control, "X-GlcMolecularChange", searchResult.glcMolecularChange)
                   createAtt(control, "X-GlcType", searchResult.glcType)
                   createAtt(control, "X-GlcPercent", searchResult.glcPercent)
                   createAtt(control, "X-GlcNumber", searchResult.glcNumber)
                   createAtt(control, "X-PtmRegion", searchResult.ptmRegion)
                   createAtt(control, "X-PtmType", searchResult.ptmType)
                   createAtt(control, "X-PtmChange", searchResult.ptmChange)
                   createAtt(control, "X-LohLoci", searchResult.lohLoci)
                   createAtt(control, "X-MutationType", searchResult.mutationType)
                   createAtt(control, "X-MutationChange", searchResult.mutationChange)
                   createAtt(control, "X-MutationSites", searchResult.mutationSites)
                   createAtt(control, "X-EpigeneticRegion", searchResult.epigeneticRegion)
                   createAtt(control, "X-EpigeneticType", searchResult.epigeneticType)
                    
                   if (!refCountMap.containsKey(searchResult.reference.referenceId))	{
                       refCount = 1
                       refCountMap.put(searchResult.reference.referenceId, new Integer(refCount))
                   } else	{                       
                       refCount = (new Integer(refCountMap.get(searchResult.reference.referenceId))).intValue()
                       refCount++
                       refCountMap.put(searchResult.reference.referenceId, new Integer(refCount))
                   }
                   createAtt(control, "mref", searchResult.reference.referenceId, refCount)
                   createAtt(control, "msrc", searchResult.reference.referenceTitle, refCount)
                   createAtt(control, "Disease", searchResult.reference.disease)
                   createAtt(control, "X-Disease Site", searchResult.reference.diseaseSite)
                   createAtt(control, "X-Disease Types", searchResult.reference.diseaseTypes)
                   createAtt(control, "X-Disease Stage", searchResult.reference.diseaseStage)
                   createAtt(control, "X-Disease Description", searchResult.reference.diseaseDescription)                        
                   createAtt(control, "X-Physiology", searchResult.reference.physiology)
                   createAtt(control, "X-Clinical Statistics", searchResult.reference.statClinical)
                   createAtt(control, "X-Clinical Correlation", searchResult.reference.statClinicalCorrelation)
                   createAtt(control, "X-Statistical Tests", searchResult.reference.statTests)
                   createAtt(control, "X-Coefficient", searchResult.reference.statCoefficient)
                   createAtt(control, "X-P Value", searchResult.reference.statPValue)
                   createAtt(control, "X-Statistical Description", searchResult.reference.statDescription)
                   
                   if (searchResult.inVivoModel != null)	{
                       createAtt(control, "X-In Vivo Model Type", searchResult.inVivoModel.modelType)
                       createAtt(control, "X-In Vivo Description", searchResult.inVivoModel.description)
                       createAtt(control, "X-In Vivo Stimulation", searchResult.inVivoModel.stimulation)
                       createAtt(control, "X-In Vivo Control Challenge", searchResult.inVivoModel.controlChallenge)
                       createAtt(control, "X-In Vivo Challenge", searchResult.inVivoModel.challenge)
                       createAtt(control, "X-In Vivo Sentization", searchResult.inVivoModel.sentization)
                       createAtt(control, "X-In Vivo Zygosity", searchResult.inVivoModel.zygosity)
                       createAtt(control, "X-In Vivo Experimental Model", searchResult.inVivoModel.experimentalModel)
                       createAtt(control, "X-In Vivo Animal Wild Type", searchResult.inVivoModel.animalWildType)
                       createAtt(control, "X-In Vivo Tissue", searchResult.inVivoModel.tissue)
                       createAtt(control, "X-In Vivo Cell Type", searchResult.inVivoModel.cellType)
				    
                       if (searchResult.inVivoModel.cellLine != null)	{
                           String fullCellLine = searchResult.inVivoModel.cellLine
                           def cellLineArray = fullCellLine.split(",")
                           int count = 1
                           for (cellLine in cellLineArray)	{
                               createAtt(control, "X-In Vivo Cell Line " + count, cellLine)
                               count++
                           }
                       } 
                       createAtt(control, "X-In Vivo Body Substance", searchResult.inVivoModel.bodySubstance)
                   }
                				
                   if (searchResult.inVitroModel != null)	{
                       createAtt(control, "X-In Vitro Model Type", searchResult.inVitroModel.modelType)
                       createAtt(control, "X-In Vitro Description", searchResult.inVitroModel.description)
                       createAtt(control, "X-In Vitro Stimulation", searchResult.inVitroModel.stimulation)
                       createAtt(control, "X-In Vitro Control Challenge", searchResult.inVitroModel.controlChallenge)
                       createAtt(control, "X-In Vitro Challenge", searchResult.inVitroModel.challenge)
                       createAtt(control, "X-In Vitro Sentization", searchResult.inVitroModel.sentization)
                       createAtt(control, "X-In Vitro Zygosity", searchResult.inVitroModel.zygosity)
                       createAtt(control, "X-In Vitro Experimental Model", searchResult.inVitroModel.experimentalModel)
                       createAtt(control, "X-In Vitro Animal Wild Type", searchResult.inVitroModel.animalWildType)
                       createAtt(control, "X-In Vitro Tissue", searchResult.inVitroModel.tissue)
                       createAtt(control, "X-In Vitro Cell Type", searchResult.inVitroModel.cellType)
                       if (searchResult.inVitroModel.cellLine != null)	{
                           String fullCellLine = searchResult.inVitroModel.cellLine
                           def cellLineArray = fullCellLine.split(",")
                           int count = 1
                           for (cellLine in cellLineArray)	{
                               createAtt(control, "X-In Vitro Cell Line " + count, cellLine)
                               count++
                           }
                       }
                       createAtt(control, "X-In Vitro Body Substance", searchResult.inVitroModel.bodySubstance)
                   }
                   controls.getControl().add(control)           
               }
           }
       }
       
       misses.addAll(altMisses)
       
       att = new Attr()
       att.setName("Notes")
       att.setValue(altMisses.toString())
       pResNet.getAttr().add(att)
                       
       resnet.setProperties(pResNet)
       resnet.setNodes(nodes)
       resnet.setControls(controls)

       return resnet
   }
   
   def processInhibitorData(SearchResult searchResults, String domain)	{
       def inhMisses = [] as Set  // Inihibitor misses
       def nodeMap = [:]     	  // Map of nodes where we have already found the URN
       def refCountMap = [:]	  // Map of ref counts for the index attribute
       int refCount = 0
       def random = new Random()
       def RLIMIT = 10000
       
       Resnet resnet = new Resnet()
       resnet.setName(searchFilter.searchText + "-Inh-" + domain + "-" + new Date())
       resnet.setType("Pathway")        

       Properties pResNet = new Properties()
       
       Attr att = new Attr()
       att.setName("Source")
       att.setValue("Jubilant")
       pResNet.getAttr().add(att)

       Nodes nodes = new Nodes()
       Controls controls = new Controls()
       
       for (searchResult in searchResults.result) {           
           if (searchResult.reference != null)	{                
               if (searchResult.inhibitor != null  && searchResult.reference.component != null)	{                   
                   String inhibitor = searchResult.inhibitor.trim()
                   String component = searchResult.reference.component.trim()
                   if (inhMisses.contains(inhibitor) || inhMisses.contains(component))	{
                       log.info("Inhibitor: " + inhibitor + " or component: " + component + " is missing...")
                       continue; 
                   }
                   
                   Node inhibitorNode = null                
                   String inhURN = null
                   
                   if (nodeMap.containsKey(inhibitor))	{
                       inhibitorNode = nodeMap.get(inhibitor)
                   } else	{
                       inhibitorNode = new Node()
                       inhibitorNode.setLocalId("N" + random.nextInt(RLIMIT))
                       inhURN = literatureQueryService.findSmallMolURN(inhibitor)
   				       if (inhURN == null)	{        			    
   				           log.info("Missed Inhibitor: " + inhibitor) 
   				           inhMisses.add(inhibitor)
   				           continue;
   				       }                       
                       inhibitorNode.setUrn(inhURN)
                       
                       att = new Attr()
   					   att.setName("NodeType")
   					   att.setValue("SmallMol")
   					   inhibitorNode.getAttr().add(att)
   					   att = new Attr()
   					   att.setName("Name")
   					   att.setValue(inhibitor)
   					   inhibitorNode.getAttr().add(att)
   					   nodeMap.put(inhibitor, inhibitorNode)
   					   nodes.getNode().add(inhibitorNode)      					
                   }
                   
                   Node componentNode = null                
                   String cmpURN = ""
                   
                   if (nodeMap.containsKey(component))	{
                       componentNode = nodeMap.get(component) 
                   } else	{
                       componentNode = new Node()
                       componentNode.setLocalId("N" + random.nextInt(RLIMIT))
                       cmpURN = literatureQueryService.findGeneURN(component)
   				       if (cmpURN == null)	{        			    
   				           log.info("Missed Component: " + component) 
   				           altMisses.add(component)
   				           continue;
   				       }
                       componentNode.setUrn(cmpURN)                       
                       
                       att = new Attr()
                       att.setName("NodeType")
                       att.setValue("Protein")
                       componentNode.getAttr().add(att)
                       att = new Attr()
                       att.setName("Name")
                       att.setValue(component)
                       componentNode.getAttr().add(att)
                       nodeMap.put(component, componentNode)
                       nodes.getNode().add(componentNode)                       
                   }
                                                     
      
                   Control control = new Control()
                   control.setLocalId("L" + + random.nextInt(RLIMIT))
                   
                   if (searchResult.effectMolecular != null)	{                       
   				       if ("Activation".compareToIgnoreCase(searchResult.effectMolecular) == 0 ||
   				               "Upregulation".compareToIgnoreCase(searchResult.effectMolecular) == 0)	{
   				           createAtt(control, "ControlType", "Regulation")
   				           createAtt(control, "Effect", "positive")   				              				           
   				       } else if ("Cleavage".compareToIgnoreCase(searchResult.effectMolecular) == 0)	{
   				           createAtt(control, "ControlType", "Expression")
   				           createAtt(control, "Mechanism", "Cleavage")
   				           createAtt(control, "Effect", "negative")   				        				        
   				       } else if ("Decreased".compareToIgnoreCase(searchResult.effectMolecular) == 0) {
   				           createAtt(control, "ControlType", "Expression")
				           createAtt(control, "Effect", "negative")
   				       } else if ("Degradation".compareToIgnoreCase(searchResult.effectMolecular) == 0) {
				           createAtt(control, "ControlType", "Expression")
				           createAtt(control, "Mechanism", "Degradation")
				           createAtt(control, "Effect", "negative")
   				       } else if ("Defective Binding".compareToIgnoreCase(searchResult.effectMolecular) == 0 ||
   				                  "Downregulation".compareToIgnoreCase(searchResult.effectMolecular) == 0 ||
   				                  "Downregulation, Inhibition".compareToIgnoreCase(searchResult.effectMolecular) == 0 ||
   				                  "Inhibition".compareToIgnoreCase(searchResult.effectMolecular) == 0)	{
   				           createAtt(control, "ControlType", "Regulation")
   				           createAtt(control, "Effect", "negative")
   				       } else	{
   				           log.warn("Unknown molecular effect: " + searchResult.effectMolecular)
   				           createAtt(control, "ControlType", "Unknown")
   				       }
   				   } else	{
   				       log.warn("Molecular effect is null, mapping relationship type to unknown")
   				       createAtt(control, "ControlType", "Unknown")   				       
   				   }
                   
                   Link l = new Link()
                   l.setType("in")
                   l.setRef(inhibitorNode.getLocalId())
                   control.getLink().add(l)
                   
                   l = new Link()
                   l.setType("out")
                   l.setRef(componentNode.getLocalId())
                   control.getLink().add(l)
                   
                   createAtt(control, "X-EffectResponseRate", searchResult.effectResponseRate)
                   createAtt(control, "X-EffectDownstream", searchResult.effectDownstream)
                   createAtt(control, "X-EffectBeneficial", searchResult.effectBeneficial)
                   createAtt(control, "X-EffectAdverse", searchResult.effectAdverse)
                   createAtt(control, "X-EffectDescription", searchResult.effectDescription)
                   createAtt(control, "X-EffectPharmacos", searchResult.effectPharmacos)
                   createAtt(control, "X-EffectPotentials", searchResult.effectPotentials)
                   createAtt(control, "X-TrialType", searchResult.trialType)
                   createAtt(control, "X-TrialPhase", searchResult.trialPhase)
                   createAtt(control, "X-TrialStatus", searchResult.trialStatus)
                   createAtt(control, "X-TrialExperimentalModel", searchResult.trialExperimentalModel)
                   createAtt(control, "X-TrialTissue", searchResult.trialTissue)
                   createAtt(control, "X-TrialBodySubstance", searchResult.trialBodySubstance)
                   createAtt(control, "X-TrialDescription", searchResult.trialDescription)
                   createAtt(control, "X-TrialDesigns", searchResult.trialDesigns)
                   createAtt(control, "X-TrialCellLine", searchResult.trialCellLine)
                   createAtt(control, "X-TrialCellType", searchResult.trialCellType)
                   createAtt(control, "X-TrialPatientsNumber", searchResult.trialPatientsNumber)
                   createAtt(control, "X-TrialInclusionCriteria", searchResult.trialInclusionCriteria)
                   createAtt(control, "X-Inhibitor", searchResult.inhibitor)
                   createAtt(control, "X-InhibitorStandardName", searchResult.inhibitorStandardName)
                   createAtt(control, "X-CasID", searchResult.casid)
                   createAtt(control, "X-Description", searchResult.description)
                   createAtt(control, "X-Concentration", searchResult.concentration)
                   createAtt(control, "X-TimeExposure", searchResult.timeExposure)
                   createAtt(control, "X-Administration", searchResult.administration)
                   createAtt(control, "X-Treatment", searchResult.treatment)
                   createAtt(control, "X-Techniques", searchResult.techniques)
                   createAtt(control, "X-EffectMolecular", searchResult.effectMolecular)
                   createAtt(control, "X-EffectPercent", searchResult.effectPercent)
                   createAtt(control, "X-EffectNumber", searchResult.effectNumber)
                   createAtt(control, "X-EffectValue", searchResult.effectValue)
                   createAtt(control, "X-EffectSd", searchResult.effectSd)
                   createAtt(control, "X-EffectUnit", searchResult.effectUnit)
                    
                   if (!refCountMap.containsKey(searchResult.reference.referenceId))	{
                       refCount = 1
                       refCountMap.put(searchResult.reference.referenceId, new Integer(refCount))
                   } else	{                       
                       refCount = (new Integer(refCountMap.get(searchResult.reference.referenceId))).intValue()
                       refCount++
                       refCountMap.put(searchResult.reference.referenceId, new Integer(refCount))
                   }
                   createAtt(control, "mref", searchResult.reference.referenceId, refCount)
                   createAtt(control, "msrc", searchResult.reference.referenceTitle, refCount)
                   createAtt(control, "Disease", searchResult.reference.disease)
                   createAtt(control, "X-Disease Site", searchResult.reference.diseaseSite)
                   createAtt(control, "X-Disease Types", searchResult.reference.diseaseTypes)
                   createAtt(control, "X-Disease Stage", searchResult.reference.diseaseStage)
                   createAtt(control, "X-Disease Description", searchResult.reference.diseaseDescription)                        
                   createAtt(control, "X-Physiology", searchResult.reference.physiology)
                   createAtt(control, "X-Clinical Statistics", searchResult.reference.statClinical)
                   createAtt(control, "X-Clinical Correlation", searchResult.reference.statClinicalCorrelation)
                   createAtt(control, "X-Statistical Tests", searchResult.reference.statTests)
                   createAtt(control, "X-Coefficient", searchResult.reference.statCoefficient)
                   createAtt(control, "X-P Value", searchResult.reference.statPValue)
                   createAtt(control, "X-Statistical Description", searchResult.reference.statDescription)
     
                   controls.getControl().add(control)                              
               }
           }
       }       
       misses.addAll(inhMisses)
       
       att = new Attr()
       att.setName("Notes")
       att.setValue(inhMisses.toString())
       pResNet.getAttr().add(att)
                       
       resnet.setProperties(pResNet)
       resnet.setNodes(nodes)
       resnet.setControls(controls)

       return resnet
   }
   
   // Only create attributes for non-null entries to keep the RNEF file size down
   def createAtt(Control control, String attName, String attValue)	{
       if (attValue != null && attValue.trim().length() > 0)	{
           Attr att = new Attr()
	        att.setName(attName)
	        att.setValue(replaceUnicode(attValue))	       
	        control.getAttr().add(att)                
       }        
   }
    
    // Only create attributes for non-null entries to keep the RNEF file size down needed for indexed attributes
    def createAtt(Control control, String attName, String attValue, int cnt)	{
        if (attValue != null && attValue.trim().length() > 0)	{
            Attr att = new Attr()
	        att.setName(attName)
	        att.setValue(replaceUnicode(attValue))
            att.setIndex(cnt)
	        control.getAttr().add(att)                
        }        
    }
       
    // We need to replace the unicode character for now since Pathway Studio cannot import them
    def replaceUnicode(String input)	{
        Pattern p = Pattern.compile("[^\\p{ASCII}]");
        Matcher m = p.matcher(input);
        while(m.find()) {
            input = input.replace(m.group(0), '-')
        }
        return input
    }
}