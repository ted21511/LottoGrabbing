package com.lt.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LottoXJUtils {

	public static String getNowDateTime() {

		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String dateTime = dateFormat.format(now);

		return dateTime;
	}

	public static Element getNowNumber(Document xmlDoc) {
			
		Calendar now = Calendar.getInstance();
		int checkTime = now.get(Calendar.HOUR_OF_DAY);		
		if(checkTime < 10){
			now.add(Calendar.DAY_OF_MONTH, -1);
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String dateTime = dateFormat.format(now.getTime());
 
		Elements newLists = new Elements();
		Elements tmpnewList = xmlDoc.getElementById("pagedata").select("tr");
		for(Element tmp : tmpnewList){
			
			String mappingDate = tmp.select(".z_bg_05").get(1).text().substring(0,8);
			if(dateTime.equals(mappingDate)){
				newLists.add(tmp);
			}
		}
		Element newList = newLists.last();
		if(!newLists.isEmpty()){
		String newNumber  = newList.select(".z_bg_05").get(1).text();
		String award = newList.select(".z_bg_13").text();
		
		System.out.println("XJ最新彩期:" + newNumber + " | " + award);
		}
		
		return newList;
	}

	public static HashMap<String, String> Crawl(Document xmlDoc, String lastNumber) {

		Elements list = xmlDoc.getElementById("pagedata").select("tr");
		
		String tmplastNumber = lastNumber.substring(0,8) +"0"+lastNumber.substring(8,10);
		long Number = Long.parseLong(tmplastNumber);

		HashMap<String, String> awardMap = new HashMap<String, String>();

		for (int i = 0; i <= list.size() - 1; i++) {

			String tmpdateList = list.get(i).select(".z_bg_05").get(1).text();	
			long dataNumber = Long.parseLong(tmpdateList);
				if (Number <= dataNumber) {
					String dateList = tmpdateList.substring(0,8) + tmpdateList.substring(9,11);
					String newAward = "[" + list.get(i).select(".z_bg_13").text() + "]";
					awardMap.put(dateList, newAward);		
			}
		}
		return awardMap;
	}

}
