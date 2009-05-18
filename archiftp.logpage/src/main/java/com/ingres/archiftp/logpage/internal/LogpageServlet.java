package com.ingres.archiftp.logpage.internal;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;

public class LogpageServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private LogReaderServiceWrapper logReaderService;
	private DateFormat dateFormat;

	public void doGet(HttpServletRequest req, HttpServletResponse rsp) {
		try {
			renderLogpage(rsp);
		}
		catch (Exception e) {
			try {
				renderErrorReportPage(rsp, e);
				e.printStackTrace();
			} catch (IOException e1) {
			}
		}
	}

	private void renderLogpage(HttpServletResponse rsp) throws IOException, Exception {
		Enumeration<LogEntry> logEntrys = logReaderService.getLogEntries();
		renderHeader(rsp);
		renderLogs(rsp, logEntrys);
		renderFooter(rsp);
	}

	private void renderLogs(HttpServletResponse rsp,
			Enumeration<LogEntry> logEntrys) throws IOException {
		renderHtml(rsp, "<h1>Archiftp Logpage</h1>");
		while (logEntrys.hasMoreElements()) {
			LogEntry entry = logEntrys.nextElement();
			renderLog(rsp, entry);
		}
	}

	private void renderLog(HttpServletResponse rsp, LogEntry entry) throws IOException {
		String style = getStyleOfLogLevel(entry);
		String dateTime = getStringOfDateTime(entry);
		String level = getStringOfLogLevel(entry);
		String reference = getStringOfServiceReference(entry.getServiceReference());
		String message = entry.getMessage();
		
		if (entry.getException() != null) {
			String exception = entry.getException().toString();
			renderHtml(rsp, String.format(
					"<li><span style=\"%s\">[%s] [%s] [%s] %s (Exception: %s)</span></li>\n", 
					style, dateTime, level, reference, message, exception));
		}
		else {
			renderHtml(rsp, String.format(
					"<li><span style=\"%s\">[%s] [%s] [%s] %s</span></li>", 
					style, dateTime, level, reference, message));
		}
	}

	private String getStringOfDateTime(LogEntry entry) {
		return this.dateFormat.format(new Date(entry.getTime())); 
	}
	
	private String getStringOfLogLevel(LogEntry entry) {
		String result;
		switch (entry.getLevel()){
			case LogService.LOG_ERROR:
				result = "ERROR";
				break;
			case LogService.LOG_WARNING:
				result = "WARN";
				break;
			case LogService.LOG_INFO:
				result = "INFO";
				break;
			case LogService.LOG_DEBUG:
				result = "DEBUG";
				break;
			default:
				result = "N/A";
				break;
		}
		
		return result;
	}
	
	private String getStyleOfLogLevel(LogEntry entry) {
		String result;
		switch (entry.getLevel()){
			case LogService.LOG_ERROR:
				result = "color:red;";
				break;
			case LogService.LOG_WARNING:
				result = "color:orange;";
				break;
			case LogService.LOG_INFO:
				result = ";";
				break;
			case LogService.LOG_DEBUG:
				result = "color:grey;";
				break;
			default:
				result = ";";
				break;
		}
		
		return result;
	}
	
	private String getStringOfServiceReference(ServiceReference reference) {
		String referenceString = "N/A";
		if (reference != null) {
			Bundle referenceBundle = reference.getBundle();
			if (referenceBundle != null) {
				referenceString = referenceBundle.getSymbolicName();
			}
		}
		
		return referenceString;
	}
	
	private void renderErrorReportPage(HttpServletResponse rsp, Exception e)
			throws IOException {
		renderHeader(rsp);
		renderHtml(rsp, String.format("<p>ERROR: Can't show logpage. (%s: %s)</p>", 
				e.getClass().getName(), e.getMessage()));
		renderFooter(rsp);
	}

	private void renderHeader(HttpServletResponse rsp) throws IOException {
		rsp.setContentType("text/html");
		renderHtml(rsp, "<html>");
		renderHtml(rsp, "<head>");
		renderHtml(rsp, "<title>Archiftp Logpage</title>");
		renderHtml(rsp, "</head>");
		renderHtml(rsp, "<body>");
	}

	private void renderFooter(HttpServletResponse rsp) throws IOException {
		renderHtml(rsp, "</body>");
		renderHtml(rsp, "</html>");
	}

	private void renderHtml(HttpServletResponse rsp, String html)
			throws IOException {
		rsp.getWriter().println(html);
	}
	
	public LogpageServlet(LogReaderServiceWrapper logReaderService) {
		this.logReaderService = logReaderService;
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss:SSS");
	}
	
}
