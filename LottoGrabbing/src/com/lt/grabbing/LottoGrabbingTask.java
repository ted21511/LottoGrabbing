package com.lt.grabbing;

import java.util.HashMap;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.framework.support.spring.SpringObjectFactory;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.lt.dao.DrawDAO;
import com.lt.util.EmailNotificated;
import com.lt.util.SmtpInfo;
import com.lt.ssl.TLSSocketConnectionFactory;

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
			
			String[] config= { "classpath:spring/applicationContext*.xml"};
			System.out.println("server initializing... xml loading...");
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
			context.setConfigLocations(config);
			context.afterPropertiesSet();
			context.registerShutdownHook();
			SpringObjectFactory.setAppContext(context);
			context.start();
			System.out.println("server initialized successfully");

		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public void startGrabbing() {
		System.out.println("********** Start Lotto Grabbing... **********");
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
		HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketConnectionFactory());
	}

	protected void updateData(String socketHttpDestination, HashMap<String, String> httpRequestInfo, Logger logger) {

		Connection con = Jsoup.connect(socketHttpDestination).userAgent(
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");

		Document response = null;

		try {

			con.data(httpRequestInfo);
			con.timeout(10000);
			response = con.post();
			drawDAO.insertLog(httpRequestInfo, 0);
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
			System.out.println(e.toString());
			logger.error("Error in posting data. Error message: " + e.getMessage());
			drawDAO.insertLog(httpRequestInfo, 2);
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

	static HostnameVerifier hv = new HostnameVerifier() {
		public boolean verify(String urlHostName, SSLSession session) {
			System.out.println("Warning: URL Host: " + urlHostName + " vs. " + session.getPeerHost());
			return true;
		}
	};

	static class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
			return true;
		}

		public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
			return true;
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}
	}
}
