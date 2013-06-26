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
 * $Id: GeneTest.java 9178 2011-08-24 13:50:06Z mmcduffie $
 */
package com.recomdata.etl.xml;

import groovy.util.GroovyTestCase;

/**
 * Unit test of the Gene class
 * 
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class GeneTest extends GroovyTestCase {
	private static Gene g = new Gene();
	private static String name = "Unit Test";
	private static Double value = 1.0;

	/**
	 * Test method for {@link com.recomdata.etl.xml.Gene#getName()}.
	 */
	public void testGetName() {
		g.setName(name);
		assertEquals(g.getName(), name);		
	}

	/**
	 * Test method for {@link com.recomdata.etl.xml.Gene#setName(java.lang.String)}.
	 */
	public void testSetName() {
		String actual = "foo";
		g.setName(actual);
		assertEquals(g.getName(), actual);
	}

	/**
	 * Test method for {@link com.recomdata.etl.xml.Gene#getValue()}.
	 */
	public void testGetValue() {
		g.setValue(value);
		assertEquals(g.getValue(), value);
	}

	/**
	 * Test method for {@link com.recomdata.etl.xml.Gene#setValue(java.lang.Double)}.
	 */
	public void testSetValue() {
		Double actual = 100.5;
		g.setValue(actual);
		assertEquals(g.getValue(), actual);
	}
}
