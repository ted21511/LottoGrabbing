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
import com.lt.util.CommonUnits;
import com.lt.util.GameCode;
import com.lt.util.LottoJXUtils;
import com.lt.util.Market;

public class LottoGrabbingJX extends LottoGrabbingTask {
	
	private static final Logger logger = LoggerFactory.getLogger(LottoGrabbingJX.class);
	private String url;// = "https://www.ydniu.com/open/70.html";
	int error = 1;

//	public static void main(String[] args) {
//		LottoGrabbingJX task = new LottoGrabbingJX();
//		task.startGrabbing();
//	}
	
	public void startGrabbing() {
		String resultTime = CommonUnits.getNowDateTime();
		try {
			System.out.println("----------lotto JX start----------");
			Document xmlDoc = Jsoup.connect(url).timeout(10000).post();
			List<Draw> list = null;
			List<Draw> drawlist = null;
			Element newList = LottoJXUtils.getNowNumber(xmlDoc);
			String newNumber = resultTime.substring(0, 2) + newList.select("td").get(1).text();
			String startNumber = newNumber.substring(0, 8) + "01";
			list = drawDAO.getDrawNum(GameCode.HL11x5.name(), Market.JX.name(), newNumber);
			drawlist = drawDAO.getDrawNumList(GameCode.HL11x5.name(), Market.JX.name(), startNumber, newNumber);
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
						 newAward = awardMap.get(mappingNumber.substring(2));
						 if (newAward != null){
							 drawDAO.updateDrawResult(GameCode.HL11x5.name(), Market.JX.name(), mappingNumber, newAward);
						 }						
					}else {
						if (mappingNumber.equals(newNumber) && dList.getResult() == null) {
							newAward = "[" + newList.select(".kj_hm").text().replace(" ", ",") + "]";
							
							httpRequestInfo = new HashMap<String, String>();
							httpRequestInfo.put("drawId", "" + dList.getId());
							httpRequestInfo.put("gameCode", GameCode.HL11x5.name());
							httpRequestInfo.put("market", Market.JX.name());
							httpRequestInfo.put("drawNumber", mappingNumber);
							httpRequestInfo.put("drawResultTime", resultTime);
							httpRequestInfo.put("result", newAward);

							updateData(socketHttpDestination, httpRequestInfo, logger);
							drawDAO.insertLog(httpRequestInfo,0);
						}
					}
				}				
			}
			
			System.out.println("----------lotto JX end----------");
			error = 1;
		}catch (Exception e) {
			e.printStackTrace();
			if (error <= 3) {
				System.out.println("JX錯誤次數:" + error);
				error++;
				changeIP();
			} else {
				logger.error("Error in drawing " + Market.JX.name() + " data. Error message: " + e.getMessage());
				drawDAO.insertErrorLog(GameCode.HL11x5.name(), Market.JX.name(), resultTime, 1);
				error = 1;
			}
		}
	}
	
	public HashMap<String, String> supplyNumber(Document xmlDoc, String lastNumber) throws IOException {
		
		HashMap<String, String> awardMap = new HashMap<String, String>();
		
		awardMap = LottoJXUtils.Crawl(xmlDoc, lastNumber);
		
		return awardMap;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
}
