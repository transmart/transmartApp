


/**
 *
 */
package com.recomdata.util.genego;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;

/**
 * @author JBoles
 */
public class GeneGoTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        GeneGoTest ggTest = new GeneGoTest();
        ggTest.testing();
    }

    public void testing() {
        GeneGOLocator ggL = new GeneGOLocator();
        String foo = ggL.getGeneGOPortAddress();
        System.out.println(foo);

        try {
            GeneGOPortType ggPT = ggL.getGeneGOPort();
            String authKey = ggPT.login("demo", "demo");
            System.out.println(authKey);
            String version = ggPT.getVersion(authKey);
            System.out.println(version);
            String mainURL = ggPT.getMainPageURL(authKey);
            System.out.println(mainURL);
            ggPT.logout(authKey);
            System.out.println("Logged Out");
        } catch (ServiceException se) {
            se.printStackTrace();
        } catch (RemoteException re) {
            re.printStackTrace();
        }
    }
}
