import org.springframework.util.StringUtils
import org.transmart.searchapp.ImportXnatConfiguration;
import org.transmart.searchapp.ImportXnatVariable;
import groovy.xml.MarkupBuilder

/**
 * ImportXnat controller.
 */
class ImportXnatController {

	def springSecurityService
	def grailsApplication

	// the delete, save and update actions only accept POST requests
	static Map allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

	def index = {
		redirect action: list, params: params
	}

	def list = {
		if (!params.max) {
			params.max = grailsApplication.config.com.recomdata.admin.paginate.max
		}
		[importXnatConfigurationList: ImportXnatConfiguration.list(params)]
	}

	def show = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		if (!importXnatConfiguration) {
			flash.message = "Import XNAT Configuration not found with id $params.id"
			redirect action:list
			return
		}
		[importXnatConfiguration: importXnatConfiguration]
	}

	def delete = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		if (!importXnatConfiguration) {
			flash.message = "Import XNAT Configuration not found with id $params.id"
			redirect action:list
			return
		}

		importXnatConfiguration.delete()

		flash.message = "Import XNAT Configuration $params.id deleted."
		redirect(action: list)
	}

	def delete_coupling = {
		def importXnatVariable = ImportXnatVariable.get(params.id)
		if (!importXnatVariable) {
			flash.message = "Import XNAT Variable not found with id $params.id"
			redirect action: create_coupling, id: params.configId
			return
		}

		importXnatVariable.delete()

		flash.message = "Variable $params.id deleted."
		redirect action: create_coupling, id: params.configId
	}

	def edit = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		if (!importXnatConfiguration) {
			flash.message = "Import XNAT Configuration not found with id $params.id"
			redirect(action: list)
			return
		}

		[importXnatConfiguration: importXnatConfiguration]
	}

	/**
	 * Update action, called when an existing Requestmap is updated.
	 */
	def update = {
		def importXnatConfiguration= ImportXnatConfiguration.get(params.id)
		if (!importXnatConfiguration) {
			flash.message = "Import XNAT Configuration not found with id $params.id"
			redirect(action: edit, id :params.id)
			return
		}

		long version = params.version.toLong()
		if (importXnatConfiguration.version > version) {
			importXnatConfiguration.errors.rejectValue 'version', "importxnatconfiguration.optimistic.locking.failure",
				"Another user has updated this Configuration while you were editing."
			render view: 'edit', model: [importXnatConfiguration: importXnatConfiguration]
			return
		}

		importXnatConfiguration.properties = params
		if (importXnatConfiguration.save()) {
			redirect action: show, id: importXnatConfiguration.id
		}
		else {
			render view: 'edit', model: [importXnatConfiguration: importXnatConfiguration]
		}
	}

	def create = {
		[importXnatConfiguration: new ImportXnatConfiguration(params)]
	}

	def create_coupling = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		def importXnatVariable = new ImportXnatVariable(params)

		if (!importXnatConfiguration) {
			flash.message = "Import XNAT Configuration not found with id $params.id"
			redirect(action: list)
			return
		}

		def importXnatVariableList = importXnatConfiguration.variables
		[importXnatVariable: importXnatVariable, importXnatVariableList: importXnatVariableList, importXnatConfiguration: importXnatConfiguration]
	}

	/**
	 * Save action, called when a new Requestmap is created.
	 */
	def save = {
		def importXnatConfiguration = new ImportXnatConfiguration(params)
		if (importXnatConfiguration.save()) {
			redirect action: create_coupling, id: importXnatConfiguration.id
		}
		else {
			render view: 'create', model: [importXnatConfiguration: importXnatConfiguration]
		}
	}
	
	def save_coupling = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		def importXnatVariable = new ImportXnatVariable(params)
		def importXnatVariableList = importXnatConfiguration.variables
		importXnatVariable.configuration = importXnatConfiguration

		if (!importXnatVariable.save()) {
			render view: 'create_coupling', model: [importXnatVariable: importXnatVariable, importXnatConfiguration: importXnatConfiguration, importXnatVariableList: importXnatVariableList]
		} else {
			redirect action: create_coupling, id: importXnatConfiguration.id
		}
	}

	def import_wizard = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		if (!importXnatConfiguration) {
			flash.message = "Import XNAT Configuration not found with id $params.id"
			redirect(action: list)
			return
		}

		[importXnatConfiguration: importXnatConfiguration]
	}

	def import_variables = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		
		def password = params.password
		if (password == "") {
			flash.message = "Please enter XNAT password"
			redirect action: import_wizard, id: importXnatConfiguration.id
		} else {
		
			export()

			def url = importXnatConfiguration.url
			def username = importXnatConfiguration.username
			def project = importXnatConfiguration.project
			def node = importXnatConfiguration.node

			def process = ("python " + getTransmartDataLocation() + "/samples/postgres/_scripts/xnattotransmartlink/downloadscript.py ${url} ${username} ${password} ${project} ${node}").execute(null, new File(getTransmartDataLocation() + "/samples/postgres/_scripts/xnattotransmartlink"))
			process.waitFor()
			if (process.err.text == "") {
				flash.message = "${process.in.text}"
			} else {
				flash.message = "${process.err.text}</br>${process.in.text}"
			}				
			redirect action: import_wizard, id: importXnatConfiguration.id
		}

		return
	}

	def export = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		def importXnatVariableList = importXnatConfiguration.variables
		def project = importXnatConfiguration.project
		def xmlFile = (getTransmartDataLocation() + "/samples/postgres/_scripts/xnattotransmartlink/${project}.xml")
		def writer = new FileWriter(new File(xmlFile))
		def xml = new MarkupBuilder(writer)

		xml.project(name: project) {
			variables {
				importXnatVariableList.each{item->
					variable(
						name: item.name,
						dataType: item.datatype,
						url: item.url
					)
				}
				
			}
		}
	}

	def downloadXml = {
		export();

		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		def importXnatVariable = new ImportXnatVariable(params)
		def importXnatVariableList = importXnatConfiguration.variables
		def project = importXnatConfiguration.project

		def xmlFile = (getTransmartDataLocation() + "/samples/postgres/_scripts/xnattotransmartlink/${project}.xml")
		def file = new File(xmlFile)
		response.setContentType("application/xml;charset='utf8'")
		response.setHeader("Content-disposition", "attachment;filename=${file.getName()}")
		response.outputStream << file.newInputStream()
		
		render view: 'create_coupling', model: [importXnatVariable: importXnatVariable, importXnatConfiguration: importXnatConfiguration, importXnatVariableList: importXnatVariableList]
	}

	def getTransmartDataLocation = {
		def dir = grailsApplication.config.org.transmart.data.location
		if (dir.isEmpty()) {
			dir = grailsAttributes.getApplicationContext().getResource("/").getFile().getParentFile().getParentFile().toString() + "/transmart-data"
		}
		return dir
	}
}
