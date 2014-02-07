import com.recomdata.transmart.domain.i2b2.ModifierDimension
import com.recomdata.transmart.domain.i2b2.ConceptVisit
import org.transmart.searchapp.AuthUser

class CrossTrialService {
	def dataSource;
	def i2b2HelperService
	def springSecurityService
	def i2b2DemoDataService
	/**
	 * This is the main function to generate a list of modifier objects based on a search string, or all nodes below a certain other node.
	 * @param searchTerm - Text term to search on.
	 * @param nodeLevel - The level of the current node in the tree.
	 * @param nodeToOpen - The text of the node that is being opened.
	 * @return
	 */
	def generateTree(String searchTerm, String nodeLevel, String nodeToOpen, String inOutCd) 
	{
		
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
		log.trace("User is:"+user.username);
		log.trace(user.toString());
		def List<String> studyNames=i2b2DemoDataService.getShortStudyNamesForUser(user);
		def studyNamesIN = i2b2HelperService.listToIN(studyNames)
		//Get the list of Modifiers from the database.
		def modifierList
		
		//This is the level of the nodes we are retrieving.
		def retrieveNodeLevel = nodeLevel.toInteger() + 1;
		
		//I am doing this because I can't figure out how to get the two domain classes to work.
		//ModifierDimension and ModifierMetadata are a 1-1 relationship. Both tables share the MODIFIER_CD PK.
		if(searchTerm)
		{
			modifierList = ModifierDimension.executeQuery("""
				SELECT new map(	A.id as id, 
								A.modifierPath as modifierPath, 
								A.nameChar as nameChar,
								B.valtypeCd as valtypeCd, 
								A.modifierLevel as modifierLevel, 
								A.modifierNodeType as modifierNodeType, 
								E.linkType as timingLevel,
								count(DISTINCT C.patientNum) as observationCount)  
				FROM	ModifierDimension A,
						ModifierMetadata B,
						ObservationFact C,
						EncounterLevel E
				WHERE 	A.id = B.id
				AND		A.id = C.modifierCd
				AND		lower(A.modifierPath) LIKE :searchTerm
				AND     C.sourcesystemCd IN ("""+ studyNamesIN +""")
				GROUP BY A.id, A.modifierPath, A.nameChar,B.valtypeCd, A.modifierLevel, A.modifierNodeType, E.linkType
				ORDER BY A.modifierNodeType,A.nameChar""",[searchTerm : "%" + searchTerm.toLowerCase() + "%"])
		}
		else
		{
			if(inOutCd)
			{
				//If we have an inOut code, remove it from the concept path as it isn't a real node.
				nodeToOpen = nodeToOpen.replace(inOutCd + "\\", "")
				
				modifierList = ModifierDimension.executeQuery("""
					SELECT new map(	A.id as id, 
									CONCAT(substr(A.modifierPath,1,instr(A.modifierPath,'\\',-1,2)),D.visitName,SUBSTR(A.modifierPath,INSTR(A.modifierPath,'\\',-1,2))) as modifierPath, 
									A.nameChar as nameChar,
									B.valtypeCd as valtypeCd, 
									A.modifierLevel as modifierLevel, 
									A.modifierNodeType as modifierNodeType, 
									B.visitInd as visitInd,
									D.visitName as inOutCode,
									E.linkType as timingLevel,
									count(DISTINCT C.patientNum) as observationCount) 
					FROM	ModifierDimension A, 
							ModifierMetadata B,
							ObservationFact C,
							EncounterLevel E 
					LEFT JOIN C.conceptVisit D
					WHERE 	A.id = B.id 
					AND		A.id = C.modifierCd
					AND		A.modifierLevel = :retrieveNodeLevel
					AND		A.modifierPath LIKE :nodeToOpen
					AND		D.visitName = :inOutCode
					AND     C.sourcesystemCd IN ("""+ studyNamesIN +""")
					GROUP BY 	A.id, 
								CONCAT(substr(A.modifierPath,1,instr(A.modifierPath,'\\',-1,2)),D.visitName,SUBSTR(A.modifierPath,INSTR(A.modifierPath,'\\',-1,2))), 
								A.nameChar,
								B.valtypeCd, 
								A.modifierLevel, A.modifierNodeType, B.visitInd, D.visitName, E.linkType
					ORDER BY A.modifierNodeType,A.nameChar""",[retrieveNodeLevel : retrieveNodeLevel, nodeToOpen : nodeToOpen + "%", inOutCode : inOutCd])
			}
			else
			{
				 //Netezza
				//Since we aren't retrieving leaf time series data, we need to case the name_char and modifier path to get rid of duplicates.
				modifierList = ModifierDimension.executeQuery("""
					SELECT new map(	case B.visitInd
										WHEN 'Y' THEN D.visitName
										ELSE A.id
									END as id,
									case B.visitInd
										WHEN 'Y' THEN CONCAT(substr(A.modifierPath,1,instr(A.modifierPath,'\\',-1,2)),D.visitName,'\\')
										ELSE A.modifierPath
									END as modifierPath,
									case
										WHEN B.visitInd = 'Y' THEN D.visitName
										ELSE A.nameChar
									END as nameChar,
									B.valtypeCd as valtypeCd,
									A.modifierLevel as modifierLevel,
									A.modifierNodeType as modifierNodeType,
									B.visitInd as visitInd,
									case B.visitInd
										WHEN 'Y' THEN D.visitName
										ELSE NULL
									END as inOutCode,
									E.linkType as timingLevel,
									count(DISTINCT C.patientNum) as observationCount)
					FROM	ModifierDimension A,
							ModifierMetadata B,
							ModifierDimension A1,
							ObservationFact C,
							EncounterLevel E
					LEFT JOIN C.conceptVisit D
					WHERE 	A.id = B.id
					AND     A1.id = C.modifierCd
					AND     A1.modifierNodeType = 'L'
					AND     A1.modifierPath LIKE A.modifierPath || '%' escape ''
                	AND		E.conceptCode = C.conceptCd
					AND		A.modifierLevel = :retrieveNodeLevel
					AND		A.modifierPath LIKE :nodeToOpen escape ''
					AND     C.sourcesystemCd IN ("""+ studyNamesIN +""")
					GROUP BY case B.visitInd
										WHEN 'Y' THEN D.visitName
										ELSE A.id
									END,
						case B.visitInd
							WHEN 'Y' THEN CONCAT(substr(A.modifierPath,1,instr(A.modifierPath,'\\',-1,2)),D.visitName,'\\')
							ELSE A.modifierPath
						END,
						case
							WHEN B.visitInd = 'Y' THEN D.visitName
							ELSE A.nameChar
						END,
						B.valtypeCd, A.modifierLevel, A.modifierNodeType, B.visitInd,
						case B.visitInd
							WHEN 'Y' THEN D.visitName
							ELSE NULL
						END,
						E.linkType
					ORDER BY A.modifierNodeType,case
										WHEN B.visitInd = 'Y' THEN D.visitName
										ELSE A.nameChar
									END""",[retrieveNodeLevel : retrieveNodeLevel, nodeToOpen : nodeToOpen + "%"])

                  //Oracle
                 //Since we aren't retrieving leaf time series data, we need to case the name_char and modifier path to get rid of duplicates.
                /* modifierList = ModifierDimension.executeQuery("""
					SELECT new map(	case B.visitInd
										WHEN 'Y' THEN D.visitName
										ELSE A.id
									END as id,
									case B.visitInd
										WHEN 'Y' THEN CONCAT(substr(A.modifierPath,1,instr(A.modifierPath,'\\',-1,2)),D.visitName,'\\')
										ELSE A.modifierPath
									END as modifierPath,
									case
										WHEN B.visitInd = 'Y' THEN D.visitName
										ELSE A.nameChar
									END as nameChar,
									B.valtypeCd as valtypeCd,
									A.modifierLevel as modifierLevel,
									A.modifierNodeType as modifierNodeType,
									B.visitInd as visitInd,
									case B.visitInd
										WHEN 'Y' THEN D.visitName
										ELSE NULL
									END as inOutCode,
									E.linkType as timingLevel,
									count(DISTINCT C.patientNum) as observationCount)
					FROM	ModifierDimension A,
							ModifierMetadata B,
							ModifierDimension A1,
							ObservationFact C,
							EncounterLevel E
					LEFT JOIN C.conceptVisit D
					WHERE 	A.id = B.id
					AND     A1.id = C.modifierCd
                    AND     A1.modifierNodeType = 'L'
                    AND     A1.modifierPath LIKE A.modifierPath || '%'
					AND		E.conceptCode = C.conceptCd
					AND		A.modifierLevel = :retrieveNodeLevel
					AND		A.modifierPath LIKE :nodeToOpen
					AND     C.sourcesystemCd IN ("""+ studyNamesIN +""")
					GROUP BY case B.visitInd
										WHEN 'Y' THEN D.visitName
										ELSE A.id
									END,
						case B.visitInd
							WHEN 'Y' THEN CONCAT(substr(A.modifierPath,1,instr(A.modifierPath,'\\',-1,2)),D.visitName,'\\')
							ELSE A.modifierPath
						END,
						case
							WHEN B.visitInd = 'Y' THEN D.visitName
							ELSE A.nameChar
						END,
						B.valtypeCd, A.modifierLevel, A.modifierNodeType, B.visitInd,
						case B.visitInd
							WHEN 'Y' THEN D.visitName
							ELSE NULL
						END,
						E.linkType
					ORDER BY A.modifierNodeType,case
										WHEN B.visitInd = 'Y' THEN D.visitName
										ELSE A.nameChar
									END""",[retrieveNodeLevel : retrieveNodeLevel, nodeToOpen : nodeToOpen + "%"])*/
			}
		}
		
		return modifierList;	
	}
	
