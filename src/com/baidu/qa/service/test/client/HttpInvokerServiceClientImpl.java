package com.baidu.qa.service.test.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.baidu.qa.service.test.dto.CaseData;
import com.baidu.qa.service.test.dto.Config;


public class HttpInvokerServiceClientImpl implements ServiceInterfaceClient{

	private static Log log = LogFactory.getLog(HttpInvokerServiceClientImpl.class);
	private static String beanFile = "httpinvoker-client.xml";
	
	public Object invokeServiceMethod(CaseData casedata, Config config) {
		
		if (casedata == null || config == null) {
			log.error("[HttpInvoker Request Error]caseinfo & config maybe null");
			throw new AssertionError("[HttpInvoker Request Error]caseinfo & config maybe null");
		}
		String serviceinfo = casedata.getAction();
		if(!serviceinfo.contains(":") || serviceinfo.split(":").length < 2){
			log.error("[HttpInvoker Request Error]The action must be 'beanname:methodname'");
			throw new AssertionError("[HttpInvoker Request Error]The action must be 'beanname:methodname'");
		}
		String beanName = serviceinfo.split(":")[0];
		String methodName = serviceinfo.split(":")[1];
		
		try {
			ApplicationContext ac = new FileSystemXmlApplicationContext(beanFile); 
			log.info("[HttpInvokerExecutor]"+beanFile);
			Object obj = ac.getBean(beanName);
			log.info("[HttpInvokerExecutor] get the bean "+beanName+" success");
			Class clazz = obj.getClass();
			
			ObjectMapper mapper = new ObjectMapper();
			Method[] methods =  clazz.getMethods();
			for(int i = 0;i < methods.length;i++){
				if(methods[i].getName().equals(methodName)){
					Class[] paramTypes = methods[i].getParameterTypes();

					Map paramMap = casedata.getInput();
					if(paramMap.size() != paramTypes.length){
						log.info("HttpInvokerExecutor]input params count not match "+methods[i].getName());
						continue;
					}
					Object[] testParams = this.castInputToParameters(paramMap, paramTypes);
					log.info("HttpInvokerExecutor]Invoke Method : "+methods[i].getName());
					for(Object o : testParams){
						log.info("HttpInvokerExecutor]HttpInvoker Request Param : "+o.toString());
					}
					Object robj = methods[i].invoke(obj, testParams);
					log.info("HttpInvokerExecutor]Get Httpinvoker Object Response : "+robj);
					
					StringWriter buf = new StringWriter();
		            mapper.writeValue(buf, robj);
		            log.info("HttpInvokerExecutor]Get Httpinvoker Json Response : "+buf.toString());
		            return buf.toString();
				}
			}
			
            
		} catch(Exception e){
			log.error("[HttpInvoker Request Error]",e);
			throw new AssertionError(e.getCause());
		}
		
		return null;
	}
	
	
	private Object[] castInputToParameters(Map paramMap,Class[] paramTypes){
		ObjectMapper mapper = new ObjectMapper();
		String paramname = "param";
		Object[] testParams = new Object[paramMap.size()];
		int k = 1;
		for(int j = 0;j < paramTypes.length;j++){
			try{
				
			log.info("[HttpInvokerExecutor]the paramater type is: "+paramTypes[j].getName());
			if(paramTypes[j].getName().equals(Long.class.getName()) || paramTypes[j].getName().equals("long"))
				testParams[j] = Long.valueOf(paramMap.get(paramname+k).toString());
			else if(paramTypes[j].getName().equals(Integer.class.getName()) || paramTypes[j].getName().equals("int"))
				testParams[j] = Integer.valueOf(paramMap.get(paramname+k).toString());
			else if(paramTypes[j].getName().equals(Double.class.getName()) || paramTypes[j].getName().equals("double"))
				testParams[j] = Double.valueOf(paramMap.get(paramname+k).toString());
			else if(paramTypes[j].getName().equals(Float.class.getName()) || paramTypes[j].getName().equals("float"))
				testParams[j] = Float.valueOf(paramMap.get(paramname+k).toString());
			else if(paramTypes[j].getName().equals(String.class.getName()) || paramTypes[j].getName().equals("string"))
				testParams[j] = paramMap.get(paramname+k).toString();
			else if(paramTypes[j].getName().equals(Boolean.class.getName()) || paramTypes[j].getName().equals("boolean"))
				testParams[j] = Boolean.valueOf(paramMap.get(paramname+k).toString());
			//if param type is List
			else if(paramTypes[j].getName().equals(List.class.getName()) ||
					paramTypes[j].getName().equals(Map.class.getName()) ||
					paramTypes[j].getName().equals(Set.class.getName()))
				testParams[j] =  mapper.readValue(paramMap.get(paramname+k).toString(),paramTypes[j]);
			//if param is java class 
			else {
				testParams[j] = mapper.readValue(paramMap.get(paramname+k).toString(),paramTypes[j]);
				log.info("[HttpInvokerExecutor]the object param is "+paramMap.get(paramname+k).toString());
			}
			
				
					
			}catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(Exception e){
				log.error("[HttpInvoker Request Error]",e);
				throw new AssertionError("[Cast Input To Paramater Error]"+e.getCause());
			}
			k++;
				
		}
		
		return testParams;
	}
	

}
