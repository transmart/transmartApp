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
 * $Id: LiteratureController.groovy 10133 2011-10-20 21:34:43Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 10133 $
 */
import grails.converters.*

import com.recomdata.util.*
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.transmart.SearchResult;
import org.transmart.searchapp.AccessLog;

import com.recomdata.util.ariadne.Batch;

class LiteratureController {

	def literatureQueryService
	def trialQueryService
	def jubilantResNetService
	def searchService
	def springSecurityService

    def index = {

	}

	def showJubFilter = {
		def model = [:]
		model.disease = literatureQueryService.diseaseList(session.searchFilter)
		model.diseaseSite = literatureQueryService.diseaseSiteList(session.searchFilter)
		model.component = literatureQueryService.componentList(session.searchFilter)
		model.mutationType = literatureQueryService.mutationTypeList(session.searchFilter)
		model.mutationSite = literatureQueryService.mutationSiteList(session.searchFilter)
		model.epigeneticType = literatureQueryService.epigeneticTypeList(session.searchFilter)
		model.epigeneticRegion = literatureQueryService.epigeneticRegionList(session.searchFilter)
		model.moleculeType = literatureQueryService.moleculeTypeList(session.searchFilter)
		model.ptmType = literatureQueryService.ptmTypeList(session.searchFilter)
		model.ptmRegion = literatureQueryService.ptmRegionList(session.searchFilter)
		model.source = literatureQueryService.sourceList(session.searchFilter)
		model.target = literatureQueryService.targetList(session.searchFilter)
		model.experimentalModel = literatureQueryService.experimentalModelList(session.searchFilter)
		model.mechanism = literatureQueryService.mechanismList(session.searchFilter)
		model.trialType = literatureQueryService.trialTypeList(session.searchFilter)
		model.trialPhase = literatureQueryService.trialPhaseList(session.searchFilter)
		model.inhibitorName = literatureQueryService.inhibitorNameList(session.searchFilter)
		model.trialExperimentalModel = literatureQueryService.trialExperimentalModelList(session.searchFilter)
	  	render(template:'litFilter', model:model)
	}

	def getJubOncologyAlterationDetail = {
		def id = params?.id
		def result = [:]
		if (id != null) {
			result = LiteratureAlterationData.get(Long.valueOf(id))
		}
		render(template:'litDetail', model:[result:result])
	}

	def getJubOncologyInhibitorDetail = {
		def id = params?.id
		def result = [:]
		if (id != null) {
			result = LiteratureInhibitorData.get(Long.valueOf(id))
		}
		render(template:'litDetail', model:[result:result])
	}

	def getJubOncologyInteractionDetail = {
		def id = params?.id
		def result = [:]
		if (id != null) {
			result = LiteratureInteractionData.get(Long.valueOf(id))
		}
		render(template:'litDetail', model:[result:result])
	}

    def filterJubilant = {
		def searchFilter = session.searchFilter
		bindData(searchFilter.litFilter, params)
		searchFilter.litFilter.parseDiseaseSite(params?.diseaseSite)
		searchFilter.litFilter.parseComponentList(params?.componentList)
		for (alterationType in searchFilter.litFilter.alterationTypes.keySet()) {
			searchFilter.litFilter.alterationTypes.put(alterationType, "on".equals(params.get("alterationtype_" + alterationType.toLowerCase().replace(" ", "_"))))
		}
		searchFilter.datasource = "literature"
		def sResult = new SearchResult()
		searchService.doResultCount(sResult, searchFilter)
		render(view:'/search/list', model:[searchresult:sResult])
	}

