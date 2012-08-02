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
	
	if (isOracleConfigured())
	{
		log.info("Oracle configured")
		clinicalDataService(ClinicalDataService)
	}
	else
	{
		log.info("Postgres configured")
		clinicalDataService(PostgresClinicalDataService)
	}
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
	def configLocations = ["/etc/transmart", "/usr/local/transmart"]
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