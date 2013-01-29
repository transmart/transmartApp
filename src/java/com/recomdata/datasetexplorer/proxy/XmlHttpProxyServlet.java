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
  

/* Copyright 2007 You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: XmlHttpProxyServlet.java 9178 2011-08-24 13:50:06Z mmcduffie $ 
*/
package com.recomdata.datasetexplorer.proxy;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.json.*;

/**  XmlHttpProxyServlet
 *   @author Greg Murray
 */
public class XmlHttpProxyServlet extends HttpServlet {

    private static String XHP_LAST_MODIFIED = "xhp_last_modified_key";
    private static String XHP_CONFIG = "xhp.json";

    private static boolean allowXDomain = true;
    private static boolean requireSession = false;
    private static String responseContentType = "text/xml;charset=UTF-8";  //changed from text/json in jmaki source
    private static boolean rDebug = false;
    private Logger logger = null;
    private XmlHttpProxy xhp = null;
    private ServletContext ctx;
    private JSONObject services = null;
    private String resourcesDir = "/resources/";
    private String classpathResourcesDir = "/META-INF/resources/";
    
    public XmlHttpProxyServlet() {
        if (rDebug) {
            logger = getLogger();
        }
    }
    
    public void init(ServletConfig config) throws ServletException {
	    super.init(config);
        ctx = config.getServletContext();
        // set the response content type
        if (ctx.getInitParameter("responseContentType") != null) {
            responseContentType = ctx.getInitParameter("responseContentType");
        }
        // allow for resources dir over-ride at the xhp level otherwise allow
        // for the jmaki level resources
        if (ctx.getInitParameter("jmaki-xhp-resources") != null) {
            resourcesDir = ctx.getInitParameter("jmaki-xhp-resources");
        } else if (ctx.getInitParameter("jmaki-resources") != null) {
            resourcesDir = ctx.getInitParameter("jmaki-resources");
        }
        // allow for resources dir over-ride
        if (ctx.getInitParameter("jmaki-classpath-resources") != null) {
            classpathResourcesDir = ctx.getInitParameter("jmaki-classpath-resources");
        }        
        
        String requireSessionString = ctx.getInitParameter("requireSession");
        if (requireSessionString != null) {
            if ("false".equals(requireSessionString)) {
                requireSession = false;
                getLogger().severe("XmlHttpProxyServlet: intialization. Session requirement disabled.");
            } else if ("true".equals(requireSessionString)) {
                requireSession = true;
                getLogger().severe("XmlHttpProxyServlet: intialization. Session requirement enabled.");
            }
        }  
        String xdomainString = ctx.getInitParameter("allowXDomain");
        if (xdomainString != null) {
            if ("true".equals(xdomainString)) {
                allowXDomain = true;
                getLogger().severe("XmlHttpProxyServlet: intialization. xDomain access is enabled.");
            } else if ("false".equals(xdomainString)) {
                allowXDomain = false;
                getLogger().severe("XmlHttpProxyServlet: intialization. xDomain access is disabled.");
            }
        }
        // if there is a proxyHost and proxyPort specified create an HttpClient with the proxy
        String proxyHost = ctx.getInitParameter("proxyHost");
        String proxyPortString = ctx.getInitParameter("proxyPort");
        if (proxyHost != null && proxyPortString != null) {
            int proxyPort = 8080;
            try {
                proxyPort= new Integer(proxyPortString).intValue();
                xhp = new XmlHttpProxy(proxyHost, proxyPort);
            } catch (NumberFormatException nfe) {
                getLogger().severe("XmlHttpProxyServlet: intialization error. The proxyPort must be a number");
                throw new ServletException("XmlHttpProxyServlet: intialization error. The proxyPort must be a number");
            }
        } else {
            xhp = new XmlHttpProxy();
        }
	}
    
    private void getServices(HttpServletResponse res) {
        InputStream is = null;
        try {
            URL url = ctx.getResource(resourcesDir + XHP_CONFIG);
            // use classpath if not found locally.
            if (url == null) url = XmlHttpProxyServlet.class.getResource(classpathResourcesDir + XHP_CONFIG);
            is = url.openStream();
        } catch (Exception ex) {
            try {
                getLogger().severe("XmlHttpProxyServlet error loading xhp.json : " + ex);
                PrintWriter writer = res.getWriter();
                writer.write("XmlHttpProxyServlet Error: Error loading xhp.json. Make sure it is available in the /resources directory of your applicaton.");
                writer.flush();
            } catch (Exception iox) {
            }
        }
        services = xhp.loadServices(is);
    }
   
    public void doGet(HttpServletRequest req, HttpServletResponse res) {
       doProcess(req,res, false);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) {
       doProcess(req,res, true);
    }
    
