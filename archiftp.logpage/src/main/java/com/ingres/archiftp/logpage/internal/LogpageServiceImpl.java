package com.ingres.archiftp.logpage.internal;

import com.ingres.archiftp.logpage.LogpageService;

public final class LogpageServiceImpl implements LogpageService {
	
	private LogpageProperties properties;
	private LogServiceWrapper logService;
	private HttpServiceWrapper httpService;
	private LogReaderServiceWrapper logReaderService;
	private LogpageServlet servlet;
	private String aliasOfServlet;
	
	public LogpageServiceImpl(LogpageProperties properties, LogServiceWrapper logService, 
			HttpServiceWrapper httpService, LogReaderServiceWrapper logReaderService) {
		this.properties = properties;
		this.logService = logService;
		this.httpService = httpService;
		this.logReaderService = logReaderService;
	}
	
	public void startLogpage() {
		if (this.properties.isInitialized()) {
			if (this.servlet != null) {
				unregisterLogpage();
			}
			try {
				registerLogpage();
			} catch (Throwable e) {
				e.printStackTrace();
				this.logService.error("Cannot start logpage servlet.", e);
			}
		}
		else {
			throw new RuntimeException("Cannot start logpage : "
					+ "Some configuration(s) maybe incorrect or missing.");
		}
	}
	
	private void registerLogpage() {
		this.servlet = new LogpageServlet(this.logReaderService);
		this.aliasOfServlet = this.properties.getAlias();
		this.httpService.registerServlet(this.aliasOfServlet, this.servlet);
	}
	
	private void unregisterLogpage() {
		this.httpService.unregisterServlet(this.aliasOfServlet);
		this.servlet.destroy();
	}
	
}
