<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
 
-->

<table class="detail">
	${createNameValueRow(name:"LiteratureReferenceData.component", value:jubData.reference.component)}
	${createNameValueRow(name:"LiteratureReferenceData.componentClass", value:jubData.reference.componentClass)}
	${createNameValueRow(name:"LiteratureReferenceData.geneId", value:jubData.reference.geneId)}
	${createNameValueRow(name:"LiteratureReferenceData.moleculeType", value:jubData.reference.moleculeType)}
	${createNameValueRow(name:"LiteratureReferenceData.variant", value:jubData.reference.variant)}
	${createNameValueRow(name:"LiteratureReferenceData.referenceType", value:jubData.reference.referenceType)}
	${createNameValueRow(name:"LiteratureReferenceData.referenceId", value:jubData.reference.referenceId)}
	${createNameValueRow(name:"LiteratureReferenceData.referenceTitle", value:jubData.reference.referenceTitle)}
	${createNameValueRow(name:"LiteratureReferenceData.backReferences", value:jubData.reference.backReferences)}
	${createNameValueRow(name:"LiteratureReferenceData.studyType", value:jubData.reference.studyType)}
	${createNameValueRow(name:"LiteratureReferenceData.disease", value:jubData.reference.disease)}
	${createNameValueRow(name:"LiteratureReferenceData.diseaseIcd10", value:jubData.reference.diseaseIcd10)}
	${createNameValueRow(name:"LiteratureReferenceData.diseaseMesh", value:jubData.reference.diseaseMesh)}
	${createNameValueRow(name:"LiteratureReferenceData.diseaseSite", value:jubData.reference.diseaseSite)}
	${createNameValueRow(name:"LiteratureReferenceData.diseaseStage", value:jubData.reference.diseaseStage)}
	${createNameValueRow(name:"LiteratureReferenceData.diseaseGrade", value:jubData.reference.diseaseGrade)}
	${createNameValueRow(name:"LiteratureReferenceData.diseaseTypes", value:jubData.reference.diseaseTypes)}
	${createNameValueRow(name:"LiteratureReferenceData.diseaseDescription", value:jubData.reference.diseaseDescription)}
	${createNameValueRow(name:"LiteratureReferenceData.physiology", value:jubData.reference.physiology)}
	${createNameValueRow(name:"LiteratureReferenceData.statClinical", value:jubData.reference.statClinical)}
	${createNameValueRow(name:"LiteratureReferenceData.statClinicalCorrelation", value:jubData.reference.statClinicalCorrelation)}
	${createNameValueRow(name:"LiteratureReferenceData.statTests", value:jubData.reference.statTests)}
	${createNameValueRow(name:"LiteratureReferenceData.statCoefficient", value:jubData.reference.statCoefficient)}
	${createNameValueRow(name:"LiteratureReferenceData.statPValue", value:jubData.reference.statPValue)}
	${createNameValueRow(name:"LiteratureReferenceData.statDescription", value:jubData.reference.statDescription)}
