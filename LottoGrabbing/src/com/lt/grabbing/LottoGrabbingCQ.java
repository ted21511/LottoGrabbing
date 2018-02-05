package com.lt.grabbing;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ct.lk.domain.Draw;
import com.lt.util.GameCode;
import com.lt.util.LottoCQUtils;
import com.lt.util.Market;
import com.lt.util.CommonUnits;

public class LottoGrabbingCQ extends LottoGrabbingTask {

	private static final Logger logger = LoggerFactory.getLogger(LottoGrabbingCQ.class);
	private static boolean flag = true;
	private String url;
	int error = 1;
	
	// public static void main(String[] args) {
	// LottoGrabbingCQ task = new LottoGrabbingCQ();
	// task.startGrabbing();
	// }
	
	public void startGrabbing() {
		String resultTime = CommonUnits.getNowDateTime();
		try {
			System.out.println("----------lotto CQ start----------");
			Document xmlDoc = Jsoup.parse(new URL(url), 10000);
			List<Draw> list = null;
			List<Draw> drawlist = null;
			Elements newList = LottoCQUtils.getNowNumber(xmlDoc);
			String newNumber = resultTime.substring(0, 2) + newList.get(0).text();
			String startNumber = newNumber.substring(0, 8) + "001";
			list = drawDAO.getDrawNum(GameCode.LT.name(), Market.CQ.name(), newNumber);
			drawlist = drawDAO.getDrawNumList(GameCode.LT.name(), Market.CQ.name(), startNumber, newNumber);
			HashMap<String, String> awardMap = null;
			HashMap<String, String> httpRequestInfo = null;
			String newAward = null;

			if (list.isEmpty() && !drawlist.isEmpty()) {
				String lastNumber = drawlist.get(drawlist.size() - 1).getNumber();
				awardMap = supplyNumber(xmlDoc, url, lastNumber);
				list = drawlist;
			}

			if (!list.isEmpty()) {
				for (Draw dList : list) {
					String mappingNumber = dList.getNumber();
					if (awardMap != null) {
						String tmpnewAward = awardMap.get(mappingNumber.substring(2));
						if (tmpnewAward != null){
						newAward = tmpnewAward.replace("-", "");
						drawDAO.updateDrawResult(GameCode.LT.name(), Market.CQ.name(), mappingNumber, newAward);
						flag = true;
						}
					} else {

						if (mappingNumber.equals(newNumber) && dList.getResult() == null) {
							newAward = "[" + newList.get(1).text().replace("-", "") + "]";
							
							httpRequestInfo = new HashMap<String, String>();
							httpRequestInfo.put("drawId", "" + dList.getId());
							httpRequestInfo.put("gameCode", GameCode.LT.name());
							httpRequestInfo.put("market", Market.CQ.name());
							httpRequestInfo.put("drawNumber", mappingNumber);
							httpRequestInfo.put("drawResultTime", resultTime);
							httpRequestInfo.put("result", newAward);

							updateData(socketHttpDestination, httpRequestInfo, logger);
						}
					}
				}
			}
            
			System.out.println("----------lotto CQ end----------");
			error = 1;
		} catch (Exception e) {
			e.printStackTrace();
			if (error <= 3) {
				System.out.println("CQ錯誤次數:" + error);
				error++;
				changeIP();
			} else {
				logger.error("Error in drawing " + Market.CQ.name() + " data. Error message: " + e.getMessage());
				drawDAO.insertErrorLog(GameCode.LT.name(), Market.CQ.name(), resultTime, 1);
				error = 1;
			}
		}
	}

	public HashMap<String, String> supplyNumber(Document xmlDoc, String url, String lastNumber) throws IOException {

		HashMap<String, String> awardMap = new HashMap<String, String>();

		while (flag) {
			HashMap<String, String> tmpAwardList = LottoCQUtils.Crawl(xmlDoc, lastNumber);
			awardMap.putAll(tmpAwardList);

			if (awardMap.get(lastNumber.substring(2)) != null || tmpAwardList.size() == 0) {
				flag = false;
			} else {
				xmlDoc = LottoCQUtils.postNext(xmlDoc, url);
			}
		}

		return awardMap;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
