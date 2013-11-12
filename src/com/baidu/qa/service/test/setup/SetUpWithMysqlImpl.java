/*  
 * 	Copyright(C) 2010-2013 Baidu Group
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *  
 */

package com.baidu.qa.service.test.setup;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import com.baidu.qa.service.test.client.MysqlClientImpl;
import com.baidu.qa.service.test.dto.Config;
import com.baidu.qa.service.test.dto.Constant;
import com.baidu.qa.service.test.template.VariableGenerator;
import com.baidu.qa.service.test.util.FileCharsetDetector;
import com.baidu.qa.service.test.util.FileUtil;
import com.baidu.qa.service.test.util.JdbcUtil;



public class SetUpWithMysqlImpl implements SetUpWithMysql {
	private Log log = LogFactory.getLog(SetUpWithMysqlImpl.class);
	
	
	
	
	public boolean setTestDataWithSql(File file,Config config) {
		FileCharsetDetector det = new FileCharsetDetector();
		try{
			String oldcharset = det.guestFileEncoding(file);
			if(oldcharset.equalsIgnoreCase("UTF-8") == false)
			FileUtil.transferFile(file, oldcharset, "UTF-8");
		}catch(Exception ex){
			log.error("[change expect file charset error]:"+ex);
		}
		
		List<String> datalist = FileUtil.getListFromFile(file);
		if(datalist.size() <= 1){
			return true;
		}
		try{
			String table = file.getName().substring(0, file.getName().indexOf(Constant.FILE_TYPE_DB)-1);
			String[] name=table.split("\\.");
			Assert.assertEquals("[get db name error]:from "+file.getName(),2, name.length);
			
			String dbname=name[0];
			StringBuilder sb = new StringBuilder();
			sb.append("insert into ");
			sb.append(table);
			sb.append(" ( "+datalist.get(0)+" )");
			sb.append(" values ");
			for(int i=1;i<datalist.size();i++){
				sb.append(" ("+datalist.get(i)+" ),");
			}
			sb.deleteCharAt(sb.length()-1);
			
			log.info("[insert csv data into db,the sql is]:"+sb.toString());
			JdbcUtil.excuteInsertOrUpdateSql(sb.toString(),dbname,config.getReplace_time());
			
			
			log.debug("[insert csv data into db,the data are]:");
			for(int i=0;i<datalist.size();i++){
				System.out.println(datalist.get(i));
			}
			
			return true;
		}catch(Exception e){
			log.error("[insert csv data into mysql error]:", e);
			throw new AssertionError("insert csv data into mysql error..."+e.getMessage());
		}
		
	}

	
	
	public boolean setTestDataWithCsvFile(File file,Config config){
		FileCharsetDetector det = new FileCharsetDetector();
		try{
			String oldcharset = det.guestFileEncoding(file);
			if(oldcharset.equalsIgnoreCase("UTF-8") == false)
			FileUtil.transferFile(file, oldcharset, "UTF-8");
		}catch(Exception ex){
			log.error("[change expect file charset error]:"+ex);
		}
		
		List<String> datalist = FileUtil.getListFromFile(file);
		if(datalist.size() == 0){
			return true;
		}
		try{
			for(String sql : datalist){
				log.info("[setup sql:]"+sql);
				
				String[] split=sql.split("\\.");
				if(split.length<2){
					log.error("wrong sql:"+file);
					return false;
				}
				String[] name=split[0].split(" ");
				String dbname=name[name.length-1];
				JdbcUtil.excuteInsertOrUpdateSql(sql,dbname,config.getReplace_time());
				
			}
			return true;
		}catch(Exception e){
			log.error("[insert csv data into mysql error]:", e);
			throw new AssertionError("insert csv data into mysql error..."+e.getMessage());
		}
		
	}



	
	public boolean setTestDataFromMysql(File file, Config config,
			VariableGenerator vargen) {
		MysqlClientImpl mc = new MysqlClientImpl();
		try{
			mc.selectVar(file, config, vargen);
			return true;
		}catch(Exception e){
			log.error("[get test result data from mysql error]:", e);
			return false;
		}
		
	}
	


}