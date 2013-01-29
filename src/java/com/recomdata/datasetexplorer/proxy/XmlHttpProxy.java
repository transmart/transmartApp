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
 $Id: XmlHttpProxy.java 11083 2011-12-09 06:05:13Z jliu $ 
*/
package com.recomdata.datasetexplorer.proxy;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.json.*;

public class XmlHttpProxy {
	
	private String userName = null;
	private String password = null;
    private static Logger logger;
    private String proxyHost = "";
    int proxyPort = -1;
    private JSONObject config;
    private static String USAGE = "Usage:  -url service_URL  -id service_key [-url or -id required] -xslurl xsl_url [optional] -format json|xml [optional] -callback[optional] -config [optional] -resources base_directory_containing XSL stylesheets [optional]";
    
    public XmlHttpProxy() {}
    
    public XmlHttpProxy(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }
    
    public XmlHttpProxy(String proxyHost, int proxyPort,
                        String userName, String password) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.userName = userName;
        this.password = password;
    }

    /**
     * This method will go out and make the call and it will apply an XSLT Transformation with the
     * set of parameters provided.
     *
     * @param urlString - The URL which you are looking up
     * @param out - The OutputStream to which the resulting document is written
     *
     */
    public void doGet(String urlString,
            OutputStream out)
            throws IOException, MalformedURLException {
        doProcess(urlString, out, null, null, null, null,null,null);
    }
    
    
   /**
     * This method will go out and make the call and it will apply an XSLT Transformation with the
     * set of parameters provided.
     *
     * @param urlString - The URL which you are looking up
     * @param out - The OutputStream to which the resulting document is written
     * @param xslInputStream - An input Stream to an XSL style sheet that is provided to the XSLT processor. If set to null there will be no transformation
     * @paramsMap - A Map of parameters that are feed to the XSLT Processor. These params may be used when generating content. This may be set to null if no parameters are necessary.
     *
     */
    public void doGet(String urlString,
            OutputStream out,
            InputStream xslInputStream,
            Map paramsMap) throws IOException, MalformedURLException {
        doProcess(urlString, out, xslInputStream, paramsMap, null,null, null,null);
    }
         
    public void doGet(String urlString,
            OutputStream out,
            InputStream xslInputStream,
            Map paramsMap,
            String userName,
            String password) throws IOException, MalformedURLException {
        doProcess(urlString, out, xslInputStream, paramsMap, null,null, userName,password);
    }

    /**
     * This method will go out and make the call and it will apply an XSLT Transformation with the
     * set of parameters provided.
     *
     * @param urlString - The URL which you are looking up
     * @param out - The OutputStream to which the resulting document is written
     *
     */
    public void doPost(String urlString,
            OutputStream out,
            InputStream xslInputStream,
            Map paramsMap,
            String postData,
            String postContentType
        ) throws IOException, MalformedURLException {
        doPost(urlString, out, xslInputStream, paramsMap, postData, postContentType, null, null);
    }
    
    public void doPost(String urlString,
            OutputStream out,
            InputStream xslInputStream,
            Map paramsMap,
            String postData,
            String postContentType,
            String userName,
            String password) throws IOException, MalformedURLException {
        doProcess(urlString, out, xslInputStream, paramsMap, postData, postContentType, userName, password);
    }
     
    /**
     * This method will go out and make the call and it will apply an XSLT Transformation with the
     * set of parameters provided.
     *
     * @param urlString - The URL which you are looking up
     * @param out - The OutputStream to which the resulting document is written
     * @param xslInputStream - An input Stream to an XSL style sheet that is provided to the XSLT processor. If set to null there will be no transformation
     * @paramsMap - A Map of parameters that are feed to the XSLT Processor. These params may be used when generating content. This may be set to null if no parameters are necessary.
     * @postData - A String of the bodyContent to be posted. A doPost will be used if this is parameter is not null.
     * @postContentType - The request contentType used when posting data. Will not be set if this parameter is null.
     * @userName - userName used for basic authorization
     * @password - password used for basic authorization
     */        
    public void doProcess(String urlString,
            OutputStream out,
            InputStream xslInputStream,
            Map paramsMap,
            String postData,
            String postContentType,
            String userName,
            String password) throws IOException, MalformedURLException {
        
        if (paramsMap == null) {
            paramsMap = new HashMap();
        }
        
        String format = (String)paramsMap.get("format");
        if (format == null) {
            format = "xml";
        }
        
        InputStream in = null;
        BufferedOutputStream os = null;
        
        HttpClient httpclient = null;
        
        if (userName != null &&
           password != null) {
                httpclient = new HttpClient(proxyHost, proxyPort, urlString, userName, password);
        }  else {
             httpclient = new HttpClient(proxyHost, proxyPort, urlString);
        }
        
        // post data determines whether we are going to do a get or a post
        if (postData == null ||postData.trim().length()==0) {
            in = httpclient.getInputStream();
        } else {
            in = httpclient.doPost(postData, postContentType);
        }
        // read the encoding from the incoming document and default to 8859-1
        // if an encoding is not provided
        String ce = httpclient.getContentEncoding();
        if (ce == null) {
            String ct = httpclient.getContentType();
            if (ct != null) {
                int idx = ct.lastIndexOf("charset=");
                if (idx >= 0) {
                    ce = ct.substring(idx+8);
                } else {
                    ce = "UTF-8";
                }
            } else {
                ce = "UTF-8";
            }
        }
        
        try {
            byte[] buffer = new byte[1024];
            int read = 0;
            String cType = null;
            // write out hte content type
            if (format.equals("json")) {
                cType = "text/javascript;charset="+ce;
            } else {
                cType = "text/xml;charset="+ce;
            }
            if (xslInputStream == null) {
                while (true) {
                    read = in.read(buffer);
                    if (read <= 0) break;
                    out.write(buffer, 0, read );
                }
            } else {
                transform(in, xslInputStream, paramsMap, out, ce);
            }
        } catch (Exception e) {
            getLogger().severe("XmlHttpProxy transformation error: " + e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }
    
    /**
     * Do the XSLT transformation
     */
    public void transform( InputStream xmlIS,
            InputStream xslIS,
            Map params,
            OutputStream result,
            String encoding) {
        try {
            TransformerFactory trFac = TransformerFactory.newInstance();
            Transformer transformer = trFac.newTransformer(new StreamSource(xslIS));
            Iterator it = params.keySet().iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                transformer.setParameter(key, (String)params.get(key));
            }
            transformer.setOutputProperty("encoding", encoding);
            transformer.transform(new StreamSource(xmlIS), new StreamResult(result));
        } catch (Exception e) {
            getLogger().severe("XmlHttpProxy: Exception with xslt " + e);
        }
    }

    /**
    *
    * CLI to the XmlHttpProxy
    */	
    public static void main(String[] args)
            throws IOException, MalformedURLException {
        
        getLogger().info("XmlHttpProxy 1.1");
        XmlHttpProxy xhp = new XmlHttpProxy();
        
        if (args.length == 0) {
            System.out.println(USAGE);
        }
        
        InputStream xslInputStream = null;
        String serviceKey = null;
        String urlString = null;
        String xslURLString = null;
        String format = "xml";
        String callback = null;
        String urlParams = null;
        String configURLString = "xhp.json";
        String resourceBase = "file:src/conf/META-INF/resources/xsl/";
        
        // read in the arguments
        int index = 0;
        while (index < args.length) {
            if (args[index].toLowerCase().equals("-url") && index + 1 < args.length) {
                urlString = args[++index];
            } else if (args[index].toLowerCase().equals("-key") && index + 1 < args.length) {
                serviceKey = args[++index];
            } else if (args[index].toLowerCase().equals("-id") && index + 1 < args.length) {
                serviceKey = args[++index];                
            } else if (args[index].toLowerCase().equals("-callback") && index + 1 < args.length) {
                callback = args[++index];
            }  else if (args[index].toLowerCase().equals("-xslurl") && index + 1 < args.length) {
                xslURLString = args[++index];
            } else if (args[index].toLowerCase().equals("-urlparams") && index + 1 < args.length) {
                urlParams = args[++index];
            } else if (args[index].toLowerCase().equals("-config") && index + 1 < args.length) {
                configURLString = args[++index];
            } else if (args[index].toLowerCase().equals("-resources") && index + 1 < args.length) {
                resourceBase = args[++index];
            }
            index++;
        }
        
        if (serviceKey != null) {
            try {
                InputStream is = (new URL(configURLString)).openStream();
                JSONObject services = loadServices(is);
                JSONObject service = services.getJSONObject(serviceKey);
                // default to the service default if no url parameters are specified
                if (urlParams == null && service.has("defaultURLParams")) {
                    urlParams = service.getString("defaultURLParams");
                }
                String serviceURL = service.getString("url");
                // build the URL properly
                if (urlParams != null && serviceURL.indexOf("?") == -1){
                    serviceURL += "?";
                } else if (urlParams != null){
                    serviceURL += "&";
                }
                urlString = serviceURL + service.getString("apikey") +  "&" + urlParams;
                if (service.has("xslStyleSheet")) {
                    xslURLString = service.getString("xslStyleSheet");
                    // check if the url is correct of if to load from the classpath
                    
                }
            } catch (Exception ex) {
                getLogger().severe("XmlHttpProxy Error loading service: " + ex);
                System.exit(1);
            }
        } else if (urlString == null) {
            System.out.println(USAGE);
            System.exit(1);
        }
        // The parameters are feed to the XSL Stylsheet during transformation.
        // These parameters can provided data or conditional information.
        Map paramsMap = new HashMap();
        if (format != null) {
            paramsMap.put("format", format);
        }
        if (callback != null) {
            paramsMap.put("callback", callback);
        }
        
        if (xslURLString != null) {
            URL xslURL = new URL(xslURLString);
            if (xslURL != null) {
                xslInputStream  = xslURL.openStream();
            } else {
                getLogger().severe("Error: Unable to locate XSL at URL " + xslURLString);
            }
        }
        xhp.doGet(urlString, System.out, xslInputStream, paramsMap);
    }
	
    public static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger("jmaki.xhp.Log");
        }
        return logger;
    }
    
    public static JSONObject loadServices(InputStream is) {
	        JSONObject config = null;
	        JSONObject services = new JSONObject();
	        try {
	            config = loadJSONObject(is).getJSONObject("xhp");
	            JSONArray sA = config.getJSONArray("services");
	            for (int l=0; l < sA.length(); l++) {
	               JSONObject value = sA.getJSONObject(l);
	               String key = value.getString("id");
	               services.put(key,value);
	            }
            } catch (Exception ex) {
	            getLogger().severe("XmlHttpProxy error loading services." + ex);
            }
	        return services;
	}
    
    public static JSONObject loadJSONObject(InputStream in) {
        ByteArrayOutputStream out = null;
        try {
			byte[] buffer = new byte[1024];
			int read = 0;
            out = new ByteArrayOutputStream();
            while (true) {
                read = in.read(buffer);
                if (read <= 0) break;
                out.write(buffer, 0, read );
            }
            return new JSONObject(out.toString());
        } catch (Exception e) {
            getLogger().severe("XmlHttpProxy error reading in json "  + e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (Exception e) {
            }
        }
        return null;
    }
}