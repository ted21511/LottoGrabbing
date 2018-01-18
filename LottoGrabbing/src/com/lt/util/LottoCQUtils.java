package com.lt.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class LottoCQUtils {
	
	public static Elements getNowNumber(Document xmlDoc) {
		
		Elements newList = xmlDoc.getElementById("openlist").select("ul").get(1).select("li");	
		System.out.println("CQ最新彩期:"+newList.get(0).text()+" | "+newList.get(1).text());
		
        return newList;
	}
		
	public static String getNowDateNumber() {
	
	   Date now = new Date();
	   SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd"); 
	   String nowdate = dateFormat.format(now); 
 	
	   return nowdate;
	}
	
	public static Document postNext(Document xmlDoc,String url) throws IOException {
		
		String viewstate = xmlDoc.select("#__VIEWSTATE").val();
		String eventvalidation = xmlDoc.select("#__EVENTVALIDATION").val();
		
		
		PostDataInfo postDataInfo = new PostDataInfo();
		postDataInfo.set__EVENTTARGET("lnkBtnNext");
		postDataInfo.set__VIEWSTATE(viewstate);
		postDataInfo.set__EVENTVALIDATION(eventvalidation);
		
			
		Connection con = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
	    con.data("__EVENTTARGET",postDataInfo.get__EVENTTARGET());       
        con.data("__VIEWSTATE",postDataInfo.get__VIEWSTATE());       
        con.data("__EVENTVALIDATION",postDataInfo.get__EVENTVALIDATION());
        Document doc = con.post();
        
		return doc;
	}
	
	public static HashMap<String, String> Crawl(Document xmlDoc,String lastNumber) {
		
		Elements list = xmlDoc.getElementById("openlist").select("ul");
			
		lastNumber = lastNumber.substring(2);
		long Number = Long.parseLong(lastNumber);
		
		HashMap<String, String> awardMap = new HashMap<String, String>();
		
		for(int i = 1;i<= list.size()-1 ; i++){	
			String dateList = list.get(i).select("li").get(0).text();	
			long dataNumber = Long.parseLong(dateList);
			if(Number <= dataNumber){
			String tmpAward = list.get(i).select("li").get(1).text();
			String newAward = "[" + tmpAward.replace("-", "") + "]";	
	    	awardMap.put(dateList, newAward);					
			}else{								
			  break;	
			}
		}
		
		return awardMap;
	}
}
