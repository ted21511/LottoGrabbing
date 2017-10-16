package com.lt.grabbing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ct.lk.domain.Draw;
import com.lt.util.GameCode;
import com.lt.util.Market;

public class LottoGrabbingJS extends LottoGrabbingTask {
	private int ISSUE_PERIOD; //= 10;
	private String url; //= "http://caipiao.163.com/award/jskuai3/";
	int error = 1;
	
	private static final Logger logger = LoggerFactory.getLogger(LottoGrabbingJS.class);
	
	public static void main(String[] args) {
		LottoGrabbingJS task = new LottoGrabbingJS();
		task.startGrabbing();

	}
	
	public void startGrabbing() {
		try {
//			Document doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
			Document doc = Jsoup.connect(url).timeout(10000).get();
			Elements elements = doc.select(".start");
			elements.remove(0);
			elements.remove(0);
			elements.remove(0);
			
			HashMap<String, String>	map = new HashMap<String, String>();
			String result;
			for (Element e : elements) {
				result = e.attr("data-win-number");
				if (!result.isEmpty()) {
					map.put("20"+e.attr("data-period"), "["+e.attr("data-win-number").replace(" ", "")+"]");
				}
			}
			List<String> drawNumberList = new ArrayList<String>(map.keySet());
			Collections.sort(drawNumberList);
			Collections.reverse(drawNumberList);
						
			String drawNumber = "";
			String drawResult = "";
			int numOfDatas = drawNumberList.size();
			if (numOfDatas >= ISSUE_PERIOD) numOfDatas = ISSUE_PERIOD;	
			for (int i = 0; i < numOfDatas; i++) {
				drawNumber = drawNumberList.get(i);
				drawResult = map.get(drawNumber);

				//System.out.println("********** ProcessDrawData -> DrawNumber = " + drawNumber + ", DrawResult = " + drawResult + " **********");
				//logger.info("********** ProcessDrawData -> DrawNumber = " + drawNumber + ", DrawResult = " + drawResult + " **********");

				processDrawData(drawNumber, drawResult);
			}
			error = 1;
		} catch (Exception e) {
			e.printStackTrace();
			if (error <= 3) {
				System.out.println("JS錯誤次數:" + error);
				error++;
				changeIP();
			} else {
				logger.error("Error in drawing " + Market.JS.name() + " data. Error message: " + e.getMessage());
				//sendNotifyMail("Error in drawing " + Market.JS.name() + " data","Error message: " + e.getMessage());
				error = 1;
			}
		} 
	}

	private void processDrawData(String drawNumber, String drawResult) {
		List<Draw> checkResult = drawDAO.selectByDrawNumberAndMarket(Market.JS.name(), drawNumber, GameCode.K3.name());

		if (!checkResult.isEmpty()) {
			//System.out.println("Target DRAW data: " + checkResult.get(0).drawNumberLogInfo());
			Draw draw = checkResult.get(0);
			HashMap<String, String> httpRequestInfo = new HashMap<String, String>();
			
			try {				
				httpRequestInfo.put("drawId", "" + draw.getId());
				httpRequestInfo.put("gameCode", GameCode.K3.name());
				httpRequestInfo.put("market", Market.JS.name());
				httpRequestInfo.put("drawNumber", drawNumber);
				httpRequestInfo.put("result", drawResult);

				updateData(socketHttpDestination, httpRequestInfo, logger);

			} catch (Exception e) {
				e.printStackTrace();				
				logger.error("Error in drawing " + Market.JS.name() + " data. Error message: " + e.getMessage());
				//sendNotifyMail("Error in drawing " + Market.JS.name() + " data","Error message: " + e.getMessage());
					
			}
		}

	}

	public void setISSUE_PERIOD(int ISSUE_PERIOD) {
		this.ISSUE_PERIOD = ISSUE_PERIOD;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
