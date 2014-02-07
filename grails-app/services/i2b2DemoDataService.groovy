import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import org.transmart.searchapp.AuthUser

class i2b2DemoDataService {
	def dataSource;
	def i2b2HelperService;
	
	//Generate a new result instance ID using the sequence and return it.
	def generateResultInstanceId()
	{
		def sqlObject = new Sql(dataSource)
		
        //Orcle
		//def newInstanceId = sqlObject.firstRow('SELECT QT_SQ_QRI_QRIID.nextval NEXTVAL from dual')

        //Netezza
		def newInstanceId = sqlObject.firstRow('SELECT next value for QT_SQ_QRI_QRIID NEXTVAL')

		return newInstanceId.NEXTVAL;
	}
	
	//Generate records in QT_QUERY_MASTER, QT_QUERY_INSTANCE, QT_QUERY_RESULT_INSTANCE so we can retrieve information about this query later.
	def createQueryRecords(resultInstanceId, requestXML)
	{
		//This is the object we use for our queries.
		def sqlObject = new Sql(dataSource)

        //Oracle
        //Generate the query master id.
        //def queryMasterId = sqlObject.firstRow('SELECT QT_SQ_QM_QMID.nextval NEXTVAL from dual')

        //Netezza
		//Generate the query master id.
		def queryMasterId = sqlObject.firstRow('SELECT next value for QT_SQ_QM_QMID NEXTVAL')

        //Oracle
        //Insert a record in the QT_QUERY_MASTER table.
        //sqlObject.execute("INSERT INTO QT_QUERY_MASTER (QUERY_MASTER_ID, NAME, USER_ID, GROUP_ID, CREATE_DATE, REQUEST_XML, DELETE_FLAG) VALUES (?, ?, 'DEMO','DEMO', sysdate, ?, 'N')", [queryMasterId.NEXTVAL,'Across Trial Query',requestXML])

        //Netezza
		//Insert a record in the QT_QUERY_MASTER table.
        sqlObject.execute("INSERT INTO QT_QUERY_MASTER (QUERY_MASTER_ID, NAME, USER_ID, GROUP_ID, CREATE_DATE, REQUEST_XML, DELETE_FLAG) VALUES (?, ?, 'DEMO','DEMO', now(), ?, 'N')", [queryMasterId.NEXTVAL,'Across Trial Query',requestXML])

        //Oracle
        //Generate the query instance id.
        //def queryInstanceId = sqlObject.firstRow('SELECT QT_SQ_QI_QIID.nextval NEXTVAL from dual')

        //Netezza
		//Generate the query instance id.
		def queryInstanceId = sqlObject.firstRow('SELECT next value for  QT_SQ_QI_QIID NEXTVAL')
		
		//Insert a record in the QT_QUERY_INSTANCE table.
		sqlObject.execute("INSERT INTO QT_QUERY_INSTANCE (QUERY_INSTANCE_ID, QUERY_MASTER_ID, USER_ID, GROUP_ID, START_DATE) VALUES (?,?,'DEMO','DEMO', now())", [queryInstanceId.NEXTVAL, queryMasterId.NEXTVAL])

        //Oracle
        //Insert a record in the QT_QUERY_RESULT_INSTANCE table.
        //sqlObject.execute("INSERT INTO QT_QUERY_RESULT_INSTANCE (RESULT_INSTANCE_ID, QUERY_INSTANCE_ID, RESULT_TYPE_ID, START_DATE, STATUS_TYPE_ID) VALUES (?,?,1,sysdate,3)", [resultInstanceId, queryInstanceId.NEXTVAL])

		//Netezza
		//Insert a record in the QT_QUERY_RESULT_INSTANCE table.
		sqlObject.execute("INSERT INTO QT_QUERY_RESULT_INSTANCE (RESULT_INSTANCE_ID, QUERY_INSTANCE_ID, RESULT_TYPE_ID, START_DATE, STATUS_TYPE_ID) VALUES (?,?,1,now(),3)", [resultInstanceId, queryInstanceId.NEXTVAL])
		
	}
	
