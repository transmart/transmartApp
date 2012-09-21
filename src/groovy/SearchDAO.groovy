import org.apache.log4j.Logger
import org.springframework.context.ApplicationContext

class SearchDAO {
	ApplicationContext ctx = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getAttribute(org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes.APPLICATION_CONTEXT)
	def dataSource = ctx.getBean('dataSource')
	def grailsApplication = ctx.getBean('grailsApplication')
	
	static Logger log = Logger.getLogger(SearchDAO.class)
	
	def getGwasData(analysisId)
	{
		def results = bio.BioAssayAnalysisGwas.executeQuery("""
					SELECT	gwas.rsId,
							gwas.pValue,
							gwas.logPValue,
							gwas.ext_data
					FROM	bio.BioAssayAnalysisGwas gwas
					WHERE	gwas.analysis.id = :parAnalaysisId
					""",[parAnalaysisId : analysisId],[max:100,offset:5])
		return results
	}
	
	def getGwasIndexData()
	{
		def results = bio.BioAssayAnalysisDataIdx.findAllByExt_type("GWAS", [sort:"display_idx",order:"asc"])
		
		return results
	}

	def getEqtlIndexData()
	{
		def results = bio.BioAssayAnalysisDataIdx.findAllByExt_type("EQTL", [sort:"display_idx",order:"asc"])
		
		return results
	}
	
	def getEqtlData(analysisId)
	{
		def results = bio.BioAssayAnalysisGwas.executeQuery("""
			SELECT	eqtl.rsId,
					eqtl.pValue,
					eqtl.logPValue,
					eqtl.ext_data
			FROM	bio.BioAssayAnalysisEqtl eqtl
			WHERE	gwas.analysis.id = :parAnalaysisId
			""",[parAnalaysisId : analysisId],[max:100,offset:5])
		
		return results
	}
	
}