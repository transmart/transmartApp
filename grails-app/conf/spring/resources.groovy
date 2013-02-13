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
  

import org.springframework.security.web.authentication.session.ConcurrentSessionControlStrategy
import org.springframework.security.web.session.ConcurrentSessionFilter
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.DefaultRedirectStrategy

import com.recomdata.transmart.data.export.ClinicalDataService;
import com.recomdata.transmart.data.export.PostgresClinicalDataService;
import com.recomdata.transmart.data.export.PostgresDataCountService;
import com.recomdata.transmart.data.export.PostgresExportService;
import com.recomdata.transmart.data.export.PostgresGeneExpressionDataService;
import com.recomdata.transmart.data.export.PostgresSnpDataService;

import I2b2HelperService;
import PostgresI2b2HelperService;

import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.apache.commons.dbcp.BasicDataSource
import org.codehaus.groovy.grails.orm.hibernate.ConfigurableLocalSessionFactoryBean
import grails.spring.BeanBuilder

beans = {
	dataSourcePlaceHolder(com.recomdata.util.DataSourcePlaceHolder){
		dataSource = ref('dataSource')
	}
	sessionRegistry(SessionRegistryImpl)
	sessionAuthenticationStrategy(ConcurrentSessionControlStrategy, sessionRegistry) {
		maximumSessions = 10
	}
	concurrentSessionFilter(ConcurrentSessionFilter){
		sessionRegistry = sessionRegistry
		expiredUrl = '/login'
	}
	userDetailsService(com.recomdata.security.AuthUserDetailsService)
	redirectStrategy(DefaultRedirectStrategy)
	
	/*if (isOracleConfigured())
	{
		log.debug("Oracle configured")
		clinicalDataService(ClinicalDataService)
		i2b2HelperService(I2b2HelperService)
	}
	else
	{*/
	
		// TODO -- NEEDS TO BE REVIEWED
	
		utilService(com.recomdata.transmart.util.UtilService)
		fileDownloadService(com.recomdata.transmart.util.FileDownloadService)

		springSecurityService(grails.plugins.springsecurity.SpringSecurityService){bean ->
			authenticationTrustResolver = ref('authenticationTrustResolver')
			grailsApplication = ref('grailsApplication')
			passwordEncoder = ref('passwordEncoder')
			objectDefinitionSource = ref('objectDefinitionSource')
			userDetailsService = ref('userDetailsService')
			userCache = ref('userCache')
		}
		snpService(SnpService)
		plinkService(PlinkService)
		conceptService(ConceptService)
		sampleInfoService(SampleInfoService)
	
		// --
	
		log.debug("Postgres configured")
		
		dataCountService(PostgresDataCountService){bean ->
			dataSource = ref('dataSource')
		}
		geneExpressionDataService(PostgresGeneExpressionDataService){bean ->
			dataSource = ref('dataSource')
			grailsApplication = ref('grailsApplication')
			i2b2HelperService = ref('i2b2HelperService')
			springSecurityService = ref('springSecurityService')
			fileDownloadService = ref ('fileDownloadService')
			utilService = ref('utilService')
		}
		snpDataService(PostgresSnpDataService){bean ->
			dataSource = ref('dataSource')
			grailsApplication = ref('grailsApplication')
			i2b2HelperService = ref('i2b2HelperService')
			springSecurityService = ref('springSecurityService')
			fileDownloadService = ref ('fileDownloadService')
			utilService = ref('utilService')
			snpService = ref('snpService')
			plinkService = ref('plinkService')
		}
		clinicalDataService(PostgresClinicalDataService){bean ->
			dataSource = ref('dataSource')
			i2b2HelperService = ref('i2b2HelperService')
			springSecurityService = ref('springSecurityService')
			utilService = ref('utilService')
		}
		i2b2HelperService(PostgresI2b2HelperService){bean->
			dataSource = ref('dataSource')
			sessionFactory = ref('sessionFactory')
			conceptService = ref('conceptService')
			sampleInfoService = ref('conceptService')
		}
		exportService(PostgresExportService){bean->
			quartzScheduler = ref('quartzScheduler')
			grailsApplication = ref('grailsApplication')
			dataCountService = ref('dataCountService')
			geneExpressionDataService = ref('geneExpressionDataService')
			i2b2HelperService = ref('i2b2HelperService')
			i2b2ExportHelperService = ref('i2b2ExportHelperService')
			jobResultsService = ref('jobResultsService')
			asyncJobService = ref('asyncJobService')
			dataExportService = ref('dataExportService')
		}
	/*}*/
}

def isOracleConfigured()
{
	def locations = configurationLocations()

	for (loc in locations)
	{
		def config = openConfig(loc)
		if (config)
		{
			return config.dataSource.driverClassName ==~ /.*oracle.*/
		}
	}

	log.error("Could not find configuration files");
	return false;
}

def configurationLocations()
{
	def configLocations = ["/etc/transmart", "/usr/local/transmart", "/home/mkapoor/.grails/transmartConfig"]
	def env = System.getenv()
	def configOverride = env['TRANSMART_CONFIG']
	def locations

	return configOverride ? [configOverride] : configLocations
}

def openConfig(String dir)
{
	try
	{
		def config = new ConfigSlurper().parse(new File("${dir}/DataSource.groovy").toURL())
		return config
	}
	catch (Throwable e)
	{
		return null
	}
}