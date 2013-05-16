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
 * $Id: GeneConverterTest.java 9178 2011-08-24 13:50:06Z mmcduffie $
 */
package com.recomdata.etl.xml;

import groovy.util.GroovyTestCase;

/**
 * Unit test of the GeneConverter class
 * 
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class GeneConverterTest extends GroovyTestCase {
	private static GeneConverter gc = new GeneConverter();	
	private static String name = "Unit Test";	

	/**
	 * Test method for {@link com.recomdata.etl.xml.GeneConverter#toString(java.lang.Object)}.
	 */
	public void testToStringObject() {
		Gene g = new Gene();
		g.setName(name);
		assertEquals(gc.toString(g), name);		
	}

	/**
	 * Test method for {@link com.recomdata.etl.xml.GeneConverter#fromString(java.lang.String)}.
	 */
	public void testFromString() {
		assertNull(gc.fromString(name));
	}

	/**
	 * Test method for {@link com.recomdata.etl.xml.GeneConverter#canConvert(java.lang.Class)}.
	 */
	public void testCanConvert() {
		assertTrue(gc.canConvert(Gene.class));
	}
}
