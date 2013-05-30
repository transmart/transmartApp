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
  

/*
 * net/balusc/util/HttpServletUtil.java
 *
 * Copyright (C) 2007 BalusC
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package net.balusc.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Useful HttpServlet utilities.
 *
 * @author BalusC
 * @link http://balusc.blogspot.com/2006/05/httpservletutil.html
 */
public final class HttpServletUtil {

    // Init ---------------------------------------------------------------------------------------

    private HttpServletUtil() {
        // Utility class, hide the constructor.
    }

    // Cookies ------------------------------------------------------------------------------------

    /**
     * Retrieve the cookie value from the given servlet request based on the given
     * cookie name.
     * @param request The HttpServletRequest to be used.
     * @param name The cookie name to retrieve the value for.
     * @return The cookie value associated with the given cookie name.
     */

    public static String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie != null && name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Set the cookie value in the given servlet response based on the given cookie
     * name and expiration interval.
     * @param response The HttpServletResponse to be used.
     * @param name The cookie name to associate the cookie value with.
     * @param value The actual cookie value to be set in the given servlet response.
     * @param maxAge The expiration interval in seconds. If this is set to 0,
     * then the cookie will immediately expire.
     */
    public static void setCookieValue(
        HttpServletResponse response, String name, String value, int maxAge)
    {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    /**
     * Remove the cookie from the given servlet response based on the given cookie
     * name. It actually sets the cookie expiration interval to zero, resulting the
     * cookie being expired immediately.
     * @param response The HttpServletResponse to be used.
     * @param name The cookie name of the cookie to be removed.
     */
    public static void removeCookie(HttpServletResponse response, String name) {
        setCookieValue(response, name, null, 0);
    }

    // Downloads ----------------------------------------------------------------------------------

    /**
     * Send the given file as a byte array to the servlet response. If attachment is set to true,
     * then show a "Save as" dialogue, else show the file inline in the browser or let the
     * operating system open it in the right application.
     * @param response The HttpServletResponse to be used.
     * @param bytes The file contents in a byte array.
     * @param fileName The file name.
     * @param attachment Download as attachment?
     */
    public static void downloadFile(
        HttpServletResponse response, byte[] bytes, String fileName, boolean attachment)
            throws IOException
    {
        // Wrap the byte array in a ByteArrayInputStream and pass it through another method.
        downloadFile(response, new ByteArrayInputStream(bytes), fileName, attachment);
    }

    /**
     * Send the given file as a File object to the servlet response. If attachment is set to true,
     * then show a "Save as" dialogue, else show the file inline in the browser or let the
     * operating system open it in the right application.
     * @param response The HttpServletResponse to be used.
     * @param file The file as a File object.
     * @param attachment Download as attachment?
     */
    public static void downloadFile(
        HttpServletResponse response, File file, boolean attachment)
            throws IOException
    {
        // Prepare stream.
        BufferedInputStream input = null;

        try {
            // Wrap the file in a BufferedInputStream and pass it through another method.
            input = new BufferedInputStream(new FileInputStream(file));
            downloadFile(response, input, file.getName(), attachment);
        } finally {
            // Gently close stream.
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    String message = "Closing file " + file.getPath() + " failed.";
                    // Do your thing with the exception and the message. Print it, log it or mail it.
                    System.err.println(message);
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Send the given file as an InputStream to the servlet response. If attachment is set to true,
     * then show a "Save as" dialogue, else show the file inline in the browser or let the
     * operating system open it in the right application.
     * @param response The HttpServletResponse to be used.
     * @param input The file contents in an InputStream.
     * @param fileName The file name.
     * @param attachment Download as attachment?
     */
    public static void downloadFile(
        HttpServletResponse response, InputStream input, String fileName, boolean attachment)
            throws IOException
    {
        // Prepare stream.
        BufferedOutputStream output = null;

        try {
            // Prepare.
            int contentLength = input.available();
            String contentType = URLConnection.guessContentTypeFromName(fileName);
            String disposition = attachment ? "attachment" : "inline";

            // If content type is unknown, then set the default value.
            // For all content types, see: http://www.w3schools.com/media/media_mimeref.asp
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Init servlet response.
            response.reset();
            response.setContentLength(contentLength);
            response.setContentType(contentType);
            response.setHeader(
                "Content-disposition", disposition + "; filename=\"" + fileName + "\"");
            output = new BufferedOutputStream(response.getOutputStream());

            // Write file contents to response.
            while (contentLength-- > 0) {
                output.write(input.read());
            }

            // Finalize task.
            output.flush();
        } finally {
            // Gently close stream.
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    String message = "Closing HttpServletResponse#getOutputStream() failed.";
                    // Do your thing with the exception and the message. Print it, log it or mail it.
                    System.err.println(message);
                    e.printStackTrace();
                }
            }
        }
    }

    // Queriers -----------------------------------------------------------------------------------

    /**
     * Invoke a GET request on the given URL with the given parameter map which will be encoded as
     * UTF-8. It is highly recommended to close the obtained inputstream after processing!
     * @param url The URL to be invoked.
     * @param parameterMap The parameter map to be processed as query parameters.
     * @return The result of the GET request as an InputStream.
     * @throws MalformedURLException If the given URL is invalid.
     * @throws IOException If the given URL cannot be connected nor written.
     */
    public static InputStream doGet(String url, Map<String, String[]> parameterMap)
        throws MalformedURLException, IOException
    {
        return doGet(url, parameterMap, "UTF-8");
    }

    /**
     * Invoke a GET request on the given URL with the given parameter map and the given charset
     * encoding. It is highly recommended to close the obtained inputstream after processing!
     * @param url The URL to be invoked.
     * @param parameterMap The parameter map to be processed as query parameters.
     * @param charset The encoding to be applied.
     * @return The result of the GET request as an InputStream.
     * @throws MalformedURLException If the given URL is invalid.
     * @throws IOException If the given URL cannot be connected nor written.
     * @throws UnsupportedEncodingException If the given charset is not supported.
     */
    public static InputStream doGet(String url, Map<String, String[]> parameterMap, String charset)
        throws MalformedURLException, IOException, UnsupportedEncodingException
    {
        String query = createQuery(parameterMap, charset);
        URLConnection urlConnection = new URL(url + "?" + query).openConnection();
        urlConnection.setUseCaches(false);

        return urlConnection.getInputStream();
    }

    /**
     * Invoke a POST request on the given URL with the given parameter map which will be encoded as
     * UTF-8. It is highly recommended to close the obtained inputstream after processing!
     * @param url The URL to be invoked.
     * @param parameterMap The parameter map to be processed as query parameters.
     * @return The result of the POST request as an InputStream.
     * @throws MalformedURLException If the given URL is invalid.
     * @throws IOException If the given URL cannot be connected nor written.
     */
    public static InputStream doPost(String url, Map<String, String[]> parameterMap)
        throws MalformedURLException, IOException
    {
        return doPost(url, parameterMap, "UTF-8");
    }

    /**
     * Invoke a POST request on the given URL with the given parameter map and the given charset
     * encoding. It is highly recommended to close the obtained inputstream after processing!
     * @param url The URL to be invoked.
     * @param parameterMap The parameter map to be processed as query parameters.
     * @param charset The encoding to be applied.
     * @return The result of the POST request as an InputStream.
     * @throws MalformedURLException If the given URL is invalid.
     * @throws IOException If the given URL cannot be connected nor written.
     * @throws UnsupportedEncodingException If the given charset is not supported.
     */
    public static InputStream doPost(String url, Map<String, String[]> parameterMap, String charset)
        throws MalformedURLException, IOException, UnsupportedEncodingException
    {
        String query = createQuery(parameterMap, charset);
        URLConnection urlConnection = new URL(url).openConnection();
        urlConnection.setUseCaches(false);
        urlConnection.setDoOutput(true); // Triggers POST.
        urlConnection.setRequestProperty("accept-charset", charset);
        urlConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
        OutputStreamWriter writer = null;

        try {
            writer = new OutputStreamWriter(urlConnection.getOutputStream());
            writer.write(query);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    String message = "Closing URLConnection#getOutputStream() of " + url + " failed.";
                    // Do your thing with the exception and the message. Print it, log it or mail it.
                    System.err.println(message);
                    e.printStackTrace();
                }
            }
        }

