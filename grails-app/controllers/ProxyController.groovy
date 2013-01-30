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
 * @author JIsikoff
 *
 */
import com.recomdata.datasetexplorer.proxy.*
import javax.servlet.*
import javax.servlet.http.*
import com.recomdata.export.*


class ProxyController{

	static  defaultAction="proxy"
	
	def proxy={
		def post=false;
		if(request.getMethod()=="POST")
			post=true;
		doProcess(request, response, post)
	}
	

	private def doProcess(HttpServletRequest req, HttpServletResponse res, boolean isPost) {

		  boolean allowXDomain = true;
		  boolean requireSession = false;
		  String responseContentType = "text/xml;charset=UTF-8";  //changed from text/json in jmaki source
		  boolean rDebug = false;
		 XmlHttpProxy xhp = new XmlHttpProxy();;
		 ServletContext ctx;
		 
		StringBuilder bodyContent = new StringBuilder();;
		OutputStream out = null;
		PrintWriter writer = null;
		String serviceKey = null;

		/*try {
			BufferedReader inp = req.getReader();
			String line = null;
			while ((line = inp.readLine()) != null) {
				if (bodyContent == null) bodyContent = new StringBuffer();
				bodyContent.append(line);
				//log.trace(line);
			}
		} catch (Exception e) {
			println(e)
			log.error(e.toString())
		}*/
		
		BufferedReader bufferedReader = null;
		try {
		  InputStream inputStream = req.getInputStream();
		  if (inputStream != null) {
		   bufferedReader = new BufferedReader(new InputStreamReader(
		inputStream));
		   char[] charBuffer = new char[128];
		   int bytesRead = -1;
		   while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
			bodyContent.append(charBuffer, 0, bytesRead);
		   }
		  } else {
		   bodyContent.append("");
		  }
		} catch (IOException ex) {
			log.error(ex);
		 // throw ex;
		} finally {
		  if (bufferedReader != null) {
		   try {
			bufferedReader.close();
		   } catch (IOException ex) {
		   log.error(ex)
			//throw ex;
		   }
		  }
		}
		
	//	println(bodyContent.toString());
		try {
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
				if (false) {
					log.trace("wrong")
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
				log.error("XmlHttpProxyServlet Error loading service: " + ex);
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
					log.debug(message);
					try {
						out.write(message.getBytes());
						out.flush();
						return;
					} catch (java.io.IOException iox){
					}
				}
			}
		//	println("url:"+urlString);
		//	println("body:"+bodyContent);
			if (!isPost) {
				log.trace("proxying to:"+urlString);
				xhp.doGet(urlString, out, xslInputStream, paramsMap, userName, password);
			} else {
				if (bodyContent == null || bodyContent.length()==0) log.debug("XmlHttpProxyServlet attempting to post to url " + urlString + " with no body content");
				log.trace("proxying to:"+urlString);
				xhp.doPost(urlString, out, xslInputStream, paramsMap, bodyContent.toString(), req.getContentType(), userName, password);
			}
		} catch (Exception iox) {
			iox.printStackTrace();
			log.trace("XmlHttpProxyServlet: caught " + iox);
			try {
				writer = res.getWriter();
				writer.write("XmlHttpProxyServlet error loading service for " + serviceKey + " . Please notify the administrator.");
				writer.flush();
			} catch (java.io.IOException ix) {
				ix.printStackTrace();
			}
			return;
		} finally {
			try {
				//if (out != null) out.close();
				//if (writer != null) writer.close();
			} catch (java.io.IOException iox){
			}
		}
	}
}
