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
  

/**
* $Id: IdentityVaultAuthenticationFilter.groovy
* @author $Author: Sai Munikuntla 
* 
*/
package com.recomdata.security

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import org.joda.time.DateTime;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.filter.GenericFilterBean

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class IdentityVaultAuthenticationFilter extends GenericFilterBean	{

	static Logger log = Logger.getLogger(IdentityVaultAuthenticationFilter.class)

	def authenticationManager
	def springSecurityService
	def userDetailsService	
	def redirectStrategy	

	private String filterProcessesUrl

	/**
	 * Ensure the correct beans are specified
	 */
	void afterPropertiesSet() {
		assert authenticationManager != null, 'authenticationManager must be specified'
		assert springSecurityService != null, 'springSecurityService must be specified'
		assert userDetailsService != null, 'userDetailsService must be specified'
		assert filterProcessesUrl != null, 'filterProcessesUrl must be specified'
		assert redirectStrategy != null, 'redirectStrategy must be specified'		
	}

	/**
	 * Main method to handle the filter request
	 * Check to see if this is from Identity Vault (/saml/sso) and if so attempt to communicate 
	 * with the Identity Vault to get the WWID
	 * 
	 * @param req the HTTP request
	 * @param res the HTTP response 
	 * @param chain the filter chain 
	 */
	void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req
		HttpServletResponse response = (HttpServletResponse) res

		OutputStreamWriter out = null

		if (requiresAuthentication(request, response)) {
			if (log.isDebugEnabled()) {
				log.debug("Attempting to authenticate the user with the Identity Vault")
			}						

			/**
			 * get "artifact" and "target" and then setup userPass based on environment
			 * 
			 * Prepare a SOAP call to the SAML Server using URL and then get HttpURLConnection from it
			 * 
			 * Get the WWID from the SOAP message using samlp:StatusCode and saml:NameIdentifier
			 * 
			 * Load the user details based on WWID by using the userDetailsService of Spring Security = AuthUserDetailsService.loadUserByWWID(wwid)
			 * 
			 * After the user is found re-authenticate using springSecurityService
			 */
			def userLanding = CH.config.grails.plugins.springsecurity.successHandler.defaultTargetUrl			
			redirectStrategy.sendRedirect(request, response, userLanding)
		}	else	{
			chain.doFilter(request, response)
		}		
	}	

	/**
	 * Indicates whether this filter should attempt to process the authentication information from the Identity Vault
 	 * <p>
	 * It strips any parameters from the "path" section of the request URL (such
	 * as the jsessionid parameter in
	 * <em>http://host/myapp/index.html;jsessionid=blah</em>) before matching
	 * against the <code>filterProcessesUrl</code> property.
	 * <p>
	 *
	 * @return <code>true</code> if the filter should attempt authentication, <code>false</code> otherwise.
	 */
     protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
		 String uri = request.getRequestURI();
		 int pathParamIndex = uri.indexOf(';');

		 if (pathParamIndex > 0) {
			 // strip everything after the first semi-colon
			 uri = uri.substring(0, pathParamIndex);
			 if (log.isDebugEnabled())	{
				 log.debug("Stripped out everything after the first semi-colon: ${uri}")
			 }
		 }

		 if ("".equals(request.getContextPath())) {
			 return uri.endsWith(filterProcessesUrl);
		 }
		 return uri.endsWith(request.getContextPath() + filterProcessesUrl);
	 }

	 /**
	  * Setter for the filterProcessesUrl
	  * 
	  * @param filterProcessesUrl
	  */
	 public void setFilterProcessesUrl(String filterProcessesUrl) {
		 this.filterProcessesUrl = filterProcessesUrl
	 }
}