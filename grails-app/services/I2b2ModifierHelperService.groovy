import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import groovy.sql.Sql
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import java.sql.SQLException
import java.util.HashMap.Entry

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
  * 
  *
  * @author $Author: mmcduffie $
  * @version $Revision:  $
  */

class I2b2ModifierHelperService {

    def dataSource;
    def i2b2HelperService;

    /**
     *  Gets the concept distributions for a concept in a subset
     */
    def  HashMap<String,Integer> getModifierDistributionDataForModifier(modifierObject, String result_instance_id) throws SQLException {

        //As we loop through the returned records in the DB, make a note if any need our "imaginary" visit nodes.
        Boolean needsVisitNodes = false;

        //This is the object we will return.
        HashMap<String,Integer> results = new LinkedHashMap<String, Integer>();

        def modifierPath = modifierObject.name;

        //If we have an in out code, remove it from the path.
        if(modifierObject.inOutCode)
        {
            modifierPath = modifierPath.replace(modifierObject.inOutCode + "\\","")
        }

        Integer levelCount = modifierPath.count("\\")
        levelCount+=1

        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
        String sqlt = "SELECT DISTINCT NAME_CHAR, MODIFIER_PATH, VISIT_IND FROM i2b2demodata.modifier_dimension MD INNER JOIN MODIFIER_METADATA MM ON MM.MODIFIER_CD = MD.MODIFIER_CD WHERE MODIFIER_PATH LIKE ? AND length(MODIFIER_PATH) - length(translate(MODIFIER_PATH, '\\', '')) = ? ORDER BY MODIFIER_PATH";

        log.debug("Running following SQL in getModifierDistributionDataForModifier - " + sqlt)
        log.debug("Parameters - " + modifierPath + "%")
        log.debug("Parameters - " + levelCount)

        sql.eachRow(sqlt, [modifierPath + '%', levelCount],
            {
                row ->

                //If there was a visit indicator flag on the metadata, we return a record with visit codes instead.
                //If there is an in out code passed in, we know we want to collect all the leaf nodes of this "imaginary" folder, so process as normal.
                if(row[2] == "Y" && !modifierObject.inOutCode)
                {
                    needsVisitNodes = true
                }
                else
                {
                    results.put(row[0], getObservationCountForModifierForSubset("\\blah"+row[1], result_instance_id, modifierObject.inOutCode));
                }
            });

        //If we need visit nodes, retrieve them all together here.
        if(needsVisitNodes)
        {
           //Pull the nodes in a temp hashmap.
           def tempResults = retrieveVisitCountsByModifier(modifierObject,result_instance_id)

           //Iterate through the hashmap and add our "imaginary" nodes to the final result set.
           for (Entry<String, Integer> entry : tempResults.entrySet())
           {
                 results.put(entry.getKey(), entry.getValue())
           }

        }

        return results;
    }


    /**
     * Query for counts and group by the visit name so we can get concepts per visit.
     * @param modifier
     * @param result_instance_id
     * @return
     */
    def retrieveVisitCountsByModifier(modifier, String result_instance_id)
    {
        HashMap<String,Integer> results = new LinkedHashMap<String, Integer>();

        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

        String sqlt = """select VD.INOUT_CD, count (*) as obscount FROM i2b2demodata.observation_fact obsf
            INNER JOIN VISIT_DIMENSION VD ON VD.ENCOUNTER_NUM = obsf.ENCOUNTER_NUM
           WHERE (((MODIFIER_CD IN (select MODIFIER_CD from i2b2demodata.MODIFIER_DIMENSION MD
           where MODIFIER_PATH LIKE ?)))) AND obsf.PATIENT_NUM IN (select distinct patient_num from qt_patient_set_collection where result_instance_id = ?) GROUP BY VD.INOUT_CD""";

        log.debug("Running following SQL in retrieveVisitCountsByModifier - " + sqlt)
        log.debug("Parameters - " + modifier.name+"%")
        log.debug("Parameters - " + result_instance_id)

        sql.eachRow(sqlt, [modifier.name + '%', result_instance_id],
            {
                row ->

                    results.put(row[0], row[1].toInteger());
            });


        return results;
    }

