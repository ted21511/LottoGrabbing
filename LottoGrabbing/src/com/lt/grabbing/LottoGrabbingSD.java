package com.lt.grabbing;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ct.lk.domain.Draw;
import com.lt.util.CommonUnits;
import com.lt.util.GameCode;
import com.lt.util.Market;

public class LottoGrabbingSD extends LottoGrabbingTask {
	private int ISSUE_PERIOD;// = 10;
	private int DATA_COUNTER;
	private String url;// = "http://www.sdticai.com/find/find_syxw.asp";
	int error = 1;

	private static final Logger logger = LoggerFactory.getLogger(LottoGrabbingSD.class);

	public static void main(String[] args) {
		LottoGrabbingSD task = new LottoGrabbingSD();
		task.startGrabbing();
	}

	public void startGrabbing() {
		String resultTime = CommonUnits.getNowDateTime();
		try {
			System.out.println("----------lotto SD start----------");
			Document doc= Jsoup.parse(new URL(url), 10000);
			Elements tablelist = doc.select("table");
			if (tablelist.size() >= 15) {
				Element targetTable = tablelist.get(14);
				Elements tdList = targetTable.getElementsByAttributeValue("height", "20");
	
				int counter = 0;
				Element targetTd;
				DATA_COUNTER = ISSUE_PERIOD * 6;
				List<String> datas = new ArrayList<String>();
				while (counter < DATA_COUNTER) {
					targetTd = tdList.get(counter);
					datas.add(targetTd.text());
					counter++;
				}
				
				String drawNumber = "";
				String drawResult = "";
				for (int i = 0; i < ISSUE_PERIOD; i++) {
					drawNumber = datas.get(0);
					drawResult = "[" + datas.get(1) + "," + datas.get(2) + "," + datas.get(3) + "," + datas.get(4) + ","
							+ datas.get(5) + "]";
	
					processDrawData(drawNumber, drawResult, resultTime);
					removeProcessedData(datas);
				}
			}
			System.out.println("----------lotto SD end----------");
			error = 1;
		} catch (Exception e) {
			e.printStackTrace();
			if (error <= 3) {
				System.out.println("SD錯誤次數:" + error);
				error++;
				changeIP();
			} else {
				logger.error("Error in drawing " + Market.SD.name() + " data. Error message: " + e.getMessage());
				drawDAO.insertErrorLog(GameCode.HL11x5.name(), Market.SD.name(), resultTime, 1);
				error = 1;
			}
		} 

	}

	private void processDrawData(String drawNumber, String drawResult, String resultTime) {
		List<Draw> checkResult = drawDAO.selectByDrawNumberAndMarket(Market.SD.name(), drawNumber, GameCode.HL11x5.name());

		if (!checkResult.isEmpty()) {
			Draw draw = checkResult.get(0);
			HashMap<String, String> httpRequestInfo = new HashMap<String, String>();
			
				httpRequestInfo.put("drawId", "" + draw.getId());
				httpRequestInfo.put("gameCode", GameCode.HL11x5.name());
				httpRequestInfo.put("market", Market.SD.name());
				httpRequestInfo.put("drawNumber", drawNumber);
				httpRequestInfo.put("drawResultTime", resultTime);
				httpRequestInfo.put("result", drawResult);

				if (draw.getResult() == null || draw.getResult().length() == 0) {
					updateData(socketHttpDestination, httpRequestInfo, logger);
					drawDAO.insertLog(httpRequestInfo,0);
				}
		}

	}

	private void removeProcessedData(List<String> datas) {
		datas.remove(0);
		datas.remove(0);
		datas.remove(0);
		datas.remove(0);
		datas.remove(0);
		datas.remove(0);
	}

	public void setISSUE_PERIOD(int ISSUE_PERIOD) {
		this.ISSUE_PERIOD = ISSUE_PERIOD;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