    def datasourceJubilant = {
		def datatype = params?.datatype // != null ? params.datatype : "JUBILANT_ONCOLOGY_ALTERATION"
		def searchFilter = session.searchFilter
		def sResult = new SearchResult()
		sResult.litJubOncAltCount = literatureQueryService.litJubOncAltCount(searchFilter)
		if (datatype == null && sResult.litJubOncAltCount > 0) {
			datatype = "JUBILANT_ONCOLOGY_ALTERATION"
		}
		sResult.litJubOncInhCount = literatureQueryService.litJubOncInhCount(searchFilter)
		if (datatype == null && sResult.litJubOncInhCount > 0) {
			datatype = "JUBILANT_ONCOLOGY_INHIBITOR"
		}
		sResult.litJubOncIntCount = literatureQueryService.litJubOncIntCount(searchFilter)
		if (datatype == null && sResult.litJubOncIntCount > 0) {
			datatype = "JUBILANT_ONCOLOGY_INTERACTION"
		}
		sResult.litJubAsthmaAltCount = literatureQueryService.litJubAsthmaAltCount(searchFilter)
		if (datatype == null && sResult.litJubAsthmaAltCount > 0) {
			datatype = "JUBILANT_ASTHMA_ALTERATION"
		}
		sResult.litJubAsthmaInhCount = literatureQueryService.litJubAsthmaInhCount(searchFilter)
		if (datatype == null && sResult.litJubAsthmaInhCount > 0) {
			datatype = "JUBILANT_ASTHMA_INHIBITOR"
		}
		sResult.litJubAsthmaIntCount = literatureQueryService.litJubAsthmaIntCount(searchFilter)
		if (datatype == null && sResult.litJubAsthmaIntCount > 0) {
			datatype = "JUBILANT_ASTHMA_INTERACTION"
		}
		sResult.litJubAsthmaPECount = literatureQueryService.litJubAsthmaPECount(searchFilter)
		if (datatype == null && sResult.litJubAsthmaPECount > 0) {
			datatype = "JUBILANT_ASTHMA_PROTEIN_EFFECT"
		}
		sResult.resultType = datatype
		switch (datatype) {
		case "JUBILANT_ONCOLOGY_ALTERATION":
			sResult.resultCount = sResult.litJubOncAltCount
			sResult.result = literatureQueryService.litJubOncAltData(searchFilter, params)
			break
		case "JUBILANT_ONCOLOGY_INHIBITOR":
			sResult.resultCount = sResult.litJubOncInhCount
			sResult.result = literatureQueryService.litJubOncInhData(searchFilter, params)
			break
		case "JUBILANT_ONCOLOGY_INTERACTION":
			sResult.resultCount = sResult.litJubOncIntCount
			sResult.result = literatureQueryService.litJubOncIntData(searchFilter, params)
			break
		case "JUBILANT_ASTHMA_ALTERATION":
			sResult.resultCount = sResult.litJubAsthmaAltCount
			sResult.result = literatureQueryService.litJubAsthmaAltData(searchFilter, params)
			break
		case "JUBILANT_ASTHMA_INHIBITOR":
			sResult.resultCount = sResult.litJubAsthmaInhCount
			sResult.result = literatureQueryService.litJubAsthmaInhData(searchFilter, params)
			break
		case "JUBILANT_ASTHMA_INTERACTION":
			sResult.resultCount = sResult.litJubAsthmaIntCount
			sResult.result = literatureQueryService.litJubAsthmaIntData(searchFilter, params)
			break
		case "JUBILANT_ASTHMA_PROTEIN_EFFECT":
			sResult.resultCount = sResult.litJubAsthmaPECount
			sResult.result = literatureQueryService.litJubAsthmaPEData(searchFilter, params)
			break
		}
		render(template:'litResult', model:[searchresult:sResult])
	}
/*
	def datasourceJubOncologyAlteration = {
		def searchFilter = session.searchFilter
		def sResult = new SearchResult()
		sResult.litJubOncAltCount = literatureQueryService.litJubOncAltCount(session.searchFilter)
		sResult.result = literatureQueryService.litJubOncAltData(searchFilter, params)
		render(template:'LiteratureAlterationData', model:[searchresult:sResult])
	}

	def datasourceJubOncologyInhibitor = {
		def searchFilter = session.searchFilter
		def sResult = new SearchResult()
		sResult.result = literatureQueryService.litJubOncInhData(searchFilter, params)
		render(template:'LiteratureInhibitorData', model:[searchresult:sResult])
	}

	def datasourceJubOncologyInteraction = {
		def searchFilter = session.searchFilter
		def sResult = new SearchResult()
		sResult.result = literatureQueryService.litJubOncIntData(searchFilter, params)
		render(template:'LiteratureInteractionData', model:[searchresult:sResult])
	}
*/

	def createJubSummary = {

		def searchFilter = session.searchFilter
		def resultCount
		def result
		resultCount = literatureQueryService.litJubOncAltSumCount(searchFilter)
		result = literatureQueryService.litJubOncAltSumData(searchFilter, params)
		def rows = []
		for (summary in result) {
			def values = [dataType:summary.dataType,
			              alterationType:summary.alterationType,
			              totalFrequency:summary.totalFrequency,
			              totalAffectedCases:summary.totalAffectedCases,
			              summary:summary.summary,
			              target:summary.target,
			              variant:summary.variant,
			              diseaseSite:summary.diseaseSite]
			rows.add(values)
		}

		return [count: resultCount, rows:rows]

	}

