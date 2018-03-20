package com.lt.grabbing;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ct.lk.domain.Draw;
import com.lt.util.GameCode;
import com.lt.util.HtmlUnit;
import com.lt.util.LottoTJUtils;
import com.lt.util.Market;
import com.lt.util.CommonUnits;

public class LottoGrabbingTJ extends LottoGrabbingTask {

	private static final Logger logger = LoggerFactory.getLogger(LottoGrabbingTJ.class);
	private String url;
	int error = 1;
	// public static void main(String[] args) {
	// LottoGrabbingTJ task = new LottoGrabbingTJ();
	// task.startGrabbing();
	// }

	public void startGrabbing() {
		String resultTime = CommonUnits.getNowDateTime();
		try {
			System.out.println("----------lotto TJ start----------");
//			Document doc = null;
//
//			if (error > 1) {
//				doc = HtmlUnit.changeIPStart(url, proxyHost, 80);
//			} else {
//				doc = HtmlUnit.start(url);
//			}

			Document doc = error > 1 ? HtmlUnit.changeIPStart(url, proxyHost, 80) : HtmlUnit.start(url);
			List<Draw> list = null;
			List<Draw> drawlist = null;
			Element newList = LottoTJUtils.getNowNumber(doc);
			Elements award = newList.select(".r_td_win_Css");
			String newNumber = newList.select(".ReportTable_Td_TermCode_Css").text();

			String startNumber = newNumber.substring(0, 8) + "001";
			list = drawDAO.getDrawNum(GameCode.LT.name(), Market.TJ.name(), newNumber);
			drawlist = drawDAO.getDrawNumList(GameCode.LT.name(), Market.TJ.name(), startNumber, newNumber);
			HashMap<String, String> awardMap = null;
			HashMap<String, String> httpRequestInfo = null;
			String newAward = null;

			if (list.isEmpty() && !drawlist.isEmpty()) {
				String lastNumber = drawlist.get(drawlist.size() - 1).getNumber();
				awardMap = supplyNumber(doc, lastNumber);
				list = drawlist;
			}

			if (!list.isEmpty()) {
				for (Draw dList : list) {
					String mappingNumber = dList.getNumber();
					if (awardMap != null) {
						newAward = awardMap.get(mappingNumber);
						if(newAward != null){
							drawDAO.updateDrawResult(GameCode.LT.name(), Market.TJ.name(), mappingNumber, newAward);
						}
					} else {

						if (mappingNumber.equals(newNumber) && dList.getResult() == null) {

							newAward = "[";
							for (int i = 0; i <= award.size() - 1; i++) {
								newAward = newAward + award.get(i).text();
							}
							newAward = newAward + "]";

							httpRequestInfo = new HashMap<String, String>();
							httpRequestInfo.put("drawId", "" + dList.getId());
							httpRequestInfo.put("gameCode", GameCode.LT.name());
							httpRequestInfo.put("market", Market.TJ.name());
							httpRequestInfo.put("drawNumber", mappingNumber);
							httpRequestInfo.put("drawResultTime", resultTime);
							httpRequestInfo.put("result", newAward);

							updateData(socketHttpDestination, httpRequestInfo, logger);
						}
					}

				}

			}
			System.out.println("----------lotto TJ end----------");
			error = 1;
		} catch (Exception e) {
			System.out.println(e.toString());
			if (error <= 3) {
				System.out.println("TJ錯誤次數:" + error);
				error++;
				startGrabbing();
			} else {
				logger.error("Error in drawing " + Market.TJ.name() + " data. Error message: " + e.getMessage());
				drawDAO.insertErrorLog(GameCode.LT.name(), Market.TJ.name(), resultTime, 1);
				error = 1;
			}
		}

	}

	public HashMap<String, String> supplyNumber(Document Doc, String lastNumber) throws IOException {

		HashMap<String, String> awardMap = new HashMap<String, String>();

		awardMap = LottoTJUtils.Crawl(Doc, lastNumber);

		return awardMap;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
