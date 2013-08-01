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
  
package org.transmart.searchapp

import org.transmart.biomart.Compound
import org.transmart.biomart.ConceptCode

import org.transmart.biomart.BioAssayPlatform;
import org.transmart.biomart.CellLine;
import org.transmart.searchapp.SearchKeyword
import com.recomdata.util.IDomainExcelWorkbook
import com.recomdata.util.ExcelSheet
import com.recomdata.util.ExcelGenerator
 
/**
 * GeneSignature domain class
 */
class GeneSignature implements Cloneable, IDomainExcelWorkbook {

	static def DOMAIN_KEY = "GENESIG"
	static def DISPLAY_TAG = "Gene Signature"

	// gene list version
	static def DOMAIN_KEY_GL = "GENELIST"
	static def DISPLAY_TAG_GL = "Gene List"

	Long id
	String name
	String description
	String uploadFile
	GeneSignatureFileSchema fileSchema
	ConceptCode foldChgMetricConceptCode
	ConceptCode analyticCatConceptCode
	String analyticCatOther
	BioAssayPlatform techPlatform
	String analystName
	ConceptCode normMethodConceptCode
	String normMethodOther
	ConceptCode analysisMethodConceptCode
	String analysisMethodOther
	boolean multipleTestingCorrection = false
	ConceptCode pValueCutoffConceptCode
	String uniqueId
	Date dateCreated
	AuthUser createdByAuthUser
	Date lastUpdated
	AuthUser modifiedByAuthUser
	String versionNumber
	boolean publicFlag = false
	boolean deletedFlag = false
	GeneSignature parentGeneSignature
	ConceptCode sourceConceptCode
	String sourceOther
	ConceptCode ownerConceptCode
	String stimulusDescription
	String stimulusDosing
	String treatmentDescription
	String treatmentDosing
	Compound treatmentCompound
	String treatmentProtocolNumber
	String pmIds
	ConceptCode speciesConceptCode
	ConceptCode speciesMouseSrcConceptCode
	String speciesMouseDetail
	ConceptCode tissueTypeConceptCode
	ConceptCode experimentTypeConceptCode
	CellLine experimentTypeCellLine
	String experimentTypeInVivoDescr
	String experimentTypeATCCRef

	static hasMany = [geneSigItems:GeneSignatureItem]

	static mapping = {
		table 'SEARCH_GENE_SIGNATURE'
		version false
		id generator:'sequence', params:[sequence:'SEQ_SEARCH_DATA_ID']
		geneSigItems sort: 'foldChgMetric'
		columns {
			id column:'SEARCH_GENE_SIGNATURE_ID'
			name column:'NAME'
			description column:'DESCRIPTION'
			uploadFile column:'UPLOAD_FILE'
			fileSchema column:'SEARCH_GENE_SIG_FILE_SCHEMA_ID'
			foldChgMetricConceptCode column:'FOLD_CHG_METRIC_CONCEPT_ID'
			analyticCatConceptCode column:'ANALYTIC_CAT_CONCEPT_ID'
			analyticCatOther column:'ANALYTIC_CAT_OTHER'
			techPlatform column:'BIO_ASSAY_PLATFORM_ID'
			analystName column:'ANALYST_NAME'
			normMethodConceptCode column:'NORM_METHOD_CONCEPT_ID'
			normMethodOther column:'NORM_METHOD_OTHER'
			analysisMethodConceptCode column:'ANALYSIS_METHOD_CONCEPT_ID'
			analysisMethodOther column:'ANALYSIS_METHOD_OTHER'
			multipleTestingCorrection column:'MULTIPLE_TESTING_CORRECTION'
			pValueCutoffConceptCode column:'P_VALUE_CUTOFF_CONCEPT_ID'
			uniqueId column:'UNIQUE_ID'
			dateCreated column:'CREATE_DATE'
			createdByAuthUser column:'CREATED_BY_AUTH_USER_ID'
			lastUpdated column:'LAST_MODIFIED_DATE'
			modifiedByAuthUser column:'MODIFIED_BY_AUTH_USER_ID'
			versionNumber column:'VERSION_NUMBER'
			publicFlag column:'PUBLIC_FLAG'
			deletedFlag column:'DELETED_FLAG'
			parentGeneSignature column:'PARENT_GENE_SIGNATURE_ID'
			sourceConceptCode column:'SOURCE_CONCEPT_ID'
			sourceOther column:'SOURCE_OTHER'
			ownerConceptCode column:'OWNER_CONCEPT_ID'
			stimulusDescription column:'STIMULUS_DESCRIPTION'
			stimulusDosing column:'STIMULUS_DOSING'
			treatmentDescription column:'TREATMENT_DESCRIPTION'
			treatmentDosing column:'TREATMENT_DOSING'
			treatmentCompound column:'TREATMENT_BIO_COMPOUND_ID'
			treatmentProtocolNumber column:'TREATMENT_PROTOCOL_NUMBER'
			pmIds column:'PMID_LIST'
			speciesConceptCode column:'SPECIES_CONCEPT_ID'
			speciesMouseSrcConceptCode column:'SPECIES_MOUSE_SRC_CONCEPT_ID'
			speciesMouseDetail column:'SPECIES_MOUSE_DETAIL'
			tissueTypeConceptCode column:'TISSUE_TYPE_CONCEPT_ID'
			experimentTypeConceptCode column:'EXPERIMENT_TYPE_CONCEPT_ID'
			experimentTypeCellLine column:'EXPERIMENT_TYPE_CELL_LINE_ID'
			experimentTypeInVivoDescr column:'EXPERIMENT_TYPE_IN_VIVO_DESCR'
			experimentTypeATCCRef column:'EXPERIMENT_TYPE_ATCC_REF'
		}
	}

