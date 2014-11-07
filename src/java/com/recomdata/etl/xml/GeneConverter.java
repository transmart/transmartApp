


/**
 * $Id: GeneConverter.java 9178 2011-08-24 13:50:06Z mmcduffie $
 */
package com.recomdata.etl.xml;

import com.thoughtworks.xstream.converters.SingleValueConverter;

/**
 * Provides basic toString/fromString for the Gene class
 *
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class GeneConverter implements SingleValueConverter {
    /**
     * @param obj Gene object whose name will be returned
     * @return the name of the Gene object
     */
    public String toString(Object obj) {
        return ((Gene) obj).getName();
    }

    /**
     * @param name the name of the Gene
     * @return null for now
     */
    public Object fromString(String name) {
        // return new Gene(name);
        return null;
    }

    /**
     * @param type the class type to see if we can convert to a Gene class
     * @return true if the class is of type Gene
     */
    public boolean canConvert(Class type) {
        return type.equals(Gene.class);
    }
}
