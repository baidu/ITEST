/*  
 * 	Copyright(C) 2010-2013 Baidu Group
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *  
 */

package com.baidu.qa.service.test.util;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;



/**
 * 
 * @author liuliangxiang,wangrongrong
 * @date 2013-9-5
 * @classname BJSON
 * @version 1.0.0
 * @desc JSON处理的工具类
 */
public class BJSON {
	    
	    public static final int Error= 0;
	    public static final int ObjectJson = 1;
		public static final int ArrayJson = 2;
		public static final int Number=3;
		public static final int Str=4;
	
	/**
	 * 简单判断是否是json格式的字符串
	 * @param str
	 * @return str 的类型 
	 */
	public static int JudgeStringJson(String strJson){
	   int flag=Error;
	   String str=strJson.trim();
	   if(str.startsWith("{")&&str.contains(":")&&str.endsWith("}"))
	   {
		   flag=ObjectJson;
		   return flag;
	   }
	   
	   if(str.startsWith("[")&&str.endsWith("]"))
	   {
		   flag=ArrayJson;
		   return flag;
	   }
	   if(str.matches("\\d*"))
		{
		   flag=Number;
		   return flag;
		  }else
		return flag=Str;
	}
	
	/**
	 * 简单判断是否是json格式的字符串
	 * @param str
	 * @return
	 */
	public static boolean BooleanJudgeStringJson(String str){
	   if(str.startsWith("{")&&str.contains(":")&&str.endsWith("}"))
	   {
		   return true;
	   }
	   
	   if(str.startsWith("[")&&str.endsWith("]"))
	   {
		   return true;
	   }
		return false;
	}
	/**
	 * 将json串　转换入Hashmap中 
	 * @param jsonStr
	 * @return
	 */
	private  static HashMap<String, String> GetJSONASString(String jsonStr){
		HashMap<String, String> hashMap=null;
		hashMap=new HashMap<String, String>();
		if(JudgeStringJson(jsonStr)==BJSON.ObjectJson){
			JSONObject jsonObject=JSONObject.fromObject(jsonStr);
			Iterator<String> it=jsonObject.keys();
			while(it.hasNext()){
				String key=it.next();
				String value=jsonObject.get(key).toString();
				hashMap.put(key, value);
			}
		}
		if(JudgeStringJson(jsonStr)==BJSON.ArrayJson){
			JSONArray jsonArray=JSONArray.fromObject(jsonStr);
				for (int i = 0; i < jsonArray.size(); i++) {
					String key=String.valueOf(i);
					String value=jsonArray.get(i).toString();
					hashMap.put(key, value);
				}
			}
		return hashMap;
	}
	private  static ArrayList<String> GetJSONASStringByArrary(String jsonStr){
		ArrayList<String> arrayList=null;
		arrayList=new ArrayList<String>();
		if(JudgeStringJson(jsonStr)==BJSON.ArrayJson){
			JSONArray jsonArray=JSONArray.fromObject(jsonStr);
				for (int i = 0; i < jsonArray.size(); i++) {
					String value=jsonArray.get(i).toString();
					arrayList.add(value);
				}
			}
		return arrayList;
	}
	/**
	 * 
	 * @param actualStr 实际对比的json 对象
	 * @param expectStr 标准对比的json 对象 双向比较 会把来那个串中不同都打印出来
	 * @param diffHash 保持比较后的结果
	 */
	public HashMap<String, String> findDiffInJson(String actualStr,String expectStr){
		HashMap<String, String> diffHash=new HashMap<String, String>();
		findDiffInJson(actualStr,expectStr,diffHash,"rootExpect","nameEctual","nameExpect",null);
		return diffHash;
	}
	/**
	 * 
	 * @param actualStr 实际对比的json 对象
	 * @param expectStr 标准对比的json 对象  期望中没有的 如果实际结果中 有不会报错
	 * @param diffHash 保持比较后的结果
	 */
	public HashMap<String, String> findDiffSingleInJson(String actualStr,String expectStr){
		HashMap<String, String> diffHash=new HashMap<String, String>();
		findDiffSingleInJson(actualStr,expectStr,diffHash,"rootExpect","nameEctual","nameExpect",null);
		return diffHash;
	}
	/**
	 * 
	 * @param actualStr 实际对比的json 对象
	 * @param expectStr 标准对比的json 对象
	 * @param diffHash 保持比较后的结果
	 * @param nameEctual 表示期望的别名 
	 * @param nameExpect 表示实际结果别名
	 */
	public HashMap<String, String> findDiffInJson(String actualStr,String expectStr, String nameEctual,String nameExpect){
		HashMap<String, String> diffHash=new HashMap<String, String>();
		findDiffInJson(actualStr,expectStr,diffHash,"rootExpect",nameEctual,nameExpect,null);
		return diffHash;
	}
	public HashMap<String, String> findDiffSingleInJson(String actualStr,String expectStr, String nameEctual,String nameExpect){
		HashMap<String, String> diffHash=new HashMap<String, String>();
		findDiffSingleInJson(actualStr,expectStr,diffHash,"rootExpect",nameEctual,nameExpect,null);
		return diffHash;
	}
	/**
	 * 
	 * @param actualStr 实际对比的json 对象
	 * @param expectStr 标准对比的json 对象
	 * @param diffHash 保持比较后的结果
	 * @param excludes 比对结果时的排除字段 可以为空
	 */
	public HashMap<String, String> findDiffInJson(String actualStr,String expectStr,ArrayList<String> excludes){
		HashMap<String, String> diffHash=new HashMap<String, String>();
		findDiffInJson(actualStr,expectStr,diffHash,"rootExpect","nameEctual","nameExpect",excludes);
		return diffHash;
	}
	public HashMap<String, String> findDiffSingleInJson(String actualStr,String expectStr,ArrayList<String> excludes){
		HashMap<String, String> diffHash=new HashMap<String, String>();
		findDiffSingleInJson(actualStr,expectStr,diffHash,"rootExpect","nameEctual","nameExpect",excludes);
		return diffHash;
	}
	
