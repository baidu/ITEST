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
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import com.baidu.qa.service.test.dto.Constant;
import com.baidu.qa.service.test.execute.ServiceInterfaceCaseTest;


/**
 * mysql建立链接的工具类
 * @author xuedawei
 * @date 2013-8-29
 * @classname MysqlDatabaseManager
 * @version 1.0.0
 * @desc TODO
 */
public class MysqlDatabaseManager {
	private static Log log = LogFactory.getLog(MysqlDatabaseManager.class);

	/**
	 * 根据mysql db名称建立链接
	 * @param dbname
	 * @return
	 * @throws SQLException
	 */
	static public Connection getCon(String dbname) throws SQLException {

		String host = "";
		String user = "";
		String database = "";
		String password = "";
		String driverClass = "";
		String useUnicode = "";
		try {
			// 从配置文件中读取相关配置信息
			InputStream in = new BufferedInputStream(new FileInputStream(
					ServiceInterfaceCaseTest.CASEPATH + Constant.FILENAME_DB));

			Properties Info = new Properties();
			Info.load(in);
			

			host = Info.getProperty(dbname + "_DB_Host");
			user = Info.getProperty(dbname + "_DB_User");
			database = Info.getProperty(dbname + "_DB_DataBase");
			password = Info.getProperty(dbname + "_DB_Password");
			driverClass = Info.getProperty(dbname + "_DB_DriverClass");
			useUnicode = Info.getProperty(dbname + "_DB_UseUnicode");
		} catch (Exception e) {
			log.error("[mysql configure file for" + dbname + "error]:", e);
			throw new AssertionError("[get mysql database config error from properties file]");
		}
		if (host == null) {
			log.error("[load configure file for " + dbname + " error]:");
			return null;
		}

		// 显示中文
		String url = "jdbc:mysql://"
				+ host.trim()
				+ "/"
				+ ((database != null) ? database.trim() : "")
				+ "?useUnicode="
				+ ((useUnicode != null) ? useUnicode.trim()
						: "true&characterEncoding=gbk");

		try {
			Class.forName(driverClass);
		} catch (ClassNotFoundException e) {
			log.error("[class not found]:", e);
			throw new AssertionError("[class not found]");
		}
		Connection con = null;
		try {
			con = DriverManager.getConnection(url, user, password);
		} catch (SQLException a) {
			log.error("[mysql connection exception] ", a);
			throw new AssertionError("[mysql connection exception]");
		}

		return con;

	}
	

}
