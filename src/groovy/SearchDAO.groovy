import org.apache.log4j.Logger
import org.springframework.context.ApplicationContext

class SearchDAO {
	ApplicationContext ctx = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getAttribute(org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes.APPLICATION_CONTEXT)
	def dataSource = ctx.getBean('dataSource')
	def grailsApplication = ctx.getBean('grailsApplication')
	
	static Logger log = Logger.getLogger(SearchDAO.class)
	
	def getGwasData(analysisId) {
		getGwasData(analysisId, null)
	}
	
	def getGwasData(analysisId, ranges)
	{
		def queryParams = [parAnalysisId: analysisId]
		StringBuilder qb = new StringBuilder("""
					SELECT	gwas.rsId,
							gwas.pValue,
							gwas.logPValue,
							gwas.ext_data
					FROM	bio.BioAssayAnalysisGwas gwas
					WHERE	gwas.analysis.id = :parAnalysisId""")
		
		if (ranges) {
			qb.append(" AND gwas.rsId IN (:parSearchProbes)");
		}
		def results = bio.BioAssayAnalysisGwas.executeQuery(qb.toString(), queryParams,[max:100])
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
	
	def getEqtlData(analysisId) {
		getEqtlData(analysisId, null)
	}
	
	def getEqtlData(analysisId, searchProbes)
	{
		def results = bio.BioAssayAnalysisGwas.executeQuery("""
			SELECT	eqtl.rsId,
					eqtl.pValue,
					eqtl.logPValue,
					eqtl.ext_data
			FROM	bio.BioAssayAnalysisEqtl eqtl
			WHERE	eqtl.analysis.id = :parAnalaysisId
			""",[parAnalaysisId : analysisId],[max:100])
		
		return results
	}
	
}