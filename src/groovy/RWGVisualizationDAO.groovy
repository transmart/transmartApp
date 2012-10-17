import org.apache.log4j.Logger
import org.springframework.context.ApplicationContext
import bio.BioMarkerCorrelationMV
import search.SearchKeyword
import org.json.*

import bio.BioAssayCohort

/**
 * $Id: $
 * @author $Author: $
 * @version $Revision: $  
 */

/**
 * Main Data Access Object to get all of the visualization data for the RWG datasets
 */
class RWGVisualizationDAO {
	ApplicationContext ctx = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getAttribute(org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes.APPLICATION_CONTEXT)
	def dataSource = ctx.getBean('dataSource')
	def grailsApplication = ctx.getBean('grailsApplication')
	
	static Logger log = Logger.getLogger(RWGVisualizationDAO.class)


	/**
	* Method to retrieve a list of gene names for the give probes (identified by probe name)
	*
	* @param probes - string containing pipe delimited list of probe names
	* @param indexByProbeName - if true, use probe name for index in map, else use the numeric probe id (feature_group_id)
	* @param analysisId - if not null, use to construct a subquery for filtering on probes
	* @param showSigResultsOnly - if true, subquery for probes will be limited to significant results only 
	*
	* @return a map of containing one map for each probe - contains 4 items - a) the short display (e.g. first gene with a plus indicator if more than one) and b) long display containing the full list of gene names and
	*                                                                           c) display for select box d) gene Id 
	**/
   def getGenesForProbes(probes, indexByProbeName = false, analysisId=null, showSigResultsOnly=false)  {
	   
	   def probesListInClause

	   // if an analysis Id is not passed in, create a probe list IN clause, else
	   //  	   construct a subquery to filter on the probes
	   if (analysisId == null)  {
		   probesListInClause = convertPipeDelimitedStringToInClause(probes, "feature_group_name")
	   }
	   else {
		   probesListInClause = new StringBuilder()
		   probesListInClause.append(" AND feature_group_name in ") 
		   probesListInClause.append("  (select probe_id from heat_map_results ") 
           probesListInClause.append("   where bio_assay_analysis_id=${analysisId} ")
		   if (showSigResultsOnly)  {
			   probesListInClause.append(" and significant=1")
		   } 
		   probesListInClause.append(")")
	   }
	   
	   groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)

	   // don't use a string builder with append stmts here -- causes major performance issue when tries to append
	   // large probes list clause
	   String s = "SELECT bafg.bio_assay_feature_group_id, bafg.feature_group_name, bm.bio_marker_name, bm.primary_external_id gene_id FROM biomart.bio_assay_data_annotation bada, biomart.bio_marker bm, biomart.bio_assay_feature_group bafg WHERE bada.bio_assay_feature_group_id=bafg.bio_assay_feature_group_id "
	   s = s +  " AND bada.bio_marker_id=bm.bio_marker_id ${probesListInClause} ORDER BY bafg.bio_assay_feature_group_id, bafg.feature_group_name, bm.bio_marker_name"
	   
       def results = [:]
	   def genes = []
	   def geneIds = []
	   def currentProbeId 
	   def currentProbeName 
	   def currentGene 
	   def currentGeneId 
	   def previousProbeId
	   def previousProbeName 
	   
	   log.info("before query in getGenesForProbes")
	   log.info("query="+s)
	   def rows = sql.rows(s) 
	   log.info("after query in getGenesForProbes")
       // store the sql results into a map containing one map for each probe (which contains a list of genes)
       rows.each {row->
		   currentProbeId = row.bio_assay_feature_group_id
		   currentProbeName = row.feature_group_name
		   currentGene = row.bio_marker_name
		   currentGeneId = row.gene_id
		   // new probe encountered
		   if ((previousProbeId == null) || (currentProbeId != previousProbeId))  {
			   
			   // add previous gene list to results map
			   if (previousProbeId != null)  {
				   def result = [:]
				   result.put('id', previousProbeId)
				   result.put('name', previousProbeName) 
				   result.put('genes', genes)
				   result.put('geneIds', geneIds)
				   results.put(previousProbeName, result)
			   }
			   // clear out gene list
			   genes = []
			   geneIds = []
		   }
		   
		   // add gene to gene list for the probe 
		   genes.push(currentGene)
		   geneIds.push(currentGeneId)
		   
		   log.info('getGenesForProbes loop (gene, gene_id):' +currentGene +', ' +currentGeneId )
		   
		   previousProbeId = currentProbeId
		   previousProbeName = currentProbeName
	   }

	   // add final gene list to results list
	   if (previousProbeId != null)  {
		   def result = [:]
		   result.put('id', previousProbeId)
		   result.put('name', previousProbeName) 
		   result.put('genes', genes)
		   result.put('geneIds', geneIds)
		   results.put(previousProbeName, result)
	   }

	   def returnMap = [:]
	   
