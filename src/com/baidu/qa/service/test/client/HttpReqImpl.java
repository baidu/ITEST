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
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.qa.service.test.dto.Config;
import com.baidu.qa.service.test.dto.Constant;
import com.baidu.qa.service.test.template.VariableGenerator;
import com.baidu.qa.service.test.util.FileCharsetDetector;
import com.baidu.qa.service.test.util.FileUtil;


/**
 * 
 * @author xuedawei
 * @date 2013-8-30
 * @classname HttpReqImpl
 * @version 1.0.0
 * @desc 处理http请求，被用在setup，teardown，verify等环节
 */
public class HttpReqImpl {

	private static Log log = LogFactory.getLog(HttpReqImpl.class);

	private String url = "";
	private Config config;
	private Map<String, String> parammap = null;
	private String itest_expect="";
	private String itest_expect_json="";

	private boolean hashost=false;

	
	
	
	/**
	 * use httpclient
	 * @param file
	 * @param config
	 * @param vargen
	 * @return
	 */
	public Object requestHttpByHttpClient(File file, Config config, VariableGenerator vargen) {
		FileCharsetDetector det = new FileCharsetDetector();
		try {
			String oldcharset = det.guestFileEncoding(file);
			if (oldcharset.equalsIgnoreCase("UTF-8") == false)
				FileUtil.transferFile(file, oldcharset, "UTF-8");
		} catch (Exception ex) {
			log.error("[change expect file charset error]:" + ex);
		}

		Map<String, String> datalist = FileUtil.getMapFromFile(file, "=");
		if (datalist.size() <= 1) {
			return true;
		}
		if (!datalist.containsKey(Constant.KW_ITEST_HOST)&&!datalist.containsKey(Constant.KW_ITEST_URL)) {
			log.error("[wrong file]:" + file.getName() + " hasn't Url");
			return null;
		}
		if(datalist.containsKey(Constant.KW_ITEST_HOST)){
			this.url = datalist.get(Constant.KW_ITEST_HOST);
			this.hashost=true;
			datalist.remove(Constant.KW_ITEST_HOST);
		}
		else{
			String action = datalist.get(Constant.KW_ITEST_URL);
			if(config.getHost().lastIndexOf("/") == config.getHost().length()-1 &&
					action.indexOf("/") == 0){
				action = action.substring(1);
			}
			this.url = config.getHost()+ action;
			datalist.remove("itest_url");

		}
		if(datalist.containsKey(Constant.KW_ITEST_EXPECT)){
			this.itest_expect=datalist.get(Constant.KW_ITEST_EXPECT);
			datalist.remove(Constant.KW_ITEST_EXPECT);
		}
		if(datalist.containsKey(Constant.KW_ITEST_JSON)){
			this.itest_expect_json=datalist.get(Constant.KW_ITEST_JSON);
			datalist.remove(Constant.KW_ITEST_JSON);
		}
		parammap = datalist;
		this.config = config;
		
		
		
		//构造HttpClient的实例
		HttpClient httpClient = new HttpClient();
		//设置超时时间
		httpClient.setConnectionTimeout(30000);
		httpClient.setTimeout(30000);	
		httpClient.getParams().setParameter("http.protocol.content-charset", "UTF-8");
		PostMethod postMethod = new PostMethod(url);
		
		if(hashost==false){
			//cookie copy
			if (config.getVariable() != null && 
					config.getVariable().containsKey(Constant.V_CONFIG_VARIABLE_COOKIE)) {
				
				postMethod.addRequestHeader(Constant.V_CONFIG_VARIABLE_COOKIE,
						(String) config.getVariable().get(Constant.V_CONFIG_VARIABLE_COOKIE));
				log.info("[HTTP Request Cookie]"+ (String) config.getVariable().get(Constant.V_CONFIG_VARIABLE_COOKIE));
			}
		}
		
		//这里两个逻辑：如果是只有一个参数，且keys是params，那么默认为直接发送body；否则才是key=value的方式
		if(parammap.size() == 1 && (parammap.containsKey("params") || parammap.containsKey("Params"))){
			String key = "";
			if( parammap.containsKey("params")){
				key = "params";
			}else if ( parammap.containsKey("Params")){
				key ="Params";
			}
			postMethod.setRequestHeader("Content-Type", "text/json;charset=utf-8");
			postMethod.setRequestBody(parammap.get(key).toString());
		}else{
			NameValuePair[] data = new NameValuePair[parammap.size()];
			int i = 0;
			for (Map.Entry<String, String> entry : parammap.entrySet()) {
				log.info("[HTTP post params]" + (String) entry.getKey() + ":"+ (String) entry.getValue() + ";");
				if (entry.getValue().toString().contains("###")) {
					
				} else {
					data[i] = new NameValuePair((String) entry.getKey(), (String) entry.getValue());
				}
				i++;
			}
			// 将表单的值放入postMethod中
			postMethod.setRequestBody(data);
		}
		
		
		Assert.assertNotNull("get request error,check the input file", postMethod);
		
		 String response = "";
		// 执行postMethod
				try {
					int statusCode = httpClient.executeMethod(postMethod);
					// HttpClient对于要求接受后继服务的请求，象POST和PUT等不能自动处理转发
					// 301或者302
					if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || 
					statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
						// 从头中取出转向的地址
						Header locationHeader = postMethod.getResponseHeader("location");
					    String location = null;
					    if (locationHeader != null) {
					    	location = locationHeader.getValue();
					    	log.info("The page was redirected to:" + location);
					    } else {
					    	log.info("Location field value is null.");
					    }
					}
					
					//读取内容 
					   byte[] responseBody = postMethod.getResponseBody();
					   if (responseBody == null) {
							log.error("[HTTP response is null]:please check login or servlet");
							return "";
						}
					   //统一处理为utf-8
					  response  = new String(responseBody,"UTF-8");
					   //处理内容
					   log.info("[The Post Request's Response]["+url+"]"+response);
					   
					   // 把response记录到output中
						File resfile = FileUtil.rewriteFile(file.getParentFile().getParent()
								+ Constant.FILENAME_OUTPUT, file.getName().substring(0,
								file.getName().indexOf("."))
								+ ".response", response);

						// 调用赋值
						vargen.processProps(resfile);
						//验证字面结果
						if(this.itest_expect!=null&&this.itest_expect.trim().length()!=0){
						Assert.assertTrue("response different with expect:[expect]:"+this.itest_expect+"[actual]:"+response, response.contains(this.itest_expect));
						}
//						if(this.itest_expect_json!=null&&this.itest_expect_json.trim().length()!=0){
//							VerifyJsonTypeResponseImpl.verifyResponseWithJson(this.itest_expect_json,response);
//
//						}
						
					   
				} catch (HttpException e) {
					   //发生致命的异常，可能是协议不对或者返回的内容有问题
					   log.error("Please check your provided http address!"+ e.getMessage());
					   
					} catch (IOException e) {
					   //发生网络异常
						log.error(e.getMessage());
					} catch (Exception e) {
						log.error("[HTTP REQUEST ERROR]:", e);
						//请求都异常了，判定case fail
						throw new RuntimeException("HTTP REQUEST ERROR:"+ e.getMessage());
					} 
					finally {
					   //释放连接
					   postMethod.releaseConnection();
					}
				return response;
	}
	
}
