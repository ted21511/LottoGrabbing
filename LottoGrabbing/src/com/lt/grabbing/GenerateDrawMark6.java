package com.lt.grabbing;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ct.lk.domain.Draw;

public class GenerateDrawMark6 extends LottoGrabbingTask {

	private String url = "https://kjrq.org/";

	public void startGrabbing() {
		System.out.println("----------GenerateDraw Mark6 start----------");
		try {

			List<Draw> draws = drawDAO.selectLastMark6();
			String[] drawMonth = draws.get(0).getDate().toString().split("-");
			int drawM = Integer.parseInt(drawMonth[1]);
			Calendar calendar = Calendar.getInstance();
			String year = Integer.toString(calendar.get(Calendar.YEAR));
			
			int tmpThisMonth = calendar.get(Calendar.MONTH) + 1;
			String thismonth = (tmpThisMonth < 10 ? "0" : "") + tmpThisMonth;
			String Url = url + "d/?d=" + year + "-" + thismonth;
			
			int tmpMonth = calendar.get(Calendar.MONTH) + 2;
			String month = (tmpMonth < 10 ? "0" : "") + tmpMonth;	
			String tmpUrl = url + "d/?d=" + year + "-" + month;
			
			int tmpNextMonth = calendar.get(Calendar.MONTH) + 3;
			String nextMonth = (tmpNextMonth < 10 ? "0" : "") + tmpNextMonth;
			String tmpNextUrl = url + "d/?d=" + year + "-" + nextMonth;
			
			List<String> oldDates = new ArrayList<String>();
			List<String> dates = new ArrayList<String>();
			
			if(tmpThisMonth == drawM){
				Document xmlDoc = Jsoup.connect(Url).timeout(10000).get();
				String checkLastDay = xmlDoc.select(".pm4").select(".td_kj").last().text();
				if(!checkLastDay.equals(drawMonth[2])){
					Document Doc = Jsoup.connect(tmpUrl).timeout(10000).get();
					String firstDay = Doc.select(".td_kj").get(0).text();
					int checkdate = Integer.parseInt(drawMonth[2]);
					Elements tmpDates = xmlDoc.select(".pm4").select(".td_kj");
					for(Element date : tmpDates){
						int selectDate = Integer.parseInt(date.text());
						if(selectDate > checkdate){
							oldDates.add(date.text());						
						}
					}
					makeDraw(oldDates,year,thismonth,month,firstDay);			
				}	
			}
			
			if (tmpMonth == drawM) {
				Document xmlDoc = Jsoup.connect(tmpUrl).timeout(10000).get();
				String checkLastDay = xmlDoc.select(".pm4").select(".td_kj").last().text();
				if(!checkLastDay.equals(drawMonth[2])){
					Document Doc = Jsoup.connect(tmpNextUrl).timeout(10000).get();
					String firstDay = Doc.select(".td_kj").get(0).text();
					int checkdate = Integer.parseInt(drawMonth[2]);
					Elements tmpDates = xmlDoc.select(".pm4").select(".td_kj");
					for(Element date : tmpDates){
						int selectDate = Integer.parseInt(date.text());
						if(selectDate > checkdate){
							dates.add(date.text());						
						}
					}					
					makeDraw(dates,year,month,nextMonth,firstDay);				
				}
			
			}else if(tmpMonth > drawM){
				Document xmlDoc = Jsoup.connect(tmpUrl).timeout(10000).get();
		    	Document Doc = Jsoup.connect(tmpNextUrl).timeout(10000).get();
				String firstDay = Doc.select(".td_kj").get(0).text();
				Elements tmpDates = xmlDoc.select(".pm4").select(".td_kj");
				for(Element date : tmpDates){
					dates.add(date.text());	
				}				
				makeDraw(dates,year,month,nextMonth,firstDay);				
			}
            
			System.out.println("----------GenerateDraw Mark6 end----------");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
		}
	}

	public void makeDraw(List<String> dates, String year, String month, String nextMonth, String firstDay) {

		try {

			int lastDraw = drawDAO.selectDrawMarketMark6();
			String checkYear = Integer.toString(lastDraw).substring(0, 4);

			if (!year.equals(checkYear)) {
				lastDraw = Integer.parseInt(year + "000");
			}

			for (int i = 0; i <= dates.size() - 1; i++) {

				String date = year + "-" + month + "-" + dates.get(i);
				String nextDate = "";
				String drawNumber = "";
				int j = i + 1;
				if (j < dates.size()) {
					nextDate = year + "-" + month + "-" + dates.get(j);
				} else {
					nextDate = year + "-" + nextMonth + "-" + firstDay;
				}
				drawNumber = Integer.toString(lastDraw + j);

				Draw draw = new Draw();

				SimpleDateFormat drawDateFormat = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				Date drawDate = drawDateFormat.parse(date);
				Date beginTime = timeFormat.parse(date + " 21:30:00.000");
				Date endTime = timeFormat.parse(nextDate + " 21:30:00.000");
				Date closeTime = timeFormat.parse(nextDate + " 21:25:00.000");
				Date officialEndTime = timeFormat.parse(nextDate + " 21:25:00.000");

				draw.setDate(drawDate);
				draw.setNumber(drawNumber);
				draw.setSequence(1);
				draw.setGameCode("MARK6");
				draw.setMarket("HK");
				draw.setBeginTime(beginTime);
				draw.setEndTime(endTime);
				draw.setCloseTime(closeTime);
				draw.setStatus("S");
				draw.setOfficialEndTime(officialEndTime);
				draw.setTotalTicket(0);

				drawDAO.saveDrawMark6(draw);
			}
			int updateLastDraw = lastDraw + dates.size();
			drawDAO.updateDrawMarketMark6(updateLastDraw);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
		}

	}

}
