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
  

import java.util.LinkedHashSet;

/**
 * 
 * @author mkapoor
 *
 */
class CohortInformation {
	
	public static final int PLATFORMS_TYPE=1;
	public static final int TRIALS_TYPE=2;
	public static final int TIMEPOINTS_TYPE=3;
	public static final int SAMPLES_TYPE=4;
	public static final int GPL_TYPE=5;
	public static final int TISSUE_TYPE=6;
	public static final int RBM_PANEL_TYPE=7;
	
	def platforms=new ArrayList();
	def trials=new ArrayList();
	def timepoints=new ArrayList();
	def samples=new ArrayList();
	def gpls=new ArrayList();
	def tissues=new ArrayList();
	def rbmpanels=new ArrayList();
	
	def getAllTrials = 
	{
			StringBuilder strng = new StringBuilder();
    		Iterator itr = trials.iterator();
    		if(itr.hasNext()){
    			strng.append(itr.next());
    		}	
    		while (itr.hasNext()){
    			strng.append(",").append(itr.next());
    		}	
			
			return strng.toString();
	}
}
