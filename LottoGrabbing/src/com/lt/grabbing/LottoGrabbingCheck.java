package com.lt.grabbing;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.ct.lk.domain.Draw;
import com.lt.util.Market;

public class LottoGrabbingCheck extends LottoGrabbingTask{
	
	public void startChecking() {
		System.out.println("********** Start Lotto Grabbing Checking **********");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(new Date());
		String resultStr = "";
		
		for (Market market : Market.values()) {			
			List<Draw> result = drawDAO.getCountOfStatusL(market.getMarketName(), market.getGameCode(), dateStr);
			resultStr += "Draw_date: " + dateStr + ", Market: " + market.getMarketName() + 
					", GameCode: " + market.getGameCode() + ", count: " + result.size() + "\n";
			
			if (result.size() >= 3) {
				sendNotifyMail("iLotto [" + market.getMarketName() + "]" + " Market exception Please check", "[" + market.getMarketName() + "]" + "超過三期DATA未更新，請從下方連結檢查網站/期號是否正常。\nhttp://grabbermt.staging.qqcp518.net/market.jsp");
			}
		}
		resultStr += "Last updated time: " + Calendar.getInstance().getTime().toString();
		System.out.println(resultStr);
		try {
            File file = new File("/usr/local/applications/lt-grabbing-server/lotto-check-result.txt");
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(resultStr);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	
		System.out.println("********** End Lotto Grabbing Checking **********");
	}

}
