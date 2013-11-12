package com.baidu.qa.service.test.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.baidu.gson.Gson;
import com.baidu.gson.GsonBuilder;
import com.baidu.gson.JsonElement;
import com.baidu.gson.JsonObject;
import com.baidu.qa.service.test.dto.CaseData;
import com.baidu.qa.service.test.dto.Config;
import com.baidu.qa.service.test.dto.Constant;
import com.baidu.rpc.McpackProcessor;
import com.baidu.rpc.exception.ExceptionHandler;
import com.baidu.rpc.exception.InternalErrorException;
import com.baidu.rpc.exception.JsonRpcException;
import com.baidu.rpc.exception.ParseErrorException;
import com.baidu.rpc.exception.ServerErrorException;

public class JsonRpcServiceClientImpl implements ServiceInterfaceClient {
	
	private static Logger log = Logger.getLogger(JsonRpcServiceClientImpl.class);
	
	final static Gson gson = new GsonBuilder().serializeNulls()
			.disableHtmlEscaping().serializeSpecialFloatingPointValues()
			.create();

	private static final String encoding = "gbk";

	protected AtomicInteger counter = new AtomicInteger();
	
	protected ExceptionHandler exceptionHandler = new ExceptionHandler();

//	private String url;

	private int _connectTimeout = 30000;

	private int _readTimeout = 30000;

