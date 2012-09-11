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
		//
		
		return results
	}
	
	def getGwasColumnNames()
	{
		
	}
	
	def getExtendedMetaData()
	{
		def results = bio.BioAssayAnalysisDataExt.executeQuery("""
					SELECT	ext.ext_data 
					FROM	bio.BioAssayAnalysisDataExt ext			

			""")	
		
		
	}
}