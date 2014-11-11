


package com.recomdata.etl.db;

public class OracleConnectImpl implements DBConnect {

    private java.sql.Connection con = null;
    private final String url = "jdbc:oracle:thin:@";
    private String serverName = "localhost";
    private String portNumber = "1521";
    private String databaseName = "xe";
    private String userName = "biomart_wz";
    private String password = "biomart_wz";
    // Informs the driver to use server a side-cursor,
    // which permits more than one active statement
    // on a connection.
    private final String selectMethod = "cursor";

    public static OracleConnectImpl createHost2SearchAppConnect() {
        return new OracleConnectImpl("host", "sid", "userid", "password");
    }

    public static OracleConnectImpl createAWSDevSearchAppConnect() {
        return new OracleConnectImpl("localhost", "DW1", "1520", "searchapp", "searchapp");
    }

    public static OracleConnectImpl createDW1BiomartWZConnect() {
        return new OracleConnectImpl("localhost", "DW1", "1520", "biomart_wz", "biomart_wz");
    }

    public static OracleConnectImpl createDW1BiomartCTConnect() {
        return new OracleConnectImpl("localhost", "DW1", "1520", "control", "control");
    }

    public static OracleConnectImpl createLocalConnect() {
        return new OracleConnectImpl();
    }

    public OracleConnectImpl(String serverName,
                             String databaseName,
                             String portNumber,
                             String userName,
                             String password) {
        super();
        this.databaseName = databaseName;
        this.password = password;
        this.serverName = serverName;
        this.userName = userName;
        this.portNumber = portNumber;
    }

    public OracleConnectImpl(String serverName,
                             String databaseName,
                             String userName,
                             String password) {
        super();
        this.databaseName = databaseName;
        this.password = password;
        this.serverName = serverName;
        this.userName = userName;
    }


    // Constructor
    public OracleConnectImpl() {
    }

    private String getConnectionUrl() {
        return url + serverName + ":" + portNumber + "/" + databaseName;
    }

    public synchronized java.sql.Connection getConnection() {
        if (con == null) {
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                con = java.sql.DriverManager.getConnection(getConnectionUrl(), userName, password);
                //   if(con!=null) System.out.println("Connection Successful!");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error Trace in getConnection() : " + e.getMessage());
            }
        }
        return con;
    }

	/*
             Display the driver properties, database details
	 */

    public void displayDbProperties() {
        java.sql.DatabaseMetaData dm = null;
        java.sql.ResultSet rs = null;
        try {
            con = this.getConnection();
            if (con != null) {
                dm = con.getMetaData();
                System.out.println("Driver Information");
                System.out.println("\tDriver Name: " + dm.getDriverName());
                System.out.println("\tDriver Version: " + dm.getDriverVersion());
                System.out.println("\nDatabase Information ");
                System.out.println("\tDatabase Name: " + dm.getDatabaseProductName());
                System.out.println("\tDatabase Version: " + dm.getDatabaseProductVersion());
                System.out.println("Avalilable Catalogs ");
                rs = dm.getCatalogs();
                while (rs.next()) {
                    System.out.println("\tcatalog: " + rs.getString(1));
                }
                rs.close();
                rs = null;
                closeConnection();
            } else System.out.println("Error: No active Connection");
        } catch (Exception e) {
            e.printStackTrace();
        }
        dm = null;
    }

    public void closeConnection() {
        try {
            if (con != null)
                con.close();
            con = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
    }
}
