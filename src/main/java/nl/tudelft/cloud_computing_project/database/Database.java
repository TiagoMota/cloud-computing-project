package nl.tudelft.cloud_computing_project.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Database {

    private static String dbname = "jdbc:mysql://cloudocr-db.c8yqjvuwxgg2.eu-west-1.rds.amazonaws.com:3306" ;
    private static String dbuser = "cloudocr";
    private static String dbpwd = "cloudocr";
    
    public static void main (String [] args) {
    	insertJob();
    }
   
    public static void insertJob(){
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} 
		catch (Exception ex) {
		    Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		}

        Connection connection = null;
        try {
        	// create a database connection
	        connection = DriverManager.getConnection(dbname, dbuser, dbpwd);
	        Statement statement = connection.createStatement();
	        statement.setQueryTimeout(30);  // set timeout to 30 sec.
	        
	        //statement.executeUpdate("create table post (id integer primary key autoincrement , titolo string,"
	        //          + "testo string, uda int, visibilita string, nodi string, bozza string, risorsa string, "
	        //          + "data string, utente string, pluginid int)");
        }
        catch(SQLException e) {
        	System.err.println(e.getMessage());
        }
        finally {
        	try {
        		if(connection != null)
        		connection.close();
        	}
	        catch(SQLException e) {
	        	// connection close failed.
	        	System.err.println(e);
	        }
        }
    }
    	
    public static void printQuery(String sql){
		try {
		    Class.forName("org.sqlite.JDBC");
		} 
		catch (ClassNotFoundException ex) {
		    Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		}

        Connection connection = null;
        try {
        	// create a database connection
	        connection = DriverManager.getConnection(dbname);
	        Statement statement = connection.createStatement();
	        statement.setQueryTimeout(30);  // set timeout to 30 sec.

		    ResultSet rs = statement.executeQuery(sql);
		    ResultSetMetaData rsmd = rs.getMetaData();
		    int numberOfColumns = rsmd.getColumnCount();
	    
		    while (rs.next()) {
		    	for (int i = 1; i <= numberOfColumns; i++) {
		    		if (i > 1) System.out.print(",  ");
		    		String columnValue = rs.getString(i);
		    		System.out.print(columnValue);
		    	}
		        System.out.println("");  
		    }
        }
        catch(SQLException e) {
        	System.err.println(e.getMessage());
        }
        finally {
        	try {
        		if(connection != null)
        			connection.close();
        	}
        	catch(SQLException e) {
        		// connection close failed.
        		System.err.println(e);
        	}
        }
    }
}

