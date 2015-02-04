


/**
 * $Id: IExcelProfile.java 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
package com.recomdata.util;

/**
 * Interface that is implemented by domain objects that will be exported to Excel
 */
public interface IBioTag {
    /**
     * Retrieves the object UID value
     *
     * @return a String of values
     */
    public String getBioUID();
}
