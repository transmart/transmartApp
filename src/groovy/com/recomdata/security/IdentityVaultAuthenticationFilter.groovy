/**
* $Id: IdentityVaultAuthenticationFilter.groovy 8631 2011-06-20 17:38:55Z jboles $
* @author $Author: jboles $
* @version $Revision: 8631 $
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

			def artifact = request.getParameter("SAMLart")						
			def userPass = CH.config.com.recomdata.searchtool.identityVaultCredentials
			if (userPass == null || userPass.length() < 1)	{
				log.error("Identity Vault credentials are not set in the external configuration file, unable to login")				
			}
			
			URL soapURL = new URL("https://login.psso.its.jnj.com/nidp/saml/soap");
			URLConnection conn = soapURL.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) conn;

			httpConn.setAllowUserInteraction(false);
			httpConn.setDoOutput(true);
			httpConn.setRequestMethod("POST");
			httpConn.setRequestProperty("Content-Type","text/xml; charset=UTF-8");
			httpConn.setRequestProperty("SOAPAction","http://www.oasis-open.org/committees/security");

			byte[] encUserPass = new Base64(0).encode(userPass.getBytes());	 // Set line length to 0 so no wrapping (i.e. /r/n)
			String sEncUserPass = new String(encUserPass)			 
			httpConn.setRequestProperty("Authorization", "Basic ${sEncUserPass}")

			Random r = new Random();
			String requestID = 'a' + Long.toString(Math.abs(r.nextLong()), 20) + Long.toString(Math.abs(r.nextLong()), 20);

			StringBuilder soap = new StringBuilder("<SOAP-ENV:Envelope\n");
			soap.append("xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n");
			soap.append("\t<SOAP-ENV:Header/>\n");
			soap.append("\t<SOAP-ENV:Body>\n");
			soap.append("\t\t<samlp:Request\n");
			soap.append("\t\t\txmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\"\n");
			soap.append("\t\t\tMajorVersion=\"1\" MinorVersion=\"1\"\n");
			soap.append("\t\t\tRequestID=\"" + requestID + "\"\n");
			soap.append("\t\t\tIssueInstant=\"" + new DateTime() + "\">\n");
			soap.append("\t\t\t<samlp:AssertionArtifact>");
			soap.append(artifact);
			soap.append("</samlp:AssertionArtifact>\n");
			soap.append("\t\t</samlp:Request>\n");
			soap.append("\t</SOAP-ENV:Body>\n");
			soap.append("</SOAP-ENV:Envelope>\n");

			if (log.isDebugEnabled())   {
				log.debug(soap.toString());
			}
			
			out = new OutputStreamWriter(httpConn.getOutputStream());
			out.write(soap.toString());
			out.close();
/*
			File tfile = File.createTempFile("samlres", ".txt");
			log.error("file location:"+tfile.getAbsolutePath());
			try	{
				InputStream inputStream = httpConn.getInputStream();
				OutputStream outf=new FileOutputStream(tfile);
				byte buf[]=new byte[1024];
				int len;
				while((len=inputStream.read(buf))>0)	{
					outf.write(buf,0,len);
				}
				outf.close();
				inputStream.close();
			} catch (IOException e){
				log.error(e.getMessage(), e);
			}

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tfile);
*/
		    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(httpConn.getInputStream());

			Node statusCode = doc.getElementsByTagName("samlp:StatusCode").item(0);
			String codeValue = statusCode.getAttributes().getNamedItem("Value").getNodeValue();
			log.info("SAML Status code: " + codeValue);
/*
			if ("samlp:Success".compareToIgnoreCase(codeValue) != 0)    {
				log.error("Authorization was not successful: " + codeValue);
				throw new AuthenticationServiceException("SAML status is not success code: " + codeValue);
			}
*/
			Element nameID = (Element) doc.getElementsByTagName("saml:NameIdentifier").item(0);
			NodeList nameIDList = nameID.getChildNodes();
			String wwid = nameIDList.item(0).getNodeValue();
			log.info("Obtained ${wwid} from the Identity Vault");
			
			UserDetails ud = userDetailsService.loadUserByWWID(wwid)
			log.debug("We have found user: ${ud.username}")
			springSecurityService.reauthenticate(ud.username)
			
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