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
  



import grails.converters.JSON;

class SubjectController {

	def subjectService
	
    def index = { }
	
	def profile = {
		def subjectId = params["subjectId"]
		def patientId = params["patientId"]
		def trial = params["trial"]
		
		def demographics = subjectService.getDemographics(subjectId)

        def observations = subjectService.getAvailableObservations(subjectId,false)

        def timelineObservations = subjectService.getAvailableObservations(subjectId,true)
				
		def studyName = subjectService.getStudyName(trial)
				
		render (view:"profile", model:[demographics:demographics, observations:observations, subjectId:subjectId, patientId:patientId, trial:trial, study:studyName,encounter:false])
	}
	
	def observations={
		def subjectId = params["subjectId"]
		def selectedObservations = params["selectedObservationValues"]
        def selectedObservationValues = subjectService.getAllSelectedObservationValues(selectedObservations, subjectId,false)
		def encounter = subjectService.hasEncounters(selectedObservationValues)
		render (template:"subjectDetails", model:[details: selectedObservationValues, title:'',encounter:encounter])
	}
	
	def timepointData={
		def subjectId = params["subjectId"]
		def selectedTimedObservation = params["selectedTimedObservation"]
        def selectedTimedObservationValues = subjectService.getAllSelectedObservationValues(selectedObservations, subjectId,true)
        def timedEncounter = subjectService.hasEncounters(selectedTimedObservation)
		
		def timedDataPoints = subjectService.getTimedDataPoints(selectedTimedObservation, subjectId)
		
		render timedDataPoints as JSON
        //You will need to add a new render similar to the above observations
        //render (template:"subjectDetails", model:[details: selectedTimedObservation, title:'',encounter:timedEncounter])
	}
}
