package com.lt.grabbing;

import java.util.HashMap;
import java.util.Properties;

import org.framework.support.spring.SpringObjectFactory;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.lt.dao.DrawDAO;
import com.lt.util.EmailNotificated;
import com.lt.util.SmtpInfo;

public class LottoGrabbingTask extends Thread {

	public static SmtpInfo smtpEmailProperties;
	public static Properties marketUrlproperties;
	public static String socketHttpDestination;
	public static DrawDAO drawDAO;
	public static String proxyHost = "proxy.hinet.net";
			
	public static void main(String[] args) {
		 LottoGrabbingTask task = new LottoGrabbingTask();
		 task.loadConfiguration();
	}
	
	public void loadConfiguration() {
		try {
			
			String[] config= { "classpath:spring/applicationContext*.xml","classpath:spring/applicationContext*.xml" };
			System.out.println("server initializing... xml loading...");
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
			context.setConfigLocations(config);
			context.afterPropertiesSet();
			context.registerShutdownHook();
			SpringObjectFactory.setAppContext(context);
			context.start();
			System.out.println("server initialized successfully");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startGrabbing() {
		System.out.println("********** Start Lotto Grabbing... **********");
	}

	protected void updateData(String socketHttpDestination, HashMap<String, String> httpRequestInfo, Logger logger) {

		Connection con = Jsoup.connect(socketHttpDestination).userAgent(
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");

		Document response = null;

		try {

			con.data(httpRequestInfo);
			con.timeout(10000);
			response = con.post();
			String rd = response.select("body").text();

			String line = "";
			if ((line = rd) != null) {
				System.out.println("********** Market=" + httpRequestInfo.get("market") + ", Game_Code="
						+ httpRequestInfo.get("gameCode") + ", Draw_Number=" + httpRequestInfo.get("drawNumber")
						+ ", Result=" + httpRequestInfo.get("result") + ", Response Code: " + line + "**********");
				logger.info("Return code after update [draw]: {}", line);
			} else {
				logger.info("Waited for response, time out, so forced consuming response");
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in posting data. Error message: " + e.getMessage());
			// sendNotifyMail("Error in posting data", "Error message: " +
			// e.getMessage());

		}

	}

	public void changeIP() {

		System.getProperties().setProperty("proxySet", "true");
		System.getProperties().setProperty("http.proxyHost", proxyHost);
		System.getProperties().setProperty("http.proxyPort", "80");
		startGrabbing();
	}

	public void sendNotifyMail(String subject, String content) {
		new EmailNotificated().emailNotify(smtpEmailProperties, subject, content);
	}

	@SuppressWarnings("static-access")
	public void setSmtpEmailProperties(SmtpInfo smtpEmailProperties) {
		this.smtpEmailProperties = smtpEmailProperties;
	}

	@SuppressWarnings("static-access")
	public void setSocketHttpDestination(String socketHttpDestination) {
		this.socketHttpDestination = socketHttpDestination;
	}

	@SuppressWarnings("static-access")
	public void setDrawDAO(DrawDAO drawDAO) {
		this.drawDAO = drawDAO;
	}

}
