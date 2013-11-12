package com.baidu.qa.service.test.client;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.qa.service.test.dto.CaseData;
import com.baidu.qa.service.test.dto.Config;
import com.baidu.qa.service.test.dto.Constant;
import com.baidu.qa.service.test.util.FileUtil;
import com.meterware.httpunit.HttpException;


/**
 * 使用httpclient实现的http请求post/get执行器
 * @author xuedawei
 *
 */
public class HttpServiceClientImpl implements ServiceInterfaceClient{
	
	private static Log log = LogFactory.getLog(HttpServiceClientImpl.class);
	private static final int timeout = 30000;
	private CaseData casedata;
	private Config config;

	
	
	public Object invokeServiceMethod(CaseData casedata, Config config) {
		this.casedata = casedata;
		this.config = config;
		
		//如果是tpl文件，进行替换和重新set input
		if(casedata.getInputFile().getName().endsWith(Constant.FILE_TYPE_TPL)){
			this.casedata.setInputFile(this.casedata.getVarGen().processTemplate(this.casedata.getInputFile()));
			this.casedata.setInput();
		}
		
		String response = "";
		
		if (casedata.getRequesttype().equalsIgnoreCase(Constant.HTTP_REQUEST_TYPE)){
			response = requestGetMethod();
		} else{
			response = requestPostMethod();
		}
				
		// 把response记录到output中
		File resfile = FileUtil.rewriteFile(casedata.getCaselocation() + Constant.FILENAME_OUTPUT, 
				casedata.getInputFile().getName().substring(0, casedata.getInputFile().getName().indexOf("."))+ ".response",
				response);
		// 调用赋值
		casedata.getVarGen().processProps(resfile);
		log.info("[HTTP request end,and save response to output file]");
		return response;
	}
	
	
	
	
	/**
	 * 处理post请求
	 * @return
	 */
	private String requestPostMethod(){
		//构造HttpClient的实例
		HttpClient httpClient = new HttpClient();
		//设置超时时间
		httpClient.setConnectionTimeout(timeout);
		httpClient.setTimeout(timeout);	
		httpClient.getParams().setParameter("http.protocol.content-charset", "UTF-8");
		
		String host = config.getHost();
		String respStr = "";
		if(host == null || host.length() == 0){
			return  null;
		}
		
		PostMethod postMethod = new PostMethod(host + casedata.getAction());
		//cookie copy
		if (config.getVariable() != null && 
				config.getVariable().containsKey(Constant.V_CONFIG_VARIABLE_COOKIE)) {
			postMethod.addRequestHeader(Constant.V_CONFIG_VARIABLE_COOKIE,
					(String) config.getVariable().get(Constant.V_CONFIG_VARIABLE_COOKIE));
			log.info("[HTTP Request Cookie]"+ (String) config.getVariable().get(Constant.V_CONFIG_VARIABLE_COOKIE));
		}

		// 填入各个表单域的值
		Map<String, Object> parammap = casedata.getInput();
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
		}
		else{
			NameValuePair[] data = new NameValuePair[parammap.size()];
			int i = 0;
			for (Map.Entry<String, Object> entry : parammap.entrySet()) {
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
			   respStr = new String(responseBody,"UTF-8");
			   //处理内容
			   log.info("[The Post Request's Response]["+casedata.getAction()+"]"+respStr);
			   
		} catch (HttpException e) {
			   //发生致命的异常，可能是协议不对或者返回的内容有问题
			   log.error("Please check your provided http address!"+ e.getMessage());
			   
			} catch (IOException e) {
			   //发生网络异常
				log.error(e.getMessage());
			} catch (Exception e) {
				log.error("[HTTP REQUEST ERROR]:", e);
				//请求都异常了，判定case fail
				throw new AssertionFailedError("HTTP REQUEST ERROR:"+ e.getMessage());
			} 
			finally {
			   //释放连接
			   postMethod.releaseConnection();
			}
			
			
			return respStr;
		
	}
	
	
	
	
	
	/**
	 * 处理get请求
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String  requestGetMethod() {
		//构造HttpClient的实例
		HttpClient httpClient = new HttpClient();
		//设置超时时间
		httpClient.setConnectionTimeout(timeout);
		httpClient.setTimeout(timeout);	
				
		String host = config.getHost();
		String respStr = "";
		if(host == null || host.length() == 0){
			return  null;
		}
		
		
		//创建GET方法的实例
		GetMethod getMethod = new GetMethod(host + casedata.getAction());
		//填充cookie
		if (config.getVariable() != null &&
				config.getVariable().containsKey(Constant.V_CONFIG_VARIABLE_COOKIE)) {
			
			getMethod.setRequestHeader(Constant.V_CONFIG_VARIABLE_COOKIE,
					(String) config.getVariable().get(Constant.V_CONFIG_VARIABLE_COOKIE));
			
			log.info("[HTTP Request Cookie]"+ (String) config.getVariable().get(Constant.V_CONFIG_VARIABLE_COOKIE));
		}
		//使用系统提供的默认的恢复策略
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
		    new DefaultHttpMethodRetryHandler());
		try {
		   //执行getMethod
		   int statusCode = httpClient.executeMethod(getMethod);
		   if (statusCode != HttpStatus.SC_OK) {
			   log.error("[Method failed] " + getMethod.getStatusLine());
		   }
		   //读取内容 
		   byte[] responseBody = getMethod.getResponseBody();
		   if (responseBody == null) {
				log.error("[HTTP response is null]:please check login or servlet");
				return "";
			}
		   //统一处理为utf-8
		   respStr = new String(responseBody,"UTF-8");
		   //处理内容
		   log.info("[The Get Request's Response]["+casedata.getAction()+"]"+respStr);
		   
		} catch (HttpException e) {
		   //发生致命的异常，可能是协议不对或者返回的内容有问题
		   log.error("Please check your provided http address!"+ e.getMessage());
		   
		} catch (IOException e) {
		   //发生网络异常
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error("[HTTP REQUEST ERROR]:", e);
			//请求都异常了，判定case fail
			throw new AssertionFailedError("HTTP REQUEST ERROR:"+ e.getMessage());
		} 
		finally {
		   //释放连接
		   getMethod.releaseConnection();
		}
		
		
		return respStr;
	}
	
	
	

}
