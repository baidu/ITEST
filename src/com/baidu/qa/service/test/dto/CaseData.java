package com.baidu.qa.service.test.dto;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.qa.service.test.template.VariableGenerator;
import com.baidu.qa.service.test.util.FileUtil;



/**
 * 
 * @author xuedawei
 * @date 2013-8-9
 * @classname CaseData
 * @version 1.0.0
 * @desc case文件对应的实体类
 */
public class CaseData {

	private String caseid = "0";
	private String desc = "";
	private String action = "";
	private String status="START";
	private VariableGenerator varGen=null;
	private boolean hasVar=false;
	private File inputFile=null;
	/**
	 * setup目录下为数据构造，文件名数组
	 */
	private File[] setup = new File[]{};
	/**
	 * input目录下请求参数，以k-v格式标示
	 */
	private Map<String,Object> input = new HashMap<String, Object>();
	/**
	 * expect目录下是验证结果的数据文件，不同的文件名代表不同验证方式
	 */
	private File[] expect = new File[]{};
	/**
	 * teardown目录下为数据清理
	 */
	private File[] teardown = new File[]{};
	/**
	 * case所在目录
	 */
	private String caselocation = "";
	/**
	 * list<map>结构的输入参数，是由于某些http请求参数的key是相同的
	 */
	private List<Map<String,Object>> inputlist = new ArrayList<Map<String,Object>>();
	
	private String inputxml = "";
	
	private String inputJson = "";
	
	private String requesttype ="";
	
	

	public String getCaseid() {
		return caseid;
	}
	public void setCaseid(String caseid) {
		this.caseid = caseid;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public File[] getSetup() {
		return setup;
	}
	public void setSetup(File[] setup) {
		this.setup = setup;
	}
	public Map<String,Object> getInput() {
		return input;
	}
	private void setInput(Map<String,Object> input) {
		this.input = input;
	}
	public File[] getExpect() {
		return expect;
	}
	public void setExpect(File[] expect) {
		this.expect = expect;
	}
	public File[] getTeardown() {
		return teardown;
	}
	public void setTeardown(File[] teardown) {
		this.teardown = teardown;
	}

	public VariableGenerator getVarGen() {
		return varGen;
	}
	public void setVarGen(VariableGenerator varGen) {
		this.varGen = varGen;
	}
	public String getCaselocation() {
		return caselocation;
	}
	public void setCaselocation(String caselocation) {
		this.caselocation = caselocation;
	}
	public void setInputlist(List<Map<String,Object>> inputlist) {
		this.inputlist = inputlist;
	}
	public List<Map<String,Object>> getInputlist() {
		return inputlist;
	}

	public String getRequesttype() {
		return requesttype;
	}
	public void setRequesttype(String requesttype) {
		this.requesttype = requesttype;
	}
	public void setInputJson(String inputJson) {
		this.inputJson = inputJson;
	}
	public String getInputJson() {
		return inputJson;
	}
	public File getInputFile() {
		return inputFile;
	}
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}
	public void setInput()
	{

		if (this.getRequesttype() == null || !this.getRequesttype().trim().equalsIgnoreCase(Constant.HTTP_REQUEST_TYPE)) {

			List<String> paramlist = FileUtil.getListFromFile(inputFile);
			
			Map<String, Object> parammap = new HashMap<String, Object>();
			for (String paramstr : paramlist) {
				if (parammap.containsKey(paramstr.substring(0, paramstr.indexOf("="))) == false) {
					parammap.put(paramstr.substring(0, paramstr.indexOf("=")), paramstr.substring(paramstr.indexOf("=") + 1));
				} else {
					parammap.put(paramstr.substring(0, paramstr.indexOf("=")),parammap.get(paramstr.substring(0, paramstr.indexOf("=")))
									+ "###"+ paramstr.substring(paramstr.indexOf("=") + 1));
				}
			}
			this.setInput(parammap);
		} else if (this.getRequesttype() != null
				&& this.getRequesttype().trim().equalsIgnoreCase(
						Constant.HTTP_REQUEST_TYPE)) {
			this.setInput(null);

		}
	
	}
	public boolean isHasVar() {
		return hasVar;
	}
	public void setHasVar(boolean hasVar) {
		this.hasVar = hasVar;
	}
	
}