<g:if test="${resultType?.endsWith('ALTERATION')}">
	${createNameValueRow(name:"LiteratureAlterationData.alterationType", value:jubData.alterationType)}
	${createNameValueRow(name:"LiteratureAlterationData.control", value:jubData.control)}
	${createNameValueRow(name:"LiteratureAlterationData.effect", value:jubData.effect)}
	${createNameValueRow(name:"LiteratureAlterationData.description", value:jubData.description)}
	${createNameValueRow(name:"LiteratureAlterationData.techniques", value:jubData.techniques)}
	${createNameValueRow(name:"LiteratureAlterationData.patientsPercent", value:jubData.patientsPercent)}
	${createNameValueRow(name:"LiteratureAlterationData.patientsNumber", value:jubData.patientsNumber)}
	${createNameValueRow(name:"LiteratureAlterationData.popNumber", value:jubData.popNumber)}
	${createNameValueRow(name:"LiteratureAlterationData.popInclusionCriteria", value:jubData.popInclusionCriteria)}
	${createNameValueRow(name:"LiteratureAlterationData.popExclusionCriteria", value:jubData.popExclusionCriteria)}
	${createNameValueRow(name:"LiteratureAlterationData.popDescription", value:jubData.popDescription)}
	${createNameValueRow(name:"LiteratureAlterationData.popType", value:jubData.popType)}
	${createNameValueRow(name:"LiteratureAlterationData.popValue", value:jubData.popValue)}
	${createNameValueRow(name:"LiteratureAlterationData.popPhase", value:jubData.popPhase)}
	${createNameValueRow(name:"LiteratureAlterationData.popStatus", value:jubData.popStatus)}
	${createNameValueRow(name:"LiteratureAlterationData.popExperimentalModel", value:jubData.popExperimentalModel)}
	${createNameValueRow(name:"LiteratureAlterationData.popTissue", value:jubData.popTissue)}
	${createNameValueRow(name:"LiteratureAlterationData.popBodySubstance", value:jubData.popBodySubstance)}
	${createNameValueRow(name:"LiteratureAlterationData.popLocalization", value:jubData.popLocalization)}
	${createNameValueRow(name:"LiteratureAlterationData.popCellType", value:jubData.popCellType)}
	${createNameValueRow(name:"LiteratureAlterationData.clinSubmucosaMarkerType", value:jubData.clinSubmucosaMarkerType)}
	${createNameValueRow(name:"LiteratureAlterationData.clinSubmucosaUnit", value:jubData.clinSubmucosaUnit)}
	${createNameValueRow(name:"LiteratureAlterationData.clinSubmucosaValue", value:jubData.clinSubmucosaValue)}
	${createNameValueRow(name:"LiteratureAlterationData.clinAsmMarkerType", value:jubData.clinAsmMarkerType)}
	${createNameValueRow(name:"LiteratureAlterationData.clinAsmUnit", value:jubData.clinAsmUnit)}
	${createNameValueRow(name:"LiteratureAlterationData.clinAsmValue", value:jubData.clinAsmValue)}
	${createNameValueRow(name:"LiteratureAlterationData.clinCellularSource", value:jubData.clinCellularSource)}
	${createNameValueRow(name:"LiteratureAlterationData.clinCellularType", value:jubData.clinCellularType)}
	${createNameValueRow(name:"LiteratureAlterationData.clinCellularCount", value:jubData.clinCellularCount)}
	${createNameValueRow(name:"LiteratureAlterationData.clinPriorMedPercent", value:jubData.clinPriorMedPercent)}
	${createNameValueRow(name:"LiteratureAlterationData.clinPriorMedDose", value:jubData.clinPriorMedDose)}
	${createNameValueRow(name:"LiteratureAlterationData.clinPriorMedName", value:jubData.clinPriorMedName)}
	${createNameValueRow(name:"LiteratureAlterationData.clinBaselineVariable", value:jubData.clinBaselineVariable)}
	${createNameValueRow(name:"LiteratureAlterationData.clinBaselinePercent", value:jubData.clinBaselinePercent)}
	${createNameValueRow(name:"LiteratureAlterationData.clinBaselineValue", value:jubData.clinBaselineValue)}
	${createNameValueRow(name:"LiteratureAlterationData.clinSmoker", value:jubData.clinSmoker)}
	${createNameValueRow(name:"LiteratureAlterationData.clinAtopy", value:jubData.clinAtopy)}
	${createNameValueRow(name:"LiteratureAlterationData.controlExpPercent", value:jubData.controlExpPercent)}
	${createNameValueRow(name:"LiteratureAlterationData.controlExpNumber", value:jubData.controlExpNumber)}
	${createNameValueRow(name:"LiteratureAlterationData.controlExpValue", value:jubData.controlExpValue)}
	${createNameValueRow(name:"LiteratureAlterationData.controlExpSd", value:jubData.controlExpSd)}
	${createNameValueRow(name:"LiteratureAlterationData.controlExpUnit", value:jubData.controlExpUnit)}
	${createNameValueRow(name:"LiteratureAlterationData.overExpPercent", value:jubData.overExpPercent)}
	${createNameValueRow(name:"LiteratureAlterationData.overExpNumber", value:jubData.overExpNumber)}
	${createNameValueRow(name:"LiteratureAlterationData.overExpValue", value:jubData.overExpValue)}
	${createNameValueRow(name:"LiteratureAlterationData.overExpSd", value:jubData.overExpSd)}
	${createNameValueRow(name:"LiteratureAlterationData.overExpUnit", value:jubData.overExpUnit)}
	${createNameValueRow(name:"LiteratureAlterationData.lossExpPercent", value:jubData.lossExpPercent)}
	${createNameValueRow(name:"LiteratureAlterationData.lossExpNumber", value:jubData.lossExpNumber)}
	${createNameValueRow(name:"LiteratureAlterationData.lossExpValue", value:jubData.lossExpValue)}
	${createNameValueRow(name:"LiteratureAlterationData.lossExpSd", value:jubData.lossExpSd)}
	${createNameValueRow(name:"LiteratureAlterationData.lossExpUnit", value:jubData.lossExpUnit)}
	${createNameValueRow(name:"LiteratureAlterationData.totalExpPercent", value:jubData.totalExpPercent)}
	${createNameValueRow(name:"LiteratureAlterationData.totalExpNumber", value:jubData.totalExpNumber)}
	${createNameValueRow(name:"LiteratureAlterationData.totalExpValue", value:jubData.totalExpValue)}
	${createNameValueRow(name:"LiteratureAlterationData.totalExpSd", value:jubData.totalExpSd)}
	${createNameValueRow(name:"LiteratureAlterationData.totalExpUnit", value:jubData.totalExpUnit)}
	${createNameValueRow(name:"LiteratureAlterationData.glcControlPercent", value:jubData.glcControlPercent)}
	${createNameValueRow(name:"LiteratureAlterationData.glcMolecularChange", value:jubData.glcMolecularChange)}
	${createNameValueRow(name:"LiteratureAlterationData.glcType", value:jubData.glcType)}
	${createNameValueRow(name:"LiteratureAlterationData.glcPercent", value:jubData.glcPercent)}
	${createNameValueRow(name:"LiteratureAlterationData.glcNumber", value:jubData.glcNumber)}
	${createNameValueRow(name:"LiteratureAlterationData.ptmRegion", value:jubData.ptmRegion)}
	${createNameValueRow(name:"LiteratureAlterationData.ptmType", value:jubData.ptmType)}
	${createNameValueRow(name:"LiteratureAlterationData.ptmChange", value:jubData.ptmChange)}
	${createNameValueRow(name:"LiteratureAlterationData.lohLoci", value:jubData.lohLoci)}
	${createNameValueRow(name:"LiteratureAlterationData.mutationType", value:jubData.mutationType)}
	${createNameValueRow(name:"LiteratureAlterationData.mutationChange", value:jubData.mutationChange)}
	${createNameValueRow(name:"LiteratureAlterationData.mutationSites", value:jubData.mutationSites)}
	${createNameValueRow(name:"LiteratureAlterationData.epigeneticRegion", value:jubData.epigeneticRegion)}
	${createNameValueRow(name:"LiteratureAlterationData.epigeneticType", value:jubData.epigeneticType)}
	<g:each in="${jubData.assocMoleculeDetails}" var="amdData">
	<tr class="prop"><td colspan="2" class="name">Associated Molecule Details</td></tr>
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.molecule", value:amdData.molecule)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.moleculeType", value:amdData.moleculeType)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.totalExpPercent", value:amdData.totalExpPercent)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.totalExpNumber", value:amdData.totalExpNumber)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.totalExpValue", value:amdData.totalExpValue)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.totalExpSd", value:amdData.totalExpSd)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.totalExpUnit", value:amdData.totalExpUnit)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.overExpPercent", value:amdData.overExpPercent)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.overExpNumber", value:amdData.overExpNumber)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.overExpValue", value:amdData.overExpValue)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.overExpSd", value:amdData.overExpSd)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.overExpUnit", value:amdData.overExpUnit)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.coExpPercent", value:amdData.coExpPercent)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.coExpNumber", value:amdData.coExpNumber)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.coExpValue", value:amdData.coExpValue)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.coExpSd", value:amdData.coExpSd)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.coExpUnit", value:amdData.coExpUnit)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.mutationType", value:amdData.mutationType)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.mutationSites", value:amdData.mutationSites)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.mutationChange", value:amdData.mutationChange)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.mutationPercent", value:amdData.mutationPercent)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.mutationNumber", value:amdData.mutationNumber)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.targetExpPercent", value:amdData.targetExpPercent)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.targetExpNumber", value:amdData.targetExpNumber)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.targetExpValue", value:amdData.targetExpValue)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.targetExpSd", value:amdData.targetExpSd)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.targetExpUnit", value:amdData.targetExpUnit)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.targetOverExpPercent", value:amdData.targetOverExpPercent)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.targetOverExpNumber", value:amdData.targetOverExpNumber)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.targetOverExpValue", value:amdData.targetOverExpValue)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.targetOverExpSd", value:amdData.targetOverExpSd)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.targetOverExpUnit", value:amdData.targetOverExpUnit)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.techniques", value:amdData.techniques)}
	${createNameValueRow(name:"LiteratureAssocMoleculeDetailsData.description", value:amdData.description)}
	</g:each>
