package com.lt.util;

import java.util.HashMap;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LottoJSUtils {
	
	public static Element getNowNumber(Document xmlDoc) {

		Element newList = xmlDoc.select("tr").last();
		String award = newList.select("td").get(1).text()+newList.select("td").get(2).text()+newList.select("td").get(3).text();
    	System.out.println("JS最新彩期:" + newList.select("td").get(0).text() + " | " + award);

		return newList;
	}

	public static HashMap<String, String> Crawl(Document xmlDoc, String lastNumber) {
		
		Elements list = xmlDoc.select("tr");
		lastNumber = lastNumber.substring(0,8)+lastNumber.substring(9,11);
		long Number = Long.parseLong(lastNumber);
		
		HashMap<String, String> awardMap = new HashMap<String, String>();
		for(int i = list.size()-1;i>= 1 ; i--){	
			String dateList = list.get(i).select("td").get(0).text();
			long dataNumber = Long.parseLong(dateList);
			if(Number <= dataNumber){
				String tmpAward = list.get(i).select("td").get(1).text() + list.get(i).select("td").get(2).text() + list.get(i).select("td").get(3).text();
				String newAward = "[" + tmpAward + "]";
				awardMap.put(dateList, newAward);
			}else{
				 break;
			}			
		}		
		return awardMap;
	}
}
