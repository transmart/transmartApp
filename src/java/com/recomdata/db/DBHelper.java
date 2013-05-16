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
package com.recomdata.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.List;

/**
 * @author JIsikoff
 *
 */
public class DBHelper {
	public static String ClobToString(Clob cl) throws IOException, SQLException 
    {
      if (cl == null) 
        return  "";
          
      StringBuffer strOut = new StringBuffer();
      String aux;
            
	// We access to stream, as this way we don't have to use the CLOB.length() which is slower...
	BufferedReader br = new BufferedReader(cl.getCharacterStream());

      while ((aux=br.readLine())!=null)
             strOut.append(aux);

      return strOut.toString();
    }
	
	public static String listToInString(List list) {
		if (list == null || list.size() == 0) return null;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			Object obj = list.get(i);
			if (obj != null && obj.toString().length() != 0) {
				if (i != 0) buf.append(", ");
				buf.append("'" + obj.toString() + "'");
			}
		}
		return buf.toString();
	}
}
