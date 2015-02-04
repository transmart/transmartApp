


/**
 * $Id: Gene.java 9178 2011-08-24 13:50:06Z mmcduffie $
 */
package com.recomdata.etl.xml;

/**
 * Gene bean
 *
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class Gene {
    private String name;
    private Double value;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public Double getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Double value) {
        this.value = value;
    }
}
