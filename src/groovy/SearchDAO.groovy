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
							gwas.logPValue,
							ext.ext_data
					FROM	bio.BioAssayAnalysisGwas gwas
					JOIN	gwas.bioAssayAnalysisDataExts ext
					""",[max:100,offset:5])
		return results
	}
	
	def getGwasIndexData()
	{
		def results = bio.BioAssayAnalysisDataIdx.findAllByExt_type("GWAS", [sort:"display_idx",order:"asc"])
		
		return results
	}
	
	def getExtendedMetaData()
	{
		def results = bio.BioAssayAnalysisDataExt.executeQuery("""
					SELECT	ext.ext_data 
					FROM	bio.BioAssayAnalysisDataExt ext			

			""")	
		
		
	}
}