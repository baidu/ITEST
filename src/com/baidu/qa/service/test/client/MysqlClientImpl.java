/*  
 * 	Copyright(C) 2010-2013 Baidu Group
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *  
 */

package com.baidu.qa.service.test.client;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.qa.service.test.dto.Config;
import com.baidu.qa.service.test.template.VariableGenerator;
import com.baidu.qa.service.test.util.FileCharsetDetector;
import com.baidu.qa.service.test.util.FileUtil;
import com.baidu.qa.service.test.util.JdbcUtil;


/**
 * 
 * @author xuedawei
 * @date 2013-9-3
 * @classname MysqlClientImpl
 * @version 1.0.0
 * @desc Mysql数据库执行sql的client类
 */
public class MysqlClientImpl {
	private Log log = LogFactory.getLog(MysqlClientImpl.class);

	/**
	 * 执行mysql的查询语句，得到数据库中的数据，以用作测试结果校验
	 * @param file
	 * @param config
	 * @param vargen
	 */
	public void selectVar(File file, Config config, VariableGenerator vargen) {
		FileCharsetDetector det = new FileCharsetDetector();
		try {
			String oldcharset = det.guestFileEncoding(file);
			if (oldcharset.equalsIgnoreCase("UTF-8") == false)
				FileUtil.transferFile(file, oldcharset, "UTF-8");
		} catch (Exception ex) {
			log.error("[change expect file charset error]:" + ex);
		}

		List<String> datalist = FileUtil.getListFromFile(file);
		if (datalist.size() == 0) {
			return;
		}
		try {
			for (String sql : datalist) {
				log.info("[select sql:]" + sql);
				// 执行sql
				String[] split = sql.split("\\.");
				if (split.length < 2) {
					log.error("wrong sql:" + file);
					return;
				}
				String[] name = split[0].split(" ");
				String dbname = name[name.length - 1];
				List<Map<String, Object>> resultlist = JdbcUtil.excuteQuerySql(sql, dbname, config.getReplace_time());
				// 赋值
				if (resultlist == null || resultlist.size() == 0) {
					log.error("[set var error]:hasn't result :" + sql);
				}
				Map<String, Object> resultmap = resultlist.get(0);
				if (resultmap != null || resultmap.size() != 0) {
					Map<String, Object> map = new HashMap<String, Object>();
					for (Entry<String, Object> entry : resultmap.entrySet()) {
						map.put(file.getName().substring(0,file.getName().indexOf(".")) +"."+entry.getKey(), 
								String.valueOf(entry.getValue()));
						vargen.add2Map(map);
					}
				}
			}
			return;
		} catch (Exception e) {
			log.error("[setup data error]:" + file.getName() + ":", e);
//			throw new AssertionError("setup error" + e.getMessage());
		}
	}

}
