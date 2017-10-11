package com.lt.grabbing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
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

	// public void start() {
	// LottoGrabbingSD task = new LottoGrabbingSD();
	// task.loadConfiguration();
	// }

	public void startGrabbing() {
		try {
			System.out.println("********** Start SD Drawing, ISSUE_PERIOD="+ISSUE_PERIOD+"**********");
			Document doc= Jsoup.parse(new URL(url), 10000);
//			Document doc = Jsoup.parse(downloadHtml(url));
			Elements tablelist = doc.select("table");
			if (tablelist.size() >= 15) {
				Element targetTable = tablelist.get(14);
				Elements tdList = targetTable.getElementsByAttributeValue("height", "20");
				System.out.println("********** Start SD parsing **********");
	
				int counter = 0;
				Element targetTd;
				DATA_COUNTER = ISSUE_PERIOD * 6;
				List<String> datas = new ArrayList<String>();
				while (counter < DATA_COUNTER) {
					targetTd = tdList.get(counter);
					datas.add(targetTd.text());
					counter++;
				}
	
				System.out.println("********** SD DATAS size="+datas.size()+" **********");
				
				String drawNumber = "";
				String drawResult = "";
				for (int i = 0; i < ISSUE_PERIOD; i++) {
					drawNumber = datas.get(0);
					drawResult = "[" + datas.get(1) + "," + datas.get(2) + "," + datas.get(3) + "," + datas.get(4) + ","
							+ datas.get(5) + "]";
	
					//System.out.println("********** ProcessDrawData -> DrawNumber = " + drawNumber + ", DrawResult = " + drawResult + " **********");
					//logger.info("********** ProcessDrawData -> DrawNumber = " + drawNumber + ", DrawResult = " + drawResult + " **********");
	
					processDrawData(drawNumber, drawResult);
					removeProcessedData(datas);
				}
			}
			System.out.println("********** End SD **********");
			error = 1;
		} catch (Exception e) {
			e.printStackTrace();
			if (error <= 3) {
				System.out.println("SD錯誤次數:" + error);
				error++;
				changeIP();
			} else {
				logger.error("Error in drawing " + Market.SD.name() + " data. Error message: " + e.getMessage());
				//sendNotifyMail("Error in drawing " + Market.SD.name() + " data","Error message: " + e.getMessage());
				error = 1;
			}
		} 

	}

	private void processDrawData(String drawNumber, String drawResult) {
		List<Draw> checkResult = drawDAO.selectByDrawNumberAndMarket(Market.SD.name(), drawNumber, GameCode.HL11x5.name());

		if (!checkResult.isEmpty()) {
			//System.out.println("Target DRAW data: " + checkResult.get(0).drawNumberLogInfo());
			Draw draw = checkResult.get(0);
			HashMap<String, String> httpRequestInfo = new HashMap<String, String>();
			
			try {
				httpRequestInfo.put("drawId", "" + draw.getId());
				httpRequestInfo.put("gameCode", GameCode.HL11x5.name());
				httpRequestInfo.put("market", Market.SD.name());
				httpRequestInfo.put("drawNumber", drawNumber);
				httpRequestInfo.put("result", drawResult);

				updateData(socketHttpDestination, httpRequestInfo, logger);

			} catch (Exception e) {
				e.printStackTrace();			
				logger.error("Error in drawing " + Market.SD.name() + " data. Error message: " + e.getMessage());
				//sendNotifyMail("Error in drawing " + Market.SD.name() + " data","Error message: " + e.getMessage());					
			}
		}

	}
	
	private String downloadHtml(String path) {
		System.out.println("********** Start SD downloadHtml **********");
		InputStream is = null;
		try {
			String result = "";
			String line;

			URL url = new URL(path);
			System.out.println("********** Start SD url.openStream() **********");
			is = url.openStream();// throws an IOException
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			while ((line = br.readLine()) != null) {
				result += line;
			}
			System.out.println("********** End SD downloadHtml **********");
			return result;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			sendNotifyMail("Error in downloadHtml " + Market.SD.name() + " data",
					"Error message: " + ioe.getMessage());
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException ioe) {
				// nothing to see here
			}
			System.out.println("********** End SD downloadHtml finally **********");
		}
		return "";
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
