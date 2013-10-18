package nl.tudelft.cloud_computing_project.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DBCreator {

    private static String dbname = "jdbc:sqlite:database.db" ;
	
    public static void setDebugMode(boolean debug) {
		if(debug)
		    dbname = "jdbc:sqlite:database_debug.db" ;
		else
		    dbname =  "jdbc:sqlite:database.db";
    }
    

    public static void resetDB(){
        String prevdbname = dbname;
        setDebugMode(true);

        creaPostTabella();
		
        dbname=prevdbname;
    }
   
    public static void creaPostTabella(){
		try {
			Class.forName("org.sqlite.JDBC");
		} 
		catch (ClassNotFoundException ex) {
		    Logger.getLogger(DBCreator.class.getName()).log(Level.SEVERE, null, ex);
		}

        Connection connection = null;
        try {
        	// create a database connection
	        connection = DriverManager.getConnection(dbname);
	        Statement statement = connection.createStatement();
	        statement.setQueryTimeout(30);  // set timeout to 30 sec.
	
	        statement.executeUpdate("drop table if exists post");
	        statement.executeUpdate("create table post (id integer primary key autoincrement , titolo string,"
	                  + "testo string, uda int, visibilita string, nodi string, bozza string, risorsa string, "
	                  + "data string, utente string, pluginid int)");
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
		    Logger.getLogger(DBCreator.class.getName()).log(Level.SEVERE, null, ex);
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