    /**
     * Gets the count of the observations in the fact table for a modifier and a subset
     */
    def Integer getObservationCountForModifierForSubset(String modifierKey, String result_instance_id, String inOutCd)
    {

       def parameterList = [];

       String fullname = modifierKey.substring(modifierKey.indexOf("\\",2), modifierKey.length());

       int i=0;

       groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

       String sqlt = """select count (*) as obscount FROM i2b2demodata.observation_fact f
       LEFT JOIN VISIT_DIMENSION VD ON VD.ENCOUNTER_NUM = f.ENCOUNTER_NUM
       WHERE (((MODIFIER_CD IN (select MODIFIER_CD from i2b2demodata.MODIFIER_DIMENSION MD
       where MODIFIER_PATH LIKE ?)))) AND f.PATIENT_NUM IN (select distinct patient_num from qt_patient_set_collection where result_instance_id = ?)""";

       parameterList.push(fullname+"%")
       parameterList.push(result_instance_id)

       if(inOutCd)
       {
           sqlt += " AND  VD.INOUT_CD = ? "
           parameterList.push(inOutCd)
       }

       log.debug("Running following SQL in getObservationCountForModifierForSubset - " + sqlt)
       log.debug("Parameters - " + fullname+"%")
       log.debug("Parameters - " + result_instance_id)

        sql.eachRow(sqlt, parameterList, {row ->
            i= row[0]
        })
        return i;
    }

    def getModifierDistributionDataForValueModifier(Set<String> childConcepts)
    {
            def trialdata=[];

           groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

           String sqlt="""SELECT 'All Studies' TRIAL, NVAL_NUM FROM OBSERVATION_FACT f  INNER JOIN PATIENT_TRIAL t
           ON f.PATIENT_NUM=t.PATIENT_NUM WHERE MODIFIER_CD IN ("""+i2b2HelperService.listToIN(childConcepts.asList())+""") AND
            f.PATIENT_NUM IN (select distinct patient_num
                    from qt_patient_set_collection) """;

           log.debug("Running following SQL in getModifierDistributionDataForValueModifierByTrialByConcepts - " + sqlt)

           log.debug("about to execute query: "+sqlt);

           sql.eachRow(sqlt,
                   {
                       row ->
                       if(row.NVAL_NUM!=null)
                           {
                               trialdata.add(row.NVAL_NUM);
                           }
                   })

           return trialdata;
   }

