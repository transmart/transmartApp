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




class SubjectService {

    boolean transactional = true
	
	def dataSource

    def serviceMethod() {
    }
	
	/**
	 * 
	 * @param subjectId
	 * @return
	 */
	def getDemographics(subjectId){
		Map demographics = new HashMap()
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		String demographicsQuery = """select ofa.concept_cd, cdim.concept_cd, cdim.concept_path, ofa.nval_num, ofa.tval_char, ofa.valtype_cd
									from concept_dimension cdim
										join observation_fact ofa on ofa.concept_cd=cdim.concept_cd
									where ofa.patient_num=?
										and cdim.concept_path like '%Demographics%'""";
		
		sql.eachRow(demographicsQuery, [subjectId], {row ->
			def value	
			def valTypeCd = row.valtype_cd
				if(valTypeCd=='T'){
					value=row.tval_char
				}else if(valTypeCd=='N'){
					value=row.nval_num
				}
				
			String conceptPath = row.concept_path
			String[] conceptArray = conceptPath.split("Demographics")
			String [] labelArray = conceptArray[1].split("\\\\")
			String label = labelArray[1]
			
			if(value!='E'){
				demographics.put(label, value)
			}
		})
		
		return demographics
	}
	
	/**
	 * 
	 * @param selectedObservations
	 * @param subjectId
	 * @return
	 */
/*	def getSelectedObservationValues(selectedObservations, subjectId){
		Map observations = getAvailableObservationsMap(subjectId)
		Map retObservations = new TreeMap()
		
		if(selectedObservations.length()>0){
			def selectedObservationArray = selectedObservations.split("\\|")
			
			selectedObservationArray.each(){selectedObservation->
				retObservations.put(selectedObservation, observations.get(selectedObservation))	
			}
		}

		return retObservations
	}*/
	/**
	 *
	 * @param selectedObservations
	 * @param subjectId
     * @param isTimeline
	 * @return
	 */
    def getAllSelectedObservationValues(selectedObservations, subjectId,isTimeline){
        Map observations = getAvailableObservationsMap(subjectId,isTimeline)
		List encounterList = getAllEncounterKeys(observations)
		Map retObservations = new TreeMap()	
		def each = encounterList.each {encounterNum ->
			Map encounter = observations.get(encounterNum)
			Map selected = getSelectedObservations(selectedObservations, encounter)
			if(selected.size() > 0){
				//retObservations.put(encounterNum, "test")
				retObservations.put(encounterNum, selected)
			}			
		}
		
	return retObservations
	}
		/**
		 *
		 * @param selectedObservations
		 * @param subjectId
		 * @return
		 */
		def getSelectedObservations(selectedObservations, observations){
			Map retObservations = new TreeMap()
				if(observations.size()>0 && selectedObservations.length()>0  ){
				def selectedObservationArray = selectedObservations.split("\\|")
				selectedObservationArray.each(){selectedObservation->
					retObservations.put(selectedObservation, observations.get(selectedObservation))
				}
			}

			return retObservations
		}
	/**
	 *
	 * @param Get Observation Values 
	 * @param subjectId
     * @param isTimeline
	 * @return
	 */
    def getAvaiableObservationMap(subjectId,encounterNum,isTimeline){
		def observations= new TreeMap();
        Map observationsMap = getAvailableObservationsMap(subjectId,isTimeline)
		////
		if(observationsMap.containsKey(encounterNum)) {
			observations = observationsMap.get(encounterNum)
		}
		return observations
	}
	/**
	 *  Get Observation Keys 
	 *  @param subjectId
     *  @param isTimeline
	 *  @return 
	 */
    def getAllObservationKeys(subjectId,isTimeline){
		def observationKeys= new ArrayList();
        def observationsMap = getAvailableObservationsMap(subjectId,isTimeline)
		////
		observationsMap.each { encounterKey, observations ->
            println("-EncounterKey: ${encounterKey}");
            println("-Observations: ${observations }");
		  observations.each {key, value ->
                  println("-observationsKey: ${key}, -observationsValue: ${value}");
				  String keyString = key
                  if(!observationKeys.contains(keyString)){  //check if value already does not exists, than add
                      observationKeys.add(keyString)
                  }
				};}; 
		////
		return observationKeys
	}
	/**
	 *  Get Encounter Indexes
	 *  @param observationsMap
	 *  @return
	 */
	def getAllEncounterIndexes(observationsMap){
		def encounterIndexList= new ArrayList();
		////
		observationsMap.eachWithIndex() { encounterKey, encounterIndex ->
			println("EncounterKey: ${encounterKey}");
			encounterIndexList.add(encounterIndex)
				};
		////
		return encounterIndexList
	}
	/**
	 *  Get Encounter Keys
	 *  @param observationsMap
	 *  @return
	 */
	def getAllEncounterKeys(observationsMap){
		List encounterKeyList= new ArrayList();
		////
		observationsMap.each { encounterKey, observations ->
			println("EncounterKey: ${encounterKey}");
			println("Observations: ${observations }");
		 	encounterKeyList.add(encounterKey)
				};
		////
		return encounterKeyList
	}
	/**
	 *  has Encounter 
	 *  @param observationsMap
	 *  @return
	 */
	def hasEncounters(observationsMap){
		Boolean flag = false
		List encounterList = getAllEncounterKeys(observationsMap)
		if(encounterList.size()>0){
			flag = true
		}
		return flag
	}
	/**
	 * 
	 * @param subjectId
     * @param isTimeline
	 * @return
	 */
    def getAvailableObservations(subjectId,isTimeline){
        def observationKeys = getAllObservationKeys(subjectId,isTimeline)
				
		return observationKeys
	}
	
