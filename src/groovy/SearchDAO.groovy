import org.apache.log4j.Logger
import org.springframework.context.ApplicationContext

class SearchDAO {
	ApplicationContext ctx = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getAttribute(org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes.APPLICATION_CONTEXT)
	def dataSource = ctx.getBean('dataSource')
	def grailsApplication = ctx.getBean('grailsApplication')
	
	static Logger log = Logger.getLogger(SearchDAO.class)
	
	def getGwasData()
	{
		def results = bio.BioAssayAnalysisGwas.executeQuery("""
					SELECT	gwas.rsId,
							gwas.pValue,
							gwas.logPValue 
					FROM	bio.BioAssayAnalysisGwas gwas
					
					""")
		//JOIN 	bio.BioAssayAnalysisDataExt analysisExt
		
		results = bio.BioAssayAnalysisDataExt.executeQuery("SELECT BAADE.id FROM bio.BioAssayAnalysisDataExt BAADE")
		
		return results
	}
}