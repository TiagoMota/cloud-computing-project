package nl.tudelft.cloud_computing_project.database;

import org.sql2o.Sql2o;

public class Database {

    public static String dbname = "jdbc:mysql://cloudocr-db.c8yqjvuwxgg2.eu-west-1.rds.amazonaws.com:3306" ;
    public static String dbuser = "cloudocr";
    public static String dbpwd = "cloudocr";

    public static Sql2o getInstance() {
    	return null;
    }
    
}
   
    