	/**
	 *
	 * @param trial
	 * @return
	 */
	def getStudyName(trial){
		def study
		String sqlQuery = """select c_Name from i2b2 i where substr(i.c_comment,7,length(i.c_comment)) like '""" + trial + """' and  i.c_hlevel = 1""";
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		
		sql.eachRow(sqlQuery, {row ->
			if (study == null || study == ""){
				study = row.c_Name
			}else
				{
					study = study + "/" + row.c_Name
				}					
		})		
		return study.replace("_", " ")
	}
	
	
	/**
	 * 
	 * @param subjectId
     * @param isTimeline
	 * @return
	 */
    def getAvailableObservationsMap( subjectId,isTimeline){
		Map observationsPerEncounterMap = new TreeMap()
        print(isTimeline)

        def visitNameFlag =""
        if(isTimeline){
            visitNameFlag = "NOT"
        }
        String otherObservationsQuery = """SELECT ofa.concept_cd,  cdim.concept_cd,  cdim.concept_path,  ofa.encounter_num,  ofa.nval_num,  ofa.tval_char,  ofa.valtype_cd,  vdim.visit_name
										 FROM observation_fact ofa
										 JOIN concept_dimension cdim
										 ON ofa.concept_cd=cdim.concept_cd
										 LEFT JOIN deapp.de_concept_visit vdim
										 ON vdim.concept_cd = ofa.concept_cd
										 WHERE ofa.patient_num = ?
										 AND cdim.concept_path NOT LIKE '%Demographics%'
										 AND vdim.visit_name IS """+visitNameFlag+""" NULL
										 and ofa.valtype_cd != 'D'
										 AND ofa.concept_cd <> 'SECURITY'
										 GROUP BY ofa.encounter_num,  ofa.concept_cd,  cdim.concept_cd,  cdim.concept_path,  ofa.nval_num,  ofa.tval_char,  ofa.valtype_cd,  vdim.visit_name
										 order by ofa.encounter_num"""

        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		
		sql.eachRow(otherObservationsQuery, [subjectId], {row ->
			def observationLabel=""
			def observationValue
			def valTypeCd = row.valtype_cd
			Integer encounterNum = row.encounter_num
			Map observations = new TreeMap()
			//place all subject level concepts 
			if(encounterNum < 0)
				encounterNum = 0;
			//check if already exists			
			if(observationsPerEncounterMap.containsKey(encounterNum)) {
				observations = observationsPerEncounterMap.get(encounterNum)
			} 
			
			if(valTypeCd=='T'){
				
				observationValue=row.tval_char
				
				String conceptPath = row.concept_path
				String[] conceptArray = conceptPath.split("\\\\")
				
				for(int i = 4; i<conceptArray.length-1; i++){
					observationLabel+="\\"
					observationLabel+=conceptArray[i]
				}
				if(observationValue!='E'){
					observations.put(observationLabel, observationValue)					
				}
			}else if(valTypeCd=='N'){
				observationValue=row.nval_num
				
				String conceptPath = row.concept_path
				String[] conceptArray = conceptPath.split("\\\\")
				
				for(int i = 4; i<conceptArray.length; i++){
					observationLabel+="\\"
					observationLabel+=conceptArray[i]
				}
				
				observations.put(observationLabel, observationValue)
			}
			observationsPerEncounterMap.put(encounterNum, observations)
		})
		
		return observationsPerEncounterMap
	}
	
	def getTimedDataPoints(selectedTimedObservation, subjectId){
		
	}
}
