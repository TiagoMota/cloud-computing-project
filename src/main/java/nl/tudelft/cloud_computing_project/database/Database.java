package nl.tudelft.cloud_computing_project.database;

import org.sql2o.Sql2o;

public class Database {

    private static final String dbname = "jdbc:mysql://cloudocr-db.c8yqjvuwxgg2.eu-west-1.rds.amazonaws.com:3306" ;
    private static final String dbuser = "cloudocr";
    private static final String dbpwd = "cloudocr";
    
    private static final ThreadLocal<Sql2o> instance = new ThreadLocal<Sql2o>() {
    	@Override protected Sql2o initialValue() {
            return new Sql2o(dbname, dbuser, dbpwd);
    	}
    };
    
    /**
     * Get a database connection (thread-local)
     */
    public static Sql2o getConnection() {
    	return instance.get();
    }
    
    /**
     * Release the database connection (thread-local)
     */
    public static void releaseConnection() {
    	instance.remove();
    }
}
   
    