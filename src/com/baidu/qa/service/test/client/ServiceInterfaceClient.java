package com.baidu.qa.service.test.client;

import com.baidu.qa.service.test.dto.CaseData;
import com.baidu.qa.service.test.dto.Config;


public interface ServiceInterfaceClient {

	public Object invokeServiceMethod(CaseData casedata,Config config);
}
