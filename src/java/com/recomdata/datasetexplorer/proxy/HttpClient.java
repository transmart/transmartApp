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
  

/* Copyright 2007 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: HttpClient.java 11083 2011-12-09 06:05:13Z jliu $ 
*/
package com.recomdata.datasetexplorer.proxy;

import java.io.*;
import java.net.*;
import java.util.logging.*;
import java.security.Security;
//import javax.net.ssl.*;
import com.sun.net.ssl.*;

import org.apache.commons.codec.binary.Base64;

/**
 * @author Yutaka Yoshida, Greg Murray
 *
 * Minimum set of HTTPclient supporting both http and https.
 * It's aslo capable of POST, but it doesn't provide doGet because
 * the caller can just read the inputstream.
 */
public class HttpClient {

    private static Logger logger;
    private String proxyHost = null;
    private int proxyPort = -1;
    private boolean isHttps = false;
    private boolean isProxy = false;
    private URLConnection urlConnection = null;
    
    /**
     * @param url URL string
     */
    public HttpClient(String url) 
        throws MalformedURLException {
        this.urlConnection = getURLConnection(url);
    }
    /**
     * @param phost PROXY host name
     * @param pport PROXY port string
     * @param url URL string
     */
    public HttpClient(String phost, int pport, String url)
        throws MalformedURLException {
        if (phost != null && pport != -1) {
            this.isProxy = true;
        }
        this.proxyHost = phost;
        this.proxyPort = pport;
        if (url.trim().startsWith("https:")) {
            isHttps = true;
        }
        this.urlConnection = getURLConnection(url);
    }
       /**
     * @param phost PROXY host name
     * @param pport PROXY port string
     * @param url URL string
     * @param userName string
     * @param password string
     */
    public HttpClient(String phost,
                      int pport,
                      String url,
                      String userName,
                      String password)
        throws MalformedURLException {
        try {            
            if (phost != null && pport != -1) {
                this.isProxy = true;
            }
            this.proxyHost = phost;
            this.proxyPort = pport;
            if (url.trim().startsWith("https:")) {
                isHttps = true;
            }
            this.urlConnection = getURLConnection(url);
            // set basic authentication information
            String auth = userName + ":" +  password;   
            // String encoded = new sun.misc.BASE64Encoder().encode (auth.getBytes());
            byte[] encodedBytes= Base64.encodeBase64(auth.getBytes());
            String encoded = new String(encodedBytes);
            // set basic authorization
            this.urlConnection.setRequestProperty ("Authorization", "Basic " + encoded);
            this.urlConnection.setConnectTimeout(600000);
            this.urlConnection.setReadTimeout(600000);
        } catch (Exception ex) {
            HttpClient.getLogger().severe("Unable to set basic authorization for " + userName  + " : " +ex);
        }        
    }
    
    /**
     * private method to get the URLConnection
     * @param str URL string
     */
    private URLConnection getURLConnection(String str) 
        throws MalformedURLException {
    	try {
        	

            if (isHttps) {
            	
            	
            	
            	
                /* when communicating with the server which has unsigned or invalid
                 * certificate (https), SSLException or IOException is thrown.
                 * the following line is a hack to avoid that
                 */
                Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
                System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
                if (isProxy) {
                    System.setProperty("https.proxyHost", proxyHost);
                    System.setProperty("https.proxyPort", proxyPort + "");            
                }
            } else {
                if (isProxy) {
                    System.setProperty("http.proxyHost", proxyHost);
                    System.setProperty("http.proxyPort", proxyPort  + "");
                }
                
            }
        	SSLUtilities.trustAllHostnames();
        	SSLUtilities.trustAllHttpsCertificates();
            URL url = new URL(str);
            URLConnection uc = url.openConnection();
            if(isHttps)
            {
            	/*((HttpsURLConnection)uc).setHostnameVerifier(new HostnameVerifier()
            	{
            		public boolean verify (String hostname, String 
            				session)
            				                      {
            				                        return true;
            				                      }
            	});*/
            }
            // set user agent to mimic a common browser
            String ua="Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322)";
            uc.setRequestProperty("user-agent", ua);  
            
       
           
            return uc;
        } catch (MalformedURLException me) {
            throw new MalformedURLException(str + " is not a valid URL");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * returns the inputstream from URLConnection
     * @return InputStream
     */
    public InputStream getInputStream() {
        try {
            return (this.urlConnection.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * return the OutputStream from URLConnection
     * @return OutputStream
     */
    public OutputStream getOutputStream() {
        
        try {
            return (this.urlConnection.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * posts data to the inputstream and returns the InputStream.
     * @param postData data to be posted. must be url-encoded already.
     * @param contentType allows you to set the contentType of the request.
     * @return InputStream input stream from URLConnection
     */
    public InputStream doPost(String postData, String contentType) {
    	
    //	System.out.println("postdata:"+postData);
   // 	System.out.println("ct:"+contentType);
        this.urlConnection.setDoOutput(true);     
        if (contentType != null) this.urlConnection.setRequestProperty( "Content-type", contentType );
               
        OutputStream os = this.getOutputStream();
        PrintStream ps = new PrintStream(os);
        ps.print(postData);
        ps.close(); 
        return (this.getInputStream());
    }
    
    public String getContentEncoding() {
        if (this.urlConnection == null) return null;
        return (this.urlConnection.getContentEncoding());
    }
    public int getContentLength() {
        if (this.urlConnection == null) return -1;
        return (this.urlConnection.getContentLength());
    }
    public String getContentType() {
        if (this.urlConnection == null) return null;
        return (this.urlConnection.getContentType());
    }
    public long getDate() {
        if (this.urlConnection == null) return -1;
        return (this.urlConnection.getDate());
    }
    public String getHeader(String name) {
        if (this.urlConnection == null) return null;
        return (this.urlConnection.getHeaderField(name));
    }
    public long getIfModifiedSince() {
        if (this.urlConnection == null) return -1;
        return (this.urlConnection.getIfModifiedSince());
    }

    public static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger("jmaki.xhp.Log");
        }
        return logger;
    }    
}
