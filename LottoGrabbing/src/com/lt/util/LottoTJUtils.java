package com.lt.util;

import java.util.HashMap;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LottoTJUtils {


	public static Element getNowNumber(Document xmlDoc) {
		
		Elements tmpnewList = xmlDoc.getElementsByClass("ReportTable_Tr_Content_Css");
		Element newList = tmpnewList.get(tmpnewList.size()-2);
		String award = newList.select(".r_td_win_Css").text();
		String newNumber = newList.select(".ReportTable_Td_TermCode_Css").text();
		
		System.out.println("TJ最新彩期:" + newNumber + " | " + award);

		return newList;
	}

	public static HashMap<String, String> Crawl(Document Doc, String lastNumber) {

		Elements list = Doc.getElementsByClass("ReportTable_Tr_Content_Css");
		long Number = Long.parseLong(lastNumber);

		HashMap<String, String> awardMap = new HashMap<String, String>();

		for (int i = list.size() - 1; i >= 0; i--) {
			String dateList = list.get(i).select(".ReportTable_Td_TermCode_Css").text();
			long dataNumber = Long.parseLong(dateList);
			if (Number <= dataNumber) {

				Elements award = list.get(i).select(".r_td_win_Css");
				String newAward = "[";
				for (int j = 0; j <= award.size() - 1; j++) {
					newAward = newAward + award.get(j).text();
				}
				newAward = newAward + "]";

				awardMap.put(dateList, newAward);

			}
		}

		return awardMap;
	}
}
