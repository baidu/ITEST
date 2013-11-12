/*  
 * 	Copyright(C) 2010-2013 Baidu Group
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *  
 */

package com.baidu.qa.service.test.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import junit.framework.AssertionFailedError;

import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.EntityUtils;

import com.baidu.qa.service.test.dto.Config;
import com.baidu.qa.service.test.dto.Constant;
import com.baidu.qa.service.test.template.VariableGenerator;
import com.baidu.qa.service.test.util.FileUtil;

/**
 * 
 * @author cuican
 * @date 2013-8-30
 * @classname SoapReqImpl
 * @version 1.0.0
 * @desc soap协议的client，被复用到setup，teardown
 */
public class SoapReqImpl {

	private static Log log = LogFactory.getLog(SoapReqImpl.class);

	public static Object requestSoap(File file, Config config, VariableGenerator vargen) {
		String xml  = FileUtil.readFileByLines(file);
		String[] lines= xml.split("-->");
		if(!lines[0].trim().startsWith("<!--")){
			log.error("has no url");
			return null;
		}
		String[] urls =lines[0].substring(lines[0].indexOf("<!--")+4).split("=");
		if(urls.length!=2){
			log.error("wrong requestSoap file:"+file.getPath());
			return null;
		}
		String caseAction="";
		if(urls[0].trim().equals(Constant.KW_ITEST_URL)){
			 caseAction=urls[1];
		}
		else {
			log.error("has no url in requestSoap file:"+file.getPath());
			return null;
		}
		return requestSoap(caseAction,file,config,vargen);
	}
	
	
	
	
	public static  Object requestSoap(String caseAction,File file, Config config, VariableGenerator vargen){
		String host=config.getHost();
		String ip = null ;
		int port = 0;
		String action = null;
		String method = null; 
		String xml  = FileUtil.readFileByLines(file);

		boolean isHttps = false;
		try {
			/*
			 "http://xxx:8880/yyy"
			 host == "http://xxx:8880/sem/"
			 casedata.getAction() == "yyy"
			*/
			String [] protocolAndUrl = host.split("://");
			
			if("https".equals(protocolAndUrl[0])){
				isHttps = true;
			}
			String withoutProtocol = host.split("//")[1];
			String [] uriAndAction = withoutProtocol.split("/", 2);
			String [] ipAndPort = uriAndAction[0].split(":");
			ip = ipAndPort[0];
			if(ipAndPort.length != 2){
				port = isHttps ? 443 : 80;
			}else{
				port = Integer.parseInt(ipAndPort[1]);
			}
			int indexOfSlash = caseAction.lastIndexOf('/');
			method = caseAction.substring(indexOfSlash, caseAction.length());
			action = "/" + uriAndAction[1] + caseAction.substring(0, indexOfSlash);
			
		} catch (Exception e) {
			log.error("wrong format host and action in the config~!");
			log.error(e.getMessage(), e);
		}
		
		String res = null;
		
		try {
			res = sendSoap(config.getHost(),ip, port, action, method, xml, isHttps);
		} catch (Exception e) {
			log.error("[invoke soap service error]:");
			log.error(e.getMessage(), e);
			throw new AssertionFailedError("[invoke soap service error]:"
					+ file.getPath());
		}
		

			log.info("[HTTP request end]" + res);
			// 把response记录到output中
			File resfile = FileUtil.rewriteFile(file.getParentFile()
					.getParent()
					+ Constant.FILENAME_OUTPUT, file.getName().substring(0,
					file.getName().indexOf("."))
					+ ".response", res);

			// 调用赋值
			vargen.processProps(resfile,Constant.FILE_TYPE_XML);

			return res;
		

	}

	
	
	
	private static String sendSoap(String hosturl,String ip, int port, String action, String method,
			String xml, boolean isHttps) throws Exception {
		if(isHttps){
			return sendSoapViaHttps(hosturl,ip, port, action, method, xml);
		}

		HttpParams params = new SyncBasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");// must be UTF-8
		HttpProtocolParams.setUserAgent(params, "itest-by-HttpCore/4.2");


		HttpProcessor httpproc = new ImmutableHttpProcessor(
				new HttpRequestInterceptor[] {
						// Required protocol interceptors
						new RequestContent(), new RequestTargetHost(),
						// Recommended protocol interceptors
						new RequestConnControl(), new RequestUserAgent(),
						new RequestExpectContinue() });

		HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

		HttpContext context = new BasicHttpContext(null);

		// log.info("ip:port - " + ip + ":" + port );
		HttpHost host = new HttpHost(ip, port);// TODO

		DefaultHttpClientConnection conn = new DefaultHttpClientConnection();
		conn.setSocketTimeout(10000);
		HttpConnectionParams.setSoTimeout(params, 10000);
		ConnectionReuseStrategy connStrategy = new DefaultConnectionReuseStrategy();

		context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
		context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, host);

		String res = null;