	def jubSummaryJSON = {
		def result = createJubSummary()
        render(text:params.callback + "(" + (result as JSON) + ")", contentType:"application/javascript")
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

	def createLitSheet(sheetName, tableName, tableCols, results) {

		def dataCols = new LinkedHashSet() // all the null column names from each result set.
		def dataRows = []

		for (record in results) {
			def row = [:]
			def refRecord = record.reference
			for (col in litRefDataColumns) {
				def value = refRecord[col]
				if (value != null && value.size() > 0) {
					dataCols.add("LiteratureReferenceData." + col)
					row.put("LiteratureReferenceData." + col, value)
				}
			}
			for (col in tableCols) {
				def value = record[col]
				if (value != null && value.size() > 0) {
					dataCols.add(tableName + "." + col)
					row.put(tableName + "." + col, value)
				}
			}
			if (tableName != "LiteratureInhibitorData" && record.inVivoModel != null) {
				for (col in litModelDataColumns) {
					def value = record.inVivoModel[col]
					if (value != null && value.size() > 0) {
						dataCols.add("LiteratureModelData.InVivo." + col)
						row.put("LiteratureModelData.InVivo." + col, value)
					}
				}
			}
			if (tableName != "LiteratureInhibitorData" && record.inVitroModel != null) {
				for (col in litModelDataColumns) {
					def value = record.inVitroModel[col]
					if (value != null && value.size() > 0) {
						dataCols.add("LiteratureModelData.InVitro." + col)
						row.put("LiteratureModelData.InVitro." + col, value)
					}
				}
			}
			if (tableName == "LiteratureAlterationData" && record.assocMoleculeDetails?.size() > 0) {
				for (amdRecord in record.assocMoleculeDetails) {
					def amdRow = [:]
					amdRow.putAll(row)
					for (col in litAMDDataColumns) {
						def value = amdRecord[col]
						if (value != null && value.size() > 0) {
							dataCols.add("LiteratureAssocMoleculeDetailsData." + col)
							row.put("LiteratureAssocMoleculeDetailsData." + col, value)
						}
					}
					dataRows.add(row)
				}
			} else {
				dataRows.add(row)
			}
		}

		if (dataRows.size() == 0) {
			return null
		}

		// Only use non-null columns, but put them in order based on the static column lists defined above.
		def cols = []
		def orderedCols = []
		for (col in litRefDataColumns) {
			def orderedCol = "LiteratureReferenceData." + col
			if (dataCols.contains(orderedCol)) {
				cols.add(message(code:orderedCol, default:orderedCol))
				orderedCols.add(orderedCol)
			}
		}
		for (col in tableCols) {
			def orderedCol = tableName + "." + col
			if (dataCols.contains(orderedCol)) {
				cols.add(message(code:orderedCol, default:orderedCol))
				orderedCols.add(orderedCol)
			}
		}
		if (tableName != "LiteratureInhibitorData") {
			for (col in litModelDataColumns) {
				def orderedCol = "LiteratureModelData.InVivo." + col
				if (dataCols.contains(orderedCol)) {
					cols.add("In Vivo " + message(code:"LiteratureModelData." + col, default:col))
					orderedCols.add(orderedCol)
				}
			}
			for (col in litModelDataColumns) {
				def orderedCol = "LiteratureModelData.InVitro." + col
				if (dataCols.contains(orderedCol)) {
					cols.add("In Vitro " + message(code:"LiteratureModelData." + col, default:col))
					orderedCols.add(orderedCol)
				}
			}
		}
		if (tableName == "LiteratureAlterationData") {
			for (col in litAMDDataColumns) {
				def orderedCol = "LiteratureAssocMoleculeDetailsData." + col
				if (dataCols.contains(orderedCol)) {
					cols.add(message(code:orderedCol, default:orderedCol))
					orderedCols.add(orderedCol)
				}
			}
		}

		def rows = []
		for (dataRow in dataRows) {
			def row = []
			for (col in orderedCols) {
				def value = dataRow[col]
				row.add(value)
			}
			rows.add(row)
		}

		return new ExcelSheet(name:sheetName, headers:cols, values:rows)

	}

	def downloadJubData = {
		def searchFilter = session.searchFilter
		def sheet
		def results
		def sheets = []
		params.offset = 0

		params.max = literatureQueryService.litJubOncAltCount(searchFilter)
		results = literatureQueryService.litJubOncAltData(searchFilter, params)
		sheet = createLitSheet("Jub Onc Alterations", "LiteratureAlterationData", litAltDataColumns, results)
		if (sheet != null) {
			sheets.add(sheet)
		}

		params.max = literatureQueryService.litJubOncInhCount(searchFilter)
		results = literatureQueryService.litJubOncInhData(searchFilter, params)
		sheet = createLitSheet("Jub Onc Inhibitors", "LiteratureInhibitorData", litInhDataColumns, results)
		if (sheet != null) {
			sheets.add(sheet)
		}

		params.max = literatureQueryService.litJubOncIntCount(searchFilter)
		results = literatureQueryService.litJubOncIntData(searchFilter, params)
		sheet = createLitSheet("Jub Onc Interactions", "LiteratureInteractionData", litIntDataColumns, results)
		if (sheet != null) {
			sheets.add(sheet)
		}

		params.max = literatureQueryService.litJubAsthmaAltCount(searchFilter)
		results = literatureQueryService.litJubAsthmaAltData(searchFilter, params)
		sheet = createLitSheet("Jub Asthma Alterations", "LiteratureAlterationData", litAltDataColumns, results)
		if (sheet != null) {
			sheets.add(sheet)
		}

//		params.max = literatureQueryService.litJubAsthmaInhCount(searchFilter)
//		results = literatureQueryService.litJubAsthmaInhData(searchFilter, params)
//		sheet = createLitSheet("Jub Asthma Inhibitors", "LiteratureInhibitorData", litInhDataColumns, results)
//		if (sheet != null) {
//			sheets.add(sheet)
//		}

		params.max = literatureQueryService.litJubAsthmaIntCount(searchFilter)
		results = literatureQueryService.litJubAsthmaIntData(searchFilter, params)
		sheet = createLitSheet("Jub Asthma Interactions", "LiteratureInteractionData", litIntDataColumns, results)
		if (sheet != null) {
			sheets.add(sheet)
		}

		params.max = literatureQueryService.litJubAsthmaPECount(searchFilter)
		results = literatureQueryService.litJubAsthmaPEData(searchFilter, params)
		sheet = createLitSheet("Jub Asthma Protein Effects", "LiteratureProteinEffectData", litPEDataColumns, results)
		if (sheet != null) {
			sheets.add(sheet)
		}

		if (sheets.size() > 0) {
			def gen = new ExcelGenerator()
			response.setHeader("Content-Type", "application/vnd.ms-excel; charset=utf-8")
			response.setHeader("Content-Disposition", "attachment; filename=\"literature.xls\"")
			response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
			response.setHeader("Pragma", "public");
			response.setHeader("Expires", "0");
			response.outputStream << gen.generateExcel(sheets);
		}
		// TODO: Display error message if there is no data.

	}

//	def downloadJubSummary = {
//
//		def cols = ["Datatype",
//		            "Alteration Type",
//		            "Frequency",
//		            "Affected Cases",
//		            "Summary",
//		            "Target Name",
//		            "Variant Name",
//		            "Disease",
//		            "Numerator",
//		            "Denominator"]
//		def result = createJubSummary()
//
//		def sheets = []
//		sheets.add(new ExcelSheet(name:"Jubilant Summary", headers:cols, values:result.rows))
//
//		def gen = new ExcelGenerator()
//		response.setHeader("Content-Type", "application/vnd.ms-excel; charset=utf-8")
//		response.setHeader("Content-Disposition", "attachment; filename=\"jubilantsummary.xls\"")
//		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
//		response.setHeader("Pragma", "public");
//		response.setHeader("Expires", "0");
//		response.outputStream << gen.generateExcel(sheets);
//
//	}

    //	 Call the ResNetService to create the ResNet .rnef file
    def downloadresnet = {
		def misses = "No results found"
	    jubilantResNetService.searchFilter = session.searchFilter
	    JAXBContext context = JAXBContext.newInstance(Batch.class);
	    Marshaller m = context.createMarshaller();
	    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    Batch b = jubilantResNetService.createResNet()
	    if (b != null)	{
	        misses = "Misses: " + jubilantResNetService.misses
	        response.setHeader("Content-Disposition", "attachment; filename=\"resnetexport.rnef\"")
	        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
	        response.setHeader("Pragma", "public");
	        response.setHeader("Expires", "0");
	        m.marshal(b, response.getWriter())
	    } else	{
	        render(template:'noResult')

	    }

	    def al = new AccessLog(username: springSecurityService.getPrincipal().username,
	            event:"Export ResNet", eventmessage:misses, accesstime:new Date())
	    al.save();
    }
}
