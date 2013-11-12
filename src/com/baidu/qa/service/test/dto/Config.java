package com.baidu.qa.service.test.dto;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


public class Config {
	private Log log = LogFactory.getLog(Config.class);
	private ObjectMapper mapper = new ObjectMapper();
	private String host;

	private Map<String, Object> variable = new HashMap<String, Object>();

	private String prod;
	private String casetype;
	private String suitetype = "";
	private Map<String, String> replace_time = new HashMap<String, String>();
	private int before_wait_time = 0;
	private int after_wait_time = 0;
	private int suite_before_wait_time = 0;
	private int suite_after_wait_time = 0;
	private Map<String, Object> downloadfile=new HashMap<String, Object>();
	private boolean hasVar=false;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}



	public Map<String, Object> getVariable() {
		return variable;
	}

	public void setVariable(String variablestr) {

		if (variablestr != null && variablestr.equals("") == false) {
			JSONObject obj = (JSONObject) JSONSerializer.toJSON(variablestr);
			for (Iterator<?> iter = obj.keys(); iter.hasNext();) {
				String key = (String) iter.next();
				variable.put(key, obj.getString(key));
			}
		}

	}

	public void addVariable(String key, Object value) {
		variable.put(key, value);
	}

	public String getProd() {
		return prod;
	}

	public void setProd(String prod) {
		this.prod = prod;
	}

	public String getCasetype() {
		return casetype;
	}

	public void setCasetype(String casetype) {
		this.casetype = casetype;
	}

	public String getSuitetype() {
		return suitetype;
	}

	public void setSuitetype(String suitetype) {
		this.suitetype = suitetype;
	}

	public Map<String, String> getReplace_time() {
		return replace_time;
	}

	public void setReplace_time(String replaceTimeStr) {
		if (replaceTimeStr != null && replaceTimeStr.equals("") == false) {
			JSONObject obj = (JSONObject) JSONSerializer.toJSON(replaceTimeStr);
			for (Iterator<?> iter = obj.keys(); iter.hasNext();) {
				String key = (String) iter.next();
				replace_time.put(key, obj.getString(key));
			}
		}
	}

	public void setWait_time(String waitTimeStr) {
		if (waitTimeStr != null && waitTimeStr.equals("") == false) {
			JSONObject obj = (JSONObject) JSONSerializer.toJSON(waitTimeStr);
			for (Iterator<?> iter = obj.keys(); iter.hasNext();) {
				String key = (String) iter.next();
				if (key.trim().equalsIgnoreCase("before")) {
					String time = obj.getString(key);
					if (time==null||time.trim().length()==0) {
						this.before_wait_time = 0;
					} else {
						this.before_wait_time = Integer.valueOf(time)
								.intValue()*1000;
					}

				} else if (key.trim().equalsIgnoreCase("after")) {
					String time = obj.getString(key);
					if (time==null||time.trim().length()==0) {
						this.after_wait_time = 0;
					} else {
						this.after_wait_time = Integer.valueOf(time).intValue()*1000;
					}

				}
				else if (key.trim().equalsIgnoreCase("after_suite")) {
					String time = obj.getString(key);
					if (time==null||time.trim().length()==0) {
						this.suite_after_wait_time= 0;
					} else {
						this.suite_after_wait_time = Integer.valueOf(time).intValue()*1000;
					}

				}
				else if (key.trim().equalsIgnoreCase("before_suite")) {
					String time = obj.getString(key);
					if (time==null||time.trim().length()==0) {
						this.suite_before_wait_time = 0;
					} else {
						this.suite_before_wait_time = Integer.valueOf(time).intValue()*1000;
					}

				}
			}
		}
	}

	public int getBefore_wait_time() {

		return before_wait_time;
	}

	public int getAfter_wait_time() {
		return after_wait_time;
	}
	public int getSuite_before_wait_time() {
		return suite_before_wait_time;
	}

	public int getSuite_after_wait_time() {
		return suite_after_wait_time;
	}
	public Map<String, Object> getDownloadfile() {
		return downloadfile;
	}
	public boolean isHasVar() {
		return hasVar;
	}
	public void setHasVar(boolean hasVar) {
		this.hasVar = hasVar;
	}
	@SuppressWarnings("unchecked")
	public void setDownloadfile(String downloadfile) {
		try {
			this.downloadfile = mapper.readValue(downloadfile, Map.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}


	public Config() {

	}

	public Config(String CASEPATH) throws FileNotFoundException {
		InputStream cfin = new BufferedInputStream(new FileInputStream(CASEPATH+ Constant.FILENAME_CONFIG));
	
		Properties cfInfo = new Properties();
		try {
			cfInfo.load(cfin);
			
			this.setProd(cfInfo.getProperty("prod"));
			this.setHost(cfInfo.getProperty("host"));
			this.setVariable(cfInfo.getProperty("variable"));
			this.setCasetype(cfInfo.getProperty("casetype"));
			if (cfInfo.containsKey("replace_time")) {
			this.setReplace_time(cfInfo.getProperty("replace_time"));
			}
			if (cfInfo.containsKey("wait_time")) {
			this.setWait_time(cfInfo.getProperty("wait_time"));
			}
			if (cfInfo.containsKey("suitetype")) {
				this.setSuitetype(cfInfo.getProperty("suitetype"));
			}
			if (cfInfo.containsKey("downloadfile")) {
				this.setDownloadfile(cfInfo.getProperty("downloadfile"));
			}
			//判断是否存在var替换
			if (cfInfo.containsKey("var")&&cfInfo
					.getProperty("var").trim().length()!=0) {
				this.setHasVar(true);
			}
			cfin.close();
		} catch (IOException e) {

			log.error("[get config error]:", e);
		}
	}

}
