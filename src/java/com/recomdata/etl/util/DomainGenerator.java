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
 * $Id: DomainGenerator.java 9178 2011-08-24 13:50:06Z mmcduffie $
 */
package com.recomdata.etl.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.recomdata.etl.db.DBConnect;
import com.recomdata.etl.db.MssqlConnectImpl;
import com.recomdata.etl.db.OracleConnectImpl;

/**
 * Creates the domain class based on given table schema
 *
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class DomainGenerator {

	public static String DOMAIN_CLASS_DIRECTORY = "c:\\temp\\domains";

	public static void main(String[] args) {

	//	generateOracleSchemaDomain();
		generateMssqlDomainObj();

	}


	public static void generateMssqlDomainObj() {

		DBConnect connect = null;


		try {
			//connect =new MssqlConnectImpl("ph","rdc_DM", "sa","e");
			connect = new OracleConnectImpl("rd","orcl","dep","dep");
				//MssqlConnectImpl.createLocalConnect();
		//	generateDomain(connect, 
		//			"dm_patient_sat_summary", "PatientSatisfactionSummary", "dm");
		//	generateDomain(connect, 
		//			"dm_patient_sat_details", "PatientSatisfactionDetail", "dm");

		//	generateDomain(connect, 
		//			"DE_RC_SNP_INFO", "DeSNPInfo", "de");
			generateDomain(connect, 
					"DE_SUBJECT_SAMPLE_MAPPING", "DeSubjectSampleMap", "de");
			generateDomain(connect, 
					"QT_PATIENT_SET_COLLECTION", "QtPatientSet", "i2b2");

		}
		finally {
			if (connect != null)
				connect.closeConnection();
		}
	}


	public static void generateOracleSchemaDomain() {

		DBConnect connect = null;


		try {

			connect = OracleConnectImpl.createDW1BiomartCTConnect();
			//generateDomain(connect, "CZ_TEST","Test","qa");
			//generateDomain(connect, "AZ_TEST_RUN","TestRun","qa");
			//generateDomain(connect, "AZ_TEST_STEP_RUN","TestStepRun","qa");
			generateDomain(connect, "AZ_TEST_STEP_ACT_RESULT","TestStepRunResult","qa");


		/*	generateDomain(connect, "BIO_DATA_LITERATURE", "Literature", "bio");
			generateDomain(connect, "BIO_LIT_ALT_DATA", "LiteratureAlterationData", "bio");
			generateDomain(connect, "BIO_LIT_INH_DATA", "LiteratureInhibitorData", "bio");
			generateDomain(connect, "BIO_LIT_INT_DATA", "LiteratureInteractionData", "bio");
			generateDomain(connect, "BIO_LIT_PE_DATA", "LiteratureProteinEffectData", "bio");
			generateDomain(connect, "BIO_LIT_SUM_DATA", "LiteratureSummaryData", "bio");
			generateDomain(connect, "BIO_LIT_REF_DATA", "LiteratureReferenceData", "bio");
			generateDomain(connect, "BIO_LIT_MODEL_DATA", "LiteratureModelData", "bio");
			generateDomain(connect, "BIO_LIT_AMD_DATA", "LiteratureAssocMoleculeDetailsData", "bio");
	*/

		}
		finally {
			if (connect != null)
				connect.closeConnection();
		}
	}

	public static void initialOracleDomainClassNameGenerator() {

		DBConnect connect = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			connect = OracleConnectImpl.createHost2SearchAppConnect();



			String sql = "SELECT TABLE_NAME FROM USER_TABLES";
			st = connect.getConnection().createStatement();
			rs = st.executeQuery(sql);
			while(rs.next()){
				String tname = rs.getString(1);
				String[] allnames= tname.split("_");
				String pname = allnames[0].toLowerCase();
				String className = convertToClassName(tname.substring(tname.indexOf("_")+1));
				System.out.println("generateDomain(connect, \""+tname+"\", \""+className+"\", \""+pname+"\");");
			}
			//generateDomain(connect, "CONCEPT_DIMENSION", "ConceptDim");
			// generateDomain(connect, "", "");

		}catch(SQLException e){
			e.printStackTrace();
		}
		finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			}
			if (connect != null)
				connect.closeConnection();
		}

	}


	/**
	 * Creates the domain class based on table schema
	 *
	 * @param connect
	 *            - database connection
	 * @param tableName
	 *            the name of the table for the new domain class
	 * @param className
	 *            the new domain class name
	 *
	 *@param packageName
	 *			the new package name
	 */

	public static void generateDomain(DBConnect connect,
			String tableName,
			String className,
			String packageName) {
		PreparedStatement pst = null;
		ResultSet rs = null;

		StringBuilder s = new StringBuilder();
		if(packageName!=null){
			s.append("package ").append(packageName).append("\n");
		}
		s.append("class ");
		s.append(className);
		s.append(" {\n");



		try {
			String sql = "";
			String idmatch ="id";
			boolean oracle = true;
			boolean mssql = false;
			if (connect instanceof OracleConnectImpl) {
				sql = "select column_name, data_type, data_scale, data_length, nullable from user_tab_cols where table_name= ?";
				idmatch=tableName+"_ID";
			} else {
				oracle = false;
				mssql = true;
				sql = "SELECT COLUMN_NAME, DATA_TYPE, NUMERIC_SCALE, NULL,NULL FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ?";;
			}

			StringBuilder cons = new StringBuilder(" static constraints = {\n");
			StringBuilder sm = new StringBuilder(" static mapping = {\n");
			sm.append("\t table '");
			sm.append(tableName);
			sm.append("'\n");
			sm.append("\t version false\n");
			//sm.append("\t id column:'").append(idmatch).append("'\n");
			if(oracle){

				sm.append("\t id generator:'sequence', params:[sequence:'").append("SEQ_").append(packageName.toUpperCase()).append("_DATA_ID").append("']\n");
			}
			sm.append("\t columns {\n");

			pst = connect.getConnection().prepareStatement(sql);
			pst.setString(1, tableName);
			rs = pst.executeQuery();
			while (rs.next()) {

				String col = rs.getString(1);

				String type = rs.getString(2).toLowerCase();
				String scale = rs.getString(3);
				String datalength = rs.getString(4);
				String nullable = rs.getString(5);
				boolean isString =false;
				s.append("\t\t");
				if (type.startsWith("dec") || type.startsWith("num")
						|| type.startsWith("int")) {
					if (scale != null && Integer.valueOf(scale) > 0)
						s.append("Double ");
					else
						s.append("Long ");
				}else if (type.startsWith("date")){
					s.append("Date ");
				}
				else {
					s.append("String ");
					isString = true;
				}
				String newCol = col;
				if(idmatch.equalsIgnoreCase(col)){
					newCol = "id";
				}else{
					if (mssql) { // mssql we need to make all lower case to make
						// gorm happy
						newCol = col.replaceAll("_", "").toLowerCase();

					} else {

						if (newCol.length() >= 29) {
							System.out.println(newCol + " is too long");
						}
						newCol = convertToCamelCase(col);

					}
				}
				s.append(newCol).append("\n");

				if("Y".equalsIgnoreCase(nullable)|| isString){
					cons.append("\t").append(newCol).append("(");
					boolean p = false;
					if("Y".equalsIgnoreCase(nullable)){
						cons.append("nullable:true");
						p = true;
					}
					if(isString){
						if(p)
							cons.append(", ");
						cons.append("maxSize:").append(datalength);
					}
					cons.append(")\n");
				}
				if (!idmatch.equalsIgnoreCase(col)) {
					sm.append("\t\t").append(newCol).append(" column:'").append(
							col).append("'\n");
				}else {
					sm.append("\t\t").append("id column:'").append(idmatch).append("'\n");
				}
			}
			sm.append("\t\t}").append("\n");
			sm.append("\t}").append("\n");
			s.append(sm);
			s.append(cons);
			s.append("\t}");

			s.append("\n").append("}");
			String pname = packageName==null?"":File.separator+packageName;
			File dir = new File(DOMAIN_CLASS_DIRECTORY+pname);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File classFile = new File(dir, className + ".groovy");

			FileWriter f = null;
			try {
				f = new FileWriter(classFile);
				f.write(s.toString());
				f.close();
				System.out.print(s.toString());
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (f != null)
					try {
						f.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pst != null) {
					pst.close();
				}
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			}

		}
		// return s.toString();
	}


	private static String convertToCamelCase(String str){
		StringBuilder s = new StringBuilder();
		String[] all = str.toLowerCase().split("_");
		for(int i = 0; i<all.length; i++){
			String term = all[i];
			if(i>0){
				s.append(String.valueOf(term.charAt(0)).toUpperCase()).append(term.substring(1));
			}else {
				s.append(term);
			}
		}
		return s.toString();
	}
	private static String convertToClassName(String str){
		StringBuilder s = new StringBuilder();
		String[] all = str.toLowerCase().split("_");
		for(String term:all){

			s.append(String.valueOf(term.charAt(0)).toUpperCase()).append(term.substring(1));

		}
		return s.toString();
	}
}
