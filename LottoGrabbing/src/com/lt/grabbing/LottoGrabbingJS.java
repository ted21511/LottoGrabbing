package com.lt.grabbing;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ct.lk.domain.Draw;
import com.lt.util.CommonUnits;
import com.lt.util.GameCode;
import com.lt.util.LottoJSUtils;
import com.lt.util.Market;

public class LottoGrabbingJS extends LottoGrabbingTask {
	private String url; // = "http://caipiao.163.com/award/jskuai3/";
	int error = 1;

	private static final Logger logger = LoggerFactory.getLogger(LottoGrabbingJS.class);

	// public static void main(String[] args) {
	// LottoGrabbingJS task = new LottoGrabbingJS();
	// task.startGrabbing();
	// }

	public void startGrabbing() {
		String resultTime = CommonUnits.getNowDateTime();
		try {
			System.out.println("----------lotto JS start----------");

			Connection.Response doc = Jsoup.connect(url).data("index", "1").data("method", "CheckUpdate").timeout(10000)
					.method(Method.POST).execute();

			String[] tmpDoc = doc.body().split("<tbody>|<\\/tbody>");
			String tmpHtml = "<table>" + tmpDoc[1] + "</table>";
			Document xmlDoc = Jsoup.parse(tmpHtml);
			List<Draw> list = null;
			List<Draw> drawlist = null;
			Element newList = LottoJSUtils.getNowNumber(xmlDoc);
			String newNumber = newList.select("td").get(0).text().substring(0, 8) + "0"
					+ newList.select("td").get(0).text().substring(8, 10);
			String startNumber = newNumber.substring(0, 8) + "001";
			list = drawDAO.getDrawNum(GameCode.K3.name(), Market.JS.name(), newNumber);
			drawlist = drawDAO.getDrawNumList(GameCode.K3.name(), Market.JS.name(), startNumber, newNumber);
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
						String tmpMappingNumber = mappingNumber.substring(0,8)+mappingNumber.substring(9,11);
						newAward = awardMap.get(tmpMappingNumber);
						if (newAward != null){
							 drawDAO.updateDrawResult(GameCode.K3.name(), Market.JS.name(), mappingNumber, newAward);
						 }
					} else {
						if (mappingNumber.equals(newNumber) && dList.getResult() == null) {
							newAward = "[" + newList.select("td").get(1).text() + newList.select("td").get(2).text()
									+ newList.select("td").get(3).text() + "]";

							httpRequestInfo = new HashMap<String, String>();
							httpRequestInfo.put("drawId", "" + dList.getId());
							httpRequestInfo.put("gameCode", GameCode.K3.name());
							httpRequestInfo.put("market", Market.JS.name());
							httpRequestInfo.put("drawNumber", mappingNumber);
							httpRequestInfo.put("drawResultTime", resultTime);
							httpRequestInfo.put("result", newAward);

							updateData(socketHttpDestination, httpRequestInfo, logger);
							drawDAO.insertLog(httpRequestInfo, 0);
						}
					}
				}
			}

			System.out.println("----------lotto JS end----------");
			error = 1;
		} catch (Exception e) {
			e.printStackTrace();
			if (error <= 3) {
				System.out.println("JS錯誤次數:" + error);
				error++;
				changeIP();
			} else {
				logger.error("Error in drawing " + Market.JS.name() + " data. Error message: " + e.getMessage());
				drawDAO.insertErrorLog(GameCode.K3.name(), Market.JS.name(), resultTime, 1);
				error = 1;
			}
		}
	}

	public HashMap<String, String> supplyNumber(Document xmlDoc, String lastNumber) throws IOException {

		HashMap<String, String> awardMap = new HashMap<String, String>();

		awardMap = LottoJSUtils.Crawl(xmlDoc, lastNumber);

		return awardMap;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
