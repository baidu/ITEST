/*  
 * 	Copyright(C) 2010-2013 Baidu Group
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *  
 */

package com.baidu.qa.service.test.verify;

import java.io.File;
import java.util.ArrayList;
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



public class VerifyMysqlDataImpl implements VerifyMysqlData {
	private static Log log = LogFactory.getLog(VerifyMysqlDataImpl.class);

	

	public void verifyMysqlDataByCsvFile(File file,Config config) {
		FileCharsetDetector det = new FileCharsetDetector();
		try{
			String oldcharset = det.guestFileEncoding(file);
			if(oldcharset.equalsIgnoreCase("UTF-8") == false)
			FileUtil.transferFile(file, oldcharset, "UTF-8");
		}catch(Exception ex){
			log.error("[change expect file charset error]:"+ex);
		}
		
		boolean isdelete = false;
		if(file==null){
			return;
		}
		String[] name = file.getName().trim().split("\\.");

		String table = "";
		String dbname = "";
		List<String[]> datalist = FileUtil.getSplitedListFromFile(file,Constant.FILE_REGEX_DB);
		if (datalist == null || datalist.size() <= 1) {
			log.info("[no available Expect dbfile]");
			return;
		}
		try {
			if (FileUtil.getPrefixTag(file) != null
					&& FileUtil.getPrefixTag(file).equalsIgnoreCase("delete")) {
				isdelete = true;
				Assert.assertEquals("[get db name error]:from"+ file.getName(),4, name.length);
				table = name[1].trim() +"."+ name[2].trim();
				dbname = name[1].trim();
			}
			else {
				Assert.assertEquals("[get db name error]:from"+ file.getName(),3, name.length);
				table = name[0].trim() +"."+ name[1].trim();
				dbname = name[0].trim();
			}
				
			
			List<String> sqlList = new ArrayList<String>();
			for (int i = 1; i < datalist.size(); i++) {
						StringBuilder sb = new StringBuilder();
						sb.append("select * from ");
						sb.append(table);
						sb.append(" where ");
						sb.append(datalist.get(0)[0] + "=");
						sb.append(datalist.get(i)[0]);
						for (int j = 1; j < datalist.get(i).length; j++) {
							sb.append(" and ");
							sb.append(datalist.get(0)[j] + "=");
							sb.append(datalist.get(i)[j]);
						}
						sqlList.add(sb.toString());
						 log.info(sb.toString());
			}
				
			if (isdelete) {
				JdbcUtil.excuteVerifyDeleteSqls(sqlList, dbname ,config.getReplace_time());
			} else {
				JdbcUtil.excuteVerifySqls(sqlList, dbname,config.getReplace_time());

			}
				
			
		} catch (Exception e) {
			log.error("[the file "+file+" data verify fail]:", e);
			throw new AssertionError("the file "+file+" data verify fail: "+ e.getMessage());
		}

	}





	public void verifyMysqlDataBySqlFile(File file, Config config) {
		FileCharsetDetector det = new FileCharsetDetector();
		try{
			String oldcharset = det.guestFileEncoding(file);
			if(oldcharset.equalsIgnoreCase("UTF-8") == false)
			FileUtil.transferFile(file, oldcharset, "UTF-8");
		}catch(Exception ex){
			log.error("[change expect file charset error]:"+ex);
		}
		
		Assert.assertNotNull("sql file to verify is null",file);
		boolean isdelete = false;		
		try {
			if (FileUtil.getPrefixTag(file) != null
					&& FileUtil.getPrefixTag(file).equalsIgnoreCase("delete")) {
				isdelete = true;				
			}			
			List<String> datalist = FileUtil.getListFromFile(file);
			if(datalist.size() == 0){
				return ;
			}					
			try{
						for(String sql : datalist){
							if(sql==null ||sql.trim().length()==0){
								continue;
							}
							String[] split1=sql.split(" ");
							Assert.assertTrue("wrong sql without select in "+file+":"+sql,split1[0].equalsIgnoreCase("select"));
							

							String[] split=sql.split("\\.");
							Assert.assertTrue("wrong sql with wrong dbname or tablename in "+file+":"+sql,split.length>=2);
							
							String[] name=split[0].split(" ");
							String dbname=name[name.length-1];
							List<String> sqls=new ArrayList<String>();
							sqls.add(sql);
							if (isdelete){
								JdbcUtil.excuteVerifyDeleteSqls(sqls, dbname,config.getReplace_time());

							}
							else{
								JdbcUtil.excuteVerifySqls(sqls, dbname,config.getReplace_time());
							}
							log.info("[setup sql:]"+sql);
						}
						return ;
					}catch(Exception e){
						log.error("[setup data error]:"+file.getName()+":", e);
						throw new AssertionError("setup error"+e.getMessage());
					}
				
				
				
			
		} catch (Exception e) {
			log.error("[the file "+file+" data verify fail]:", e);
			throw new AssertionError("the file "+file+" data verify fail:"+ e.getMessage());
		}

	}





	public void getMysqlDataBySqlUsedToVerify(File file, Config config,
			VariableGenerator vargen) {
		MysqlClientImpl mc = new MysqlClientImpl();
		try{
			mc.selectVar(file, config, vargen);
		}catch(Exception e){
			log.error("[get test result data from mysql error]:", e);
		}
	}
		

}
