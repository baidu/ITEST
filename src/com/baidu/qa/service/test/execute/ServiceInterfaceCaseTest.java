package com.baidu.qa.service.test.execute;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.databene.contiperf.timer.ConstantTimer;
import org.databene.contiperf.timer.RandomTimer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.baidu.qa.service.test.client.HttpInvokerServiceClientImpl;
import com.baidu.qa.service.test.client.HttpServiceClientImpl;
import com.baidu.qa.service.test.client.JsonRpcServiceClientImpl;
import com.baidu.qa.service.test.client.ServiceInterfaceClient;
import com.baidu.qa.service.test.client.SoapServiceClientImpl;
import com.baidu.qa.service.test.dto.*;
import com.baidu.qa.service.test.login.*;
import com.baidu.qa.service.test.parser.CaseFolderParser;
import com.baidu.qa.service.test.parser.CaseFolderParserImpl;

import com.baidu.qa.service.test.setup.SetUp;
import com.baidu.qa.service.test.setup.SetUpImpl;
import com.baidu.qa.service.test.teardown.TearDown;
import com.baidu.qa.service.test.teardown.TearDownImpl;
import com.baidu.qa.service.test.util.*;
import com.baidu.qa.service.test.verify.Verify;
import com.baidu.qa.service.test.verify.VerifyImpl;



/**
 * 
 * @author xuedawei
 * @date 2013-8-9
 * @classname ServiceInterfaceCaseTest
 * @version 1.0.0
 * @desc service-ddt:service data driver test,是将一系列service接口的自动化测试，统一为数据驱动的模式;
 * 采用了junit的参数化模式，将service接口测试所需的参数、验证结果等依次从外部case文件加载后，执行测试
 * @desc 测试对象：HTTP(POST/GET)接口，JSON-RPC接口，SOAP接口
 * @desc 测试结果验证模式：接口返回字符串，返回json，mysql结果
 */
@RunWith(Parameterized.class)
public class ServiceInterfaceCaseTest {
	

	private static Log log = LogFactory.getLog(ServiceInterfaceCaseTest.class);
	/**
	 * case重复执行的次数，默认是1次
	 */
	private static int invocations = 1;
	/**
	 * 需要权限的service，且不方便做登录，可以手工准备cookie文件，加以复用 
	 */
	private static String cookiefile = "";
	/**
	 * 是否执行sql的开关项，统一设置
	 */
	public static boolean is_execute_sql = true;
	
	/**
	 * 是否需要在测试http接口的时候自动登录
	 */
	public static boolean is_auto_login = false;

	private CaseData casedata;
	private CaseSuite casesuite;
	private boolean isfirstinsuite;
	private boolean islastinsuite;
	private boolean isnewserver;

	/**
	 * CASEPATH为外部case的路径，默认的是project的case目录，如果test.properties中指定则替换
	 */
	public static String CASEPATH = System.getProperty("user.dir") + "/case";
	/**
	 * RUNPATH为CASEPATH下有多个子目录的情况下，特别指定1个或多个子目录单独执行,在test.properties中指定
	 */
	public static String RUNPATH = null;
	
	private ServiceInterfaceClient siclient = null;
	private SetUp setup = new SetUpImpl();
	private Verify verify = new VerifyImpl();
	private TearDown teardown = new TearDownImpl();

	
	
