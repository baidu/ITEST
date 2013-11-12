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

import com.baidu.qa.service.test.client.HttpReqImpl;
import com.baidu.qa.service.test.client.SoapReqImpl;
import com.baidu.qa.service.test.dto.Config;
import com.baidu.qa.service.test.template.VariableGenerator;
/**
 * 
 * @author xuedawei
 * @date 2013-8-30
 * @classname TearDownWithServiceInterfaceImpl
 * @version 1.0.0
 * @desc 复用http，soap接口来做数据清理的工作
 */
public class TearDownWithServiceInterfaceImpl implements
		TearDownWithServiceInterface {

	public boolean cleanTestDataByHttpRequest(File file, Config config,VariableGenerator vargen) {

		try{
			HttpReqImpl req = new HttpReqImpl();
			req.requestHttpByHttpClient(file, config, vargen);
			return true;
		}catch(Exception e){
			return false;
		}
		
	}

	public boolean cleanTestDataBySoapRequest(File file, Config config,VariableGenerator vargen) {
		try{
			SoapReqImpl req = new SoapReqImpl();
			req.requestSoap(file, config, vargen);
			return true;
		}catch(Exception e){
			return false;
		}
	}

}
