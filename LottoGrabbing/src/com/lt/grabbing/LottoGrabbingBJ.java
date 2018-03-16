package com.lt.grabbing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ct.lk.domain.Draw;
import com.lt.util.GameCode;
import com.lt.util.LottoBJUtils;
import com.lt.util.Market;
import com.lt.util.UseIPInfo;
import com.lt.util.CommonUnits;

public class LottoGrabbingBJ extends LottoGrabbingTask {

	private static final Logger logger = LoggerFactory.getLogger(LottoGrabbingBJ.class);
	private String url; // = "http://www.bwlc.net/bulletin/trax.html?page=";
	private int page = 1;
	private String ipUrl = "http://www.xdaili.cn/ipagent//freeip/getFreeIps?page=1&rows=10";
	private String checkipUrl = "http://www.xdaili.cn/ipagent//checkIp/ipList?";
	private String subCheckipUrl = "http://cn-proxy.com";
	private static boolean flag = true;
	int error = 1;

	// public static void main(String[] args) {
	// LottoGrabbingBJ task = new LottoGrabbingBJ();
	// task.startGrabbing();
	// }

	public void startGrabbing() {

		System.out.println("----------pk10 BJ start----------");
		String resultTime = CommonUnits.getNowDateTime();
		List<UseIPInfo> useIPList = new ArrayList<UseIPInfo>();
		useIPList = checkCNIP(resultTime);
		if (useIPList.isEmpty() || useIPList.size() < 3) {
			useIPList = subCheckCNIP(useIPList,resultTime);
		}	
		startMain(useIPList,resultTime);
		System.out.println("----------pk10 BJ end----------");

	}

	public void startMain(List<UseIPInfo> useIPList,String resultTime) {
		try {
			if (!useIPList.isEmpty()) {

				UseIPInfo porxyIp = new UseIPInfo();
				porxyIp = changeIP(useIPList, error);
				int port = Integer.parseInt(porxyIp.getPort());			
				String pageUrl = url + page;
				Document xmlDoc = Jsoup.connect(pageUrl).proxy(porxyIp.getIp(),port).timeout(5000).post();
				Elements newlist = LottoBJUtils.getNowNumber(xmlDoc);
				

				String newNumber = newlist.get(0).text();
				List<Draw> getStartNB = drawDAO.getStartNumber(GameCode.PK10.name(), Market.BJ.name(), newNumber);
				String startNumber = getStartNB.get(0).getNumber();
				List<Draw> list = null;
				List<Draw> drawlist = null;
				list = drawDAO.getDrawNum(GameCode.PK10.name(), Market.BJ.name(), newNumber);
				drawlist = drawDAO.getDrawNumList(GameCode.PK10.name(), Market.BJ.name(), startNumber, newNumber);
				HashMap<String, String> awardMap = null;
				HashMap<String, String> httpRequestInfo = null;
				String newAward = null;

				if (list.isEmpty() && !drawlist.isEmpty()) {
					String lastNumber = drawlist.get(drawlist.size() - 1).getNumber();
					awardMap = supplyNumber(xmlDoc, url, page, lastNumber);
					list = drawlist;
				}

				if (!list.isEmpty()) {
					for (Draw dList : list) {
						String mappingNumber = dList.getNumber();
						if (awardMap != null) {
							newAward = awardMap.get(mappingNumber);
							if (newAward != null) {
								drawDAO.updateDrawResult(GameCode.PK10.name(), Market.BJ.name(), mappingNumber,
										newAward);
								flag = true;
							}
						} else {
							if (mappingNumber.equals(newNumber) && dList.getResult() == null) {
								newAward = "[" + newlist.get(1).text() + "]";

								httpRequestInfo = new HashMap<String, String>();
								httpRequestInfo.put("drawId", "" + dList.getId());
								httpRequestInfo.put("gameCode", GameCode.PK10.name());
								httpRequestInfo.put("market", Market.BJ.name());
								httpRequestInfo.put("drawNumber", mappingNumber);
								httpRequestInfo.put("drawResultTime", resultTime);
								httpRequestInfo.put("result", newAward);

								updateData(socketHttpDestination, httpRequestInfo, logger);
							}
						}
					}
				}

			} else {
				System.out.println("目前無ip可以使用orIP回應速度過慢");
			}
//			System.getProperties().remove("http.proxyHost");
//			System.getProperties().remove("http.proxyPort");
			error = 1;
		} catch (Exception e) {
			e.printStackTrace();
			if (error <= 3) {
				System.out.println("BJ錯誤次數:" + error);
				error++;
				startMain(useIPList,resultTime);
			} else {
				logger.error("Error in drawing " + Market.BJ.name() + " data. Error message: " + e.getMessage());
				drawDAO.insertErrorLog(GameCode.PK10.name(), Market.BJ.name(), resultTime, 1);
				error = 1;
			}
		}
	}

