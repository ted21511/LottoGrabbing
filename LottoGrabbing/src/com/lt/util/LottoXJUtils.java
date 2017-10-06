package com.lt.util;

import java.text.SimpleDateFormat;
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

		Element newList = xmlDoc.getElementById("pagedata").select("tr").last();
				
		String newNumber  = newList.select(".z_bg_05").get(1).text();
		String award = newList.select(".z_bg_13").text();
				
		System.out.println("XJ最新彩期:" + newNumber + " | " + award);

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
