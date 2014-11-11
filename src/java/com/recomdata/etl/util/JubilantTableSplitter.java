


/**
 * $Id: JubilantTableSplitter.java 9178 2011-08-24 13:50:06Z mmcduffie $
 */
package com.recomdata.etl.util;

import com.recomdata.etl.db.MssqlConnectImpl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Helper class to normalize the columns in the Jubilant tables
 *
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class JubilantTableSplitter {
    /**
     * @param args not used at this time
     */
    public static void main(String[] args) {
        System.out.println(normalizeColumnNames());
    }

    /**
     * Main method to replace whitespace, dashes and check lengths of column names
     * <p/>
     * return String a script to run renaming the necessary columns
     */
    public static String normalizeColumnNames() {
        // Original is shown below, not sure how this worked since no settings for the connection were made
        // Using the StrangeLoveConnect to unit test this functionality
        // Connect local = Connect.createLocalConnect();
        MssqlConnectImpl local = MssqlConnectImpl.createStrangeLoveConnect();

        Statement s = null;
        ResultSet rs = null;
        StringBuilder retValue = new StringBuilder();

        try {
            s = local.getConnection().createStatement();
            String mainSQL = "SELECT TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE ";
            String chkSpace = "COLUMN_NAME LIKE '% %'";
            String chkDash = "COLUMN_NAME LIKE '%-%'";
            String chkLen = "LEN(COLUMN_NAME)>=30";
            rs = s.executeQuery(mainSQL + chkSpace);
            while (rs.next()) {
                String col = rs.getString(2);
                String newCol = col.replaceAll(" ", "");
                retValue.append(generateSp(rs.getString(1), col, newCol));
            }
            rs.close();
            rs = s.executeQuery(mainSQL + chkDash);
            while (rs.next()) {
                String col = rs.getString(2);
                String newCol = col.replaceAll("-", "");
                retValue.append(generateSp(rs.getString(1), col, newCol));
            }
            rs.close();
            rs = s.executeQuery(mainSQL + chkLen);
            while (rs.next()) {
                String col = rs.getString(2);
                String newCol = col.replaceAll("", "");   // What are we doing here?
                retValue.append(generateSp(rs.getString(1), col, newCol));
            }
            rs.close();
            s.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (s != null) {
                    s.close();
                }
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
            local.closeConnection();
        }
        return retValue.toString();
    }

    /**
     * Helper method to generate the string to execute sp_rename
     *
     * @param tableName the table where the columns will be renamed
     * @param oldCol    the name of the existing column to change
     * @param newCol    the new name for the column
     * @return a String that will be appended to the list
     */
    private static String generateSp(String tableName, String oldCol, String newCol) {
        StringBuilder ret = new StringBuilder("EXEC sp_rename '");
        ret.append(tableName);
        ret.append(".");
        ret.append(oldCol);
        ret.append("', '");
        ret.append(newCol);
        ret.append("', 'COLUMN';");
        ret.append("\n");

        return ret.toString();
    }
}