		try {
			// HttpEntity requestBody = new
			// ByteArrayEntity(xml.getBytes("UTF-8"));// TODO
			byte[] b = xml.getBytes("UTF-8"); // must be UTF-8
			InputStream is = new ByteArrayInputStream(b, 0, b.length);

			HttpEntity requestBody = new InputStreamEntity(is, b.length,
					ContentType.create("text/xml;charset=UTF-8"));// must be
																	// UTF-8

			// .create("application/xop+xml; charset=UTF-8; type=\"text/xml\""));//
			// TODO

			// RequestEntity re = new InputStreamRequestEntity(is, b.length,
			// "application/xop+xml; charset=UTF-8; type=\"text/xml\"");
			// postmethod.setRequestEntity(re);

			if (!conn.isOpen()) {
				Socket socket = new Socket(host.getHostName(), host.getPort());
				conn.bind(socket, params);
			}
			BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest(
					"POST", action);

			// add the 3 headers below
			request.addHeader("Accept-Encoding", "gzip,deflate");
			request.addHeader("SOAPAction", hosturl + action
					+ method);// SOAP action
			request.addHeader("uuid", "itest");// for editor token of DR-Api

			request.setEntity(requestBody);
			log.info(">> Request URI: " + request.getRequestLine().getUri());

			request.setParams(params);
			httpexecutor.preProcess(request, httpproc, context);
			HttpResponse response = httpexecutor
					.execute(request, conn, context);
			response.setParams(params);
			httpexecutor.postProcess(response, httpproc, context);

			log.info("<< Response: " + response.getStatusLine());

			String contentEncoding = null;
			Header ce = response.getEntity().getContentEncoding();
			if (ce != null) {
				contentEncoding = ce.getValue();
			}

			if (contentEncoding != null
					&& contentEncoding.indexOf("gzip") != -1) {
				GZIPInputStream gzipin = new GZIPInputStream(response
						.getEntity().getContent());
				Scanner in = new Scanner(new InputStreamReader(gzipin, "UTF-8"));
				StringBuilder sb = new StringBuilder();
				while (in.hasNextLine()) {
					sb.append(in.nextLine()).append(
							System.getProperty("line.separator"));
				}
				res = sb.toString();
			} else {
				res = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
			log.info(res);

			log.info("==============");
			if (!connStrategy.keepAlive(response, context)) {
				conn.close();
			} else {
				log.info("Connection kept alive...");
			}
		} finally {
			try {
				conn.close();
			} catch (IOException e) {
			}
		}
		return res;
	}
	
	private static String sendSoapViaHttps(String hosturl,String ip, int port, String action,
			String method, String xml) {

		String reqURL = "https://" + ip + ":" + port + action;
//		Map<String, String> params = null;
		long responseLength = 0; // 响应长度
		String responseContent = null; // 响应内容

		HttpClient httpClient = new DefaultHttpClient(); // 创建默认的httpClient实例
		httpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 10000);
		
		X509TrustManager xtm = new X509TrustManager() { // 创建TrustManager
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
		try {
			// TLS1.0与SSL3.0基本上没有太大的差别，可粗略理解为TLS是SSL的继承者，但它们使用的是相同的SSLContext
			SSLContext ctx = SSLContext.getInstance("TLS");

			// 使用TrustManager来初始化该上下文，TrustManager只是被SSL的Socket所使用
			ctx.init(null, new TrustManager[] { xtm }, null);

			// 创建SSLSocketFactory
			SSLSocketFactory socketFactory = new SSLSocketFactory(ctx);

			// 通过SchemeRegistry将SSLSocketFactory注册到我们的HttpClient上
			httpClient.getConnectionManager().getSchemeRegistry()
					.register(new Scheme("https", port, socketFactory));

			HttpPost httpPost = new HttpPost(reqURL); // 创建HttpPost
			
			// add the 3 headers below
			httpPost.addHeader("Accept-Encoding", "gzip,deflate");
			httpPost.addHeader("SOAPAction", hosturl + action
					+ method);// SOAP action
			httpPost.addHeader("uuid", "itest");// for editor token of DR-Api

			// HttpEntity requestBody = new
			// ByteArrayEntity(xml.getBytes("UTF-8"));// TODO
			byte[] b = xml.getBytes("UTF-8"); // must be UTF-8
			InputStream is = new ByteArrayInputStream(b, 0, b.length);

			HttpEntity requestBody = new InputStreamEntity(is, b.length,
					ContentType.create("text/xml;charset=UTF-8"));// must be
																	// UTF-8
			httpPost.setEntity(requestBody);
			log.info(">> Request URI: " + httpPost.getRequestLine().getUri());

			HttpResponse response = httpClient.execute(httpPost); // 执行POST请求
			HttpEntity entity = response.getEntity(); // 获取响应实体

			if (null != entity) {
				responseLength = entity.getContentLength();
				
				String contentEncoding = null;
				Header ce = response.getEntity().getContentEncoding();
				if (ce != null) {
					contentEncoding = ce.getValue();
				}

				if (contentEncoding != null
						&& contentEncoding.indexOf("gzip") != -1) {
					GZIPInputStream gzipin = new GZIPInputStream(response
							.getEntity().getContent());
					Scanner in = new Scanner(new InputStreamReader(gzipin, "UTF-8"));
					StringBuilder sb = new StringBuilder();
					while (in.hasNextLine()) {
						sb.append(in.nextLine()).append(
								System.getProperty("line.separator"));
					}
					responseContent = sb.toString();
				} else {
					responseContent = EntityUtils.toString(response.getEntity(), "UTF-8");
				}
				
				EntityUtils.consume(entity); // Consume response content
			}
			log.info("请求地址: " + httpPost.getURI());
			log.info("响应状态: " + response.getStatusLine());
			log.info("响应长度: " + responseLength);
			log.info("响应内容: " + responseContent);
		} catch (KeyManagementException e) {
			log.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		} catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
		} catch (ParseException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			httpClient.getConnectionManager().shutdown(); // 关闭连接,释放资源
			return responseContent;
		}
	}

}
