package com.baidu.qa.service.test.template;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.baidu.qa.service.test.dto.Constant;
import com.baidu.qa.service.test.util.CCJSONUtils;
import com.baidu.qa.service.test.util.DateTimeUtil;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 
 * @author cuican
 * @date 2013-8-9
 * @classname VariableGenerator
 * @version 1.0.0
 * @desc 模板相关的操作，用以对tpl文件中的${}变量做赋值替换
 */
public class VariableGenerator {
	
	private static Logger log = Logger.getLogger(VariableGenerator.class);
	
	private static Map<String, VariableGenerator> all = new HashMap<String, VariableGenerator>();
	
	private Map props = null;
	
	public Map getProps() {
		return props;
	}

	public void setProps(Map props) {
		this.props = props;
	}


	private VariableGenerator parent = null;
	
	
	public VariableGenerator(Map map){
		props = map;
	}
	
	/**
	 * Factory method, key is the .properties full filename of the case
	 * @param filename
	 * @return VariableGenerator
	 */
	public static VariableGenerator getGenerator(String filename){
		if (all.get(filename) == null){
			all.put(filename, new VariableGenerator(filename));
		}
		return all.get(filename);
	}
	
	
	private VariableGenerator(String filename, VariableGenerator parent){
		this(filename);
		setParent(parent);
	}
	
	private VariableGenerator(){
	}
	
	private VariableGenerator(String filename){
		init(filename);
	}

