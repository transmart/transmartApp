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
 * $Id: ExportController.groovy 10098 2011-10-19 18:39:32Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 10098 $
 *
 */
import grails.converters.*
import org.json.*;
import org.transmart.searchapp.AuthUser;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class ExportController {

    def index = { }
    def i2b2HelperService
    def springSecurityService
	def dataCountService
	
    def exportSecurityCheck={
    		log.debug("Check export security")
        	String rid1 = request.getParameter("result_instance_id1");
    		String rid2 = request.getParameter("result_instance_id2");
    		def user=AuthUser.findByUsername(springSecurityService.getPrincipal().username)
    		def canExport=CanExport(user, rid1, rid2);
    		log.debug("CANEXPORT:"+canExport);
      		def result=[canExport: canExport]
    		log.trace(result as JSON)
    		render result as JSON
    }

    private boolean CanExport(AuthUser user, String rid1, String rid2)
    {
		def trials=i2b2HelperService.getDistinctTrialsInPatientSets(rid1, rid2);
   		def sectokens=i2b2HelperService.getSecureTokensWithAccessForUser(user)
   		for (String it : trials) {
   			if(!sectokens.containsKey(it))
   			{
   				log.debug("not found key in export check:"+it)
   				return false; //short circuit if found a single one that isnt in the tokens collection

   			}
   			else if(sectokens.containsKey(it))
   			{
   				log.debug("checking found key:"+it+":"+sectokens[it])
   				log.debug("equals own:"+sectokens[it].equals("OWN"))
   				log.debug("equals export:"+sectokens[it].equals("EXPORT"))
   				if(!(sectokens[it].equals("OWN")) & !(sectokens[it].equals("EXPORT"))) //if not export or own then also return false
   				{
   					log.debug("in return false inner")
   				    return false;
   				}
   			}
   		}
   		log.debug("made it to end of loop so the user can export");
   		return true;
    }

    /**
     * Checks to see if the user has run at least one heatmap.  If so, return true
     * so we can export the file.  If not, notify the client so they can alert the
     * user.
     */
    def check = {
            boolean exportReady = session.expdsfilename != null
            //   PrintWriter pw=new PrintWriter(response.getOutputStream());
       	//	pw.write(exportReady.toString());
       	//	pw.flush();
                def result=[ready: exportReady]
                   		render result as JSON
           }

    /**
     * This method simply just takes the stored csv file that was stored and presents
     * it to the user.  Kinda silly to save it locally and then just restream it to the
     * user but we'll leave that for now.  We could also cache the initial results
     * or just take the tab separated values that are saved, convert it and then
     * send it to the user.
     */
    def exportDataset = {
        log.debug("Export filename: " + session.expdsfilename)
        byte[] bytes = new String("No data to export").getBytes();
        if (session.expdsfilename != null)	{
            log.debug("Made it to exportDataset for file: " +  session.expdsfilename)
            File testFile = new File(session.expdsfilename)
            InputStream is = new FileInputStream(testFile);
            long fLen = testFile.length()
            log.debug("Length: " + fLen)
            bytes = new byte[(int)fLen]
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            } // Ensure all the bytes have been read in

            if (offset < bytes.length) {
                throw new IOException("Could not completely read file "+file.getName());
            }

            is.close();
        }

            int outputSize=bytes.length;
            log.debug("Size of bytes: " + outputSize)
            response.setContentType("text/csv");
            response.setHeader("Content-disposition", "attachment; filename=" + "exportdatasets.csv");
            response.setContentLength(outputSize);
            ServletOutputStream servletoutputstream = response.getOutputStream();
            servletoutputstream.write(bytes);
            servletoutputstream.flush();

    }
}
