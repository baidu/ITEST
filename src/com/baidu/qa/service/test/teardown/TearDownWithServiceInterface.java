package com.baidu.qa.service.test.teardown;

import java.io.File;

import com.baidu.qa.service.test.dto.Config;
import com.baidu.qa.service.test.template.VariableGenerator;

public interface TearDownWithServiceInterface {
	
	public boolean cleanTestDataByHttpRequest(File file,Config config,VariableGenerator vargen);
	
	public boolean cleanTestDataBySoapRequest(File file,Config config,VariableGenerator vargen);
	

}