	/**
	 * 
	 * @param actualStr 实际对比的json 对象
	 * @param expectStr 标准对比的json 对象
	 * @param diffHash 保持比较后的结果
	 * @param nameEctual 表示期望的别名 
	 * @param nameExpect 表示实际结果别名
	 * @param excludes 比对结果时的排除字段
	 */
	public HashMap<String, String> findDiffInJson(String actualStr,String expectStr, String nameEctual,String nameExpect,ArrayList<String> excludes){
		HashMap<String, String> diffHash=new HashMap<String, String>();
		findDiffInJson(actualStr,expectStr,diffHash,"rootExpect",nameEctual,nameExpect,excludes);
		return diffHash;
	}
	public HashMap<String, String> findDiffSingleInJson(String actualStr,String expectStr, String nameEctual,String nameExpect,ArrayList<String> excludes){
		HashMap<String, String> diffHash=new HashMap<String, String>();
		findDiffSingleInJson(actualStr,expectStr,diffHash,"rootExpect",nameEctual,nameExpect,excludes);
		return diffHash;
	}
	
	/**
	 * 
	 * @param actualStr 实际对比的json 对象
	 * @param expectStr 标准对比的json 对象
	 * @param diffHash 保持比较后的结果
	 * @param pathKey  路径
	 * @param nameEctual 表示期望的name 
	 * @param nameExpect 表示实际结果别名
	 * @param excludes 比对结果时的排除字段 可以为空哟
	 */
	private void findDiffInJson(String actualStr,String expectStr,HashMap<String, String> diffHash,String pathKey,String nameEctual,String nameExpect,ArrayList<String> excludes){
		findDiffSingleInJson(actualStr, expectStr,diffHash, pathKey, nameEctual, nameExpect,excludes);
		findDiffSingleInJson(expectStr,actualStr,diffHash, pathKey, nameExpect,nameEctual, excludes);
	}
	/**
	 * 
	 * @param actualStr 实际对比的json 对象
	 * @param expectStr 标准对比的json 对象
	 * @param diffHash 保持比较后的结果
	 * @param pathKey  路径
	 * @param nameEctual 表示期望的name 
	 * @param nameExpect 表示实际结果别名
	 * @param excludes 比对结果时的排除字段 可以为空哟
	 */
	private void findDiffSingleInJson(String actualStr,String expectStr,HashMap<String, String> diffHash,String pathKey,String nameEctual,String nameExpect,ArrayList<String> excludes){
		HashMap<String, String> actulMap=new HashMap<String, String>();
		HashMap<String, String> expectMap=new HashMap<String, String>();
		ArrayList<String> actulArray=new ArrayList<String>();
		ArrayList<String> expectArray=new ArrayList<String>();
		String actulJson=actualStr.trim();
		String expectJson=expectStr.trim();
		String[] excludeItems = {};
		if(JudgeStringJson(actulJson)==BJSON.ObjectJson&&JudgeStringJson(expectJson)==BJSON.ObjectJson){
			actulMap=GetJSONASString(actulJson);
			expectMap=GetJSONASString(expectJson);
			Iterator acitKey=actulMap.keySet().iterator();
			Iterator exitKey=expectMap.keySet().iterator();
			while(exitKey.hasNext()){
				String expectKey=(String) exitKey.next();
				if(expectMap.get(expectKey)==null&&actulMap.get(expectKey)==null){
					continue;
				}
               if((expectMap.get(expectKey)==null&&actulMap.get(expectKey)!=null)||(expectMap.get(expectKey)!=null&&actulMap.get(expectKey)==null)){
                   if(excludes!=null&&excludes.contains(expectKey)){
                	   continue;
                   }
                	String parentKeyTemp=pathKey+"->"+expectKey;
                	diffHash.put(parentKeyTemp, "\n\t"+nameExpect+":["+expectMap.get(expectKey)+"]\n\t"+nameEctual+":["+actulMap.get(expectKey)+"]");
				    continue;
                }
				String expectValue=expectMap.get(expectKey).trim();
				String actulValue=actulMap.get(expectKey).trim();
				if(!actulValue.equals(expectValue)){
					if(excludes!=null&&excludes.contains(expectKey)){
	                	   continue;
	                   }
					String parentKeyTemp=pathKey+"->"+expectKey;
					if(JudgeStringJson(expectValue)<BJSON.Number){
						findDiffSingleInJson(actulValue, expectValue,diffHash, parentKeyTemp,nameEctual,nameExpect,excludes);
					}else{
						diffHash.put(parentKeyTemp, "\n\t"+nameExpect+":["+expectValue+"]\n\t"+nameEctual+":["+actulValue+"]");
					}
				}
			}
		}else if(JudgeStringJson(actulJson)==BJSON.ArrayJson&&JudgeStringJson(expectJson)==BJSON.ArrayJson){
			actulArray=GetJSONASStringByArrary(actulJson);
			expectArray=GetJSONASStringByArrary(expectJson);
			ArrayList<String> expectArryTemp=new ArrayList<String>();
			for(String str:expectArray){
				if(!actulArray.contains(str)){
					expectArryTemp.add(str);
				}else
					actulArray.remove(str);
			}
			if(expectArryTemp.size()>0){
				for(int i=0;i<expectArryTemp.size();i++){
					String str=expectArryTemp.get(i);
					String parentKeyTemp=pathKey+"->"+i;
					if(JudgeStringJson(str)<BJSON.Number){
						if(actulArray.size()>i){
							findDiffSingleInJson(actulArray.get(i), str,diffHash, parentKeyTemp,nameEctual,nameExpect,excludes);
						}else{
							diffHash.put(parentKeyTemp, "\n\t"+nameExpect+":["+str+"]\n\t"+nameEctual+":[null]");
						}
					}else{
						if(actulArray.size()>i){
							diffHash.put(parentKeyTemp, "\n\t"+nameExpect+":["+str+"]\n\t"+nameEctual+":["+actulArray.get(i)+"]");
						}else{
							diffHash.put(parentKeyTemp, "\n\t"+nameExpect+":["+str+"]\n\t"+nameEctual+":[null]");
						}
					}
				}
			}
			
		}else if(JudgeStringJson(actulJson)<BJSON.Number&&JudgeStringJson(expectJson)>=BJSON.Number){
		 diffHash.put(pathKey, "Different format;"+nameExpect+" not Json ,"+nameEctual+" Json");		
		}else if(JudgeStringJson(actulJson)>=BJSON.Number&&JudgeStringJson(expectJson)<BJSON.Number){
		 diffHash.put(pathKey, "Different format;"+nameExpect+" Json ,"+nameEctual+" not Json");		
	   }else{
		  if(!actulJson.equals(expectJson)){
			  if(excludes!=null&&excludes.contains(pathKey)){
           	   System.out.println(pathKey+"为排除字段，不进行比对");
              }else{
			    diffHash.put(pathKey, "\n\t"+nameExpect+":["+expectJson+"]\n\t"+nameEctual+":["+actulJson+"]");  
              }
		   }
	   }
	}
	
	
}
