


package com.recomdata.etl.db;

public interface DBConnect {
    public java.sql.Connection getConnection();

    public void closeConnection();

}
