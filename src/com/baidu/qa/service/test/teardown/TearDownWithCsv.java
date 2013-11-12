package com.baidu.qa.service.test.teardown;

import java.io.File;

import com.baidu.qa.service.test.dto.Config;


public interface TearDownWithCsv {
	
		
	public boolean teardownTestDataWithCSV(File file,Config config);

	
}