	def generateRequestXML(criteriaJSON)
	{
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		
		xml.queryDefinition()
		{
			queryDimensionTable("MODIFIER_DIMENSION")
			
			criteriaJSON.each()
			{
				currentAndGroup ->
					
					panel() 
					{
						//Find out where these get set.
						invert(currentAndGroup["invert"])
						panel_number(currentAndGroup["panel_number"])
                        panel_timing("any")
                        total_item_occurrences(1)
						
						currentAndGroup.items.each()
						{
							currentOrGroup ->
							
							item()
							{
								item_name(currentOrGroup["conceptFullName"])
                                item_key(currentOrGroup["conceptId"])
                                tooltip(currentOrGroup["conceptFullName"])
								itemCode(currentOrGroup["conceptId"])
								hlevel(currentOrGroup["conceptLevel"])
								oktousevalues(currentOrGroup["oktousevalues"])
								inOutCode(currentOrGroup["inOutCode"])
                                'class'("ENC")
								
								if(currentOrGroup["criteriaType"] == "numeric")
								{
									constrain_by_value()
									{
										value_operator(currentOrGroup["valueOperator"])
										
										if(currentOrGroup["valueOperator"] == "BETWEEN")
										{
											value_constraint(currentOrGroup["valueLow"] + " and " + currentOrGroup["valueHigh"])
										}
										else
										{
											value_constraint(currentOrGroup["valueLow"])
										}
									}
								}
							}
							
						}
					}
				}
			}
		  
		return writer.toString()
	}
	
