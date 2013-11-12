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
 * 对接口返回内容做验证的主要接口，包括字符串对比、json对比
 * @author xuedawei
 * @date 2013-9-5
 * @classname VerifyResponse
 * @version 1.0.0
 * @desc TODO
 */
public interface VerifyResponse {
	/**
	 * 验证接口返回String，是否与预期一致
	 * @return
	 */
	public void verifyResponseWithExpectString(File file,String response);

	
	/**
	 * 复用被测程序的http接口去查询得出实际的数据，并验证是否与预期一致
	 * @param file
	 * @param response
	 */
	public void verifyTestResultByHttpRequest(File file, Config config,VariableGenerator vargen);
	
	
	/**
	 * 复用被测程序的soap接口去查询得出实际的数据，并验证是否与预期一致
	 * @param file
	 * @param response
	 */
	public void verifyTestResultBySoapRequest(File file, Config config,VariableGenerator vargen);
}