	static constraints = {
		uploadFile(maxSize:255)
		analyticCatConceptCode(nullable:true)
		analyticCatOther(nullable:true, maxSize:255)
		analystName(nullable:true, maxSize:100)
		normMethodConceptCode(nullable:true)
		normMethodOther(nullable:true, maxSize:255)
		analysisMethodConceptCode(nullable:true)
		analysisMethodOther(nullable:true, maxSize:255)
		name(maxSize:100)
		description(nullable:true, maxSize:1000)
		uniqueId(nullable:true, maxSize:50)
		lastUpdated(nullable:true)
		modifiedByAuthUser(nullable:true)
		versionNumber(nullable:true, maxSize:50)
		parentGeneSignature(nullable:true)
		sourceConceptCode(nullable:true)
		sourceOther(nullable:true, maxSize:255)
		ownerConceptCode(nullable:true)
		stimulusDescription(nullable:true, maxSize:1000)
		stimulusDosing(nullable:true, maxSize:255)
		treatmentDescription(nullable:true, maxSize:1000)
		treatmentDosing(nullable:true, maxSize:255)
		treatmentCompound(nullable:true)
		treatmentProtocolNumber(nullable:true, maxSize:50)
		pmIds(nullable:true, maxSize:255)
		speciesMouseSrcConceptCode(nullable:true)
		speciesMouseDetail(nullable:true, maxSize:255)
		tissueTypeConceptCode(nullable:true)
		experimentTypeConceptCode(nullable:true)
		experimentTypeCellLine(nullable:true)
		experimentTypeInVivoDescr(nullable:true, maxSize:255)
		experimentTypeATCCRef(nullable:true, maxSize:255)
	}

	/**
	 * event called before an insert
	 */
	def beforeInsert = {
		dateCreated = new Date()
	}

	/**
	 * event called before an update
	 */
	def beforeUpdate = {
		lastUpdated = new Date()
		uniqueId = DOMAIN_KEY+":"+id
	}

	def updateUniqueId(){
		setUniqueId(DOMAIN_KEY+":"+id);
	}

	/**
	 * parse comma separated Ids into a list
	 */
	def getPmIdsAsList(){
		List pmidList = new ArrayList();

		if(pmIds==null) return pmidList;

		// parse into tokens
		StringTokenizer st = new StringTokenizer(pmIds,",");
		while(st.hasMoreTokens()) {
			pmidList.add(st.nextToken())
		}

		return pmidList;
	}

	/**
	 * cloneable interface implementation
	 */
	def clone() {

		// clone object using a map of params
		Map params = createParamMap();
		GeneSignature clone = new GeneSignature();
		clone.properties = params;
		return clone;
	}