	//Take the JSON Object of packaged criteria and generate a SQL statement which will populate the qt_patient_set_collection table.
	def populatePatientSetFromJSON(criteriaJSON, resultInstanceId, currentUser)
	{
		//Netezza
		//This is the final SQL string which does the inserts.
		def finalSQLString = "INSERT INTO qt_patient_set_collection (patient_set_coll_id, result_instance_id, patient_num) select next value for qt_sq_qs_qsid, ?,t.patient_num from ( SELECT DISTINCT OBSFACT.PATIENT_NUM FROM OBSERVATION_FACT OBSFACT "

        //Oracle
        //This is the final SQL string which does the inserts.
        //def finalSQLString = "INSERT INTO qt_patient_set_collection (result_instance_id, patient_num) SELECT DISTINCT ?, OBSFACT.PATIENT_NUM FROM OBSERVATION_FACT OBSFACT "

		//This is the portion of the SQL statement that makes up the criteria for selecting the patients.
		def sqlORList = ""
		def sqlANDList = ""
		def joinText = ""
		def parameterList = []
		def levelJoin = ""
		def excludeJoinNull = ""
		def joinCount = 0
		//Netezza
        def finalWhereClause = " WHERE MODIFIER_CD IS NOT NULL ) t"

        //Oracle
        //def finalWhereClause = " WHERE MODIFIER_CD IS NOT NULL "
		
		//Add our result instance id to the insert statement.
		parameterList.push(resultInstanceId)
		
		//Loop through each of the criteria groups, these queries get ANDed together.
		criteriaJSON.each()
		{	currentAndGroup ->
			
			sqlORList = ""
			joinText = ""
			levelJoin = ""
			excludeJoinNull = ""
			
			log.debug("Outside Criteria")
			log.debug(currentAndGroup)
			
			//All the criteria inside gets OR'ed together.
			currentAndGroup.items.each()
			{
				currentOrGroup ->
				
				if(sqlORList != "") sqlORList += " OR  "
				
				log.debug("Inside Critera")
				log.debug(currentOrGroup)
				log.debug(currentOrGroup["criteriaType"])
				
				def sqlModifierConstraint = ""
				
				//As long as this isn't a time series node, add the modifier code to the query.
				if(currentOrGroup["inOutCode"] == "" || currentOrGroup["inOutCode"] == "false")
				{
					sqlModifierConstraint = " MD.MODIFIER_CD = ? "
					parameterList.push(currentOrGroup["conceptId"]);
				}
				
				//If the in out code isn't empty, we need to filter our analysis by the text name of the visit in visit_dimension.
				if(currentOrGroup["inOutCode"] != "" && currentOrGroup["inOutCode"] != "false")
				{
					//If this is a leaf time series node, we can just use the modifier code. If it's the "imaginary" folder node, we need to do a lookup by the modifier path.
					if(currentOrGroup["conceptLevel"] == "leaf")
					{
						sqlModifierConstraint += " MD.MODIFIER_CD = ? "
						parameterList.push(currentOrGroup["conceptId"]);
					}
					else
					{
						sqlModifierConstraint += " MD.MODIFIER_PATH LIKE ? "
						
						//The path we need to like is the full concept path with the timepoint text removed.
						def pathLike = currentOrGroup["conceptFullName"]
						pathLike = pathLike.replace(currentOrGroup["inOutCode"] + "\\", "") + "%"
						
						parameterList.push(pathLike);
					}
					
					sqlModifierConstraint += " AND VD.INOUT_CD = ?  "
					
					parameterList.push(currentOrGroup["inOutCode"]);
				}
				
				//Check to see what the type of this criteria is.
				switch(currentOrGroup["criteriaType"])
				{
					case "novalue":
						break;
					case "highlow":
						//This isn't used for now, but may be in the future.
						break;
					case "numeric":
						switch(currentOrGroup["valueOperator"])
						{
							case "GT":
								sqlModifierConstraint += " AND NVAL_NUM > ?";
								parameterList.push(currentOrGroup["valueLow"]);
								break;
							case "GE":
								sqlModifierConstraint += " AND NVAL_NUM >= ?";
								parameterList.push(currentOrGroup["valueLow"]);
								break;
							case "LT":
								sqlModifierConstraint += " AND NVAL_NUM < ?";
								parameterList.push(currentOrGroup["valueLow"]);
								break;
							case "LE":
								sqlModifierConstraint += " AND NVAL_NUM <= ?";
								parameterList.push(currentOrGroup["valueLow"]);
								break;
							case "EQ":
								sqlModifierConstraint += " AND NVAL_NUM = ?";
								parameterList.push(currentOrGroup["valueLow"]);
								break;
							case "BETWEEN":
								sqlModifierConstraint += " AND NVAL_NUM BETWEEN ? and ?";
								parameterList.push(currentOrGroup["valueLow"]);
								parameterList.push(currentOrGroup["valueHigh"]);
								break;
						}
						break;
				}
				
				sqlORList += sqlModifierConstraint
				
			}
			
			//Build the name of the sub select.
			def subselectName = "OBSFACT" + joinCount
			
			//Include a space if this isn't the first join.
			if(sqlANDList != "") sqlANDList += "  "


			
			//We change the join type if we are inverting the joined table inclusion.
            if(currentAndGroup.invert == 1 || currentAndGroup.invert == 0){
                if(currentAndGroup.invert == 1)
                {
                    joinText = " LEFT OUTER JOIN  "
                    excludeJoinNull = " AND ${subselectName}.PATIENT_NUM IS NULL "
                    finalWhereClause += excludeJoinNull
                }
                else
                {
                    joinText = " INNER JOIN "
                }
            }
            else{
                throw new Exception(" Invalid request parameter.")
            }
			
			//Same Event panels need to also join on encounter number.
			if(currentAndGroup.sameEvent == 1) 
			{
				levelJoin = " AND ${subselectName}.ENCOUNTER_NUM = OBSFACT.ENCOUNTER_NUM "
			}
			else
			{
				levelJoin = " "
			}
			
			//Build the sub select statement.
			sqlANDList += " ${joinText} (SELECT OBSFACT_INNER.PATIENT_NUM, OBSFACT_INNER.ENCOUNTER_NUM FROM OBSERVATION_FACT OBSFACT_INNER INNER JOIN MODIFIER_DIMENSION MD ON MD.MODIFIER_CD = OBSFACT_INNER.MODIFIER_CD LEFT JOIN VISIT_DIMENSION VD ON VD.ENCOUNTER_NUM = OBSFACT_INNER.ENCOUNTER_NUM WHERE " + sqlORList + ") ${subselectName} ON ${subselectName}.PATIENT_NUM = OBSFACT.PATIENT_NUM ${levelJoin}"
			
			//Increment the counter for the number of sub selects.
			joinCount += 1
			
		}
		
		finalSQLString += sqlANDList
		finalSQLString += finalWhereClause
		
		//We need to take security into consideration when the user is saving a patient list.
		//If the user is an admin, we don't have to add anything to this query.
		def isUserAdmin = false;
		
		for (role in currentUser.authorities)
		{
			if (i2b2HelperService.isAdminRole(role)) 
			{
				isUserAdmin=true;
				
				log.debug("ADMINISTRATOR, SKIPPING PERMISSION CHECKING")
				
			}
		}
		
		//If the user is not an administrator we have to delve into the security settings to restrict the studies they aren't allowed to use.
		if(!isUserAdmin)
		{
			//Get all the objects that this user has access to.
		}
		
		log.debug("finalSQL - " + finalSQLString)
		log.debug("parameterList - " + parameterList)
		
		//Run the insert statement.
		def sqlObject = new Sql(dataSource)
		def insertPatientRecords = sqlObject.execute(finalSQLString, parameterList)

	}
	
