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
import org.junit.Assert;

import com.baidu.qa.service.test.dto.Config;
import com.baidu.qa.service.test.dto.Constant;
import com.baidu.qa.service.test.util.FileUtil;
import com.baidu.qa.service.test.util.JdbcUtil;

/**
 * 
 * @author xuedawei
 * @date 2013-8-30
 * @classname TearDownWithCsvImpl
 * @version 1.0.0
 * @desc csv表格类文件用作mysql数据库数据清理，转化为sql后执行
 */
public class TearDownWithCsvImpl implements TearDownWithCsv {
	private Log log = LogFactory.getLog(TearDownWithCsvImpl.class);
	
	public boolean teardownTestDataWithCSV(File file,Config config) {
		
		if(file==null){
			return false;
		}
		String[] name = file.getName().trim().split("\\.");

		String table = "";
		String dbname = "";
		List<String[]> datalist = FileUtil.getSplitedListFromFile(file,
				Constant.FILE_REGEX_DB);
		if (datalist == null || datalist.size() <= 1) {
			log.info("[no available Expect dbfile]");
			return false;
		}
		
		try {Assert.assertEquals("[get db name error]:from"
				+ file.getName(),3, name.length);
		table = name[0].trim() +"."+ name[1].trim();
		dbname = name[0].trim();
	


		for (int i = 1; i < datalist.size(); i++) {
			StringBuilder sb = new StringBuilder();
			sb.append("delete from ");
			sb.append(table);
			sb.append(" where ");
			sb.append(datalist.get(0)[0] + "=");
			sb.append(datalist.get(i)[0]);
			for (int j = 1; j < datalist.get(i).length; j++) {
				sb.append(" and ");
				sb.append(datalist.get(0)[j] + "=");
				sb.append(datalist.get(i)[j]);
			}
			
			JdbcUtil.excuteInsertOrUpdateSql(sb.toString(), dbname,config.getReplace_time());
			 log.debug(sb.toString());
		}

		
		return true;

		} catch (Exception e) {
				log.error("[delete data error]:", e);
				throw new RuntimeException("delete data error:"+ e.getMessage());
		}
		
	}


}