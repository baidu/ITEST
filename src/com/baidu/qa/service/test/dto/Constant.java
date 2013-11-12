package com.baidu.qa.service.test.dto;

/**
 * 
 * @author xuedawei
 * @date 2013-8-9
 * @classname Constant
 * @version 1.0.0
 * @desc 常量类
 */
public class Constant {

	public static final String CASETYPE_HTTP ="HTTP";
	public static final String CASETYPE_SOAP ="SOAP";
	public static final String CASETYPE_JSON ="JSON";
	public static final String CASETYPE_RMI ="RMI";
	public static final String CASETYPE_HTTPINVOKER = "HttpInvoker";
	
	public static final String FILENAME_TEST="/test.properties";
	public static final String FILENAME_CONFIG="/config.properties";
	public static final String FILENAME_CASEINFO="/caseinfo.properties";
	public static final String FILENAME_VAR="/var.properties";
	public static final String FILENAME_DB="/mysql.properties";

	public static final String FILENAME_INPUT = "/Input";
	public static final String FILENAME_EXPECT = "/Expect";
	public static final String FILENAME_OUTPUT = "/Output";
	public static final String FILENAME_SETUP ="/SetUp";
	public static final String FILENAME_TEARDOWN = "/TearDown";
	public static final String FILENAME_ACTUAL_RESULT = "actual.txt";
	public static final String FILENAME_EXPECT_RESULT = "expect.txt";
	public static final String FILENAME_DIFF_RESULT = "diff.html";
	
	public static final String FILENAME_RECORDRESPONSE = "recordexpect.response";
	public static final String FILENAME_ORDERFILE = "file.order";
	public static final String FILENAME_DIFFFILE = "difffile";

	public static final String FILE_TYPE_RESPONSE = "response";
	public static final String FILE_TYPE_SQL = "sql";
	public static final String FILE_TYPE_DB = "csv";
	public static final String FILE_TYPE_JSON = "json";
	public static final String FILE_TYPE_XML = "xml";
	public static final String FILE_TYPE_FILE = "file";
	public static final String FILE_TYPE_MEMCACHE= "memcache";
	public static final String FILE_REGEX_DB = ",";
	public static final String FILE_REGEX_TAB = "\t";
	public static final String FILE_TYPE_INPUT = "properties";
	public static final String FILE_TYPE_XPATH = "xpath";
	public static final String FILE_TYPE_INPUT_JSON = "json";
	public static final String FILE_TYPE_HTTP = "http";
	public static final String FILE_TYPE_SOAP = "soap";
	public static final String FILE_TYPE_TPL = "tpl";
	public static final String FILE_TYPE_SELECT ="select";
	public static final String HTTP_REQUEST_TYPE = "get";
	
	public static final String V_CONFIG_VARIABLE_COOKIE = "cookie";
	public static final String V_CONFIG_SUITETYPE_BATCH = "batch";
	
	
	public static final String CASE_STATUS_START = "START";
	public static final String CASE_STATUS_STOP = "STOP";
	public static final String CASE_STATUS_DEBUG = "DEBUG";
	public static final String KW_ITEST_URL="itest_url";
	public static final String KW_ITEST_HOST="itest_host";
	public static final String KW_ITEST_EXPECT="itest_expect";
	public static final String KW_ITEST_JSON="itest_expect_json";
}