    def getModifierDistributionDataForValueModifierWithResultInstanceId(modifier, String result_instance_id) {

        def trialdata=[:];
        def parameterList = [];

        if(result_instance_id!=null && result_instance_id!="" && modifier.modifierCode != "")
        {

           groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

           String sqlt="""SELECT 'All Studies' TRIAL, NVAL_NUM FROM OBSERVATION_FACT f  INNER JOIN PATIENT_TRIAL t
           ON f.PATIENT_NUM=t.PATIENT_NUM
           LEFT JOIN VISIT_DIMENSION VD ON VD.ENCOUNTER_NUM = f.ENCOUNTER_NUM
           WHERE f.MODIFIER_CD = ? AND
           f.PATIENT_NUM IN (select distinct patient_num
           from qt_patient_set_collection
           where result_instance_id= ? ) """;

           parameterList.push(modifier.modifierCode)
           parameterList.push(result_instance_id)

           if(modifier.inOutCode)
           {
               sqlt += " AND  VD.INOUT_CD = ? "
               parameterList.push(modifier.inOutCode)
           }

           log.debug("Running following SQL in getModifierDistributionDataForValueModifierWithResultInstanceId - " + sqlt)
           log.debug("about to execute query: "+sqlt);
           log.debug("Parameters : " + parameterList)

           sql.eachRow(sqlt,parameterList,
                   {row ->
                       if(row.NVAL_NUM!=null) {
                           //add a new Array if this is the first time I am hitting this trial
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


    //In the request XML is a field which indicates which dimension table the query is hitting, e.g. MODIFIER_DIMENSION, CONCEPT_DIMENSION... Default should be CONCEPT_DIMENSION if there is no entry.
    def determineQueryDimension(resultInstanceId1, resultInstanceId2)
    {

        ArrayList<String> dimensions = new ArrayList<String>();

        //Construct our SQL objects.
        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
        String sqlt = """SELECT REQUEST_XML FROM QT_QUERY_MASTER c INNER JOIN QT_QUERY_INSTANCE a
           ON a.QUERY_MASTER_ID=c.QUERY_MASTER_ID INNER JOIN QT_QUERY_RESULT_INSTANCE b
           ON a.QUERY_INSTANCE_ID=b.QUERY_INSTANCE_ID WHERE RESULT_INSTANCE_ID IN (CAST (? AS numeric),CAST (? AS numeric))""";

        String xmlrequest="";

        sql.eachRow(sqlt, [resultInstanceId1, resultInstanceId2], {
            row ->

            //Pull the XML column out.
            xmlrequest = i2b2HelperService.clobToString(row.request_xml);

            log.debug("REQUEST_XML in determineQueryDimension:" +xmlrequest)

            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true); // never forget this!
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlrequest)));

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            //From the XML determine the dimension table.
            Object result = xpath.evaluate("//queryDimensionTable", doc, XPathConstants.NODESET);

            NodeList nodes = (NodeList) result;
            Node x=null;
            String key=null;

            //Iterate over all the nodes, should only be one per query.
            for (int i = 0; i < nodes.getLength(); i++)
            {
                x = nodes.item(i);
                key = x.getTextContent();
                dimensions.add(key);
            }
        });

        if(dimensions.unique().size() > 1) throw new Exception("Multiple Dimension types found in Query.")

        return dimensions.join("");
    }

    def getModifierObjectForAnalysis(resultInstanceId1, resultInstanceId2)
    {

        ArrayList<Map> modifierObjects = new ArrayList<Map>();
        def modifierObject = [:]

        //Oracle
        //Construct our SQL objects.
        //groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
        //String sqlt = """SELECT REQUEST_XML FROM QT_QUERY_MASTER c INNER JOIN QT_QUERY_INSTANCE a
           //ON a.QUERY_MASTER_ID=c.QUERY_MASTER_ID INNER JOIN QT_QUERY_RESULT_INSTANCE b
           //ON a.QUERY_INSTANCE_ID=b.QUERY_INSTANCE_ID WHERE RESULT_INSTANCE_ID IN (?,?)""";


        //Netezza
        //Construct our SQL objects.
        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
        String sqlt = """SELECT REQUEST_XML FROM QT_QUERY_MASTER c INNER JOIN QT_QUERY_INSTANCE a
           ON a.QUERY_MASTER_ID=c.QUERY_MASTER_ID INNER JOIN QT_QUERY_RESULT_INSTANCE b
           ON a.QUERY_INSTANCE_ID=b.QUERY_INSTANCE_ID WHERE RESULT_INSTANCE_ID IN ( CAST(? AS numeric), CAST(? AS numeric))""";

        def xmlrequest;

        def records

        log.debug("Running following SQL in getModifierObjectForAnalysis - " + sqlt)
        log.debug("Parameters - " + resultInstanceId1)
        log.debug("Parameters - " + resultInstanceId2)

        sql.eachRow(sqlt, [resultInstanceId1, resultInstanceId2], {
            row ->

            //Pull the XML column out.
            xmlrequest = i2b2HelperService.clobToString(row.request_xml);

            log.debug("REQUEST_XML in getModifierObjectForAnalysis:" +xmlrequest)

            records = new XmlParser().parseText(xmlrequest)

            log.debug("XML RECORDS\n" + records.queryDefinition)

            records.panel.item.each()
            {
                modifierObject = [:]

                modifierObject["modifierCode"] = it.itemCode.text();
                modifierObject["level"] = it.hlevel.text();
                modifierObject["oktousevalues"] = it.oktousevalues.text();
                modifierObject["name"] = it.item_name.text();
                modifierObject["inOutCode"] = it.inOutCode.text();

                modifierObjects.push(modifierObject)

            }

        });

        //if(dimensions.unique().size() > 1) throw new Exception("Multiple Dimension types found in Query.")

        return modifierObjects;
    }

    def getModifierObjectParent(modifierObject)
    {
       //First trim the name of the modifier.
       modifierObject["name"] = modifierObject["name"].substring(0,modifierObject["name"].lastIndexOf("\\", modifierObject["name"].length()-2)+1);

       //The lookup name changes if we have a visit node.
       def lookupName = modifierObject["name"]

       if(modifierObject["inOutCode"])
       {
           lookupName = lookupName.replace(modifierObject["inOutCode"] + "\\","")
       }

       //Now we need to look up the MODIFIER_CD for this path.
       def sqlObject = new Sql(dataSource)
       def modifierCode = sqlObject.firstRow('SELECT MODIFIER_CD FROM MODIFIER_DIMENSION WHERE MODIFIER_PATH = ?', [lookupName])
       modifierObject["modifierCode"] = modifierCode.MODIFIER_CD;

       //Set the level property.
       modifierObject["level"] = modifierObject["name"].count("\\")

       //Set the value property (no longer a value, should be folder).
       modifierObject["oktousevalues"] = "N"

       return(modifierObject)
    }

    def getConceptDistributionDataForValueModifierFromCode(modifier, String result_instance_id)
    {
        def modifierCd = modifier.modifierCode
        def parameterList = []

        ArrayList<Double> values=new ArrayList<Double>();
        ArrayList<Double> returnvalues=new ArrayList<Double>(values.size());

        if (result_instance_id==""){
            log.debug("getConceptDistributionDataForValueModifierFromCode called with no result_istance_id");
            return returnvalues;
        }

        log.trace("Getting concept distribution data for value concept code:"+modifierCd);

        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
        log.trace("preparing query");
        String sqlt="""SELECT NVAL_NUM FROM OBSERVATION_FACT LEFT JOIN VISIT_DIMENSION VD ON VD.ENCOUNTER_NUM = OBSERVATION_FACT.ENCOUNTER_NUM WHERE OBSERVATION_FACT.MODIFIER_CD = ? AND
           OBSERVATION_FACT.PATIENT_NUM IN (select distinct patient_num
           from qt_patient_set_collection
           where result_instance_id = ?)""";

           parameterList.push(modifierCd)
           parameterList.push(result_instance_id)

           log.debug("Running following SQL in getConceptDistributionDataForValueModifierFromCode - " + sqlt)
           log.debug("Parameters - " + modifierCd)
           log.debug("Parameters - " + result_instance_id)

           if(modifier.inOutCode)
           {
               sqlt += " AND  VD.INOUT_CD = ? "
               parameterList.push(modifier.inOutCode)
               log.debug("Parameters - " + modifier.inOutCode)
           }



        log.debug("executing query: "+sqlt);
        sql.eachRow(sqlt, parameterList, {row ->
            if(row.NVAL_NUM!=null) {
                values.add(row.NVAL_NUM);
            }
        })
        for(int i=0;i<values.size();i++) {
            returnvalues[i]=values.get(i);
        }
        log.trace("getConceptDistributionDataForValueModifierFromCode now finished");
        return returnvalues;
    }


    /**
     * Gets all the value nodes below a parent node.
     */
    def ArrayList<Map> getChildModifiersFromParentKey(String modifierPath, Boolean valueOnly, String inOutCode) {

        //This is the object we return in the end.
        ArrayList<Map> modifierObjects = new ArrayList<Map>();

        //This is a temp map that we add to our array.
        def modifierObject = [:]

        //The level is based off the number of backslashes we find.
        int treeLevel = modifierPath.count("\\") + 1

        //We will use this flag later to determine if we need to gather "imaginary" folders.
        Boolean needsVisitNodes = false

        //If we have an in out code, remove the in out code from the path and decrement the level. This is because the in out represents an "imaginary" node.
        if(inOutCode)
        {
            modifierPath = modifierPath.replace(inOutCode + "\\", "")
            treeLevel-=1
        }

        //If we are only pulling values, add an additional filter here.
        def valtypeCode = ""
        if(valueOnly) valtypeCode = "AND VALTYPE_CD = 'N'"

        //Construct our SQL objects.
        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
        String sqlt = """
                SELECT MD.NAME_CHAR, MD.MODIFIER_PATH, MM.VISIT_IND, MD.MODIFIER_CD
                FROM i2b2demodata.modifier_dimension MD
                INNER JOIN i2b2demodata.modifier_metadata MM ON MD.MODIFIER_CD = MM.MODIFIER_CD
                WHERE MODIFIER_PATH LIKE ?
                AND length(MODIFIER_PATH) - length(translate(MODIFIER_PATH, '\\', '')) =  ? ${valtypeCode}
                ORDER BY MODIFIER_PATH"""

        log.debug("Running following SQL in getChildModifiersFromParentKey - " + sqlt)
        log.debug("Parameters - " + modifierPath+"%")
        log.debug("Parameters - " + treeLevel)

        //Pull the list of modifierCodes below this node.
        sql.eachRow(sqlt, [modifierPath + "%", treeLevel], {
            row ->

                //If we only want value nodes below our path.
                if(valueOnly)
                {
                    //If the VISIT_IND is not "Y", then it's a normal node. If we have an inOutCode that means it was an "imaginary" folder. We are leaving out of this the leaf time series data, they are handled differently.
                    if(inOutCode || row.VISIT_IND != "Y")
                    {
                       modifierObject = [:]

                       modifierObject["modifierCode"] = row.MODIFIER_CD;
                       modifierObject["level"] = "";
                       modifierObject["oktousevalues"] = "Y";
                       modifierObject["name"] = row.MODIFIER_PATH;
                       modifierObject["inOutCode"] = inOutCode

                       modifierObjects.push(modifierObject)
                    }
                }
                else
                {

                   //If we are getting the imaginary folders we need to do something a little different to collect the possible subfolders.
                   if(row.VISIT_IND == "Y" && !inOutCode)
                   {
                       needsVisitNodes = true
                   }
                   else
                   {
                       modifierObject = [:]

                       modifierObject["modifierCode"] = row.MODIFIER_CD;
                       modifierObject["level"] = "";
                       modifierObject["oktousevalues"] = "Y";
                       modifierObject["name"] = row.MODIFIER_PATH;
                       modifierObject["inOutCode"] = inOutCode

                       modifierObjects.push(modifierObject)
                   }
                }

            })

        //If we need to add visit nodes to the modifier object, do it here.
        if(needsVisitNodes)
        {
            //Pull the nodes in a temp hashmap.
            def tempResults = retrieveVisitModifiersByModifierPath(modifierPath)

            //Iterate through the hashmap and add our "imaginary" nodes to the final result set.
            tempResults.each()
            {
                modifierObjects.push(it)
            }
        }

        return modifierObjects;

    }


    def createModifierObject(modifierCode,level,oktousevalues,name, inOutCode)
    {
        //Create a modifier object from the params.
        ArrayList<Map> modifierObjects = new ArrayList<Map>();
        def modifierObject = [:]

        modifierObject["modifierCode"] = modifierCode;
        modifierObject["level"] = level;
        modifierObject["oktousevalues"] = oktousevalues;
        modifierObject["name"] = name;
        modifierObject["inOutCode"] = inOutCode;

        return modifierObject
    }



    /**
     * Adds a column of data to the grid export table
     */
    def ExportTableNew addModifierDataToTable(ExportTableNew tablein, modifierObject, String result_instance_id) {

        if(modifierObject["level"] == "leaf")
        {
            //Trim the strings for ID and Name.
            String columnid = getShortNameFromKey(modifierObject["name"]).replace(" ", "_").replace("...", "");
            String columnname = i2b2HelperService.getColumnNameFromKey(modifierObject["name"]).replace(" ", "_");

            def parameterList = []

            //Add subject column to table if it doesn't already exist.
            if(tablein.getColumn("subject")==null)
            {
                tablein.putColumn("subject", new ExportColumn("subject", "Subject", "", "String", 50));
            }

            //This is the column object for the modifier we are adding.
            ExportColumn thisColumn = tablein.getColumn(columnid);

            //Add this column if it isn't already in the table.
            if(tablein.getColumn(columnid)==null)
            {
                thisColumn = new ExportColumn(columnid, columnname, "", "");
                tablein.putColumn(columnid, thisColumn);
            }

            //If this is a value column.
            if(modifierObject["oktousevalues"] == "Y")
            {
               thisColumn.setType("Number");

               //Get the actual Modifier data.
               groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
               String sqlt = """SELECT f.PATIENT_NUM, f.NVAL_NUM, f.START_DATE FROM OBSERVATION_FACT f """

               if(modifierObject["inOutCode"])
               {
                   sqlt += " INNER JOIN VISIT_DIMENSION VD ON VD.ENCOUNTER_NUM = f.ENCOUNTER_NUM AND VD.INOUT_CD = ? "
                   parameterList.push(modifierObject["inOutCode"])
               }

                sqlt += """WHERE f.MODIFIER_CD = ? AND
               f.PATIENT_NUM IN (select  patient_num
               from qt_patient_set_collection
               where result_instance_id = ?)""";

               parameterList.push(modifierObject["modifierCode"])
               parameterList.push(result_instance_id)

               log.debug("Running following SQL in addModifierDataToTable - " + sqlt)
               log.debug("Parameters - " + parameterList)

                sql.eachRow(sqlt, parameterList, {row ->
                    /*If I already have this subject mark it in the subset column as belonging to both subsets*/
                    String subject=row.PATIENT_NUM
                    Double value=row.NVAL_NUM
                    if(tablein.containsRow(subject)) /*should contain all subjects already if I ran the demographics first*/ {
                        tablein.getRow(subject).put(columnid, value.toString());
                    }
                    else /*fill the row*/ {
                        ExportRowNew newrow = new ExportRowNew();
                        newrow.put("subject", subject);
                        newrow.put(columnid, value.toString());
                        tablein.putRow(subject, newrow);
                    }
                })
            }
            else {

                thisColumn.setType("String");

                groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
                String sqlt = """SELECT f.PATIENT_NUM, f.TVAL_CHAR, f.START_DATE FROM OBSERVATION_FACT f """

                       if(modifierObject["inOutCode"])
                       {
                           sqlt += " INNER JOIN VISIT_DIMENSION VD ON VD.ENCOUNTER_NUM = f.ENCOUNTER_NUM AND VD.INOUT_CD = ? "
                           parameterList.push(modifierObject["inOutCode"])
                       }

                        sqlt += """WHERE f.MODIFIER_CD = ? AND
                       f.PATIENT_NUM IN (select  patient_num
                       from qt_patient_set_collection
                       where result_instance_id = ?)""";

                       parameterList.push(modifierObject["modifierCode"])
                       parameterList.push(result_instance_id)

                       log.debug("Running following SQL in addModifierDataToTable - " + sqlt)
                       log.debug("Parameters - " + parameterList)


                sql.eachRow(sqlt, parameterList, {row ->
                    /*If I already have this subject mark it in the subset column as belonging to both subsets*/
                    String subject=row.PATIENT_NUM
                    String value=row.TVAL_CHAR

                    if(value==null)
                    {
                        value="Y";
                    }

                    if(tablein.containsRow(subject)) /*should contain all subjects already if I ran the demographics first*/ {
                        tablein.getRow(subject).put(columnid, value.toString());
                    }
                    else
                    {
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
                    row.put(columnid, "");
                }
            }
        }
        else {
            log.trace("must be a folder dont add to grid");
        }
        return tablein;
    }


    /**
     * Query for counts and group by the visit name so we can get concepts per visit.
     * @param modifier
     * @param result_instance_id
     * @return
     */
    def retrieveVisitModifiersByModifierPath(modifierPath)
    {
        ArrayList<Map> modifierObjects = new ArrayList<Map>();
        def modifierObject = [:]

        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

        String sqlt = """SELECT	DISTINCT MD.MODIFIER_LEVEL,
                                   substr(MD.MODIFIER_PATH,1,instr(MD.MODIFIER_PATH,'\\',-1,2)) || VD.INOUT_CD || '\\' AS MODIFIER_PATH,
                                   VD.INOUT_CD
           FROM		MODIFIER_DIMENSION MD
           INNER JOIN MODIFIER_METADATA MM ON MM.MODIFIER_CD = MD.MODIFIER_CD
           INNER JOIN OBSERVATION_FACT OBSF ON OBSF.MODIFIER_CD = MD.MODIFIER_CD
           INNER JOIN VISIT_DIMENSION VD ON VD.ENCOUNTER_NUM = OBSF.ENCOUNTER_NUM
           WHERE	MD.MODIFIER_PATH LIKE ?""";

        log.debug("Running following SQL in retrieveVisitModifiersByModifierPath - " + sqlt)
        log.debug("Parameters - " + modifierPath+"%")

        sql.eachRow(sqlt, [modifierPath + '%'],
            {
                row ->

                       modifierObject = [:]

                       modifierObject["modifierCode"] = row.INOUT_CD;
                       modifierObject["level"] = row.MODIFIER_LEVEL;
                       modifierObject["oktousevalues"] = "Y";
                       modifierObject["name"] = row.MODIFIER_PATH;
                       modifierObject["inOutCode"] = row.INOUT_CD

                       modifierObjects.push(modifierObject)
            });


        return modifierObjects;
    }

    /**
     * Adds a column of data to the grid export table
     */
    def ExportTableNew addModifierFolderDataToTable(ExportTableNew tablein, parentModifier, childModifier, String result_instance_id) {

        def parameterList = []
        def searchPath = childModifier["name"]

        //Get the id and name of the column that is used in the export table.
        String columnid = getShortNameFromKey(parentModifier["name"].replace("\\","\\\\")).replace(" ", "_").replace("...", "");
        String columnname = i2b2HelperService.getColumnNameFromKey(parentModifier["name"].replace("\\","\\\\")).replace(" ", "_");

        println("columnid - " + columnid)
        println("columnname - " + columnname)

        //Get or create the column.
        ExportColumn thisColumn = tablein.getColumn(columnid);

        if(!thisColumn)
        {
            thisColumn = new ExportColumn(columnid, columnname, "", "");
            tablein.putColumn(columnid, thisColumn);
        }

        //This is the SQL statement to get the patient list for the modifiers.
        String sqlt = """	SELECT  DISTINCT Q.PATIENT_NUM
                            FROM I2B2DEMODATA.MODIFIER_DIMENSION MD
                            INNER JOIN I2B2DEMODATA.OBSERVATION_FACT f ON (f.MODIFIER_CD = MD.MODIFIER_CD AND MD.MODIFIER_PATH LIKE ?)
                            INNER JOIN I2B2DEMODATA.QT_PATIENT_SET_COLLECTION Q ON ( Q.PATIENT_NUM = f.PATIENT_NUM AND RESULT_INSTANCE_ID = ?)""";

       //Do a string replace on modifier path
       if(childModifier["inOutCode"])
       {
           searchPath = searchPath.replace(childModifier["inOutCode"] + "\\","")
       }

        parameterList.push(searchPath + "%")
        parameterList.push(result_instance_id)

        //Add the in out code if it exists.
       if(childModifier["inOutCode"])
       {
           sqlt += " INNER JOIN VISIT_DIMENSION VD ON VD.ENCOUNTER_NUM = f.ENCOUNTER_NUM AND VD.INOUT_CD = ? "
           parameterList.push(childModifier["inOutCode"])
       }

        //Change column type based on whether this is a value column.
        if(childModifier["oktousevalues"] == "Y")
        {
            thisColumn.setType("Number");
        }
        else
        {
            thisColumn.setType("String");
        }

        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

        def resultRows = sql.rows(sqlt, parameterList);

            resultRows.each { row ->

                try {
                    String subject=row.PATIENT_NUM;

                    if(tablein.containsRow(subject))
                    {

                        String shortName=i2b2HelperService.getColumnNameFromKey(childModifier["name"]).replace(" ", "_").replace("...", "");

                        println("shortname - " + shortName)

                        //Get the current value and add the concept if not there
                        def tRow = tablein.getRow(subject);

                        def tColVal = tRow.get(columnid);

                        if (tColVal)
                        {
                            List<String> values = tColVal.split(",").toList();

                            if (!values.contains(shortName))
                            {
                                tColVal += tColVal ? ", " + shortName :  shortName;
                            }
                        }
                        else
                        {
                            tColVal = shortName;
                        }

                        tRow.put(columnid, tColVal.toString());
                    }
                    else /*fill the row*/ {
                        ExportRowNew newrow=new ExportRowNew();
                        newrow.put("subject", subject);
                        newrow.put(columnid, "");
                        tablein.putRow(subject, newrow);
                    }

                }
                catch (Exception exc)
                {
                    log.trace(exc);

                }
            };

        for(ExportRowNew row: tablein.getRows())
        {
            if(!row.containsColumn(columnid)) {
                row.put(columnid, "");
            }
        }

        return tablein;
    }



    /**
     * Gets the short display name from a concept key
     */
    def String getShortNameFromKey(String concept_key) {
        String[] splits=concept_key.split("\\\\");
        String concept_name="";

        concept_name = splits.join("\\")


        return concept_name;
    }




}