	   for (p in probes.split(/\|/))  {
		   def probeMap = [:]
		   def shortDisplay = ""
		   def longDisplay = ""
		   def geneCount = 0
		   def currentId
		   
		   // retrieve the map object containing the results for this probe	
		   def probe = results.get(p)

           if (indexByProbeName)  {
			   currentId = probe.get('name')					   
		   } 				   
		   else  {					   
			   currentId = probe.get('id')					   
		   }
		   // retrieve the genes for this probe and determine short and long display
		   probe.get('genes').each()  {gene->
				   
				   geneCount++
				   
				   // first gene, set short as Short Display, and add to long
				   if (geneCount == 1) {
					   shortDisplay = gene
					   longDisplay = gene
				   }
				   else {
					   // just add to long display with a comma preceding
					   longDisplay = /${longDisplay}, ${gene}/
				   }
		   }

		   // add + indicator to end of gene name if we have more than one
		   if (geneCount > 1)  {
			   shortDisplay = /${shortDisplay}+/
		   }
		   
		   def geneId = probe.get('geneIds')[0]
		   
		   
	   	   probeMap.put("geneId", geneId)
	   	   probeMap.put("shortDisplay", shortDisplay)
		   probeMap.put("longDisplay", longDisplay)

		   def selectDisplay    // display for select box on line and box plots
		   selectDisplay = /${shortDisplay} (${currentId})/		   
		   probeMap.put("selectDisplay", selectDisplay)

		   // add probe to return map
		   returnMap.put(currentId, probeMap)
	   }
	   return returnMap
   }
	
	

   /**
   * Method to retrieve the data values and cohort information for a given analysis and probe_id for use by line plot or box plot
   *
   * @param probe_name - bio_assay_feature_group name for the probe
   * @param analysisID - the analysis ID
   *
   * @return a map of cohort as key and a map containing cohort desc, cohort display order, and data necessary for the graph
   **/
  def getBoxplotOrLineplotData(analysisId, probe_name, boxplot=true)  {
	  groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
	  StringBuilder s = new StringBuilder()
	  s.append("""
		 Select cohort_id, log_intensity, assay_id, min(gene_id) as gene_id
		 From heat_map_results
		 Where Bio_Assay_Analysis_Id = ?
		 And probe_id=?
		   group by cohort_id, log_intensity, assay_id order by cohort_id, log_intensity
	  """)
	  def sqlParams = [analysisId, probe_name]
	  log.info("${s}")
	  log.info("${sqlParams}")
	  def cohortDataMap = [:]
	  def cohortSampleCountMap = [:]
	  def intensityArray = []
	  def cohort = null
	  def gene_id = null
	  
	  log.debug("Loop through and store all of the intensity values in the array and use the cohort as the key")
	  sql.eachRow(s.toString(), sqlParams, {row->
		  
		  //get the gene_id just once (it will be the same for all rows)
		  if(gene_id == null){
			  gene_id = row.gene_id
		  }
		  
		  if (cohort == null)	{
			  cohort = row.cohort_id
		  }
		  if (cohort != row.cohort_id)	{
			  
			  // for a box plot put the array of intensity values on the data map, for a line plot put a map of calculated values (e.g. mean, std error)			  
			  if (boxplot)  {
				  cohortDataMap.put(cohort, intensityArray)
			  }
			  else {
				  cohortDataMap.put(cohort, createLinePlotCohortMap(intensityArray))
			  }

			  cohortSampleCountMap.put(cohort, intensityArray.size())
			  
			  
			  intensityArray = []
			  cohort = row.cohort_id
		  }
		  intensityArray << row.log_intensity
	  })
	  
	 // for a box plot put the array of intensity values on the data map, for a line plot put a map of calculated values (e.g. mean, std error)
	 if (boxplot)  {
		 cohortDataMap.put(cohort, intensityArray)		 
	 }
	 else {
		 cohortDataMap.put(cohort, createLinePlotCohortMap(intensityArray))
	 }
	 cohortSampleCountMap.put(cohort, intensityArray.size())
	 
	 def cohortMap = [:]   // this is the map containing all info needed for box plot (descriptions + order + data)

	 // loop through each cohort for the analysis and create a map that contains all the info
	 // needed for the line plot (i.e. order, desc, data)
	 def analysisInfo = getHeatmapAnalysisInfo(analysisId)
	  
	 // First, retrieve all N cohorts from analysInfo map
	 def cohorts =  analysisInfo.get("cohorts")
			 
	 for (cohortIndex in 1..cohorts.size())  {
		 // find cohort with specified order
		 def c = cohorts.find {  cohorts.get(it.key).get("order") == cohortIndex }
				 
		 def desc = cohorts.get(c.key).get("desc")
		 def order = cohortIndex
		 def data = cohortDataMap.get(c.key)		 
		 def sampleCount = cohortSampleCountMap.get(c.key)
		 
		 def cMap = [:]
		 cMap.put('order', order)
		 cMap.put('desc', desc)
		 cMap.put('data', data)
		 cMap.put('sampleCount', sampleCount)
		 
		 cohortMap.put(c.key, cMap)
		 
		 cohortMap.put('gene_id', gene_id)
	 }
	 return cohortMap
  }

	
	/**
	 * Method to retrieve the log2 intensity values and cohort information for a given analysis and probe_id
	 *
	 * @param probe_name - bio_assay_feature_group name for the probe
	 * @param analysisID - the analysis ID
	 *
	 * @return a map of cohort as key and an array of log2 intensity values and cohort information
	 **/
	def getBoxplotData(analysisId, probe_name)  {
		return getBoxplotOrLineplotData(analysisId, probe_name, true)
	}

	/**
	* Calculate the statistics needed for a cohort on the line plot and create a map 
	*
	* @param intensityArray - list of intensity values for a cohort
	*
	* @return a map containing mean log2 intensity and standard error
	**/
	def createLinePlotCohortMap(intensityArray)  {
				
		def mean = getMean(intensityArray)
		def stdError = getStandardError(intensityArray)
		def dataMap = [:]
		dataMap.put('mean', mean)
		dataMap.put('stdError', stdError)
		
		return dataMap
 	}
 
	
	/**
	* Method to retrieve the mean and standard error of log2 intensity values for the cohort IDs for a given analysis and probe_id
	*
	* @param probe_name - bio_assay_feature_group name for the probe
	* @param analysisID - the analysis ID
	*
	* @return a map with cohort id as key, containing data map with mean log2 intensity and standard error, cohort desc, and cohort display order
	**/
   def getLineplotData(analysisId, probe_name)  {
	   return getBoxplotOrLineplotData(analysisId, probe_name, false)
	   
   }
   			
	/**
	 * Method to get information related to the analysis
	 * 
	 * @param analysisId - the analysis ID
	 * 
	 * @return a map that contains information related to the analysis (e.g. study name, cohorts, analysis description)
	 */	
	def getHeatmapAnalysisInfo(analysisId)  {
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)

		StringBuilder s = new StringBuilder()
		s.append("""
				SELECT a.STUDY_ID, a.analysis_cd as ANALYSIS_NAME, b.LONG_DESCRIPTION
				 FROM BIO_ANALYSIS_COHORT_XREF a, BIO_ASSAY_ANALYSIS b
				WHERE a.BIO_ASSAY_ANALYSIS_ID = b.BIO_ASSAY_ANALYSIS_ID
				AND a.BIO_ASSAY_ANALYSIS_ID = ?
		""")
		def firstRow = sql.firstRow(s.toString(), [analysisId])
		def studyId = firstRow.study_id
		def a_name = firstRow.analysis_name
		def desc = firstRow.long_description
		
		// retrieve cohorts from semicolon delimited string, and trim whitespace
		def cohortsUntrimmed = a_name.split(';')
		def cohorts = []
		cohortsUntrimmed.each { cohorts.add (it.trim()) }
				
		// create a map for cohorts, containing an entry for each cohort with its id as key and its description/order as value
		def cohortsMap = [:]

		def cohortIndex = 1
		cohorts.each {
			// create a new map for a new cohort 
			def cohortMap = [:]

											
     		// retrieve descriptions from database for current cohort
		    def cohortDesc = BioAssayCohort.findByStudyIdAndCohortId(studyId, it).cohortTitle
			
			cohortMap.put("desc", cohortDesc)
			cohortMap.put("order", cohortIndex)
			cohortsMap.put(it, cohortMap)
	
			cohortIndex++
		}
				
						
		def returnMap = [:]
		returnMap.put('analysisId', analysisId)
		returnMap.put('studyId', studyId)
		returnMap.put('a_name', a_name)
		returnMap.put('desc', desc)

		returnMap.put('cohorts', cohortsMap)
		return returnMap
		
	}

	/**
	 * convert list in form probe1|probe2|probe3, change to - AND (fieldName in ('probe1',...,'probe1000') OR fieldName in () )
	 * 
	 * @param listPipes - original string with pipes and no quotes
	 * @param fieldName - name of field
	 * 
	 * @return string with single quotes around each item with a comma between
	 */	
	def convertPipeDelimitedStringToInClause(listPipes, fieldName)  {

		def s = new StringBuilder()
		def l = listPipes?.tokenize("|")
		
		if (l?.size > 0)	{
 
			s.append("AND (")
			
			def count = 0
			// l is a List
			// add an IN clause for each chunk of 1000 with ORs between each clause
			for (item in l) {

				   // is this first gene to add, or is it 1000, 2000, ...
				if ( (count % 1000) == 0 ) {
					
					// not the first in list, we need to close up prior OR clause and start another
					if (count > 0)  {
						s.append(") OR ")
					}
					
					s.append(fieldName + " IN (")
				}
				else  {
					// not the first and not divisble by 1000, need to add comman before next one
					s.append(",")
				}
				
				s.append("'" + item + "'")

				count++
				
			}
			// close out the last OR, and the AND surrounding all the IN clauses
			s.append(") )")
		}

		return s
			
	}
		
	
	/**
	 * Method to add heat map filters to the SQL for retrieving heat map probe rankings or heat map results
	 * 
	 * @param s - sql string (i.e. StringBuilder object) that we're adding the filters to
	 * @param probesList - string containing list of probes in form probe1|probe2|probe3
	 * @param genes - Pipe delimited string of search keyword ids for genes 
	 * @param showSigResultsOnly - flag indicating whether only significant results are being shown
	 * 
	 * @return list of SQL params added to the query
	 */	
	def addHeatmapFilters(s, probesList, genes, showSigResultsOnly, analysisInfo) {

		// retrieve all N cohorts from analysInfo map
		def cohorts =  analysisInfo.get("cohorts")
		
		def cohortList = []

		// add the cohort keys to a list
		for (cohortIndex in 1..cohorts.size())  {
			// find cohort with specified order
			def cohort = cohorts.find {  cohorts.get(it.key).get("order") == cohortIndex }
			cohortList.add(cohort.key)
		}
		// create a string containing a question mark for each placeholder for a cohort
		def paramList = ""
		cohortList.each {
			if (paramList == "")  {
				paramList = "?"
			}  
			else  {
				paramList = "${paramList},?"				
			}		
		}

		s.append(""" WHERE trial_name = ?
		AND cohort_id in ( ${paramList})
		AND bio_assay_analysis_id = ?
		""")

		
		def sqlParams = [analysisInfo.get("studyId")]
		cohortList.each {
			sqlParams.add(it)
		}
     	sqlParams.add(analysisInfo.get("analysisId"))
		 
		 
		 
		 log.info("---------- probesList = " +probesList)
		 

		// probes list will be null when querying to get rankings
		if (probesList != null && probesList.toString() != 'allProbes')  {
			
			def probesListInClause = convertPipeDelimitedStringToInClause(probesList, "PROBE_ID")
			s.append(probesListInClause)
		}
		
		
		//if we are exporting everything, do not limit by search_keyword (gene ID)
		if(probesList.toString() != 'allProbes')
		{
		
			def geneList = genes?.tokenize("|")
			
			if (geneList?.size > 0)	{
 
				s.append("AND (")
				
				def count = 0			
				// genes is a List
				// add an IN clause for each chunk of 1000 with ORs between each clause			
	            for (gene in geneList) {
	
	               	// is this first gene to add, or is it 1000, 2000, ...						
					if ( (count % 1000) == 0 ) {
						
						// not the first in list, we need to close up prior OR clause and start another 
						if (count > 0)  {
						    s.append(") OR ")
						}
						
						s.append("search_keyword_id IN (")
					}
					else  {
						// not the first and not divisble by 1000, need to add comman before next one
						s.append(",")
					}
					
					s.append(gene)
	
					count++
					
	            }			
				// close out the last OR, and the AND surrounding all the IN clauses 
				s.append(") )")
			}
		
		}
		if (showSigResultsOnly)	{
			s.append("AND (significant = 1)")
		}

		return sqlParams		
	}

	
	/**
	 * Main method to get the heatmap data for RWG
	 * 
	 * @param analysisID - the analysis ID
	 * @param genes - an optional list of genes 
	 * @param showSigResultsOnly - are we showing only the significant results?
	 * @param pageNumber - What page number are we obtaining data for?  Defaults to 1
	 * @param probesPerPage - number of probes on each page
	 * 
	 * @return an array of maps that contain the heatmap data and metadata
	 */	
	def getHeatmapData(analysisID, genes, showSigResultsOnly, pageNumber, probesPerPage)	{
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
				
		// retrieve information related to the analysis
		def analysisInfo = getHeatmapAnalysisInfo(analysisID)
		 	   
		StringBuilder s = new StringBuilder()
	    def sampleArray = []
	    def sampleMap = [:]
	    def currentProbe = null
	    def currentGene = null
		def currentGeneID = null;
	    def currentGeneList = null
	    def currentFoldChange = null
	    def currentPValue = null
		def currentPreferredPValue = null
	    def fullKeyListUnsorted = []
	    def fullKeyListSorted = []
	    def values = []
	    def sortedValues = []
		def probesMap = getHeatmapProbeRankings(analysisID, genes, showSigResultsOnly, pageNumber, probesPerPage.toInteger())
		
		def probesList = ""
	    def probesListMap = probesMap.get("probesList")
	    def selectListMap = probesMap.get("selectList")
		
		probesListMap.keys().each() {
			if (probesList != "")  {
				probesList = probesList + "|"
			}
			probesList = probesList + probesListMap.get(it)
			
		}
					   
	    // This query grabs all of the values by significance 
	    s = new StringBuilder()
	    s.append("""
				  SELECT DISTINCT
				  SUBJECT_ID,
				  LOG_INTENSITY,
				  COHORT_ID,
				  PROBE_ID,
				  fold_change_ratio,
				  tea_normalized_pvalue,
				  assay_id,
                  bio_assay_feature_group_id,
				  PREFERRED_PVALUE
				  FROM heat_map_results
	    """)

		def sqlParams = addHeatmapFilters(s, probesList, genes, showSigResultsOnly, analysisInfo)

		// order by fold change ratio then pvalue
	    s.append(" order by fold_change_ratio desc, tea_normalized_pvalue, probe_id, cohort_id, subject_id ")
		
		log.info("${s}")
		log.info("${sqlParams}")

	    def cohortList = []

		def genesDisplayMap = getGenesForProbes(probesList)
		
				
		log.info("before data query")
		sql.eachRow(s.toString(), sqlParams, {row->	
			if (currentProbe == null)	{
				
				log.info(row.probe_id)
				currentProbe = row.probe_id
				
				log.info(row.bio_assay_feature_group_id)
			    def genesDisplay = genesDisplayMap.get(row.bio_assay_feature_group_id)
				
			//	log.info(genesDisplay)
			//	printlin(genesDisplay)
				
				log.info(genesDisplay.get("geneId"))
				currentGeneID = genesDisplay.get("geneId")
				currentGene = genesDisplay.get("shortDisplay")
				currentGeneList = genesDisplay.get("longDisplay")
			    currentFoldChange = row.fold_change_ratio
			    currentPValue = row.tea_normalized_pvalue
				currentPreferredPValue = row.PREFERRED_PVALUE
			}
		    if (currentProbe != row.probe_id )	{
				sampleMap["GENE"] = currentGene
				sampleMap["GENE_ID"] = currentGeneID
				sampleMap["GENELIST"] = currentGeneList
			    sampleMap["PROBE"] = currentProbe
			    sampleMap["FOLD_CHANGE"] = currentFoldChange
			    sampleMap["TEA_P_VALUE"] = currentPValue
				sampleMap["PREFERRED_PVALUE"] =  currentPreferredPValue
			    sampleArray.add(sampleMap)
			    sampleMap = [:]

				def genesDisplay = genesDisplayMap.get(row.bio_assay_feature_group_id)
				currentGene = genesDisplay.get("shortDisplay")
				currentGeneList = genesDisplay.get("longDisplay")
			    currentProbe = row.probe_id
				currentGeneID = genesDisplay.get("geneId")
				currentFoldChange = row.fold_change_ratio
			    currentPValue = row.tea_normalized_pvalue
				currentPreferredPValue = row.PREFERRED_PVALUE
		    }
		    def key = row.cohort_id + ":" + row.assay_id
			
			// add the cohort to the cohort list if not already in cohort list
			if (cohortList.find{ it == row.cohort_id} == null)  {
			   cohortList.push(row.cohort_id)
			}

			// add the key to the full key list if not already in full key list
			if (fullKeyListUnsorted.find{ it == key} == null)  {
			   fullKeyListUnsorted.push(key)
			}

		    sampleMap[key] = row.log_intensity
		    values.add(row.log_intensity)
		    sortedValues.add(row.log_intensity)			
	    })
		log.info("after data query")
		
	    sampleMap["GENE"] = currentGene
		sampleMap["GENE_ID"] = currentGeneID
		sampleMap["GENELIST"] = currentGeneList
	    sampleMap["PROBE"] = currentProbe
	    sampleMap["FOLD_CHANGE"] = currentFoldChange
	    sampleMap["TEA_P_VALUE"] = currentPValue
		sampleMap["PREFERRED_PVALUE"] =  currentPreferredPValue
				
	    sampleArray.add(sampleMap)
	   
	    // Normalize the values
	    sortedValues.sort()
	    def median = getMedian(sortedValues)
	    def stdDev = getStandardDeviation(values)
		def normalizedValues = getNormalizedValues(values, median, stdDev)
	//	def normalizedValues = values //used to view log2 values in heatmap
			   
	    // Replace log_intensity with the center scale standardization
	    def cNormalizedCount = 0
	    for (val in sampleArray)	{
			def es=val.entrySet()
		    es.each {
				if (it.key != "GENE" && it.key != "GENELIST" && it.key != "GENE_ID" && it.key != "PROBE" && it.key != "FOLD_CHANGE" && it.key != "TEA_P_VALUE" && it.key != "PREFERRED_PVALUE")	{
					it.value = normalizedValues[cNormalizedCount]
					cNormalizedCount++
				}
			}
		}

		///////////////////////////////////////////////////////////////////////////////////
		// Loop thru the full set of keys, and add the keys for each cohort to the ordered full list based on
		// the order of the cohorts as in the semicolon delimted list from the database
		///////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////
		// First, retrieve all N cohorts from analysInfo map		
		def cohorts =  analysisInfo.get("cohorts")
		
		// add the cohort keys to a list
		for (cohortIndex in 1..cohorts.size())  {
			// find cohort with specified order
			def cohort = cohorts.find {  cohorts.get(it.key).get("order") == cohortIndex }
			
			// loop thru unsorted keys and add any keys for this cohort to sorted key list
			fullKeyListUnsorted.each {
				
				def currentKeys = it.split(':')
				def currentCohortKey = currentKeys[0]
				
				if (currentCohortKey == cohort.key)  {
					fullKeyListSorted.add(it)
				}
				
			}
		}				
		///////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////
		
		// Go through and insert any missing values
		def finalArray = []
		def cohortSwitch = 0
		def currentCohort = null
		def previousCohort = null
		def firstMap = true
		for (existingMap in sampleArray)	{
			def newMap = [:]
			for (aKey in fullKeyListSorted )	{
				def keySplit = aKey.split(':')
				
				if (firstMap)  {
					currentCohort = keySplit[0]
					
					// if a switch in cohorts, add keys for cohort
					if ((previousCohort == null)  || (previousCohort != currentCohort))
					{
						def cohortIndex = analysisInfo.get("cohorts").get(currentCohort).get("order")
						def newKey = "COHORT_" + cohortIndex
						def newKeyDesc = "DESC_" + cohortIndex
						def newKeySwitch = "SWITCH_" + cohortIndex
						newMap[newKey] = currentCohort
						newMap[newKeyDesc] = analysisInfo.get("cohorts").get(currentCohort).get("desc")
						newMap[newKeySwitch] = cohortSwitch
					}
					
					previousCohort = currentCohort
					cohortSwitch++
				}
				
				def newValue = existingMap.containsKey(aKey) ? existingMap[aKey] : null
				newMap[aKey] = newValue
			}
			firstMap = false
			
			newMap["GENE"] = existingMap["GENE"]
			newMap["GENE_ID"] = existingMap["GENE_ID"]
			newMap["GENELIST"] = existingMap["GENELIST"]
			newMap["PROBE"] = existingMap["PROBE"]
			newMap["FOLD_CHANGE"] = existingMap["FOLD_CHANGE"]
			newMap["TEA_P_VALUE"] = existingMap["TEA_P_VALUE"]
			newMap["PREFERRED_PVALUE"] = existingMap["PREFERRED_PVALUE"]
			finalArray.add(newMap)
		}

		return finalArray
	}


	
	
	/**
	 * Main method to get the heatmap data to export as a csv file
	 * 
	 * @param analysisID - the analysis ID
	 * @param probesList - pipe delimited list of probe names
	 * @param genes - an optional list of genes 
	 * @param showSigResultsOnly - are we showing only the significant results?
	 * @param pageNumber - What page number are we obtaining data for?  Defaults to 1
	 * 
	 * @return a string containing the heatmap data, comma seperated
	 */	
	def getHeatmapDataForExport2(analysisID, probesList, genes, showSigResultsOnly, pageNumber = 1)	{
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
				
		// retrieve information related to the analysis
		def analysisInfo = getHeatmapAnalysisInfo(analysisID)
	    def fullKeyListUnsorted = []
	    def fullKeyListSorted = []
		def currentCohort = null
		def previousCohort = null
		def firstMap = true
		
		StringBuilder s = new StringBuilder()
		StringBuilder out = new StringBuilder()
		
		def cohorts =  analysisInfo.get("cohorts")
		
		log.info("chorts="+cohorts)
		
		out.append("Cohort ID, Cohort Description\n")
		
		// add the cohort keys to a list
		for (cohortIndex in 1..cohorts.size())  {
			// find cohort with specified order
			def cohort = cohorts.find {  cohorts.get(it.key).get("order") == cohortIndex }
			
			log.info("cohort = "+cohort)
			log.info("cohort key = " +cohort.key)
			log.info("cohort desc= " +cohorts.get(cohort.key).get("desc"))
			
			//out.append(cohort.key +","+cohorts.get(cohort.key).get("desc")+"\n")
			
			out.append(cohort.key +","+cohort.getValue().desc+"\n")

			}
		
		out.append("\n \n")

		
		//output header
		out.append("Probe ID, Gene ID, Gene Symbol, Fold Change Ratio, tea p-value, Preferred p-value")

		// This query grabs all of the values by significance
		s.append("""
				  SELECT DISTINCT
				  (cohort_id ||':'|| assay_id) as key
				  FROM heat_map_results
		""")

		def sqlParams = addHeatmapFilters(s, probesList, genes, showSigResultsOnly, analysisInfo)

		s.append(" order by (cohort_id ||':' || assay_id) desc")
		
		log.info("${s}")
		log.info("${sqlParams}")
	
		
		
		
		def subjects = []
		sql.eachRow(s.toString(), sqlParams, {row->
			subjects.push(row.key)
			out.append(','+row.key) //add the key (cohortID:subjectID:assayID) to the header
			})

		s.setLength(0) //clear the string for re-use
			   
		// This query grabs all of the values by significance
		s.append("""
				  SELECT DISTINCT
				  (cohort_id ||':'|| assay_id) as key,
				  LOG_INTENSITY,
				  PROBE_ID,
				  fold_change_ratio,
          		  max(gene_id || ','||bio_marker_name) as biomarker,
				  tea_normalized_pvalue,
				  PREFERRED_PVALUE
				  FROM heat_map_results
		""")

		sqlParams = addHeatmapFilters(s, probesList, genes, showSigResultsOnly, analysisInfo)

		//group by is to only capture the first gene symbol (otherwise, we have duplicates)
		s.append(" group by (cohort_id ||':' ||  assay_id),  LOG_INTENSITY,PROBE_ID,fold_change_ratio,  tea_normalized_pvalue, PREFERRED_PVALUE")
		s.append(" order by probe_id, (cohort_id ||':' || assay_id) desc")
		
		log.info("${s}")
		log.info("${sqlParams}")

		def i=0 //counter used for subjects array
		def currentProbe = ''
		
		log.info("before data query")
		
		
		sql.eachRow(s.toString(), sqlParams, {row->

			//just finished a row (or first pass in loop); output the next line summary info and reset currentProbe
			if(row.probe_id != currentProbe){
				out.append('\n'+row.probe_id +',' +row.biomarker +',' +row.fold_change_ratio +',' +row.tea_normalized_pvalue +',' +row.PREFERRED_PVALUE); //output the summary level info
				currentProbe = row.probe_id
				i=0 //reset the subject count
			}
				
		//	log.info('-- i=' +i +', row.subject_id = ' +row.key +', subjects[i]='+subjects[i])
			
			//check if the current subject key is the one expected; if so, output the log intensity
			if(row.key == subjects[i]){
				out.append(','+row.log_intensity )
				i=i+1
			}else{			
				while(i<subjects.size()-1 && row.key != subjects[i]){
					//if the row.key value did not match the expected key, then one or more
					//values are missing from the result. Loop through the keys until a match is found
					//and output nulls for any missing value
					
					out.append(',')//null
					i=i+1
		//			log.info('while loop| i=' +i +', row.subject_id = ' +row.key +', subjects[i]='+subjects[i])
				}
				out.append(','+row.log_intensity)
				i=i+1
			}
			
		})
			
	log.info("after data query")
	
	return out.toString()
}
	
	
	
	
	def getHeatmapProbeRankings(analysisID, genes, showSigResultsOnly, pageNumber, int probesPerPage)	{
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
			   			   
		// retrieve information related to the analysis
		def analysisInfo = getHeatmapAnalysisInfo(analysisID)

	    //def geneIds = getGeneListFromKeywords(genes)
	    
		StringBuilder s = new StringBuilder()
		s = new StringBuilder()

		// This query grabs all of the values by significance
		s.append("""
               select probe_id, proberank, gene_name, gene_count from (
                  SELECT probe_id, rownum proberank, gene_name, gene_count from (
	                 SELECT probe_id, tea_normalized_pvalue, fold_change_ratio, gene_name, gene_count FROM (
		                SELECT probe_id, tea_normalized_pvalue, fold_change_ratio, min(bio_marker_name) gene_name, count(distinct bio_marker_name) gene_count FROM heat_map_results
		""")
	   
		def sqlParams = addHeatmapFilters(s, null, genes, showSigResultsOnly, analysisInfo)
		s.append(" group by probe_id, tea_normalized_pvalue, fold_change_ratio ")
		
    	s.append(" order by fold_change_ratio desc, tea_normalized_pvalue ) order by fold_change_ratio desc, tea_normalized_pvalue) ")
    	s.append(" ) where proberank between ? and ? ")
		
		int startIndex = (pageNumber.toInteger()-1)*probesPerPage + 1
		int endIndex = (pageNumber.toInteger())*probesPerPage 
		
		sqlParams.add(startIndex)
		sqlParams.add(endIndex)
		log.info("${s}")
		log.info("${sqlParams}")

		// build the probesList - one object for each probe id		
		JSONObject probesList = new JSONObject()
		
		// build the selectList - one for each probe Id
		JSONObject selectList = new JSONObject()
		
		log.info("before probe ranking query")
		// execute query and save rows (since we need to do this twice, we don't want to execute query twice)
		def results = sql.rows(s.toString(), sqlParams)
		
		def probeMap = [:]

		// loop through return set again to add to probes list with rankings to the probeId and selectDisplay objects 				
		results.each{ row->
			
			probesList.put(row.proberank.toString(), row.probe_id)
			def multiGeneIndicator = ""
			if (row.gene_count > 1)  {
                 multiGeneIndicator = "+"				
			}
			
			def probeSelectDisplay = /${row.gene_name}${multiGeneIndicator} (${row.probe_id})/ 
			selectList.put(row.proberank.toString(), probeSelectDisplay)						
		}
		log.info("after probe ranking query second loop")
		log.info("after probe ranking query")
						
		probeMap.put("probesList", probesList)
		probeMap.put("selectList", selectList)
		return  probeMap
	}

	
	/**
	 * Get the number of probes for a particular analysis and other filter critera
	 *
	 * @return - the number of probes for the analysis and other filter criteria
	 */
	def getNumberProbes(analysisID, genes, showSigResultsOnly)	{
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
							  
		// retrieve information related to the analysis
		def analysisInfo = getHeatmapAnalysisInfo(analysisID)

		def numberProbes
		
		StringBuilder s = new StringBuilder()
		s = new StringBuilder()

		// This query grabs all of the values by significance
		s.append("""
				  SELECT count(distinct probe_id) probecount FROM heat_map_results
		""")
	   
		def sqlParams = addHeatmapFilters(s, null, genes, showSigResultsOnly, analysisInfo)

		log.debug("${s}")
		log.debug("${sqlParams}")

		def results = sql.rows(s.toString(), sqlParams)
		
		// loop through return set again to add to probes list with rankings to the probeId and selectDisplay objects
		results.each{ row->
			
			numberProbes = row.probecount
		}
						
		return  numberProbes
	}

	   
	/**
	 * Obtains the median on an array of sorted values
	 *
	 * @param sortedValues - the array of sorted values
	 *
	 * @return - the median (midpoint) in the array
	 */
	def getMedian(sortedValues)	{		
		def numberItems = sortedValues.size()
		def midNumber = (int)(numberItems/2)
		return numberItems %2 != 0 ? sortedValues[midNumber] : (sortedValues[midNumber] + sortedValues[midNumber-1])/2		
	}

	
	/**
	* Obtains the mean on an array of values
	*
	* @param values - the array of values
	*
	* @return - the median (midpoint) in the array
	*/
   def getMean(values)	{
	   def numberItems = values.size()
	   def sum = values.sum()
	   return sum / numberItems
   }
   

   

    /**
	 * Obtains the standard deviation based on a sample (not population) on an array of values
	 * 
	 * @param originalValues - the array of original values
	 *
	 * @return - the standard deviation
	 */
	def getStandardDeviation(originalValues)	{
		def numberItems = originalValues.size()
			   
		// Find the standard deviation
		def sum = originalValues.sum()
		def mean = sum / numberItems
		def sqDiffValues = originalValues.collect {
			(it-mean)*(it-mean)
		}
		def sumSquares = sqDiffValues.sum()
		def variance = sumSquares / (numberItems - 1)
		return Math.sqrt(variance)
	}

	/**
	* Obtains the standard error for an array of values (std dev of list of values / sqrt of size of list)
	*
	* @param originalValues - the array of original values
	*
	* @return - the standard error
	*/
   def getStandardError(originalValues)	{
	   def numberItems = originalValues.size()

	   def stdDev = getStandardDeviation(originalValues)
       
	   return stdDev / Math.sqrt(numberItems)
	   
   }

	   
    /**
	 * Implementation of the normalization method proposed by Xiaoying
	 *
	 * @param originalValues - The array of original log intensity values
	 * @param median - The midpoint of the original value array
	 * @param stdDev - The standard deviation of the original value array
	 *
	 * @return - The array of normalized (-1 to 1) values
	 */
	def getNormalizedValues(originalValues, median, stdDev)	{
		// Subtract each value with the median value
	    def newValues = originalValues.collect {it - median }

	    // Center Scale standardization
	    def cScaleValues = newValues.collect {it / stdDev}
	   
	    // Normalization to -1 to 1
	    def upperBound = cScaleValues.max()
	    def lowerBound = cScaleValues.min()
	    def midRange = (upperBound + lowerBound)/2
	    def normalValues = cScaleValues.collect {it - midRange}
	    def range = upperBound - lowerBound
	    def normalizedValues = normalValues.collect {it / (range/2)}

	    return normalizedValues
	}
	
	/**
	 * Get the children of a parent node 
	 * 
	 * 
	 */
	def getSearchTaxonomyChildren(parentid) {
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)

		String s = """select term_id, term_name from SEARCHAPP.search_taxonomy st, SEARCHAPP.search_taxonomy_rels str
						where st.term_id=str.child_id
						and str.parent_id=${parentid}"""
		
		def rows = sql.rows(s)
		log.info("after query in getChildren")
		// store the sql results into a map containing one map for each probe (which contains a list of genes)
		def children=[]
		rows.each {row->
			def result=[:]
			result.put('id', row.term_id)
			result.put('name', row.term_name)	
			children.push(result)
		}
		log.info(children)
		return children
	}	
	
	/**
	 * Get the parents of a child node
	 *
	 *
	 */
	def getSearchTaxonomyParents(childid) {
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)

		String s = """select term_id, term_name from SEARCHAPP.search_taxonomy st, SEARCHAPP.search_taxonomy_rels str
						where st.term_id=str.parent_id
						and str.child_id=${childid}"""
		
		def rows = sql.rows(s)
		log.info("after query in parent")
		// store the sql results into a map containing one map for each probe (which contains a list of genes)
		def parents=[]
		rows.each {row->
			def result=[:]
			result.put('id', row.term_id)
			result.put('name', row.term_name)
			parents.push(result)
		}
		log.info(parents)
		return parents
	}
	
	/*
	 * Get first parent from d
	 */
	def getFirstParentId(stid)
	{
		def parentid=1;
		def parents=getSearchTaxonomyParents(stid) //go get the parents
		if(!(parents.size()==0)) //if we found some parents
			{
				parentid=parents[0]["id"]
			}
		return parentid;
	}
	
	
	/**
	 * Get the data for home page pie charts
	 *
	 *
	 */
	def getPieChartData(categoryid, drilldownid, drillback, charttype) {
		def querydrilldownid=drilldownid
		def finalDrilldownAndClause=""
		def categoryAndClause=""
		
		//if we got a category id use it
		if(categoryid)
		{
			categoryAndClause="and st2.term_id=${categoryid}";
		}	
		
		//if its a drill up then find the parent instead
		def parentid=getFirstParentId(drilldownid);		
		if(drillback==true)
		{		
			if(parentid!=1)
			{
				drilldownid=parentid //if not at top then go back one
				querydrilldownid=drilldownid
			}
		}
		else
		{
			def children=getSearchTaxonomyChildren(drilldownid)
			if (children.size()==0)
			{
			finalDrilldownAndClause=" and st.term_id=${drilldownid}"
			querydrilldownid=parentid
			}
		}		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)

		String s ="""
		select st.term_name, st.term_id, count(distinct baa.study_id) as studies, count(distinct baa.bio_assay_analysis_id) as analyses
		from BIOMART.bio_analysis_attribute_lineage baal, bio_analysis_attribute baa, search_taxonomy_rels str, search_taxonomy st,
		BIOMART.bio_analysis_attribute_lineage baal2, bio_analysis_attribute baa2, search_taxonomy_rels str2, search_taxonomy st2
		where baal.ancestor_term_id=str.child_id
		and str.parent_id=${querydrilldownid}   -- term id for Disease
		and baa.bio_analysis_attribute_id=baal.bio_analysis_attribute_id                              
		and str.child_id=st.term_id
		and baal2.ancestor_term_id=str2.child_id
		--and str2.parent_id=2   -- term id for Therapeutic Areas
		and baa2.bio_analysis_attribute_id=baal2.bio_analysis_attribute_id                              
		and str2.child_id=st2.term_id
		and baa.bio_assay_analysis_id=baa2.bio_assay_analysis_id
		${finalDrilldownAndClause}
		${categoryAndClause}
		group by st.term_name, st.term_id"""
			
		def rows = sql.rows(s)
		log.info("after query in getPieChartData")
		// store the sql results into a map containing one map for each probe (which contains a list of genes)
		def chart=[:]
		def data=[]
		rows.each {row->
			def result=[:]
			result.put('id', row.term_id)
			result.put('name', row.term_name)
			if(charttype.toString().equalsIgnoreCase("studies"))
			{
			result.put('value', row.studies)
			}
			else
			{
			result.put('value', row.analyses)
			}
			data.push(result)
		}
		chart.put("ddid", drilldownid)
		chart.put("data", data)	
		log.info(chart)
		return chart
	}

	
	/*
	* Get the categories that have data for the home page pie charts
	*
	*
	*/
   def getCategoriesWithData(catparentid, subcatid) {
	 
	   groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)

	   String s ="""
		select st2.term_name, st2.term_id
		from BIOMART.bio_analysis_attribute_lineage baal, bio_analysis_attribute baa, search_taxonomy_rels str, search_taxonomy st,
		BIOMART.bio_analysis_attribute_lineage baal2, bio_analysis_attribute baa2, search_taxonomy_rels str2, search_taxonomy st2
		where baal.ancestor_term_id=str.child_id
		and str.parent_id=${subcatid}   -- term id for Disease
		and baa.bio_analysis_attribute_id=baal.bio_analysis_attribute_id                              
		and str.child_id=st.term_id
		and baal2.ancestor_term_id=str2.child_id
		and str2.parent_id=${catparentid}   -- term id for Therapeutic Areas
		and baa2.bio_analysis_attribute_id=baal2.bio_analysis_attribute_id                              
		and str2.child_id=st2.term_id
		and baa.bio_assay_analysis_id=baa2.bio_assay_analysis_id
		group by st2.term_name, st2.term_id"""
		   
	   def rows = sql.rows(s)
	   log.info("after query in getCategoriesWithData")
	   // store the sql results into a map containing one map for each probe (which contains a list of genes)
	   def categories=[]
	   rows.each {row->
		   def result=[:]
		   result.put('id', row.term_id)
		   result.put('name', row.term_name)
		   categories.push(result)
	   }
	   log.info(categories)
	   return categories
   
	   
   }
   
   
   def getTopGenesByFoldChange(analysisID)	{
	   groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
							 

    String s ="""
	select * from (
				select distinct(bio_marker_id), bio_marker_name, fold_change_ratio
				from biomart.heat_map_results
				where bio_assay_analysis_id = ${analysisID}
				order by abs(fold_change_ratio) desc)
				where rownum <= 20"""
	   
	   log.debug("${s}")
   
		   def rows = sql.rows(s)
		   
		   def topGenes=[]
		   rows.each {row->
			   def result=[:]
			   result.put('bio_marker_id', row.bio_marker_id)
		   result.put('bio_marker_name', row.bio_marker_name)
		   result.put('fold_change_ratio', row.fold_change_ratio)
		   topGenes.push(result)
	   }
   
	   return  topGenes
   }

   def getCrossTrialBioMarkerSummary(search_keyword, analysisList)	{
	   groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
							 
	String s ="""

		select distinct bio_assay_analysis_id, bio_marker_id, bio_marker_name, 
		probe_id, fold_change_ratio, tea_normalized_pvalue, preferred_pvalue
		from BIOMART.heat_map_results
		where bio_marker_id in (select distinct bmv.asso_bio_marker_id
		from BIOMART.bio_marker_correl_mv bmv
		where bmv.bio_marker_id = (select sk.bio_data_id
		from searchapp.search_keyword sk
		where sk.search_keyword_id = ${search_keyword}))
		and bio_assay_analysis_id in (${analysisList})"""
	   
	   log.debug("${s}")
   
		   def rows = sql.rows(s)
		   
		   def resultSet=[]
		   rows.each {row->
			   def result=[:]
			   result.put('bio_assay_analysis_id', row.bio_assay_analysis_id)
			   result.put('bio_marker_id', row.bio_marker_id)
			   result.put('bio_marker_name', row.bio_marker_name)
			   
			   result.put('probe_id', row.probe_id)
			   result.put('fold_change_ratio', row.fold_change_ratio)
			   result.put('tea_normalized_pvalue', row.tea_normalized_pvalue)
			   result.put('preferred_pvalue', row.preferred_pvalue)
		   resultSet.push(result)
	   }
   
	   return  resultSet
   }
   	   
	   
   
	  	  
}