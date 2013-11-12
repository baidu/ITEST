/*  
 * 	Copyright(C) 2010-2013 Baidu Group
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *  
 */

package com.baidu.qa.service.test.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.qa.service.test.dto.CaseData;
import com.baidu.qa.service.test.dto.CaseSuite;
import com.baidu.qa.service.test.dto.Constant;
import com.baidu.qa.service.test.template.VariableGenerator;
import com.baidu.qa.service.test.util.FileCharsetDetector;
import com.baidu.qa.service.test.util.FileUtil;


/**
 * 
 * @author xuedawei
 * @date 2013-8-29
 * @classname CaseFolderParserImpl
 * @version 1.0.0
 * @desc 解析case文件目录的实现类，将case文件中的数据读取后放到java对象类中供使用
 */
public class CaseFolderParserImpl implements CaseFolderParser {
	
	private Log log = LogFactory.getLog(CaseFolderParserImpl.class);
	private boolean hasdebug = false;
	private List<CaseSuite> casesuites;

	
	
	/**
	 * 解析suite目录
	 * @param folderpath case根目录路径
	 * @return 数组，元素为一个case的全部信息
	 * @throws Exception
	 */
	private List<CaseData> getCasedataFromFolder(String folderpath) throws Exception {

		List<CaseData> cases = new ArrayList<CaseData>();
		List<String> resultpaths = new ArrayList<String>();
		try{
			// 获取case的路径
			searchFolders(folderpath, Constant.FILENAME_CASEINFO, resultpaths);
			
			for (int i = 0; i < resultpaths.size(); i++) {
				CaseData casedata = parsecase(resultpaths.get(i));
				// 当case不是stop的时候添加
				if (casedata != null && casedata.getStatus() != null
						&& !casedata.getStatus().toUpperCase().equals(Constant.CASE_STATUS_STOP)) {
						cases.add(casedata);
					}
			}
			
			// 有debug类型的 只执行debug的case
			List<CaseData> debuglist = new ArrayList<CaseData>();
			for (CaseData casedata : cases) {
				if (casedata.getStatus().toUpperCase().equals(Constant.CASE_STATUS_DEBUG)) {
					{
						debuglist.add(casedata);
					}
				}
			}
			
			if (debuglist.size() > 0) {
				if (hasdebug == false) {
					casesuites = new ArrayList<CaseSuite>();
				}
				hasdebug = true;
				return debuglist;
			}
			if (hasdebug == true) {
				return null;
			}
			return (cases);
		}catch(Exception ex){
			throw new RuntimeException("parse suite folder error", ex.getCause());
		}
	}
	
	

