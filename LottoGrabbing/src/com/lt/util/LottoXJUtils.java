package com.lt.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LottoXJUtils {

	public static HashMap<String, JSONObject> getNowNumber(Document xmlDoc) {
		
		HashMap<String, JSONObject> newlist = new HashMap<String, JSONObject>();
		try {
			
			String json = xmlDoc.text();
			JSONArray jsonArray = new JSONArray(json);	
			JSONObject firstAward = jsonArray.getJSONObject(0);
			JSONObject lastAward = jsonArray.getJSONObject(jsonArray.length() - 1);
			
			System.out.println("XJ最新彩期:" + firstAward.get("lotteryIssue").toString() + " | " + firstAward.get("lotteryNumber").toString());
			
			newlist.put("firstAward", firstAward);
			newlist.put("lastAward", lastAward);
			
		}catch (Exception e) {
			System.out.println(e.toString());
			// TODO: handle exception
		}			
		return newlist;
	}
	public static String getSelectDate() {
		
		Calendar now = Calendar.getInstance();
		int checkTime = now.get(Calendar.HOUR_OF_DAY);
		if(checkTime < 3){
			now.add(Calendar.DAY_OF_MONTH, -1);
		}		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String selectDate = dateFormat.format(now.getTime());
			
		return selectDate;
	}

	public static HashMap<String, String> Crawl(Document xmlDoc, String lastNumber) {

		HashMap<String, String> awardMap = new HashMap<String, String>();
		long Number = Long.parseLong(lastNumber);
		
		try {
			String json = xmlDoc.text();
			JSONArray jsonArray = new JSONArray(json);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObj = jsonArray.getJSONObject(i);
				String dateList = jsonObj.get("lotteryIssue").toString();
				long dataNumber = Long.parseLong(dateList);
				if (Number <= dataNumber) {
					String newAward = jsonObj.get("lotteryNumber").toString();
					awardMap.put(dateList, newAward);
				}
			}

		} catch (Exception e) {
			System.out.println(e.toString());
		}
		
		return awardMap;
	}

}
