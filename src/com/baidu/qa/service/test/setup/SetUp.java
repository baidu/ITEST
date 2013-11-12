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
 * @classname SetUp
 * @version 1.0.0
 * @desc case执行的前置步骤，可处理数据构造、环境准备等工作
 */
public interface SetUp {
	
	/**
	 * 数据准备的接口
	 * @param setupfile
	 * @param config
	 * @param vargen
	 * @return
	 */
	public boolean setTestData(File[] setupfile,Config config,VariableGenerator vargen);
	
	
	/**
	 * 环境准备的接口
	 * @param setupfile
	 * @param config
	 * @param vargen
	 * @return
	 */
	public boolean setTestEnv(File[] setupfile,Config config,VariableGenerator vargen);
}
