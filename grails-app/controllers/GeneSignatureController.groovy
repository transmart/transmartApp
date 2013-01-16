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
  

import javax.servlet.ServletOutputStream

import org.transmart.biomart.BioAssayPlatform;
import org.transmart.biomart.CellLine;
import org.transmart.searchapp.AccessLog;
import org.transmart.searchapp.AuthUser;

import org.transmart.searchapp.GeneSignature
import org.transmart. searchapp.GeneSignatureFileSchema
import org.transmart.biomart.Compound
import org.transmart.biomart.ConceptCode

import com.recomdata.genesignature.FileSchemaException
import com.recomdata.genesignature.WizardModelDetails
import com.recomdata.util.DomainObjectExcelHelper

/**
 * Controller class for gene signature functionality
 */
class GeneSignatureController {

	// service injections
	def geneSignatureService
	def springSecurityService

	// concept code categories
	static def SOURCE_CATEGORY = "GENE_SIG_SOURCE"
	static def OWNER_CATEGORY = "GENE_SIG_OWNER"
	static def SPECIES_CATEGORY = "SPECIES"
	static def MOUSE_SOURCE_CATEGORY = "MOUSE_SOURCE"
	static def TISSUE_TYPE_CATEGORY = "TISSUE_TYPE"
	static def EXP_TYPE_CATEGORY = "EXPERIMENT_TYPE"
	static def ANALYTIC_TYPE_CATEGORY = "ANALYTIC_CATEGORY"
	static def NORM_METHOD_CATEGORY = "NORMALIZATION_METHOD"
	static def ANALYSIS_METHOD_CATEGORY = "ANALYSIS_METHOD"
	static def P_VAL_CUTOFF_CATEGORY = "P_VAL_CUTOFF"
	static def FOLD_CHG_METRIC_CATEGORY = "FOLD_CHG_METRIC"

	// session attributes
	static def WIZ_DETAILS_ATTRIBUTE = "wizDetails"

	//static def allowedMethods = [initWizard:'POST', create1:'POST', create2:'POST', create3:'POST', save:'POST', update:'POST']

	// map species param
	static mappings = {
		"/$species"(controller:"geneSignature", action:"cellLineLookup")
	}

	def index = {
		// track usage
		def al = new AccessLog(username: springSecurityService.getPrincipal().username, event:"GeneSignature-Summary", eventmessage:"Gene Signature summary page", accesstime:new Date())
		//log.info "saving Gene Signature access log"
		al.save();

		redirect(action:'list')
	}

	/**
	 * reset flash object before showing summary
	 */
	def refreshSummary = {
		flash.message = null
		redirect(action:'list')
	}

	/**
	 * summary page of permissioned gene signatures
	 */
	def list = {
		// reset
		session.setAttribute(WIZ_DETAILS_ATTRIBUTE, null)

		// logged in user
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
		def bAdmin = user.isAdmin()
		log.info "Admin? "+bAdmin

		// summary view
		def signatures = geneSignatureService.listPermissionedGeneSignatures(user.id, bAdmin);
		def ctMap = geneSignatureService.getPermissionedCountMap(user.id, bAdmin)

		// break into owned and public
		def myItems = []
		def pubItems = []

		signatures.each {
			if(user.id==it.createdByAuthUser.id) {
				myItems.add(it)
			} else {
				pubItems.add(it)
			}
		}

		render(view: "list", model:[user: user, adminFlag: bAdmin, myItems: myItems, pubItems: pubItems, ctMap: ctMap])
	}

	/**
	 * initialize session for the create gs wizard
	 */
	def createWizard = {
		// initialize session model data
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)

		// initialzize new gs inst
		def geneSigInst = new GeneSignature();
		geneSigInst.properties.createdByAuthUser = user;
		geneSigInst.properties.publicFlag = false;
		geneSigInst.properties.deletedFlag = false;

		// initialize session
		def newWizard = new WizardModelDetails(loggedInUser: user, geneSigInst: geneSigInst);
		session.setAttribute(WIZ_DETAILS_ATTRIBUTE, newWizard)

