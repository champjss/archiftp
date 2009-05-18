package com.ingres.archiftp.monitor.internal;

import java.lang.Thread.State;

import com.ingres.archiftp.logger.Logger;
import com.ingres.archiftp.monitor.MonitorService;

public final class MonitorServiceImpl implements MonitorService {

	private MonitorProperties properties;
	private Logger logger;
	private FtpServiceWrapper ftpService;
	private ArchiveServiceWrapper archiveService;
	private MonitorThread monitorThread;
	
	public void startMonitor() {
		if (!this.properties.isInitialized()) {
			String message = "Cannot start monitoring "
				+ "(some configurations maybe incorrect or missing).";
			this.logger.error(message);
			System.out.println(message);
			return;
		}
		
		if (!isMonitoring()) {
			this.monitorThread = new MonitorThread(this.properties, this.logger, 
					this.ftpService, this.archiveService);
			this.monitorThread.start();
			this.logger.info("Monitoring is started.");
		}
		else {
			this.logger.info("Monitoring is already started.");
		}
	}

	public void stopMonitor() {
		if (isMonitoring()) {
			this.logger.info("Preparing to stop monitoring.");
			this.monitorThread.interrupt();
			while (this.monitorThread.getState() != State.TERMINATED) {
				delayForStop();
			}
			this.monitorThread = null;
			this.logger.info("Monitoring is Stopped.");
		}
		else {
			this.logger.info("Monitoring is already stopped.");
		}
	}
	
	public boolean isMonitoring() {
		return this.monitorThread != null;
	}
	
	private void delayForStop() {
		try {
			this.logger.info("Waiting for stop monitoring.");
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	}
	
	public MonitorServiceImpl(MonitorProperties properties, Logger logger, 
			FtpServiceWrapper ftpService, ArchiveServiceWrapper archiveService) {
		this.properties = properties;
		this.logger = logger;
		this.ftpService = ftpService;
		this.archiveService = archiveService;
	}
	
}
