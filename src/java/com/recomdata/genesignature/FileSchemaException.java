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
  

package com.recomdata.genesignature;

import java.lang.RuntimeException;
import java.lang.Throwable;
import java.util.Collection;
import java.util.Iterator;

/**
 * Special exception class for invalid gene signature upload files
 * $Id: FileSchemaException.java 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class FileSchemaException extends RuntimeException {

	/**
	 * required for serialization
	 */
	private static final long serialVersionUID = 7745871701505432136L;
	
	public FileSchemaException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileSchemaException(String message) {
		super(message);
	}	

	/**
	 * helper function for constructing an error messages for invalid gene symbols that could not be 
	 * mapped to the warehouse schema
	 * @param invalidGenes
	 * @return
	 */
	public static void ThrowInvalidGenesFileSchemaException(Collection<String> invalidGenes) throws FileSchemaException {				
		StringBuffer msg = new StringBuffer("The following gene symbols could not be mapped, please fix or remove:<br><br>");
		msg.append("<ul style='list-style-type: disc; list-style-position: inside;'>");
		Iterator<String> it = (Iterator<String>) invalidGenes.iterator();
		while(it.hasNext()) {
			msg.append("<li>" + it.next().toString() + "</li>");
		}
		msg.append("<ul>");
		throw new FileSchemaException(msg.toString());
	}

}