</g:if>
<g:elseif test="${resultType?.endsWith('INHIBITOR')}">
	${createNameValueRow(name:"LiteratureInhibitorData.inhibitor", value:jubData.inhibitor)}
	${createNameValueRow(name:"LiteratureInhibitorData.inhibitorStandardName", value:jubData.inhibitorStandardName)}
	${createNameValueRow(name:"LiteratureInhibitorData.casid", value:jubData.casid)}
	${createNameValueRow(name:"LiteratureInhibitorData.description", value:jubData.description)}
	${createNameValueRow(name:"LiteratureInhibitorData.concentration", value:jubData.concentration)}
	${createNameValueRow(name:"LiteratureInhibitorData.timeExposure", value:jubData.timeExposure)}
	${createNameValueRow(name:"LiteratureInhibitorData.administration", value:jubData.administration)}
	${createNameValueRow(name:"LiteratureInhibitorData.treatment", value:jubData.treatment)}
	${createNameValueRow(name:"LiteratureInhibitorData.techniques", value:jubData.techniques)}
	${createNameValueRow(name:"LiteratureInhibitorData.effectResponseRate", value:jubData.effectResponseRate)}
	${createNameValueRow(name:"LiteratureInhibitorData.effectDownstream", value:jubData.effectDownstream)}
	${createNameValueRow(name:"LiteratureInhibitorData.effectBeneficial", value:jubData.effectBeneficial)}
	${createNameValueRow(name:"LiteratureInhibitorData.effectAdverse", value:jubData.effectAdverse)}
	${createNameValueRow(name:"LiteratureInhibitorData.effectDescription", value:jubData.effectDescription)}
	${createNameValueRow(name:"LiteratureInhibitorData.effectPharmacos", value:jubData.effectPharmacos)}
	${createNameValueRow(name:"LiteratureInhibitorData.effectPotentials", value:jubData.effectPotentials)}
	${createNameValueRow(name:"LiteratureInhibitorData.trialType", value:jubData.trialType)}
	${createNameValueRow(name:"LiteratureInhibitorData.trialPhase", value:jubData.trialPhase)}
	${createNameValueRow(name:"LiteratureInhibitorData.trialStatus", value:jubData.trialStatus)}
	${createNameValueRow(name:"LiteratureInhibitorData.trialExperimentalModel", value:jubData.trialExperimentalModel)}
	${createNameValueRow(name:"LiteratureInhibitorData.trialTissue", value:jubData.trialTissue)}
	${createNameValueRow(name:"LiteratureInhibitorData.trialBodySubstance", value:jubData.trialBodySubstance)}
	${createNameValueRow(name:"LiteratureInhibitorData.trialDescription", value:jubData.trialDescription)}
	${createNameValueRow(name:"LiteratureInhibitorData.trialDesigns", value:jubData.trialDesigns)}
	${createNameValueRow(name:"LiteratureInhibitorData.trialCellLine", value:jubData.trialCellLine)}
	${createNameValueRow(name:"LiteratureInhibitorData.trialCellType", value:jubData.trialCellType)}
	${createNameValueRow(name:"LiteratureInhibitorData.trialPatientsNumber", value:jubData.trialPatientsNumber)}
	${createNameValueRow(name:"LiteratureInhibitorData.trialInclusionCriteria", value:jubData.trialInclusionCriteria)}
	${createNameValueRow(name:"LiteratureInhibitorData.effectMolecular", value:jubData.effectMolecular)}
	${createNameValueRow(name:"LiteratureInhibitorData.effectPercent", value:jubData.effectPercent)}
	${createNameValueRow(name:"LiteratureInhibitorData.effectNumber", value:jubData.effectNumber)}
	${createNameValueRow(name:"LiteratureInhibitorData.effectValue", value:jubData.effectValue)}
	${createNameValueRow(name:"LiteratureInhibitorData.effectSd", value:jubData.effectSd)}
	${createNameValueRow(name:"LiteratureInhibitorData.effectUnit", value:jubData.effectUnit)}
