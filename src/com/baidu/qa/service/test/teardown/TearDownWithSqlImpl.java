/*  
 * 	Copyright(C) 2010-2013 Baidu Group
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *  
 */

package com.baidu.qa.service.test.teardown;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.qa.service.test.client.MysqlClientImpl;
import com.baidu.qa.service.test.dto.Config;
import com.baidu.qa.service.test.template.VariableGenerator;
import com.baidu.qa.service.test.util.FileUtil;
import com.baidu.qa.service.test.util.JdbcUtil;

/**
 * 
 * @author xuedawei
 * @date 2013-8-30
 * @classname TearDownWithSqlImpl
 * @version 1.0.0
 * @desc 执行sql语句以便对mysql的测试数据做清理和恢复
 */
public class TearDownWithSqlImpl implements TearDownWithSql {

	private Log log = LogFactory.getLog(TearDownWithSqlImpl.class);

	public boolean cleanTestDataWithSql(File file,Config config){
		
		List<String> datalist = FileUtil.getListFromFile(file);
		if(datalist.size() == 0){
			return true;
		}
		try{
			for(String sql : datalist){
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
			log.error("[delete or update mysql data error]:", e);
			throw new RuntimeException("tear down with "+file.getName()+" file error"+e.getMessage());
		}
		
	}

	public boolean getTestResultFromMysql(File file, Config config,
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