	/**
	 * JUnit参数化,case文件中的数据，被初始化到数组
	 * @param caselocation case的绝对路径
	 * @param casesuite suite对象，case按照不同suite归类执行
	 * @param casedata case数据，包含了接口请求action，接口协议类型
	 * @param isfirstinsuite 标识case是否为一个suite用途是在此时机做suite级别的初始化
	 * @param islastinsuite 表示case是否为一个suite分组的最后一个case
	 * @param desc case文本描述
	 */
	public ServiceInterfaceCaseTest(String caselocation, CaseSuite casesuite,
			CaseData casedata, boolean isfirstinsuite, boolean islastinsuite,
			String desc) {
		this.casesuite = casesuite;
		this.casedata = casedata;
		this.isfirstinsuite = isfirstinsuite;
		this.islastinsuite = islastinsuite;
		
		String service_type = casesuite.config.getCasetype();
		if (service_type.equalsIgnoreCase(Constant.CASETYPE_HTTP)) {
			siclient = new HttpServiceClientImpl();
		} else if (service_type.equalsIgnoreCase(Constant.CASETYPE_SOAP)) {
			siclient = new SoapServiceClientImpl();
		} else if (service_type.equalsIgnoreCase(Constant.CASETYPE_JSON)) {
			siclient = new JsonRpcServiceClientImpl();
		} else if (service_type.equalsIgnoreCase(Constant.CASETYPE_HTTPINVOKER)) {
			siclient = new HttpInvokerServiceClientImpl();
		}
	}

	
	/**
	 * 基础配置信息初始
	 */
	static {
		// 从test.properties中读取case路径等
		try {
			InputStream cfin = new BufferedInputStream(new FileInputStream(
					System.getProperty("user.dir") + Constant.FILENAME_TEST));
			Properties cfInfo = new Properties();
			cfInfo.load(cfin);
			
			if (cfInfo.containsKey("casepath")) {
				String path = cfInfo.getProperty("casepath").trim();
				if (path != null && path.length() != 0) {
					CASEPATH = path;
				}else{
					log.error("[CASEPATH IN TEST.PROPERTIES IS NULL]");
					throw new RuntimeException("CASEPATH IN TEST.PROPERTIES IS NULL!!");
				}
			}
			//suite
			if (cfInfo.containsKey("runpath")) {
				String path = cfInfo.getProperty("runpath").trim();
				if (path != null && path.length() != 0) {
					RUNPATH = path;
				}
			}
			
			//case执行的次�?
			if (cfInfo.containsKey("invocations")) {
				String tmp = cfInfo.getProperty("invocations").trim();
				if (tmp != null && tmp.length() != 0) {
					try{
						invocations = Integer.parseInt(tmp);
					}catch(Exception e){
						invocations = 1;
					}
				}
			}
			
			//是否读取cookie文件的方式，要得到路�?
			if (cfInfo.containsKey("cookiefile")) {
				String tmp = cfInfo.getProperty("cookiefile").trim();
				if (tmp != null && tmp.length() != 0) {
					cookiefile = tmp;
				}
			}
			
			
			if (cfInfo.containsKey("is_execute_sql") &&
					cfInfo.getProperty("is_execute_sql").trim().equalsIgnoreCase("false")) {
				is_execute_sql = false;
			}

			if (cfInfo.containsKey("is_auto_login") &&
					cfInfo.getProperty("is_auto_login").trim().equalsIgnoreCase("true")) {
				is_auto_login = true;
			}
			
			cfin.close();
			log.info("[CASE PATH]:" + CASEPATH);
			log.info("[CASE SUITE PATH]:" + RUNPATH);
		} catch (Exception e) {
			log.error("[GET CONFIG FROM TEST.PROPERTIES ERROR]:", e);
			throw new RuntimeException(e);
		}
	}
	
	

	/**
	 * 需要登录权限才能运行的http接口，需要实现Login的接口，返回登录成功后的cookie，为case所复用
	 * 如果不做自动登录的话，但也需要权限，则可手工copy可用cookie至一个文件，并在test.properties中设置cookiefile=的路径
	 */
	private void setLoginCookie() {
		
		try {
			String cookie = "";
			if(casesuite.config.getCasetype().equalsIgnoreCase(Constant.CASETYPE_HTTP) == false){
				log.info("[CASE TYPE IS NOT HTTP PROTOCOL , SKIP LOGGING..]");
			}else{
				if(is_auto_login){
					Login login = new LoginImpl();
					cookie = login.loginAndGetCookie();
				}else{
					if(!cookiefile.equals("")){
						cookie = FileUtil.readFileByLines(new File(cookiefile));
					}
				}
			}
			
			casesuite.config.getVariable().put("cookie", cookie);
		} catch (Exception e) {
			throw new AssertionError("[login error and stop run case]");
		}

	}
	
	


