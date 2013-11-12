/**
 * 
 */
package com.baidu.qa.service.test.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public  class DateTimeUtil {
	
	
	public static long calcTimeDifferenceWithMinutes(Date d1,Date d2){
		long diff = d2.getTime() - d1.getTime();
		return diff /(1000*60);
	}
	
	public static List<String> getDatelistByStartAndEnddate(String startdate,String enddate) throws ParseException{
		List list = new ArrayList();
		Calendar beginCal = Calendar.getInstance();
		Date start = DateTimeUtil.parseStringToDateByFormat(startdate,"yyyy-MM-dd");
		Date end = DateTimeUtil.parseStringToDateByFormat(enddate,"yyyy-MM-dd");
		beginCal.setTime(start);
		while(!start.after(end)){
			list.add(DateTimeUtil.parseDateToStringByFormat(start, "yyyy-MM-dd"));
			beginCal.add(Calendar.DATE, 1);
			start = beginCal.getTime();
		}
		
		return list;
	}
	
	public static String getLastday(){
		Calendar today = Calendar.getInstance();
		today.add(Calendar.DATE, -1);
		return DateTimeUtil.parseDateToStringByFormat(today.getTime(), "yyyy-MM-dd");
	}
	
	
	
	public static String getTodayToString(String format){
		Calendar today = Calendar.getInstance();
		return DateTimeUtil.parseDateToStringByFormat(today.getTime(), format);
	}
	
	
	public static String getLastdayByFormat(String format){
		Calendar today = Calendar.getInstance();
		today.add(Calendar.DATE, -1);
		return DateTimeUtil.parseDateToStringByFormat(today.getTime(), format);
	}
	

		public static String getSpecialdayByFormat(String dateStr,int day,String format) throws ParseException{
			Calendar beginCal = Calendar.getInstance();
			Date start = DateTimeUtil.parseStringToDateByFormat(dateStr,format);
			beginCal.setTime(start);
			beginCal.add(Calendar.DATE, day);
			return DateTimeUtil.parseDateToStringByFormat(beginCal.getTime(), format);
		}
	
	/**
	 * 计算指定日期加减日期后的日期
	 * @return
	 * @throws ParseException 
	 */
	public static String getSpecialday(String dateStr,int day) throws ParseException{
		Calendar beginCal = Calendar.getInstance();
		Date start = DateTimeUtil.parseStringToDateByFormat(dateStr,"yyyy-MM-dd");
		beginCal.setTime(start);
		beginCal.add(Calendar.DATE, day);
		return DateTimeUtil.parseDateToStringByFormat(beginCal.getTime(), "yyyy-MM-dd");
	}
	
	public static String parseDateToString(Date datetime){
		 SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 java.util.Date sDate=new java.util.Date(datetime.getTime());
		 return df.format(sDate);
	}
	
	public static String parseDateToStringByFormat(Date datetime,String format){
		 SimpleDateFormat df=new SimpleDateFormat(format);
		 java.util.Date sDate=new java.util.Date(datetime.getTime());
		 return df.format(sDate);
	}
	
	
	/**
	 * 
	 * @param dateString
	 * @return 返回默认时间格式为"yyyy-MM-dd HH:mm:SS"
	 * @throws ParseException
	 */
	public static Date parseStringToDate(String dateString) throws ParseException{
		DateFormat dateFormat;
	    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
	    dateFormat.setLenient(false);
	    java.util.Date timeDate = dateFormat.parse(dateString);//util类型
	    return timeDate;
	}
	
	
	/**
	 * 需要指定format
	 * @param dateString
	 * @param format 如：yyyy-MM-dd HH:mm:SS
	 * @return
	 * @throws ParseException
	 */
	public static Date parseStringToDateByFormat(String dateString,String format) throws ParseException{
		DateFormat dateFormat;
	    dateFormat = new SimpleDateFormat(format);
	    dateFormat.setLenient(false);
	    java.util.Date timeDate = dateFormat.parse(dateString);//util类型
	    return timeDate;
	}

    public Map getCurrentQuarter(Date date) {
        Calendar beginCal = Calendar.getInstance();
        beginCal.setTime(date);
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(date);
        int month = beginCal.get(Calendar.MONTH);
        if (month <= 2 && month >= 0) {
            beginCal.set(Calendar.MONTH, 0);
            endCal.set(Calendar.MONTH, 2);
        }
        else if (month <= 5 && month >= 3) {
            beginCal.set(Calendar.MONTH, 3);
            endCal.set(Calendar.MONTH, 5);
        }
        else if (month <= 8 && month >= 6) {
            beginCal.set(Calendar.MONTH, 6);
            endCal.set(Calendar.MONTH, 8);
        }
        else {
            beginCal.set(Calendar.MONTH, 9);
            endCal.set(Calendar.MONTH, 11);
        }
        beginCal.set(Calendar.DAY_OF_MONTH, beginCal.getActualMinimum(Calendar.DAY_OF_MONTH));
        beginCal.set(Calendar.HOUR_OF_DAY, 0);
        beginCal.set(Calendar.MINUTE, 0);
        beginCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        HashMap map = new HashMap();
        map.put("beginDate", beginCal.getTime());
        map.put("endDate", endCal.getTime());
        return map;
    }

    public Map getCurrentMonth(Date date) {
        Calendar beginCal = Calendar.getInstance();
        beginCal.setTime(date);
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(date);
        beginCal.set(Calendar.DAY_OF_MONTH, beginCal.getActualMinimum(Calendar.DAY_OF_MONTH));
        beginCal.set(Calendar.HOUR_OF_DAY, 0);
        beginCal.set(Calendar.MINUTE, 0);
        beginCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        HashMap map = new HashMap();
        map.put("beginDate", beginCal.getTime());
        map.put("endDate", endCal.getTime());
        return map;
    }
    public Map getCurrentWeek(Date date) {
        Calendar beginCal = Calendar.getInstance();
        beginCal.setTime(date);
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(date);
        beginCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        beginCal.set(Calendar.HOUR_OF_DAY, 0);
        beginCal.set(Calendar.MINUTE, 0);
        beginCal.set(Calendar.SECOND, 0);
        endCal.add(Calendar.DATE, 7);
        endCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        HashMap map = new HashMap();
        map.put("beginDate", beginCal.getTime());
        map.put("endDate", endCal.getTime());
        return map;
    }
    public Map getYearAndQuarter(Date date) {
        Calendar cal = Calendar.getInstance();
        HashMap map = new HashMap();
        int year = cal.get(Calendar.YEAR);
        map.put("year", year);
        int month = cal.get(Calendar.MONTH);
        if (month <= 2 && month >= 0) {
            map.put("quarter", 1);
        }
        else if (month <= 5 && month >= 3) {
            map.put("quarter", 2);
        }
        else if (month <= 8 && month >= 6) {
            map.put("quarter", 3);
        }
        else {
            map.put("quarter", 4);
        }
        return map;
    }
    
    public Map getLastYearAndQuarter(Integer year, Integer quarter) {
        HashMap map = new HashMap();
        if (quarter - 1 > 0){
            map.put("year", year);
            map.put("quarter", quarter - 1);
        } else {
            map.put("year", year - 1);
            map.put("quarter", 4);
        }
        return map;
    }
    
    public List<Map> getYearQList(Integer startYear, Integer startQ, Integer endYear, Integer endQ){
        List<Map> list = new  ArrayList<Map>();
        if (endYear.compareTo(startYear) == 0){
            for (int i = startQ; i <= endQ; i++){
                Map map = new HashMap();
                map.put("year", startYear);
                map.put("quarter", i);
                list.add(map);
            }
            
        } else if(endYear.compareTo(startYear) == 1) {
            for (int i = startQ; i <= 4; i++){
                Map map = new HashMap();
                map.put("year", startYear);
                map.put("quarter", i);
                list.add(map);
            }
            for (int i = startYear + 1; i < endYear; i++){
                for (int j = 1; j <= 4; j++){
                    Map map = new HashMap();
                    map.put("year", i);
                    map.put("quarter", j);
                    list.add(map);
                }
            }
            for (int i = 1; i <= endQ; i++){
                Map map = new HashMap();
                map.put("year", endYear);
                map.put("quarter", i);
                list.add(map);
            }
            
        }
        return list;
    }
    
    public List<Integer> getYearList(Integer startYear, Integer endYear){
        List<Integer> list = new  ArrayList<Integer>();
        for (int i = startYear; i <= endYear; i++){
            list.add(i);
        }
        return list;
    } 
    
    public static void main(String[] args){
        try {
			System.out.println(calcTimeDifferenceWithMinutes(parseStringToDateByFormat("20130515131922","yyyyMMddHHmmss"), new Date()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private static Date date = new Date(); 
    public static synchronized long nextLong(){  
    	
    	   Random rnd = new Random();
      date.setTime(System.currentTimeMillis());  
      String str = String.format("%1$tY%1$tm%1$td%1$tk%1$tM%1$tS%1$tL%2$02d", date, rnd.nextInt(100));  
      return Long.parseLong(str);  
    }  
}
