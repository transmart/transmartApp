


/**
 * $Id: WebClient.java 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
package com.recomdata.util.genego;

import org.apache.log4j.Logger;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;

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
    public String login() {
        GeneGOLocator ggL = new GeneGOLocator();
        String authKey = null;
        try {
            authKey = ggL.getGeneGOPort().login(DEMO_USER, DEMO_PWD);
        } catch (ServiceException se) {
            log.error(se.getLocalizedMessage(), se);
        } catch (RemoteException re) {
            log.error(re.getLocalizedMessage(), re);
        }
        return authKey;
    }

    /**
     * Main logout method for GeneGO
     *
     * @param authKey the authenticate key from the login of the DEMO user
     */
    public void logout(String authKey) {
        try {
            ggL.getGeneGOPort().logout(authKey);
        } catch (ServiceException se) {
            log.error(se.getLocalizedMessage(), se);
        } catch (RemoteException re) {
            log.error(re.getLocalizedMessage(), re);
        }
    }
}