
/*  
 * 	Copyright(C) 2010-2013 Baidu Group
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *  
 */

package com.baidu.qa.service.test.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import com.baidu.qa.service.test.dto.Constant;
import com.baidu.qa.service.test.execute.ServiceInterfaceCaseTest;


/**
 * JDBC模板的一个简单封装
 * @author xuedawei
 * @date 2013-8-29
 * @classname JdbcUtil
 * @version 1.0.0
 * @desc 
 */
public class JdbcUtil {

	private static Log log = LogFactory.getLog(JdbcUtil.class);

	/**
	 * 
	 * @param sqlStr
	 * @param replace_time
	 * @return
	 */
	protected static String replaceTimeInSql(String sqlStr,Map<String, String> replace_time){
		if(sqlStr==null||sqlStr.trim().length()==0){
			return sqlStr;
		}
		if(replace_time==null||replace_time.size()==0){
			return sqlStr;
		}
		for (Entry<String, String> entry : replace_time.entrySet()) {
			String fmStr= entry.getValue().trim();
			String tablename=entry.getKey().trim();
			
			SimpleDateFormat dateFm = new SimpleDateFormat(fmStr); 
			String dateTime = dateFm.format(new java.util.Date());
			
			if (sqlStr.indexOf(tablename)!=-1){
				int start=sqlStr.indexOf(tablename)+tablename.length();
				sqlStr=sqlStr.replaceAll(tablename+sqlStr.substring(start, start+fmStr.length()),tablename+dateTime);
			}
					}
		log.debug("replaced:"+sqlStr);
		return sqlStr;	
		
	}
	
	protected static List<String> replaceTimeInSqlList(List<String> sqlStrs,Map<String, String> replace_time){
		if(sqlStrs==null||sqlStrs.size()==0){
			return sqlStrs;
		}
		 List<String> replacesqlStrs=new ArrayList<String>();
		for(String sql:sqlStrs){
			replacesqlStrs.add(replaceTimeInSql(sql,replace_time));
		}
		return replacesqlStrs;
	}
	/**
	 * 查询sql，返回list
	 * @param sqlStr
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String,Object>> excuteQuerySql(String sqlStr,String dbname,Map<String, String> replace_time) throws Exception{
		if(ServiceInterfaceCaseTest.is_execute_sql){
			return excuteQuerySql(replaceTimeInSql(sqlStr,replace_time),dbname);
		}else{
			return null;
		}
	}
	protected static List<Map<String,Object>> excuteQuerySql(String sqlStr,String dbname) throws Exception{

		List<Map<String,Object>> rltList = new ArrayList<Map<String,Object>>();
		
		//	声明连接、状态、结果集
		Connection con = null;
        Statement sm = null;
        ResultSet rs = null;
        try{
        	//返回一个数据库连接
			con = MysqlDatabaseManager.getCon(dbname);
        	Assert.assertNotNull("connect to db error:"+dbname,con);


    		//创建数据库连接的状态״̬
    		sm = con.createStatement();
    		rs = sm.executeQuery(sqlStr);
    		log.info("[sql:]"+sqlStr);
    		ResultSetMetaData rsmd = rs.getMetaData(); 
    		int numberOfColumns = rsmd.getColumnCount();
    		int count = 0;
    		String key;
    		Object value;
    		
//    		遍历数据库查询结果数据集 
    		while(rs.next()){
    			Map<String,Object> expectData = new HashMap<String,Object>();
    			count = 0;
    			while(count++< numberOfColumns){
    				
    				key = rsmd.getColumnLabel(count);
    				value = rs.getObject(key);
    				expectData.put(key, value);
    			}
    			rltList.add(expectData);
    		}
    		
    		return rltList;
    		
        }catch(Exception e){
        	throw e;
        }finally{
        	if(con!=null){
            	con.close();

        	}
        	if(sm!=null){
        		sm.close();
        	}
        	if(rs!=null){
        		rs.close();
        	}
        }
		
	}
	/**
	 * 执行写sql
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public static int excuteInsertOrUpdateSql(String sql,String dbname,Map<String, String> replace_time) throws Exception{
		if(ServiceInterfaceCaseTest.is_execute_sql){
		return excuteInsertOrUpdateSql(replaceTimeInSql(sql,replace_time),dbname);
		}else{
			return 0;
		}

	}
	protected static int excuteInsertOrUpdateSql(String sql,String dbname) throws Exception{
		
//	声明连接、状态、结果集
		Connection con = null;
        Statement sm = null;
        try{
        	con = MysqlDatabaseManager.getCon(dbname);
        	Assert.assertNotNull("connect to db error:"+dbname,con);
        	//创建数据库连接的状态
    		sm = con.createStatement();
    		log.info("[sql:]"+sql);
    		return sm.executeUpdate(sql); 
    		
    		
        }catch(Exception e){
        	throw e;
        }finally{
        	if(con!=null){
            	con.close();

        	}
        	if(sm!=null){
        		sm.close();
        	}

        }
		
	}
	
	public static void excuteVerifySqls(List<String> sqlStrs,String dbname,Map<String, String> replace_time) throws Exception{
		if(ServiceInterfaceCaseTest.is_execute_sql){
		excuteVerifySqls(replaceTimeInSqlList(sqlStrs,replace_time),dbname);
		}
	}

	protected static void excuteVerifySqls(List<String> sqlStrs,String dbname) throws Exception{
	
	
//	声明连接、状态、结果集
	Connection con = null;
    Statement sm = null;
    ResultSet rs = null;
    try{
    	//返回一个数据库连接
		con = MysqlDatabaseManager.getCon(dbname);
    	Assert.assertNotNull("connect to db error:"+dbname,con);

    	//创建数据库连接的状态״̬
		sm = con.createStatement();
		for(String sqlStr:sqlStrs){
		rs = sm.executeQuery(sqlStr);
		log.info("[sql:]"+sqlStr);

		Assert.assertTrue("[db expect error],has no data like:"+sqlStr,rs.next());		
		rs.close();
		}	
		
    }catch(Exception e){
    	throw e;
    }finally{
    	if(con!=null){
        	con.close();
    	}
    	if(sm!=null){
    		sm.close();
    	}
    	if(rs!=null){
    		rs.close();
    	}
    		
    }
	
}
	
	public static void excuteVerifyDeleteSqls(List<String> sqlStrs,String dbname,Map<String, String> replace_time) throws Exception{
		if(ServiceInterfaceCaseTest.is_execute_sql){
		excuteVerifyDeleteSqls(replaceTimeInSqlList(sqlStrs,replace_time),dbname);
		}
	}
	
	protected static void excuteVerifyDeleteSqls(List<String> sqlStrs,String dbname) throws Exception{
	
		Connection con = null;
	    Statement sm = null;
	    ResultSet rs = null;
	    try{
	    	//返回一个数据库连接
			con = MysqlDatabaseManager.getCon(dbname);
	    	Assert.assertNotNull("connect to db error:"+dbname,con);

	    	//创建数据库连接的状态״̬
			sm = con.createStatement();
			for(String sqlStr:sqlStrs){
			rs = sm.executeQuery(sqlStr);
			log.info("[sql:]"+sqlStr);

			Assert.assertFalse("[db expect error],has data like:"+sqlStr,rs.next());	
			rs.close();
			}	
			
	    }catch(Exception e){
	    	
	    	throw e;
	    }finally{
	    	if(con!=null){
	        	con.close();
	    	}
	    	if(sm!=null){
	    		sm.close();
	    	}
	    	if(rs!=null){
	    		rs.close();
	    	}
	    }
	
	}
	
	

	
}
