package com.lt.grabbing;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ct.lk.domain.Draw;
import com.lt.util.GameCode;
import com.lt.util.LottoXJUtils;
import com.lt.util.Market;

public class LottoGrabbingXJ extends LottoGrabbingTask {

	private static final Logger logger = LoggerFactory.getLogger(LottoGrabbingTJ.class);
	private String url;
    int error = 1;
//	public static void main(String[] args) {
//		LottoGrabbingXJ task = new LottoGrabbingXJ();
//		task.startGrabbing();
//	}
	
	public void startGrabbing() {
		try {
			System.out.println("----------lotto XJ start----------");
			Document xmlDoc = Jsoup.connect(url).timeout(10000).post();
			String resultTime = LottoXJUtils.getNowDateTime();
			List<Draw> list = null;
			List<Draw> drawlist = null;
		    Element newList = LottoXJUtils.getNowNumber(xmlDoc);
			String tmpNewNumber  = newList.select(".z_bg_05").get(1).text();
			String newNumber = tmpNewNumber.substring(0, 8) + tmpNewNumber.substring(9,11);
			String startNumber = newNumber.substring(0, 8) + "01";	
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
		                 if (newAward != null){			 
		                	 drawDAO.updateDrawResult(GameCode.LT.name(), Market.XJ.name(), mappingNumber, newAward);
		                 }
					} else {

						if (mappingNumber.equals(newNumber) && dList.getResult() == null) {

							newAward = "[" + newList.select(".z_bg_13").text() + "]";
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
			e.printStackTrace();
			if (error <= 3) {
				System.out.println("XJ錯誤次數:" + error);
				error++;
				changeIP();
			} else {
				logger.error("Error in drawing " + Market.XJ.name() + " data. Error message: " + e.getMessage());
				//sendNotifyMail("Error in drawing " + Market.XJ.name() + " data", "Error message: " + e.getMessage());
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
