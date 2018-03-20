package com.lt.grabbing;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class LottoGrabbingHK extends LottoGrabbingTask {

	private static final Logger logger = LoggerFactory.getLogger(LottoGrabbingHK.class);
	private static boolean flag = true;
	private static String drawNumber = "";
	private String url;
	private static String sortBallUrl_first = "http://www.nfd.com.tw/house/year/";
	private static String sortBallUrl_Second = "http://m.9800.com.tw/6/drop.html";
	private static String sortBallUrl_third = "http://lotto.arclink.com.tw/kj_6.html";
	int error = 1;
	// private String url = "http://bet.hkjc.com/marksix/Results.aspx?lang=ch";

	// public static void main(String[] args) {
	// LottoGrabbingTest6ball task = new LottoGrabbingTest6ball();
	// task.startGrabbing();
	// }

	public void startGrabbing() {
		System.out.println("----------mark6 HK start----------");
		String resultTime = CommonUnits.getNowDateTime();
		Set<String> check = setCheckBall(resultTime);
		if (!check.isEmpty()) {
			String[] sortFirst = setSortBallFirst(check.size());
			String[] sortSecond = setSortBallSecond(check.size());
			String[] sortThird = setSortBallThird(check.size());
			String[] award = new String[check.size()];
			int checkFirst = 0;
			int checkSecond = 0;
			int checkThird = 0;
			for (int i = 0; i <= check.size() - 1; i++) {
				if (!check.contains(sortFirst[i])) {
					checkFirst = 1;
				}
				if (!check.contains(sortSecond[i])) {
					checkSecond = 1;
				}
				if (!check.contains(sortThird[i])) {
					checkThird = 1;
				}
			}

			if (checkFirst + checkSecond + checkThird > 2) {
				drawDAO.insertErrorLog(GameCode.MARK6.name(), Market.HK.name(), resultTime, 2);
				flag = false;
				
			}

			if (flag) {
				if (Arrays.equals(sortFirst, sortSecond)) {
					award = sortFirst;
				} else if (Arrays.equals(sortFirst, sortThird)) {
					award = sortFirst;
				} else if (Arrays.equals(sortSecond, sortThird)) {
					award = sortSecond;
				} else {
					drawDAO.insertErrorLog(GameCode.MARK6.name(), Market.HK.name(), resultTime, 2);
				}
				if(Arrays.equals(award,sortFirst) || Arrays.equals(award,sortSecond) || Arrays.equals(award,sortThird)){
					startMain(resultTime, award);
					System.out.println("----------mark6 HK end----------");
					error = 1;
				}
			}
		}
	}

	public void startMain(String resultTime, String[] award) {
		String[] tmpNewNumber = drawNumber.split("\\/");
		String[] tmpNowTime = resultTime.split("-");
		String newNumber = "";

		List<Draw> list = null;
		HashMap<String, String> httpRequestInfo = null;

		if (tmpNewNumber[0].equals(tmpNowTime[0].substring(2, 4))) {
			newNumber = tmpNowTime[0] + tmpNewNumber[1];
		}

		if (!newNumber.isEmpty()) {
			list = drawDAO.getDrawNum(GameCode.MARK6.name(), Market.HK.name(), newNumber);
			if (!list.isEmpty()) {
				for (Draw dList : list) {
					String mappingNumber = dList.getNumber();
					if (mappingNumber.equals(newNumber) && dList.getResult() == null) {
						String newAward = "[";
						for (String tmpAward : award) {

							if (tmpAward.equals(award[award.length - 1])) {
								newAward = newAward + tmpAward;
							} else {
								newAward = newAward + tmpAward + ",";
							}
						}
						newAward = newAward + "]";

						httpRequestInfo = new HashMap<String, String>();
						httpRequestInfo.put("drawId", "" + dList.getId());
						httpRequestInfo.put("gameCode", GameCode.MARK6.name());
						httpRequestInfo.put("market", Market.HK.name());
						httpRequestInfo.put("drawNumber", mappingNumber);
						httpRequestInfo.put("drawResultTime", resultTime);
						httpRequestInfo.put("result", newAward);

						updateData(socketHttpDestination, httpRequestInfo, logger);

					}
				}
			}
		}
	}

	public Set<String> setCheckBall(String resultTime) {
		Set<String> check = new HashSet<String>();
		try {
			Document xmlDoc = Jsoup.connect(url).timeout(10000).get();
			Element results = xmlDoc
					.select("#_ctl0_ContentPlaceHolder1_resultsMarkSix_markSixResultTable > tbody > tr ").get(1);
			Elements result = results.select("table > tbody> tr > td");
			drawNumber = results.select("td").get(1).select("a").html().replace("&nbsp;", "");

			for (int i = 0; i <= result.size() - 1; i++) {

				String[] award = result.get(i).select("img").attr("src").split("_");
				if (!award[1].equals("special")) {
					// System.out.println(award[1]);
					check.add(award[1]);
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			if (error <= 3) {
				System.out.println("HK錯誤次數:" + error);
				error++;
				changeIP();
			} else {
				logger.error("Error in drawing " + Market.HK.name() + " data. Error message: " + e.getMessage());
				drawDAO.insertErrorLog(GameCode.MARK6.name(), Market.HK.name(), resultTime, 1);
				error = 1;
			}
		}
		return check;
	}
	
	
	public String[] setSortBallFirst(int size) {
		String[] award = new String[size];
		Calendar calendar = Calendar.getInstance();
		String year = Integer.toString(calendar.get(Calendar.YEAR));
		try {
			String tmpSortBallUrl_first = sortBallUrl_first + "F" + year + ".htm";
			Document xmlDoc = Jsoup.connect(tmpSortBallUrl_first).timeout(10000).get();
			Elements newLists = xmlDoc.select("table > tbody > tr").last().select("td");
			int i = 0;
			for (int j = 2; j <= newLists.size() - 1; j++) {
				award[i] = newLists.get(j).text();
				i++;
			}

			// System.out.println("---------------first--------------");
			// for(String a: award){
			// System.out.println(a);
			// }
			// System.out.println("----------------------------------");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
			
		}
		return award;
	}

	public String[] setSortBallSecond(int size) {
		String[] award = new String[size];
		try {

			Document xmlDoc = Jsoup.connect(sortBallUrl_Second).timeout(10000).get();
			Elements newLists = xmlDoc.select("table").get(1).select("tr").last().select("td").get(2).select("font");

			String[] tmpAward = newLists.get(0).html().split("&nbsp;");

			for (int k = 0; k <= tmpAward.length - 1; k++) {
				award[k] = tmpAward[k];
			}
			award[size - 1] = newLists.get(1).text();

			// System.out.println("---------------second--------------");
			// for(String b: award){
			// System.out.println(b);
			// }
			// System.out.println("-----------------------------------");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
		}
		return award;
	}

	public String[] setSortBallThird(int size) {
		String[] award = new String[size];
		try {
			Document xmlDoc = Jsoup.connect(sortBallUrl_third).timeout(10000).get();

			Elements newLists = xmlDoc.select("table").get(5).select("tr").get(2).select("td");

			String[] tmpAward = newLists.get(2).text().replace("+ ", "").split("\\s");

			for (int i = 0; i <= tmpAward.length - 1; i++) {
				award[i] = tmpAward[i];
			}

			// System.out.println("---------------third--------------");
			// for (String c : award) {
			// System.out.println(c);
			// }
			// System.out.println("----------------------------------");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
		}
		return award;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