</g:elseif>
<g:elseif test="${resultType?.endsWith('INTERACTION')}">
	${createNameValueRow(name:"LiteratureInteractionData.sourceComponent", value:jubData.sourceComponent)}
	${createNameValueRow(name:"LiteratureInteractionData.sourceGeneId", value:jubData.sourceGeneId)}
	${createNameValueRow(name:"LiteratureInteractionData.targetComponent", value:jubData.targetComponent)}
	${createNameValueRow(name:"LiteratureInteractionData.targetGeneId", value:jubData.targetGeneId)}
	${createNameValueRow(name:"LiteratureInteractionData.interactionMode", value:jubData.interactionMode)}
	${createNameValueRow(name:"LiteratureInteractionData.regulation", value:jubData.regulation)}
	${createNameValueRow(name:"LiteratureInteractionData.mechanism", value:jubData.mechanism)}
	${createNameValueRow(name:"LiteratureInteractionData.effect", value:jubData.effect)}
	${createNameValueRow(name:"LiteratureInteractionData.localization", value:jubData.localization)}
	${createNameValueRow(name:"LiteratureInteractionData.region", value:jubData.region)}
	${createNameValueRow(name:"LiteratureInteractionData.techniques", value:jubData.techniques)}
</g:elseif>
<g:elseif test="${resultType?.endsWith('PROTEIN_EFFECT')}">
	${createNameValueRow(name:"LiteratureProteinEffectData.description", value:jubData.description)}
