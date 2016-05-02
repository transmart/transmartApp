package org.transmartfoundation.status

import grails.util.Holders
import org.rosuda.REngine.REXP
import org.rosuda.REngine.REXPMismatchException
import org.rosuda.REngine.REngineException
import org.rosuda.REngine.Rserve.RConnection
import org.rosuda.REngine.Rserve.RserveException

class RserveStatusService {
    static String SIMPLE_EXPRESSION = "rnorm(10)"
    static String REQUIRED_PACKAGES_NAME = "required.packages";
    static String[] REQUIRED_PACKAGES_ARRAY = ["reshape2", "ggplot2", "data.table", "Cairo",
        "snowfall", "gplots", "Rserve", "foreach", "doParallel", "visreg",
        "WGCNA", "impute", "multtest", "CGHbase", "CGHtest","CGHtestpar",
        "edgeR", "snpStats", "preprocessCore", "GO.db", "AnnotationDbi"] as String[]
    static String MISSING_PACKAGES_EXPRESSION =
            "required.packages[!(required.packages %in% installed.packages()[,\"Package\"])]"

    def String lastErrorMessage = "";

    def getStatus() {

        def url = Holders.config.RModules.host + ":" + Holders.config.RModules.port

        def canConnect = false
        def evalSimpleExpression = false
        def librariesOk = false

        RConnection c
        try {
            c = new RConnection(Holders.config.RModules.host, Holders.config.RModules.port)
            canConnect = connectionExists(c)
            if (canConnect) {
                evalSimpleExpression = willEvaluateSimpleExpression(c)
                librariesOk = hasNecessaryDependencies(c)
            }
        } catch (Exception e) {
            lastErrorMessage = "Probe failed with Exception: " + e.message
        } finally {
            if (c) closeConnection(c)
        }
		def settings = [
             'url'                  : url,
			'connected'             : canConnect,
            'simpleExpressionOK'    : evalSimpleExpression,
            'librariesOk'           : librariesOk,
            'lastErrorMessage'      : lastErrorMessage,
			'lastProbe'             : new Date()
		]
		
		RserveStatus status = new RserveStatus(settings)
		return status
	}

    boolean connectionExists(RConnection c) {
        lastErrorMessage = "";
        if (c == null){
            lastErrorMessage = "Connection returned null";
            return false;
        }
        if (!(c instanceof RConnection)) {
            lastErrorMessage = "Connection returned unrecognized object";
            return false;
        }
        lastErrorMessage = "";
        return true;
    }

    public boolean willEvaluateSimpleExpression(RConnection c) {
        lastErrorMessage = "";

        def d

        REXP results = evaluate(c,SIMPLE_EXPRESSION);
        if (results == null) {
            lastErrorMessage = "Probe = simple epression; returned null";
            return false;
        }
        try {
            d = evaluate(c,SIMPLE_EXPRESSION).asDoubles();
        } catch (REXPMismatchException e) {
            System.out.println(e.getLocalizedMessage());
            lastErrorMessage = "Probe = simple epression; exception = " + e.getLocalizedMessage();
            return false;
        }
        if (d.length == 10) {
            lastErrorMessage = "";
            return true;
        }
        lastErrorMessage = "Probe = simple epression; wrong returned value.";
        return false;
    }

    public boolean hasNecessaryDependencies(RConnection c) {
        lastErrorMessage = "";

        List<String> list = determineMissingPackages(c);
        if (list == null) {
            // determineMissingPackages will have set lastErrorMessage
            logger.debug("Return from hasNecessaryDependencies because missing packages array is null");
            return false;
        }

        boolean ok = (list.size() == 0);

        if (!ok) {
            lastErrorMessage = "list of dependencies is not empty";
        }
        lastErrorMessage = "";
        return ok;
    }

    public List<String> determineMissingPackages(RConnection c) {
        lastErrorMessage = "";
        try {
            c.assign(REQUIRED_PACKAGES_NAME,REQUIRED_PACKAGES_ARRAY);
        } catch (REngineException e) {
            logger.debug("Return from determineMissingPackages because assignment failed!");
            logger.debug("  " + e.getLocalizedMessage());
            lastErrorMessage = "exception in assignment of required packages: " + e.getLocalizedMessage();
            return null;
        }
        String[] array = new String[0];
        REXP results = evaluate(c,MISSING_PACKAGES_EXPRESSION);
        if (results != null) { // null may be returned when expression returns null - Character(0)
            try {
                array = results.asStrings();
            } catch (REXPMismatchException e) {
                logger.debug("Return from determineMissingPackages because conversion of package array failed!");
                logger.debug("  " + e.getLocalizedMessage());
                lastErrorMessage = "Exception in converting results to an String array: " + e.getLocalizedMessage();
                return null;
            }
        }
        List<String> list = new ArrayList<String>();
        for (String name: array) {
            list.add(name);
        }
        lastErrorMessage = "";
        return list;
    }

    public void closeConnection(RConnection c){
        if (c == null) return;
        c.close();
        c = null;
    }

    private REXP evaluate(RConnection c,String expression) {
        REXP results = null;
        if (c != null) {
            try {
                results = c.eval(expression);
            } catch (RserveException e) {
                results = null;
            }
        }
        return results;
    }

    String getLastErrorMessage(){
        return lastErrorMessage;
    }

}