	/**
	 * This function specifically retrieves the top level modifier objects.
	 * @return
	 */
	def generateRootTreeNodes()
	{

		//Get the list of Modifiers from the database.
		def modifierList
		
		modifierList = ModifierDimension.executeQuery("""
				SELECT new map(A.id as id, A.modifierPath as modifierPath, A.nameChar as nameChar,B.valtypeCd as valtypeCd,A.modifierLevel as modifierLevel,A.modifierNodeType as modifierNodeType, 0 as observationCount) 
				FROM	ModifierDimension A, 
						ModifierMetadata B
				WHERE 	A.id = B.id 
				AND		A.modifierLevel = 0
				ORDER BY A.modifierNodeType,A.nameChar""")
		print modifierList
		return modifierList;

	}
	
	//This generates a tree of terms related to a search term.
	def generateRelatedTree(String searchTerm, String nodeLevel, String nodeToOpen)
	{
		def modifierList 
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
		def List<String> studyNames=i2b2DemoDataService.getShortStudyNamesForUser(user);
		def studyNamesIN = i2b2HelperService.listToIN(studyNames)
		
			if(nodeToOpen && nodeToOpen != "-1")
			{
				//This is the level of the nodes we are retrieving.
				def retrieveNodeLevel = nodeLevel.toInteger() + 1;
				
				modifierList = ModifierDimension.executeQuery("""
					SELECT new map(A2.id as id, A2.modifierPath as modifierPath, A2.nameChar as nameChar,B1.valtypeCd as valtypeCd,A2.modifierLevel as modifierLevel,A2.modifierNodeType as modifierNodeType, count(DISTINCT C2.patientNum) as observationCount) 
					FROM	ModifierDimension A1,
							ModifierDimension A2,
							ModifierMetadata B1,
							ObservationFact C1,
							ObservationFact C2
					WHERE 	A1.id = C1.modifierCd
					AND		lower(A1.modifierPath) LIKE :searchTerm
					AND		C2.patientNum = C1.patientNum
					AND		C2.modifierCd = A2.id
					AND		lower(A2.modifierPath) NOT LIKE :searchTerm
					AND		A2.modifierPath LIKE :nodeToOpen
					AND		A2.modifierLevel = :retrieveNodeLevel
					AND		A2.id = B1.id
					AND     C1.sourcesystemCd IN ("""+ studyNamesIN +""")
					GROUP BY A2.id, A2.modifierPath, A2.nameChar,B1.valtypeCd,A2.modifierLevel,A2.modifierNodeType
					ORDER BY A2.modifierNodeType,A2.nameChar""",[searchTerm : "%" + searchTerm.toLowerCase() + "%", nodeToOpen : "%" + nodeToOpen + "%", retrieveNodeLevel : retrieveNodeLevel])
			}
			else
			{
				modifierList = ModifierDimension.executeQuery("""
					SELECT new map(A2.id as id, A2.modifierPath as modifierPath, A2.nameChar as nameChar,B1.valtypeCd as valtypeCd,A2.modifierLevel as modifierLevel,A2.modifierNodeType as modifierNodeType, count(DISTINCT C2.patientNum) as observationCount) 
					FROM	ModifierDimension A1,
							ModifierDimension A2,
							ModifierMetadata B1,
							ObservationFact C1,
							ObservationFact C2
					WHERE 	A1.id = C1.modifierCd
					AND		lower(A1.modifierPath) LIKE :searchTerm
					AND		C2.patientNum = C1.patientNum
					AND		C2.modifierCd = A2.id
					AND		lower(A2.modifierPath) NOT LIKE :searchTerm
					AND		A2.id = B1.id
					AND     C1.sourcesystemCd IN ("""+ studyNamesIN +""")
					GROUP BY A2.id, A2.modifierPath, A2.nameChar,B1.valtypeCd,A2.modifierLevel,A2.modifierNodeType
					ORDER BY A2.modifierNodeType,A2.nameChar""",[searchTerm : "%" + searchTerm.toLowerCase() + "%"])
			}
		
		return modifierList;
	}
	