</g:elseif>
<g:if test="${!resultType?.endsWith('INHIBITOR')}">
	<g:if test="${jubData.inVivoModel != null}">
	<tr class="prop"><td colspan="2" class="name">In Vivo Model</td></tr>
	${createNameValueRow(name:"LiteratureModelData.description", value:jubData.inVivoModel.description)}
	${createNameValueRow(name:"LiteratureModelData.stimulation", value:jubData.inVivoModel.stimulation)}
	${createNameValueRow(name:"LiteratureModelData.controlChallenge", value:jubData.inVivoModel.controlChallenge)}
	${createNameValueRow(name:"LiteratureModelData.challenge", value:jubData.inVivoModel.challenge)}
	${createNameValueRow(name:"LiteratureModelData.sentization", value:jubData.inVivoModel.sentization)}
	${createNameValueRow(name:"LiteratureModelData.zygosity", value:jubData.inVivoModel.zygosity)}
	${createNameValueRow(name:"LiteratureModelData.experimentalModel", value:jubData.inVivoModel.experimentalModel)}
	${createNameValueRow(name:"LiteratureModelData.animalWildType", value:jubData.inVivoModel.animalWildType)}
	${createNameValueRow(name:"LiteratureModelData.tissue", value:jubData.inVivoModel.tissue)}
	${createNameValueRow(name:"LiteratureModelData.cellType", value:jubData.inVivoModel.cellType)}
	${createNameValueRow(name:"LiteratureModelData.cellLine", value:jubData.inVivoModel.cellLine)}
	${createNameValueRow(name:"LiteratureModelData.bodySubstance", value:jubData.inVivoModel.bodySubstance)}
	${createNameValueRow(name:"LiteratureModelData.component", value:jubData.inVivoModel.component)}
	${createNameValueRow(name:"LiteratureModelData.geneId", value:jubData.inVivoModel.geneId)}
	</g:if>	
	<g:if test="${jubData.inVitroModel != null}">
	<tr class="prop"><td colspan="2" class="name">In Vitro Model</td></tr>
	${createNameValueRow(name:"LiteratureModelData.description", value:jubData.inVitroModel.description)}
	${createNameValueRow(name:"LiteratureModelData.stimulation", value:jubData.inVitroModel.stimulation)}
	${createNameValueRow(name:"LiteratureModelData.controlChallenge", value:jubData.inVitroModel.controlChallenge)}
	${createNameValueRow(name:"LiteratureModelData.challenge", value:jubData.inVitroModel.challenge)}
	${createNameValueRow(name:"LiteratureModelData.sentization", value:jubData.inVitroModel.sentization)}
	${createNameValueRow(name:"LiteratureModelData.zygosity", value:jubData.inVitroModel.zygosity)}
	${createNameValueRow(name:"LiteratureModelData.experimentalModel", value:jubData.inVitroModel.experimentalModel)}
	${createNameValueRow(name:"LiteratureModelData.animalWildType", value:jubData.inVitroModel.animalWildType)}
	${createNameValueRow(name:"LiteratureModelData.tissue", value:jubData.inVitroModel.tissue)}
	${createNameValueRow(name:"LiteratureModelData.cellType", value:jubData.inVitroModel.cellType)}
	${createNameValueRow(name:"LiteratureModelData.cellLine", value:jubData.inVitroModel.cellLine)}
	${createNameValueRow(name:"LiteratureModelData.bodySubstance", value:jubData.inVitroModel.bodySubstance)}
	${createNameValueRow(name:"LiteratureModelData.component", value:jubData.inVitroModel.component)}
	${createNameValueRow(name:"LiteratureModelData.geneId", value:jubData.inVitroModel.geneId)}
	</g:if>
</g:if>
</table>