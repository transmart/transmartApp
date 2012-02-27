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
 * $Id: LiteratureController.groovy 11850 2012-01-24 16:41:12Z jliu $
 * @author $Author: jliu $
 * @version $Revision: 11850 $
 */
import grails.converters.*
import com.recomdata.util.*
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.recomdata.util.ariadne.Batch;

class LiteratureController {

	def literatureQueryService
	def trialQueryService
	def searchService
	def springSecurityService

    def index = {

	}


	static List litRefDataColumns = [
  		"component",
		"componentClass",
		"geneId",
		"moleculeType",
		"variant",
		"referenceType",
		"referenceId",
		"referenceTitle",
		"backReferences",
		"studyType",
		"disease",
		"diseaseIcd10",
		"diseaseMesh",
		"diseaseSite",
		"diseaseStage",
		"diseaseGrade",
		"diseaseTypes",
		"diseaseDescription",
		"physiology",
		"statClinical",
		"statClinicalCorrelation",
		"statTests",
		"statCoefficient",
		"statPValue",
		"statDescription"
	]

	static List litAltDataColumns = [
		"alterationType",
		"control",
		"effect",
		"description",
		"techniques",
		"patientsPercent",
		"patientsNumber",
		"popNumber",
		"popInclusionCriteria",
		"popExclusionCriteria",
		"popDescription",
		"popType",
		"popValue",
		"popPhase",
		"popStatus",
		"popExperimentalModel",
		"popTissue",
		"popBodySubstance",
		"popLocalization",
		"popCellType",
		"clinSubmucosaMarkerType",
		"clinSubmucosaUnit",
		"clinSubmucosaValue",
		"clinAsmMarkerType",
		"clinAsmUnit",
		"clinAsmValue",
		"clinCellularSource",
		"clinCellularType",
		"clinCellularCount",
		"clinPriorMedPercent",
		"clinPriorMedDose",
		"clinPriorMedName",
		"clinBaselineVariable",
		"clinBaselinePercent",
		"clinBaselineValue",
		"clinSmoker",
		"clinAtopy",
		"controlExpPercent",
		"controlExpNumber",
		"controlExpValue",
		"controlExpSd",
		"controlExpUnit",
		"overExpPercent",
		"overExpNumber",
		"overExpValue",
		"overExpSd",
		"overExpUnit",
		"lossExpPercent",
		"lossExpNumber",
		"lossExpValue",
		"lossExpSd",
		"lossExpUnit",
		"totalExpPercent",
		"totalExpNumber",
		"totalExpValue",
		"totalExpSd",
		"totalExpUnit",
		"glcControlPercent",
		"glcMolecularChange",
		"glcType",
		"glcPercent",
		"glcNumber",
		"ptmRegion",
		"ptmType",
		"ptmChange",
		"lohLoci",
		"mutationType",
		"mutationChange",
		"mutationSites",
		"epigeneticRegion",
		"epigeneticType"
	]
	static List litInhDataColumns = [
		"effectResponseRate",
		"effectDownstream",
		"effectBeneficial",
		"effectAdverse",
		"effectDescription",
		"effectPharmacos",
		"effectPotentials",
		"trialType",
		"trialPhase",
		"trialStatus",
		"trialExperimentalModel",
		"trialTissue",
		"trialBodySubstance",
		"trialDescription",
		"trialDesigns",
		"trialCellLine",
		"trialCellType",
		"trialPatientsNumber",
		"trialInclusionCriteria",
		"inhibitor",
		"inhibitorStandardName",
		"casid",
		"description",
		"concentration",
		"timeExposure",
		"administration",
		"treatment",
		"techniques",
		"effectMolecular",
		"effectPercent",
		"effectNumber",
		"effectValue",
		"effectSd",
		"effectUnit"
	]
	static List litIntDataColumns = [
		"sourceComponent",
		"sourceGeneId",
		"targetComponent",
		"targetGeneId",
		"interactionMode",
		"regulation",
		"mechanism",
		"effect",
		"localization",
		"region",
		"techniques"
	]
	static List litPEDataColumns = [
	    "description"
	]

	static List litModelDataColumns = [
		"description",
		"stimulation",
		"controlChallenge",
		"challenge",
		"sentization",
		"zygosity",
		"experimentalModel",
		"animalWildType",
		"tissue",
		"cellType",
		"cellLine",
		"bodySubstance",
		"component",
		"geneId"
    ]

	static List litAMDDataColumns = [
		"molecule",
		"moleculeType",
		"totalExpPercent",
		"totalExpNumber",
		"totalExpValue",
		"totalExpSd",
		"totalExpUnit",
		"overExpPercent",
		"overExpNumber",
		"overExpValue",
		"overExpSd",
		"overExpUnit",
		"coExpPercent",
		"coExpNumber",
		"coExpValue",
		"coExpSd",
		"coExpUnit",
		"mutationType",
		"mutationSites",
		"mutationChange",
		"mutationPercent",
		"mutationNumber",
		"targetExpPercent",
		"targetExpNumber",
		"targetExpValue",
		"targetExpSd",
		"targetExpUnit",
		"targetOverExpPercent",
		"targetOverExpNumber",
		"targetOverExpValue",
		"targetOverExpSd",
		"targetOverExpUnit",
		"techniques",
		"description"
	]


}