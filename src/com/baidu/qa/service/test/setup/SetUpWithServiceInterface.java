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
 * @classname SetUpWithServiceInterface
 * @version 1.0.0
 * @desc 复用service的接口来构造case所需的测试数据
 */
public interface SetUpWithServiceInterface {
	
	/**
	 * 复用http接口以构造数据
	 * @param file
	 * @param config
	 * @param vargen
	 * @return
	 */
	public boolean setTestDataByHttpRequest(File file,Config config,VariableGenerator vargen);
	/**
	 * 复用soap接口以构造数据
	 * @param file
	 * @param config
	 * @param vargen
	 * @return
	 */
	public boolean setTestDataBySoapRequest(File file,Config config,VariableGenerator vargen);
	

}
