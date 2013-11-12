/*  
 * 	Copyright(C) 2010-2013 Baidu Group
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *  
 */

package com.baidu.qa.service.test.teardown;

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
 * @date 2013-8-30
 * @classname TearDownImpl
 * @version 1.0.0
 * @desc 测试执行后的数据清理、恢复的环节，主要针对mysql数据库、memcache、使用被测接口做数据恢复，可补充
 */
public class TearDownImpl implements TearDown {
	

	private Log log = LogFactory.getLog(TearDownImpl.class);

	public boolean cleanTestData(File[] tearDowns,Config config,VariableGenerator vargen) {
		if (tearDowns==null||tearDowns.length==0){
			return true;
		}
		// 按照file.order排序&获取等待时间
		Map<String, String> timemap = null;
		File[] tearDownFiles = tearDowns;
		File orderfile = new File(tearDowns[0].getParentFile().getPath() + "/"
				+ Constant.FILENAME_ORDERFILE);
		if (orderfile.exists()) {
			tearDownFiles = FileUtil.orderFiles(tearDowns);
			timemap = FileUtil.getSplitedMapFromFile(orderfile, "\t");
		}
		//循环处理文件
		for(int i=0;i<tearDownFiles.length;i++){
				
			if(tearDownFiles[i].getName().endsWith(Constant.FILE_TYPE_SQL)){
				TearDownWithSql tearDown = new TearDownWithSqlImpl();
				if(!tearDown.cleanTestDataWithSql(tearDownFiles[i],config)){
					return false;
				}
			}
			
			else if(tearDownFiles[i].getName().endsWith(Constant.FILE_TYPE_DB)){
				TearDownWithCsv tearDown = new TearDownWithCsvImpl();
				if(!tearDown.teardownTestDataWithCSV(tearDownFiles[i],config)){
					return false;
				}
			}
			
			else if (FileUtil.getFileSuffix(tearDownFiles[i]).equals(Constant.FILE_TYPE_HTTP)){
				TearDownWithServiceInterface tearDown = new TearDownWithServiceInterfaceImpl();
				tearDown.cleanTestDataByHttpRequest(tearDownFiles[i], config, vargen);
			}
			
			else if (FileUtil.getFileSuffix(tearDownFiles[i]).equals(Constant.FILE_TYPE_SOAP)){
				TearDownWithServiceInterface tearDown = new TearDownWithServiceInterfaceImpl();
				tearDown.cleanTestDataBySoapRequest(tearDownFiles[i], config,vargen);
			}
			
			else if (FileUtil.getFileSuffix(tearDownFiles[i]).equals(Constant.FILE_TYPE_SELECT)) {
				TearDownWithSql tearDown = new TearDownWithSqlImpl();
				tearDown.getTestResultFromMysql(tearDownFiles[i], config, vargen);
			}
			
			
			if (FileUtil.getFileSuffix(tearDownFiles[i]).equals(Constant.FILE_TYPE_TPL)){
				File processedfile=vargen.processTemplate(tearDownFiles[i]);
				if(processedfile==null||!processedfile.exists()){
					log.error("has no file from tpl:"+tearDownFiles[i].getPath());
					continue;
				}
				//对于tpl文件的暂停时间要设置到替换后的文件上
				if (timemap != null && timemap.containsKey(tearDownFiles[i].getName())) {
					timemap.put(processedfile.getName(), timemap.get(tearDownFiles[i]
							.getName()));
					timemap.remove(tearDownFiles[i].getName());
				}
				//加入到当前的处理队列中,作为下一个要处理的文件
				tearDownFiles[i]=processedfile;
				i--;
			}
			//如果不是tpl文件，需要处理后按设置时间暂停case
			else if (timemap != null
					&& timemap.containsKey(tearDownFiles[i].getName())) {
				int time = Integer.valueOf(timemap.get(tearDownFiles[i].getName()));
				log.debug("case sleep " + time + "s after "
						+ tearDownFiles[i].getName());
				try {
					Thread.sleep(time * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
		
		return true;
	}

	

}