	public UseIPInfo changeIP(List<UseIPInfo> useIPList, int error) {

		int errorCount = error - 1;

		if (errorCount > useIPList.size() - 1) {
			errorCount = useIPList.size() - 1;
		}
		String ip = useIPList.get(errorCount).getIp();
		String port = useIPList.get(errorCount).getPort();
		UseIPInfo porxyIp = new UseIPInfo();
		
		System.out.println("ip:" + ip + "|port:" + port);

		porxyIp.setIp(ip);
		porxyIp.setPort(port);
//		System.getProperties().setProperty("proxySet", "true");
//		System.getProperties().setProperty("http.proxyHost", ip);
//		System.getProperties().setProperty("http.proxyPort", port);
		return porxyIp;
	}

	public List<UseIPInfo> checkCNIP(String resultTime) {

		List<UseIPInfo> ipList = new ArrayList<UseIPInfo>();
		String checkIPUrl = checkipUrl;

		try {		
			Document doc = Jsoup.connect(ipUrl).ignoreContentType(true).timeout(5000).get();
			String json = doc.select("body").text();
			String checkIPJson = LottoBJUtils.splitJson(json);

			JSONArray jsonArray = new JSONArray(checkIPJson);

			for (int i = 0; i <= jsonArray.length() - 1; i++) {
				JSONObject tmpJson = jsonArray.getJSONObject(i);
				String ip = tmpJson.get("ip").toString();
				String port = tmpJson.get("port").toString();
				checkIPUrl = checkIPUrl + "ip_ports%5B%5D=" + ip + "%3A" + port + "&";
			}

			Document ckipDoc = Jsoup.connect(checkIPUrl).ignoreContentType(true).get();
			String ckipJson = ckipDoc.select("body").text();
			String ipJson = LottoBJUtils.splitJson(ckipJson);

			JSONArray jsonIPArray = new JSONArray(ipJson);

			for (int j = 0; j <= jsonIPArray.length() - 1; j++) {
				JSONObject tmpIPJson = jsonIPArray.getJSONObject(j);
				String tmpTime = tmpIPJson.optString("time");

				if (!tmpTime.isEmpty()) {
					int time = LottoBJUtils.formatInt(tmpTime);

					if (time <= 3000) {
						UseIPInfo useIPInfo = new UseIPInfo();
						useIPInfo.setIp(tmpIPJson.get("ip").toString());
						useIPInfo.setPort(tmpIPJson.get("port").toString());
						useIPInfo.setTime(time);
						ipList.add(useIPInfo);					
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ipList;
	}

	public List<UseIPInfo> subCheckCNIP(List<UseIPInfo> useIPList,String resultTime) {

		List<UseIPInfo> ipList = useIPList;
		
		try {
			
			Document doc = Jsoup.connect(subCheckipUrl).timeout(5000).post();
			Elements allIP = doc.select(".sortable").select("tbody").select("tr");
			for (Element checkIP : allIP) {
				String checkPort = checkIP.select("td").get(1).text();
				if (checkPort.equals("80")) {
					String[] speed = checkIP.select(".bar").attr("style").split("\\s|;|%");
					int resSpeed = Integer.parseInt(speed[1]);
					if (resSpeed >= 75) {
						UseIPInfo useIPInfo = new UseIPInfo();
						useIPInfo.setIp(checkIP.select("tr").select("td").get(0).text());
						useIPInfo.setPort("80");
						ipList.add(useIPInfo);
						if (ipList.size() > 4) {
							return ipList;
						}
					}
				}
			}

			if(ipList.isEmpty()){
				drawDAO.insertErrorLog(GameCode.PK10.name(), Market.BJ.name(), resultTime, 4);
			}
		} catch (Exception e) {
			e.printStackTrace();
			drawDAO.insertErrorLog(GameCode.PK10.name(), Market.BJ.name(), resultTime, 3);
		}
		return ipList;
		
	}

	public HashMap<String, String> supplyNumber(Document xmlDoc, String url, int page, String lastNumber)
			throws Exception {

		HashMap<String, String> awardMap = new HashMap<String, String>();

		while (flag) {
			HashMap<String, String> tmpAwardList = LottoBJUtils.Crawl(xmlDoc, lastNumber);
			awardMap.putAll(tmpAwardList);

			if (awardMap.get(lastNumber) != null || tmpAwardList.size() == 0) {
				flag = false;
			} else {
				page++;
				String pUrl = url + page;
				xmlDoc = Jsoup.connect(pUrl).timeout(5000).post();
			}
		}
		return awardMap;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
