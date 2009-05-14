package com.ingres.archiftp.ftp.internal;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class LogServiceWrapper {

	private ServiceTracker tracker;
	private ServiceReference referece;
	
	public LogServiceWrapper(ServiceReference reference, ServiceTracker tracker) {
		this.referece = reference;
		this.tracker = tracker;
	}
	
	public void error(String message) {
		log(LogService.LOG_ERROR, message);
	}
	
	public void error(String message, Throwable e) {
		log(LogService.LOG_ERROR, message, e);
	}
	
	public void warning(String message) {
		log(LogService.LOG_WARNING, message);	
	}
	
	public void warning(String message, Throwable e) {
		log(LogService.LOG_WARNING, message, e);
	}
	
	public void info(String message) {
		log(LogService.LOG_INFO, message);
	}
	
	public void info(String message, Throwable e) {
		log(LogService.LOG_INFO, message, e);
	}
	
	public void debug(String message) {
		log(LogService.LOG_DEBUG, message);
	}
	
	public void debug(String message, Throwable e) {
		log(LogService.LOG_DEBUG, message, e);
	}
	
	private void log(int logLevel, String message) {
		LogService service = getService();
		if (service != null) {
			service.log(referece, logLevel, message);
		}
		else {
			System.out.println(String.format("[%s] %s", 
					getStringOfLogLevel(logLevel), message));
		}
	}
	
	private void log(int logLevel, String message, Throwable e) {
		LogService service = getService();
		if (service != null) {
			service.log(referece, logLevel, message, e);
		}
		else {
			System.out.println(String.format("[%s] %s (Exception: %s / Message: %s)", 
					getStringOfLogLevel(logLevel), message, e.getClass().getName(), e.getMessage()));
		}
	}
	
	private String getStringOfLogLevel(int logLevel) {
		String result;
		switch (logLevel){
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
	
	private LogService getService() {
		return (LogService)this.tracker.getService();
	}
	
}
