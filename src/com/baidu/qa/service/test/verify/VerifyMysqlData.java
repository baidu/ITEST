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

import com.baidu.qa.service.test.dto.Config;
import com.baidu.qa.service.test.template.VariableGenerator;


/**
 * 
 * @author xuedawei
 * @date 2013-9-5
 * @classname VerifyMysqlData
 * @version 1.0.0
 * @desc 对mysql数据库中的实际数据做验证
 */
public interface VerifyMysqlData {

	/**
	 * 以case目录下的/expect/xxx.csv文件，作为mysql验证结果的一种形式
	 * @param files
	 * @return
	 */
	public void verifyMysqlDataByCsvFile(File file,Config config);
	
	
	/**
	 * 以case目录下的/expect/xxx.sql文件，作为mysql验证结果的一种形式
	 * @param file
	 * @param config
	 */
	public void verifyMysqlDataBySqlFile(File file,Config config);
	
	
	/**
	 * 在结果验证环节先查询出mysql的数据，提供对比
	 * @param file
	 * @param config
	 * @param vargen
	 */
	public void getMysqlDataBySqlUsedToVerify(File file, Config config,VariableGenerator vargen);
}
