


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
 */
public class DBHelper {
    public static String ClobToString(Clob cl) throws IOException, SQLException {
        if (cl == null)
            return "";

        StringBuffer strOut = new StringBuffer();
        String aux;

        // We access to stream, as this way we don't have to use the CLOB.length() which is slower...
        BufferedReader br = new BufferedReader(cl.getCharacterStream());

        while ((aux = br.readLine()) != null)
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
