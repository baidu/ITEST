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

import com.baidu.qa.service.test.dto.Config;
import com.baidu.qa.service.test.template.VariableGenerator;

/**
 * 
 * @author xuedawei
 * @date 2013-9-3
 * @classname SetUpWithMysql
 * @version 1.0.0
 * @desc 针对mysql测试时的数据准备
 */
public interface SetUpWithMysql {

	/**
	 * 执行sql来构造数据
	 * @param file 后缀为“.sql”的文件，如insert，update语句
	 * @param config
	 * @return
	 */
	public boolean setTestDataWithSql(File file,Config config);
	
	/**
	 * 执行csv表格来构造数据
	 * @param file 后缀为“.csv”的文件，至少两行，第一行为table的字段，第二行对应的是数据
	 * @param config
	 * @return
	 */
	public boolean setTestDataWithCsvFile(File file,Config config);
	
	
	/**
	 * 通过sql文件事先查出一些测试数据
	 * @param file
	 * @param config
	 * @param vargen
	 * @return
	 */
	public boolean setTestDataFromMysql(File file,Config config,VariableGenerator vargen);
}
