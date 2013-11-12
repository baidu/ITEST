package com.baidu.qa.service.test.util;

import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CCJSONUtils {
	public static String getFirst(JSONObject jobj, String key) {
		if (jobj == null) {
			return null;
		} else if (jobj.isArray()) {
			JSONArray ja = JSONArray.fromObject(jobj);
			return getFirst(ja, key);
		} else {
			Set keys = jobj.keySet();
			for (Object k : keys) {
				Object v = jobj.get(k);
				if (v instanceof JSONObject) {
					String value = getFirst((JSONObject) v, key);
					if (value != null) {
						return value;
					}
				} else if (v instanceof JSONArray) {
					String value = getFirst(((JSONArray) v), key);
					if (value != null) {
						return value;
					}
				} else {
					if (key.equals(k)) {
						return v.toString();
					}
				}
			}
		}
		return null;
	}

	private static String getFirst(JSONArray ja, String key) {
		if (ja == null) {
			return null;
		}
		int size = ja.size();
		for (int i = 0; i < size; i++) {
			
			if (ja.get(i) instanceof JSONObject){
				JSONObject jo = ja.getJSONObject(i);
				String value = getFirst(jo, key);
				if (value != null) {
					return value;
				}
			}else if (ja.get(i) instanceof JSONArray) {
				String value = getFirst(((JSONArray) ja.get(i)), key);
				if (value != null) {
					return value;
				}
			} 
		}
		return null;
	}
	
	
	public static void main(String[] args){
		JSONObject jobj = JSONObject.fromObject("{\"status\":200,\"data\":{\"sum\":null,\"listData\":[{\"showprob\":1,\"planname\":\"itest_online_test_plan\",\"planstat\":2,\"clks\":0.0,\"showpay\":0.0,\"allnegativecnt\":0,\"qrstat1\":0,\"pausestat\":1,\"avgprice\":0.0,\"plancyc\":[[200,224],[300,324],[400,424],[500,524],[600,624],[700,724]],\"deviceprefer\":0,\"clkrate\":0.0,\"wregion\":\"1,3,13\",\"allipblackcnt\":0,\"paysum\":0.0,\"trans\":0.0,\"shows\":0.0,\"phonetrans\":0,\"wbudget\":200.0,\"planid\":9628120}]},\"errorCode\":null}"); 
		System.out.println(getFirst(jobj, "planid"));
		JSONObject jobj1 = JSONObject.fromObject("{\"a\":1,b:[[1,2],[{\"c\":2}],[{\"c\":22}]]}");
		System.out.println(getFirst(jobj1, "c"));
	}
}