	public Object invokeServiceMethod(CaseData casedata, Config config) {
		
		//如果是tpl文件，进行替换和重新set input
		if(casedata.getInputFile().getName().endsWith(Constant.FILE_TYPE_TPL)){
			casedata.setInputFile(casedata.getVarGen().processTemplate(casedata.getInputFile()));
			casedata.setInput();
		}
		
		String input = casedata.getInputJson();
		
		String action = casedata.getAction();
		
		String [] serviceAndMethod = getSmFromAction(action);
		
		String host = "";
	
		
			host=config.getHost();
		
		
		String actual = null;
		
		try {
			actual = invoke(host, serviceAndMethod, input);
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
		return actual;
	}
	
	
	
	/**
	 * action : XxxApi/xxx
	 * @param action
	 * @return
	 */
	private String[] getSmFromAction(String action) {
		return action.split("/");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.rpc.client.RpcProxyBase#deserialize(byte[])
	 */
	protected JsonElement deserialize(byte[] req) throws ParseErrorException {
		return  new McpackProcessor().deserialize(encoding, req);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.baidu.rpc.client.RpcProxyBase#serialize(com.google.gson.JsonElement)
	 */
	protected byte[] serialize(JsonElement res) throws ParseErrorException {
		return  new McpackProcessor().serialize(encoding, res);
	}


	public String invoke(String host, String[] serviceAndMethod, String input)
			throws Throwable {
		try {
			int id = counter.getAndIncrement();
			JsonElement request = makeRequest(id, serviceAndMethod, input);
			byte[] reqBytes = serialize(request);
			// log.debug("request bytes size is " + reqBytes.length);
			
			String url = host + serviceAndMethod[0];
			
			log.info("input: " + input);
			log.info("url: " + url);
			
			HttpURLConnection connection = (HttpURLConnection) new URL(url)
					.openConnection();
			if (_connectTimeout > 0) {
				connection.setConnectTimeout(_connectTimeout);
			}
			if (_readTimeout > 0) {
				connection.setReadTimeout(_readTimeout);
			}
			sendRequest(reqBytes, connection);
			byte[] resBytes = null;
			resBytes = readResponse(connection);
			JsonElement resJson = deserialize(resBytes);
//			return parseResult(id, resJson, method);
			return resJson.toString();
		} catch (IOException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * 读取服务器返回的信息
	 * 
	 * @param connection
	 * @return 读取到的数据
	 * @throws IOException
	 * @throws JsonRpcException
	 */
	protected byte[] readResponse(URLConnection connection)
			throws JsonRpcException {
		byte[] resBytes;
		InputStream in = null;
		HttpURLConnection httpconnection = (HttpURLConnection) connection;
		try {
			if (httpconnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				in = httpconnection.getInputStream();
			} else {
				if (httpconnection.getContentType().equals(contentType())
						&& httpconnection.getErrorStream() != null) {
					in = httpconnection.getErrorStream();
				} else {
					in = httpconnection.getInputStream();
				}
			}
			int len = httpconnection.getContentLength();
			if (len <= 0) {
				throw new InternalErrorException("no response to get.");
			}
			resBytes = new byte[len];
			int offset = 0;
			while (offset < resBytes.length) {
				int bytesRead = in.read(resBytes, offset, resBytes.length
						- offset);
				if (bytesRead == -1)
					break; // end of stream
				offset += bytesRead;
			}
			if (offset <= 0) {
				throw new InternalErrorException("there is no service to "
						+ connection.getURL());
			}
			// log.debug("response bytes size is " + offset);
		} catch (IOException e) {
			throw new InternalErrorException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new InternalErrorException(e);
				}
			}
		}
		return resBytes;
	}

	/**
	 * 向服务器发送信息
	 * 
	 * @param reqBytes
	 * @param connection
	 * @throws IOException
	 */

	protected void sendRequest(byte[] reqBytes, URLConnection connection) {
		HttpURLConnection httpconnection = (HttpURLConnection) connection;
		OutputStream out = null;
		try {
			httpconnection.setRequestMethod("POST");
			httpconnection.setUseCaches(false);
			httpconnection.setDoInput(true);
			httpconnection.setDoOutput(true);
			httpconnection.setRequestProperty("Content-Type", contentType()
					+ ";charset=" + encoding);
			httpconnection.setRequestProperty("Content-Length", ""
					+ reqBytes.length);
			httpconnection.connect();
			out = httpconnection.getOutputStream();
			out.write(reqBytes);
		} catch (Exception e) {
			throw new InternalErrorException(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new InternalErrorException(e);
				}
			}
		}
	}

	/**
	 * 组装rpc数据报
	 * 
	 * @param serviceAndMethod
	 * @param input
	 * @return 生成的请求数据
	 * @throws ParseErrorException
	 */
	protected JsonElement makeRequest(int id, String[] serviceAndMethod, String input)
			throws ParseErrorException {
		String name = serviceAndMethod[1];
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("{\"jsonrpc\":\"2.0\",\"method\":\"").append(name).append("\",\"params\":[");
		
		if (input != null) {
			sb.append(input);
		} else {
			sb.append("{}");
		}
		
		sb.append("],\"id\":\"").append(id).append("\"}");
		
		log.info(sb);
		
//		Map<String, Object> all = new HashMap<String, Object>(); 
		
		JSONObject a = JSONObject.fromObject(sb.toString());
		
		return new Gson().toJsonElement(a);
	}

	/**
	 * 处理接受到的rpc数据报
	 * 
	 * @param ele
	 * @param method
	 * @return 调用的返回值
	 * @throws Exception
	 */
	@Deprecated
	protected Object parseResult(int id, JsonElement ele, Method method)
			throws Exception {
		JsonObject res = (JsonObject) ele;
		if (!res.get("jsonrpc").getAsString().equals("2.0")) {
			throw new InternalErrorException();
		}
		JsonElement result = res.get("result");
		if (result != null) {
			if (res.get("id").getAsInt() != id) {
				throw new InternalErrorException("no id in response");
			} else {
				return gson.fromJson(result, method.getGenericReturnType());
			}
		} else {
			JsonElement e = res.get("error");
			if (e != null) {
				JsonRpcException jre = exceptionHandler.deserialize(e);
				if (jre instanceof ServerErrorException) {
					String msg = jre.getMessage();
					Class<?>[] exp_types = method.getExceptionTypes();
					for (Class<?> exp_type : exp_types) {
						if (msg.equals(exp_type.getSimpleName())) {
							Exception custom_exp = (Exception) exp_type
									.newInstance();
							custom_exp.initCause(jre.getCause());
							throw custom_exp;
						}
					}
				}
				throw jre;
			} else {
				throw new InternalErrorException("no error or result returned");
			}
		}
	}
	
	protected String contentType() {
		return "application/baidu.mcpack-rpc";
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
