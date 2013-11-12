package com.baidu.qa.service.test.dto;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.qa.service.test.template.VariableGenerator;
import com.baidu.qa.service.test.util.FileUtil;


/**
 * 
 * @author xuedawei
 * @date 2013-8-9
 * @classname CaseSuite
 * @version 1.0.0
 * @desc suite的定义,即包含有一系列case，共用一些公共数据
 */
public class CaseSuite {
	private Log log = LogFactory.getLog(CaseSuite.class);

	private List<CaseData> casedatalist;
	private String casesuitepath;
	private File[] suitesetup = new File[] {};
	private File[] suiteteardown = new File[] {};
	private VariableGenerator varGen = null;
	public Config config;

	public List<CaseData> getCasedatalist() {
		return casedatalist;
	}

	public void setCasedatalist(List<CaseData> casedatalist) {
		this.casedatalist = casedatalist;
	}

	public String getCasesuitepath() {
		return casesuitepath;
	}

	public void setCasesuitepath(String casesuitepath) {
		this.casesuitepath = casesuitepath;
	}

	public File[] getSuitesetup() {
		return suitesetup;
	}

	public void setSuitesetup(File[] suitesetup) {
		this.suitesetup = suitesetup;
	}

	public File[] getSuiteteardown() {
		return suiteteardown;
	}

	public void setSuiteteardown(File[] suiteteardown) {
		this.suiteteardown = suiteteardown;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public VariableGenerator getVarGen() {
		return varGen;
	}

	public void setVarGen(VariableGenerator varGen) {
		this.varGen = varGen;
	}

	
	
	
	/**
	 * 
	 * @param path:casesuite即一批case对应的文件路径，改路径下需要有config.properties文化
	 * @param cases
	 * @desc 构造函数，用来初始化数据
	 */
	public CaseSuite(String path, List<CaseData> cases) {
		this.setCasesuitepath(path);
		// 如果有suite级别的var替换文件，设置替换case变量的vargen，此处不进行是否为空的判断
		this.setVarGen(VariableGenerator.getGenerator(path+ Constant.FILENAME_CONFIG));
		
		// 对于casesuite的caselist中所有的case，指定其parent
		for (CaseData cd : cases) {
			cd.getVarGen().setParent(this.getVarGen());
		}

		this.setCasedatalist(cases);

		try {
			this.setConfig(new Config(path));
			HashMap<Object,Object> map=new HashMap<Object,Object>();
			//map.put("config.USERID", this.getConfig().getVariable().get("USERID"));
			Map<String, Object> variable=this.getConfig().getVariable();
			for (Entry<String, Object> entry : variable.entrySet()) {
				map.put("config."+((String) entry.getKey()).trim(),
						(String) entry.getValue());
			}
			this.getVarGen().add2Map(map);
		} catch (FileNotFoundException e) {
			log.error("[get file "+Constant.FILENAME_CONFIG+" error]:", e);
		}

		// 要求写成SetUp和TearDown，但是会有Setup和setup...
		if (new File(path + Constant.FILENAME_SETUP).exists()) {
			this.setSuitesetup(FileUtil
					.getFiles(path + Constant.FILENAME_SETUP));
		} else if (new File(path + "/Setup").exists()) {
			this.setSuitesetup(FileUtil.getFiles(path + "/Setup"));
		} else if (new File(path + "/setup").exists()) {
			this.setSuitesetup(FileUtil.getFiles(path + "/setup"));
		}
		if (new File(path + Constant.FILENAME_TEARDOWN).exists()) {
			this.setSuiteteardown(FileUtil.getFiles(path
					+ Constant.FILENAME_TEARDOWN));
		} else if (new File(path + "/Teardown").exists()) {
			this.setSuiteteardown(FileUtil.getFiles(path + "/Teardown"));
		} else if (new File(path + "/teardown").exists()) {
			this.setSuiteteardown(FileUtil.getFiles(path + "/teardown"));
		}

	}
	
	

}
