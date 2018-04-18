package com.lt.grabbing;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ct.lk.domain.Draw;
import com.lt.util.GameCode;
import com.lt.util.LottoXJUtils;
import com.lt.util.Market;
import com.lt.util.CommonUnits;

public class LottoGrabbingXJ extends LottoGrabbingTask {

	private static final Logger logger = LoggerFactory.getLogger(LottoGrabbingTJ.class);
	private String url;
	int error = 1;
	// public static void main(String[] args) {
	// LottoGrabbingXJ task = new LottoGrabbingXJ();
	// task.startGrabbing();
	// }

	public void startGrabbing() {
		String resultTime = CommonUnits.getNowDateTime();
		try {
			System.out.println("----------lotto XJ start----------");
			
			String selectDate = LottoXJUtils.getSelectDate();
			Document xmlDoc = Jsoup.connect(url).data("selectDate",selectDate).timeout(10000).post();			
			HashMap<String, JSONObject> newlist = LottoXJUtils.getNowNumber(xmlDoc);			
			String newNumber = newlist.get("firstAward").get("lotteryIssue").toString();
			String startNumber =newlist.get("lastAward").get("lotteryIssue").toString();
			List<Draw> list = null;
			List<Draw> drawlist = null;
			list = drawDAO.getDrawNum(GameCode.LT.name(), Market.XJ.name(), newNumber);
			drawlist = drawDAO.getDrawNumList(GameCode.LT.name(), Market.XJ.name(), startNumber, newNumber);						
			HashMap<String, String> awardMap = null;
			HashMap<String, String> httpRequestInfo = null;
			String newAward = null;

				if (list.isEmpty() && !drawlist.isEmpty()) {
					String lastNumber = drawlist.get(drawlist.size() - 1).getNumber();
					awardMap = supplyNumber(xmlDoc, lastNumber);
					list = drawlist;
				}

				if (!list.isEmpty()) {

					for (Draw dList : list) {
						String mappingNumber = dList.getNumber();
						if (awardMap != null) {
							newAward = awardMap.get(mappingNumber);
							if (newAward != null) {
								drawDAO.updateDrawResult(GameCode.LT.name(), Market.XJ.name(), mappingNumber, newAward);
							}
						} else {

							if (mappingNumber.equals(newNumber) && dList.getResult() == null) {
                                String tmpNewAward = newlist.get("firstAward").get("lotteryNumber").toString().replace(",", "");
								newAward = "[" + tmpNewAward + "]";
								httpRequestInfo = new HashMap<String, String>();
								httpRequestInfo.put("drawId", "" + dList.getId());
								httpRequestInfo.put("gameCode", GameCode.LT.name());
								httpRequestInfo.put("market", Market.XJ.name());
								httpRequestInfo.put("drawNumber", mappingNumber);
								httpRequestInfo.put("drawResultTime", resultTime);
								httpRequestInfo.put("result", newAward);

								updateData(socketHttpDestination, httpRequestInfo, logger);
							}
						}

					}

				}
			
			System.out.println("----------lotto XJ end----------");
			error = 1;
		} catch (Exception e) {
			System.out.println(e.toString());
			if (error <= 3) {
				System.out.println("XJ錯誤次數:" + error);
				error++;
				changeIP();
			} else {
				logger.error("Error in drawing " + Market.XJ.name() + " data. Error message: " + e.getMessage());
				drawDAO.insertErrorLog(GameCode.LT.name(), Market.XJ.name(), resultTime, 1);
				error = 1;
			}

		}

	}

	public HashMap<String, String> supplyNumber(Document xmlDoc, String lastNumber) throws IOException {

		HashMap<String, String> awardMap = new HashMap<String, String>();

		awardMap = LottoXJUtils.Crawl(xmlDoc, lastNumber);

		return awardMap;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
