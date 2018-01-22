package com.lt.util;

import java.util.HashMap;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LottoJXUtils {

	public static Element getNowNumber(Document xmlDoc) {

		Element newList = xmlDoc.select(".font_zt > tbody > tr").last();
		if (newList.select("td").get(1).text().equals("") || newList.select(".kj_hm").text().equals("")){
			newList = null;
		}else{
		System.out.println("JX最新彩期:" + newList.select("td").get(1).text() + " | " + newList.select(".kj_hm").text());
		}
		return newList;
	}

	public static HashMap<String, String> Crawl(Document xmlDoc, String lastNumber) {
		
		Elements list = xmlDoc.select(".font_zt > tbody > tr");
		lastNumber = lastNumber.substring(2);
		long Number = Long.parseLong(lastNumber);
		
		HashMap<String, String> awardMap = new HashMap<String, String>();
		
		for(int i = list.size()-1;i>= 1 ; i--){	
			String dateList = list.get(i).select("td").get(1).text();
			long dataNumber = Long.parseLong(dateList);
			if(Number <= dataNumber){
				String tmpAward = list.get(i).select(".kj_hm").text();
				String newAward = "[" + tmpAward.replace(" ", ",") + "]";
				awardMap.put(dateList, newAward);
			}else{
				 break;
			}			
		}		
		return awardMap;
	}
}