	/*
	 * A.id as id, 
								A.modifierPath as modifierPath, 
								A.nameChar as nameChar,
								B.valtypeCd as valtypeCd, 
								A.modifierLevel as modifierLevel, 
								A.modifierNodeType as modifierNodeType, 
								B.visitInd as visitInd,
								D.visitName as inOutCode
	 */
	
	def createTimeSeriesJSNodeFromModifier(modifierNode, leafnode)
	{
		
		//Form a new concept path with this made up node.
		//def tokenizedPath = modifierNode.modifierPath.split("\\\\")
		//def newPath = "\\" + tokenizedPath[1..<tokenizedPath.size()-1].join("\\") + "\\" + modifierNode.inOutCode + "\\";
		
		//if(leafnode) newPath +=  tokenizedPath[tokenizedPath.size()-1] + "\\"
		
		//Find out the node level. We are creating an "imaginary" node before the time series leaves.
		def nodeLevel = modifierNode.modifierLevel.toInteger() - 1
		
		//Return the node level to normal if this is the leaf time node.
		if(leafnode) nodeLevel += 1
		
		//Each node has a metadata section which the jstree uses to pass data around about the nodes.
		def metaDataJson = [:]
		metaDataJson["id"] = modifierNode.id
		
		//Both of these represent concept paths.
		metaDataJson["qtip"] = modifierNode.modifierPath
		metaDataJson["dimcode"] = modifierNode.modifierPath
		metaDataJson["tablename"] = "MODIFIER_DIMENSION"
		metaDataJson["level"] = nodeLevel
		
		if(!leafnode)
		{
			metaDataJson["iconCls"] = "T"
		}
		else
		{
			metaDataJson["iconCls"] = modifierNode.valtypeCd
		}
		
		//This is a text flag indicating whether this is a node that has visit information.
		metaDataJson["visitInd"] = "Y"
		
		//This code is the text that represents the name of the visit.
		metaDataJson["inOutCode"] = modifierNode.inOutCode
		
		//The attribute section controls how the node is displayed.
		def attrDataJson = [:]
		
		def nodeName = modifierNode.nameChar
		
		//If we are drawing the imaginary folder, set rel = "".
		if(!leafnode)
		{
			attrDataJson["rel"] = ""
			attrDataJson["name"] = modifierNode.inOutCd
			
			//The node name should be the visit information, no the actual name of the node in this case.
			nodeName = modifierNode.inOutCode
		}
		else
		{
			attrDataJson["rel"] = modifierNode.valtypeCd
			attrDataJson["name"] = modifierNode.nameChar
		}
		
		nodeName+= " (" + modifierNode.observationCount + ")"
		
		def finalJson = [:]
		finalJson["data"] = nodeName
		
		if(!leafnode) finalJson["state"] = "closed"

		//Build the final object from the sub arrays.
		finalJson["metadata"] = metaDataJson
		finalJson["attr"] = attrDataJson
		finalJson["children"] = []
		
		return finalJson
	}
	
