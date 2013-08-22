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
  

import java.util.List;

import java.io.BufferedWriter;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import i2b2.SnpProbeSortedDef;

/**
 * $Id: I2b2HelperService.groovy 11303 2011-12-23 06:05:17Z mkapoor $
 */
import groovy.sql.*;
import i2b2.Concept;
import i2b2.GeneWithSnp
import i2b2.SnpDataByPatient;
import i2b2.SnpDataByProbe;
import i2b2.SnpDataset;
import i2b2.SnpDatasetByPatient;
import i2b2.SnpDatasetListByProbe;
import i2b2.SnpInfo
import i2b2.StringLineReader;
import i2b2.SampleInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.sql.*;

import org.jfree.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.transmart.CohortInformation;
import org.transmart.searchapp.AuthUser;
import org.transmart.searchapp.AuthUserSecureAccess;
import org.transmart.searchapp.SecureObjectPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import oracle.jdbc.driver.OracleTypes

import org.Hibernate.*;

import com.recomdata.db.DBHelper;
import com.recomdata.export.*;

import com.recomdata.i2b2.SurvivalConcepts;

/**
 * ResNetService that will provide an .rnef file for Jubilant data
 *
 * @author $Author: mkapoor $
 * @version $Revision: 11303 $
 */
class I2b2HelperService {
	
	static String GENE_PATTERN_WHITE_SPACE_DEFAULT = "0";
	static String GENE_PATTERN_WHITE_SPACE_EMPTY = "";
	
	boolean transactional = false;
	def sessionFactory
	def dataSource;
	def conceptService;
	def sampleInfoService;
	
	/**
	 * Gets a distribution of information from the patient dimention table for value columns
	 */
	def double[] getPatientDemographicValueDataForSubset(String col, String result_instance_id) {
		ArrayList<Double> values=new ArrayList<Double>();
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		String sqlt = """SELECT """ + col + """ FROM patient_dimension f WHERE
		    PATIENT_NUM IN (select distinct patient_num
			from qt_patient_set_collection
			where result_instance_id = ?)""";
		sql.eachRow(sqlt, [result_instance_id], {row ->
			values.add(row[0])
		});
		double[] returnvalues=new double[values.size()];
		for(int i=0;i<values.size();i++) {
			returnvalues[i]=values.get(i);
		}
		return returnvalues;
	}
	
	
	/**
	 * Converts a concept key to a path
	 */
	def keyToPath(String concept_key) {
		log.trace("keytoPath from key: "+concept_key);
		String fullname=concept_key.substring(concept_key.indexOf("\\",2), concept_key.length());
		String path=fullname;
		if(!fullname.endsWith("\\")) {
			path=path+"\\";
		}
		return path;
	}
	
	/**
	 *  Gets the parent concept key of a concept key
	 */
	def String getParentConceptKey(String concept_key) {
		
		//	String parent=
		return concept_key.substring(0,concept_key.lastIndexOf("\\", concept_key.length()-2)+1);
		//	if(concept_key!=null && concept_key.endsWith("\\")){
		//	parent =concept_key.substring(0,concept_key.lastIndexOf("\\", concept_key.length()-2));
		//		logMessage("parent="+parent);
		//		}else {
		//concept_key.substring(0,concept_key.lastIndexOf("\\"));
		//  }
		//	return parent;
	}
	
	/**
	 * Gets the short display name from a concept key
	 */
	def String getShortNameFromKey(String concept_key) {
		String[] splits=concept_key.split("\\\\");
		String concept_name="";
		if(splits.length>2) {
			concept_name="...\\"+splits[splits.length-3]+"\\"+splits[splits.length-2]+"\\"+splits[splits.length-1];
		}
		else if(splits.length>1) {
			concept_name="...\\"+splits[splits.length-2]+"\\"+splits[splits.length-1];
		}
		else concept_name=splits[splits.length-1];
		return concept_name;
	}
	
	/**
	 * Gets a grid column name from a concept key
	 */
	def String getColumnNameFromKey(String concept_key) {
		String[] splits=concept_key.split("\\\\");
		String concept_name="";
		//if(splits.length>1)
		//{
		//	concept_name="...\\"+splits[splits.length-2]+"\\"+splits[splits.length-1];
		//}
		//else
		concept_name=splits[splits.length-1];
		return concept_name;
	}
	
	/**
	 * Gets the concept codes associated with a concept key (comma delimited string returned)
	 */
	def String getConceptCodeFromKey(String key)  {
		log.trace("Getting concept codes for key:" +key);
		//String slash="\\";
		//logMessage("Here is slash: "+slash);
		StringBuilder concepts = new StringBuilder();
		String path=key.substring(key.indexOf("\\",2), key.length());
		//path=path.replace("@", slash);
		if(!path.endsWith("\\")){
			path +="\\";
		}
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlt =
				sql.eachRow("SELECT CONCEPT_CD FROM CONCEPT_DIMENSION c WHERE CONCEPT_PATH = ?", [path], {row ->
					log.trace("Found code:"+row.CONCEPT_CD);
					concepts.append(row.CONCEPT_CD);
				});
		log.trace("Done getting concept codes for key:" +key);
		return concepts.toString();
	}
	
	def String getConceptPathFromCode(String code) {
		String path = null;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		String sqlt =
				sql.eachRow("SELECT CONCEPT_PATH FROM CONCEPT_DIMENSION c WHERE CONCEPT_CD = ?", [code], {row ->
					path = row.CONCEPT_PATH;
				})
		return path;
	}
	
	/**
	 * Gets concept key for analysis, does logic to return parent folder if a leaf
	 * is not a value type leaf
	 */
	def String getConceptKeyForAnalysis(String concept_key_in) {
		if(isLeafConceptKey(concept_key_in)) {
			if(isValueConceptKey(concept_key_in)) {
				return concept_key_in; //just use me cause im a value node
			}
			else return getParentConceptKey(concept_key_in); //get parent folder (could make recursive)
		}
		else return concept_key_in; //must be folder
	}
	
	/**
	 * Gets the level from a concept key (level indicates depth in tree)
	 */
	def  int getLevelFromKey(String key) {
		log.trace("Getting level from key:"+key);
		String fullname=key.substring(key.indexOf("\\",2), key.length());
		//path=path.replace("@", slash);
		int res=0;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlt =
				sql.eachRow("SELECT c_hlevel FROM i2b2metadata.i2b2 WHERE C_FULLNAME = ?", [fullname], {row ->
					res=row.c_hlevel
				})
		log.trace("Level is:"+res);
		return res;
	}
	
	def getMarkerTypeFromConceptCd(conceptCd){
		log.trace("Getting marker type from concept code:"+conceptCd);
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		
		def markerType = ""
		//def rs = sql.executePreparedQuery("select dgi.marker_type from concept_dimension cd, de_gpl_info dgi where cd.name_char=dgi.title "+
		//		"and cd.concept_cd = ?",[conceptCd])
		sql.eachRow("select dgi.marker_type from concept_dimension cd, de_gpl_info dgi where cd.concept_path like('%'||dgi.title||'%') "+
				"and cd.concept_cd = ?",[conceptCd], {row ->
					markerType = row.marker_type
		})
		//return "Gene Expression"
		//return "SNP"
		return markerType
	}
	
	/**
	 * Determines if a concept key is a value concept or not
	 */
	def Boolean isValueConceptKey(String concept_key) {
		return isValueConceptCode(getConceptCodeFromKey(concept_key));
	}
	
	/**
	 * Determines if a concept key is a leaf or not
	 */
	def Boolean isLeafConceptKey(String concept_key) {
		String fullname=concept_key.substring(concept_key.indexOf("\\",2), concept_key.length());
		Boolean res=false;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		sql.eachRow("SELECT C_VISUALATTRIBUTES FROM I2B2METADATA.I2B2 WHERE C_FULLNAME = ?", [fullname], {row ->
			res=row.c_visualattributes.indexOf('L')>-1
		})
		return res;
	}
	
	/**
	 * Gets the distinct patient counts for the children of a parent concept key
	 */
	def getChildrenWithPatientCountsForConcept(String concept_key) {
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		def counts = [:];
		log.trace("Trying to get counts for parent_concept_path="+keyToPath(concept_key));
		sql.eachRow("select * from CONCEPT_COUNTS where parent_concept_path = ?", [keyToPath(concept_key)], {row ->
			log.trace "Found " << row.concept_path
			counts.put(row.concept_path, row.patient_count)
		});
		return counts;
	}
	
	/**
	 * Gets the data associated with a value type concept from observation fact table
	 * for display in a distribution histogram
	 */
	def getConceptDistributionDataForValueConcept(String concept_key) {
		log.trace("Getting concept distribution data for value concept: " +concept_key);
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String concept_cd=getConceptCodeFromKey(concept_key);
		ArrayList<Double> values=new ArrayList<Double>();
		sql.eachRow("SELECT NVAL_NUM FROM OBSERVATION_FACT f WHERE CONCEPT_CD = ?", [concept_cd], {row ->
			if(row.NVAL_NUM!=null)	{
				values.add(row.NVAL_NUM);
				log.trace("adding"+row.NVAL_NUM);
			}
		});
		ArrayList<Double> returnvalues=new ArrayList<Double>(values.size());
		for(int i=0;i<values.size();i++) {
			log.trace("trying to add"+values.get(i));
			returnvalues[i]=values.get(i);
		}
		return returnvalues;
	}
	
	/**
	 *  Gets the data associated with a value type concept from observation fact table
	 * for display in a distribution histogram for a given subset
	 */
	
	def getConceptDistributionDataForValueConcept(String concept_key, String result_instance_id) 
	{
		log.debug("Getting concept distribution data for value concept:"+concept_key);
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String concept_cd=getConceptCodeFromKey(concept_key);
		ArrayList<Double> values=new ArrayList<Double>();
		
		log.debug("concept_cd: " + concept_cd);
		log.debug("result_instance_id: " + result_instance_id);
		
		log.debug("getConceptDistributionDataForValueConcept: preparing query");
		//String sqlt=""""SELECT NVAL_NUM FROM OBSERVATION_FACT f WHERE CONCEPT_CD = ? AND PATIENT_NUM IN (select distinct patient_num
		//        from qt_patient_set_collection where result_instance_id = ?)""";
		
		String sqlt="SELECT NVAL_NUM FROM OBSERVATION_FACT f WHERE CONCEPT_CD = '" +
				concept_cd + "' AND PATIENT_NUM IN (select distinct patient_num " +
				"from qt_patient_set_collection where result_instance_id = " + result_instance_id + ")";
		
		log.debug("executing query: sqlt=" + sqlt);
		try {
			//sql.eachRow(sqlt, [concept_cd, result_instance_id], {row ->
			sql.eachRow(sqlt, {row ->
				if(row.NVAL_NUM!=null) {
					values.add(row.NVAL_NUM);
				}
			});
		} catch (Exception e) {
			log.error("exception in getConceptDistributionDataForValueConcept: " + e.getMessage())
		}
		ArrayList<Double> returnvalues = new ArrayList<Double>(values.size());
		for(int i=0;i<values.size();i++) {
			returnvalues[i]=values.get(i);
		}
		log.debug("getConceptDistributionDataForValueConcept now finished");
		return returnvalues;
	}
	
	def getConceptDistributionDataForValueConceptFromCode(String concept_cd, String result_instance_id) {
		ArrayList<Double> values=new ArrayList<Double>();
		ArrayList<Double> returnvalues=new ArrayList<Double>(values.size());
		if (result_instance_id==""){
			log.debug("getConceptDistributionDataForValueConceptFromCode called with no result_istance_id");
			return returnvalues;
		}
		log.trace("Getting concept distribution data for value concept code:"+concept_cd);
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		log.trace("preparing query");
		String sqlt="""SELECT NVAL_NUM FROM OBSERVATION_FACT f WHERE CONCEPT_CD = ? AND
		    PATIENT_NUM IN (select distinct patient_num
			from qt_patient_set_collection
			where result_instance_id = ?)""";
		log.debug("executing query: "+sqlt);
		sql.eachRow(sqlt, [
			concept_cd,
			result_instance_id
		], {row ->
			if(row.NVAL_NUM!=null) {
				values.add(row.NVAL_NUM);
			}
		})
		for(int i=0;i<values.size();i++) {
			returnvalues[i]=values.get(i);
		}
		log.trace("getConceptDistributionDataForValueConceptFromCode now finished");
		return returnvalues;
	}
	
	/**
	 *  Gets the count of a a patient set fromt he result instance id
	 */
	def  Integer getPatientSetSize(String result_instance_id) {
		log.trace("Getting patient set size with id:" + result_instance_id);
		Integer i=0;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

		String sqlt = """select count(distinct(patient_num)) as patcount FROM qt_patient_set_collection
				where result_instance_id = ?""";

		log.trace(sqlt);
		sql.eachRow(sqlt, [result_instance_id], {row ->
			log.trace("inrow");
			i=row.patcount;
			log.trace(row.patcount);
		});
		return i;
	}
	
	/**
	 *  Gets the intersection of the patient sets from two result instance ids
	 */
	def int getPatientSetIntersectionSize(String result_instance_id1, String result_instance_id2) {
		log.trace("Getting patient set intersection");
		Integer i=0;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlt = """Select count(*) as patcount FROM ((select distinct patient_num from qt_patient_set_collection
		        where result_instance_id = ?) a inner join (select distinct patient_num from qt_patient_set_collection
		        where result_instance_id = ?) b ON a.patient_num=b.patient_num)""";
		log.trace(sqlt);
		sql.eachRow(sqlt, [
			result_instance_id1,
			result_instance_id2
		], {row ->
			log.trace("inrow of intersection")
			i=row.patcount;
			log.trace(row.patcount);
		})
		return i;
	}
	
	/**
	 * Converts a clob to a string I hope
	 */
	def String clobToString(clob) {
		if(clob==null) return "";
        if (clob instanceof java.lang.String)
        {
            return clob;
        }
		def buffer = new byte[1000];
		def num = 0;
		def inStream = clob.asciiStream;
		def out = new ByteArrayOutputStream();
		while ((num = inStream.read(buffer)) > 0) {
			out.write (buffer,0,num);
		}
		return new String(out.toByteArray());
	}
	