	/**
	 * creat a Map with the properties and values for each property similar to a request map
	 */
	def createParamMap() {

		Map params = new HashMap();
		params.put("name",name)
		params.put("description",description)
		params.put("uploadFile",uploadFile)
		params.put("fileSchema.id",fileSchema?.id)
		params.put("foldChgMetricConceptCode.id",foldChgMetricConceptCode?.id)
		params.put("analyticCatConceptCode.id",analyticCatConceptCode?.id)
		params.put("analyticCatOther",analyticCatOther)
		params.put("techPlatform.id",techPlatform?.id)
		params.put("analystName",analystName)
		params.put("normMethodConceptCode.id",normMethodConceptCode?.id)
		params.put("normMethodOther",normMethodOther)
		params.put("analysisMethodConceptCode.id",analysisMethodConceptCode?.id)
		params.put("analysisMethodOther",analysisMethodOther)
		params.put("multipleTestingCorrection",multipleTestingCorrection)
		params.put("pValueCutoffConceptCode.id",pValueCutoffConceptCode?.id)
		params.put("uniqueId",uniqueId)
		params.put("publicFlag",publicFlag)
		params.put("deletedFlag",deletedFlag)
		//params.put("parentGeneSignature.id",parentGeneSignature?.id)
		params.put("sourceConceptCode.id",sourceConceptCode?.id)
		params.put("sourceOther",sourceOther)
		params.put("ownerConceptCode.id",ownerConceptCode?.id)
		params.put("stimulusDescription",stimulusDescription)
		params.put("stimulusDosing",stimulusDosing)
		params.put("treatmentDescription",treatmentDescription)
		params.put("treatmentDosing",treatmentDosing)
		params.put("treatmentCompound.id",treatmentCompound?.id)
		params.put("treatmentProtocolNumber",treatmentProtocolNumber)
		params.put("pmIds",pmIds)
		params.put("speciesConceptCode.id",speciesConceptCode?.id)
		params.put("speciesMouseSrcConceptCode.id",speciesMouseSrcConceptCode?.id)
		params.put("speciesMouseDetail",speciesMouseDetail)
		params.put("tissueTypeConceptCode.id",tissueTypeConceptCode?.id)
		params.put("experimentTypeConceptCode.id",experimentTypeConceptCode?.id)
		params.put("experimentTypeCellLine.id",experimentTypeCellLine?.id)
		params.put("experimentTypeInVivoDescr",experimentTypeInVivoDescr)
		params.put("experimentTypeATCCRef",experimentTypeATCCRef)
		params.put("createdByAuthUser.id",createdByAuthUser?.id)
		params.put("dateCreated",dateCreated)
		params.put("modifiedByAuthUser.id",modifiedByAuthUser?.id)
		params.put("lastUpdated",lastUpdated)
		params.put("versionNumber",versionNumber)
		return params;
	}

	/**
	 * copy properties from this instance to the specified object
	 */
	def copyPropertiesTo(GeneSignature gs) {
		gs.name=name
		gs.description=description
		gs.uploadFile=uploadFile
		gs.fileSchema=fileSchema
		gs.foldChgMetricConceptCode=foldChgMetricConceptCode
		gs.analyticCatConceptCode=analyticCatConceptCode
		gs.analyticCatOther=analyticCatOther
		gs.techPlatform=techPlatform
		gs.analystName=analystName
		gs.normMethodConceptCode=normMethodConceptCode
		gs.normMethodOther=normMethodOther
		gs.analysisMethodConceptCode=analysisMethodConceptCode
		gs.analysisMethodOther=analysisMethodOther
		gs.multipleTestingCorrection=multipleTestingCorrection
		gs.pValueCutoffConceptCode=pValueCutoffConceptCode
		gs.uniqueId=null
		gs.publicFlag=publicFlag
		gs.deletedFlag=deletedFlag
		//gs.parentGeneSignature=parentGeneSignature
		gs.sourceConceptCode=sourceConceptCode
		gs.sourceOther=sourceOther
		gs.ownerConceptCode=ownerConceptCode
		gs.stimulusDescription=stimulusDescription
		gs.stimulusDosing=stimulusDosing
		gs.treatmentDescription=treatmentDescription
		gs.treatmentDosing=treatmentDosing
		gs.treatmentCompound=treatmentCompound
		gs.treatmentProtocolNumber=treatmentProtocolNumber
		gs.pmIds=pmIds
		gs.speciesConceptCode=speciesConceptCode
		gs.speciesMouseSrcConceptCode=speciesMouseSrcConceptCode
		gs.speciesMouseDetail=speciesMouseDetail
		gs.tissueTypeConceptCode=tissueTypeConceptCode
		gs.experimentTypeConceptCode=experimentTypeConceptCode
		gs.experimentTypeCellLine= experimentTypeCellLine
		gs.experimentTypeInVivoDescr=experimentTypeInVivoDescr
		gs.experimentTypeATCCRef=experimentTypeATCCRef
		gs.createdByAuthUser=createdByAuthUser
		gs.dateCreated=dateCreated
		gs.modifiedByAuthUser=modifiedByAuthUser
		gs.lastUpdated=lastUpdated
		gs.versionNumber=versionNumber
	}