    public void doProcess(HttpServletRequest req, HttpServletResponse res, boolean isPost) {
        StringBuffer bodyContent = null;
        OutputStream out = null;
        PrintWriter writer = null;
        String serviceKey = null; 
    
        try {
            BufferedReader in = req.getReader();
            String line = null;
            while ((line = in.readLine()) != null) {
                if (bodyContent == null) bodyContent = new StringBuffer();
                bodyContent.append(line); 
            }
        } catch (Exception e) {
        }
        try {
            if (requireSession) {
                // check to see if there was a session created for this request
                // if not assume it was from another domain and blow up
                // Wrap this to prevent Portlet exeptions
                HttpSession session = req.getSession(false);
                if (session == null) {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
            serviceKey = req.getParameter("id");
            // only to preven regressions - Remove before 1.0
            if (serviceKey == null) serviceKey = req.getParameter("key");
            // check if the services have been loaded or if they need to be reloaded
            if (services == null || configUpdated()) {
                getServices(res);
            }
            String urlString = null;
            String xslURLString = null;
            String userName = null;
            String password = null;            
            String format = "json";
            String callback = req.getParameter("callback");
            String urlParams = req.getParameter("urlparams");
            String countString = req.getParameter("count");
            // encode the url to prevent spaces from being passed along
            if (urlParams != null) {
                urlParams = urlParams.replace(' ', '+');
            }
            
            try {
                if (services.has(serviceKey)) {
                    JSONObject service = services.getJSONObject(serviceKey);
                    // default to the service default if no url parameters are specified
                    if (urlParams == null && service.has("defaultURLParams")) {
                        urlParams = service.getString("defaultURLParams");
                    }
                    String serviceURL = service.getString("url");
                    // build the URL
                    if (urlParams != null && serviceURL.indexOf("?") == -1){
                        serviceURL += "?";
                    } else  if (urlParams != null) {
                        serviceURL += "&";
                    }
                    String apikey = "";
                    if (service.has("username")) userName = service.getString("username");
                    if (service.has("password")) password = service.getString("password");
                    if (service.has("apikey")) apikey = service.getString("apikey");
                    urlString = serviceURL + apikey;
                    if (urlParams != null) urlString += "&" + urlParams;
                    if (service.has("xslStyleSheet")) {
                        xslURLString = service.getString("xslStyleSheet");
                    }
                }   
                //code for passing the url directly through instead of using configuration file
                else if(req.getParameter("url")!=null)
                {	
                     String serviceURL = req.getParameter("url");
                     // build the URL
                     if (urlParams != null && serviceURL.indexOf("?") == -1){
                         serviceURL += "?";
                     } else  if (urlParams != null) {
                         serviceURL += "&";
                     }
                     urlString = serviceURL;
                     if (urlParams != null) urlString += urlParams;
                }
                else {
                    writer = res.getWriter();
                    if (serviceKey == null) writer.write("XmlHttpProxyServlet Error: id parameter specifying serivce required.");
                    else writer.write("XmlHttpProxyServlet Error : service for id '" + serviceKey + "' not  found.");
                    writer.flush();
                    return;
                }
            } catch (Exception ex) {
                getLogger().severe("XmlHttpProxyServlet Error loading service: " + ex);
            }

            Map paramsMap = new HashMap();
            paramsMap.put("format", format);
			// do not allow for xdomain unless the context level setting is enabled.
            if (callback != null && allowXDomain) {
                paramsMap.put("callback", callback);
            }
            if (countString != null) {
                paramsMap.put("count", countString);
            }            
            
            InputStream xslInputStream = null;
            
            if (urlString == null) {
                writer = res.getWriter();
                writer.write("XmlHttpProxyServlet parameters:  id[Required] urlparams[Optional] format[Optional] callback[Optional]");
                writer.flush();
                return;
            }
            // default to JSON
            res.setContentType(responseContentType);
            out = res.getOutputStream();
            // get the stream for the xsl stylesheet
            if (xslURLString != null) {
                // check the web root for the resource
                URL xslURL = null;
                xslURL = ctx.getResource(resourcesDir + "xsl/"+ xslURLString);
                // if not in the web root check the classpath
                if (xslURL == null) {
                    xslURL = XmlHttpProxyServlet.class.getResource(classpathResourcesDir + "xsl/" + xslURLString);
                }
                if (xslURL != null) {
                    xslInputStream  = xslURL.openStream();
                } else {
                    String message = "Could not locate the XSL stylesheet provided for service id " +  serviceKey + ". Please check the XMLHttpProxy configuration.";
                    getLogger().severe(message);
                    try {
                        out.write(message.getBytes());
                        out.flush();
                        return;
                    } catch (java.io.IOException iox){
                    }
                }
            }
            if (!isPost) {
                xhp.doGet(urlString, out, xslInputStream, paramsMap, userName, password);
            } else {                
                if (bodyContent == null) getLogger().info("XmlHttpProxyServlet attempting to post to url " + urlString + " with no body content");      
                xhp.doPost(urlString, out, xslInputStream, paramsMap, bodyContent.toString(), req.getContentType(), userName, password);
            }
        } catch (Exception iox) {
            iox.printStackTrace();
            getLogger().severe("XmlHttpProxyServlet: caught " + iox);
            try {
                writer = res.getWriter();
                writer.write(iox.toString());
                writer.flush();
            } catch (java.io.IOException ix) {
                ix.printStackTrace();                
            }
            return;
        } finally {
            try {
                if (out != null) out.close();
                if (writer != null) writer.close();
            } catch (java.io.IOException iox){
            }
        }
    }

    /**
    * Check to see if the configuration file has been updated so that it may be reloaded.
    */
    private boolean configUpdated() {
        try {
            URL url = ctx.getResource(resourcesDir + XHP_CONFIG);
            URLConnection con;
            if (url == null) return false ;
            con = url.openConnection(); 
            long lastModified = con.getLastModified();
            long XHP_LAST_MODIFIEDModified = 0;
            if (ctx.getAttribute(XHP_LAST_MODIFIED) != null) {
                XHP_LAST_MODIFIEDModified = ((Long)ctx.getAttribute(XHP_LAST_MODIFIED)).longValue();
            } else {
                ctx.setAttribute(XHP_LAST_MODIFIED, new Long(lastModified));
                return false;
            }
            if (XHP_LAST_MODIFIEDModified < lastModified) {
                ctx.setAttribute(XHP_LAST_MODIFIED, new Long(lastModified));
                return true;
            }
        } catch (Exception ex) {
            getLogger().severe("XmlHttpProxyServlet error checking configuration: " + ex);
        }
        return false;
    }
         
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger("jmaki.services.xhp.Log");
        }
        return logger;
    }
    
    private void logMessage(String message) {
        if (rDebug) {
            getLogger().info(message);
        }
    }
}