	/**
	 * Converts a clob to a string I hope
	 */
	def String clobToString(clob) {
		if(clob==null) return "";
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
	 * Gets the distinct study short names the user has access to
	 */
	def getShortStudyNamesForUser( AuthUser user, String accessLevel = "VIEW") {
		
		def studyNameTokens = getAllShortStudyNamesWithTokens()
		def List<String> studyNames = new ArrayList<String>();
		def admin = i2b2HelperService.isAdmin(user)
		if(admin){ //skipping checking for studies and adding all of of them
			studyNames.addAll(studyNameTokens.keySet())			
		} else {
			def userTokenmap=i2b2HelperService.getSecureTokensWithAccessForUser(user);
			for (studyName in studyNameTokens.keySet()) //have some kind of access to each of these tokens
			{
				def studyToken = studyNameTokens.get(studyName)
				if(userTokenmap.containsKey(studyToken)){  ///check if user has a security token for this study
					//check user access level before adding it to the list
					def userAccessLevel = userTokenmap.get(studyToken)
					switch (accessLevel){
					case "OWN":
						if(userAccessLevel == "OWN" ){
							studyNames.add(studyName)
						}
						break
					case "EXPORT":
						if(userAccessLevel == "OWN"  || userAccessLevel == "EXPORT"){
							studyNames.add(studyName)
						}
					break				
					default: //its a VIEW so the user can have OWN, EXPORT or VIEW privileges
							studyNames.add(studyName)
					break
					}
				}
			}		
		}	
		return studyNames;		
	}
	
	/*
	 * This method will retrieve the "Centricity" or levels of the nodes so we know whether it has encounter numbers that span a patient or a subset of encounters.
	 */
	def getNodeTimingLevels(String parentConcept)
	{
		//Create the prefix value we use to construct the path later.
		String prefix = parentConcept.substring(0, parentConcept.indexOf("\\",2));
		
		//This is the full path we use in the query.
		String fullname = parentConcept.substring(parentConcept.indexOf("\\",2), parentConcept.length());
		
		//This is the list we return in the end.
		def ls=[:];
		
		//Get the level of the concept so we can grab the next level down.
		int i = i2b2HelperService.getLevelFromKey(parentConcept) + 1;
		
		//Build a SQL statement to retrieve the level records.
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		
		String sqlt = "SELECT DE_ENCOUNTER_LEVEL.LINK_TYPE,i2b2.C_FULLNAME FROM DE_ENCOUNTER_LEVEL INNER JOIN i2b2 ON i2b2.C_BASECODE = DE_ENCOUNTER_LEVEL.CONCEPT_CD WHERE i2b2.C_FULLNAME LIKE ? AND i2b2.C_HLEVEL = ?";
		
		//Build the list of concept paths and link types.
		sql.eachRow(sqlt, [fullname+"%", i], {row ->
			
			String conceptkey = prefix+row.C_FULLNAME;
			ls.put(i2b2HelperService.keyToPath(conceptkey), row.LINK_TYPE);
		})
		
		return ls;
	}
	
	
	/**
	 * Gets the  study short names 
	 */
	def  getAllShortStudyNamesWithTokens() {
		def c_hlevel=1;
		String xml;
		def ls=[:];
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		
		
		String sqlt = "SELECT sourcesystem_cd, secure_obj_token FROM i2b2metadata.i2b2_SECURE WHERE c_hlevel = ? ORDER BY SOURCESYSTEM_CD";
		sql.eachRow(sqlt, [c_hlevel], {row ->
			ls.put(row.sourcesystem_cd, row.secure_obj_token);
			log.trace("@@found"+row.sourcesystem_cd);
		})
		return ls;
	}
	
}