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
 * $Id: WebClient.java 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
package com.recomdata.util.genego;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

public class WebClient {
    static final String DEMO_USER = "demo";
    static final String DEMO_PWD = "demo";
        
    static Logger log = Logger.getLogger(WebClient.class);
    
    GeneGOLocator ggL = null;    
    
    /**
     * Main login method for GeneGO
     * 
     * @return the authenticate key that we will use to ensure that the DEMO user is logged out
     */
    public String login()   {
        GeneGOLocator ggL = new GeneGOLocator();
        String authKey = null;
        try {
            authKey = ggL.getGeneGOPort().login(DEMO_USER, DEMO_PWD);
        } catch(ServiceException se)    {
            log.error(se.getLocalizedMessage(), se);
        } catch(RemoteException re) {
            log.error(re.getLocalizedMessage(), re);
        }
        return authKey;
    }
    
    /**
     * Main logout method for GeneGO
     * 
     * @param authKey the authenticate key from the login of the DEMO user
     */
    public void logout(String authKey)    {
        try {
            ggL.getGeneGOPort().logout(authKey);
        } catch(ServiceException se)    {
            log.error(se.getLocalizedMessage(), se);
        } catch(RemoteException re) {
            log.error(re.getLocalizedMessage(), re);
        }
    }
}