	private void init(String filename) {
		//2012-10-16 merge the properties file in config.properties, mapping the key-value 'var=foo,bar,...'
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(filename));
		} catch (FileNotFoundException e) {
			log.warn("the file " + filename + " does not exist. if there is no need for this file, please ignore this warning.");
			log.warn(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		this.props = new HashMap();
		String value = props.getProperty("var");
		if(value == null || "".equals(value)){
			return;
		}
		try {
			initWithVarList(value);
		} catch (Exception e) {
			log.error("there is properly something wrong at the item 'var' in the file '" + filename + "' anyway, please check." );
			log.error(e.getMessage(), e);
		}
	}

	private void initWithVarList(String value) {
		String[] vars = value.split(",");
		for (String var : vars) {
			 if(!(var == null) && !("".equals(var))){
				 if(var.contains("=")){
					 String[] temp=var.split("=");
					 if(temp!=null&&temp.length==2&&temp[1].trim().equals("random")){						
							 this.props.put(temp[0], String.valueOf(DateTimeUtil.nextLong()));
					}				
					 
					 else if(temp!=null&&temp.length==1){
							this.props.put(temp[0], "");
					 }
					 else {
						 log.error("wrong var set:"+value);
					 }
				 }
				 else{
						this.props.put(var, "");

				 }
			}
		}
	}


	/**
	 * if one case is in a suit, set its parent with the suit's generator
	 * 
	 * @param parent
	 */
	public void setParent(VariableGenerator parent) {
		this.parent = parent;
	}

	/**
	 * get its parent Object
	 * 
	 * @return
	 */
	public VariableGenerator getParent() {
		return parent;
	}
	
	public void add2Map(Map map){
		this.props.putAll(map);
	}
	
	public Object removeFromMap(Object key){
		return this.props.remove(key);
	}
	
	public void processProps(File response){
		processProps(response, null);
	}
	/**
	 * with the response file, fulfill the .properties's value 
	 * 
	 * @param response
	 */
	public void processProps(File response, String type){

		if(this.parent != null){
			this.parent.processProps(response,type);
		}
		
		String basename = response.getName().split("\\.")[0];
		

		Set<Object> keys = props.keySet();
		for (Object key : keys) {
			if(((String)key).split("\\.")[0].equals(basename)){
				String value = null;
				if(Constant.FILE_TYPE_XML.equals(type)){
					value = searchFromXml((String)key, response);
				}else{
					value = searchFromJson((String)key, response);
				}
				if(value == null){
					log.warn("there is no field '" + key + "' found in the response: " + response);
				}
				props.put(key, value == null ? "" : value);
			}
		}
	}


	private String searchFromXml(String key, File response) {
		
		org.w3c.dom.Document doc = null;
		XPath xpath = null;
		
		// 将XML文档加载到DOM Document对象中
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new FileInputStream(response));
		} catch (Exception e) {
			log.error("parse the xml failed.");
			log.error(e.getMessage(), e);
			Assert.fail("parse the xml failed.");
		}
		// };xmlFilename);//"src/result.xml");
		// 创建 XPathFactory
		XPathFactory pathFactory = XPathFactory.newInstance();
		// 使用XPathFactory工厂创建 XPath 对象
		xpath = pathFactory.newXPath();
		xpath.setNamespaceContext(new NamespaceContext() {
			public String getNamespaceURI(String prefix) {
				if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
					return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
				} else if (prefix.equals("soap")) {
					return "http://schemas.xmlsoap.org/soap/envelope/";
				} else if (prefix.equals("ns1")) {
					return "http://api.baidu.com/sem/common/v2";
				} else if (prefix.equals("ns2")) {
					return "https://api.baidu.com/sem/sms/v2";
				} else if (prefix.equals("ns4")) {
					return "https://api.baidu.com/sem/nms/v2";
				}
				return XMLConstants.NULL_NS_URI;
			}
			public String getPrefix(String namespaceURI) {
				return null;
			}
			public Iterator getPrefixes(String namespaceURI) {
				return null;
			}
		});
		// 使用XPath对象编译XPath表达式
		XPathExpression pathExpression = null;
		String subKey = ((String)key).split("\\.")[1];
		String searchKey = "//" + subKey + "/text()";
		try {
			pathExpression = xpath.compile(searchKey);
		} catch (XPathExpressionException e) {
			log.error("the xpath ` " + searchKey + " ` is invalid.");
			log.error(e.getMessage(), e);
			Assert.fail("the xpath ` " + searchKey + " ` is invalid.");
		}
		// 计算 XPath 表达式得到结果
		Object result = null;
		try {
			result = pathExpression.evaluate(doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			log.error("the xpath ` " + searchKey + " ` is invalid.");
			log.error(e.getMessage(), e);
			Assert.fail("the xpath ` " + searchKey + " ` is invalid.");
		}
		// 节点集node-set转化为NodeList
		// 将结果强制转化成 DOM NodeList
		org.w3c.dom.NodeList nodes = (NodeList) result;
		
		if(nodes.getLength() == 0){
			log.error("nothing was found by the xpath ` " + searchKey + " `.");
			Assert.fail("nothing was found by the xpath ` " + searchKey + " `.");
		}else if(nodes.getLength() > 1){
			log.error("more than 1 items were found by the xpath ` " + searchKey + " `.");
			Assert.fail("more than 1 items were found by the xpath ` " + searchKey + " `.");
		}
		
		Node theOne = nodes.item(0);
		if (theOne == null) {
			return "";
		}
		String itemValue = theOne.getNodeValue().trim();
		return itemValue;
	}


	private String searchFromJson(String key, File response) {
		String subKey = ((String)key).split("\\.")[1];
		Scanner in = null;
		try {
			in = new Scanner(response);
		} catch (FileNotFoundException e) {
			log.error("the file '" + response + "' does not exist.");
			log.error(e.getMessage(), e);
		}
		StringBuilder sb = new StringBuilder();
		while(in.hasNextLine()){
			sb.append(in.nextLine());
		}
		String allLines = sb.toString();
		
		JSONObject jobj = JSONObject.fromObject((allLines == null || allLines.equals("")) ? "{}"  
                : allLines);  
		
		String value = CCJSONUtils.getFirst(jobj, subKey);
		
		return value;
	}


	/**
	 *  process the template , generate the real request file
	 * @param template
	 * @return
	 */
	public File processTemplate(File template) {
		return processTemplate(template, null);
	}
	
	/**
	 *  process the template with customed key-values, generate the real request file
	 * @param template
	 * @return
	 */
	public File processTemplate(File template, Map keyValue) {
		Configuration cfg = new Configuration();
		File path = template.getParentFile();
		String tName = template.getName();
		int lastDot = tName.lastIndexOf('.');
//		String realName = tName.split("\\.")[0];
		String realName = tName.substring(0, lastDot);
		//put it to ${case_path}/Output/
		File output = new File(path.getParent() + "/" + Constant.FILENAME_OUTPUT);
		if(!output.exists()){
			output.mkdir();
			log.info("mkdir " + output);
		}
		File real = new File(output + "/" + realName);
		//delete if exists
		if(real.exists()){
			log.info("delete existed filed first.");
			real.delete();
		}
		Writer out = null;
		try {
			
			cfg.setDirectoryForTemplateLoading(path);
			cfg.setObjectWrapper(new BeansWrapper());
			Template temp = cfg.getTemplate(tName);
			out = new PrintWriter(real);

			if (this.parent != null) {
				log.info("need to putall parent.props");
				this.props.putAll(this.parent.props);
			}
			if (keyValue != null && !keyValue.isEmpty()){
				log.info("need to putall customed vars");
				this.props.putAll(keyValue);
			}
			temp.process(makeLevelMap(this.props), out);
			out.flush();
		} catch (Exception e) {
			log.error("processing template errors, please check");
			log.error(e.getMessage(), e);
		}finally{
			try {
				out.close();
			} catch (IOException e) {
			}
		}

		return real;
	}
	
	
	
	/**
	 * kengdie de freemaker will parse the 'a.b=xxx' as map A with a map B, B has a key that value is b.
	 * so this method will make a plain map to a level-relationship-map.
	 * @param props
	 * @return
	 */
	private Map makeLevelMap(Map props){
		Map level = new HashMap();
		Set<Object> keys = props.keySet();
		for (Object key : keys) {
			String k = (String)key;
			if(k.contains(".")){
				String[] two = k.split("\\.");
				if(level.get(two[0]) == null){
					level.put(two[0], new HashMap());
				}
				((Map)(level.get(two[0]))).put(two[1], props.get(key));
			}else{
				level.put(key, props.get(key));
			}
		}
		
		return level;
	}
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		File prop = new File("E:\\cctest.properties");
		/*
		 * E:\\cctest.properties  content:
		 * 
		 * addAdgroup.adgroupId=
		 * 
		 */
		
		File res = new File("E:\\addAdgroup.response");
		/*
		 * E:\\addAdgroup.response  content:
		 * 
		 * {"jsonrpc":"2.0",
		 * "result":{
		 * 	"data":[
		 * 		{"adgroupId":131977020,
		 * 		"campaignId":21573518,
		 * 		"adgroupName":"ccc",
		 * 		"maxPrice":88.0,
		 * 		"pause":false,
		 * 		"negativeWords":[],
		 * 		"exactNegativeWords":[],
		 * 		"status":31,
		 * 		"opt":null}
		 * 		],
		 * "errors":[],
		 * "options":{
		 * 	"total":1,
		 * 	"success":1}
		 * },
		 * "id":"0"}
		 * 
		 */
		
		File template = new File("E:\\template.cc.ftl");
		/*
		 * E:\\template.cc.ftl  content:
		 * 
		 * {"opUser":111,"dataUser":222},{"creativeType":1, "tableId":0, "tablePositionId":0, "number":1, "adgroupId":${addAdgroup.adgroupId},{"options":{}}
		 * 
		 */
		VariableGenerator vg = VariableGenerator.getGenerator(prop.getAbsolutePath());
		
		vg.processProps(res);
		
//		System.out.println(vg.props.get("addAdgroup.adgroupId"));
		
		vg.processTemplate(template);
		/*
		 * expect: E:\\Output\\template.cc
		 * 
		 * {"opUser":111,"dataUser":222},{"creativeType":1, "tableId":0, "tablePositionId":0, "number":1, "adgroupId":131977020,{"options":{}}
		 * 
		 */
		
		
		
//		VariableGenerator vg = new VariableGenerator();
//		
//		Properties a = vg.getProps();
//		
//		for (Object obj : a.keySet()){
//			System.out.println(obj + "=" + a.getProperty((String)obj));
//		}
//		a.put("ccc", "99999");
//		a.store(new FileOutputStream("src/variable.propertirs"), null);
			
	}
	
}