	/**
	 * 解析suite下的case目录
	 * @param casepath case文件路径
	 * @return 单个case信息
	 * @throws Exception
	 */
	private CaseData parsecase(String casepath) throws Exception {
		
		CaseData casedata = new CaseData();
		File caseinfo = new File(casepath + Constant.FILENAME_CASEINFO);
		File input = new File(casepath + Constant.FILENAME_INPUT);
		// 解析case基本信息
		InputStream in_caseinfo = new BufferedInputStream(new FileInputStream(
				casepath + Constant.FILENAME_CASEINFO));
		Properties Info_caseinfo = new Properties();
		try {
			Info_caseinfo.load(in_caseinfo);

			casedata.setCaseid(Info_caseinfo.getProperty("caseid"));
			casedata.setDesc(Info_caseinfo.getProperty("desc"));
			casedata.setAction(Info_caseinfo.getProperty("action"));
			casedata.setStatus(Info_caseinfo.getProperty("status"));
			if (Info_caseinfo.containsKey("requesttype")) {
				casedata.setRequesttype(Info_caseinfo.getProperty("requesttype"));
			}
			//判断是否存在var替换
			if (Info_caseinfo.containsKey("var")&&Info_caseinfo
					.getProperty("var").trim().length()!=0) {
				casedata.setHasVar(true);
			}
			in_caseinfo.close();

			// 设置替换case变量的vargen，在此不进行文件路径是否为空的判断
			casedata.setVarGen(VariableGenerator.getGenerator(casepath
					+ Constant.FILENAME_CASEINFO));
			
			casedata = parseCaseinfo(casedata,casepath);
			return casedata;
		} catch (IOException e) {
			throw new RuntimeException("parse case folder error", e.getCause());

		}
		
	}
	
	
	/**
	 * case目录下各个子目录的解析
	 * @param casepath
	 * @return
	 */
	private CaseData parseCaseinfo(CaseData casedata,String casepath){
		
		try {
			File[] inputfiles = FileUtil.getFiles(casepath + Constant.FILENAME_INPUT);

			File inputfile = null;
			for (File f : inputfiles) {
				String suffix = FileUtil.getFileSuffix(f);

				if (suffix.trim().equalsIgnoreCase(Constant.FILE_TYPE_INPUT)
						|| (suffix.trim().equalsIgnoreCase(Constant.FILE_TYPE_SOAP))
						|| (suffix.trim().equalsIgnoreCase(Constant.FILE_TYPE_INPUT_JSON))
						|| (suffix.trim().equalsIgnoreCase(Constant.FILE_TYPE_TPL))) {
					inputfile = f;
				}
			}
			
			if (inputfile != null) {
				FileCharsetDetector det = new FileCharsetDetector();
				try {
					String oldcharset = det.guestFileEncoding(inputfile);
					if (oldcharset.equalsIgnoreCase("UTF-8") == false)
						FileUtil.transferFile(inputfile, oldcharset, "UTF-8");
				} catch (Exception ex) {
					log.error("[change expect file charset error]:" + ex);
					throw new RuntimeException("change expect file charset error", ex.getCause());
				}
			}
			
			//如果是tpl只是文件添加到了casedata中
			casedata.setInputFile(inputfile);
			// 两个分支，分别处理properties和xml两种输入
			if (inputfile != null&& FileUtil.getFileSuffix(inputfile).equalsIgnoreCase(
							Constant.FILE_TYPE_INPUT)) {
				casedata.setInput();
			} 
			else if (inputfile != null&& FileUtil.getFileSuffix(inputfile).equalsIgnoreCase(
							Constant.FILE_TYPE_INPUT_JSON)) {
				casedata.setInputJson(FileUtil.readFileByLines(inputfile));
			}
			
		} catch (Exception ex) {
			log.error("[parse " + Constant.FILENAME_INPUT + " error]:", ex);
			throw new RuntimeException("parse case info error", ex.getCause());
		}

		// 期望数据文件
		File expect = new File(casepath + Constant.FILENAME_EXPECT);
		if (expect.exists()) {
			casedata.setExpect(expect.listFiles());
		}
		// 这里有个缺陷，要求写成SetUp和TearDown，但是会有Setup和setup...
		if (new File(casepath + Constant.FILENAME_SETUP).exists()) {
			casedata.setSetup(FileUtil.getFiles(casepath
					+ Constant.FILENAME_SETUP));
		} else if (new File(casepath + "/Setup").exists()) {
			casedata.setSetup(FileUtil.getFiles(casepath + "/Setup"));
		} else if (new File(casepath + "/setup").exists()) {
			casedata.setSetup(FileUtil.getFiles(casepath + "/setup"));
		}
		if (new File(casepath + Constant.FILENAME_TEARDOWN).exists()) {
			casedata.setTeardown(FileUtil.getFiles(casepath
					+ Constant.FILENAME_TEARDOWN));
		} else if (new File(casepath + "/Teardown").exists()) {
			casedata.setTeardown(FileUtil.getFiles(casepath + "/Teardown"));
		} else if (new File(casepath + "/teardown").exists()) {
			casedata.setTeardown(FileUtil.getFiles(casepath + "/teardown"));
		}

		casedata.setCaselocation(casepath);
		return casedata;
	}
	
	
	