	/**
	 * create a workbook showing the details of this gene signature
	 */
	public byte[] createWorkbook() {

		def descr

		// gs sheet
		def headers=[]
		def values=[]

		// general section
		values.add(["1) General Info"])
		values.add([])
		values.add(["Name:",name])
		values.add(["Description:",description])
		values.add(["Public?:",publicFlag ? "Public":"Private"])
		values.add(["Author:",createdByAuthUser?.userRealName])
		values.add(["Create Date:",dateCreated])
		values.add(["Modified By:",modifiedByAuthUser?.userRealName])
		values.add(["Modified Date:",modifiedByAuthUser!=null ? lastUpdated : ""])

		// meta section
		values.add([])
		values.add(["2) Meta-Data"])
		values.add([])

		descr = sourceConceptCode?.id==1 ? sourceOther : sourceConceptCode?.codeName
		values.add(["Source of list:",descr])

		values.add(["Owner of data:",ownerConceptCode?.codeName])

		values.add(["Stimulus>>"])
		values.add(["- Description:",stimulusDescription])
		values.add(["- Dose, units, and time:",stimulusDosing])

		values.add(["Treatment>>"])
		values.add(["- Description:",treatmentDescription])
		values.add(["- Dose, units, and time:",treatmentDosing])
		descr = ""
		if(treatmentCompound!=null) descr = treatmentCompound?.codeName + ' [' + treatmentCompound?.genericName + ' / ' + treatmentCompound?.brandName +']'
		values.add(["- Compound:",descr])
		values.add(["- Protocol Number:",treatmentProtocolNumber])

		values.add(["PMIDs (comma separated):",pmIds])

		values.add(["Species:",speciesConceptCode?.codeName])
		if(speciesMouseSrcConceptCode!=null) values.add(["- Mouse Source:",speciesMouseSrcConceptCode?.codeName])
		if(speciesMouseDetail!=null) values.add(["- knockout/transgenic' or 'other' mouse strain:",speciesMouseDetail])

		descr = ""
		if(techPlatform!=null) descr = techPlatform?.vendor + ' - ' + techPlatform?.array + ' [' + techPlatform?.accession + ']'
		values.add(["Technology Platform:",descr])

		values.add(["Tissue Type:",tissueTypeConceptCode?.codeName])

		values.add(["Experiment Info>>"])
		values.add(["- Type:",experimentTypeConceptCode?.codeName])
		if(experimentTypeCellLine!=null) values.add(["- Established Cell Line:",experimentTypeCellLine.cellLineName])
		if(experimentTypeConceptCode?.bioConceptCode=='IN_VIVO_ANIMAL' || experimentTypeConceptCode?.bioConceptCode=='IN_VIVO_HUMAN') values.add(["- 'in vivo' model:",experimentTypeInVivoDescr])
		values.add(["- ATCC Designation:",experimentTypeATCCRef])

		// analysis section
		values.add([])
		values.add(["3) Analysis Meta-Data"])
		values.add([])
		values.add(["Analysis Performed By:",analystName])

		descr = normMethodConceptCode?.id==1 ? normMethodOther : normMethodConceptCode?.codeName
		values.add(["Normalization Method:",descr])

		descr = analyticCatConceptCode?.id==1 ? analyticCatOther : analyticCatConceptCode?.codeName
		values.add(["Analytic Category:",descr])

		descr = analysisMethodConceptCode?.id==1 ? analysisMethodOther : analysisMethodConceptCode?.codeName
		values.add(["Analysis Method:",descr])

		values.add(["Multiple Testing Correction?", (multipleTestingCorrection!=null) ? (multipleTestingCorrection==1?"Yes":"No") : ""])
		values.add(["P-value Cutoff:",pValueCutoffConceptCode?.codeName])
		values.add(["Fold-change metric:",foldChgMetricConceptCode?.codeName])
		values.add(["Original upload file:",uploadFile])
		
		def metaSheet = new ExcelSheet("Gene Signature Info", headers, values);
		
		values=[]
		
		//This is a quick fix. These booleans will tell us whether a gene signature was entered with probes or genes. In the future we should add some indicator field to the "gene" list to say what it is made of. 
		Boolean hasGenes = false;
		Boolean hasProbes = false;
		
		geneSigItems.each 
		{
			if(it.bioMarker != null)
			{
				hasGenes = true;
				values.add([it.bioMarker.name, it.foldChgMetric])
			}
			else if(it.probeset != null)
			{
				hasProbes = true;
				values.add([it.probeset.name, it.foldChgMetric])
			}
		}
		
		String itemName = ""
		
		if(hasGenes) itemName = "Gene Symbol"
		if(hasProbes) itemName = "Probe ID"
		
		// items sheet
		headers=[itemName,"Fold Change Metric"]
				
		def itemsSheet = new ExcelSheet("Gene Signature Items", headers, values);

		// return Excel bytes
		return ExcelGenerator.generateExcel([metaSheet, itemsSheet])
	}
}
