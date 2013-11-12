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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import com.baidu.qa.service.test.client.HttpReqImpl;
import com.baidu.qa.service.test.client.SoapReqImpl;
import com.baidu.qa.service.test.dto.Config;
import com.baidu.qa.service.test.template.VariableGenerator;
import com.baidu.qa.service.test.util.BJSON;
import com.baidu.qa.service.test.util.FileCharsetDetector;
import com.baidu.qa.service.test.util.FileUtil;


public class VerifyResponseImpl implements VerifyResponse {
	
	private Log log = LogFactory.getLog(VerifyResponseImpl.class);
	
	
	public void verifyResponseWithExpectString(File expectfile,String actual) {
		
		
		FileCharsetDetector det = new FileCharsetDetector();
		String expectedStr = FileUtil.readFileByLines(expectfile);
		try{
			String oldcharset = det.guestFileEncoding(expectfile);
			if(oldcharset.equalsIgnoreCase("UTF-8") == false)
			FileUtil.transferFile(expectfile, oldcharset, "UTF-8");
		}catch(Exception ex){
			log.error("[change expect file charset error]:"+ex);
		}
		
		
		//如果接口返回的数据不是json格式，则认为是普通字符串，直接验证actual.contain(expect)
		if(!BJSON.BooleanJudgeStringJson(actual)||!BJSON.BooleanJudgeStringJson(expectedStr)){
			
			List<String> datalist = FileUtil.getListFromFileWithBOMFilter(expectfile);
			for(String data:datalist){
				log.info("[expected string]:"+data);
				Assert.assertTrue("[response different with expect][expect]:"+data.trim()+"[actual]:"+actual, actual.contains(data.trim()));
			}
		}
		//如果是json的话，则需要以递归的方式，逐层寻找并对比
		else{
			
			BJSON service =new BJSON();	
			HashMap<String, String> diffHash=service.findDiffSingleInJson(actual,expectedStr);
			if(diffHash.size()!=0){
			for(Entry<String, String> it :diffHash.entrySet()){
				log.error(it.getKey()+"----"+it.getValue());
			}
			Assert.assertEquals(0, diffHash.size());
			}
		}
	}


	public void verifyTestResultByHttpRequest(File file, Config config,
			VariableGenerator vargen) {
		try{
			HttpReqImpl req = new HttpReqImpl();
			req.requestHttpByHttpClient(file, config, vargen);
		}catch(Exception e){
			throw new AssertionError("verify test result by http request fail");
		}
		
	}


	public void verifyTestResultBySoapRequest(File file, Config config,
			VariableGenerator vargen) {
		try{
			SoapReqImpl req = new SoapReqImpl();
			req.requestSoap(file, config, vargen);
		}catch(Exception e){
			throw new AssertionError("verify test result by soap request fail");
		}
	}
}
