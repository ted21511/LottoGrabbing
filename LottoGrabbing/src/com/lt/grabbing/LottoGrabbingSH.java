package com.lt.grabbing;

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

public class LottoGrabbingSH extends LottoGrabbingTask {
	private int ISSUE_PERIOD;// = 10;
	private int DATA_COUNTER;
	private String url;// = "http://tc.gooooal.com/bonussh115!query.action";
	private List<String> DATAS = new ArrayList<String>();
	int error = 1;

	private static final Logger logger = LoggerFactory.getLogger(LottoGrabbingSH.class);


	public static void main(String[] args) {
		LottoGrabbingSH task = new LottoGrabbingSH();
		task.startGrabbing();
	}
	public void startGrabbing() {
		String resultTime = CommonUnits.getNowDateTime();
		try {
			System.out.println("----------lotto SH start----------");
			Document doc = Jsoup.connect(url).timeout(10000).get();
			Elements tdList = doc.select("td");
			
			int counter = 0;
			Element targetTd;
			DATA_COUNTER = ISSUE_PERIOD * 3;
			while (counter < DATA_COUNTER) {
				targetTd = tdList.get(counter);
				DATAS.add(targetTd.text().length()==0? targetTd.data():targetTd.text());
				counter++;
			}
			
			String drawNumber;
			String drawResult;
			int idx;
			for (int i = 0; i < ISSUE_PERIOD; i++) {
				drawNumber = DATAS.get(0);
				idx = DATAS.get(1).indexOf("'");
				drawResult = "[" + DATAS.get(1).substring(idx+1, idx+15) + "]";

				processDrawData(drawNumber, drawResult, resultTime);
				removeProcessedData();
			}
			System.out.println("----------lotto SH end----------");
			error = 1;
		} catch (Exception e) {
			e.printStackTrace();
			if (error <= 3) {
				System.out.println("SH錯誤次數:" + error);
				error++;
				changeIP();
			} else {
				logger.error("Error in drawing " + Market.SH.name() + " data. Error message: " + e.getMessage());
				drawDAO.insertErrorLog(GameCode.HL11x5.name(), Market.SH.name(), resultTime, 1);
				error = 1;
			}
		} 
	}

	private void processDrawData(String drawNumber, String drawResult, String resultTime) {
		List<Draw> checkResult = drawDAO.selectByDrawNumberAndMarket(Market.SH.name(), drawNumber, GameCode.HL11x5.name());

		if (!checkResult.isEmpty()) {
			//System.out.println("Target DRAW data: " + checkResult.get(0).drawNumberLogInfo());
			Draw draw = checkResult.get(0);
			HashMap<String, String> httpRequestInfo = new HashMap<String, String>();
						
				httpRequestInfo.put("drawId", "" + draw.getId());
				httpRequestInfo.put("gameCode", GameCode.HL11x5.name());
				httpRequestInfo.put("market", Market.SH.name());
				httpRequestInfo.put("drawNumber", drawNumber);
				httpRequestInfo.put("drawResultTime", resultTime);
				httpRequestInfo.put("result", drawResult);

				if (draw.getResult() == null || draw.getResult().length() == 0) {
					updateData(socketHttpDestination, httpRequestInfo, logger);
					drawDAO.insertLog(httpRequestInfo,0);
				}
		}

	}

	private void removeProcessedData() {
		DATAS.remove(0);
		DATAS.remove(0);
		DATAS.remove(0);
	}

	public void setISSUE_PERIOD(int ISSUE_PERIOD) {
		this.ISSUE_PERIOD = ISSUE_PERIOD;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
