/**
 * Created with IntelliJ IDEA.
 * User: sbedard
 * Date: 8/8/13
 */

import org.springframework.context.ApplicationContext;

class TransmartQueryDefinition {

    ApplicationContext ctx = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getAttribute(org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes.APPLICATION_CONTEXT)
    def dataSource = ctx.getBean('dataSource')
    def i2b2HelperService = ctx.getBean('i2b2HelperService');

    public TransmartQueryDefinition(String resultInstanceId)
    {
        this.resultInstanceId = resultInstanceId;
    }

    private HashMap<String, List<List<TransmartQueryItem>>> conceptDistributions;

    String resultInstanceId;
    String queryTiming;
    List<TransmartQueryPanel> panels = new ArrayList<TransmartQueryPanel>();

    Boolean getIsSameEvent() {
        return queryTiming == "SAMEVISIT";
    }

    HashMap<String, List<List<TransmartQueryItem>>> getDistributions()
    {
        if (!conceptDistributions) {
            conceptDistributions = i2b2HelperService.loadDistributions(this)  ;
        };

        return conceptDistributions;
    }

    Boolean isSetInDistribution(ArrayList<TransmartQueryItem> dL, ArrayList<ArrayList<TransmartQueryItem>> setOfLists) {

        for (l in setOfLists)
        {
            def newColl = (l - dL) + (dL - l);

            if (newColl.size() == 0)
            {
                return true;
            }
        }

        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    Boolean isKeyInDistributionsUsingLongPath(String longPath)
    {
        def shortPath = longPath.substring(longPath.indexOf("\\", 2), longPath.length());
        return distributions.containsKey(shortPath);

    }

    Boolean isKeyInPanels(String conceptKey) {

        Boolean found = false;
        for (p in panels)
        {
           for (i in p.items)
           {
             if (i.itemKey == conceptKey)
             {
                 found = true;
                 break;
             }
           }
        }

        return found;
    }



}
