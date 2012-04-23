package com.tscp.mvno.smpp.util.dbUtil;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import oracle.jdbc.driver.OracleTypes;


public class StoredProc {
	
	// [start] member variables
	
	public static final String DB_PRODUCT_NAME_ORACLE = "Oracle";
	public static final String NULL_VARCHAR				= "NULLVarchar";
	public static final String NULL_INTEGER				= "NULLInteger";
	public static final String NULL_DATE				= "NULLDate";
	
	private Connection conn;
	private String dbProductName;
	private HashMap resultSetMap = new HashMap();
	private String userName;
	  
	// [end] member variables
	  
	// [start] constructors
	
	public StoredProc( Connection iConnection ) {
		this(iConnection,"SYSTEM");
	}
	  
	public StoredProc( Connection iConnection, String iUserName ) {
		this(iConnection,iUserName,"Oracle");
	}
	
	public StoredProc( Connection iConnection, String iUserName, String iDBProductName ) {
		conn = iConnection;
		userName = iUserName;
		dbProductName = iDBProductName;
	}
	  
	// [end] constructors
	
	public ResultSet exec(SPArgs spargs) {
		ResultSet rs = null;
		if( dbProductName.equals(DB_PRODUCT_NAME_ORACLE) ) {
			return execOracle(spargs);
		}
		return rs;
	}
	
	public ResultSet exec(SPArgs spargs, int kenan) {
		return execKenan(spargs);
	}
	
	public void close( ResultSet rs ) {
		Statement statement = (Statement)resultSetMap.get(rs);
		if( statement != null ) {
			try {
				rs.close();
				statement.close();
			} catch( Exception e ) {
				rs = null;
				statement = null;
			} finally {
				resultSetMap.remove(rs);
			}
		} else {
			System.out.println(" statement is NULL and cannot be closed...");
		}
	}
	
	private ResultSet execOracle(SPArgs spargs) {
		ResultSet rs = null;
		//determine the number of args
		int numargs = 0;
		for( int i = 1; i < spargs.size(); ++i ) {
			String arg = "arg"+i;
			Object obj = spargs.get(arg);
			if( obj != null ) {
				++numargs;
			}
		}
		
		numargs += 2; //move two spaces for cursor and username
		
		String sp = (String)spargs.get("sp");
		StringBuffer spString = new StringBuffer("{ call " + sp);
		StringBuffer logString = new StringBuffer(sp + "(");
		
		if( numargs > 0 ) {
			for( int i = 1; i < numargs; ++i ) {
				if( i == 1 ) {
					spString.append("(?");
				} else {
					spString.append(",?");
				}
			}
			spString.append(")");
		}
		spString.append("}");
		
		CallableStatement cs = null;
		try {
			cs = conn.prepareCall(spString.toString());
			
			cs.registerOutParameter(1, OracleTypes.CURSOR);
			
			if( userName != null ) {
				cs.setString(2, userName);
			} else {
				cs.setString(2, "SYSTEM");
			}
			
			logString.append(":CURSOR, '"+userName);
			for( int i = 3; i < numargs; ++i ) {
				String argVal = "args"+(i-2);
				Object value = spargs.get(argVal);
				
				if( value == null || value.toString().equals(NULL_VARCHAR) ) {
					logString.append("', 'NULLVarchar");
					cs.setNull(i, OracleTypes.VARCHAR);
				} else if( value.toString().equals(NULL_INTEGER) ) {
					logString.append("', 'NULLInteger");
					cs.setNull(i, OracleTypes.INTEGER);
				} else if( value.toString().equals(NULL_DATE) ) {
					logString.append("', 'NULLDate");
					cs.setNull(i, OracleTypes.DATE);
				} else {
					logString.append("', '"+value);
					if( value instanceof java.util.Date ) {
						cs.setTimestamp(i, new java.sql.Timestamp(((java.util.Date)value).getTime()));
					} else {
						cs.setString(i, value.toString());
					}
				}
			}
			logString.append("')");
			
			System.out.println("executing :: "+logString);
			
			cs.execute();
			rs = (ResultSet)cs.getObject(1);
			if( rs != null ) {
				resultSetMap.put(rs, cs);
			}
		} catch( SQLException sql_ex ) {
			System.out.println("SQLException thrown with the following message: "+sql_ex.getMessage());
		}
		return rs;
	}
	
	private ResultSet execKenan(SPArgs spargs) {
		ResultSet rs = null;
		//determine the number of args
		int numargs = 0;
		for( int i = 1; i < spargs.size(); ++i ) {
			String arg = "arg"+i;
			Object obj = spargs.get(arg);
			if( obj != null ) {
				++numargs;
			}
		}
		
		numargs += 1; //move one space for cursor
		
		String sp = (String)spargs.get("sp");
		StringBuffer spString = new StringBuffer("{ call " + sp);
		StringBuffer logString = new StringBuffer(sp + "(");
		
		if( numargs > 0 ) {
			for( int i = 1; i <= numargs; ++i ) {
				if( i == 1 ) {
					spString.append("(?");
				} else {
					spString.append(",?");
				}
			}
			spString.append(")");
		}
		spString.append("}");
		
		CallableStatement cs = null;
		try {
			cs = conn.prepareCall(spString.toString());
//			
//			if( userName != null ) {
//				cs.setString(2, userName);
//			} else {
//				cs.setString(2, "SYSTEM");
//			}
			
			//logString.append(":CURSOR, '"+userName);
			/*
			for( int i = 1; i < numargs; ++i ) {
				String argVal = "arg"+(i);
				Object value = spargs.get(argVal);
				
				if( value == null || value.toString().equals(NULL_VARCHAR) ) {
					logString.append("', 'NULLVarchar");
					cs.setNull(i, OracleTypes.VARCHAR);
				} else if( value.toString().equals(NULL_INTEGER) ) {
					logString.append("', 'NULLInteger");
					cs.setNull(i, OracleTypes.INTEGER);
				} else if( value.toString().equals(NULL_DATE) ) {
					logString.append("', 'NULLDate");
					cs.setNull(i, OracleTypes.DATE);
				} else {
					if( i == 1 ) {
						logString.append("'"+value);
					} else {
						logString.append("', '"+value);
					}
					if( value instanceof java.util.Date ) {
						cs.setTimestamp(i, new java.sql.Timestamp(((java.util.Date)value).getTime()));
					} else {
						cs.setString(i, value.toString());
					}
				}
			}
			logString.append("',");
			*/
			cs.registerOutParameter(numargs, OracleTypes.CURSOR);
			logString.append(":cursor)");
			
			System.out.println("executing kenan sp :: "+logString);
			
			cs.execute();
			rs = (ResultSet)cs.getObject(numargs);
			//rs = (ResultSet)cs.getResultSet();
					
			if( rs != null ) {
				resultSetMap.put(rs, cs);
			}
		} catch( SQLException sql_ex ) {
			System.out.println("SQLException thrown with the following message: "+sql_ex.getMessage());
			sql_ex.printStackTrace();
		}
		return rs;
	}
}
