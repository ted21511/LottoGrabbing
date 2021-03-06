package com.lt.grabbing;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ct.lk.domain.Draw;
import com.google.common.collect.Lists;
import com.lt.util.GameCode;
import com.lt.util.Market;

public class LottoGrabbingGD extends LottoGrabbingTask {
	private int ISSUE_PERIOD;// = 10;
	private String url;// = "http://www.gdlottery.cn/odata/zst11xuan5.jspx";
	int error = 1;
	
	private static final Logger logger = LoggerFactory.getLogger(LottoGrabbingGD.class);

	public static void main(String[] args) {
		LottoGrabbingGD task = new LottoGrabbingGD();
		task.startGrabbing();
	}

	public void startGrabbing() {
		try {
			Pattern pattern = Pattern.compile("[0-9]*");
			Document doc = Jsoup.connect(url).timeout(5000).get();
//			System.out.println(doc);
//			Document doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
			Elements tablelist = doc.select("table");
			if (tablelist.size() >= 3) {
				Element targetTable = tablelist.get(2);
				Elements tdList = targetTable.getElementsByAttributeValue("bgcolor", "#FFFFFF");
				Elements resultLists = targetTable.select("strong");
				String number;
				List<String> numberList = new ArrayList<String>();
				List<String> resultList = new ArrayList<String>();
				resultList = resultLists.eachText();
				for (Element e : tdList) {
					number = e.text().trim();
					if (pattern.matcher(number).matches())
						numberList.add(number);
				}
				numberList = Lists.reverse(numberList);	
				resultList = Lists.reverse(resultList);
				
				String drawNumber;
				String drawResult;	
				int numOfDatas = numberList.size();
				if (numOfDatas >= ISSUE_PERIOD) numOfDatas = ISSUE_PERIOD;
				for (int i = 0; i < numOfDatas; i++) {
					drawNumber = numberList.get(i);
					drawResult = "[" + resultList.get(i).trim().replace("，", ",") + "]";
	
					//System.out.println("********** ProcessDrawData -> DrawNumber = " + drawNumber + ", DrawResult = " + drawResult + " **********");
					//logger.info("********** ProcessDrawData -> DrawNumber = " + drawNumber + ", DrawResult = " + drawResult + " **********");
	
					processDrawData(drawNumber, drawResult);
				}
			}
			error = 1;
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			if (error <= 3) {
				System.out.println("GD錯誤次數:" + error);
				error++;
				changeIP();
			} else {
				logger.error("Error in drawing " + Market.GD.name() + " data. Error message: " + e.getMessage());
				//sendNotifyMail("Error in drawing " + Market.GD.name() + " data","Error message: " + e.getMessage());
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error in drawing " + Market.GD.name() + " data. Error message: " + e.getMessage());
		}
	}

	private void processDrawData(String drawNumber, String drawResult) {
		List<Draw> checkResult = drawDAO.selectByDrawNumberAndMarket(Market.GD.name(), drawNumber, GameCode.HL11x5.name());

		if (!checkResult.isEmpty()) {
			//System.out.println("Target DRAW data: " + checkResult.get(0).drawNumberLogInfo());
			Draw draw = checkResult.get(0);
			HashMap<String, String> httpRequestInfo = new HashMap<String, String>();
			
			try {				
				httpRequestInfo.put("drawId", "" + draw.getId());
				httpRequestInfo.put("gameCode", GameCode.HL11x5.name());
				httpRequestInfo.put("market", Market.GD.name());
				httpRequestInfo.put("drawNumber", drawNumber);
				httpRequestInfo.put("result", drawResult);

				updateData(socketHttpDestination, httpRequestInfo, logger);

			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error in drawing " + Market.GD.name() + " data. Error message: " + e.getMessage());
				//sendNotifyMail("Error in drawing " + Market.GD.name() + " data","Error message: " + e.getMessage());				
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
