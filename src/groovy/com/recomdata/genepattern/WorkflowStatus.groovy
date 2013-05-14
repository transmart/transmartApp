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
  


package com.recomdata.genepattern

import com.recomdata.genepattern.JobStatus;;
import org.json.JSONObject;

class WorkflowStatus {
	def jobStatusList = new ArrayList();
	def currentStatus = "Starting"; /* Starting, Running, Cancelled, Completed*/
	JSONObject result = null;
	int currentStatusIndex = 1;
	
	// repeat count to help manage dup javascript firings
	int rpCount=0;
	
	/**
	 * update object if it's in set, add if not exists
	 */
	def addJobStatus(status){
			int sindex = jobStatusList.indexOf(status);
			if(sindex>-1){
				def s = jobStatusList.get(sindex);
						s.status = status.status;
						s.message = status.message;
						s.gpJobId = status.gpJobId;
						s.totalRecord = status.totalRecord;
					}
				
			else{
			jobStatusList.add(status);
			}
	}
	
	def addNewJob(String sname){
		jobStatusList.add(new JobStatus(name:sname, status:"Q"));
	}
	
	
	def setCurrentJobStatus(status){
		// set previous job to be completed..
		int sindex = jobStatusList.indexOf(status);
		if(sindex>-1){
			for(int i = 0; i<sindex; i++){
				jobStatusList[i].setComplete();
			}
		}	
		addJobStatus(status);
		// find running index
		int si = 0;
		for(s in jobStatusList){
			si++;
			if(s.isRunning()){
				currentStatusIndex = si;
				break;
			}
		}
		currentStatus = "Running";
		
	}
	
	def setCancelled(){
		this.currentStatus = "Cancelled";
	}
	def isCancelled(){
		return currentStatus =="Cancelled";
	}
	
	def isCompleted(){
		return currentStatus =="Completed";
	}
	def setCompleted(){
		currentStatus = "Completed";
	}

}