	/**
	 * 在strPath目录下递归寻找filename的文件path，filename文件不嵌套存在
	 * @param strPath
	 * @param filename
	 * @param resultpaths
	 */
	private void searchFolders(String strPath, String filename,
			List<String> resultpaths) {
		File dir = new File(strPath);
		if (new File(strPath + filename).exists()) {
			resultpaths.add(strPath);

		} else {
			File[] files = dir.listFiles();

			if (files == null)
				return;
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					searchFolders(files[i].getAbsolutePath(), filename,
							resultpaths);
				}
			}
		}
	}

	
	
	/*
	 * 在strPath目录向上寻找最近的存在filename文件的path,最多寻找到root
	 */
	private String searchUPFolders(String strPath, String filename,
			String rootpath) {

		if (new File(strPath + filename).exists()) {
			return strPath;
		} else {
			strPath = (new File(strPath)).getParent();

			if (new File(strPath).equals(new File(rootpath.trim()).getParent())) {
				log.error("can't find " + filename + " between " 
						+ strPath + " and " + rootpath);
				return null;
			}
			return searchUPFolders(strPath, filename, rootpath);
		}
	}

	
	
	private List<CaseSuite> getCasesuiteFromOneFolder(String folderpath)
			throws Exception {
		casesuites = new ArrayList<CaseSuite>();
		// 寻找config文件的路径集合
		List<String> resultpaths = new ArrayList<String>();
		searchFolders(folderpath, Constant.FILENAME_CONFIG, resultpaths);
		if (resultpaths == null || resultpaths.size() == 0) {
			log.error("can't find " + Constant.FILENAME_CONFIG + " in "
					+ folderpath);
			return null;
		}
		// 对每个存在config文件的文件夹（即suite），获取caselist
		for (int i = 0; i < resultpaths.size(); i++) {

			List<CaseData> cases = getCasedataFromFolder(resultpaths.get(i));
			if (cases == null || cases.size() == 0) {
				continue;
			}
			CaseSuite casesuite = new CaseSuite(resultpaths.get(i), cases);

			casesuites.add(casesuite);
		}

		return (casesuites);
	}

	public List<CaseSuite> getCasesuiteFromFolder(String folderpath)
			throws Exception {
		hasdebug = false;
		return getCasesuiteFromOneFolder(folderpath);
	}

	public List<CaseSuite> getCasesuiteFromPathlist(String runpath,
			String rootpath) throws Exception {
		String[] pathlist = runpath.trim().split(",");
		if (pathlist.length == 0) {
			return null;
		}
		hasdebug = false;
		List<CaseSuite> casesuitelist = new ArrayList<CaseSuite>();
		for (String path : pathlist) {
			// 如果是case文件
			if (path.trim().endsWith(Constant.FILENAME_CASEINFO.substring(1))) {
				CaseData onecase = parsecase(new File(rootpath + "/"
						+ path.trim()).getParent());
				if (onecase == null) {
					return null;
				}
				boolean isadded = false;
				// 如果case所在的casesuite已经load了，将case加入
				for (CaseSuite cs : casesuitelist) {
					if ((rootpath + "/" + path.trim()).startsWith(casesuitelist
							.get(0).getCasesuitepath().trim())) {
						cs.getCasedatalist().add(onecase);
						isadded = true;
					}
				}
				// 如果case所在的casesuite还没load了，寻找casesuite并且加入case
				if (isadded == false) {
					String suitepath = searchUPFolders(rootpath + "/"
							+ path.trim(), Constant.FILENAME_CONFIG, rootpath);
					List<CaseData> cl = new ArrayList<CaseData>();
					cl.add(onecase);
					if (suitepath != null) {
						CaseSuite cs = new CaseSuite(suitepath, cl);
						casesuitelist.add(cs);
					}
				}
			}
			// 如果是文件夹
			else {
				List<CaseSuite> casesuites = getCasesuiteFromOneFolder(rootpath
						+ "/" + path.trim());
				if (casesuites != null) {
					casesuitelist.addAll(casesuites);
				}
			}

		}
		return casesuitelist;
	}
}