        return urlConnection.getInputStream();
    }

    /**
     * Create a query string based on the given parameter map and the given charset encoding.
     * @param parameterMap The parameter map to be processed as query parameters.
     * @param charset The encoding to be applied.
     * @return The parameter map as query string.
     * @throws UnsupportedEncodingException If the given charset is not supported.
     */
    public static String createQuery(Map<String, String[]> parameterMap, String charset)
        throws UnsupportedEncodingException
    {
        StringBuilder query = new StringBuilder();

        for (Iterator<String> names = parameterMap.keySet().iterator(); names.hasNext();) {
            String name = names.next();
            String[] values = parameterMap.get(name);

            for (int i = 0; i < values.length;) {
                query.append(URLEncoder.encode(name, charset));
                query.append("=");
                query.append(URLEncoder.encode(values[i], charset));

                if (++i < values.length) {
                    query.append("&");
                }
            }

            if (names.hasNext()) {
                query.append("&");
            }
        }

        return query.toString();
    }
    /**
     * Trim the given string with the given trim value.
     * @param string The string to be trimmed.
     * @param trim The value to trim the given string off.
     * @return The trimmed string.
     */
    public static String trim(String string, String trim) {
        if (trim.length() == 0) {
            return string;
        }

        int start = 0;
        int end = string.length();
        int length = trim.length();

        while (start + length <= end && string.substring(start, start + length).equals(trim)) {
            start += length;
        }
        while (start + length <= end && string.substring(end - length, end).equals(trim)) {
            end -= length;
        }

        return string.substring(start, end);
    }

}