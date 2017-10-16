package com.lt.grabbing;

import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ct.lk.domain.Draw;
import com.lk.share.GameCode;
import com.lk.share.LottoKenoGrabbingTask;
import com.lk.share.Market;

public class LottoGrabbingJX extends LottoKenoGrabbingTask {
	private int ISSUE_PERIOD;// = 10;
	private String url;// = "https://www.ydniu.com/open/70.html";
	int error = 1;

	private static final Logger logger = LoggerFactory.getLogger(LottoGrabbingGD.class);


	public static void main(String[] args) {
		LottoGrabbingJX task = new LottoGrabbingJX();
		task.startGrabbing();
	}
	public void startGrabbing() {
		try {
//			Document doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
			Document doc = Jsoup.connect(url).timeout(10000).get();
			Elements tablelist = doc.select("table");
			if (tablelist.size() >= 12) {
				Element targetTable = tablelist.get(11);
				Elements tdList = targetTable.select("td");
	
				String drawNumber;
				String drawResult;
				if (tdList.size() > 0) {
					for (int i=0 ; i<ISSUE_PERIOD ; i++) {
						drawNumber = tdList.get(0).text().substring(0, 10);
						drawResult = "["+tdList.get(2).text().replace(" ", ",")+"]";
						
						//System.out.println("********** ProcessDrawData -> DrawNumber = " + drawNumber + ", DrawResult = " + drawResult + " **********");
						processDrawData(drawNumber, drawResult);
						tdList.remove(0);
						tdList.remove(0);
						tdList.remove(0);
					}
				}
			}
			error = 1;
		} catch (Exception e) {
			e.printStackTrace();
			if (error <= 3) {
				System.out.println("JX錯誤次數:" + error);
				error++;
				changeIP();
			} else {
				logger.error("Error in drawing " + Market.JX.name() + " data. Error message: " + e.getMessage());
				//sendNotifyMail("Error in drawing " + Market.JX.name() + " data","Error message: " + e.getMessage());
				error = 1;
			}
		} 
	}

	private void processDrawData(String drawNumber, String drawResult) {
		List<Draw> checkResult = drawDAO.selectByDrawNumberAndMarket(Market.JX.name(), drawNumber, GameCode.HL11x5.name());

		if (!checkResult.isEmpty()) {
			//System.out.println("Target DRAW data: " + checkResult.get(0).drawNumberLogInfo());
			Draw draw = checkResult.get(0);
			HashMap<String, String> httpRequestInfo = new HashMap<String, String>();
			
			try {				
				httpRequestInfo.put("drawId", "" + draw.getId());
				httpRequestInfo.put("gameCode", GameCode.HL11x5.name());
				httpRequestInfo.put("market", Market.JX.name());
				httpRequestInfo.put("drawNumber", drawNumber);
				httpRequestInfo.put("result", drawResult);

				updateData(socketHttpDestination, httpRequestInfo, logger);

			} catch (Exception e) {
				e.printStackTrace();				
				logger.error("Error in drawing " + Market.JX.name() + " data. Error message: " + e.getMessage());
				//sendNotifyMail("Error in drawing " + Market.JX.name() + " data","Error message: " + e.getMessage());			
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
