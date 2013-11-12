package com.baidu.qa.service.test.teardown;

import java.io.File;

import com.baidu.qa.service.test.dto.Config;
import com.baidu.qa.service.test.template.VariableGenerator;



public interface TearDownWithSql {

	
	public boolean cleanTestDataWithSql(File file,Config config);
	
	
	public boolean getTestResultFromMysql(File file,Config config,VariableGenerator vargen);
}
