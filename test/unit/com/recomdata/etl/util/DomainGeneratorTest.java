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
 * $Id: DomainGeneratorTest.java 9178 2011-08-24 13:50:06Z mmcduffie $
 */
package com.recomdata.etl.util;

import groovy.util.GroovyTestCase;
import com.recomdata.etl.db.MssqlConnectImpl;

/**
 * Unit test of DomainGenerator class
 *
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class DomainGeneratorTest extends GroovyTestCase {
	/**
	 * Test method for {@link com.recomdata.etl.util.DomainGenerator#generateDomain(java.lang.String, java.lang.String)}.
	 */
	public final void testGenerateDomain() {
		
		/*
		 * This test was failing on the attempt to create a connection to a non-existent database in the call MssqlConnectImpl.createStrangeLoveConnect() below.
		 * The code called looks like test/development code that is not referenced anywhere else in the project. 
		 * I have commented out the test for the time being, Februaru 14, 2013, Terry Weymouth
		 */

//		String tableName = "GeneExpressionAnalysisContent";
//		String className = "GeneExprAnalysisContent";
//
//		StringBuilder expected = new StringBuilder("class GeneExprAnalysisContent {\n");
//		expected.append("\t String contentid\n");
//		expected.append("\t String shortdescription\n");
//		expected.append("\t String longdescription\n");
//		expected.append("\t String experiment\n");
//		expected.append("\t String group1desc\n");
//		expected.append("\t String group2desc\n");
//		expected.append("\t String pvaluecutoff\n");
//		expected.append("\t String foldchangecutoff\n");
//		expected.append("\t String qacriteria\n");
//		expected.append("\t String compound\n");
//		expected.append("\t String source\n");
//		expected.append("\t String platform\n");
//		expected.append("\t String trial\n");
//		expected.append("\t String analyst\n");
//		expected.append("\t String analysisplatform\n");
//		expected.append("\t String id\n");
//		expected.append(" static mapping = {\n");
//		expected.append("\ttable 'GeneExpressionAnalysisContent'\n");
//		expected.append("\tversion false\n");
//		expected.append("\tcolumns {\n");
//		expected.append("\tcontentid column:'contentID'\n");
//		expected.append("\tshortdescription column:'shortDescription'\n");
//		expected.append("\tlongdescription column:'longDescription'\n");
//		expected.append("\texperiment column:'experiment'\n");
//		expected.append("\tgroup1desc column:'group1desc'\n");
//		expected.append("\tgroup2desc column:'group2desc'\n");
//		expected.append("\tpvaluecutoff column:'pValuecutoff'\n");
//		expected.append("\tfoldchangecutoff column:'foldchangecutoff'\n");
//		expected.append("\tqacriteria column:'QAcriteria'\n");
//		expected.append("\tcompound column:'compound'\n");
//		expected.append("\tsource column:'source'\n");
//		expected.append("\tplatform column:'platform'\n");
//		expected.append("\ttrial column:'trial'\n");
//		expected.append("\tanalyst column:'analyst'\n");
//		expected.append("\tanalysisplatform column:'analysisplatform'\n");
//		expected.append("\tid column:'id'\n");
//		expected.append("\t}\n\t}\n\n}");

	//	String actual =
//			DomainGenerator.generateDomain(MssqlConnectImpl.createStrangeLoveConnect(), tableName, className, null);

		// expected not defined 
		//assertEquals(actual, expected.toString());
	}
}
