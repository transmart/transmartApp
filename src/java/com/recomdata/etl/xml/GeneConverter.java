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
