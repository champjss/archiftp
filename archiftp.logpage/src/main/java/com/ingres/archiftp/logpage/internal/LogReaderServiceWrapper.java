package com.ingres.archiftp.logpage.internal;

import java.util.Enumeration;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogReaderService;
import org.osgi.util.tracker.ServiceTracker;

public class LogReaderServiceWrapper {

	private ServiceTracker tracker;
	
	public LogReaderServiceWrapper(ServiceTracker tracker) {
		this.tracker = tracker;
	}
	
	@SuppressWarnings("unchecked")
	public Enumeration<LogEntry> getLogEntries() {
		LogReaderService service = getService();
		if (service != null) {
			return service.getLog();
		}
		else {
			throw new RuntimeException("Cannot read LogEntry : "
					+ "LogReaderService (org.osgi.service.log.LogReaderService) not found.");
		}
	}
	
	private LogReaderService getService() {
		return (LogReaderService)this.tracker.getService();
	}
	
}
