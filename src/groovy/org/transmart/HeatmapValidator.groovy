package org.transmart
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
 */



/**
 * @author JIsikoff
 *
 */
public class HeatmapValidator{

	def platforms=new LinkedHashSet();
	def timepoints=new LinkedHashSet();
	def timepointLabels=new LinkedHashSet();
	def samples=new LinkedHashSet();
	def sampleLabels=new LinkedHashSet ();
	def gpls=new LinkedHashSet();
	def gplLabels=new LinkedHashSet();
	def tissues=new LinkedHashSet();
	def tissueLabels=new LinkedHashSet();
	def rbmpanels= new LinkedHashSet();
	def rbmpanelsLabels=new LinkedHashSet();
	def msg="";
	def valid=false;
	
	def validate =
	{
			msg="";
			def p=platforms.size();
			def t=timepoints.size();
			def s=samples.size();

			//Check platforms
			if(p>1)
			{
				msg+="Too many platforms found. "
				valid=false;
			}
			else if(p<1)
			{
				msg+="No platforms found. "
				valid=false;
			}

			/*
			//check timepoints
			if(t>1)
			{
				msg+="Too many timepoints found. "
				valid=false;
			}
			else if(t<1)
			{
				msg+="No timepoints found. "
				valid=false;
			}
			*/
			
			if(p==1 /* && t==1 */)
			{
				valid=true; //found one timepoint and one platform
			}
			return valid;
			
			
	}
	def getFirstPlatform=
	{   
    		Iterator itr = platforms.iterator();
    		if(itr.hasNext()){
    			return itr.next();
    			}	
	}
	
	def getFirstPlatformLabel={
    		Iterator itr = platforms.iterator();
    		if(itr.hasNext()){
    			String platform = (String)itr.next();
    			return("MRNA_AFFYMETRIX".equals(platform)?"MRNA":platform);
    			}
	}
	
	def getFirstTimepoint=
	{   
    		Iterator itr = timepoints.iterator();
    		if(itr.hasNext()){
    			return itr.next();
    			}	
	}
	def getAllTimepoints = 
	{
			StringBuilder strng = new StringBuilder();
    		Iterator itr = timepoints.iterator();
    		if(itr.hasNext()){
    			strng.append(itr.next());
    		}	
    		while (itr.hasNext()){
    			strng.append(",").append(itr.next());
    		}	
			
			return strng.toString();
	}
	
	def getAllTimepointLabels = 
	{
			StringBuilder strng = new StringBuilder();
    		Iterator itr = timepointLabels.iterator();
    		if(itr.hasNext()){
    			strng.append(itr.next());
    		}	
    		while (itr.hasNext()){
    			strng.append(",").append(itr.next());
    		}	
			
			return strng.toString();
	}
	
	def getAllSamples = 
	{
			StringBuilder strng = new StringBuilder();
    		Iterator itr = samples.iterator();
    		if(itr.hasNext()){
    			strng.append(itr.next());
    		}	
    		while (itr.hasNext()){
    			strng.append(",").append(itr.next());
    		}	
			
			return strng.toString();
	}
	
	def getAllSampleLabels = 
	{
			StringBuilder strng = new StringBuilder();
    		Iterator itr = sampleLabels.iterator();
    		if(itr.hasNext()){
    			strng.append(itr.next());
    		}	
    		while (itr.hasNext()){
    			strng.append(",").append(itr.next());
    		}	
			
			return strng.toString();
	}
	
	def getAll(field){
		Iterator itr
		if(field=='gplLabels'){
			itr = gplLabels.iterator();
		}else if(field=='gpls'){
			itr = gpls.iterator();
		}else if(field=='tissueLabels'){
			itr = tissueLabels.iterator();
		}else if(field=='tissues'){
			itr = tissues.iterator();
		}else if(field=='rbmpanels'){
			itr = rbmpanels.iterator();
		}else if(field=='rbmpanelsLabels'){
			itr = rbmpanelsLabels.iterator();
		}
		
		StringBuilder strng = new StringBuilder();
		
		if(itr.hasNext()){
			strng.append(itr.next());
		}	
		while (itr.hasNext()){
			strng.append(",").append(itr.next());
		}	
		
		return strng.toString();
	}
}

