package com.lt.grabbing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.apache.bcel.internal.generic.LASTORE;

public class LottoGrabbingHK {

	private static final Logger logger = LoggerFactory.getLogger(LottoGrabbingHK.class);
	private static boolean flag = true;
	private String url = "http://bet.hkjc.com/marksix/Results.aspx?lang=ch";
	int error = 1;

//	public static void main(String[] args) {
//		LottoGrabbingTest6ball task = new LottoGrabbingTest6ball();
//		task.startGrabbing();
//	}

	public void startGrabbing() {
		
        HashMap<String, Set<String>> check = new HashMap<String, Set<String>>();
		try {
		Connection con = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
		con.data("radioDrawRange","GetDrawID");       
        con.data("radioResultType","GetAll");       
        con.data("hiddenSelectDrawID","20");
        Document doc = con.post();
		
        Elements result = doc.select("#_ctl0_ContentPlaceHolder1_resultsMarkSix_markSixResultTable > tbody > tr ");

		for(int i =1;i<=result.size()-1;i++){
			 Set<String> checkBall = new HashSet<String>();
			 
			 String tmpDraw = result.get(i).select("td").get(2).text();
			 String[] tmp = tmpDraw.split("/");
			 String draw = tmp[2]+tmp[1]+tmp[0];
			 System.out.println(draw);
			 Elements awards = result.get(i).select("td").get(4).select("img");
			
			 for(Element tmpAward : awards){
				
				String[] award =tmpAward.attr("src").split("_");
				
				if(!award[1].equals("special")){
					System.out.println(award[1]);
					checkBall.add(award[1]);
				}
			 }
			 check.put(draw, checkBall);
		}
       System.out.println(check.get("20180106"));
       
       
//	    System.out.println(checkBall.get("20180106"));
//	    String[] test = checkBall.get("20180106").split(",:|,");
//		for (String t : test) {
//			System.out.println(t);
//		}
//		
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
