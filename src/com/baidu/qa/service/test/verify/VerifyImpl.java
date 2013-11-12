/*  
 * 	Copyright(C) 2010-2013 Baidu Group
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *  
 */

package com.baidu.qa.service.test.verify;

import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.qa.service.test.dto.Config;
import com.baidu.qa.service.test.dto.Constant;
import com.baidu.qa.service.test.template.VariableGenerator;
import com.baidu.qa.service.test.util.FileUtil;


/**
 * 
 * @author xuedawei
 * @date 2013-9-4
 * @classname VerifyImpl
 * @version 1.0.0
 * @desc 测试结果验证的实现类，主要包括对接口返回的字符串做验证，以及mysql结果验证，复用被测接口获取结果来验证
 */
public class VerifyImpl implements Verify {
	private Log log = LogFactory.getLog(VerifyImpl.class);

	
	
	public boolean verify(File[] expects, String response, String caselocation,
			Config config, VariableGenerator vargen) {
		if (expects == null) {
			return true;
		}
		// 按照file.order排序&获取等待时间
		Map<String, String> timemap = null;
		File[] expectfiles = expects;

		if(expects.length!=0){
		File orderfile = new File(expects[0].getParentFile().getPath() + "/"
				+ Constant.FILENAME_ORDERFILE);
		if (orderfile.exists()) {
			expectfiles = FileUtil.orderFiles(expects);
			timemap = FileUtil.getSplitedMapFromFile(orderfile, "\t");
		}
		}
		//是否有response的expect文件，如果没有则自动记录response
		boolean hasresponse = false;
		//循环处理expect中的文件
		try {
			for (int i = 0; i < expectfiles.length; i++) {
				File file = expectfiles[i];
				log.info("[EXPECT FILE CONTENT]:" + FileUtil.getListFromFileWithBOMFilter(file));
				if (file == null) {
					continue;
				}
				
				//expect文件的命名为xxx.response，则对接口的返回做检查
				else if (FileUtil.getFileSuffix(file).equals(Constant.FILE_TYPE_RESPONSE)) {
					hasresponse = true;
					VerifyResponse verify = new VerifyResponseImpl();
					verify.verifyResponseWithExpectString(file, response);
				} 
				//expect文件的命名为xxx.csv，则转化为sql与mysql数据库中验证是否存在这样的数据
				else if (FileUtil.getFileSuffix(file).equals(Constant.FILE_TYPE_DB)) {
					VerifyMysqlData verify = new VerifyMysqlDataImpl();
					verify.verifyMysqlDataByCsvFile(file, config);
				}

				//expect文件的命名为xxx.sql，执行它在mysql数据库中验证是否存在这样的数据
				else if (FileUtil.getFileSuffix(file).equals(Constant.FILE_TYPE_SQL)) {
					VerifyMysqlData verify = new VerifyMysqlDataImpl();
					verify.verifyMysqlDataBySqlFile(file, config);
				}
				//expect文件的命名为xxx.http，则执行该http请求，对查询出的数据做验证
				else if (FileUtil.getFileSuffix(file).equals(Constant.FILE_TYPE_HTTP)) {
					VerifyResponse verify = new VerifyResponseImpl();
					verify.verifyTestResultByHttpRequest(file, config,vargen);
					hasresponse = true;
				} 
				//expect文件的命名为xxx.select，则执行该sql请求，对查询出的数据做验证
				else if (FileUtil.getFileSuffix(file).equals(Constant.FILE_TYPE_SELECT)) {
					VerifyMysqlData verify = new VerifyMysqlDataImpl();
					verify.getMysqlDataBySqlUsedToVerify(file, config, vargen);
				}
				//
				if (FileUtil.getFileSuffix(file).equals(Constant.FILE_TYPE_TPL)) {
					File processedfile = vargen.processTemplate(file);
					if (processedfile == null || !processedfile.exists()) {
						log.error("has no file from tpl:" + file.getPath());
						continue;
					}
					//对于tpl文件的暂停时间要设置到替换后的文件上
					if (timemap != null && timemap.containsKey(file.getName())) {
						timemap.put(processedfile.getName(), timemap.get(file
								.getName()));
						timemap.remove(file.getName());
					}
					// 加入到当前的处理队列中,作为下一个要处理的文件
					expectfiles[i] = processedfile;
					i--;
				}
				//如果不是tpl文件，需要处理后按设置时间暂停case
				else if (timemap != null
						&& timemap.containsKey(file.getName())) {
					int time = Integer.valueOf(timemap.get(file.getName()));
					log.debug("case sleep " + time + "s after "
							+ file.getName());
					Thread.sleep(time * 1000);
				}
			}

		} catch (Exception e) {
			log.error("[load " + Constant.FILENAME_EXPECT + "error]:", e);
			throw new AssertionError("[test result verify fail] " + e);
		}
		try {
			if (!hasresponse) {

				FileUtil.writeFile(caselocation + Constant.FILENAME_EXPECT,
						Constant.FILENAME_RECORDRESPONSE, response);
				log.info("has no expect response file,save the response"
						+ "to " + caselocation + Constant.FILENAME_EXPECT + "/"
						+ Constant.FILENAME_RECORDRESPONSE);
			}
		} catch (Exception e) {
			log.error("[write " + Constant.FILENAME_RECORDRESPONSE + "error]:",
					e);
			throw new AssertionError("[test result verify fail]:" + e);
		}

		return true;
	}

}