	/**
	 * 使用参数化的方式将case数据载入到数组
	 * @return 二维数组
	 * @throws Exception
	 */
	@Parameters(name = "{index}][{0}][{5}")
	public static Collection<Object[]> loadTestData() throws Exception {
		
		log.info("[loading test cases from case folders...]");
		CaseFolderParser cfp = new CaseFolderParserImpl();
		List<CaseSuite> casesuitelist = null;
		
		if (RUNPATH != null && RUNPATH.trim().length() != 0) {
			log.info("use runpath load cases" + RUNPATH);
			casesuitelist = cfp.getCasesuiteFromPathlist(RUNPATH, CASEPATH);
		} else {
			casesuitelist = cfp.getCasesuiteFromFolder(CASEPATH);
		}

		if(casesuitelist == null || casesuitelist.isEmpty()){
			throw new AssertionError("[CASES NOT FOUND...]");
		}

		int casenum = 0;
		for (int i = 0; i < casesuitelist.size(); i++) {
			casenum += casesuitelist.get(i).getCasedatalist().size();
		}
		
		casenum = casenum * invocations;
		
		Object[][] objects = new Object[casenum][6];
		casenum = 0;

		for (int i = 0; i < casesuitelist.size(); i++) {
			for(int n = 0; n < invocations;n++){
				for (int j = 0; j < casesuitelist.get(i).getCasedatalist().size(); j++) {
					objects[casenum][0] = casesuitelist.get(i).getCasedatalist().get(j).getCaselocation();
					objects[casenum][1] = casesuitelist.get(i);
					objects[casenum][2] = casesuitelist.get(i).getCasedatalist().get(j);
					objects[casenum][3] = false;
					objects[casenum][4] = false;
					objects[casenum][5] = FileUtil.transCoding(casesuitelist.get(i).getCasedatalist().get(j).getDesc());
//					objects[casenum][6] = false;
//					objects[casenum][7] = null;

					if (j == 0 && n ==0) {
						objects[casenum][3] = true;
					}
					if (j == casesuitelist.get(i).getCasedatalist().size() - 1 && n == invocations - 1) {
						objects[casenum][4] = true;
					}
					casenum++;
				}
			}
		}	
		
		log.info("[load testcases from case folders done!!]");
		return Arrays.asList(objects);
	}

	
	
	
	
	@Before
	/**
	 * test前置工作，主要处理测试准备的一些工作
	 */
	public void setup() {
		log.info("===============================start testing===============================");
		log.info("[test case info]:caseid=" + casedata.getCaseid() + ",casepath="+ casedata.getCaselocation());
		// 第一步：清理output文件
		try{
			FileUtil.deleteInFolder(casedata.getCaselocation()+ Constant.FILENAME_OUTPUT);
		}catch(Exception e){
			log.error("[delete output folder error]", e);
		}
		
		//第二部：有权限控制的地方需要设置登录cookie,且避免重复登录,在一个suite内第一个case需要登录
		try{
			if (isfirstinsuite) {
				setLoginCookie();
			}
		}catch(Exception e){
			throw new AssertionError("[Login error]"+e.getMessage());
		}
		
		// 第三步：如果是suite，则执行suite级别的teardown
		if (isfirstinsuite || casesuite.getConfig().getSuitetype().equalsIgnoreCase(Constant.V_CONFIG_SUITETYPE_BATCH)) {
			log.info("[test casesuite path]:" + casesuite.getCasesuitepath());
			// 如果没有变量替换，就先执行teardown
			if (!casedata.isHasVar() && !casesuite.getConfig().isHasVar()) {
				try {
					teardown.cleanTestData(casesuite.getSuiteteardown(),casesuite.config, casesuite.getVarGen());
				} catch (Exception e) {
					log.error("[EXCUTE SUITE TEARDOWN ERROR]:", e);
				}
			}
		}
		// 第四步：先清理脏数据，如果没有变量替换先进行case数据清理
		if (!casedata.isHasVar() && !casesuite.getConfig().isHasVar()) {
			teardown.cleanTestData(casedata.getTeardown(), casesuite.config, casedata.getVarGen());
		}
		// 第五步：如果是suite的第�?��case或�?是batch的方式，则执行suite级别的setup
		if (isfirstinsuite|| casesuite.getConfig().getSuitetype().equalsIgnoreCase(Constant.V_CONFIG_SUITETYPE_BATCH)) {
			log.info("[begin set case suite data]");
			try {
				setup.setTestData(casesuite.getSuitesetup(), casesuite.config,casesuite.getVarGen());
			} catch (Exception e) {
				log.error("[EXCUTE SUITE SETUP ERROR]:", e);
			}
			// suite的setup之后等待时间
			if (casesuite.config.getSuite_before_wait_time() != 0) {
				try {
					log.debug("suite sleep before cases "+ casesuite.config.getSuite_before_wait_time()/ 1000 + "s");
					Thread.sleep(casesuite.config.getSuite_before_wait_time());
				} catch (InterruptedException e) {
					log.error("[waite before casesuite error]:", e);
				}
			}
		}
		
		log.info("[test case path]:" + casedata.getCaselocation());
		log.info("[test case id]:" + casedata.getCaseid());
		log.info("[test case desc]:"+ FileUtil.transCoding(casedata.getDesc()));
		log.info("[test case url]:" + casesuite.config.getHost()+ casedata.getAction());

		
		try {
			log.info("[BEGIN SET TEST DATA]");
			setup.setTestData(casedata.getSetup(), casesuite.config, casedata.getVarGen());
			log.info("[SET TEST DATA DONE]");
		} catch (Exception e) {
			log.error("[EXCUTE CASE SETUP STEP ERROR]:", e);
		}

	}

	
	
