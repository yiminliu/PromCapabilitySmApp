package com.tscp.mvno.smpp.util.dbUtil;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.tscp.mvno.smpp.util.logging.SMPPLogger;

public class DBConnectionUtil {

    private Connection conn;
    private int maxConnections = 50;    
    private String dbType;
    private String dbDriver;
    private String dbServer;
    private String dbLogin;
    private String dbPassword;
    private String inputProperties = "dbconfig.properties";    

    private static SMPPLogger logger = new SMPPLogger();
    
    public String getDbDriver() {
		return dbDriver;
	}

	public String getDbLogin() {
		return dbLogin;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public String getDbServer() {
		return dbServer;
	}

	public String getDbType() {
		return dbType;
	}

	public int getMaxConnections() {
		return maxConnections;
	}
	
	public void setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
	}

	public void setDbLogin(String dbLogin) {
		this.dbLogin = dbLogin;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public void setDbServer(String dbServer) {
		this.dbServer = dbServer;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}
	
	public DBConnectionUtil() {
    	init();
    }
    
    public synchronized Connection getConnection() throws Exception {
		conn = null;
		try {
			conn = DriverManager.getConnection(getDbServer(),getDbLogin(),getDbPassword());
		} catch( Exception e ) { 
			logger.error("Error obtaining database connection!! "+e.getMessage());
			throw e;
		}
		logger.info("Connected to DB server: " + getDbServer());
		return conn;
	}
    
    public synchronized void releaseConnection() {
    	if( conn != null ) {
    		try {
    		conn.close();
    		} catch( SQLException sql_ex ) {
    			logger.error("SQL Exception thrown when closing connection..."+sql_ex.getMessage());
    		} catch( Exception ex ) {
    			logger.error("General Exception thrown when closing connection..."+ex.getMessage());
    			
    		} finally {
    			conn = null;
    		}
    	}
    }
        
    private void init() {
    	Properties props = new Properties();
    	ClassLoader cl = DBConnectionUtil.class.getClassLoader();
    	InputStream in = cl.getResourceAsStream(inputProperties);
    	    			
    	try {
    		if(in != null)
    		props.load(in);
    		    		
    		dbType		= props.getProperty("dbType", "Oracle");
    		dbDriver	= props.getProperty("dbDriver","oracle.jdbc.driver.OracleDriver");
    		//dbServer	= props.getProperty("dbServer","jdbc:oracle:thin:@USCAELMUX18:1521:K11MVNOT");
    		//dbServer	= props.getProperty("dbServer","jdbc:oracle:thin:@K11MVNO:1521:K11MVNOT");
    		dbServer	= props.getProperty("dbServer","jdbc:oracle:thin:@uscael200:1521:K11MVNOT");    		
    		dbLogin		= props.getProperty("dbLogin", "REPORT");
    		dbPassword	= props.getProperty("dbPassword", "REPORTMVNO");
    	
    		Class.forName(dbDriver).newInstance();
    	} catch ( Exception e ) {
      		logger.error("Error loading DB Properties!! due to : "+e.getMessage());
    	}
    }    
    
    public static void main(String[] args) { 
    	logger.info("Testing SMPP Project ConnectionInfo class....");
    	DBConnectionUtil ci = new DBConnectionUtil();
    	logger.info("ConnectionInfo initialized...");
    	logger.info("**** ConnectionInfo.DBType            :: "+ci.getDbType());
    	logger.info("**** ConnectionInfo.DBServer          :: "+ci.getDbServer());
    	logger.info("**** ConnectionInfo.DBDriver          :: "+ci.getDbDriver());
    	logger.info("**** ConnectionInfo.DBLogin           :: "+ci.getDbLogin());
    	logger.info("**** ConnectionInfo.DBPassword        :: "+ci.getDbPassword());
    	try {
    	    Connection conn = ci.getConnection();
    	    if( conn != null ) {
    		   try {
    		       conn.close();
    		   } catch (Exception e) {
    			logger.error("General Exception thrown!! e.getMessage() = "+e.getMessage());
    		   }
    	    } else {
    		    conn = null;
    	    }
    	}
    	catch(Exception e){
    		logger.error("Error occured while creating connection, due to : " + e.getMessage());
    	}
    	
    	logger.info("Done Testing SMPP Project ConnectionInfo Class.");
    }    
}