	/**
	 * Determines if a concept code is a value concept code or not by checking the metadata xml
	 */
	def Boolean isValueConceptCode(String concept_code) {
		log.trace("Checking isValueConceptCode for code:"+concept_code);
		Boolean res=false;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlt = "SELECT C_METADATAXML FROM I2B2METADATA.I2B2 WHERE C_BASECODE = ?"
		String xml="";
		log.trace(sqlt);
		sql.eachRow(sqlt, [concept_code], {row ->
			log.trace("checking metadata xml:"+row.c_metadataxml);
			xml=clobToString(row.c_metadataxml);
		});
		log.trace("METADATA XML:" +xml);
		if(!xml.equalsIgnoreCase("")) {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true); // never forget this!
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xml)));
			
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			//xpath.setNamespaceContext(new QueryNamespaceContext());
			//XPathExpression expr  = xpath.compile("item");
			Object result=xpath.evaluate("//ValueMetadata/Oktousevalues", doc, XPathConstants.NODE);
			//Object result = expr.evaluate(doc, XPathConstants.NODESET);
			//NodeList nodes = (NodeList) result;
			Node x=(Node) result;
			String key=x.getTextContent();
			//   logMessage("Found oktousevalues: "+key);
			if(key.equalsIgnoreCase("Y")){res=true;
			}
		}
		return res;
	}
	
	/**
	 * Gets the distribution of data for a concept
	 */
	def HashMap<String,Integer> getConceptDistributionDataForConceptOld(String concept_key, String result_instance_id) throws SQLException {
		String fullname=concept_key.substring(concept_key.indexOf("\\",2), concept_key.length());
		HashMap<String,Integer> results = new LinkedHashMap<String, Integer>();
		int i=getLevelFromKey(concept_key)+1;
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		String sqlt = """Select DISTINCT m.c_name, nvl(i.obscount,0) as obscount FROM
		    (SELECT c_name, c_basecode FROM i2b2metadata.i2b2 WHERE C_FULLNAME LIKE ? AND c_hlevel = ?) m
		    LEFT OUTER JOIN
		    (Select c_name, count(c_basecode) as obscount FROM
			(SELECT c_name, c_basecode FROM i2b2metadata.i2b2 WHERE C_FULLNAME LIKE ? AND c_hlevel = ?) c
			INNER JOIN observation_fact f ON f.concept_cd=c.c_basecode
			WHERE PATIENT_NUM IN (select distinct patient_num from qt_patient_set_collection where result_instance_id = ?)
		    GROUP BY c_name) i
		    ON i.c_name=m.c_name
		    ORDER BY c_name""";
		sql.eachRow(sqlt, [
			fullname+"%",
			i,
			fullname+"%",
			i,
			result_instance_id
		], {row ->
			results.put(row[1], row[2])
		})
		return results;
	}
	
	/**
	 *  Gets the concept distributions for a concept in a subset
	 */
	def  HashMap<String,Integer> getConceptDistributionDataForConcept(String concept_key, String result_instance_id) throws SQLException {
		String fullname=concept_key.substring(concept_key.indexOf("\\",2), concept_key.length());
		HashMap<String,Integer> results = new LinkedHashMap<String, Integer>();
		
		// check to see if there is a mapping from this concept_key to a concept_key for the results
		log.debug("getConceptDistributionDataForConcept: looking up parent_concept of fullname: " + fullname)
		String parent_concept = lookupParentConcept(fullname);
		log.debug("getConceptDistributionDataForConcept: parent_concept: "+parent_concept);
		Set<String>	concepts = new HashSet<String>();
		if (parent_concept != null) {
			// lookup appropriate children
			Set<String>	childConcepts = lookupChildConcepts(parent_concept, result_instance_id);
			if (childConcepts.isEmpty()) {
				childConcepts.add(concept_key);
			}
			log.debug("getConceptDistributionDataForConcept: childConcepts: "+childConcepts);
			for (c in childConcepts) {
				int i=getLevelFromKey(concept_key)+1;
				fullname = getConceptPathFromCode(c);
				log.debug("** IN LOOP: fullname: "+fullname);
				groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
				String sqlt =
						"SELECT DISTINCT c_name, c_fullname FROM i2b2metadata.i2b2 WHERE C_FULLNAME LIKE ? AND c_hlevel = ? ORDER BY C_FULLNAME";
				log.trace(sqlt);
				sql.eachRow(sqlt, [fullname+"%", i], {row ->
					if (results.get(row[0]) == null) {
						results.put(row[0], getObservationCountForConceptForSubset("\\blah"+row[1], result_instance_id));
					} else {
						results.put(row[0], results.get(row[0]) + getObservationCountForConceptForSubset("\\blah"+row[1], result_instance_id));
					}
				})
			}
		} else {
			int i=getLevelFromKey(concept_key)+1;
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
			String sqlt = "SELECT DISTINCT c_name, c_fullname FROM i2b2metadata.i2b2 WHERE C_FULLNAME LIKE ? AND c_hlevel = ? ORDER BY C_FULLNAME";
			log.trace(sqlt);
			sql.eachRow(sqlt, [fullname+"%", i], {row ->
				results.put(row[0], getObservationCountForConceptForSubset("\\blah"+row[1], result_instance_id));
			});
		}
		return results;
	}
	
	/**
	 * Gets the children value type concepts of a parent key
	 */
	def List<String> getChildValueConceptsFromParentKey(String concept_key) {
		String prefix=concept_key.substring(0, concept_key.indexOf("\\",2)); //get the prefix to put on to the fullname to make a key
		String fullname=concept_key.substring(concept_key.indexOf("\\",2), concept_key.length());
		
		String xml;
		ArrayList ls=new ArrayList();
		int i=getLevelFromKey(concept_key)+1;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlt = "SELECT C_FULLNAME, C_METADATAXML FROM i2b2metadata.i2b2 WHERE C_FULLNAME LIKE ? AND c_hlevel = ? ORDER BY C_FULLNAME";
		sql.eachRow(sqlt, [fullname+"%", i], {row ->
			String conceptkey=prefix+row.c_fullname;
			xml=clobToString(row.c_metadataxml);
			log.trace("METADATA XML:" +xml);
			if(!xml.equalsIgnoreCase("")) {
				DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
				domFactory.setNamespaceAware(true); // never forget this!
				DocumentBuilder builder = domFactory.newDocumentBuilder();
				Document doc = builder.parse(new InputSource(new StringReader(xml)));
				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();
				//xpath.setNamespaceContext(new QueryNamespaceContext());
				//XPathExpression expr  = xpath.compile("item");
				Object result=xpath.evaluate("//ValueMetadata/Oktousevalues", doc, XPathConstants.NODE);
				//Object result = expr.evaluate(doc, XPathConstants.NODESET);
				//NodeList nodes = (NodeList) result;
				Node x=(Node) result;
				String key=x.getTextContent();
				log.trace("Found oktousevalues: "+key);
				if(key.equalsIgnoreCase("Y"))
				{
					ls.add(conceptkey);
				}
			}
		});
		return ls;
	}
	
	/**
	 *  Returns the patient count for a concept key
	 */
	def  Integer getPatientCountForConcept(String concept_key) {
		String fullname=concept_key.substring(concept_key.indexOf("\\",2), concept_key.length());
		int i=0;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlt = """select count (distinct patient_num) as patcount
		    FROM i2b2demodata.observation_fact
		    WHERE (((concept_cd IN (select concept_cd from i2b2demodata.concept_dimension c
		    where concept_path LIKE ?))))""";
		sql.eachRow(sqlt, [fullname+"%"], {row ->
			i= row[1];
		})
		return i;
	}
	
	
	/**
	 * Gets the count of the observations in the fact table for a concept and a subset
	 */
	def Integer getObservationCountForConceptForSubset(String concept_key, String result_instance_id) {
		log.trace("Getting observation count for concept:"+concept_key+" and instance:"+result_instance_id);
		String fullname=concept_key.substring(concept_key.indexOf("\\",2), concept_key.length());
		int i=0;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlt = """select count (*) as obscount FROM i2b2demodata.observation_fact
		    WHERE (((concept_cd IN (select concept_cd from i2b2demodata.concept_dimension c
			where concept_path LIKE ?)))) AND PATIENT_NUM IN (select distinct patient_num from qt_patient_set_collection where result_instance_id = ?)""";
		sql.eachRow(sqlt, [
			fullname+"%",
			result_instance_id
		], {row ->
			i= row[0]
		})
		return i;
	}
	
	/**
	 * Fills the main demographic data in an export table for the grid
	 */
	def ExportTableNew addAllPatientDemographicDataForSubsetToTable(ExportTableNew tablein, String result_instance_id, String subset) {
		log.trace("Adding patient demographic data to grid with result instance id:" +result_instance_id+" and subset: "+subset)
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		String sqlt = """SELECT * FROM patient_dimension p INNER JOIN patient_trial t ON p.patient_num=t.patient_num
		    WHERE p.PATIENT_NUM IN (select distinct patient_num from qt_patient_set_collection where result_instance_id = ?)
		    ORDER BY p.PATIENT_NUM""";
		
		//if i have an empty table structure so far
		if(tablein.getColumns().size()==0)
		{
			tablein.putColumn("subject", new ExportColumn("subject", "Subject", "", "String"));
			tablein.putColumn("patient", new ExportColumn("patient", "Patient", "", "String"));
			tablein.putColumn("subset", new ExportColumn("subset", "Subset", "", "String"));
			//tablein.putColumn("BIRTH_DATE", new ExportColumn("BIRTH_DATE", "Birth Date", "", "Date"));
			//tablein.putColumn("DEATH_DATE", new ExportColumn("DEATH_DATE", "Death Date", "", "Date"));
			tablein.putColumn("TRIAL", new ExportColumn("TRIAL", "Trial", "", "String"));
			tablein.putColumn("SEX_CD", new ExportColumn("SEX_CD", "Sex", "", "String"));
			tablein.putColumn("AGE_IN_YEARS_NUM", new ExportColumn("AGE_IN_YEARS_NUM", "Age", "", "Number"));
			//tablein.putColumn("LANGUAGE_CD", new ExportColumn("LANGUAGE_CD", "Language", "", "String"));
			tablein.putColumn("RACE_CD", new ExportColumn("RACE_CD", "Race", "", "String"));
			//tablein.putColumn("MARITAL_STATUS_CD", new ExportColumn("MARITAL_STATUS_CD", "Marital Status", "", "Number"));
			//tablein.putColumn("RELIGION_CD", new ExportColumn("RELIGION_CD", "Religion", "", "String"));
			//tablein.putColumn("ZIP_CD", new ExportColumn("ZIP_CD", "Zipcode", "", "String"));
		}
		//def founddata=false;
		sql.eachRow(sqlt, [result_instance_id], {row ->
			/*If I already have this subject mark it in the subset column as belonging to both subsets*/
			//founddata=true;
			String subject=row.PATIENT_NUM;
			if(tablein.containsRow(subject))
			{
				String s=tablein.getRow(subject).get("subset");
				s=s+","+subset;
				tablein.getRow(subject).put("subset", s);
			}
			else /*fill the row*/ {
				ExportRowNew newrow=new ExportRowNew();
				newrow.put("subject", subject);
				def arr = row.SOURCESYSTEM_CD?.split(":")
				newrow.put("patient", arr?.length == 2 ? arr[1] : "");
				newrow.put("subset", subset);
				newrow.put("TRIAL", row.TRIAL)
				newrow.put("SEX_CD", row.SEX_CD)
				newrow.put("AGE_IN_YEARS_NUM", row.AGE_IN_YEARS_NUM.toString())
				newrow.put("RACE_CD", row.RACE_CD)
				tablein.putRow(subject, newrow);
			}
		})
		//log.trace("FOUND DEMOGRAPHIC DATA=:"+founddata.toString())
		return tablein;
	}
	
	/**
	 * Adds a column of data to the grid export table
	 */
	def ExportTableNew addConceptDataToTable(ExportTableNew tablein,String concept_key,String result_instance_id) {
		if(isLeafConceptKey(concept_key)) {
			String columnid=getShortNameFromKey(concept_key).replace(" ", "_").replace("...", "");
			String columnname=getColumnNameFromKey(concept_key).replace(" ", "_");
			//String columnid="...test\\test";
			/*add the column to the table if its not there*/
			if(tablein.getColumn("subject")==null)
			{
				tablein.putColumn("subject", new ExportColumn("subject", "Subject", "", "string"));
			}
			if(tablein.getColumn(columnid)==null) {
				tablein.putColumn(columnid, new ExportColumn(columnid, columnname, "", "number"));
			}
			
			if(isValueConceptKey(concept_key)) {
				/*get the data*/
				String concept_cd=getConceptCodeFromKey(concept_key);
				groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
				String sqlt = """SELECT PATIENT_NUM, NVAL_NUM, START_DATE FROM OBSERVATION_FACT f WHERE CONCEPT_CD = ? AND
				        PATIENT_NUM IN (select distinct patient_num
						from qt_patient_set_collection
						where result_instance_id = ?)""";
				
				sql.eachRow(sqlt, [
					concept_cd,
					result_instance_id
				], {row ->
					/*If I already have this subject mark it in the subset column as belonging to both subsets*/
					String subject=row.PATIENT_NUM
					Double value=row.NVAL_NUM
					if(tablein.containsRow(subject)) /*should contain all subjects already if I ran the demographics first*/ {
						tablein.getRow(subject).put(columnid, value.toString());
					}
					else /*fill the row*/ {
						ExportRowNew newrow=new ExportRowNew();
						newrow.put("subject", subject);
						newrow.put(columnid, value.toString());
						tablein.putRow(subject, newrow);
					}
				})
			}
			else {
				String concept_cd=getConceptCodeFromKey(concept_key);
				groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
				String sqlt = """SELECT PATIENT_NUM, TVAL_CHAR, START_DATE FROM OBSERVATION_FACT f WHERE CONCEPT_CD = ? AND
				        PATIENT_NUM IN (select distinct patient_num
				        from qt_patient_set_collection
						where result_instance_id = ?)""";
				
				sql.eachRow(sqlt, [
					concept_cd,
					result_instance_id
				], {row ->
					/*If I already have this subject mark it in the subset column as belonging to both subsets*/
					String subject=row.PATIENT_NUM
					String value=row.TVAL_CHAR
					if(value==null){value="Y";
					}
					if(tablein.containsRow(subject)) /*should contain all subjects already if I ran the demographics first*/ {
						tablein.getRow(subject).put(columnid, value.toString());
					}
					else /*fill the row*/ {
						ExportRowNew newrow=new ExportRowNew();
						newrow.put("subject", subject);
						newrow.put(columnid, value.toString());
						tablein.putRow(subject, newrow);
					}
				});
			}
			//pad all the empty values for this column
			for(ExportRowNew row: tablein.getRows())
			{
				if(!row.containsColumn(columnid)) {
					row.put(columnid, "N");
				}
			}
		}
		else {
			log.trace("must be a folder dont add to grid");
		}
		return tablein;
	}
	
	/**
	 * Gets a distribution of information from the patient dimension table
	 * */
	def HashMap<String,Integer> getPatientDemographicDataForSubset(String col, String result_instance_id) {
		HashMap<String,Integer> results = new LinkedHashMap<String, Integer>();
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		String sqlt = """SELECT a.cat as demcategory, nvl(b.demcount,0) as demcount FROM
		(SELECT DISTINCT UPPER("""+col+""") as cat FROM patient_dimension) a
		LEFT OUTER JOIN
		(SELECT UPPER("""+col+""") as cat,COUNT(*) as demcount FROM patient_dimension
		WHERE PATIENT_NUM IN (select distinct patient_num from qt_patient_set_collection where result_instance_id = ?)
		Group by UPPER("""+col+""")) b
		ON a.cat=b.cat ORDER BY a.cat""";
		sql.eachRow(sqlt, [result_instance_id], {row ->
			results.put(row[0], row[1])
			log.trace("in row getting patient demographic data for subset")
		})
		return results;
	}
	
	/**
	 * Gets a list of concept keys in a subset
	 */
	def List<String> getConceptKeysInSubset(String resultInstanceId) {
		
		log.trace("called getConceptKeysInSubset");
		
		ArrayList<String> concepts = new ArrayList<String>();
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		String sqlt = """SELECT REQUEST_XML FROM QT_QUERY_MASTER c INNER JOIN QT_QUERY_INSTANCE a
		    ON a.QUERY_MASTER_ID=c.QUERY_MASTER_ID INNER JOIN QT_QUERY_RESULT_INSTANCE b
		    ON a.QUERY_INSTANCE_ID=b.QUERY_INSTANCE_ID WHERE RESULT_INSTANCE_ID = ?""";
		
		String xmlrequest="";
		sql.eachRow(sqlt, [resultInstanceId], {row ->
			xmlrequest=clobToString(row.request_xml);
			log.trace("REQUEST_XML:" +xmlrequest)
			
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true); // never forget this!
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xmlrequest)));
			
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			//xpath.setNamespaceContext(new QueryNamespaceContext());
			//XPathExpression expr  = xpath.compile("item");
			Object result=xpath.evaluate("//item/item_key", doc, XPathConstants.NODESET);
			//Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			Node x=null;
			String key=null;
			//iterate over all the nodes
			for (int i = 0; i < nodes.getLength(); i++) {
				x=nodes.item(i);
				key=x.getTextContent();
				concepts.add(key);
			}
		});
		log.trace("getConceptKeysInSubset done");
		return concepts;
	}
	
	/**
	 * Gets all the concept keys in both subsets into a hashmap that distincts them
	 */
	def List<String> getConceptKeysInSubsets(String result_instance_id1, String result_instance_id2) {
		/*get all distinct  concepts for analysis from both subsets into hashmap*/
		HashMap<String, String> h=new HashMap<String,String>();
		ArrayList<String> results=new ArrayList<String>();
		if(result_instance_id1!=null && result_instance_id1!="") {
			for(String c:getConceptKeysInSubset(result_instance_id1)) {
				//String analysis_key=getConceptKeyForAnalysis(c);
				//h.put(analysis_key, analysis_key);
				h.put(c, c);
			}
		}
		if(result_instance_id2!=null && result_instance_id2!="") {
			for(String c:getConceptKeysInSubset(result_instance_id2)) {
				//String analysis_key=getConceptKeyForAnalysis(c);
				//h.put(analysis_key, analysis_key);
				h.put(c, c);
			}
		}
		/*Analyze each concept in subsets*/
		int mapsize = h.size();
		Iterator keyValuePairs1 = h.entrySet().iterator();
		for (int i = 0; i < mapsize; i++) {
			Map.Entry<String,String> entry = (Map.Entry<String,String>) keyValuePairs1.next();
			String analysis_key = entry.getKey();
			results.add(analysis_key);
		}
		return results;
	}
	/**
	 *  Returns a hashmap mapping each named concept in the output to a
	 *  specific concept within a single trial
	 *  This is used when presenting results across trials
	 */
	def String lookupParentConcept(String conceptPath) {
		/*get all distinct  concepts for analysis from both subsets into hashmap*/
		
		try {
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
			String sqlQuery = """select parent_cd from deapp.de_xtrial_child_map xcm
				inner join concept_dimension cd
				on xcm.concept_cd=cd.concept_cd
				where concept_path = ?""";
			log.debug("\ncalled with conceptPath:"+conceptPath);
			log.debug("\nexecuting query:"+sqlQuery);
			String parentConcept = "";
			sql.eachRow(sqlQuery, [conceptPath], {row -> parentConcept=row.parent_cd;});
			if (parentConcept != "") {
				log.debug("returning parentConcept="+parentConcept);
				return parentConcept;
			} else {
				return null;
			}
		} catch(e) {
			log.error("Exception occurred when looking up parent concept: " + e.getMessage());
			return null;
		}
	}
	
	def Set<String> lookupChildConcepts(String parentConcept, String result_instance_id1, String result_instance_id2) {
		
		Set<String>	childConcepts = new HashSet<String>();
		
		if (parentConcept == null) {
			log.debug("lookupChildConcepts called with parentConcept==null");
			return(childConcepts);
		}
		
		if (result_instance_id1=="" && result_instance_id2=="") {
			log.debug("empty result_instance_id fields");
			return(childConcepts)
		}
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		//	String sqlTemplate1 =
		//	  """SELECT distinct f.concept_CD
		//	  	 FROM OBSERVATION_FACT f
		//         INNER JOIN deapp.de_xtrial_child_map x
		//         ON f.concept_cd=x.concept_cd
		//         WHERE f.PATIENT_NUM IN (select distinct patient_num
		//         from qt_patient_set_collection
		//         where result_instance_id=""";
		
		String sqlTemplate1 =
				"""SELECT distinct x.concept_CD
				FROM deapp.de_xtrial_child_map x
				WHERE x.study_id IN (
						select distinct p.trial
						from qt_patient_set_collection q
						inner join patient_trial p
						on q.patient_num=p.patient_num
						where q.result_instance_id=""";
		
		String sqlTemplate2 = """ or result_instance_id=""";
		String sqlTemplate3 = """) and x.parent_cd=""";
		
		
		
		String sqlQuery = "";
		if (result_instance_id1 == "") {
			sqlQuery = sqlTemplate1 + result_instance_id2  + sqlTemplate3 + parentConcept;
		} else if (result_instance_id2 =="") {
			sqlQuery = sqlTemplate1 + result_instance_id1  + sqlTemplate3 + parentConcept;
		} else {
			sqlQuery = sqlTemplate1 + result_instance_id1 + sqlTemplate2 + result_instance_id2 + sqlTemplate3 + parentConcept;
		}
		
		log.debug("query to get child concepts: "+sqlQuery);
		log.debug("\n");
		
		try {
			sql.eachRow(sqlQuery, {row -> childConcepts.add(row.concept_cd);});
		} catch(e) {
			log.error("Exception occurred when looking up child concepts: " + e.getMessage());
			log.error("query: " + sqlQuery);
			log.error("parentConcept: "+parentConcept);
			log.error("result_instance_id1: "+result_instance_id1);
			log.error("result_instance_id2: "+result_instance_id2);
		}
		
		return(childConcepts);
	}
	
	def Set<String> lookupChildConcepts(String parentConcept, String result_instance_id) {
		return lookupChildConcepts(parentConcept, result_instance_id, null);
	}
	
	
	def Set<String> getDistinctConceptSet(String result_instance_id1, String result_instance_id2) {
		/* get all distinct  concepts for analysis from both subsets into hashmap
		 * only need one concept from each family, because the rendering functions find the others
		 * */
		
		Set<String>              workingSet = new HashSet<String>();
		Set<String>              finalSet   = new HashSet<String>();
		Set<String>              parentSet  = new HashSet<String>();
		
		log.debug("getDistinctConceptSet called with arguments: "+result_instance_id1+" and "+result_instance_id2)
		
		workingSet.addAll(getConceptKeysInSubset(result_instance_id1));
		workingSet.addAll(getConceptKeysInSubset(result_instance_id2));
		
		for (String k : workingSet) {
			// always look for a parent
			String parentConcept = lookupParentConcept(keyToPath(k));
			if (parentConcept == null) {
				finalSet.add(k); // add an orphan straight to the final set
			} else if (!parentSet.contains(parentConcept)) {
				parentSet.add(parentConcept);
				finalSet.add(k); // add the first concept to the final set
			}
		}
		
		log.debug("getDistinctConceptSet returning set: "+finalSet);
		return finalSet;
	}
	
	
	/**
	 * Gets the querymasterid for resultinstanceid
	 */
	def String getQIDFromRID(String resultInstanceId) {		
		String qid=""
		if (resultInstanceId != null && resultInstanceId.length() > 0)	{
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
			String sqlt="""select QUERY_MASTER_ID FROM QT_QUERY_INSTANCE a
		    	INNER JOIN QT_QUERY_RESULT_INSTANCE b
		    	ON a.QUERY_INSTANCE_ID=b.QUERY_INSTANCE_ID WHERE RESULT_INSTANCE_ID = ?"""
			sql.eachRow(sqlt, [resultInstanceId], {row ->qid=row.QUERY_MASTER_ID;})
		}
		return qid
	}
	/**
	 * Gets the request xml for query def id
	 */
	def String getQueryDefinitionXMLFromQID(String qid) {
		log.trace("Called getQueryDefinitionXML")
		String xmlrequest="";
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		
		String sqlt="""select REQUEST_XML from QT_QUERY_MASTER WHERE QUERY_MASTER_ID = ?""";
		log.trace(sqlt);
		sql.eachRow(sqlt, [qid], {row ->
			log.trace("in xml query")
			log.trace(row.REQUEST_XML)
			xmlrequest=clobToString(row.REQUEST_XML);
			log.trace("Request XML:" +xmlrequest);
		})
		return xmlrequest;
	}
	/**
	 * Gets the request xml for a result instance id
	 */
	def String getQueryDefinitionXML(String resultInstanceId) {
		log.trace("Called getQueryDefinitionXML")
		String xmlrequest="";
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		
		String sqlt="""select REQUEST_XML from QT_QUERY_MASTER c INNER JOIN QT_QUERY_INSTANCE a
		    ON a.QUERY_MASTER_ID=c.QUERY_MASTER_ID INNER JOIN QT_QUERY_RESULT_INSTANCE b
		    ON a.QUERY_INSTANCE_ID=b.QUERY_INSTANCE_ID WHERE RESULT_INSTANCE_ID = ?""";
		log.trace(sqlt);
		sql.eachRow(sqlt, [resultInstanceId], {row ->
			log.trace("in xml query");
			log.trace(row.REQUEST_XML);
			xmlrequest=clobToString(row.REQUEST_XML);
			log.trace("Request XML:" +xmlrequest);
		})
		return xmlrequest;
	}
	
	/**
	 * Gets a comma delimited list of subjects for a result instance id
	 */
	def  String getSubjects(String resultInstanceId){
		if (resultInstanceId == null) {
			return null;
		}
		StringBuilder subjectIds = new StringBuilder();
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		
		String sqlt = """select distinct patient_num from qt_patient_set_collection where result_instance_id = ? 
		AND patient_num IN (select patient_num from patient_dimension where sourcesystem_cd not like '%:S:%')""";
		log.trace("before sql call")
		sql.eachRow(sqlt, [resultInstanceId], {row ->
			log.trace("in iterator")
			if (subjectIds.length() > 0) {
				subjectIds.append(",");
			}
			subjectIds.append(row.PATIENT_NUM);
		})
		log.trace("before return")
		return subjectIds.toString();
	}
	
	/**
	 * Gets a list of subjects for a result instance id
	 */
	def  List<String> getSubjectsAsList(String resultInstanceId){
		List<String> subjectIds=new ArrayList<String>();
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		String sqlt = "select distinct patient_num from qt_patient_set_collection where result_instance_id = ?";
		log.trace("before sql call")
		sql.eachRow(sqlt, [resultInstanceId], {row ->
			subjectIds.add(row.PATIENT_NUM);
		})
		return subjectIds;
	}

	/**
	* Gets a list of subjects for a list of sample ids.
	*/
   def  List<String> getSubjectsAsListFromSample(ArrayList SampleIDList){
	   
	   //This is the list of patient_nums we return.
	   List<String> subjectIds=new ArrayList<String>();
	   
	   //This is the SQL object we use to gather our data.
	   groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
	   
	   //This is the SQL statement we run.
	   String sqlt = "select distinct PATIENT_ID from DE_SUBJECT_SAMPLE_MAPPING where SAMPLE_ID in (" + listToIN(SampleIDList) + ")";

	   sql.eachRow(sqlt, [ ], {row -> subjectIds.add(row.PATIENT_ID);})
	   
	   return subjectIds;
   }
	
   /**
   * Gets a list of subjects for a list of sample ids.
   */
  def  List<Long> getSubjectsAsListFromSampleLong(ArrayList SampleIDList){
	  
	  //This is the list of patient_nums we return.
	  List<Long> subjectIds=new ArrayList<Long>();
	  
	  //This is the SQL object we use to gather our data.
	  groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
	  
	  //This is the SQL statement we run.
	  String sqlt = "select distinct PATIENT_ID from DE_SUBJECT_SAMPLE_MAPPING where SAMPLE_ID in (" + listToIN(SampleIDList) + ")";

	  sql.eachRow(sqlt, [ ], {row -> subjectIds.add(row.PATIENT_ID);})
	  
	  return subjectIds;
  }
   
   /**
   * Gets a list of concepts for a list of sample ids.
   */
  def  List<String> getConceptsAsListFromSample(ArrayList SampleIDList){
	  
	  //This is the list of patient_nums we return.
	  List<String> conceptIds=new ArrayList<String>();
	  
	  //This is the SQL object we use to gather our data.
	  groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
	  
	  //This is the SQL statement we run.
	  String sqlt = "select distinct CONCEPT_CODE from DE_SUBJECT_SAMPLE_MAPPING where SAMPLE_ID in (" + listToIN(SampleIDList) + ")";

	  sql.eachRow(sqlt, [ ], {row -> conceptIds.add(row.CONCEPT_CODE);})
	  
	  return conceptIds;
  }
   
	/**
	 * Gets concept_cds from key with like %
	 */
	def String getConceptsFromKey(String key) throws SQLException{
		//String slash="\\";
		//logMessage("Here is slash: "+slash);
		
		String path=key.substring(key.indexOf("\\",2), key.length());
		//path=path.replace("@", slash);
		StringBuilder concepts = new StringBuilder();
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		String sqlt = "SELECT CONCEPT_CD FROM CONCEPT_DIMENSION c WHERE CONCEPT_PATH LIKE ?";
		sql.eachRow(sqlt, [path+"%"], {row ->
			if(concepts.length() >0) {
				concepts.append(",");
			}
			concepts.append(row.CONCEPT_CD);
		})
		return concepts.toString();
	}
	
	/**
	 * Gets concept_cds from key with like % as list
	 */
	def List<String> getConceptsFromKeyAsList(String key) throws SQLException{
		//String slash="\\";
		//logMessage("Here is slash: "+slash);
		
		String path=key.substring(key.indexOf("\\",2), key.length());
		//path=path.replace("@", slash);
		List<String> concepts = new ArrayList<String>();
		
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		String sqlt = "SELECT CONCEPT_CD FROM CONCEPT_DIMENSION c WHERE CONCEPT_PATH LIKE ?";
		sql.eachRow(sqlt, [path+"%"], {row ->
			concepts.add(row.CONCEPT_CD)
		})
		return concepts;
	}
	
	def  String getConcepts(String resultInstanceId) {
		
		if (resultInstanceId == null) {
			return null;
		}
		
		StringBuilder concepts = new StringBuilder();
		String xmlrequest=getQueryDefinitionXML(resultInstanceId);
		
		log.trace("Request XML:" +xmlrequest);
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xmlrequest)));
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		//xpath.setNamespaceContext(new QueryNamespaceContext());
		//XPathExpression expr  = xpath.compile("item");
		Object result=xpath.evaluate("//item/item_key", doc, XPathConstants.NODESET);
		//Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		Node x=null;
		String key=null;
		String conceptcds=null;
		//iterate over all the nodes
		for (int i = 0; i < nodes.getLength(); i++) {
			x=nodes.item(i);
			key=x.getTextContent();
			//conceptcds=getConceptsFromKey(key);
			conceptcds=getConceptCodeFromKey(key);  //should only return the exact concept_cd not the children
			if (concepts.length() > 0)
			{
				concepts.append(",");
			};
			if(conceptcds!="") {
				concepts.append(conceptcds);
			}
			log.trace("Found Concept_CDs: "+conceptcds + " for key: "+key);
		}
		return concepts.toString();
	}
	
	
	
	def  List<String> getConceptsAsList(String resultInstanceId) {
		List<String> concepts=new ArrayList<String>();
		String xmlrequest=getQueryDefinitionXML(resultInstanceId);
		
		log.trace("Request XML:" +xmlrequest);
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xmlrequest)));
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		//xpath.setNamespaceContext(new QueryNamespaceContext());
		//XPathExpression expr  = xpath.compile("item");
		Object result=xpath.evaluate("//item/item_key", doc, XPathConstants.NODESET);
		//Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		Node x=null;
		String key=null;
		String conceptcds=null;
		//iterate over all the nodes
		for (int i = 0; i < nodes.getLength(); i++)
		{
			x=nodes.item(i);
			key=x.getTextContent();
			//conceptcds=getConceptsFromKey(key);
			conceptcds=getConceptCodeFromKey(key);  //should only return the exact concept_cd not the children
			log.trace("found concept code:" +conceptcds);
			concepts.add(conceptcds);
		}
		return concepts;
	}
	
	
	def filterSubjectIdByBiomarkerData(ids){
		if(ids==null || ids.length()==0)
			return ids;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		//println(ids);
		StringBuilder fids = new StringBuilder();
		StringBuilder sampleQ = new StringBuilder("SELECT distinct s.patient_id FROM de_subject_sample_mapping s WHERE s.patient_id IN (").append(ids).append(")");
		
		//	println(sampleQ);
		sql.eachRow(sampleQ.toString(),
				{row->
					String st = row.patient_id;
					if(fids.length()>0){
						fids.append(",");
					}
					fids.append(st);
				});
		return fids.toString();
	}
	
	def  getSampleHeatMapData(String pathwayName,
		String sampleIdList,
		String subjectIds1,
		String subjectIds2,
		String concepts1,
		String concepts2,
		String timepoint1,
		String timepoint2,
		String sample1,
		String sample2,
		String rbmPanels1,
		String rbmPanels2,
		String datatype,
		GenePatternFiles gpf,
		boolean fixlast,
		boolean rawdata,
		String analysisType ) throws Exception {
		
		//  For most cases, GenePattern server cannot accept gct file with empty expression ratio.
		//  Use 0 rather than empty cell. However, Comparative Marker Select needs to use empty space
		String whiteString = GENE_PATTERN_WHITE_SPACE_DEFAULT;
		if (analysisType == "Select") whiteString = GENE_PATTERN_WHITE_SPACE_EMPTY;
		
		//This is the list of all Sample IDs.
		String sampleIdAllListStr = null;
		
		//We need to preserve the order of the samples because they are in Subset1, Subset2 order.
		List<SampleInfo> sampleInfoList = sampleInfoService.getSampleInfoListInOrder(sampleIdAllListStr);
		
		//This will be a list of Assay ID's.
		String assayIdAllListStr = "";
		
		//Get the Assay ID for each SampleInfo object and add it to our string.
		for (SampleInfo sampleInfo : sampleInfoList) 
		{
			if (assayIdAllListStr.length() != 0) assayIdAllListStr += ",";
			assayIdAllListStr += sampleInfo.assayId;
		}
		
		//Build a map with all the SampleInfo objects with their ID as the key.
		Map<Long, SampleInfo> sampleInfoMap = new HashMap<Long, SampleInfo>();
		for (SampleInfo sampleInfo : sampleInfoList) {
			sampleInfoMap.put(sampleInfo.id, sampleInfo);
		}
		
		//Create a list that has the sample names with a prefix for which subset it's a part of.
		List<String> sampleNameListWithPrefix = new ArrayList<String>();
		getSampleNameListFromIds(sampleIds1, "S1_", sampleInfoMap, sampleNameListWithPrefix);
		getSampleNameListFromIds(sampleIds2, "S2_", sampleInfoMap, sampleNameListWithPrefix);
		String[] sampleNameArray = new String[sampleNameListWithPrefix.size()];
		
		for (int i = 0; i < sampleNameListWithPrefix.size(); i ++) 
		{
			sampleNameArray[i] = sampleNameListWithPrefix.get(i);
		}
		
		Integer columns = sampleNameArray.length;
		
		try{
			gpf.openGctFile();
			gpf.openCSVFile();
			// write cls file
			// gpf.writeClsFile(ids1, ids2);
			
			//We need to get all the subject id
			
			//Get the trial names based on sample_id.
			String trialNames  = getTrialNameBySampleID(sampleIdAllListStr);
			
			if(trialNames == "") throw new Exception("Could not find trial names for the given subjects!")
			
			//Build the SQL statement to get the microarray data.
			String sqlStr = "select a.assay_id, round(avg(a.zscore), 3) as zvalue, b.gene_symbol  from de_subject_microarray_data a, de_mrna_annotation b ";
			sqlStr += "where a.probeset_id = b.probeset_id and a.assay_id in (" + assayIdAllListStr + ") ";
			sqlStr += " and a.trial_name IN (" + trialNames + ") ";
			if (pathwayName != null && pathwayName.length() != 0) {
				String geneListStr = getGenes(pathwayName);
				if (geneListStr != null && geneListStr.length() != 0) {
					sqlStr += "and b.gene_id in (" + geneListStr + ") ";
				}
			}
			sqlStr += "group by a.assay_id, b.gene_symbol order by b.gene_symbol, a.assay_id ";
			
			log.debug("mRNA heatmap query: " + sqlStr);
			
			String curGeneSymbol = null;
			Map<Long, Float> assayIdValueMap = null;
			StringBuffer dataBuf = new StringBuffer();
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
			int geneNum = 0;
			sql.eachRow(sqlStr) { row ->
				String geneSymbol = row.gene_symbol;
				Long assayId = row.assay_id;
				Float zvalue = row.zvalue;
				if (curGeneSymbol == null || curGeneSymbol.equals(geneSymbol) == false) { // reached next gene
					if (assayIdValueMap != null) {	// Output data from previous gene
						outputMRNAValueToBuffer(sampleInfoList, curGeneSymbol, assayIdValueMap, dataBuf, analysisType);
					}
					curGeneSymbol = geneSymbol;
					assayIdValueMap = new HashMap<Long, Float>();
					geneNum ++;
				}
				assayIdValueMap.put(assayId, zvalue);
			}
			if (assayIdValueMap != null) { // process the last gene 
				outputMRNAValueToBuffer(sampleInfoList, curGeneSymbol, assayIdValueMap, dataBuf, analysisType);
			}
				
			gpf.createGctHeader(geneNum, sampleNameArray, "\t");
			gpf.writeToGctFile(dataBuf.toString());
			if (geneNum == 0) {
				throw new Exception("No heatmap data for the specified parameters.");
			}
			
			if ((analysisType == "Cluster") && (rows==1))
				throw new Exception("No enough data for Hierarchical Clustering Analysis.");
		}finally{
			gpf.closeGctFile();
			gpf.closeCSVFile();
		}
	}
	
	Long[] parseIdStrToArray(String idsStr) {
		if (idsStr == null || idsStr.length() == 0) return null;
		String[] strArray = idsStr.split(",");
		Long[] idsArray = new Long[strArray.length];
		for (int i = 0; i < strArray.length; i ++) {
			Long id = null;
			try {
				id = new Long(strArray[i]);
			}
			catch(Exception e) {}
			idsArray[i] = id;
		}
	}
	
	void getSampleNameListFromIds(String sampleIds, String prefix, Map<Long, SampleInfo> sampleInfoMap, List<String> sampleNameList) {
		if (sampleIds == null || sampleIds.size() == 0 ||
			sampleInfoMap == null || sampleInfoMap.size() == 0 || sampleNameList == null )
			return;
		
		String[] sampleIdStrArray = sampleIds.split(",");
		for (String sampleIdStr : sampleIdStrArray) {
			SampleInfo sampleInfo = sampleInfoMap.get(sampleIdStr);
			String sampleName = sampleInfo.sampleName;
			if (prefix != null && prefix.length() != 0)
				sampleName = prefix + sampleName;
			sampleNameList.add(sampleName);
		}
	}
	
	void outputMRNAValueToBuffer(List<SampleInfo> sampleInfoList, String curGeneSymbol, Map<Long, Float> assayIdValueMap, 
		StringBuffer dataBuf, String analysisType) {
		if (sampleInfoList == null || sampleInfoList.size() == 0) return;
		StringBuffer tempBuffer = new StringBuffer();
		
		for (SampleInfo sampleInfo : sampleInfoList) {
			if (tempBuffer.length() != 0) // Not the first value, add "\t"
				tempBuffer.append("\t")
			Float zvalue = assayIdValueMap.get(sampleInfo.assayId);
			if (zvalue != null)
				tempBuffer.append(zvalue);
			else {
				//  For most cases, GenePattern server cannot accept gct file with empty expression ratio.
				//  Use 0 rather than empty cell. However, Comparative Marker Select needs to use empty space
				String whiteString = GENE_PATTERN_WHITE_SPACE_DEFAULT;
				if (analysisType == "Select") whiteString = GENE_PATTERN_WHITE_SPACE_EMPTY;
				tempBuffer.append(whiteString);
			}
		}
		dataBuf.append(curGeneSymbol + "\t" + curGeneSymbol + "\t");
		dataBuf.append(tempBuffer);
		dataBuf.append("\n");
	}
		
	def  getHeatMapData(String pathwayName,
	String sids1,
	String sids2,
	String concepts1,
	String concepts2,
	String timepoint1,
	String timepoint2,
	String sample1,
	String sample2,
	String rbmPanels1,
	String rbmPanels2,
	String datatype,
	GenePatternFiles gpf,
	boolean fixlast,
	boolean rawdata,
	String analysisType ) throws Exception {
		
		//  For most cases, GenePattern server cannot accept gct file with empty expression ratio.
		//  Use 0 rather than empty cell. However, Comparative Marker Select needs to use empty space
		String whiteString = GENE_PATTERN_WHITE_SPACE_DEFAULT;
		if (analysisType == "Select") whiteString = GENE_PATTERN_WHITE_SPACE_EMPTY;
		
		//Get a distinct list of the patients we have data on. Queries "de_subject_sample_mapping".
		String ids1 = filterSubjectIdByBiomarkerData(sids1);
		String ids2 = filterSubjectIdByBiomarkerData(sids2);
		
		//Check to see if we actually had data in the table.
		if (ids1.equals("") || ids2.equals(""))	{
			throw new Exception("No heatmap data for the given subjects.");
		}
		
		if (datatype == null)	{
			throw new Exception("Please choose a platform for analysis.");
		}
		
		//Get pretty names for the subjects.
		String[] subjectNameArray = getSubjectNameArray(ids1, ids2, "S1_",  "S2_");
		
		try{
			gpf.openGctFile();
			gpf.openCSVFile();
			
			//Write cls file
			gpf.writeClsFile(ids1, ids2);
			
			Integer rows = 0;
			def numCols =0
			def numColumnsClosure ={ meta ->  numCols= meta.columnCount }
			
			//Determine if we are dealing with MRNA or Protein data.
			if(datatype.toUpperCase()=="MRNA_AFFYMETRIX") 
			{
				
				String intensityType ="LOG2";
				
				if(rawdata){intensityType ="RAW";}
				
				// handle *
				if (fixlast) {
					String[] newNameArray = new String[subjectNameArray.length + 1];
					newNameArray[subjectNameArray.length] = "*";
					System.arraycopy(subjectNameArray, 0, newNameArray, 0, subjectNameArray.length);
					subjectNameArray = newNameArray;
				}
				
				String query = createMRNAHeatmapBaseQuery(pathwayName,
						ids1,
						ids2,
						concepts1,
						concepts2,
						timepoint1,
						timepoint2,
						sample1,
						sample2,
						intensityType);
				log.debug("mRNA heatmap query: " + query);
				
				StringBuilder s  = new StringBuilder("");
				
				String id = null;
				String sval = null;
				
				StringBuilder cs = new StringBuilder("");
				
				def session = sessionFactory.getCurrentSession();
				
				// execute your statement against the connection
				Statement st = null;
				ResultSet rs = null;
				def trans = null;
				try{
					trans = session.beginTransaction();
					def conn = session.connection();
						st = conn.createStatement();
					st.execute("alter session enable parallel dml");
					st.setFetchSize(5000);
					rs = st.executeQuery(query);
					int totalCol = rs.getMetaData().getColumnCount();
					
					while(rs.next()){
						cs.setLength(0);
						if(rows==0){
							System.out.println("getting first row!");
						}
						sval=null;
						for(int count = 1;count<totalCol; count++){
							if(count>1){
								s.append("\t");
								cs.append(",");
							}
							
							sval = rs.getString(count);
							if(sval!=null){
							//	sval = val.toString();
								if(sval.equals("null")){
									s.append(whiteString);
									cs.append(whiteString);
								}
								else{
									s.append(sval);
									cs.append(sval);
								}
							}else{
								s.append(whiteString);
								cs.append(whiteString);
							}
							//if (val == null || val =="null")
							//	val = whiteString;
							//if(count>0)  s.append("\t");
							//s.append(val);
						}
						// special *
						if(fixlast){
							s.append("\t").append("0");
							cs.append(",").append("0");
						}
						//s.append("\n");
						rows++;
						// write to csv file to improve performance
						s.append("\n");
						gpf.writeToCSVFile(cs.toString())
						
					}
				} finally	{
					if (rs != null)	{
						rs.close()
					}
					if (st != null)	{
						st.close()
					}					
					trans.commit()
				}
				
				gpf.createGctHeader( rows, subjectNameArray, "\t");
				gpf.writeToGctFile(s.toString());
				//cleanup
				s = null;
				
				// force clean up
				//rowsObj = null;
				//	log.trace("results: " + s);
			} else if(datatype.toUpperCase()=="RBM") {
				StringBuilder s  = new StringBuilder("");
				String query = createRBMHeatmapQuery(pathwayName, ids1, ids2,
						concepts1, concepts2, timepoint1, timepoint2, rbmPanels1, rbmPanels2);
				log.debug("RBM heatmap query: " + query);
				groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
					
				def rowsObj = sql.rows(query, numColumnsClosure)
				
				// create header
				gpf.createGctHeader( rowsObj.size(), subjectNameArray, "\t");
				
				rowsObj.each {	row ->
					s.setLength(0);
					for (int count in 0 ..< numCols-1) {
						String val = row.getAt(count);
						// just impute zero; these are z scores
						if (val == "null" || val == null) val = whiteString;
						if(count>0)  s.append("\t");
						s.append(val);
					}
					rows++;
					gpf.writeToGctFile(s.toString()	);
					gpf.writeToCSVFile(s.toString().replaceAll("\t",","))
				}
				
				rowsObj = null;
				//	log.trace("results: " + s);
			} else if(datatype.toUpperCase()=="PROTEIN") {
				String query = createProteinHeatmapQuery(pathwayName, ids1, ids2,
						concepts1, concepts2, timepoint1, timepoint2);
				log.debug("Protein heatmap query: " + query);
				
				StringBuilder s  = new StringBuilder("");
				groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
				def rowsObj = sql.rows(query, numColumnsClosure)
				
				// create header
				gpf.createGctHeader( rowsObj.size(), subjectNameArray, "\t");
				
				rowsObj.each {	row ->
					s.setLength(0);
					if (row.getAt("component") == null) {
						s.append(row.getAt("GENE_SYMBOL"));
					} else {
						s.append(row.getAt("component"));
					}
					s.append("\t" + row.getAt("GENE_SYMBOL"));
					for (int count: 2 ..< numCols-1) {
						String val = row.getAt(count);
						if (val == "null" || val == null) val = whiteString;
						if(count>0)  s.append("\t");
						s.append(val);
					}
					
					rows++;
					gpf.writeToGctFile(s.toString()	);
					gpf.writeToCSVFile(s.toString().replaceAll("\t",","))
				}
				//	log.trace("results: " + s);
			}
			
			if (rows == 0) {
				throw new Exception("No heatmap data for the specified parameters.");
			}
			
			if ((analysisType == "Cluster") && (rows==1))
				throw new Exception("Not enough data for Hierarchical Clustering Analysis.");
		}finally{
			gpf.closeGctFile();
			gpf.closeCSVFile();
		}
	}
	
	def  getSurvivalAnalysisData(List<String> concepts1, List<String> concepts2, List<String> subjects1, List<String> subjects2,
			SurvivalAnalysisFiles saFiles) throws Exception {
		
		if (concepts1 == null || concepts1.size() == 0) throw new Exception("The subset 1 has empty concepts");
		if (concepts2 == null || concepts2.size() == 0) throw new Exception("The subset 2 has empty concepts");
		
		if (subjects1 == null || subjects1.size() == 0) throw new Exception("The subset 1 has no subjects");
		if (subjects2 == null || subjects2.size() == 0) throw new Exception("The subset 2 has no subjects");
		
		if (saFiles == null) throw new Exception("The object saFiles cannot be null");
		
		List<SurvivalData> survivalDataList_1 = getSurvivalDataForSubset(subjects1, concepts1);
		List<SurvivalData> survivalDataList_2 = getSurvivalDataForSubset(subjects2, concepts2);
		
		File clsFile = saFiles.getClsFile();
		File dataFile = saFiles.getDataFile();
		
		StringBuffer clsBuf = new StringBuffer();
		StringBuffer dataBuf = new StringBuffer();
		
		int totalCount = survivalDataList_1.size() + survivalDataList_2.size();
		
		clsBuf.append(totalCount + " 2 1\n# clsA clsB\n");
		dataBuf.append("name\ttime\tcensor\n");
		survivalDataList_1.eachWithIndex() {obj, i ->
			clsBuf.append("1 ");
			dataBuf.append(obj.subjectId + "\t" + obj.survivalTime.intValue() + "\t");
			if(obj.isEvent != null && obj.isEvent.booleanValue() == true)
				dataBuf.append("1\n");
			else
				dataBuf.append("0\n");
		}
		
		survivalDataList_2.eachWithIndex() {obj, i ->
			clsBuf.append("2");
			if (i != survivalDataList_2.size() - 1) {
				clsBuf.append(" ");
			}
			dataBuf.append(obj.subjectId + "\t" + obj.survivalTime.intValue() + "\t");
			if(obj.isEvent != null && obj.isEvent.booleanValue() == true)
				dataBuf.append("1\n");
			else
				dataBuf.append("0\n");
		}
		
		clsBuf.append("\n");	// The format for cls file in survival analysis is very strict. The last flag is followed by a line break, and nothing else
		
		clsFile.write(clsBuf.toString());
		dataFile.write(dataBuf.toString());
	}
	
	List<SurvivalData> getSurvivalDataForSubset(List<String> subjectStrList, List<String> conceptStrList) throws Exception {
		List<Concept> conceptList = new ArrayList<Concept>();
		for (String concept : conceptStrList) {
			Concept conceptObj = conceptService.getConceptByBaseCode(concept);
			if (conceptObj != null) {
				conceptList.add(conceptObj);
			}
		}
		
		SurvivalConcepts survivalConcepts = new SurvivalConcepts();
		defineConceptsForSurvivalAnalysis(conceptList, survivalConcepts);
		if (survivalConcepts.conceptSurvivalTime == null) throw new Exception("The concept for survival time is not defined");
		
		Map<String, SurvivalData> survivalDataMap = getSurvivalDataForSurvivalTime(survivalConcepts.conceptSurvivalTime, subjectStrList);
		fillCensoringToSurvivalData(survivalConcepts.conceptCensoring, survivalDataMap, subjectStrList);
		fillEventToSurvivalData(survivalConcepts.conceptEvent, survivalDataMap, subjectStrList);
		
		List<SurvivalData> dataList = new ArrayList<SurvivalData>();
		survivalDataMap.each() {key, value ->
			dataList.add(value);
		}
		return dataList;
	}
	
	/**
	 * The modeling of survival data is standardized. The censoring node will has String "(PFSCENS)" or "(OSCENS)" in the name. It also will have
	 * child nodes of "Yes" and "No" to indicate censored or Event.
	 * The survival time node will have String "(PFS)" and "(OS)" in the name, and the unit is in days.
	 * @param concepts
	 * @param survivalConcepts
	 */
	void defineConceptsForSurvivalAnalysis(List<Concept> concepts, SurvivalConcepts survivalConcepts) throws Exception {
		if (survivalConcepts == null) return;
		List<Concept> conceptNumericalList = new ArrayList<Concept>();
		for(Concept concept : concepts) {
			String shortName = concept.getName();
			if (isSurvivalData(shortName)) {
				if (survivalConcepts.conceptSurvivalTime != null) {
					throw new Exception("More than one node with \"(PFS)\" or \"(OS)\" in the name that can be used as survival time node");
				}
				survivalConcepts.conceptSurvivalTime = concept;
			}
			if (isSurvivalCensor(shortName)) {
				// Need to get the "Yes" and "No" child nodes
				List<Concept> children = conceptService.getChildrenConcepts(concept);
				for (Concept conceptChild : children) {
					String shortNameChild = conceptChild.getName();
					if (shortNameChild.equalsIgnoreCase("Yes")) {
						survivalConcepts.conceptCensoring = conceptChild;
					}
					else if (shortNameChild.equalsIgnoreCase("No")) {
						survivalConcepts.conceptEvent = conceptChild;
					}
				}
			}
		}
	}
	
	boolean isSurvivalData(String conceptName) {
		def dataList = ConfigurationHolder.config.com.recomdata.analysis.survival.survivalDataList;
		for (String data : dataList) {
			if (conceptName.indexOf(data) > 0) return true;
		}
		return false;
	}
	
	boolean isSurvivalCensor(String conceptName) {
		def censorList = ConfigurationHolder.config.com.recomdata.analysis.survival.censorFlagList;
		for (String data : censorList) {
			if (conceptName.indexOf(data) > 0) return true;
		}
		return false;
	}
	
	
	
	Map<String, SurvivalData> getSurvivalDataForSurvivalTime(Concept conceptSurvivalTime, List<String> subjectStrList) throws Exception {
		if (conceptSurvivalTime == null || conceptSurvivalTime.getBaseCode() == null)
			throw new Exception ("The concept for survival time is not defined");
		
		Map<SurvivalData> dataMap = new HashMap<String, SurvivalData>();
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String subjectIdListInStr = DBHelper.listToInString(subjectStrList);
		String sqlt = "SELECT * FROM observation_fact WHERE CONCEPT_CD = ?";
		if (subjectIdListInStr != null)
			sqlt += " and PATIENT_NUM in (" + subjectIdListInStr + ")";
		sql.eachRow(sqlt, [
			conceptSurvivalTime.getBaseCode()
		], {row ->
			SurvivalData survivalData= new SurvivalData();
			survivalData.subjectId = row.patient_num;
			survivalData.survivalTime = row.nval_num;
			dataMap.put(survivalData.subjectId, survivalData);
		})
		return dataMap;
	}	
	
	/**
	 * For now the patients have to be in the same trial, for the sake of simplicity.
	 * 
	 * @param subjectIds1
	 * @param subjectIds2
	 * @param geneSearchIdList
	 * @param snpFiles
	 */
	void getSNPViewerDataByProbe(String subjectIds1, String subjectIds2, List<Long> geneSearchIdList, List<String> geneNameList,
				List<String> snpNameList, SnpViewerFiles snpFiles, StringBuffer geneSnpPageBuf) throws Exception {
		if (snpFiles == null) throw new Exception("The SNPViewerFiles object is not instantiated");
		if (geneSnpPageBuf == null) throw new Exception("The geneSnpPageBuf object is not instantiated");
		
		SnpDatasetListByProbe allDataByProbe = new SnpDatasetListByProbe();

		List<Long>[] patientNumListArray = new List<Long>[2]; // For the patient numbers selected by users in subset 1 and subset 2
		patientNumListArray[0] = getPatientNumListFromSubjectIdStr(subjectIds1);
		allDataByProbe.patientNumList_1 = patientNumListArray[0];
		patientNumListArray[1] = getPatientNumListFromSubjectIdStr(subjectIds2);
		allDataByProbe.patientNumList_2 = patientNumListArray[1];

		// Get SQL query String for all the subject IDs
		String subjectListStr = "";
		if (subjectIds1 != null && subjectIds1.length() != 0) subjectListStr += subjectIds1;
		if (subjectIds2 != null && subjectIds2.length() != 0) {
			if (subjectListStr.length() != 0) subjectListStr += ", ";
			subjectListStr += subjectIds2;
		}
		
		// Get the gene-snp map, and the snp set related to all the user-input genes.
		// Map<chrom, Map<chromPos of Gene, GeneWithSnp>>
		Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapForGene = new HashMap<String, SortedMap<Long, Map<Long, GeneWithSnp>>>();
		Map<Long, GeneWithSnp> geneEntrezIdMap = new HashMap<Long, GeneWithSnp>();
		Map<String, GeneWithSnp> geneNameToGeneWithSnpMap = new HashMap<String, GeneWithSnp>();
		getGeneWithSnpMapForGenes(geneSnpMapForGene, geneEntrezIdMap, geneNameToGeneWithSnpMap, geneSearchIdList);
		
		// Get the gene-snp map for the user-selected SNPs.
		Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapForSnp = new HashMap<String, SortedMap<Long, Map<Long, GeneWithSnp>>>();
		getGeneWithSnpMapForSnps(geneSnpMapForSnp, snpNameList);
		
		Collection<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>> geneSnpMapList = new ArrayList<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>>();
		geneSnpMapList.add(geneSnpMapForGene);
		geneSnpMapList.add(geneSnpMapForSnp);
		Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap = mergeGeneWithSnpMap(geneSnpMapList);
			
		if (allGeneSnpMap == null || allGeneSnpMap.size() == 0)
			throw new Exception("There is no SNP data for selected genes and SNP IDs");
			
		// Generate the web page to display the Gene and SNP selected by User
		getSnpGeneAnnotationPage(geneSnpPageBuf, allGeneSnpMap, geneEntrezIdMap, 
			geneNameToGeneWithSnpMap, geneNameList, snpNameList);

		Map<Long, SnpDataset[]> snpDatasetBySubjectMap = allDataByProbe.snpDatasetBySubjectMap;
		getSnpDatasetBySubjectMap (snpDatasetBySubjectMap, subjectListStr);

		StringBuffer sampleInfoBuf = new StringBuffer();
		List<SnpDataset> datasetList = allDataByProbe.datasetList;
		List<String> datasetNameForSNPViewerList = allDataByProbe.datasetNameForSNPViewerList;
		getSnpSampleInfo(datasetList, datasetNameForSNPViewerList, patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf);
		
		// Get the compacted SNP data and insert them into the map, organized by chrom, and further ordered by chrom position
		Map<String, List<SnpDataByProbe>> snpDataByChromMap = allDataByProbe.snpDataByChromMap;
		
		// 
		Set<Long> allSnpIdSet = getSnpSet(allGeneSnpMap);
		getSNPDataByProbeByChrom(datasetList, snpDataByChromMap, allSnpIdSet);
		
		// Write the sample info text file for SNPViewer
		File sampleFile = snpFiles.getSampleFile();
		sampleFile << sampleInfoBuf.toString();
		
		// Write the xcn file
		File dataFile = snpFiles.getDataFile();
		BufferedWriter dataWriter = new BufferedWriter(new FileWriter(dataFile));
		// Write the header column
		dataWriter.write("SNP\tChromosome\tPhysicalPosition");
		for (String datasetName : datasetNameForSNPViewerList) {
			dataWriter.write("\t" + datasetName + "\t" + datasetName + " Call");
		}
		dataWriter.write("\n");
		// Write the data section, by chrom. Stop at the last used chrom in snpDataByChromMap
		List<String> sortedChromList = getSortedChromList(snpDataByChromMap.keySet());
		String lastChrom = sortedChromList.get(sortedChromList.size() - 1);
		String[] allChromArray = getAllChromArray();
		Map<String, String[]> chromEndProbeLineMap = getChromEndProbeLineMap();
		for (String chrom_it : allChromArray) {
			List<SnpDataByProbe> snpDataByProbeList = snpDataByChromMap.get(chrom_it);
			if (snpDataByProbeList != null && snpDataByProbeList.size() != 0) {
				// SNPViewer has problem rendering single SNP without boundary blank SNPs.
				String[] chromEndProbeLine = chromEndProbeLineMap.get(chrom_it);
				dataWriter.write(chromEndProbeLine[0]);
				for (int i = 0; i < datasetList.size(); i ++ ) {
					dataWriter.write("\t2.0\tNC");
				}
				dataWriter.write("\n");
				
				for (SnpDataByProbe snpDataByProbe : snpDataByProbeList) {
					dataWriter.write(snpDataByProbe.snpName + "\t" + chrom_it + "\t" + snpDataByProbe.chromPos);
					String[][] dataArray = snpDataByProbe.dataArray;
					for (int i = 0; i < datasetList.size(); i ++ ) {
						dataWriter.write("\t" + dataArray[i][0].trim() + "\t" + dataArray[i][1]);
					}
					dataWriter.write("\n");
				}
				
				dataWriter.write(chromEndProbeLine[1]);
				for (int i = 0; i < datasetList.size(); i ++ ) {
					dataWriter.write("\t2.0\tNC");
				}
				dataWriter.write("\n");
			}
			else {	// There is no snp data needed for this chrom
				String[] chromEndProbeLine = chromEndProbeLineMap.get(chrom_it);
				for (int idxEndProbe = 0; idxEndProbe < 2; idxEndProbe ++) {
					dataWriter.write(chromEndProbeLine[idxEndProbe]);
					for (int i = 0; i < datasetList.size(); i ++ ) {
						dataWriter.write("\t2.0\tNC");
					}
					dataWriter.write("\n");
				}
			}
			
			if (chrom_it.equals(lastChrom)) break;
		}
		dataWriter.close();
	}
	
	void getSNPDataByProbeByChrom(List<SnpDataset> datasetList, Map<String, List<SnpDataByProbe>> snpDataByChromMap,
			Collection snpIds) {
		if (datasetList == null || datasetList.size() == 0) throw new Exception("The datasetList is empty");
		if (snpDataByChromMap == null) throw new Exception("The snpDataByChromMap is null");
		if (snpIds == null || snpIds.size() == 0) throw new Exception("The snpIds is empty");
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		String trialName = datasetList.get(0).trialName;
		// Get the order of each dataset in the compacted data String
		Map<Long, Integer> datasetCompactLocationMap = new HashMap<Long, Integer>();
		String sqlStr = "select snp_dataset_id, location from de_snp_data_dataset_loc where trial_name = ?";
		sql.eachRow(sqlStr, [trialName]) { row ->
			Long datasetId = row.snp_dataset_id;
			Integer order = row.location;
			datasetCompactLocationMap.put(datasetId, order);
		}
		
		String snpIdListStr = getStringFromCollection(snpIds);
		// Get the compacted SNP data and insert them into the map, organized by chrom, and further ordered by chrom position
		sqlStr = "select b.name, b.chrom, b.chrom_pos, c.snp_data_by_probe_id, c.snp_id, c.probe_id, c.probe_name, c.trial_name, c.data_by_probe ";
		sqlStr += "from de_snp_info b, de_snp_data_by_probe c where b.snp_info_id = c.snp_id ";
		sqlStr += "and c.trial_name = ? and b.snp_info_id in (" + snpIdListStr + ") order by b.chrom, b.chrom_pos";
		sql.eachRow(sqlStr, [trialName]) { row ->
			SnpDataByProbe snpDataByProbe = new SnpDataByProbe();
			snpDataByProbe.snpDataByProbeId = row.snp_data_by_probe_id;
			snpDataByProbe.snpInfoId = row.snp_id;
			snpDataByProbe.snpName = row.name;
			snpDataByProbe.probeId = row.probe_id;
			snpDataByProbe.probeName = row.probe_name;
			snpDataByProbe.trialName = row.trial_name;
			snpDataByProbe.chrom = row.chrom;
			snpDataByProbe.chromPos = row.chrom_pos;
			java.sql.Clob clob = (java.sql.Clob)row.data_by_probe;
			String dataByProbe = clob.getAsciiStream().getText();
			snpDataByProbe.dataArray = getSnpDataArrayFromCompactedString(datasetList, datasetCompactLocationMap, dataByProbe);
			
			List<SnpDataByProbe> snpDataByProbeList = snpDataByChromMap.get(snpDataByProbe.chrom);
			if (snpDataByProbeList == null) {
				snpDataByProbeList = new ArrayList<SnpDataByProbe>();
				snpDataByChromMap.put(snpDataByProbe.chrom, snpDataByProbeList);
			}
			snpDataByProbeList.add(snpDataByProbe);
		}
	}
	
	String[][] getSnpDataArrayFromCompactedString(List<SnpDataset> datasetList, 
		Map<Long, Integer> datasetCompactLocationMap, String dataByProbe) {
		String[][] dataArray = new String[datasetList.size()][2];
		
		for (int i = 0; i < datasetList.size(); i ++) {
			SnpDataset snpDataset = datasetList.get(i);
			Integer location = datasetCompactLocationMap.get(snpDataset.id);
			// The snp data is compacted in the format of [##.##][AB] for copy number and genotype
			String copyNumber = dataByProbe.substring(location.intValue() * 7, location.intValue() * 7 + 5);
			String genotype = dataByProbe.substring(location.intValue() * 7 + 5, location.intValue() * 7 + 7);
			dataArray[i][0] = copyNumber;
			dataArray[i][1] = genotype;
		}
		return dataArray;
	}
	
	String getStringFromCollection(Collection inCollection) {
		if (inCollection == null || inCollection.size() ==0) return null;
		StringBuffer buf = new StringBuffer();
		for (Object obj : inCollection) {
			if (buf.length() != 0) buf.append(", ");
			if (obj instanceof Long || obj instanceof Integer || obj  instanceof Float || obj instanceof Double) {
				buf.append(obj);
			}
			else {
				buf.append("'" + obj + "'");
			}
		}
		return buf.toString();
	}
	
	List<Long> getPatientNumListFromSubjectIdStr(String subjectIdStr) {
		if (subjectIdStr == null || subjectIdStr.length() == 0) return null;
		List<Long> patientNumList = new ArrayList<Long>();
		String[] subjectArray = subjectIdStr.split(",");
		for (String subjectId : subjectArray) {
			Long patientNum = new Long(subjectId.trim());
			patientNumList.add(patientNum);
		}
		return patientNumList;
	}
	
	String getConceptDisplayName(String conceptId, Map<String, String> conceptIdToDisplayNameMap) {
		if (conceptId == null || conceptId.length() == 0 || conceptIdToDisplayNameMap == null) return null;
		String conceptDisplayName = conceptIdToDisplayNameMap.get(conceptId);
		if (conceptDisplayName == null) {
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
			sql.eachRow("select name_char from concept_dimension where concept_cd = ?", [conceptId]) { row ->
				conceptDisplayName = row.name_char;
			}
		}
		return conceptDisplayName;
	}
	
	String getEntrezIdStrFromSearchIdStr(String geneSearchIdListStr) {
		String sqlStr = "select unique_id from search_keyword where search_keyword_id in (" + geneSearchIdListStr + ")";
		String geneEntrezIdListStr = "";
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		sql.eachRow(sqlStr) {row ->
			String unique_id = row.unique_id;
			int idx = unique_id.indexOf(":");
			String geneEntrezIdStr = unique_id.substring(idx + 1).trim();
			if (geneEntrezIdListStr.size() != 0) geneEntrezIdListStr += ",";
			geneEntrezIdListStr += geneEntrezIdStr;
		}
		return geneEntrezIdListStr;
	}
	
	void getGeneWithSnpMapForGenes(Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapByChrom, 
				Map<Long, GeneWithSnp> geneEntrezIdMap, Map<String, GeneWithSnp> geneNameToGeneWithSnpMap, List<Long> geneSearchIdList) {
		if (geneSearchIdList == null || geneSearchIdList.size() == 0) return;
		if (geneSnpMapByChrom == null) throw new Exception("geneSnpMapByChrom is not instantiated");
		if (geneEntrezIdMap == null) throw new Exception("geneEntrezIdMap is not instantiated");
		String geneSearchIdListStr = getStringFromCollection(geneSearchIdList);
		
		// Get the gene entrez id
		String sqlStr = "select unique_id, keyword from search_keyword where search_keyword_id in (" + geneSearchIdListStr + ") ";
		sqlStr += " and data_category = 'GENE'";
		String geneEntrezIdListStr = "";
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		sql.eachRow(sqlStr) {row ->
			String unique_id = row.unique_id;
			int idx = unique_id.indexOf(":");
			String geneEntrezIdStr = unique_id.substring(idx + 1).trim();
			if (geneEntrezIdListStr.size() != 0) geneEntrezIdListStr += ",";
			geneEntrezIdListStr += geneEntrezIdStr;
			GeneWithSnp gene = new GeneWithSnp();
			gene.entrezId = new Long(geneEntrezIdStr);
			gene.name = row.keyword;
			geneEntrezIdMap.put(gene.entrezId, gene);
			geneNameToGeneWithSnpMap.put(gene.name, gene);
		}
		
		// Get the snp association and chrom mapping
		sqlStr = "select a.entrez_gene_id, b.* from de_snp_gene_map a, de_snp_info b where a.snp_id = b.snp_info_id and a.entrez_gene_id in (" + geneEntrezIdListStr + ") ";
		sql.eachRow(sqlStr) {row ->
			Long snpId = row.snp_info_id;
			String snpName = row.name;
			String chrom = row.chrom;
			Long chromPos = row.chrom_pos;
			Long entrezId = row.entrez_gene_id;
			
			GeneWithSnp gene = geneEntrezIdMap.get(entrezId);
			if (gene.chrom == null) {
				gene.chrom = chrom;
			}
			else {
				if (gene.chrom.equals(chrom) == false) {
					throw new Exception("Inconsistant SNP-Gene mapping in database: The Gene " + gene.name + ", with Entrez ID of " + gene.entrezId + ", is mapped to chromosome " + 
						gene.chrom + " and " + chrom);
				}
			}
			
			SnpInfo snpInfo = new SnpInfo();
			snpInfo.id = snpId;
			snpInfo.name = snpName;
			snpInfo.chrom = chrom;
			snpInfo.chromPos = chromPos;
			
			gene.snpMap.put(chromPos, snpInfo);
		}
		
		// Organize the GeneWithSnp by chrom
		for (Map.Entry geneEntry : geneEntrezIdMap) {
			GeneWithSnp gene = geneEntry.getValue();
			if (gene.chrom == null || gene.snpMap == null || gene.snpMap.size() == 0) continue;
			SortedMap<Long, Map<Long, GeneWithSnp>> genes = geneSnpMapByChrom.get(gene.chrom);
			if (genes == null) {
				genes = new TreeMap<Long, Map<Long, GeneWithSnp>>();
				geneSnpMapByChrom.put(gene.chrom, genes);
			}
			Long chromPosGene = gene.snpMap.firstKey();
			Map<Long, GeneWithSnp> geneMap = genes.get(chromPosGene);
			if (geneMap == null) {
				geneMap = new HashMap<Long, GeneWithSnp>();
				genes.put(chromPosGene, geneMap);
			}
			geneMap.put(gene.entrezId, gene);
		}
	}
	
	void getGeneWithSnpMapForSnps(Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapByChrom, List<String> snpNameList) {
		if (snpNameList == null || snpNameList.size() == 0) return;
		if (geneSnpMapByChrom == null) throw new Exception("geneSnpMapByChrom is not instantiated");
		String snpNameListStr = getStringFromCollection(snpNameList);
		
		Map<Long, GeneWithSnp> geneEntrezIdMap = new HashMap<Long, GeneWithSnp>();
		// Get the snp association and chrom mapping
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlStr = "select a.entrez_gene_id, b.* from de_snp_gene_map a, de_snp_info b where a.snp_id = b.snp_info_id and b.name in (" + snpNameListStr + ") ";
		sql.eachRow(sqlStr) {row ->
			Long snpId = row.snp_info_id;
			String snpName = row.name;
			String chrom = row.chrom;
			Long chromPos = row.chrom_pos;
			Long entrezId = row.entrez_gene_id;
			
			GeneWithSnp gene = geneEntrezIdMap.get(entrezId);
			if (gene == null) {
				gene = new GeneWithSnp();
				gene.entrezId = entrezId;
				geneEntrezIdMap.put(gene.entrezId, gene);
			}
			if (gene.chrom == null) {
				gene.chrom = chrom;
			}
			else {
				if (gene.chrom.equals(chrom) == false) {
					throw new Exception("The Gene " + gene.name + ", with Entrez ID of " + gene.entrezId + ", is on chromosome " + 
						gene.chrom + " and " + chrom);
				}
			}
			
			SnpInfo snpInfo = new SnpInfo();
			snpInfo.id = snpId;
			snpInfo.name = snpName;
			snpInfo.chrom = chrom;
			snpInfo.chromPos = chromPos;
			
			gene.snpMap.put(chromPos, snpInfo);
		}
		
		// Construct the unique_id list from Entrez IDs
		String geneSearchStr = "";
		for (Map.Entry entry : geneEntrezIdMap) {
			if (geneSearchStr.length() != 0) {
				geneSearchStr += ",";
			}
			geneSearchStr += "'GENE:" + entry.getKey() + "'";
		}
		
		// Get the gene name from search_keyword table
		sqlStr = "select unique_id, keyword from search_keyword where unique_id in (" + geneSearchStr + ") ";
		sqlStr += " and data_category = 'GENE'";
		sql.eachRow(sqlStr) {row ->
			String unique_id = row.unique_id;
			int idx = unique_id.indexOf(":");
			String geneEntrezIdStr = unique_id.substring(idx + 1).trim();
			GeneWithSnp gene = geneEntrezIdMap.get(new Long(geneEntrezIdStr));
			gene.name = row.keyword;
		}
		
		// Organize the GeneWithSnp by chrom
		for (Map.Entry geneEntry : geneEntrezIdMap) {
			GeneWithSnp gene = geneEntry.getValue();
			if (gene.chrom == null || gene.snpMap == null || gene.snpMap.size() == 0) continue;
			SortedMap<Long, Map<Long, GeneWithSnp>> genes = geneSnpMapByChrom.get(gene.chrom);
			if (genes == null) {
				genes = new TreeMap<Long, Map<Long, GeneWithSnp>>();
				geneSnpMapByChrom.put(gene.chrom, genes);
			}
			Long chromPosGene = gene.snpMap.firstKey();
			Map<Long, GeneWithSnp> geneMap = genes.get(chromPosGene);
			if (geneMap == null) {
				geneMap = new HashMap<Long, GeneWithSnp>();
				genes.put(chromPosGene, geneMap);
			}
			geneMap.put(gene.entrezId, gene);
		}
	}
	
	/* This function merge the sorted snp in sorted gene, organized by chromosome
	 * In the rare case that snp are merged into a same gene, the chrom position of the gene may change. Organize gene first. */
	Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> mergeGeneWithSnpMap(Collection<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>> mapList) {
		
		Map<Long, GeneWithSnp> geneMap = new TreeMap<Long, GeneWithSnp>();
		for (Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> map : mapList) {
			if (map == null || map.size() == 0) continue;
			for (Map.Entry mapEntry : map) {
				String chrom = mapEntry.getKey();
				SortedMap<Long, Map<Long, GeneWithSnp>> geneWithSnpMap = mapEntry.getValue();
				for (Map.Entry geneMapEntry : geneWithSnpMap) {
					Map<Long, GeneWithSnp> entrezIdGeneMap = geneMapEntry.getValue();
					for (Map.Entry entrezIdGeneMapEntry : entrezIdGeneMap) {
						Long entrezId = entrezIdGeneMapEntry.getKey();
						GeneWithSnp geneWithSnp = entrezIdGeneMapEntry.getValue();
						GeneWithSnp geneWithSnpInMap = geneMap.get(entrezId);
						if (geneWithSnpInMap == null) {	// First time to have this entrezId, use the existing gene structure
							geneWithSnpInMap = geneWithSnp;
							geneMap.put(entrezId, geneWithSnpInMap);
						}
						else {	// The gene structure and associated snp list already exist
							geneWithSnpInMap.snpMap.putAll(geneWithSnp.snpMap);
						}
					}
				}
			}
		}
		
		Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> mergedMap = new HashMap<String, SortedMap<Long, Map<Long, GeneWithSnp>>>();
		for (Map.Entry geneEntry : geneMap) {
			GeneWithSnp gene = geneEntry.getValue();
			SortedMap<Long, Map<Long, GeneWithSnp>> geneWithSnpMapByChrom = mergedMap.get(gene.chrom);
			if (geneWithSnpMapByChrom == null) {
				geneWithSnpMapByChrom = new TreeMap<Long, Map<Long, GeneWithSnp>>();
				mergedMap.put(gene.chrom, geneWithSnpMapByChrom);
			}
			Long chromPosGene = gene.snpMap.firstKey();
			Map<Long, GeneWithSnp> entrezIdgeneMap = geneWithSnpMapByChrom.get(chromPosGene);
			if (entrezIdgeneMap == null) {
				entrezIdgeneMap = new HashMap<Long, GeneWithSnp>();
				geneWithSnpMapByChrom.put(chromPosGene, entrezIdgeneMap);
			}
			entrezIdgeneMap.put(gene.entrezId, gene);
		}
		
		return mergedMap;
	}
	
	void getSnpDatasetBySubjectMap (Map<Long, SnpDataset[]> snpDatasetBySubjectMap, String subjectListStr) {
		if (snpDatasetBySubjectMap == null || subjectListStr == null || subjectListStr.length() == 0) return;
		
		// The display concept name like "Normal Blood Lymphocyte" for dataset with conceptId of "1222211"
		Map<String, String> conceptIdToDisplayNameMap = new HashMap<String, String>();
		
		// Get the dataset list from subject lists, and organize them in pairs for each patient.
		String commonPlatformName = null;	// To make sure there is noly one platform among all the datasets
		String commonTrialName = null;	// For now only one trial is allowed.
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlStr = "select * from de_subject_snp_dataset where patient_num in (" + subjectListStr + ")";
		sql.eachRow(sqlStr) { row ->
			SnpDataset snpDataset = new SnpDataset();
			snpDataset.id = row.subject_snp_dataset_id;
			snpDataset.datasetName = row.dataset_name;
			snpDataset.conceptId = row.concept_cd;
			snpDataset.conceptName = getConceptDisplayName(snpDataset.conceptId, conceptIdToDisplayNameMap);
			snpDataset.platformName = row.platform_name;
			if (commonPlatformName == null) commonPlatformName = snpDataset.platformName;
			else if (commonPlatformName.equals(snpDataset.platformName) == false) {
				throw new Exception ("The platform for SnpDataset " + snpDataset.datasetName + ", " + snpDataset.platformName + ", is different from previous platform " + commonPlatformName);
			}
			snpDataset.trialName = row.trial_name;
			if (commonTrialName == null) commonTrialName = snpDataset.trialName;
			else if (commonTrialName.equals(snpDataset.trialName) == false) {
				throw new Exception ("The trial for SnpDataset " + snpDataset.datasetName + ", " + snpDataset.trialName + ", is different from previous trial " + commonTrialName);
			}
			snpDataset.patientNum = row.patient_num;
			snpDataset.timePoint = row.timepoint;
			snpDataset.subjectId = row.subject_id;
			snpDataset.sampleType = row.sample_type;
			snpDataset.pairedDatasetId = row.paired_dataset_id;
			snpDataset.patientGender = row.patient_gender;
			
			SnpDataset[] snpDatasetPair = snpDatasetBySubjectMap.get(snpDataset.patientNum);
			if (snpDatasetPair == null) {
				snpDatasetPair = new SnpDataset[2];
				snpDatasetBySubjectMap.put(snpDataset.patientNum, snpDatasetPair);
			}
			if (snpDataset.sampleType.equals(SnpDataset.SAMPLE_TYPE_NORMAL)) {
				snpDatasetPair[0] = snpDataset;
			}
			else {
				snpDatasetPair[1] = snpDataset;
			}
		}
	}
	
	void getPatientGenderMap(String subjectListStr, Map<Long, String> patientGenderMap) {
		if (patientGenderMap == null) throw new Exception ("The object patientGenderMap is not instantiated");
		if (subjectListStr == null || subjectListStr.length() == 0) return;
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		sql.eachRow("select patient_num, sex_cd from patient_dimension where patient_num in (" + subjectListStr + ")") { row ->
			Long patientNum = row.patient_num;
			String gender = row.sex_cd;
			if (gender != null) {
				patientGenderMap.put(patientNum, gender);
			}
		}
	
	}
	
	Set<Long> getSnpSet(Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap) {
		if (allGeneSnpMap == null || allGeneSnpMap.size() == 0) return null;
		Set<Long> allSnpSet = new HashSet<Long>();
		for (Map.Entry mapEntry : allGeneSnpMap) {
			SortedMap<Long, Map<Long, GeneWithSnp>> geneWithSnpMapChrom = mapEntry.getValue();
			for (Map.Entry geneWithSnpMapChromEntry : geneWithSnpMapChrom) {
				Map<Long, GeneWithSnp> geneMap = geneWithSnpMapChromEntry.getValue();
				for (Map.Entry geneMapEntry : geneMap) {
					GeneWithSnp gene = geneMapEntry.getValue();
					for (Map.Entry snpMapEntry : gene.snpMap) {
						SnpInfo snp = snpMapEntry.getValue();
						allSnpSet.add(snp.id);
					}
				}
			}
		}
		return allSnpSet;
	}
	
	void getSnpSampleInfo(List<SnpDataset> datasetList, List<String> datasetNameForSNPViewerList, 
				List<Long>[] patientNumListArray, Map<Long, SnpDataset[]> snpDatasetBySubjectMap, StringBuffer sampleInfoBuf) {
		if (datasetList == null) throw new Exception("The datasetList is null");
		if (patientNumListArray == null) throw new Exception("The patient number list for two subsets cannot be null");
		if (sampleInfoBuf == null) throw new Exception("The StringBuffer for sample info text needs to instantiated");
		// Organize the datasetList and SNPViewer dataset name List, also generate the SNPViewer sample info text in this pass
		sampleInfoBuf.append("Array\tSample\tType\tPloidy(numeric)\tGender\tPaired");
		for (int idxSubset = 0; idxSubset < 2; idxSubset ++) {
			if (patientNumListArray[idxSubset] != null) {
				for (Long patientNum : patientNumListArray[idxSubset]) {
					SnpDataset[] snpDatasetPair = snpDatasetBySubjectMap.get(patientNum);
					if (snpDatasetPair != null) {
						String datasetNameForSNPViewer_1 = null;
						String datasetNameForSNPViewer_2 = null;
						if (snpDatasetPair[0] != null) {	// Has the control dataset
							SnpDataset snpDataset_1 = snpDatasetPair[0];
							datasetNameForSNPViewer_1 = "S" + (idxSubset + 1) + "_" + snpDataset_1.datasetName;
							datasetList.add(snpDataset_1);
							datasetNameForSNPViewerList.add(datasetNameForSNPViewer_1);
							sampleInfoBuf.append("\n" + datasetNameForSNPViewer_1 + "\t" + datasetNameForSNPViewer_1 + "\t" +
								snpDataset_1.conceptName + "\t2\t" + snpDataset_1.patientGender + "\t");
							if (snpDatasetPair[1] != null)
								sampleInfoBuf.append("Yes");	// Paired
							else
								sampleInfoBuf.append("No");	// Not paired
						}
						if (snpDatasetPair[1] != null) {	// Has the control dataset
							SnpDataset snpDataset_2 = snpDatasetPair[1];
							datasetNameForSNPViewer_2 = "S" + (idxSubset + 1) + "_" + snpDataset_2.datasetName;
							datasetList.add(snpDataset_2);
							datasetNameForSNPViewerList.add(datasetNameForSNPViewer_2);
							sampleInfoBuf.append("\n" + datasetNameForSNPViewer_2 + "\t" + datasetNameForSNPViewer_2 + "\t" +
								snpDataset_2.conceptName + "\t2\t" + snpDataset_2.patientGender + "\t");
							if (snpDatasetPair[0] != null)
								sampleInfoBuf.append(datasetNameForSNPViewer_1);	// Paired
							else
								sampleInfoBuf.append("No");	// Not paired
						}
					}
				}
			}
		}
	}
	
				
	void getSnpGeneAnnotationPage(StringBuffer geneSnpPageBuf, Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap,
					Map<Long, GeneWithSnp> geneEntrezIdMap, Map<String, GeneWithSnp> geneNameToGeneWithSnpMap, 
					List<String> geneNameList, List<String> snpNameList) {
		geneSnpPageBuf.append("<html><header></hearder><body><p>Selected Genes and SNPs</p>");
		geneSnpPageBuf.append("<table width='100%' border='1' cellpadding='4' cellspacing='3'><tr align='center'><th>Gene</th><th>SNP</th><th>Chrom</th><th>Position</th></tr>");
		String[] allChromArray = getAllChromArray();
		Set<String> snpNameSet = new HashSet<String>();
		if (snpNameList != null) {
			snpNameSet.addAll(snpNameList);
		}
		
		Set<String> geneNotUsedNameSet = new HashSet<String>();
		if (geneNameList != null && geneNameList.size() != 0) {
			for (String geneName : geneNameList) {
				if (geneNameToGeneWithSnpMap.containsKey(geneName) == false)
					geneNotUsedNameSet.add(geneName);
			}
		}
		
		Set<String> snpUsedNameSet = new HashSet<String>();
		
		for (String chrom : allChromArray) {
			SortedMap<Long, Map<Long, GeneWithSnp>> geneMapChrom = allGeneSnpMap.get(chrom);
			for (Map.Entry geneMapEntry : geneMapChrom) {
				Map<Long, GeneWithSnp> geneMap = geneMapEntry.getValue();
				for (Map.Entry geneEntry : geneMap) {
					GeneWithSnp gene = geneEntry.getValue();
					SortedMap<Long, SnpInfo> snpMap = gene.snpMap;
					String geneDisplay = gene.name;
					if (geneEntrezIdMap != null && geneEntrezIdMap.get(gene.entrezId) != null) {	// This gene is selected by user
						geneDisplay = "<font color='red'>" + gene.name + "</font>";
					}
					geneSnpPageBuf.append("<tr align='center' valign='top'><td rowspan='" + snpMap.size() + "'>" + geneDisplay + "</td>");
					boolean firstEntry = true;
					for (Map.Entry snpEntry : snpMap) {
						SnpInfo snp = snpEntry.getValue();
						String snpDisplay = snp.name;
						snpUsedNameSet.add(snpDisplay);
						if (snpNameSet != null && snpNameSet.contains(snp.name)) {	// This SNP is entered by user
							snpDisplay = "<font color='red'>" + snp.name + "</font>";
						}
						if (firstEntry == true) {
							geneSnpPageBuf.append("<td>" + snpDisplay + "</td><td>" + snp.chrom + "</td><td>" + snp.chromPos + "</td></tr>");
						}
						else {
							geneSnpPageBuf.append("<tr align='center'><td>" + snpDisplay + "</td><td>" + snp.chrom + "</td><td>" + snp.chromPos + "</td></tr>");
						}
						firstEntry = false;
					}
				}
			}
		}
		geneSnpPageBuf.append("</table>");
		
		if (geneNotUsedNameSet != null && geneNotUsedNameSet.size() != 0) {
			StringBuffer geneBuf = new StringBuffer();
			for (String geneName : geneNotUsedNameSet) {
				if (geneBuf.length() != 0)
					geneBuf.append(", ");
				geneBuf.append(geneName);
			}
			geneSnpPageBuf.append("<p>The user-selected genes that do not have matching SNP data: " + geneBuf.toString() + "</p>");
		}
		
		if (snpNameList != null && snpNameList.size() != 0) {
			Set<String> snpNotUsedNameSet = new HashSet<String>();	// Need to get the list of SNPs that do not have data
			for (String snpName : snpNameList) {
				if (snpUsedNameSet != null && snpUsedNameSet.size() != 0) {
					if (snpUsedNameSet.contains(snpName) == false)
						snpNotUsedNameSet.add(snpName);
				}
				else {
					snpNotUsedNameSet.add(snpName);
				}
			}
			if (snpNotUsedNameSet != null && snpNotUsedNameSet.size() != 0) {
				StringBuffer snpBuf = new StringBuffer();
				for (String snpName : snpNotUsedNameSet) {
					if (snpBuf.length() != 0)
						snpBuf.append(", ");
					snpBuf.append(snpName);
				}
				geneSnpPageBuf.append("<p>The user-selected SNPs that do not have data: " + snpBuf.toString() + "</p>");
			}
		}
		
		geneSnpPageBuf.append("</body></html>");
	}
		
				
	void getSNPViewerDataByPatient(String subjectIds1, String subjectIds2, String chroms, SnpViewerFiles snpFiles) throws Exception {
		if (snpFiles == null) throw new Exception("The SNPViewerFiles object is not instantiated");
		
		List<Long>[] patientNumListArray = new List<Long>[2]; // For the patient numbers selected by users in subset 1 and subset 2
		patientNumListArray[0] = getPatientNumListFromSubjectIdStr(subjectIds1);
		patientNumListArray[1] = getPatientNumListFromSubjectIdStr(subjectIds2);

		// Get SQL query String for all the subject IDs
		String subjectListStr = "";
		if (subjectIds1 != null && subjectIds1.length() != 0) subjectListStr += subjectIds1;
		if (subjectIds2 != null && subjectIds2.length() != 0) {
			if (subjectListStr.length() != 0) subjectListStr += ", ";
			subjectListStr += subjectIds2;
		}
		
		Map<Long, SnpDataset[]> snpDatasetBySubjectMap = new HashMap<Long, SnpDataset[]>();
		getSnpDatasetBySubjectMap(snpDatasetBySubjectMap, subjectListStr);
		if (snpDatasetBySubjectMap == null || snpDatasetBySubjectMap.size() == 0) {
			throw new Exception("Error: The selected cohorts do not have SNP data.");
		}
		
		StringBuffer sampleInfoBuf = new StringBuffer();
		List<SnpDataset> datasetList = new ArrayList<SnpDataset>();
		List<String> datasetNameForSNPViewerList = new ArrayList<String>();
		getSnpSampleInfo(datasetList, datasetNameForSNPViewerList, patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf);
		
		Map<Long, Map<String, String>> snpDataByDatasetByChrom = getSNPDataByDatasetByChrom(subjectListStr, chroms);

		/** There is a bug in GenePattern SNPViewer. If there is no probe position information for previous chrom,
		 * The display of chroms becomes erratic.
		 * The work-around is to enter dummy data for starting and ending probes of the absent chrom, so 
		 SNPViewer can display the chrom number correctly. Need to build a list of chroms to the last used chrom*/
		
		List<String> neededChroms = getSortedChromList(chroms);
		String[] allChroms = getAllChromArray();
		Map<String, String[]> allChromEndProbeLines = getChromEndProbeLineMap();
		String lastChrom = neededChroms.get(neededChroms.size() - 1);
		
		String platform = datasetList.get(0).platformName;
		Map<String, SnpProbeSortedDef> probeDefMap = getSNPProbeDefMap(platform, chroms);
		
		BufferedWriter dataWriter = new BufferedWriter(new FileWriter(snpFiles.dataFile));
		
		// Write the header column
		dataWriter.write("SNP\tChromosome\tPhysicalPosition");
		for (String datasetName : datasetNameForSNPViewerList) {
			dataWriter.write("\t" + datasetName + "\t" + datasetName + " Call");
		}
		dataWriter.write("\n");
		
		for (String chrom : neededChroms) {
			SnpProbeSortedDef probeDef = probeDefMap.get(chrom);
			if (probeDef != null) {	// This chrom is selected by user
				// Create the list of BufferedReader for SNP data for each dataset for this chrom
				List<StringLineReader> snpDataReaderList = new ArrayList<StringLineReader>();
				for (SnpDataset dataset : datasetList) {
					Map<String, String> snpDataByChrom = snpDataByDatasetByChrom.get(dataset.id);
					String snpDataStr = snpDataByChrom.get(chrom);
					StringLineReader dataReader = new StringLineReader(snpDataStr);
					snpDataReaderList.add(dataReader);
				}
				
				String probeDefStr = probeDef.snpIdDef;
				Integer numProbe = probeDef.getNumProbe();
				StringLineReader probeReader = new StringLineReader(probeDefStr);
				for (int idx = 0; idx < numProbe.intValue(); idx ++) {
					String probeLine = probeReader.readLine();
					if (probeLine == null || probeLine.trim().length() == 0)
						throw new Exception("The number " + idx + " line in probe definition file for chromosome " + chrom + " is empty");
					dataWriter.write(probeLine);
					
					for (StringReader dataReader : snpDataReaderList) {
						dataWriter.write("\t" + dataReader.readLine());
					}
					dataWriter.write("\n");
				}
			}
			else { // This chrom need dummy data for the starting and ending probes
				String[] endProbeLines = allChromEndProbeLines.get(chrom);
				dataWriter.write(endProbeLines[0]);
				for (SnpDataset dataset : datasetList) {
					dataWriter.write("\t2.0\tNC");
				}
				dataWriter.write("\n");
				
				dataWriter.write(endProbeLines[1]);
				for (SnpDataset dataset : datasetList) {
					dataWriter.write("\t2.0\tNC");
				}
				dataWriter.write("\n");
			}
			if (chrom.equals(lastChrom)) break;	// Stop at the last needed chrom
		}
		snpFiles.sampleFile << sampleInfoBuf;
		dataWriter.close();
		
	}
	
	
	
	void getGwasDataByPatient(List<String> subjectIdList1, List<String> subjectIdList2, String chroms, GwasFiles gwasFiles) throws Exception {
		if (gwasFiles == null) throw new Exception("The GwasFiles object is not instantiated");

		String subjectIds1 = "", subjectIds2 = "";
		if (subjectIdList1 != null && subjectIdList1.size() != 0) {
			for (String subjectId : subjectIdList1) {
				if (subjectIds1.length() != 0) {
					subjectIds1 += ",";
				}
				subjectIds1 += subjectId;
			}
		}
		if (subjectIdList2 != null && subjectIdList2.size() != 0) {
			for (String subjectId : subjectIdList2) {
				if (subjectIds2.length() != 0) {
					subjectIds2 += ",";
				}
				subjectIds2 += subjectId;
			}
		}
		
		List<Long>[] patientNumListArray = new List<Long>[2]; // For the patient numbers selected by users in subset 1 and subset 2
		patientNumListArray[0] = getPatientNumListFromSubjectIdStr(subjectIds1);
		patientNumListArray[1] = getPatientNumListFromSubjectIdStr(subjectIds2);
		
		List<Integer> patientCountList = gwasFiles.patientCountList;
		patientCountList.add(new Integer(patientNumListArray[0].size()));
		patientCountList.add(new Integer(patientNumListArray[1].size()));
				
		List<Integer> datasetCountList = gwasFiles.datasetCountList;
		List<Long> idList1 = getSNPDatasetIdList(subjectIds1);
		List<Long> idList2 = getSNPDatasetIdList(subjectIds2);
		datasetCountList.add(new Integer(idList1.size()));
		datasetCountList.add(new Integer(idList2.size()));
		
		// Get SQL query String for all the subject IDs
		String subjectListStr = "";
		if (subjectIds1 != null && subjectIds1.length() != 0) subjectListStr += subjectIds1;
		if (subjectIds2 != null && subjectIds2.length() != 0) {
			if (subjectListStr.length() != 0) subjectListStr += ", ";
			subjectListStr += subjectIds2;
		}
		
		Map<Long, SnpDataset[]> snpDatasetBySubjectMap = new HashMap<Long, SnpDataset[]>();
		getSnpDatasetBySubjectMap(snpDatasetBySubjectMap, subjectListStr);
		
		Map<Long, String> patientGenderMap = new HashMap<Long, String>();
		getPatientGenderMap(subjectListStr, patientGenderMap);

		StringBuffer sampleInfoBuf = new StringBuffer();
		List<SnpDataset> datasetList = new ArrayList<SnpDataset>();
		List<String> datasetNameForSNPViewerList = new ArrayList<String>();
		getSnpSampleInfo(datasetList, datasetNameForSNPViewerList, patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf);
		
		Map<Long, Map<String, String>> snpDataByDatasetByChrom = getSNPDataByDatasetByChrom(subjectListStr, chroms);

		List<String> neededChroms = getSortedChromList(chroms);
		gwasFiles.setChromList(neededChroms);
		String platform = datasetList.get(0).platformName;
		Map<String, SnpProbeSortedDef> probeDefMap = getSNPProbeDefMap(platform, chroms);

		BufferedWriter mapWriter = new BufferedWriter(new FileWriter(gwasFiles.getMapFile()));
		for (String chrom : neededChroms) {
			SnpProbeSortedDef probeDef = probeDefMap.get(chrom);
			StringLineReader probeReader = new StringLineReader(probeDef.snpIdDef);
			String probeLine = probeReader.readLine();
			while (probeLine != null && probeLine.trim().length() != 0) {
				String[] probeValues = probeLine.split("\t");
				String snpName = probeValues[0];
				String chromProbe = probeValues[1];
				String chromPosProbe = probeValues[2];
				mapWriter.write(chromProbe + " " + snpName + " 0 " + chromPosProbe + "\n");
				
				probeLine = probeReader.readLine();
			}
		}
		mapWriter.close();
		
		BufferedWriter pedWriter = new BufferedWriter(new FileWriter(gwasFiles.getPedFile()));
		for (int idxSet = 1; idxSet <= patientNumListArray.length; idxSet ++) {
			List<Long> patientNumList = patientNumListArray[idxSet - 1];
			for (Long patientNum : patientNumList) {
				String gender = patientGenderMap.get(patientNum);
				String genderStr = "other";
				if (gender != null) {
					if (gender.equalsIgnoreCase("M")) {
						genderStr = "1";
					}
					else if (gender.equalsIgnoreCase("F")) {
						genderStr = "2";
					}
				}
				
				SnpDataset[] snpDataArray = snpDatasetBySubjectMap.get(patientNum);
				for (SnpDataset dataset : snpDataArray) {
					if (dataset == null) continue;	// snpDataArray is means to contain [normal, disease]
					pedWriter.write(dataset.datasetName + " " + dataset.datasetName + " 0 0 " + genderStr + "  " + idxSet);
					Map<String, String> dataByChromMap = snpDataByDatasetByChrom.get(dataset.id);
					for (String chrom : neededChroms) {
						String dataByChrom = dataByChromMap.get(chrom);
						StringLineReader dataReader = new StringLineReader(dataByChrom);
						String dataLine = dataReader.readLine();
						while (dataLine != null && dataLine.trim().length() != 0) {
							String[] dataValues = dataLine.split("\t");
							String genotype = dataValues[1].trim();
							String genotype_1 = genotype.substring(0,1);
							String genotype_2 = genotype.substring(1);
							if (genotype.equalsIgnoreCase("NC") == true) {
								genotype_1 = "0";
								genotype_2 = "0";
							}
							pedWriter.write("  " + genotype_1 + " " + genotype_2);
							
							dataLine = dataReader.readLine();
						}
					}
					pedWriter.write("\n");
				}
			}
		}
		pedWriter.close();
		
	}
	
	def runPlink (GwasFiles gwasFiles) {
		if (gwasFiles == null) throw new Exception ("The object GwasFiles is not instantiated");
		File pedFile = gwasFiles.getPedFile();
		String pedFilePath = pedFile.absolutePath;
		File mapFile = gwasFiles.getMapFile();
		String mapFilePath = mapFile.absolutePath;
		String outputFileRoot = pedFile.parent + File.separator + gwasFiles.fileNameRoot
		
		String plinkExecutable = ConfigurationHolder.config.com.recomdata.datasetExplorer.plinkExcutable;
		
		String cmdLine = plinkExecutable + " --ped " + pedFilePath + " --map " + mapFilePath + " --out " + outputFileRoot + " --assoc --noweb";
		
		String[] cmdLineArray = [plinkExecutable, "--ped", pedFilePath, "--map", mapFilePath, "--out", outputFileRoot, "--assoc", "--noweb"];
		def p = cmdLineArray.execute();
		def errStream = new ByteArrayOutputStream(4096);
		p.consumeProcessOutput();
		p.consumeProcessErrorStream(errStream);
		p.waitFor();
		File assocFile = new File(outputFileRoot + ".assoc");
		if (assocFile.isFile() == false) throw new Exception ("PLINK failed to run");
		gwasFiles.assocFile = assocFile;
	}
	
	def reportGwas (String userName, GwasFiles gwasFiles, String querySummary1, String querySummary2) {
		if (gwasFiles == null) throw new Exception ("The object GwasFiles is not instantiated");
		File assocFile = gwasFiles.assocFile;
		if (assocFile  == null || assocFile.isFile() == false) throw new Exception ("The PLINK output .assoc file does not exist");
		
		TreeMap<Float, String[]> mostSignificantSnps = new TreeMap<Float, String[]>();
		TreeMap<Float, String[]> significantSnps = new TreeMap<Float, String[]>();
		Float pValueMostSignificant = new Float("1E-6");
		Float pValueSignificant = new Float("0.01");
		Map<String, String[]> snpNameDataMap = new HashMap<String, String[]>();
		assocFile.eachLine { line ->
			if (line.startsWith(" CHR") == false) {
				String chrom = line.substring(0, 4).trim();
				String snpName = line.substring(4, 17).trim();
				String chromPos = line.substring(17, 28).trim();
				String chiSquare = line.substring(56, 69).trim();
				String pValue = line.substring(69, 82).trim();
				String oddsRatio = line.substring(82, 95).trim();
				
				Float pValueFloat = null;
				try {
					pValueFloat = new Float(pValue);
				}
				catch (Exception e) {}	// pValue could be "NA"
				
				if (pValueFloat != null) {
					String[] snpData = new String[6];
					snpData[0] = snpName;
					snpData[1] = chrom;
					snpData[2] = chromPos;
					snpData[3] = pValue;
					snpData[4] = chiSquare;
					snpData[5] = oddsRatio;
					if (Float.compare(pValueFloat, pValueMostSignificant) < 0) {
						mostSignificantSnps.put(pValueFloat, snpData);
					}
					else if (Float.compare(pValueFloat, pValueSignificant) < 0) {
						significantSnps.put(pValueFloat, snpData);
					}
					snpNameDataMap.put(snpName, snpData);
				}
			}
		}
		
		TreeMap<Double, Set<String>> entrezScoreNegativeMap = new TreeMap<Double, String>(); // It is difficult to sort descending in JDK 1.5. So sort the negative in ascending
		Map<String, Set<String>> entrezSnpMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> snpEntrezMap = new HashMap<String, Set<String>>();
		Map<String, String> entrezNameMap = new HashMap<String, String>();
		Map<String, String[]> neededSnpNameDataMap = new HashMap<String, String[]>();
		int neededCount = 1000;
		int entryCount = 0;
		for (Map.Entry entry : mostSignificantSnps) {
			entryCount ++;
			if (entryCount > neededCount) break;
			String[] snpData = entry.getValue();
			neededSnpNameDataMap.put(snpData[0], snpData);
		}
		for (Map.Entry entry : significantSnps) {
			entryCount ++;
			if (entryCount > neededCount) break;
			String[] snpData = entry.getValue();
			neededSnpNameDataMap.put(snpData[0], snpData);
		}
		getSnpGeneGwasScore(neededSnpNameDataMap, entrezScoreNegativeMap, entrezSnpMap, snpEntrezMap, entrezNameMap);
		
		StringBuffer buf = new StringBuffer();
		buf.append("<html><head><title>Genome-Wide Association Study using PLINK</title></head><body><h2>Genome-Wide Association Study</h2>");
		String countStr1 = "", countStr2 = "";
		List<Integer> patientCountList = gwasFiles.patientCountList;
		if (patientCountList != null) {
			if (patientCountList.size() > 0 && patientCountList.get(0) != null) {
				countStr1 += "<br/>(" + patientCountList.get(0) + " patients";
			}
			if (patientCountList.size() > 1 && patientCountList.get(1) != null) {
				countStr2 += "<br/>(" + patientCountList.get(1) + " patients";
			}
		}
		List<Integer> datasetCountList = gwasFiles.datasetCountList;
		if (datasetCountList != null) {
			if (datasetCountList.size() > 0 && datasetCountList.get(0) != null) {
				countStr1 += ", " + datasetCountList.get(0) + " datasets)";
			}
			if (datasetCountList.size() > 1 && datasetCountList.get(1) != null) {
				countStr2 += ", " + datasetCountList.get(1) + " datasets)";
			}
		}
		
		String pedFileUrl = gwasFiles.getFileUrlWithSecurityToken(gwasFiles.getPedFile(), userName);
		String mapFileUrl = gwasFiles.getFileUrlWithSecurityToken(gwasFiles.getMapFile(), userName);
		String assocFileUrl = gwasFiles.getFileUrlWithSecurityToken(gwasFiles.getAssocFile(), userName);
		
		buf.append("<table border='1' width='100%'><tr><th>Subset 1 Query " + countStr1 + "</th><th>Subset 2 Query "
			+ countStr2 + "</th></tr><tr><td>" + querySummary1 + "</td><td>" + querySummary2 + "</td></tr></table>");

		List<String> chromList = gwasFiles.getChromList();
		StringBuffer chromBuf = new StringBuffer();
		for (String chrom : chromList) {
			if (chromBuf.length() != 0) {
				chromBuf.append(", ");
			}
			chromBuf.append(chrom);
		}
		buf.append("<h3>Chromosomes:  " + chromBuf.toString() + "</p>");
		
		buf.append("<h3>Data Files</h3>");
		buf.append("<table cellpadding='2'><tr><td><a href='" + pedFileUrl + "'>PED File</a></td><td><a href='" + 
			mapFileUrl + "'>MAP File</a></td><td><a href='" + assocFileUrl + "'>Association File</a></td></tr></table>");
		
		buf.append("<h3>Most Significantly Associated Genes</h3>");
		buf.append("<table border='1' cellpadding='2'><tr><th>Gene</th><th>Total p-Value Score</th><th>SNP (p-Value)</th></tr>");
		for (Map.Entry scoreEntry : entrezScoreNegativeMap) {
			Double scoreNegative = scoreEntry.getKey();
			double score = 0 - (scoreNegative.doubleValue());
			Set<String> entrezSet = scoreEntry.getValue();
			for (String entrezId : entrezSet) {
				StringBuffer snpBuf = new StringBuffer();
				Set<String> snpNameSet = entrezSnpMap.get(entrezId);
				for (String snpName : snpNameSet) {
					String[] snpData = snpNameDataMap.get(snpName);
					String pValue = snpData[3];
					if (snpBuf.length() != 0)
						snpBuf.append(", ");
					snpBuf.append(snpName + " (" + pValue + ")");
				}
				String geneName = entrezNameMap.get(entrezId);
						if (geneName == null) {	// This Entrez ID does not have a Gene:xxxx name in the search_keyword table
							geneName = "(Gene: Entrez ID " + entrezId + ")";
						}
				buf.append("<tr><td>" + geneName + "</td><td>" + String.format("%.2f", score) + "</td><td>" + snpBuf.toString() + "</td></tr>");
			}
		}
		buf.append("</table>");
		
		buf.append("<h3>Most Significant SNPs</h3>");
		if (mostSignificantSnps != null && mostSignificantSnps.size() != 0) {
			buf.append("<table border='1' cellpadding='2'><tr><th>SNP</th><th>Chrom</th><th>Position</th><th>P Value</th><th>Chi Square</th><th>Odds Ratio</th><th>Mapped to Genes</th></tr>");
			for (Map.Entry snpEntry : mostSignificantSnps) {
				String[] snpData = snpEntry.getValue();
				String snpName = snpData[0];
				StringBuffer geneBuf = new StringBuffer();
				Set<String> entrezSet = snpEntrezMap.get(snpName);
				if (entrezSet != null && entrezSet.size() != 0) {
					for (String entrezId : entrezSet) {
						String geneName = entrezNameMap.get(entrezId);
						if (geneName == null) {	// This Entrez ID does not have a Gene:xxxx name in the search_keyword table
							geneName = "(Gene: Entrez ID " + entrezId + ")";
						}
						if (geneBuf.length() != 0)
							geneBuf.append(", ");
						geneBuf.append(geneName);
					}
				}
				buf.append("<tr><td>" + snpData[0] + "</td><td>" + snpData[1] + "</td><td>" + snpData[2] + "</td><td>" + snpData[3] + "</td><td>" + 
					snpData[4] + "</td><td>" + snpData[5] + "</td><td>" + geneBuf.toString() + "</td></tr>");
			}
			buf.append("</table>");
		}
		else {
			buf.append("<p>None</p>");
		}
		buf.append("<h3>Significant SNPs</h3>");
		if (significantSnps != null && significantSnps.size() != 0) {
			buf.append("<table border='1' cellpadding='2'><tr><th>SNP</th><th>Chrom</th><th>Position</th><th>P Value</th><th>Chi Square</th><th>Odds Ratio</th><th>Mapped to Genes</th></tr>");
			for (Map.Entry snpEntry : significantSnps) {
				String[] snpData = snpEntry.getValue();
				String snpName = snpData[0];
				StringBuffer geneBuf = new StringBuffer();
				Set<String> entrezSet = snpEntrezMap.get(snpName);
				if (entrezSet != null && entrezSet.size() != 0) {
					for (String entrezId : entrezSet) {
						String geneName = entrezNameMap.get(entrezId);
						if (geneName == null) {	// This Entrez ID does not have a Gene:xxxx name in the search_keyword table
							geneName = "(Gene: Entrez ID " + entrezId + ")";
						}
						if (geneBuf.length() != 0)
							geneBuf.append(", ");
						geneBuf.append(geneName);
					}
				}
				buf.append("<tr><td>" + snpData[0] + "</td><td>" + snpData[1] + "</td><td>" + snpData[2] + "</td><td>" + snpData[3] + "</td><td>" + 
					snpData[4] + "</td><td>" + snpData[5] + "</td><td>" + geneBuf.toString() + "</td></tr>");
			}
			buf.append("</table>");
		}
		else {
			buf.append("<p>None</p>");
		}
		buf.append("</body></html>");
		
		gwasFiles.reportFile << buf.toString();
	}
	
	void getSnpGeneGwasScore(Map<String, String[]> snpNameDataMap, SortedMap<Double, Set<String>> entrezScoreNegativeMap, 
		Map<String, Set<String>> entrezSnpMap, Map<String, Set<String>> snpEntrezMap, Map<String, String> entrezNameMap){
		if (snpNameDataMap == null) throw new Exception ("The object snpNameDataMap is not instantiated");
		if (snpNameDataMap.size() == 0) return;
		if (entrezScoreNegativeMap == null) throw new Exception ("The object entrezScoreMap is not instantiated");
		if (entrezSnpMap == null) throw new Exception ("The object entrezSnpMap is not instantiated");
		if (snpEntrezMap == null) throw new Exception ("The object snpEntrezMap is not instantiated");
		if (entrezNameMap == null) throw new Exception ("The object entrezNameMap is not instantiated");
		
		StringBuffer snpNamesBuf = new StringBuffer();
		for (Map.Entry snpEntry : snpNameDataMap) {
			String snpName = snpEntry.getKey();
			if (snpNamesBuf.length() != 0)
				snpNamesBuf.append(",");
			snpNamesBuf.append("'" + snpName + "'");
		}
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlStr = "select * from de_snp_gene_map where snp_name in (" + snpNamesBuf.toString() + ")";
		sql.eachRow(sqlStr) {row ->
			String snpName = row.snp_name;
			String entrezId = row.entrez_gene_id;
			
			Set<String> snpSet = entrezSnpMap.get(entrezId);
			if (snpSet == null) {
				snpSet = new HashSet<String>();
				entrezSnpMap.put(entrezId, snpSet);
			}
			snpSet.add(snpName);
			
			Set<String> entrezSet = snpEntrezMap.get(snpName);
			if (entrezSet == null) {
				entrezSet = new HashSet<String>();
				snpEntrezMap.put(snpName, entrezSet);
			}
			entrezSet.add(entrezId);
		}
		
		// Contruct the entrezId list string, and get entrezNameMap
		StringBuffer entrezListBuf = new StringBuffer();
		for (Map.Entry entrezEntry : entrezSnpMap) {
			String entrezId = entrezEntry.getKey();
			if (entrezListBuf.length() != 0)
				entrezListBuf.append(",");
			entrezListBuf.append("'GENE:" + entrezId + "'");
		}
		sqlStr = "select keyword, unique_id from search_keyword where unique_id in (" + entrezListBuf.toString() + ")";
		sql.eachRow(sqlStr) {row ->
			String geneName = row.keyword;
			String entrezStr = row.unique_id;
			String entrezId = entrezStr.substring(5);
			entrezNameMap.put(entrezId, geneName);
		}	
		
		// Calculate the total p-value score for each gene
		for (Map.Entry entrezEntry : entrezSnpMap) {
			String entrezId = entrezEntry.getKey();
			Set<String> snpSet = entrezEntry.getValue();
			double score = 0;
			for (String snpName : snpSet) {
				String[] snpData = snpNameDataMap.get(snpName);
				if (snpData != null && snpData.size() > 3) {
					String pValueStr = snpData[3];
					try {
						double pValue = Double.parseDouble(pValueStr);
						double pLog = Math.log10(pValue);
						score += pLog;
					}
					catch (Exception e) {}
				}
			}
			Double scoreDouble = new Double(score);
			Set<String> entrezSet = entrezScoreNegativeMap.get(scoreDouble);
			if (entrezSet == null) {
				entrezSet = new HashSet<String>();
				entrezScoreNegativeMap.put(scoreDouble, entrezSet);
			}
			entrezSet.add(entrezId);
		}
	}
	
	Map<Long, Map<String, String>> getSNPDataByDatasetByChrom(String subjectIds, String chroms) throws Exception {
		if (subjectIds == null || subjectIds.trim().length() == 0) return null;
		Map<Long, Map<String, String>> snpDataByDatasetByChrom = new HashMap<Long, Map<String, String>>();	// Map<[datasetId], Map<chrom, data>>

		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		// Get the list of dataset first, SNP data will be fetched later
		String sqlt = "SELECT a.*, b.chrom as chrom, b.data_by_patient_chr as data FROM de_subject_snp_dataset a, de_snp_data_by_patient b ";
		sqlt += "WHERE a.subject_snp_dataset_id = b.snp_dataset_id and a.patient_num in (" + subjectIds + ") ";
		sqlt += " and b.chrom in (" + getSqlStrFromChroms(chroms) + ") ";
		
		sql.eachRow(sqlt) {row ->
			Long datasetId = row.subject_snp_dataset_id;
			String chrom = row.chrom;
			java.sql.Clob clob = (java.sql.Clob)row.data;
			String data = clob.getAsciiStream().getText();
			
			Map<String, String> dataByChromMap = snpDataByDatasetByChrom.get(datasetId);
			if (dataByChromMap == null) {
				dataByChromMap = new HashMap<String, String>();
				snpDataByDatasetByChrom.put(datasetId, dataByChromMap);
			}
			dataByChromMap.put(chrom, data);
		}
		
		return snpDataByDatasetByChrom;
	}
	
	SnpDatasetByPatient getSnpDatasetByPatientById(Long datasetId, List<SnpDatasetByPatient> datasetByPatientList) {
		if (datasetByPatientList == null) return null;
		for (SnpDatasetByPatient datasetByPatient : datasetByPatientList) {
			SnpDataset dataset = datasetByPatient.snpDataset;
			if (dataset.getId() != null && dataset.getId().equals(datasetId)) {
				return datasetByPatient;
			}
		}
		return null;
	}
	
	String[] getAllChromArray() {
		String[] allChroms = [
			"1",
			"2",
			"3",
			"4",
			"5",
			"6",
			"7",
			"8",
			"9",
			"10",
			"11",
			"12",
			"13",
			"14",
			"15",
			"16",
			"17",
			"18",
			"19",
			"20",
			"21",
			"22",
			"X",
			"Y"
		];
		return allChroms;
	}
	
	List<String> getSortedChromList(String chromListStr) {
		String[] chromArray = chromListStr.split(",");
		Set<String> chromSet = new HashSet<String>();
		for (String chrom : chromArray) {
			chromSet.add(chrom.trim());
		}
		return getSortedChromList(chromSet);
	}
	
	List<String> getSortedChromList(Set<String> chromSet) {
		if (chromSet == null || chromSet.size() == 0) return null;
		List<String> chromList = new ArrayList<String>();
		if (chromSet.size() == 1) {
			for (String chrom : chromSet){
				chromList.add(chrom);
			}
			return chromList;
		}
		
		String[] allChroms = getAllChromArray();
		SortedMap<Integer, String> chromIndexMap = new TreeMap<Integer, String>();
		for (String chrom : chromSet) {
			for (int i = 0; i < allChroms.length; i++) {
				if (chrom.equals(allChroms[i])) {
					chromIndexMap.put(new Integer(i), chrom);
				}
			}
		}
		Iterator mapIt = chromIndexMap.iterator();
		for(Map.Entry<Integer, String> entry : chromIndexMap.entrySet()) {
			chromList.add(entry.getValue());
		}
		return chromList;
	}
	
	Map<String, String[]> getChromEndProbeLineMap() {
		
		Map<String, String[]> chromEndProbeLineMap = new HashMap<String, String[]>();
		chromEndProbeLineMap.put('1', [
			"SNP_A-8575125\t1\t564621",
			"SNP_A-8391333\t1\t249198692"
		]);
		chromEndProbeLineMap.put('2', [
			"SNP_A-8615982\t2\t15703",
			"SNP_A-8304446\t2\t243048760"
		]);
		chromEndProbeLineMap.put('3', [
			"SNP_A-2100278\t3\t66866",
			"SNP_A-8336753\t3\t197856433"
		]);
		chromEndProbeLineMap.put('4', [
			"SNP_A-8661350\t4\t45410",
			"SNP_A-8713585\t4\t190921709"
		]);
		chromEndProbeLineMap.put('5', [
			"SNP_A-8392711\t5\t36344",
			"SNP_A-2186029\t5\t180692833"
		]);
		chromEndProbeLineMap.put('6', [
			"SNP_A-8533260\t6\t203249",
			"SNP_A-8608599\t6\t170918031"
		]);
		chromEndProbeLineMap.put('7', [
			"SNP_A-8539824\t7\t43259",
			"SNP_A-8436508\t7\t159119220"
		]);
		chromEndProbeLineMap.put('8', [
			"SNP_A-8325516\t8\t161222",
			"SNP_A-2094900\t8\t146293414"
		]);
		chromEndProbeLineMap.put('9', [
			"SNP_A-8574568\t9\t37747",
			"SNP_A-8302801\t9\t141071475"
		]);
		chromEndProbeLineMap.put('10', [
			"SNP_A-8435658\t10\t104427",
			"SNP_A-4271863\t10\t135434551"
		]);
		chromEndProbeLineMap.put('11', [
			"SNP_A-8300213\t11\t198510",
			"SNP_A-2246844\t11\t134944770"
		]);
		chromEndProbeLineMap.put('12', [
			"SNP_A-8434276\t12\t161382",
			"SNP_A-4219877\t12\t133777645"
		]);
		chromEndProbeLineMap.put('13', [
			"SNP_A-8687595\t13\t19045720",
			"SNP_A-8587371\t13\t115106996"
		]);
		chromEndProbeLineMap.put('14', [
			"SNP_A-8430270\t14\t20211644",
			"SNP_A-2127677\t14\t107285437"
		]);
		chromEndProbeLineMap.put('15', [
			"SNP_A-8429754\t15\t20071673",
			"SNP_A-8685263\t15\t102400037"
		]);
		chromEndProbeLineMap.put('16', [
			"SNP_A-1807459\t16\t86671",
			"SNP_A-1841720\t16\t90163275"
		]);
		chromEndProbeLineMap.put('17', [
			"SNP_A-8398136\t17\t6689",
			"SNP_A-8656409\t17\t81049726"
		]);
		chromEndProbeLineMap.put('18', [
			"SNP_A-8496414\t18\t11543",
			"SNP_A-8448011\t18\t78015057"
		]);
		chromEndProbeLineMap.put('19', [
			"SNP_A-8509279\t19\t260912",
			"SNP_A-8451148\t19\t59095126"
		]);
		chromEndProbeLineMap.put('20', [
			"SNP_A-8559313\t20\t61795",
			"SNP_A-8480501\t20\t62912463"
		]);
		chromEndProbeLineMap.put('21', [
			"SNP_A-4217519\t21\t9764385",
			"SNP_A-8349060\t21\t48084820"
		]);
		chromEndProbeLineMap.put('22', [
			"SNP_A-8656401\t22\t16055171",
			"SNP_A-8313387\t22\t51219006"
		]);
		chromEndProbeLineMap.put('X', [
			"SNP_A-8572888\tX\t119805",
			"SNP_A-8363487\tX\t154925045"
		]);
		chromEndProbeLineMap.put('Y', [
			"SNP_A-8655052\tY\t2722506",
			"SNP_A-8433021\tY\t28758193"
		]);
		return chromEndProbeLineMap;
	}
	
	String getSqlStrFromChroms(String chroms) {
		if (chroms == null || chroms.trim().length() == 0)
			return "'ALL'";
		String[] values = chroms.split(",");
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < values.length; i++) {
			if (i != 0) buf.append(",");
			buf.append("'" + values[i] + "'");
		}
		return buf.toString();
	}
	
	List<Long> getSNPDatasetIdList(String subjectIds) throws Exception {
		if (subjectIds == null || subjectIds.trim().length() == 0) return null;
		List<Long> idList = null;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlt = "SELECT subject_snp_dataset_id as id FROM de_subject_snp_dataset WHERE patient_num in (" + subjectIds + ") ";
		sql.eachRow(sqlt) {row ->
			Long id = row.id;
			if (idList == null) {
				idList = new ArrayList<Long>();
			}
			idList.add(id);
		}
		return idList;
	}
	
	/**
	 * Original example data files for SNPViewer and IGV use probe name such as "SNP_A-1780419".
	 * It is better to use the target SNP id like "rs6576700" in the data file, so the tooltip in IGV will show the SNP rs id.
	 * @param platformName
	 * @param chroms
	 * @return
	 */
	Map<String, SnpProbeSortedDef> getSNPProbeDefMap(String platformName, String chroms) throws Exception {
		if (platformName == null || platformName.trim().length() == 0) return null;
		Map<String, SnpProbeSortedDef> snpProbeDefMap = new HashMap<String, SnpProbeSortedDef>();
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlt = "SELECT snp_probe_sorted_def_id, platform_name, num_probe, chrom, snp_id_def FROM de_snp_probe_sorted_def WHERE platform_name = '";
		sqlt += platformName + "' and chrom in (" + getSqlStrFromChroms(chroms) + ") order by chrom";
		sql.eachRow(sqlt) {row ->
			SnpProbeSortedDef probeDef = new SnpProbeSortedDef();
			probeDef.id = row.snp_probe_sorted_def_id;
			probeDef.platformName = row.platform_name;
			probeDef.numProbe = row.num_probe;
			probeDef.chrom = row.chrom;
			java.sql.Clob clob = (java.sql.Clob)row.snp_id_def;
			probeDef.snpIdDef = clob.getAsciiStream().getText();
			
			snpProbeDefMap.put(probeDef.chrom, probeDef);
		}
		return snpProbeDefMap;
	}
	
	List<String> getSNPPlatformList(String subjectIds) throws Exception {
		if (subjectIds == null || subjectIds.trim().length() == 0) return null;
		List<String> list = null;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlt = "SELECT distinct(platform_name) as platform FROM de_subject_snp_dataset WHERE patient_num in (" + subjectIds + ") ";
		sql.eachRow(sqlt) {row ->
			if (list == null) {
				list = new ArrayList<String>();
			}
			list.add(row.platform);
		}
		return list;
	}
	
	String getSNPPlatform(String subjectIds1, String subjectIds2) throws Exception {
		String platform = null;
		List<String> platformList1 = getSNPPlatformList(subjectIds1);
		if (platformList1 != null) {
			if (platformList1.size() == 1) platform = platformList1.get(0);
			else throw new Exception ("More than 1 SNP platforms exist in datasets.");
		}
		List<String> platformList2 = getSNPPlatformList(subjectIds2);
		if (platformList2 != null) {
			if (platformList2.size() == 1) {
				String platform2 = platformList2.get(0);
				if (platform == null) platform = platform2;
				else if (platform != null && platform2.equals(platform) == false)
					throw new Exception ("More than 1 SNP platforms exist in datasets.");
			}
			else throw new Exception ("More than 1 SNP platforms exist in datasets.");
		}
		return platform;
	}
	
	void fillCensoringToSurvivalData(Concept conceptCensoring, Map<String, SurvivalData> dataMap, List<String> subjectStrList) throws Exception {
		if (conceptCensoring == null || conceptCensoring.getBaseCode() == null || dataMap == null || dataMap.size() == 0)
			return;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String subjectIdListInStr = DBHelper.listToInString(subjectStrList);
		String sqlt = "SELECT * FROM observation_fact WHERE CONCEPT_CD = ?";
		if (subjectIdListInStr != null)
			sqlt += " and PATIENT_NUM in (" + subjectIdListInStr + ")";
		sql.eachRow(sqlt, [
			conceptCensoring.getBaseCode()
		], {row ->
			String subjectId = row.patient_num;
			String censoringStr = row.tval_char;
			if (censoringStr != null && 
			(censoringStr.equalsIgnoreCase("Censoring") || (censoringStr.equalsIgnoreCase("Yes")))) {	// This patient is censored
				SurvivalData survivalData = dataMap.get(subjectId);
				if (survivalData != null)
					survivalData.isEvent = new Boolean(false);
			}
		})
	}
	
	void fillEventToSurvivalData(Concept conceptEvent, Map<String, SurvivalData> dataMap, List<String> subjectStrList) throws Exception {
		if (conceptEvent == null || conceptEvent.getBaseCode() == null || dataMap == null || dataMap.size() == 0)
			return;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String subjectIdListInStr = DBHelper.listToInString(subjectStrList);
		String sqlt = "SELECT * FROM observation_fact WHERE CONCEPT_CD = ?";
		if (subjectIdListInStr != null)
			sqlt += " and PATIENT_NUM in (" + subjectIdListInStr + ")";
		sql.eachRow(sqlt, [conceptEvent.getBaseCode()], {row ->
			String subjectId = row.patient_num;
			String censoringStr = row.tval_char;
			if (censoringStr != null && 
			(censoringStr.equalsIgnoreCase("Event")) || censoringStr.equalsIgnoreCase("No")) {	// This patient is censored
				SurvivalData survivalData = dataMap.get(subjectId);
				if (survivalData != null)
					survivalData.isEvent = new Boolean(true);
			}
		})
	}
	
	def String getTrialName(String ids) {
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		StringBuilder trialQ = new StringBuilder("select distinct s.trial_name from de_subject_sample_mapping s ");
		trialQ.append(" where s.patient_id in (").append(ids).append(") and s.platform = 'MRNA_AFFYMETRIX'");
		
		log.debug("getTrialName used this query: " + trialQ.toString());
		
		String trialNames = "";
		sql.eachRow(trialQ.toString(), {row ->
			if(trialNames.length()>0){
				trialNames+=",";
			}
			String tName = row.trial_name;
			if (tName.equalsIgnoreCase("BRC Antidepressant Study") ){
				tName ="BRC:mRNA:ADS";
			}
			if (tName.equalsIgnoreCase("BRC Depression Study")){
				tName = "BRC:mRNA:DS";
			}
			trialNames +="'"+tName+"'";
		}
		);
		return trialNames;
	}
	
	/**
	 * Go to the i2b2DemoData.sample_categories table and gather the trial names for the list of sample IDs.
	 * @param ids
	 * @return
	 */
	def String getTrialNameBySampleID(String ids) {
		
		//Create a SQL object.
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		//Build the query to get the trial names.
		StringBuilder trialQ = new StringBuilder("select distinct s.trial_name from i2b2DemoData.sample_categories s ");
		trialQ.append(" where s.SAMPLE_ID in (").append(quoteCSV(ids)).append(")");
		
		//Log the trial query.
		log.debug("getTrialNameBySampleID used this query: " + trialQ.toString());
		
		//This will be the list of trial names.
		String trialNames = "";
		
		//For each of the retrieved SQL records, add the trial name to the list.
		sql.eachRow(trialQ.toString(), {row ->
			
			//If we have multiple trial Names, make them comma delimited.
			if(trialNames.length()>0) trialNames+=",";
			
			//Get the trial name from the SQL record object.
			String tName = row.trial_name;
			
			//These are some hardcoded study names.
			if (tName.equalsIgnoreCase("BRC Antidepressant Study") ){
				tName ="BRC:mRNA:ADS";
			}
			if (tName.equalsIgnoreCase("BRC Depression Study")){
				tName = "BRC:mRNA:DS";
			}
			
			//Add the trial name to our string.
			trialNames +="'"+tName+"'";
		}
		);
		return trialNames;
	}
	
	def getSampleTypes (String concepts) {
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		def sampleTypesArray = [];
		StringBuilder sampleQ = new StringBuilder("SELECT distinct s.SAMPLE_TYPE_CD FROM de_subject_sample_mapping s WHERE s.CONCEPT_CODE IN ").append(convertStringToken(concepts));
		
		log.debug("getSampleTypes used this query: " + sampleQ.toString());
		
		sql.eachRow(sampleQ.toString(), {row->
			String st = row.sample_type_cd;
			if(st!=null && st.trim().length()>0){
				sampleTypesArray.add(st);
			}
		});
		String sampleTypes = convertList(sampleTypesArray, true, 100);
		return sampleTypes;
	}
	
	def String getAssayIds(String ids, String sampleTypes, String timepoint) {
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		StringBuilder assayS = new StringBuilder("select distinct s.assay_id  from de_subject_sample_mapping s where s.patient_id in (").append(ids).append(")");
		// check sample type
		if(sampleTypes!=null && sampleTypes.length()>0){
			assayS.append(" AND s.sample_type_cd IN ").append(convertStringToken(sampleTypes));
		}
		if(timepoint!=null && timepoint.trim().length()>0){
			assayS.append(" AND s.timepoint_cd IN ").append(convertStringToken(timepoint));
		}
		assayS.append (" ORDER BY s.assay_id");
		
		log.debug("getAssayIds used this query: " + assayS.toString());
		
		def assayIdsArray =[];
		sql.eachRow(assayS.toString(), {row->
			if(row.assay_id!=null){
				assayIdsArray.add(row.assay_id)
			}
		}
		);
		String assayIds = convertList(assayIdsArray, false, 1000);
		return assayIds;
	}
	
	def String getGenes (String pathwayName) {
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		// build pathway sub query and get gene symbol list
		//		StringBuilder pathwayS = new StringBuilder("(select distinct upper(gene_symbol) as gene_symbol from DE_pathway_gene c,de_pathway p where p.pathway_uid= '");
		//		pathwayS.append(pathwayName.replaceAll("'","''")).append("'");
		//		pathwayS.append(" and  p.id =c.pathway_id )");
		// gene sig or gene list
		StringBuilder pathwayS = new StringBuilder();
		if(pathwayName.startsWith("GENESIG")||pathwayName.startsWith("GENELIST")){
			pathwayS.append(" select  distinct bm.primary_external_id as gene_id from ")
					.append("search_keyword sk, ")
					.append(" search_bio_mkr_correl_fast_mv sbm,")
					.append(" bio_marker bm")
					.append(" where sk.bio_data_id = sbm.domain_object_id")
					.append(" and sbm.asso_bio_marker_id = bm.bio_marker_id")
					.append(" and sk.unique_id ='");
		}
		else {
			pathwayS.append(" select  distinct bm.primary_external_id as gene_id from ")
					.append("search_keyword sk, ")
					.append(" bio_marker_correl_mv sbm,")
					.append(" bio_marker bm")
					.append(" where sk.bio_data_id = sbm.bio_marker_id")
					.append(" and sbm.asso_bio_marker_id = bm.bio_marker_id")
					.append(" and sk.unique_id ='");
		}
		pathwayS.append(pathwayName.replaceAll("'","''")).append("'");
		
		log.debug("query to get genes from pathway: " + pathwayS.toString());
		
		def genesArray =[];
		sql.eachRow(pathwayS.toString(), {row->
			if(row.gene_id!=null){
				genesArray.add(row.gene_id);
			}
		}
		);
		
		String genes = convertList(genesArray, false, 1000);
		return genes;
	}
	
	def String createMRNAHeatmapQuery(String ids,
	String concepts, String pathwayName, String timepoint){
		
		String trialNames  = getTrialName( ids);
		String sampleTypes = getSampleTypes ( concepts);
		String assayIds    = getAssayIds(ids, sampleTypes, timepoint);
		
		StringBuilder s = new StringBuilder();
		s.append("SELECT a.PROBESET || a.GENE_SYMBOL as PROBESET,a.GENE_SYMBOL, ");
		s.append(" a.zscore as LOG2_INTENSITY, ");
		s.append(" a.patient_ID,a.ASSAY_ID,a.raw_intensity");
		s.append(" FROM de_subject_microarray_data a ");
		s.append(" WHERE a.trial_name IN (").append(trialNames).append(") ");
		s.append(" AND a.assay_id IN (").append(assayIds).append(")");
		// s.append(" order by a.patient_id, a.GENE_SYMBOL, a.PROBESET");
		
		log.debug("createMRNAHeatmapQuery generated this query: " + s.toString());
		return s.toString();
	}
	
	def String quoteCSV(String val) {
		String[] inArray;
		StringBuilder s = new StringBuilder();
		
		if (val != null && val.length() > 0) {
			inArray= val.split(",");
			s.append("'" +inArray[0] + "'");
			for (int i=1; i < inArray.length; i++) {
				s.append(",'" +inArray[i] + "'");
			}
		}
		return s.toString();
	}
	
	
	def String getSubjectIds1(String ids1, String ids2, String prefix1, String prefix2) {
		
		StringBuilder s = new StringBuilder();
		
		if (ids1 != null && ids1.length() > 0){			
			def idArray = ids1.split(",");
			for(id in idArray) s.append("'" + prefix1 + id + "' as " + prefix1 + id + ",");
		}
		
		if (ids2 != null && ids2.length() > 0){			
			def idArray = ids2.split(",");
			for(id in idArray) s.append("'" + prefix2 + id + "' as " + prefix2 + id + ",");
		}
		
		return s.substring(0, s.length()-1);
	}
	
	
	def String getSubjectIds(String ids1, String ids2, String prefix1, String prefix2, String separator) {
		String[] idArray1;
		String[] idArray2;
		
		if (ids1 != null && ids1.length() > 0)
			idArray1= ids1.split(",");
		if (ids2 != null && ids2.length() > 0)
			idArray2= ids2.split(",");
		
		StringBuilder s = new StringBuilder();
		if (ids1 != null && ids1.length() > 0 ) {
			s.append(prefix1 +idArray1[0]);
			for (int i=1; i < idArray1.length; i++) {
				s.append(separator + prefix1 + idArray1[i]);
			}
			if (ids2 != null && ids2.length() > 0 ) {
				for (int i=0; i < idArray2.length; i++) {
					s.append(separator + prefix2 + idArray2[i]);
				}
			}
		} else {
			if (ids2 != null && ids2.length() > 0 ) {
				s.append(prefix2 + idArray2[0]);
				for (int i=1; i < idArray2.length; i++) {
					s.append(separator + prefix2 + idArray2[i]);
				}
			}
		}
		return s.toString();
	}
	
	def String[] getSubjectIdArray(String ids1, String ids2, String prefix1, String prefix2) {
		Integer[] idArray1;
		Integer[] idArray2;
		
		if (ids1 != null && ids1.length() > 0) {
			String[] idStringArray = ids1.split(",");
			idArray1 = new Integer[idStringArray.length];
			for (Integer i = 0; i < idStringArray.length; i++) {
				Integer val = Integer.parseInt(idStringArray[i]);
				idArray1[i] = val;
			}
			idArray1.sort();
		}
		if (ids2 != null && ids2.length() > 0) {
			String[] idStringArray = ids2.split(",");
			idArray2 = new Integer[idStringArray.length];
			for (Integer i = 0; i < idStringArray.length; i++) {
				Integer val = Integer.parseInt(idStringArray[i]);
				idArray2[i] = val;
			}
			idArray2.sort();
		}
		String[] ids;
		
		if (idArray1 != null && idArray2 != null)
			ids = new String[idArray1.length + idArray2.length];
		else if (idArray1 != null)
			ids = new String[idArray1.length];
		else
			ids = new String[idArray2.length];
		
		if (ids1 != null && idArray1.length > 0 ) {
			for (int i=0; i < idArray1.length; i++) {
				ids[i] = prefix1 + idArray1[i].toString();
			}
			if (ids2 != null && idArray2.length > 0 ) {
				for (int i=0; i < idArray2.length; i++) {
					ids[idArray1.length + i] = prefix2 + idArray2[i].toString();
				}
			}
		} else {
			if (ids2 != null && ids2.length() > 0 ) {
				for (int i=0; i < idArray2.length; i++) {
					ids[i] = prefix2 + idArray2[i].toString();
				}
			}
		}
		return ids;
	}
	
	/* It is more meaningful to the scientists to use subject name such as S1_GSE19539_IC022 in the heatmap, and to be consistent with genomic data */
	String[] getSubjectNameArray(String ids1, String ids2, String prefix1, String prefix2) {
		List<String> nameList = null;
		if (ids1 != null && ids1.length() > 0) {
			List<String> nameList1 = getSubjectNameList(ids1, prefix1);
			if (nameList1 != null && nameList1.size() != 0)
				nameList = nameList1;
		}
		if (ids2 != null && ids2.length() > 0) {
			List<String> nameList2 = getSubjectNameList(ids2, prefix2);
			if (nameList2 != null && nameList2.size() != 0) {
				if (nameList != null && nameList.size() != 0) {
					for (String name : nameList2) {
						nameList.add(name);
					}
				}
				else {
					nameList = nameList2;
				}
			}
		}
		String[] ids = null;
		if (nameList != null && nameList.size() != 0) {
			ids = new String[nameList.size()];
			for (int i = 0; i < nameList.size(); i ++) {
				ids[i] = nameList.get(i);
			}
		}
		return ids;
	}
	
	List<String> getSubjectNameList(String ids, String prefix) {
		List<String> nameList = new ArrayList<String>();
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		String sqlStr = "SELECT sourcesystem_cd, patient_num FROM patient_dimension WHERE patient_num IN (" +
				ids + ") order by patient_num";
		
		sql.eachRow(sqlStr) { row ->
			String sourceSystemCd = row.sourcesystem_cd;
			Long patientNum = row.patient_num;
			if (sourceSystemCd != null && sourceSystemCd.length() != 0) {
				nameList.add(prefix + sourceSystemCd);
			}
			else {
				nameList.add(prefix + patientNum.toString());
			}
		}
		return nameList;
	}
	
	def String createRBMHeatmapQuery(String prefix, String ids, String concepts, String pathwayName, String timepoint, String rbmPanels) {
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		StringBuilder s = new StringBuilder();
		String genes;
		
		log.debug("Pathway: " + pathwayName)
		if (pathwayName != null && pathwayName.length() > 0 && "SHOWALLANALYTES".compareToIgnoreCase(pathwayName) != 0)	{
			genes = getGenes(pathwayName);
			log.debug("Genes obtained for given pathway: " + genes)
		}
		
		if (timepoint == null || timepoint.length() == 0 ) {
			s.append("SELECT distinct t1.ANTIGEN_NAME, t1.GENE_SYMBOL, t1.zscore as value, '");
			s.append(prefix + "'|| t1.patient_id as subject_id ");
			s.append("FROM DE_SUBJECT_RBM_DATA t1, de_subject_sample_mapping t2 ");
			s.append("WHERE t1.patient_id = t2.patient_id and t1.patient_id IN (" + ids + ")");
		} else {
			s.append("SELECT distinct t1.ANTIGEN_NAME, t1.GENE_SYMBOL, t1.zscore as value, '");
			s.append(prefix + "'|| t1.patient_id as subject_id ");
			s.append("FROM DE_SUBJECT_RBM_DATA t1, de_subject_sample_mapping t2 ");
			s.append("WHERE ")
			s.append("t2.patient_id IN ("+ ids + ") and ");
			s.append("t2.timepoint_cd IN (" + quoteCSV(timepoint) + ") and ");
			s.append("t1.data_uid = t2.data_uid and t1.assay_id=t2.assay_id");
		}
		
		if (rbmPanels != null && rbmPanels.length()>0){
			s.append(" and t2.rbm_panel IN (" + quoteCSV(rbmPanels) + ")");
		}
		
		if (pathwayName != null && pathwayName.length() > 0 && "SHOWALLANALYTES".compareToIgnoreCase(pathwayName) != 0)	{
			s.append(" AND t1.gene_id IN (").append(genes).append(")");
		}
		
		log.debug(s.toString());
		return s.toString();
	}
	
	
	def String createProteinHeatmapQuery(String prefix, String pathwayName,
	String ids, String concepts, String timepoint) {
		
		log.debug("createProteinHeatmapQuery called with concepts = " + concepts);
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		log.debug("createProteinHeatmapQuery created sql object");
		
		String cntQuery = "SELECT COUNT(*) as N FROM DE_SUBJECT_SAMPLE_MAPPING WHERE concept_code IN (" +
				quoteCSV(concepts) + ")";
		
		log.debug("createProteinHeatmapQuery created cntQuery = " + cntQuery);
		
		Integer cnt;
		
		log.debug("createProteinHeatmapQuery defined cnt = " + cntQuery);
		
		
		sql.query(cntQuery) { ResultSet rs ->
			while (rs.next()) cnt = rs.toRowResult().N;
		}
		
		log.debug("createProteinHeatmapQuery executed query to get count");
		
		log.debug("createProteinHeatmapQuery cnt=" + cnt);
		
		StringBuilder s = new StringBuilder();
		
		if (cnt == 0) {
			if (timepoint != null && timepoint.length() > 0 ) {
				s.append("SELECT distinct a.component, a.GENE_SYMBOL, a.zscore, '");
				s.append(prefix + "' || a.patient_ID as subject_id ");
				s.append("FROM DE_SUBJECT_PROTEIN_DATA a, DE_pathway_gene c, de_pathway p, ");
				s.append("DE_subject_sample_mapping b ");
				s.append("WHERE c.pathway_id= p.id and ");
				if (pathwayName != null) {
					s.append(" p.pathway_uid='" + pathwayName + "' and ");
				}
				s.append("a.gene_symbol = c.gene_symbol and ");
				s.append("a.patient_id IN (" + ids + ") and ");
				s.append("b.TIMEPOINT_CD IN (" + quoteCSV(timepoint) + ") and ");
				s.append("a.PATIENT_ID=b.patient_id and a.timepoint=b.timepoint and ");
				s.append("a.assay_id=b.assay_id  ");
			} else {
				s.append("SELECT distinct a.component, a.GENE_SYMBOL, a.zscore, '");
				s.append(prefix + "' || a.patient_ID as subject_id ");
				s.append("FROM DE_SUBJECT_PROTEIN_DATA a, DE_pathway_gene c, de_pathway p ");
				s.append("WHERE c.pathway_id= p.id and ");
				if (pathwayName != null) {
					s.append(" p.pathway_uid='" + pathwayName + "' and ");
				}
				s.append("a.gene_symbol = c.gene_symbol and ");
				s.append("a.patient_id IN (" + ids + ")");
			}
		} else {
			if (timepoint != null && timepoint.length() > 0 ) {
				s.append("select distinct a.component, a.GENE_SYMBOL, a.zscore, '");
				s.append(prefix + "' || a.patient_ID as subject_id ");
				s.append("FROM DE_SUBJECT_PROTEIN_DATA a, DE_pathway_gene c, de_pathway p, ");
				s.append("DE_subject_sample_mapping b ");
				s.append("WHERE c.pathway_id= p.id and ");
				if (pathwayName != null) {
					s.append(" p.pathway_uid='" + pathwayName + "' and ");
				}
				s.append("a.gene_symbol = c.gene_symbol and ");
				s.append("a.PATIENT_ID = b.PATIENT_ID and a.assay_id = b.assay_id and ");
				s.append("b.concept_code IN (" + quoteCSV(concepts) + ") and ");
				s.append("a.patient_id IN (" + ids + ") and ");
				s.append("b.TIMEPOINT_CD IN (" + quoteCSV(timepoint) + ") and ");
				s.append("a.PATIENT_ID=b.patient_id and a.timepoint=b.timepoint ");
			}  else {
				s.append("select distinct a.component, a.GENE_SYMBOL, a.zscore, '");
				s.append(prefix + "' || a.patient_ID as subject_id ");
				s.append("FROM DE_SUBJECT_PROTEIN_DATA a, DE_pathway_gene c, de_pathway p, ");
				s.append("DE_subject_sample_mapping b ");
				s.append("WHERE c.pathway_id= p.id and ");
				if (pathwayName != null) {
					s.append(" p.pathway_uid='" + pathwayName + "' and ");
				}
				s.append("a.gene_symbol = c.gene_symbol and ");
				s.append("a.PATIENT_ID = b.PATIENT_ID and a.assay_id = b.assay_id and ");
				s.append("b.concept_code IN (" + quoteCSV(concepts) + ") and ");
				s.append("a.patient_id IN (" + ids + ")");
			}
		}
		log.debug("createProteinHeatmapQuery complete:" + s.toString() );
		// log.debug(s.toString());
		return s.toString();
	}
	
	
	def String createProteinHeatmapQuery(String pathwayName,
	String ids1, String ids2,
	String concepts1, String concepts2,
	String timepoint1, String timepoint2) {
		
		log.debug("Protein: called with ids1=" + ids1 + " and ids2=" + ids2);
		
		String columns = listHeatmapColumns("component", ids1, ids2, "S1_", "S2_") + ", star"
		log.debug("Protein SELECT: " + columns)
		
		String s1;
		if (ids1 != null && ids1.length() > 0 ) {
			s1 = createProteinHeatmapQuery("S1_", pathwayName, ids1, concepts1, timepoint1);
		}
		String s2;
		if (ids2 != null && ids2.length() > 0 )
			s2 = createProteinHeatmapQuery("S2_", pathwayName, ids2, concepts2, timepoint2);
		//String subjects = "'" + getSubjectIds(ids1, ids2, "S1_", "S2_", "','") + "'";
		String subjects = getSubjectIds1(ids1, ids2, "S1_", "S2_") + ", '*' as star";
		log.debug("Protein Pivot: " + subjects)
		
		String r;
		if (s1 != null)
			if (s2 != null)
				r = "SELECT " + columns + " FROM (" +
						s1.replace("distinct ", " ") + " UNION " + s2.replace("distinct ", " ") +
						") PIVOT (avg(zscore) for subject_id IN (" + subjects +
						")) ORDER BY component, GENE_SYMBOL";
			else
				r = "SELECT " + columns + " FROM (" + s1 +
						") PIVOT (avg(zscore) for subject_id IN (" + subjects +
						")) ORDER BY component, GENE_SYMBOL";
		else
			r = "SELECT " + columns + " FROM (" + s2 +
					") PIVOT (avg(zscore) for subject_id IN (" + subjects +
					")) ORDER BY component, GENE_SYMBOL";
		return r;
		
	}
	
	
	def String createRBMHeatmapQuery(String pathwayName, String ids1, String ids2,
	String concepts1, String concepts2,
	String timepoint1, String timepoint2,
	String rbmPanels1, String rbmPanels2) {
		
		log.debug("RBM: called with ids1=" + ids1 + " and ids2=" + ids2);
		
		String columns = listHeatmapColumns("antigen_name", ids1, ids2, "S1_", "S2_") + ", star"
		log.debug("SELECT: " + columns)
		
		String s1;
		if (ids1 != null && ids1.length() > 0 )
			s1 = createRBMHeatmapQuery("S1_", ids1, concepts1, pathwayName, timepoint1, rbmPanels1);
		String s2;
		if (ids2 != null && ids2.length() > 0 )
			s2 = createRBMHeatmapQuery("S2_", ids2, concepts2, pathwayName, timepoint2, rbmPanels2);
		//String subjects = "'" + getSubjectIds(ids1, ids2, "S1_", "S2_", "','") + "'";
		String subjects = getSubjectIds1(ids1, ids2, "S1_", "S2_") + ", '*' as star";
		log.debug("RBM: " + subjects)
		
		String r;
		if (s1 != null)
			if (s2 != null)
				r = "SELECT " + columns + " FROM (" +
						s1.replace("distinct ", " ") + " UNION " + s2.replace("distinct ", " ") +
						") PIVOT (avg(value) for subject_id IN (" + subjects +
						")) ORDER BY ANTIGEN_NAME, GENE_SYMBOL";
			else
				r = "SELECT " + columns + " FROM (" + s1 +
						") PIVOT (avg(value) for subject_id IN (" + subjects +
						")) ORDER BY ANTIGEN_NAME, GENE_SYMBOL";
		else
			r = "SELECT " + columns + " FROM (" + s2 +
					") PIVOT (avg(value) for subject_id IN (" + subjects +
					")) ORDER BY ANTIGEN_NAME, GENE_SYMBOL";
		return r;
		
	}
	
	/**
	 * default log2 heatmap query
	 * @param pathwayName
	 * @param ids1
	 * @param ids2
	 * @param concepts1
	 * @param concepts2
	 * @param timepoint1
	 * @param timepoint2
	 * @return
	 */
	def String createMRNAHeatmapQuery(
	String pathwayName,
	String ids1,
	String ids2,
	String concepts1,
	String concepts2,
	String timepoint1,
	String timepoint2){
		
		return createMRNAHeatmapBaseQuery(pathwayName, ids1, ids2, concepts1, concepts2, timepoint1, timepoint2, "LOG2");
	}
	
	
	def String createMRNAHeatmapSelect(String ids1, String ids2, String prefix1, String prefix2){
		
		StringBuilder s = new StringBuilder();
		s.append(" probeset, gene_symbol ")
		
		if((ids1!= null) && (ids1.length()>0)){
			def idArray = ids1.split(",")
			for(id in idArray) s.append (", round(" + prefix1 + id + ", 4) as " + prefix1 + id)
		}
		
		if((ids2 != null) &&(ids2.length()>0)){
			def idArray = ids2.split(",")
			for(id in idArray) s.append (", round(" + prefix2 + id + ", 4) as " + prefix2 + id)
		}
		
		return s
	}
	
	/**
	 *  Compose a list of columns used by Heatmap and then trim average value
	 *  
	 * @param biomarker    probeset (mRNA), component (Protein) and antigen_name (RBM)
	 * @param ids1
	 * @param ids2
	 * @param prefix1   	usually use "S1_"
	 * @param prefix2		usually use "S2_"
	 * @return
	 */
	def String listHeatmapColumns(String biomarker, String ids1, String ids2, String prefix1, String prefix2){
		
		StringBuilder s = new StringBuilder();
		s.append(" " + biomarker + ", gene_symbol ")
		
		if((ids1!= null) && (ids1.length()>0)){
			def idArray = ids1.split(",")
			for(id in idArray) s.append (", round(" + prefix1 + id + ", 4) as " + prefix1 + id)
		}
		
		if((ids2 != null) &&(ids2.length()>0)){
			def idArray = ids2.split(",")
			for(id in idArray) s.append (", round(" + prefix2 + id + ", 4) as " + prefix2 + id)
		}
		
		return s
	}
	
	def String createMRNAHeatmapCountQuery(
	String pathwayName,
	String ids1,
	String ids2,
	String concepts1,
	String concepts2,
	String timepoint1,
	String timepoint2,
	String sample1,
	String sample2,
	String intensityType) throws Exception{
		return createMRNAHeatmapBaseQuery(
		pathwayName,
		ids1,
		ids2,
		concepts1,
		concepts2,
		timepoint1,
		timepoint2,
		sample1,
		sample2,
		intensityType,
		true
		);
	}
	
	/**
	 * heatmap query that takes intensity type
	 * @param pathwayName
	 * @param ids1
	 * @param ids2
	 * @param concepts1
	 * @param concepts2
	 * @param timepoint1
	 * @param timepoint2
	 * @param intensityType
	 * @return
	 */
	def String createMRNAHeatmapBaseQuery(
	String pathwayName,
	String ids1,
	String ids2,
	String concepts1,
	String concepts2,
	String timepoint1,
	String timepoint2,
	String sample1,
	String sample2,
	String intensityType) throws Exception{
		
		return createMRNAHeatmapBaseQuery(
		pathwayName,
		ids1,
		ids2,
		concepts1,
		concepts2,
		timepoint1,
		timepoint2,
		sample1,
		sample2,
		intensityType,
		false
		);
	}
	
	def String createMRNAHeatmapBaseQuery(
	String pathwayName,
	String ids1,
	String ids2,
	String concepts1,
	String concepts2,
	String timepoint1,
	String timepoint2,
	String sample1,
	String sample2,
	String intensityType,
	boolean count) throws Exception{
		log.debug("mRNA: called with ids1=" + ids1 + " and ids2=" + ids2);
		
		//String select = createMRNAHeatmapSelect(ids1, ids2, "S1_", "S2_") + ", star"
		String columns = null;
		
		if(count)
			columns = " COUNT(*) ";
		else
			columns = listHeatmapColumns("probeset", ids1, ids2, "S1_", "S2_") + ", star"
		//log.debug("SELECT: " + columns)
		
		String s1;
		if (ids1 != null && ids1.length() > 0 ){
			s1 = createMRNAHeatmapPathwayQuery("S1_",
					ids1, concepts1, pathwayName, timepoint1, sample1, intensityType);
		}
		String s2;
		if (ids2 != null && ids2.length() > 0 ){
			s2 = createMRNAHeatmapPathwayQuery("S2_",
					ids2, concepts2, pathwayName, timepoint2, sample2, intensityType);
		}
		
		// we have to use the log2_intensity to make the analysiscontroller happy..
		String intensityColumn = "LOG2_INTENSITY";
		//if("RAW".equals(intensityType)){
		//	intensityColumn="RAW_INTENSITY";
		//}
		
		//		String subjects = "'" + getSubjectIds(ids1, ids2, "S1_", "S2_", "','") + "'";
		//String subjects = "'" + getSubjectIds(ids1, ids2, "S1_", "S2_", "','") + "', '*'";
		String subjects = getSubjectIds1(ids1, ids2, "S1_", "S2_") + ", '*' as star";
		
		String r;
		if (s1 != null){
			if (s2 != null){
				
				r = "SELECT " + columns + " FROM (" +
						s1 + " UNION " + s2 +
						") PIVOT (avg("+intensityColumn+") for subject_id IN (" + subjects +
						")) ";
				if(!count) r = r+" ORDER BY PROBESET, GENE_SYMBOL";
			}
			else{
				r = "SELECT " + columns + " FROM (" + s1 +
						") PIVOT (avg("+intensityColumn+") for subject_id IN (" + subjects +
						"))";
				if(!count) r = r+" ORDER BY PROBESET, GENE_SYMBOL";
			}
		}else {
			r = "SELECT " + columns + " FROM (" + s2 +
					") PIVOT (avg("+intensityColumn+") for subject_id IN (" + subjects +
					"))";
			if(!count) r = r+" ORDER BY PROBESET, GENE_SYMBOL";
		}
		return r.toString();
	}
	
	
	def String createMRNAHeatmapPathwayQuery(String prefix, String ids, String concepts, String pathwayName, String timepoint, String sampleTypes, String intensityType) throws Exception{
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		//Get the list of trial names based on 
		String trialNames  = getTrialName(ids);
		String assayIds    = getAssayIds(ids, sampleTypes, timepoint);
		
		if (assayIds.equals(''))
			throw new Exception("No heatmap data for the specified parameters.");
		
		String genes;
		if (pathwayName != null && pathwayName.length() > 0)	{
			genes = getGenes(pathwayName);
		}
		
		String intensityCol = "zscore";
		if("RAW"==intensityType){
			intensityCol = "RAW_INTENSITY";
			
			//check if we have sufficient raw data to run gp query
			def goodPct
			String rawCountQuery="select DISTINCT /*+ parallel(de_subject_microarray_data,4) */ /*+ parallel(de_mrna_annotation,4) */ count(distinct a.raw_intensity)/count(*) as pct_good " +
					"FROM de_subject_microarray_data a, de_mrna_annotation b " +
					"WHERE a.probeset_id = b.probeset_id AND a.trial_name IN ("+trialNames+") " +
					"AND a.assay_id IN ("+assayIds+")";
					
			sql.eachRow (rawCountQuery, {row-> goodPct=row[0]})
			
			if(goodPct==0) throw new Exception("No raw data for Comparative Marker Selection.");
		}
		
		// select different fields in the version for pivoting
		// StringBuilder s = new StringBuilder("select a.PROBESET, a.GENE_SYMBOL, a.zscore as LOG2_INTENSITY, a.patient_ID, a.ASSAY_ID, a.raw_intensity");
		
		// added hint here...
		StringBuilder s = new StringBuilder();
		s.append("select DISTINCT /*+ parallel(de_subject_microarray_data,4) */ /*+ parallel(de_mrna_annotation,4) */  b.PROBE_ID || ':' || b.GENE_SYMBOL as PROBESET, b.GENE_SYMBOL, "+"a."+intensityCol+" as LOG2_INTENSITY ");
		s.append(" , '").append(prefix).append("' || a.patient_ID as subject_id ");
		s.append(" FROM de_subject_microarray_data a, de_mrna_annotation b ");
		s.append(" WHERE a.probeset_id = b.probeset_id AND a.trial_name IN (").append(trialNames).append(") ");
		s.append(" AND a.assay_id IN (").append(assayIds).append(")");
		
		if (pathwayName != null && pathwayName.length() > 0)	{
			s.append(" AND b.gene_id IN (").append(genes).append(")");
		}		
		log.debug(s.toString());
		return s.toString();
	}
	
	
	/**
	 *
	 */
	def convertStringToken(String t) {
		String[] ts = t.split(",");
		StringBuilder s = new StringBuilder("(");
		for(int i=0; i<ts.length;i++){
			if(i>0)
				s.append(",");
			s.append("'");
			s.append(ts[i]);
			s.append("'");
		}
		s.append(")");
		return s.toString();
	}
	
	/**
	 * convert id list
	 */
	def convertList(idList, boolean isString, int max) {
		StringBuilder s = new StringBuilder();
		int i = 0;
		for(id in idList){
			if(i<max){
				if(s.length()>0){
					s.append(",");
				}
				if(isString){
					s.append("'");
				}
				s.append(id);
				if(isString){
					s.append("'");
				}
			}else{
				break;
			}
			i++;
		}
		return s.toString();
	}
	/**
	 * Gets the distinct patient counts for the children of a parent concept key
	 */
	def getChildrenWithAccessForUser(String concept_key, AuthUser user) {
		def List<String> children=getChildPathsFromParentKey(concept_key)
		def access = [:]
		def path=keyToPath(concept_key);
		
		//1)put all the children into the access list with default unlocked
		for(e in children)
		{
			access.put(e.toString(), 'Unlocked');
		}
		
		
		//2)if we are at the root level then check the security
		def level=getLevelFromKey(concept_key);
		def admin=false;
		for (role in user.authorities){
			if (isAdminRole(role)) {admin=true;
				log.trace("ADMINISTRATOR, SKIPPING PERMISSION CHECKING");
			}
		}
		if(level==-1 && !admin) //only check on first level of nodes and im not an admin
		{
			log.trace("NOT AN ADMINISTRATOR CHECKING PERMISSIONS")
			//3) get the secure paths that are children of this path and lock them if they are in the children
			StringBuilder s2=new StringBuilder();
			s2.append("SELECT DISTINCT s FROM SecureObjectPath s WHERE s.conceptPath LIKE'").append(path).append("%'")
			
			def results2=SecureObjectPath.executeQuery(s2.toString());
			log.trace("***********************");
			for (row in results2)
			{
				log.trace(row[0]);
				def securePath=row.conceptPath;
				log.trace("FOUND SECUREPATH:"+securePath);
				if(access.containsKey(securePath)) {
					access[securePath]='Locked';
					log.trace("LOCKING SECURE PATH:"+securePath);
				}
			}
			log.trace("***********************")
			//4) get the access levels this user has and unlock the locked resources available to him
			StringBuilder s = new StringBuilder();
			s.append("SELECT DISTINCT ausa.accessLevel, sop.conceptPath FROM AuthUserSecureAccess ausa JOIN ausa.accessLevel JOIN ausa.authUser au JOIN ausa.secureObject.conceptPaths sop ");
			s.append(" WHERE sop.conceptPath LIKE '").append(path).append("%'");
			s.append (" AND au.id = ").append(user.id);
			
			//return access levels for the children of this path that have them
			def results = AuthUserSecureAccess.executeQuery(s.toString());
			//for each of the ones that were found with access put their access levels into the object
			//    log.trace("***********************")
			for (row in results){
				def accessLevel = row[0];
				def accessPath = row[1];
				log.trace("path: " + accessPath + " accessLevel: "+accessLevel.accessLevelName);
				
				if(access.containsKey(accessPath)) {
					access[accessPath]=accessLevel.accessLevelName;
					log.trace("GRANTING ACCESS TO:"+accessPath);
				}
			}
			//	log.trace("***********************")
		}
		return access;
	}
	
	
	/**
	 * Gets the children value type concepts of a parent key
	 */
	def List<String> getChildPathsFromParentKey(String concept_key) {
		String prefix=concept_key.substring(0, concept_key.indexOf("\\",2)); //get the prefix to put on to the fullname to make a key
		String fullname=concept_key.substring(concept_key.indexOf("\\",2), concept_key.length());
		
		String xml;
		ArrayList ls=new ArrayList();
		int i=getLevelFromKey(concept_key)+1;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlt = "SELECT C_FULLNAME FROM i2b2metadata.i2b2 WHERE C_FULLNAME LIKE ? AND c_hlevel = ? ORDER BY C_FULLNAME";
		sql.eachRow(sqlt, [fullname+"%", i], {row ->
			String conceptkey=prefix+row.c_fullname;
			ls.add(keyToPath(conceptkey));
		})
		return ls;
	}
	
	/**
	 * Minimal conversion of metadataxml to JSON format
	 */
	def metadataxmlToJSON(String xml) {
		
		def oktousevalues=false;
		def normalunits="";
		if(xml!=null && !xml.equalsIgnoreCase("")) {
			log.trace(xml)
			try {
				DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
				domFactory.setNamespaceAware(true); // never forget this!
				DocumentBuilder builder = domFactory.newDocumentBuilder();
				Document doc = builder.parse(new InputSource(new StringReader(xml)));
				
				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();
				//xpath.setNamespaceContext(new QueryNamespaceContext());
				//XPathExpression expr  = xpath.compile("item");
				Object result=xpath.evaluate("//ValueMetadata/Oktousevalues", doc, XPathConstants.NODE);
				//Object result = expr.evaluate(doc, XPathConstants.NODESET);
				//NodeList nodes = (NodeList) result;
				Node x=(Node) result;
				String key=x.getTextContent();
				//   logMessage("Found oktousevalues: "+key);
				if(key.equalsIgnoreCase("Y")){oktousevalues=true;
				}
				
				normalunits=((Node)xpath.evaluate("//ValueMetadata/UnitValues/NormalUnits", doc, XPathConstants.NODE)).getTextContent();
				//	    log.debug("normalunits": normalunits)
			}
			catch(ex){log.error("BAD METADATAXML FOUND")
			}
		}
		return [oktousevalues: oktousevalues, normalunits: normalunits]
	}
	
	
	
	/**
	 * Gets the access level for a list of concept keys
	 */
	def getConceptPathAccessForUser(List<String> paths, AuthUser user) {
		def access = [:]
		
		//1)put all the children into the access list with default unlocked
		for(e in paths)
		{
			access.put(e.toString(), 'Unlocked')
		}
		
		
		//2)if we are not an admin
		def admin=false;
		for (role in user.authorities){
			if (isAdminRole(role)) {admin=true;
				log.trace("ADMINISTRATOR, SKIPPING PERMISSION CHECKING")
			}
		}
		
		if(!admin) //level of nodes and im not an admin
		{
			log.trace("NOT AN ADMINISTRATOR CHECKING PERMISSIONS");
			//3) get the secure paths that are in the list and secure them for later unlocking if necessary
			StringBuilder s2=new StringBuilder();
			s2.append("SELECT DISTINCT s FROM SecureObjectPath s WHERE s.conceptPath IN (:ids )");
			
			def results2=SecureObjectPath.executeQuery(s2.toString(), ['ids':paths]);
			log.trace("***********************");
			for (row in results2)
			{
				log.trace(row[0]);
				def securePath=row.conceptPath;
				log.trace("FOUND SECUREPATH:"+securePath);
				if(access.containsKey(securePath)) {
					access[securePath]='Locked';
					log.trace("LOCKING SECURE PATH:"+securePath);
				}
			}
			log.trace("***********************");
			//4) get the access levels this user has and unlock the locked resources available to him
			StringBuilder s = new StringBuilder();
			s.append("SELECT DISTINCT ausa.accessLevel, sop.conceptPath FROM AuthUserSecureAccess ausa JOIN ausa.accessLevel JOIN ausa.authUser au JOIN ausa.secureObject.conceptPaths sop ");
			s.append(" WHERE sop.conceptPath IN (:ids) ");
			s.append (" AND au.id = ").append(user.id);
			
			//return access levels for the children of this path that have them
			def results = AuthUserSecureAccess.executeQuery(s.toString(), ['ids':paths] )
			//for each of the ones that were found with access put their access levels into the object
			log.debug("***********************");
			for (row in results){
				def accessLevel = row[0];
				def accessPath = row[1];
				log.trace("path: "+accessPath+" accessLevel: "+accessLevel.accessLevelName);
				if(access.containsKey(accessPath)) {
					access[accessPath]=accessLevel.accessLevelName;
					log.trace("GRANTING ACCESS TO:"+accessPath);
				}
			}
			log.trace("***********************");
		}
		return access;
	}
	
	
	def  getGenesForHaploviewFromResultInstanceId(resultInstanceId) {
		log.debug("getting genes for happloview");
		def genes=[];
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlt = "select distinct gene from haploview_data a inner join qt_patient_set_collection b on a.I2B2_ID=b.patient_num where result_instance_id = ? order by gene asc"
		//String sqlt = "select distinct patient_num from qt_patient_set_collection where result_instance_id="+resultInstanceId
		sql.eachRow(sqlt, [resultInstanceId], {row ->
			log.trace("IN ROW ITERATOR");
			log.trace("Found:"+row.gene);
			genes.add(row.gene);
			log.trace(row.gene);
		})
		return genes;
	}
	
	/**
	 * Gets the access level for a list of concept keys
	 */
	def getConceptPathAccessCascadeForUser(List<String> paths, AuthUser user) {
		def access = [:];
		
		//1)put all the children into the access list with default unlocked
		for(e in paths)
		{
			access.put(e.toString(), 'Unlocked');
		}
		
		
		//2)if we are not an admin
		def admin=false;
		for (role in user.authorities){
			if (isAdminRole(role)) {admin=true;
				log.trace("ADMINISTRATOR, SKIPPING PERMISSION CHECKING");
			}
		}
		
		if(!admin) //level of nodes and im not an admin
		{
			log.trace("NOT AN ADMINISTRATOR CHECKING PERMISSIONS");
			//3) get the secure paths that are in the list and secure them for later unlocking if necessary
			StringBuilder s2=new StringBuilder();
			s2.append("SELECT DISTINCT s FROM SecureObjectPath s");
			
			def results2=SecureObjectPath.executeQuery(s2.toString())
			log.trace("***********************")
			for (row in results2)
			{
				log.trace(row[0]);
				def securePath=row.conceptPath;
				log.trace("FOUND SECUREPATH:"+securePath);
				setChildrenAccess(access, securePath, "Locked");
			}
			log.trace("***********************");
			//4) get the access levels this user has and unlock the locked resources available to him
			StringBuilder s = new StringBuilder();
			s.append("SELECT DISTINCT ausa.accessLevel, sop.conceptPath FROM AuthUserSecureAccess ausa JOIN ausa.accessLevel JOIN ausa.secureObject.conceptPaths sop ");
			s.append(" WHERE  au.authUser is NULL or au.authUser.id = ").append(user.id).append(" ORDER BY sop.conceptPath")
			
			//return access levels for the children of this path that have them
			def results = AuthUserSecureAccess.executeQuery(s.toString() )
			//for each of the ones that were found with access put their access levels into the object
			log.trace("***********************")
			for (row in results){
				def accessLevel = row[0];
				def accessPath = row[1];
				setChildrenAccess(access, accessPath, accessLevel.accessLevelName);
			}
			log.trace("***********************");
		}
		return access;
	}
	
	def setChildrenAccess(map, path, access) {
		def es=map.entrySet()
		es.each{
			if(it.key.indexOf(path)==0)
				it.value = access;
			log.trace("Setting key: "+it.key +" set to value: "+access);
		}
	}
	
	/**
	 *  Gets the data associated with a value type concept from observation fact table
	 * for display in a distribution histogram for a given subset
	 */
	def getConceptDistributionDataForValueConceptByTrial(String concept_key, String result_instance_id) {
		def trialdata=[:];
		
		if(result_instance_id!=null && result_instance_id!="") {
			log.trace("Getting concept distribution data for value concept:"+concept_key);
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
			String concept_cd=getConceptCodeFromKey(concept_key);
			//ArrayList<Double> values=new ArrayList<Double>();
			String sqlt="""SELECT TRIAL, NVAL_NUM FROM OBSERVATION_FACT f  INNER JOIN PATIENT_TRIAL t
			    ON f.PATIENT_NUM=t.PATIENT_NUM WHERE CONCEPT_CD = ? AND
			    f.PATIENT_NUM IN (select distinct patient_num from qt_patient_set_collection
				where result_instance_id = ?)""";
			sql.eachRow(sqlt, [
				concept_cd,
				result_instance_id
			], {row ->
				if(row.NVAL_NUM!=null) {
					//add a new Array if this is the first time im hitting this trial
					if(!trialdata.containsKey(row.TRIAL))
					{
						trialdata.put(row.TRIAL, [row.NVAL_NUM]);
					}
					else {
						trialdata[row.Trial].add(row.NVAL_NUM);
					}
				}
			})
		}
		return trialdata;
	}
	
	def getConceptDistributionDataForValueConceptByTrialByConcepts(Set<String> childConcepts, String result_instance_id) {
		def trialdata=[:];
		
		if(result_instance_id!=null && result_instance_id!="" && !childConcepts.isEmpty()) {
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
			// String concept_cd=getConceptCodeFromKey(concept_key);
			//ArrayList<Double> values=new ArrayList<Double>();
			
			// IN clause here
			
			String sqlt="""SELECT TRIAL, NVAL_NUM FROM OBSERVATION_FACT f  INNER JOIN PATIENT_TRIAL t
			ON f.PATIENT_NUM=t.PATIENT_NUM
			WHERE CONCEPT_CD IN ("""+listToIN(childConcepts.asList())+""") AND
			f.PATIENT_NUM IN (select distinct patient_num
					from qt_patient_set_collection
					where result_instance_id="""+result_instance_id+""") """;
			
			log.debug("about to execute query: "+sqlt);
			
			sql.eachRow(sqlt,
					{row ->
						if(row.NVAL_NUM!=null) {
							//add a new Array if this is the first time im hitting this trial
							if(!trialdata.containsKey(row.TRIAL))
							{
								trialdata.put(row.TRIAL, [row.NVAL_NUM]);
							}
							else {
								trialdata[row.Trial].add(row.NVAL_NUM);
							}
						}
					})
		}
		return trialdata;
	}
	
	/****************************************New security stuff*************************/
	
	/**
	 *  check whether or not a role is admin
	 *
	 *
	 *  */
	def isAdminRole(role){
		return role.authority.equals("ROLE_ADMIN") || role.authority.equals("ROLE_DATASET_EXPLORER_ADMIN");
	}
	/**
	 *  check whether or not a user is admin
	 */
	def isAdmin(user){
		def admin=false;
		for (role in user.authorities){
			if (isAdminRole(role)) {admin=true;
			}
		}
		return admin;
	}
	
	/**
	 * Gets the children paths concepts of a parent key
	 */
	def  getChildPathsWithTokensFromParentKey(String concept_key) {
		String prefix=concept_key.substring(0, concept_key.indexOf("\\",2)); //get the prefix to put on to the fullname to make a key
		String fullname=concept_key.substring(concept_key.indexOf("\\",2), concept_key.length());
		
		String xml;
		def ls=[:];
		int i=getLevelFromKey(concept_key)+1;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		String sqlt = "SELECT C_FULLNAME, SECURE_OBJ_TOKEN FROM i2b2metadata.i2b2_SECURE WHERE C_FULLNAME LIKE ? AND c_hlevel = ? ORDER BY C_FULLNAME";
		sql.eachRow(sqlt, [fullname+"%", i], {row ->
			String conceptkey=prefix+row.c_fullname;
			ls.put(keyToPath(conceptkey), row.secure_obj_token);
			log.trace("@@found"+conceptkey);
		})
		return ls;
	}
	
	def getSecureTokensWithAccessForUser(user) {
		StringBuilder s = new StringBuilder();
		s.append("SELECT DISTINCT ausa.accessLevel, so.bioDataUniqueId FROM AuthUserSecureAccess ausa JOIN ausa.accessLevel JOIN ausa.secureObject so ")
		s.append(" WHERE ausa.authUser IS NULL OR ausa.authUser.id = ").append(user.id)
		def t=[:];
		//return access levels for the children of this path that have them
		def results = AuthUserSecureAccess.executeQuery(s.toString());
		for (row in results){
			def token = row[1];
			def accessLevel = row[0];
			log.trace(token+":"+accessLevel.accessLevelName);
			t.put(token,accessLevel.accessLevelName);
		}
		t.put("EXP:PUBLIC","OWN");
		return t;
	}
	
	
	/**
	 * Gets the children with access for a concept
	 */
	def getChildrenWithAccessForUserNew(String concept_key, AuthUser user) {
		def children=getChildPathsWithTokensFromParentKey(concept_key);
		return getAccess(children, user);
	}
	
	/**
	 * Checks an arbitrary list of paths with tokens against users access list map (merge)
	 */
	def getAccess (pathswithtokens, user) {
		def children=pathswithtokens;
		def access=[:]; //new map to merge the other two
		
		//def level=getLevelFromKey(concept_key);
		def admin=false;
		for (role in user.authorities)
		{
			if (isAdminRole(role)) {admin=true;
				log.trace("ADMINISTRATOR, SKIPPING PERMISSION CHECKING")
				//1)If we are an admin then grant admin to all the paths
				for(key in children.keySet())
				{
					access.put(key, 'Admin');
					log.trace("putting "+key+" with admin access");
				}
				return access; //just set everything to admin and return it all
			}
		}
		if(!admin) //if not admin merge the data from the two maps
		{
			def tokens=getSecureTokensWithAccessForUser(user);
			for(key in children.keySet()) {
				def childtoken=children[key];
				log.trace("Key:"+key+" Token:"+childtoken.toString());
				if(childtoken==null) {
					access.put(key,"VIEW"); //give read access if no security token
				}
				else if(tokens.containsKey(childtoken)) //null tokens are assumed to be unlocked
				{
					access.put(key, tokens[childtoken]); //found access for this token so put in access level
				}
				else {
					access.put(key, "Locked"); //didn't find authorization for this token
				}
			}
		}
		log.debug(access.toString());
		return access;
	}
	
	/**
	 * renderQueryDefinition provides an XML based string given a result instance ID
	 * 
	 * @param resultInstanceId - the result instance ID
	 * @param title - the title for the query (e.g. subset 2)
	 * @param pw - the StringWriter used to build the XML string
	 * 
	 * @return an XML String
	 */	
    def renderQueryDefinition(String resultInstanceId, String title, Writer pw) {
		if (log.isDebugEnabled())	{
			log.debug("renderQueryDefinition called with ${resultInstanceId} and ${title}")
		}
		if (resultInstanceId != null) {
	 	    try {
				String xmlrequest = getQueryDefinitionXML(resultInstanceId)
				if(log.isDebugEnabled())	{
					log.debug("${xmlrequest}")
				}	   
				DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance()
			    domFactory.setNamespaceAware(true) // mandatory!
			    DocumentBuilder builder = domFactory.newDocumentBuilder()
			    Document doc = builder.parse(new InputSource(new StringReader(xmlrequest)))
			   
			    XPathFactory factory = XPathFactory.newInstance()
			    XPath xpath = factory.newXPath()
	
				Object result = xpath.evaluate("//panel", doc, XPathConstants.NODESET)
	   		    NodeList panels = (NodeList) result
			    Node panel = null
	
				pw.write("<table class='analysis'>")
			    pw.write("<tr><th>${title}</th></tr>")
			    pw.write("<tr>")
			    pw.write("<td>")
				log.debug("Interating over the nodes...")
			    for (int p = 0; p < panels.getLength(); p++) {
					panel=panels.item(p)
				    Node panelnumber=(Node)xpath.evaluate("panel_number", panel, XPathConstants.NODE)
				    
					if(panelnumber?.getTextContent()?.equalsIgnoreCase("21")) {
                        log.debug("Skipping the security panel in printing the output")
						continue
					}
				    
					if(p!=0 && p!=(panels.getLength()))	{
						pw.write("<br><b>AND</b><br>")
				    }
				    
					Node invert=(Node)xpath.evaluate("invert", panel, XPathConstants.NODE)
				    if(invert?.getTextContent()?.equalsIgnoreCase("1")) {
					    pw.write("<br><b>NOT</b><br>")
	  			    } 
				   
					NodeList items=(NodeList)xpath.evaluate("item", panel, XPathConstants.NODESET)
				    pw.write("<b>(</b>")
				    
					for(int i=0; i<items.getLength(); i++) {
					   Node item=items.item(i);
					   if(i!=0 && i!=(items.getLength()))	{
						   pw.write("<br><b>OR</b><br>")
					   }
	
					   Node key=(Node)xpath.evaluate("item_key", item, XPathConstants.NODE)
					   Node valueinfo=(Node)xpath.evaluate("constrain_by_value", item, XPathConstants.NODE)
					   String operator="";
					   String constraints="";
					   
					   if(valueinfo!=null) {
						   operator=((Node)xpath.evaluate("value_operator", valueinfo, XPathConstants.NODE)).getTextContent()
						   constraints=((Node)xpath.evaluate("value_constraint", valueinfo, XPathConstants.NODE)).getTextContent()
					   }
					   String textContent = key.getTextContent()
					   log.debug("Found item ${textContent}")				   
					   pw.write(textContent+" "+operator+" "+constraints)				   
				   }
				   pw.write("<b>)</b>")
			   }
			   pw.write("</td></tr></table>")
			} catch (Exception e) {
			   log.error(e)
		   	}
		}
    }   
	
	def getSecureTokensCommaSeparated(user) {
		def tokenmap=getSecureTokensWithAccessForUser(user);
		log.trace("*********************GOT TO SB******************")
		StringBuilder sb=new StringBuilder();
		for (v in tokenmap.keySet()) //have some kind of access to each of these tokens
		{
			log.trace(v);
			sb.append("'");
			sb.append(v);
			sb.append("',");
		}
		if(sb.length()>0) {
			sb.deleteCharAt(sb.length() - 1);//remove last comma
		}
		return sb.toString();
	}
	
	/**
	 * Gets the children paths concepts of a parent key
	 */
	def  getRootPathsWithTokens() {
		//String fullname=concept_key.substring(concept_key.indexOf("\\",2), concept_key.length());
		def rootlevel=-1;
		String xml;
		def ls=[:];
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
 		String mlevelsql = "SELECT MIN(C_HLEVEL) AS mlevel FROM i2b2metadata.i2b2_SECURE";	
 		sql.eachRow(mlevelsql, {row ->	
 		rootlevel=row.mlevel;	
     		})
		//int i=getLevelFromKey(concept_key)+1;
	
		String sqlt = "SELECT C_FULLNAME, SECURE_OBJ_TOKEN FROM i2b2metadata.i2b2_SECURE WHERE c_hlevel = ? ORDER BY C_FULLNAME";
		sql.eachRow(sqlt, [rootlevel], {row ->
			String fullname=row.c_fullname;
			String prefix=fullname.substring(0, fullname.indexOf("\\",2)); //get the prefix to put on to the fullname to make a key
			String conceptkey=prefix+fullname;
			ls.put(keyToPath(conceptkey), row.secure_obj_token);
			log.trace("@@found"+conceptkey);
		})
		return ls;
	}
	
	
	def listToIN(List<String> list) {
		StringBuilder sb=new StringBuilder();
		// need to make it less than 1000! -- temp solution
		int i = 0;
		for(c in list)
		{
			//If the only thing submitted was "ALL" we return an empty string just like there was nothinbg in the box.
			if(c.toString()=="ALL" && list.size()==1)
			{
				break;
			}
			
			sb.append("'");
			sb.append(c.toString().replace("'","''"));
			sb.append("'");
			sb.append(",");
			i++;
			if(i>=1000){
				break;
			}
		}
		if(sb.length()>0) {
			sb.deleteCharAt(sb.length() - 1);//remove last comma
		}
		return sb.toString();
	}
	
	
	/**
	 * Gets the platforms found
	 * For now, subids could be null due to complexity of workflow and user error
	 */
	def fillHeatmapValidator(subids, conids, hv) {
				
		//If the list of subids does not have any elements, or it has only one element which is "ALL"
		if (subids == null || subids.size == 0 || (subids.size == 1 && subids[0] == "ALL"))
			subids = null;
		log.trace("validating heatmap:")
		log.trace(conids)
		log.trace(subids)
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		String sqlt = "SELECT TISSUE_TYPE, TISSUE_TYPE_CD, PLATFORM, TIMEPOINT, TIMEPOINT_CD, SAMPLE_TYPE_CD, SAMPLE_TYPE FROM DE_SUBJECT_SAMPLE_MAPPING WHERE "
		if (subids != null)
			sqlt += "PATIENT_ID IN (" + listToIN(subids) + ") AND ";
		sqlt += "SAMPLE_TYPE_CD IN (" + listToIN(conids) + ") GROUP BY PLATFORM, TIMEPOINT, TIMEPOINT_CD, SAMPLE_TYPE_CD, SAMPLE_TYPE, TISSUE_TYPE, TISSUE_TYPE_CD";
		sql.eachRow(sqlt, {row ->
			if(row.PLATFORM != null){hv.platforms.add(row.PLATFORM)
			} ;
			if((row.PLATFORM!=null) && (row.TIMEPOINT_CD!=null) && !(row.PLATFORM.equals('RBM') && row.TIMEPOINT_CD.indexOf(':Z:')==-1)){
				if(row.TIMEPOINT_CD != null){hv.timepoints.add(row.TIMEPOINT_CD)
				} ;
				if(row.TIMEPOINT != null){hv.timepointLabels.add(row.TIMEPOINT)
				};
			}
			if(row.SAMPLE_TYPE_CD != null){ hv.samples.add(row.SAMPLE_TYPE_CD)
			} ;
			if(row.SAMPLE_TYPE != null){ hv.sampleLabels.add(row.SAMPLE_TYPE)
			} ;
			if(row.TISSUE_TYPE_CD != null){ hv.tissues.add(row.TISSUE_TYPE_CD)
			} ;
			if(row.TISSUE_TYPE != null){ hv.tissueLabels.add(row.TISSUE_TYPE)
			} ;
		})
		if(hv.validate()){return;
		}
		
		sqlt = "SELECT TISSUE_TYPE, TISSUE_TYPE_CD, PLATFORM, TIMEPOINT, TIMEPOINT_CD, SAMPLE_TYPE_CD, SAMPLE_TYPE FROM DE_SUBJECT_SAMPLE_MAPPING WHERE "
		if (subids != null)
			sqlt += "PATIENT_ID IN (" + listToIN(subids) + ") AND ";
		sqlt += "TIMEPOINT_CD IN (" + listToIN(conids) + ") GROUP BY PLATFORM, TIMEPOINT, TIMEPOINT_CD, SAMPLE_TYPE_CD, SAMPLE_TYPE, TISSUE_TYPE, TISSUE_TYPE_CD";
		sql.eachRow(sqlt, {row ->
			if(row.PLATFORM != null){hv.platforms.add(row.PLATFORM)
			} ;
			if((row.PLATFORM!=null) && (row.TIMEPOINT_CD!=null) && !(row.PLATFORM.equals('RBM') && row.TIMEPOINT_CD.indexOf(':Z:')==-1)){
				if(row.TIMEPOINT_CD != null){hv.timepoints.add(row.TIMEPOINT_CD)
				} ;
				if(row.TIMEPOINT != null){hv.timepointLabels.add(row.TIMEPOINT)
				};
			}
			if(row.SAMPLE_TYPE_CD != null){ hv.samples.add(row.SAMPLE_TYPE_CD)
			} ;
			if(row.SAMPLE_TYPE != null){ hv.sampleLabels.add(row.SAMPLE_TYPE)
			} ;
			if(row.TISSUE_TYPE_CD != null){ hv.tissues.add(row.TISSUE_TYPE_CD)
			} ;
			if(row.TISSUE_TYPE != null){ hv.tissueLabels.add(row.TISSUE_TYPE)
			} ;
		})
		if(hv.validate()){return;
		}
		
		sqlt = "SELECT TISSUE_TYPE, TISSUE_TYPE_CD, PLATFORM, TIMEPOINT, TIMEPOINT_CD, SAMPLE_TYPE_CD, SAMPLE_TYPE FROM DE_SUBJECT_SAMPLE_MAPPING WHERE ";
		if (subids != null)
			sqlt += "PATIENT_ID IN (" + listToIN(subids) + ") AND ";
		sqlt += "CONCEPT_CODE IN (" + listToIN(conids) + ") GROUP BY PLATFORM, TIMEPOINT, TIMEPOINT_CD, SAMPLE_TYPE_CD, SAMPLE_TYPE, TISSUE_TYPE, TISSUE_TYPE_CD";
		log.trace(sqlt);
		sql.eachRow(sqlt, {row ->
			if(row.PLATFORM != null)
			{
				hv.platforms.add(row.PLATFORM)
			} ;
			if((row.PLATFORM!=null) && (row.TIMEPOINT_CD!=null) && !(row.PLATFORM.equals('RBM') && row.TIMEPOINT_CD.indexOf(':Z:')==-1))
			{
				if(row.TIMEPOINT_CD != null)
				{
					hv.timepoints.add(row.TIMEPOINT_CD)
				} ;
				if(row.TIMEPOINT != null)
				{
					hv.timepointLabels.add(row.TIMEPOINT)
				};
			}
			if(row.SAMPLE_TYPE_CD != null){ hv.samples.add(row.SAMPLE_TYPE_CD)
			} ;
			if(row.SAMPLE_TYPE != null){ hv.sampleLabels.add(row.SAMPLE_TYPE)
			} ;
			if(row.TISSUE_TYPE_CD != null){ hv.tissues.add(row.TISSUE_TYPE_CD)
			} ;
			if(row.TISSUE_TYPE != null){ hv.tissueLabels.add(row.TISSUE_TYPE)
			} ;
		})
		if(hv.validate()){return;
		}
		
		def timepoints=[];
		sql = new groovy.sql.Sql(dataSource)
		sqlt = "SELECT TISSUE_TYPE, TISSUE_TYPE_CD, PLATFORM, TIMEPOINT, TIMEPOINT_CD, SAMPLE_TYPE_CD, SAMPLE_TYPE FROM DE_SUBJECT_SAMPLE_MAPPING WHERE ";
		if (subids != null)
			sqlt += "PATIENT_ID IN ("+listToIN(subids)+") AND ";
		sqlt += "PLATFORM_CD IN (" + listToIN(conids) + ") GROUP BY PLATFORM, TIMEPOINT, TIMEPOINT_CD, SAMPLE_TYPE_CD, SAMPLE_TYPE, TISSUE_TYPE, TISSUE_TYPE_CD";
		sql.eachRow(sqlt, {row ->
			if(row.PLATFORM != null){hv.platforms.add(row.PLATFORM)
			} ;
			if((row.PLATFORM!=null) && (row.TIMEPOINT_CD!=null) && !(row.PLATFORM.equals('RBM') && row.TIMEPOINT_CD.indexOf(':Z:')==-1)){
				if(row.TIMEPOINT_CD != null){hv.timepoints.add(row.TIMEPOINT_CD)
				} ;
				if(row.TIMEPOINT != null){hv.timepointLabels.add(row.TIMEPOINT)
				};
			}
			if(row.SAMPLE_TYPE_CD != null){ hv.samples.add(row.SAMPLE_TYPE_CD)
			} ;
			if(row.SAMPLE_TYPE != null){ hv.sampleLabels.add(row.SAMPLE_TYPE)
			} ;
			if(row.TISSUE_TYPE_CD != null){ hv.tissues.add(row.TISSUE_TYPE_CD)
			} ;
			if(row.TISSUE_TYPE != null){ hv.tissueLabels.add(row.TISSUE_TYPE)
			} ;
		})
		if(hv.validate()){return;
		}
		
		sqlt = "SELECT TISSUE_TYPE, TISSUE_TYPE_CD, PLATFORM, TIMEPOINT, TIMEPOINT_CD, SAMPLE_TYPE_CD, SAMPLE_TYPE FROM DE_SUBJECT_SAMPLE_MAPPING WHERE ";
		if (subids != null)
			sqlt += "PATIENT_ID IN ("+listToIN(subids)+") AND ";
		sqlt += "TISSUE_TYPE_CD IN (" + listToIN(conids) + ") GROUP BY PLATFORM, TIMEPOINT, TIMEPOINT_CD, SAMPLE_TYPE_CD, SAMPLE_TYPE, TISSUE_TYPE, TISSUE_TYPE_CD";
		sql.eachRow(sqlt, {row ->
			if(row.PLATFORM != null){hv.platforms.add(row.PLATFORM)
			} ;
			if((row.PLATFORM!=null) && (row.TIMEPOINT_CD!=null) && !(row.PLATFORM.equals('RBM') && row.TIMEPOINT_CD.indexOf(':Z:')==-1)){
				if(row.TIMEPOINT_CD != null){hv.timepoints.add(row.TIMEPOINT_CD)
				} ;
				if(row.TIMEPOINT != null){hv.timepointLabels.add(row.TIMEPOINT)
				};
			}
			if(row.SAMPLE_TYPE_CD != null){ hv.samples.add(row.SAMPLE_TYPE_CD)
			} ;
			if(row.SAMPLE_TYPE != null){ hv.sampleLabels.add(row.SAMPLE_TYPE)
			} ;
			if(row.TISSUE_TYPE_CD != null){ hv.tissues.add(row.TISSUE_TYPE_CD)
			} ;
			if(row.TISSUE_TYPE != null){ hv.tissueLabels.add(row.TISSUE_TYPE)
			} ;
		})
	}
	
	/**
	 * Fill the cohort information requested.
	 * For now, subids could be null due to complexity of workflow and user error
	 * Incoming ci contains a list of codes. Outgoing ci contains codes:label maps
	 */
	def fillCohortInformation(subids, conids, ci, infoType){
		//If the list of subids does not have any elements, or it has only one element which is "ALL"
		if (subids == null || subids.size == 0 || (subids.size == 1 && subids[0] == "ALL"))
			subids = null;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		String sqlt="";
		switch(infoType){
			case CohortInformation.TRIALS_TYPE:
				ci.trials = new ArrayList();
				sqlt="select distinct modifier_cd from observation_fact where ";
				if (subids != null)
					sqlt += "PATIENT_NUM in ("+listToIN(subids)+") and ";
				sqlt += "concept_cd in ("+listToIN(conids)+")";
				sql.eachRow(sqlt, {row->
					ci.trials.add(row.modifier_cd)
				})
			
				if (ci.trials.size()==0){
					sqlt="select distinct sourcesystem_cd from i2b2 where c_basecode in (" + listToIN(conids)+")";
					sql.eachRow(sqlt, {row->
						ci.trials.add(row.sourcesystem_cd)
					})
				}
			
				break;
			case CohortInformation.PLATFORMS_TYPE:
				ci.platforms=new ArrayList();
				sqlt="select distinct platform from de_subject_sample_mapping where trial_name in ("+listToIN(ci.trials)+") order by platform";
				sql.eachRow(sqlt, {row->
					ci.platforms.add([platform:row.platform, platformLabel:("MRNA_AFFYMETRIX".equals(row.platform)?"MRNA":row.platform)])
				})
				break;
			case CohortInformation.TIMEPOINTS_TYPE:
				ci.timepoints=new ArrayList();
				sqlt="select distinct timepoint, timepoint_cd from de_subject_sample_mapping where trial_name in ("+listToIN(ci.trials)+") " +
						"and platform in ("+listToIN(ci.platforms)+")"
				if (ci.platforms.get(0)=='RBM'){
					sqlt+=" and instr(timepoint_cd, ':Z:')>0"
				}
				if(ci.gpls.size>0)
					sqlt+=" and gpl_id in("+listToIN(ci.gpls)+")";
				if(ci.tissues.size>0)
					sqlt+=" and tissue_type_cd in("+listToIN(ci.tissues)+")";
				if(ci.samples.size>0)
					sqlt+=" and sample_type_cd in ("+listToIN(ci.samples)+")";
				if(ci.rbmpanels.size>0)
					sqlt+=" and rbm_panel in ("+listToIN(ci.rbmpanels)+")";
				sqlt+=" order by timepoint"
				sql.eachRow(sqlt, {row->
					if(row.timepoint_cd!=null){ci.timepoints.add([timepointLabel:row.timepoint, timepoint:row.timepoint_cd])
					}
				})
				break;
			case CohortInformation.SAMPLES_TYPE:
				ci.samples=new ArrayList();
				sqlt="select distinct sample_type, sample_type_cd from de_subject_sample_mapping where trial_name in ("+listToIN(ci.trials)+") " +
						"and platform in ("+listToIN(ci.platforms)+")";
				if(ci.gpls.size>0)
					sqlt+=" and gpl_id in("+listToIN(ci.gpls)+")";
				sqlt+=" order by sample_type";
				sql.eachRow(sqlt, {row->
					ci.samples.add([sample:row.sample_type_cd, sampleLabel:row.sample_type])
				})
				break;
			case CohortInformation.TISSUE_TYPE:
				ci.tissues=new ArrayList();
				sqlt="select distinct tissue_type, tissue_type_cd from de_subject_sample_mapping where trial_name in ("+listToIN(ci.trials)+") " +
						"and platform in ("+listToIN(ci.platforms)+")";
				if(ci.gpls.size>0)
					sqlt+=" and gpl_id in("+listToIN(ci.gpls)+")";
				if(ci.samples.size>0)
					sqlt+=" and sample_type_cd in ("+listToIN(ci.samples)+")";
				sqlt+=" order by tissue_type";
				sql.eachRow(sqlt, {row->
					if(row.tissue_type_cd!=null)ci.tissues.add([tissue:row.tissue_type_cd, tissueLabel:row.tissue_type])
				})
				break;
			case CohortInformation.GPL_TYPE:
				ci.gpls=new ArrayList();
				sqlt="select distinct rgi.platform, rgi.title from de_subject_sample_mapping dssm, de_gpl_info rgi where dssm.trial_name in ("+listToIN(ci.trials)+") " +
						"and dssm.platform in ("+listToIN(ci.platforms)+")" +
						"and dssm.gpl_id=rgi.platform"
				sqlt+=" order by rgi.title";
				sql.eachRow(sqlt, {row->
					ci.gpls.add([gpl:row.platform, gplLabel:row.title])
				})
				break;
			case CohortInformation.RBM_PANEL_TYPE:
				ci.rbmpanels=new ArrayList();
				sqlt="select distinct dssm.rbm_panel from de_subject_sample_mapping dssm where dssm.trial_name in ("+listToIN(ci.trials)+") " +
						"and dssm.platform in ("+listToIN(ci.platforms)+")"
				sql.eachRow(sqlt, {row->
					ci.rbmpanels.add([rbmpanel:row.rbm_panel, rbmpanelLabel:row.rbm_panel])
				})
				break;
			default:
				log.trace('No Info Type selected');
		}
	}
	
	/**
	 * First search by trials and platform.
	 * If there is only one, that is the default.
	 * 
	 * If there are multiple, search by concept_code. Return none to multiple defaults.
	 * @param hv
	 * @param ci
	 * @return
	 */
	def fillDefaultGplInHeatMapValidator(hv, ci, concepts){
		ci.platforms.add(hv.getFirstPlatform())
		fillCohortInformation(null, null, ci, CohortInformation.GPL_TYPE)
		if(ci.gpls.size()==1){
			hv.gpls.add(((Map)ci.gpls.get(0)).get('gpl'))
			hv.gplLabels.add(((Map)ci.gpls.get(0)).get('gplLabel'))
		}else if (ci.gpls.size()>1){
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
			String sqlt="";
			sqlt="select distinct rgi.platform, rgi.title from de_subject_sample_mapping dssm, de_gpl_info rgi where dssm.trial_name in ("+listToIN(ci.trials)+") " +
					"and dssm.platform in ("+listToIN(ci.platforms)+")" +
					"and dssm.concept_code in ("+listToIN(concepts)+")" +
					"and dssm.gpl_id=rgi.platform"
			sqlt+=" order by rgi.title";
			sql.eachRow(sqlt, {row->
				hv.gpls.add(row.platform)
				hv.gplLabels.add(row.title)
			})
		}
	}
	
	/**
	 * First search by trials and platform.
	 * If there is only one, that is the default.
	 *
	 * If there are multiple, search by concept_code. Return none to multiple defaults.
	 * @param hv
	 * @param ci
	 * @return
	 */
	def fillDefaultRbmpanelInHeatMapValidator(hv, ci, concepts){
		ci.platforms.add(hv.getFirstPlatform())
		fillCohortInformation(null, null, ci, CohortInformation.RBM_PANEL_TYPE)
		if(ci.rbmpanels.size()==1){
			hv.rbmpanels.add(((Map)ci.rbmpanels.get(0)).get('rbmpanel'))
			hv.rbmpanelsLabels.add(((Map)ci.rbmpanels.get(0)).get('rbmpanelLabel'))
		}else if (ci.rbmpanels.size()>1){
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
			String sqlt="";
			sqlt="select distinct dssm.rbm_panel from de_subject_sample_mapping dssm where dssm.trial_name in ("+listToIN(ci.trials)+") " +
					"and dssm.platform in ("+listToIN(ci.platforms)+") and dssm.CONCEPT_CODE IN (" + listToIN(concepts)+")"
			sql.eachRow(sqlt, {row->
				hv.rbmpanels.add(row.rbm_panel)
				hv.rbmpanelsLabels.add(row.rbm_panel)
			})
		}
	}
	
	def getDistinctTrialsInPatientSets(String rid1, String rid2) {
		log.debug("Checking patient sets")
		def trials=[];
		
		log.debug(rid1+" "+rid2);
		if(rid2==null) {
			log.debug("TESTED AS NULL");
		}
		if(rid1!=null & rid2!=null) {
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
			String sqlt="""SELECT DISTINCT SECURE_OBJ_TOKEN FROM PATIENT_TRIAL t
			    WHERE t.PATIENT_NUM IN (select distinct patient_num
				from qt_patient_set_collection
				where result_instance_id IN (?, ?))""";
			log.debug(sqlt);
			sql.eachRow(sqlt, [rid1, rid2], {row ->
				if(row.SECURE_OBJ_TOKEN!=null) {
					trials.add(row.SECURE_OBJ_TOKEN);
				}
			});
			return trials;
		}
		
		log.debug("between tests")
		if(rid1!=null || rid2!=null) {
			log.debug("one or the other was null")
			def rid;
			if(rid1!=null) {
				rid=rid1;
			}
			else {
				rid=rid2;
			}
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
			String sqlt="""SELECT DISTINCT SECURE_OBJ_TOKEN FROM PATIENT_TRIAL t
			    WHERE t.PATIENT_NUM IN (select distinct patient_num
				from qt_patient_set_collection
				where result_instance_id = ?)""";
			log.debug(sqlt);
			sql.eachRow(sqlt, [rid], {row ->
				if(row.SECURE_OBJ_TOKEN!=null) {
					trials.add(row.SECURE_OBJ_TOKEN)
				}
			})
			return trials;
		}
	}
}
