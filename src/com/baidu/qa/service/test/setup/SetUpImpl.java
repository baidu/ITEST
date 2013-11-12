/*  
 * 	Copyright(C) 2010-2013 Baidu Group
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *  
 */

package com.baidu.qa.service.test.setup;

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
 * @date 2013-9-3
 * @classname SetUpImpl
 * @version 1.0.0
 * @desc 数据准备的实现，目前包括mysql数据库数据准备，复用被测http/soap/json-rpc接口来实现数据准备
 */
public class SetUpImpl implements SetUp {
	
	
	private Log log = LogFactory.getLog(SetUpImpl.class);
	
	
	/**
	 * 实现数据准备的接口
	 */
	public boolean setTestData(File[] setupfiles,Config config,VariableGenerator vargen) {
		if(setupfiles==null||setupfiles.length==0){
			return true;
		}
		Map<String,String> timemap=null;
		File[] setup=setupfiles;
		File orderfile = new File(setupfiles[0].getParentFile().getPath() + "/"+ Constant.FILENAME_ORDERFILE);
		if (orderfile.exists()) {			
			setup=FileUtil.orderFiles(setupfiles);
			timemap=FileUtil.getSplitedMapFromFile(orderfile, "\t");
		}
		File file=null;

		try{
			for(int i=0;i<setup.length;i++){
					file=setup[i];
					if (FileUtil.getFileSuffix(setup[i]).equals(Constant.FILE_TYPE_DB)){
						SetUpWithMysql set = new SetUpWithMysqlImpl();
						set.setTestDataWithCsvFile(setup[i], config);
					}
					
					else if (FileUtil.getFileSuffix(setup[i]).equals(Constant.FILE_TYPE_SQL)){
						SetUpWithMysql set = new SetUpWithMysqlImpl();
						set.setTestDataWithSql(setup[i], config);
					}
					
					else if (FileUtil.getFileSuffix(setup[i]).equals(Constant.FILE_TYPE_HTTP)){
						SetUpWithServiceInterface set=new SetUpWithServiceInterfaceImpl();
						set.setTestDataByHttpRequest(setup[i], config,vargen);
					}
					else if (FileUtil.getFileSuffix(setup[i]).equals(Constant.FILE_TYPE_SOAP)){
						SetUpWithServiceInterface set=new SetUpWithServiceInterfaceImpl();
						set.setTestDataBySoapRequest(setup[i], config,vargen);
					}
					else if (FileUtil.getFileSuffix(setup[i]).equals(Constant.FILE_TYPE_SELECT)){
						SetUpWithMysql set = new SetUpWithMysqlImpl();
						set.setTestDataFromMysql(setup[i], config,vargen);
					}
					if (FileUtil.getFileSuffix(setup[i]).equals(Constant.FILE_TYPE_TPL)){
						File processedfile=vargen.processTemplate(file);
						//加入到当前的处理队列中,作为下一个要处理的文件
						if(processedfile==null||!processedfile.exists()){
							log.error("has no file from tpl:"+file.getPath());
							continue;
						}
						if(timemap!=null&&timemap.containsKey(setup[i].getName())){
							timemap.put(processedfile.getName(), timemap.get(setup[i].getName()));
							timemap.remove(setup[i].getName());
						}
						
						setup[i]=processedfile;
						i--;
					}
					else if (timemap!=null&&timemap.containsKey(setup[i].getName())){
						int time=Integer.valueOf(timemap.get(setup[i].getName())) ;
						log.debug("case sleep "+time + "s after "+setup[i].getName() );
						Thread.sleep(time*1000);
					}
					
			}
			return true;
		}catch(Exception e){
			log.error("[load "+Constant.FILENAME_SETUP+"error]:"+file!=null?file.getName():null, e);
			throw new AssertionError("setup error..."+e);
		}
	}


	
	public boolean setTestEnv(File[] setupfile, Config config,
			VariableGenerator vargen) {
		// TODO Auto-generated method stub
		return false;
	}

	

}