	/**
	 * Data-driver方式驱动测试多次执行
	 * timout设置为30s内case未结束则fail
	 */
	@Test(timeout=30000)
	public void testCase() {
		
		Object resp = null;
		
		try {
			//增加休眠环节，主要是避免大系统的数据延迟给测试带来困扰
			if (casesuite.config.getBefore_wait_time() != 0) {
				log.info("[case sleep before request for "+ casesuite.config.getBefore_wait_time() / 1000 + "s]");
				Thread.sleep(casesuite.config.getBefore_wait_time());
			}
			//request
			resp = siclient.invokeServiceMethod(casedata,casesuite.config);
			log.info("[THE SERVICE INTERFACE RESPONSE]:" + resp);
			
			// sleep after request
			if (casesuite.config.getAfter_wait_time() != 0) {
				log.debug("case sleep after request for "+ casesuite.config.getAfter_wait_time() / 1000 + "s");
				Thread.sleep(casesuite.config.getAfter_wait_time());
			}
		} catch (Exception e) {
			log.error("[SERVICE INTERFACE REQUEST ERROR]", e);
			throw new AssertionError("SERVICE INTERFACE REQUEST ERROR" + e.getMessage());
		}
		
		
		try {
			//记录接口的实际返回
			FileUtil.rewriteFile(casedata.getCaselocation()+ Constant.FILENAME_OUTPUT,
					Constant.FILENAME_ACTUAL_RESULT, resp.toString());
			//记录diff文件以辅助于对应查找区别
			StringDiffUtil diff = new StringDiffUtil();
			for(File file : casedata.getExpect()){
				if(FileUtil.getFileSuffix(file).equals(Constant.FILE_TYPE_RESPONSE)){
					FileUtil.rewriteFile(casedata.getCaselocation()+ Constant.FILENAME_OUTPUT,
							Constant.FILENAME_DIFF_RESULT, diff.diff_prettyHtml(diff.diff_main(resp.toString(), FileUtil.readFileByLinesWithBOMFilter(file))));
					break;
				}
			}
		} catch (Exception e) {
			//这一步出错不影响测试继续执行
			log.error("[interface's response record or diff error]", e);
		}


		//验证环节
		try {
			//返回为null不能作为fail依据
			if (resp == null) {
				log.error("[interface response result is null]");
			} else {
				log.info("[BEGIN EXECUTE VERIFY STEP] "+casedata.getCaselocation());
				verify.verify(casedata.getExpect(), resp.toString(),
						casedata.getCaselocation(), casesuite.config, casedata.getVarGen());
				log.info("[VERIFY TEST RESULT DONE] ");
			}

		} catch (Exception e) {
			log.error("[assert error]:", e);
			throw new AssertionError("TEST RESULT VERIFY FAIL:" + e);
		}
	}

	
	
	
	
	/**
	 * case执行结束后的收尾工作，主要为数据清理的环节
	 */
	@After
	public void teardown() {
		
		//清理掉已经用过的测试数据
		try {
			log.info("[BEGIN EXCUTE CASE TEARDOWN STEP]");
			teardown.cleanTestData(casedata.getTeardown(), casesuite.config, casedata.getVarGen());
			log.info("[CASE TEARDOWN STEP EXECUTED SUCCESS]");
		} catch (Exception e) {
			log.error("[EXCUTE CASE TEARDOWN ERROR] ", e);
		}
		
		// suite级别的teardown
		if (islastinsuite|| casesuite.getConfig().getSuitetype().equalsIgnoreCase(Constant.V_CONFIG_SUITETYPE_BATCH)) {
			
			try {
				log.info("[BEGIN EXCUTE CASESUITE TEARDOWN STEP]");
				log.info("[SLEEP SOME TIMES] " + casesuite.config.getSuite_after_wait_time() / 1000+ "s");
				Thread.sleep(casesuite.config.getSuite_after_wait_time());
			} catch (InterruptedException e) {
				throw new RuntimeException("Thread error");
			}
			
			
			try {
				teardown.cleanTestData(casesuite.getSuiteteardown(), casesuite.config,casesuite.getVarGen());
			} catch (Exception e) {
				log.error("[EXCUTE SUITE TEARDOWN ERROR]:", e);
			}
		}
		
	}
}
