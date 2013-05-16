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
  

package com.recomdata.export;
import java.util.*;
import java.io.*;
import java.lang.System;
import java.lang.String;

/* A simple class to compare subject id strings for sorting */

public class subjectComparator implements Comparator {

	public int compare(Object obj1, Object obj2) { 
		
		Integer g1 = Integer.parseInt( ((String) obj1).substring(1,2) );
		Integer g2 = Integer.parseInt( ((String) obj2).substring(1,2) );
		
		Integer id1 = Integer.parseInt( ((String) obj1).substring(3));
		Integer id2 = Integer.parseInt( ((String) obj2).substring(3));
		
		if (g1 > g2) {
			return 1;
		} else if (g1 < g2) {
			return -1;
		} else if (id1 > id2) {
			return 1;
		} else if (id1 < id2) {
			return -1;
		} else {
			return 0;
		}
	}
}