	//This function takes a database modifier record and creates an object that can be used to render JSON.
	def createJSNodeFromModifier(modifierNode)
	{
	
		//Each node has a metadata section which the jstree uses to pass data around about the nodes.
		def metaDataJson = [:]
		metaDataJson["id"] = modifierNode.id
		metaDataJson["qtip"] = modifierNode.modifierPath
		metaDataJson["dimcode"] = modifierNode.modifierPath
		metaDataJson["iconCls"] = modifierNode.valtypeCd
		metaDataJson["tablename"] = "MODIFIER_DIMENSION"
		metaDataJson["level"] = modifierNode.modifierLevel
		metaDataJson["visitInd"] = modifierNode.visitInd ? modifierNode.visitInd : ""
		metaDataJson["inOutCode"] = modifierNode.inOutCode ? modifierNode.inOutCode : ""
		metaDataJson["timingLevel"] = modifierNode.timingLevel ? modifierNode.timingLevel : "S"
		
		//The attribute section controls how the node is displayed.
		def attrDataJson = [:]
		
		//If the modifier is a folder node, we put in an empty "rel" attribute.
		if(modifierNode.modifierNodeType == "F")
		{
			attrDataJson["rel"] = ""
		}
		else
		{
			attrDataJson["rel"] = modifierNode.valtypeCd
		}
		
		attrDataJson["name"] = modifierNode.nameChar

		//The node name shouldn't have counts for the root node, modifer the name if the node isn't root.
		def nodeName = modifierNode.nameChar
		
		//If it's not a top level folder, add the observation count.
		if(modifierNode.modifierLevel != 0)
		{
			 nodeName+= " (" + modifierNode.observationCount + ")"
		}
		
		def finalJson = [:]
		finalJson["data"] = nodeName
		
		//If it's a folder, add this attribute so we know to draw the "+" sign indicating there are children.
		if(modifierNode.modifierNodeType == "F")
		{
			finalJson["state"] = "closed"
		}
		
		//Build the final object from the sub arrays.
		finalJson["metadata"] = metaDataJson
		finalJson["attr"] = attrDataJson
		finalJson["children"] = []
		
		return finalJson
	}

}