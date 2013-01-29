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
 * $Id: XstreamTest.java 9178 2011-08-24 13:50:06Z mmcduffie $
 */
package com.recomdata.etl.xml;

import groovy.util.GroovyTestCase;

import com.thoughtworks.xstream.XStream;

/**
 * Unit test of the Xstream class
 * 
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class XstreamTest extends GroovyTestCase {	
	/**
	 * Test method for {@link com.thoughtworks.xstream.XStream#toXML()}.
	 */
	public void testXstream() {
		Gene g = new Gene();
		g.setName("Unit Test");
		g.setValue(1.0);
		XStream xstream = new XStream();
		xstream.alias("gene1", Gene.class);
		
		StringBuilder expected = new StringBuilder("<gene1>\n");
		expected.append("  <name>Unit Test</name>\n");
		expected.append("  <value>1.0</value>\n");
		expected.append("</gene1>");
		
		String actual = xstream.toXML(g);
		
		assertEquals(actual, expected.toString());
	}
}