		redirect(action:'create1')
	}

	/**
	 * initialize session for the edit gs wizard
	 */
	def editWizard = {
		// initialize session model data
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)

		// load gs instance
		def geneSigInst = GeneSignature.get(params.id)
		def clone = geneSigInst.clone()
		clone.modifiedByAuthUser = user
		if(clone.experimentTypeCellLine.id==null) clone.experimentTypeCellLine=null	 // this is hack, don't know how to get around this!
		log.debug "experimentTypeCellLine: "+clone.experimentTypeCellLine+"; null? "+(clone.experimentTypeCellLine==null)

		// set onto session
		def newWizard = new WizardModelDetails(loggedInUser: user, geneSigInst: clone, wizardType: WizardModelDetails.WIZ_TYPE_EDIT, editId: geneSigInst.id);
		session.setAttribute(WIZ_DETAILS_ATTRIBUTE, newWizard)

		redirect(action:'edit1')
	}

	/**
	 * initialize session for the clone (essentially edit) gs wizard
	 */
	def cloneWizard = {
		// initialize session model data
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)

		// load gs inst to clone
		def geneSigInst = GeneSignature.get(params.id)
		def clone = geneSigInst.clone()
		clone.createdByAuthUser = user
		clone.modifiedByAuthUser = null;
		clone.name=clone.name+" (clone)"
		clone.description=clone.description+" (clone)"
		clone.publicFlag = false;
		clone.deletedFlag = false;
		clone.dateCreated = null;
		clone.lastUpdated = null;
		clone.versionNumber = null;
		clone.uniqueId = null;
		if(clone.experimentTypeCellLine.id==null) clone.experimentTypeCellLine=null	 // this is hack, don't know how to get around this!

		// set onto session
		def newWizard = new WizardModelDetails(loggedInUser: user, geneSigInst: clone, wizardType: WizardModelDetails.WIZ_TYPE_CLONE, cloneId: geneSigInst.id);
		session.setAttribute(WIZ_DETAILS_ATTRIBUTE, newWizard)

		// reset items
		redirect(action:'create1')
	}

	/**
	 * set the indicated gs public for access by everyone
	 */
	def makePublic = {
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
		def gsInst = GeneSignature.get(params.id)
		gsInst.modifiedByAuthUser = user
		geneSignatureService.makePublic(gsInst)

		flash.message = "GeneSignature '${gsInst.name}' was made public to everyone"
		redirect(action:list)
	}

	/**
	 * mark the indicated gs as deleted by setting deletedFlag as true
	 */
	def delete = {
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
		def gsInst = GeneSignature.get(params.id)
		gsInst.modifiedByAuthUser = user
		geneSignatureService.delete(gsInst)

		flash.message = "GeneSignature '${gsInst.name}' was marked as deleted"
		redirect(action:list)
	}

	/**
	 * detail view
	 */
	def show = {
		def gsInst = GeneSignature.read(params.id)
		render(template:'gene_sig_detail', model:[gs:gsInst])
	}

	def showDetail = {
		def gsInst = GeneSignature.read(params.id)
		render(view:'showDetail', model:[gs:gsInst])
	}

	def create1 = {
		// get wizard
		def wizard = session.getAttribute(WIZ_DETAILS_ATTRIBUTE)

		// bind params
		bindGeneSigData(params, wizard.geneSigInst)

		// load data for page 1
		loadWizardItems(1, wizard)

		render(view: "wizard1", model:[wizard:wizard])
	}

	def create2 = {
		// get wizard
		def wizard = session.getAttribute(WIZ_DETAILS_ATTRIBUTE)

		// bind params
		bindGeneSigData(params, wizard.geneSigInst)

		// load item data
		loadWizardItems(2, wizard)

		render(view: "wizard2", model:[wizard:wizard])
	}

	def create3 = {
		def wizard = session.getAttribute(WIZ_DETAILS_ATTRIBUTE)

		// bind params
		bindGeneSigData(params, wizard.geneSigInst)

		// load data for page 3
		loadWizardItems(3, wizard)

		render(view: "wizard3", model:[wizard: wizard])
	}

	/**
	 * edit gs in page 1 of wizard
	 */
	def edit1 = {
		// get wizard
		def wizard = session.getAttribute(WIZ_DETAILS_ATTRIBUTE)
		bindGeneSigData(params, wizard.geneSigInst)

		// load data for page 1
		loadWizardItems(1, wizard)

		render(view: "wizard1", model:[wizard:wizard])
	}

	/**
	 * edit gs in page 2 of wizard
	 */
	def edit2 = {
		// get wizard
		def wizard = session.getAttribute(WIZ_DETAILS_ATTRIBUTE)

		// save original file until final save
		def origFile = wizard.geneSigInst.uploadFile
		bindGeneSigData(params, wizard.geneSigInst)
		wizard.geneSigInst.uploadFile = origFile

		// load item data
		loadWizardItems(2, wizard)

		render(view: "wizard2", model:[wizard:wizard])
	}

	/**
	 * edit gs in page 3 of wizard
	 */
	def edit3 = {
		def wizard = session.getAttribute(WIZ_DETAILS_ATTRIBUTE)
		bindGeneSigData(params, wizard.geneSigInst)

		// load data for page 3
		loadWizardItems(3, wizard)

		render(view: "wizard3", model:[wizard: wizard])
	}

	/**
	 * save new gene signature domain and composition of gene signature items
	 */
	def save = {
		def wizard = session.getAttribute(WIZ_DETAILS_ATTRIBUTE)
		def gs = wizard.geneSigInst
		assert null == gs.properties.id

		// bind params
		bindGeneSigData(params, gs)

		// get file
		def file = request.getFile('uploadFile')

		// load file contents, if clone check for file presence
		boolean bLoadFile = (wizard.wizardType==WizardModelDetails.WIZ_TYPE_CREATE) || (wizard.wizardType==WizardModelDetails.WIZ_TYPE_CLONE && file!=null && file.getOriginalFilename()!="")
		if(!bLoadFile) file = null
		if(bLoadFile) {
			gs.properties.uploadFile = file.getOriginalFilename()

			// check for empty file
			if(file.empty) {
				flash.message = "The file:'${gs.properties.uploadFile}' you uploaded is empty"
				return render(view: "wizard3", model:[wizard: wizard])
			}

			// validate file format
			def metricType = gs.foldChgMetricConceptCode?.bioConceptCode
			def schemaColCt = gs.fileSchema?.numberColumns

			try {
				geneSignatureService.verifyFileFormat(file, schemaColCt, metricType)
			} catch (FileSchemaException e) {
				flash.message = e.getMessage()
				return render(view: "wizard3", model:[wizard: wizard])
			}

		} else {
			// load items from cloned object
			GeneSignature parentGS = GeneSignature.get(wizard.cloneId)
			gs.properties.uploadFile = parentGS.uploadFile
			log.info "INFO: loading parent of clone '"+parentGS.name+"'"
			geneSignatureService.cloneGeneSigItems(parentGS, gs)
		}

		// good to go, call save service
		try {
			gs = geneSignatureService.saveWizard(gs, file)

			// clean up session
			wizard = null
			session.setAttribute(WIZ_DETAILS_ATTRIBUTE, wizard)

			// send message to user
			flash.message = "GeneSignature '${gs.name}' was created on: ${gs.dateCreated}"
			redirect(action:'list')

		} catch (FileSchemaException fse) {
			flash.message = fse.getMessage()
			render(view: "wizard3", model:[wizard: wizard])
		} catch (RuntimeException re) {
			flash.message = "Runtime exception "+re.getClass().getName()+":<br>"+re.getMessage()
			render(view: "wizard3", model:[wizard: wizard])
		}
	}

	/**
	 * update gene signature and the associated items (new file only)
	 */
	def update = {
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)

		// retrieve clone
		def wizard = session.getAttribute(WIZ_DETAILS_ATTRIBUTE)
		def clone = wizard.geneSigInst

		// bind params onto clone
		bindGeneSigData(params, clone)

		// load real domain object, apply edit params from clone
		def gsReal = GeneSignature.get(wizard.editId)
		def origFile = gsReal.uploadFile
		clone.copyPropertiesTo(gsReal)
		gsReal.modifiedByAuthUser=user
		gsReal.uploadFile = origFile

		// refresh items if new file uploaded
		def file = request.getFile('uploadFile')
		log.debug " update wizard file:'"+file?.getOriginalFilename()+"'"

		// file validation
		if(file!=null && file.getOriginalFilename()!="") {
			// empty?
			if(file.empty) {
				flash.message = flash.message = "The file:'${file.getOriginalFilename()}' you uploaded is empty"
				return render(view: "wizard3", model:[wizard: wizard])
			}

			// check schema errors
			def metricType = gsReal.foldChgMetricConceptCode?.bioConceptCode
			def schemaColCt = gsReal.fileSchema?.numberColumns
			try {
				geneSignatureService.verifyFileFormat(file, schemaColCt, metricType)
				gsReal.uploadFile = file.getOriginalFilename()

			} catch (FileSchemaException e) {
				flash.message = e.getMessage()
				return render(view: "wizard3", model:[wizard: wizard])
			}
		}

		// good to go, call update service
		try {
			geneSignatureService.updateWizard(gsReal, file)

			// clean up session
			wizard = null
			session.setAttribute(WIZ_DETAILS_ATTRIBUTE, wizard)

			// send message to user
			flash.message = "GeneSignature '${gsReal.name}' was updated on: ${gsReal.lastUpdated}"
			redirect(action:'list')

		} catch (FileSchemaException fse) {
			flash.message = fse.getMessage()
			render(view: "wizard3", model:[wizard: wizard])
		} catch (RuntimeException re) {
			flash.message = "Runtime exception "+re.getClass().getName()+":<br>"+re.getMessage()
			render(view: "wizard3", model:[wizard: wizard])
		}
	}

	/**
	 * edit screen for gs items
	 */
	def showEditItems = {
		def gs = GeneSignature.get(params.id)

		if(params.errorFlag!=null) {
			render(view: 'edit_items', model:[gs:gs, errorFlag:true])
		} else {
			render(view: 'edit_items', model:[gs:gs])
		}
	}

	/**
	 * delete the indicated gs items
	 */
	def deleteItems = {
	    def delParam = params.delete
	    def gs = GeneSignature.get(params.id)

		if(delParam==null || delParam=="") {
			flash.message = "<div class='warning'>You did not select any item(s) to delete</div>"
			return render(view: 'edit_items', model:[gs:gs, errorFlag:true])
		}

	    // if one id, request is a string
	    def delItems = []
	    if(delParam instanceof String) {
	    	delItems.add(delParam)
	    } else {
	    	delParam.each { delItems.add(it) }
	    }

		// delete indicated ids
		gs = geneSignatureService.deleteGeneSigItems(gs, delItems)

		// send user message
		flash.message = "<div class='message'>deleted " +delItems.size()+ " gene signature item(s)</div>"
		render(view: 'edit_items', model:[gs:gs])
	}

	/**
	 * add items to an existing gene signature
	 */
	 def addItems = {

		def gs = GeneSignature.get(params.id)
		log.debug " adding items to gs: "+gs.name

		// reset
		flash.message = null

		// extract symbols and value metrics
		List<String> geneSymbols = new ArrayList()
		List<Double> valueMetrics = new ArrayList()
		List<String> probes = new ArrayList()
		
		boolean bError = false
		def key
		def symbol
		def itemNum
		def valueMetric
		def param
		def probe

		// iterate through params
		Iterator iter = params.entrySet().iterator()
		while(iter.hasNext()) {
			param = iter.next()
			//log.info " eval param: "+param
			//println "PARAM: " + param
			
			key = param.getKey().trim()
			symbol = param.getValue().trim()
			
			// parse gene symbols
			if(key.startsWith("biomarker_") && symbol.length()>0) {
				itemNum = key.substring(10,key.length())
				valueMetric = params.get("foldChgMetric_"+itemNum)
				geneSymbols.add(symbol)
				println "Gene: " + symbol + "   FC: " + valueMetric

				log.debug " parsing symbol: '"+symbol+"' with valueMetric: "+valueMetric
				// parse fold chg metric
				if(valueMetric!=null && valueMetric.trim().length()>0) {
					try {
						valueMetrics.add(Double.valueOf(valueMetric))
					} catch (RuntimeException e) {
						log.error "invalid valueMetric: "+valueMetric+" detected!", e
						flash.message = "<div class='warning'>The value metric '"+valueMetric+"' for symbol: '"+symbol+"' is not a valid number</div>"
						bError = true
					}
				}
			}
			
			// parse probeset
			if(key.startsWith("probeset_") && symbol.length()>0) {
				itemNum = key.substring(9, key.length())
				valueMetric = params.get("foldChgMetric_"+itemNum)
				probes.add(symbol)
				
				log.debug " parsing symbol: '"+symbol+"' with valueMetric: "+valueMetric
				// parse fold chg metric
				if(valueMetric!=null && valueMetric.trim().length()>0) {
					try {
						valueMetrics.add(Double.valueOf(valueMetric))
					} catch (RuntimeException e) {
						log.error "invalid valueMetric: "+valueMetric+" detected!", e
						flash.message = "<div class='warning'>The value metric '"+valueMetric+"' for symbol: '"+symbol+"' is not a valid number</div>"
						bError = true
					}
				}
			}
		}

		// any symbols to add?
		if(!bError && (geneSymbols.size()==0) && (probes.size()==0)) {
			flash.message = "<div class='warning'>You did not enter any new item(s) to add</div>"
			bError=true
		}

		// valid fold chg for each gene or probeset?
		if(!bError && gs.foldChgMetricConceptCode?.bioConceptCode != GeneSignatureService.METRIC_CODE_GENE_LIST){
			if((gs.fileSchema?.id==3)&& probes.size()!=valueMetrics.size()) {
				flash.message = "<div class='warning'>You must enter a valid fold change metric for each new probeset</div>"
				bError=true
			}
		
			if((gs.fileSchema.id!=3)&& geneSymbols.size()!=valueMetrics.size()) {
				flash.message = "<div class='warning'>You must enter a valid fold change metric for each new gene symbol</div>"
				bError=true
			}
		}

		// add indicated items
		if(valueMetrics.size()==0) valueMetrics = null
			
		// add new items if no error
		if(!bError) {
			try {
				geneSignatureService.addGenSigItems(gs, geneSymbols, probes, valueMetrics)
				flash.message = "<div class='message'>"+geneSymbols.size()+ " gene signature item(s) were added to '"+gs.name+"'</div>"

			} catch (FileSchemaException fse) {
				log.error "message>> "+fse.getMessage(), fse
				flash.message = "<div class='warning'>"+fse.getMessage()+"</div>"
				bError = true
			} catch (RuntimeException re) {
				log.error "RuntimeException>>"+re.getMessage(), re
				flash.message = "<div class='warning'>Runtime exception "+re.getClass().getName()+":<br>"+re.getMessage()+"</div"
				bError = true
			}
		}

		// handle error
		if(bError) {
			// build params map from current params for redirect
			Map newParams = new HashMap();
			newParams.putAt("id",gs.id)
			newParams.putAt("errorFlag",true)

			// add new gene params
			def i = 1;
			geneSymbols.each {
				newParams.putAt("biomarker_"+i,it)
				i++
			}

			// add new fold chg params
			i = 1;
			if(valueMetrics!=null) {
				valueMetrics.each {
					newParams.putAt("foldChgMetric_"+i,it)
					i++
				}
			}
			log.debug " redirect params>> "+newParams
			redirect(action:showEditItems, params:newParams)
		} else {
			redirect(action:showEditItems, params:["id":gs.id])
		}
	}

	/**
	 * export gene signature to Excel
	 */
	def downloadExcel = {

		// download domain object
		def gs = GeneSignature.get(params.id)
		DomainObjectExcelHelper downloadHelper = new DomainObjectExcelHelper(gs,"gene_sig_"+gs.name+".xls")
		downloadHelper.downloadDomainObject(response)
	 }
	
	/**
	* export GMT: Gene Matrix Transposed file format (*.gmt)
	*/
    def downloadGMT = {
		def fileName = null
		// send content to response
		ServletOutputStream os = null;
		try {
			def content = geneSignatureService.getGeneSigGMTContent(params.id)
			fileName = 'gene_sig_'+content?.substring(0, content?.indexOf('\t'))?.replace('-', '')+'.gmt'
			// setup headers for download
			response.setContentType("application/vnd.gmt");
			response.setCharacterEncoding("charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=\""+fileName+"\"");
			response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
			response.setHeader("Pragma", "public");
			response.setHeader("Expires", "0");
			os = response.getOutputStream();
			os?.write(content.bytes);
		} finally {
			os?.flush();
		}
	}

	/**
	 * show sample upload files to user
	 */
	def showUploadSamples = {

		// set headers on output stream
		response.reset();
		response.setContentType("text/plain");
		response.setCharacterEncoding("charset=utf-8");
	    response.setHeader("Content-Disposition", "attachment; filename=\"gene_sig_samples.txt\"");
	    response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
	    response.setHeader("Pragma", "public");
		response.setHeader("Expires", "0");

		// send workbook to response
		ServletOutputStream os = null;
		try {
			os = response.outputStream;
			os.println("1) Gene List Example (no tab character, exclude fold change metric):");
			os.println("")
			os.println("TCN1")
			os.println("IL1RN")
			os.println("KIAA1199")
			os.println("G0S2")
			os.println("CXCL2")
			os.println("IL1RN")
			os.println("IL24")
			os.println("APOBEC3A")
			os.println("VNN3")
			os.println("DSG3")
			os.println("")
			os.println("")
			os.println("2) Gene Signature Example with actual fold change (separate columns with a tab):")
			os.println("")
			os.println("CXCL5\t-19.19385797")
			os.println("IL8RB\t-18.21493625")
			os.println("FPR1\t-17.6056338")
			os.println("FCGR3A\t-15.69858713")
			os.println("MMP3\t-15.31393568")
			os.println("CXCL6\t-14.7275405")
			os.println("BCL2A1\t-12.65822785")
			os.println("CXCL2\t-12.300123")
			os.println("SERPINB3\t-12.22493888")
			os.println("KYNU\t-10.76426265")
			os.println("")
			os.println("")
			os.println("3) Gene Signature Example for composite lists (separate columns with a tab):")
			os.println("")
			os.println("CXCL5\t-1")
			os.println("IL8RB\t-1")
			os.println("MMP3\t-1")
			os.println("SOD2\t0")
			os.println("PI3\t0")
			os.println("CDH3\t0")
			os.println("LAIR2\t1")
			os.println("ZBP1\t1")
			os.println("SPRR1B\t1")
			os.println("APOL1\t1")
		} finally {
			//os.flush();
		//	response.flushBuffer();
		}
	}

	/**
	 * retrieve cell lines for indicated species
	 */
	def cellLineLookup = {
		log.debug " params "+params
		def speciesId = params.id

		def cellLines = []
		if(speciesId==null || speciesId=="") {
			cellLines = CellLine.list([sort:"cellLineName"])
		} else {
			def species = ConceptCode.get(speciesId)
			def speciesFilter = species.codeName
			if(speciesFilter.indexOf("Mouse")!=-1) speciesFilter="Mouse"
			if(speciesFilter.indexOf("monkey")!=-1) speciesFilter="Monkey"

			// match on species
			log.debug " speciesFilter "+speciesFilter
			cellLines = CellLine.findAllBySpeciesIlike(speciesFilter+"%", [sort:"cellLineName"])
		}

		render(view:"cell_line_lookup", model:[cellLines: cellLines])
	}

	/**
	 * bind form parameters to GeneSignature domain instance
	 */
	void bindGeneSigData(Map params, GeneSignature gs) {
		// skip if page param not specified
		if(params.page==null) return

		long pageNum = Long.parseLong(params.page);

		switch (pageNum) {
			case 1:
				break;

			case 2:
				break;

			case 3:
				if (params.multipleTestingCorrection==null) {
                    params.multipleTestingCorrection = false
				}
				break;
		}

		// bind params
		bindData(gs, params)
		log.info "bound params from page "+pageNum+":\n"+params
	}

	/**
	 * load required data for wizard page
	 */
	void loadWizardItems(int pageNum, WizardModelDetails wizard) {

		switch (pageNum) {

			case 1:
			break;

			case 2:
			// 'other' concept code item
			def otherConceptItem = ConceptCode.get(1)

			// sources
			wizard.sources = ConceptCode.findAllByCodeTypeName(SOURCE_CATEGORY, [sort:"bioConceptCode"])
			if(otherConceptItem!=null)
				wizard.sources.add(otherConceptItem);
			//WizardModelDetails.addOtherItem(wizard.sources, "other")

			// owners
			wizard.owners = ConceptCode.findAllByCodeTypeName(OWNER_CATEGORY, [sort:"bioConceptCode"])

			// species
			wizard.species = ConceptCode.findAllByCodeTypeName(SPECIES_CATEGORY, [sort:"bioConceptCode"])

			// mouse sources
			wizard.mouseSources = ConceptCode.findAllByCodeTypeName(MOUSE_SOURCE_CATEGORY, [sort:"bioConceptCode"])

			// tissue types
			wizard.tissueTypes = ConceptCode.findAllByCodeTypeName(TISSUE_TYPE_CATEGORY, [sort:"bioConceptCode"])

			// experiment types
			wizard.expTypes = ConceptCode.findAllByCodeTypeName(EXP_TYPE_CATEGORY, [sort:"bioConceptCode"])

			// technology platforms
			def platforms = BioAssayPlatform.findAll("from BioAssayPlatform as p where p.vendor is not null order by p.vendor, p.array");
			BioAssayPlatform other = new BioAssayPlatform();
			other.accession="other"
			//platforms.add(other);
			wizard.platforms = platforms;

			// compounds
			wizard.compounds = Compound.findAll("from Compound c where c.brandName is not null or c.genericName is not null order by codeName");

			break;

			case 3:
			// 'other' concept code item
			def otherConceptItem = ConceptCode.get(1)

			// normalization methods
			wizard.normMethods = ConceptCode.findAllByCodeTypeName(NORM_METHOD_CATEGORY, [sort:"bioConceptCode"])
			wizard.normMethods.add(otherConceptItem);

			// analytic categories
			wizard.analyticTypes = ConceptCode.findAllByCodeTypeName(ANALYTIC_TYPE_CATEGORY, [sort:"bioConceptCode"])
			wizard.analyticTypes.add(otherConceptItem)

			// analysis methods
			wizard.analysisMethods = ConceptCode.findAllByCodeTypeName(ANALYSIS_METHOD_CATEGORY, [sort:"bioConceptCode"])
			wizard.analysisMethods.add(otherConceptItem);

			// file schemas
			wizard.schemas = GeneSignatureFileSchema.findAllBySupported(true, [sort:"name"])

			// p value cutoffs
			wizard.pValCutoffs = ConceptCode.findAllByCodeTypeName(P_VAL_CUTOFF_CATEGORY, [sort:"bioConceptCode"])

			// fold change metrics
			wizard.foldChgMetrics = ConceptCode.findAllByCodeTypeName(FOLD_CHG_METRIC_CATEGORY, [sort:"bioConceptCode"])
			break;

			default:
			log.warn "invalid page requested!"
		}
	}

	/**
	 * testing this concept - attaches GeneSignature domain object to hibernate session between requests in the wizard
	 */
	void mergeGeneSigInstToHibernate(WizardModelDetails wizard) {
		def gs = wizard.geneSigInst
		// merge the current domain instance into the persistence context with the wizard changes
		//if(!gs.isAttached()) wizard.geneSigInst = gs.merge()
		wizard.geneSigInst = gs.merge()
	}
}

