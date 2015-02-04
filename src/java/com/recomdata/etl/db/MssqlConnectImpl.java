


package com.recomdata.etl.db;

public class MssqlConnectImpl implements DBConnect {

    private java.sql.Connection con = null;
    private final String url = "jdbc:jtds:sqlserver://";
    private String serverName = "localhost";
    private final String portNumber = "1433";
    private String databaseName = "Billing";
    private String userName = "sa";
    private String password = "sa";
    // Informs the driver to use server a side-cursor,
    // which permits more than one active statement
    // on a connection.
    private final String selectMethod = "cursor";

    public static MssqlConnectImpl createStrangeLoveConnect() {
        return new MssqlConnectImpl("StrangeLove", "CentClinRD1", "sa", "BioMkr101");
    }

    public static MssqlConnectImpl createLocalConnect() {
        return new MssqlConnectImpl();
    }

    public MssqlConnectImpl(String serverName,
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
    public MssqlConnectImpl() {
    }

    private String getConnectionUrl() {
        return url + serverName + ":" + portNumber + ";databaseName=" + databaseName + ";selectMethod=" + selectMethod + ";";
    }

    public java.sql.Connection getConnection() {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            con = java.sql.DriverManager.getConnection(getConnectionUrl(), userName, password);
            //   if(con!=null) System.out.println("Connection Successful!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error Trace in getConnection() : " + e.getMessage());
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
        MssqlConnectImpl myDbTest = new MssqlConnectImpl();
        myDbTest.displayDbProperties();
    }
}


