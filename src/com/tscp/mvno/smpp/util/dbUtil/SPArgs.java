package com.tscp.mvno.smpp.util.dbUtil;

import java.util.HashMap;

public class SPArgs extends HashMap {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int INTEGER = 1;
	public static final int DATE    = 2;
	public static final int STRING  = 3;
	
	public SPArgs() {
		
	}
	
	public Object put( Object key, Object value ) { 
		Object obj = null;
		if( value == null ) {
			obj = super.put(key, "NULLVARCHAR");
		} else {
			obj = super.put(key, value);
		}
		
		return obj;
	}
	
	public Object put( Object key, int value ) {
		Object obj = null;
		obj = super.put(key, Integer.toString(value));
		return obj;
	}
	
	public Object put( Object key, Object value, int type ) {
		Object obj = null;
		if( value == null ) {
			switch( type ) {
			case INTEGER:
				obj = super.put(key, "NULLInteger");
				break;
			case DATE:
				obj = super.put(key, "NULLDate");
				break;
			case STRING:
			default:
				obj = super.put(key, "NULLVarchar");				
			}
		} else {
			obj = super.put(key, value);
		}
		return obj;
	}
}
