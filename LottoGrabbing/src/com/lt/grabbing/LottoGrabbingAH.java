package com.lt.grabbing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ct.lk.domain.Draw;
import com.google.common.collect.Lists;
import com.lt.util.GameCode;
import com.lt.util.Market;

public class LottoGrabbingAH extends LottoGrabbingTask {
	private int ISSUE_PERIOD;// = 10;
	private String url;// = "http://data.ahfc.gov.cn/k3/index.html";
	int error = 1;
	
	private static final Logger logger = LoggerFactory.getLogger(LottoGrabbingAH.class);

	public static void main(String[] args) {
		LottoGrabbingAH task = new LottoGrabbingAH();
		task.startGrabbing();

	}

	public void startGrabbing() {
		try {
			System.out.println("********** Start AH Drawing, ISSUE_PERIOD=" + ISSUE_PERIOD + "**********");

			Pattern pattern = Pattern.compile("[0-9]*");
			Document doc = Jsoup.connect(url).timeout(5000).get();

			System.out.println("********** Start AH parsing **********");
			// Document doc = Jsoup.parse(downloadHtml(url));
			// Document doc = Jsoup.parse(new URL(url), 10000);
			Elements tdList = doc.getElementsByAttributeValue("class", "line_r");
			Elements tdList2 = doc.getElementsByClass("ball01");

			List<String> numberList = new ArrayList<String>();
			int idx = tdList.size() <= ISSUE_PERIOD ? 0 : tdList.size() - ISSUE_PERIOD;
			for (int i = idx; i < tdList.size(); i++) {
				if (pattern.matcher(tdList.get(i).text()).matches()) {
					numberList.add(tdList.get(i).text());
				}
			}
			if (numberList.size() > 0) {
				numberList = Lists.reverse(numberList);
				int num = numberList.size();

				List<String> resultList = new ArrayList<String>();
				for (int i = tdList2.size() - (3 * num); i < tdList2.size(); i = i + 3) {
					if ((i + 3) <= tdList2.size()) {
						resultList.add("[" + tdList2.get(i).text() + tdList2.get(i + 1).text()
								+ tdList2.get(i + 2).text() + "]");
					}
				}
				resultList = Lists.reverse(resultList);

				System.out.println("********** AH NumberList size=" + numberList.size() + ", resultList size="
						+ resultList.size() + " **********");

				String drawNumber = "";
				String drawResult = "";
				for (int i = 0; i < numberList.size(); i++) {
					drawNumber = numberList.get(i);
					drawResult = resultList.get(i);

					// System.out.println("********** ProcessDrawData ->
					// DrawNumber = " + drawNumber + ", DrawResult = " +
					// drawResult + " **********");
					// logger.info("********** ProcessDrawData -> DrawNumber = "
					// + drawNumber + ", DrawResult = " + drawResult + "
					// **********");

					processDrawData(drawNumber, drawResult);
				}
			}
			System.out.println("********** End AH **********");
			error = 1;
		} catch (HttpStatusException e) {
			e.printStackTrace();
			logger.error("Error in drawing " + Market.AH.name() + " data. Error message: " + e.getMessage());
		} catch (ConnectException e) {
			e.printStackTrace();
			if (error <= 3) {
				System.out.println("AH錯誤次數:" + error);
				error++;
				changeIP();
			} else {
				logger.error("Error in drawing " + Market.AH.name() + " data. Error message: " + e.getMessage());
				//sendNotifyMail("Error in drawing " + Market.AH.name() + " data","Error message: " + e.getMessage());
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error in drawing " + Market.AH.name() + " data. Error message: " + e.getMessage());
		}
	}

	private void processDrawData(String drawNumber, String drawResult) {
		List<Draw> checkResult = drawDAO.selectByDrawNumberAndMarket(Market.AH.name(), drawNumber, GameCode.K3.name());

		if (!checkResult.isEmpty()) {
			// System.out.println("Target DRAW data: " +
			// checkResult.get(0).drawNumberLogInfo());
			Draw draw = checkResult.get(0);
			HashMap<String, String> httpRequestInfo = new HashMap<String, String>();

			try {
				httpRequestInfo.put("drawId", "" + draw.getId());
				httpRequestInfo.put("gameCode", GameCode.K3.name());
				httpRequestInfo.put("market", Market.AH.name());
				httpRequestInfo.put("drawNumber", drawNumber);
				httpRequestInfo.put("result", drawResult);

				updateData(socketHttpDestination, httpRequestInfo, logger);

			} catch (Exception e) {
				e.printStackTrace();			
				logger.error("Error in drawing " + Market.AH.name() + " data. Error message: " + e.getMessage());
				//sendNotifyMail("Error in drawing " + Market.AH.name() + " data","Error message: " + e.getMessage());				
			}
		}

	}

	private String downloadHtml(String path) {
		InputStream is = null;
		try {
			String result = "";
			String line;

			URL url = new URL(path);
			is = url.openStream();// throws an IOException
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			while ((line = br.readLine()) != null) {
				result += line;
			}
			return result;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException ioe) {
				// nothing to see here
			}
		}
		return "";
	}

	public void setISSUE_PERIOD(int ISSUE_PERIOD) {
		this.ISSUE_PERIOD = ISSUE_PERIOD;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
