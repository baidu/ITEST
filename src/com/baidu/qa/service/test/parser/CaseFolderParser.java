package com.baidu.qa.service.test.parser;

import java.util.List;

import com.baidu.qa.service.test.dto.CaseSuite;

/**
 * case文件的解析接口定义
 * @author xuedawei
 * @date 2013-8-29
 * @classname CaseFolderParser
 * @version 1.0.0
 * @desc TODO
 */
public interface CaseFolderParser {

	/**
	 * 
	 * @param folderpath
	 * @return
	 * @throws Exception
	 */
	public List<CaseSuite> getCasesuiteFromFolder(String folderpath) throws Exception;

	/**
	 * 根据case文件目录的根路径解析case信息
	 * @param pathlist
	 * @param rootpath
	 * @return
	 * @throws Exception
	 */
	List<CaseSuite> getCasesuiteFromPathlist(String pathlist,String rootpath) throws Exception;
	
}
