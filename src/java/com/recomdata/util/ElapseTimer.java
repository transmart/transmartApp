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
 * $Id: ElapseTimer.java 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
package com.recomdata.util;

import org.apache.log4j.Logger;

public class ElapseTimer {

    static Logger log = Logger.getLogger(ElapseTimer.class);
    
	private Long startTime;

	public ElapseTimer(){
		startTime =  System.currentTimeMillis();
	}

	public void reset() {
		startTime = System.currentTimeMillis();
	}

	public long elapsed(boolean reset){
		Long e = System.currentTimeMillis()-startTime;
		if(reset)
			reset();
		return e;
	}

	public long elapsed(){
		return elapsed(true);
	}

	public void logElapsed(String tag, boolean reset){
		log.info(tag+":"+(System.currentTimeMillis()-startTime)+" ms");
		if(reset)
			reset();
	}
	public String elapsedTime(){
		 long time = elapsed(true) / 1000;
		 int seconds = (int)(time % 60);
		 int minutes = (int)((time % 3600) / 60);
		 int hours = (int)(time / 3600);
		 StringBuilder s = new StringBuilder();
		 if(hours>0){
			 s.append(hours).append(" hour(s) ");
		 }
		 if(minutes>0){
			 s.append(minutes).append(" minute(s) ");

		 }
		 if(seconds>0){
			 s.append(seconds).append(" second(s)");
		 }
		 return s.toString();
	}
}
