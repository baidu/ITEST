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
 * @date 2013-9-3
 * @classname Verify
 * @version 1.0.0
 * @desc 测试结果的验证接口，包括对接口返回字符串的验证、mysql数据验证、复用service接口做验证
 */
public interface Verify {
	
	
	
	/**
	 * 根据Expect目录下文件进行数据验证
	 * @param expectfile
	 * @return
	 */
	public boolean verify(File[] expectfile,String response,String caselocation,Config config,VariableGenerator vargen);
}
