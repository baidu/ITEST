package com.baidu.qa.service.test.dto;

public class StpSetupData {
	private String hostname = "";
	private String invoke_method = "";
	private String service_name ="";
	private String json_rpc_request = "";
	public String getService_name() {
		return service_name;
	}
	public void setService_name(String serviceName) {
		service_name = serviceName;
	}
	public String getJson_rpc_request() {
		return json_rpc_request;
	}
	public void setJson_rpc_request(String jsonRpcRequest) {
		json_rpc_request = jsonRpcRequest;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getInvoke_method() {
		return invoke_method;
	}
	public void setInvoke_method(String invokeMethod) {
		invoke_method = invokeMethod;
	}

	
	
	
	
}
