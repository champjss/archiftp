package com.ingres.archiftp.monitor.internal;

import java.io.File;

public class MonitorThread extends Thread {
	
	private MonitorProperties properties;
	private LogServiceWrapper logService;
	private FtpServiceWrapper ftpService;
	private ArchiveServiceWrapper archiveService;
	
	public synchronized void run() {
		File currentMonitorDirectory;
		long currentMonitorInterval;
		
		while(!this.isInterrupted()) {
			this.logService.debug(String.format("Now monitor for %s", 
					this.properties.getMonitorPath()));
			currentMonitorDirectory = new File(this.properties.getMonitorPath());
			currentMonitorInterval = this.properties.getMonitorInterval();
			
			try {
				File[] monitoredFiles = currentMonitorDirectory.listFiles();
				FtpAndArchivedFiles(monitoredFiles);
			} catch (Throwable e) {
				e.printStackTrace();
				this.logService.error("Unfortunately error.", e);
				this.logService.info("Monitoring will be stopped because some errors occur.");
				return;
			}
			
			try {
				Thread.sleep(currentMonitorInterval);
			} catch (InterruptedException e) {
				// Someone interrupt me, time to end this thread now.
				break;
			}
		}
	}
	
	private void FtpAndArchivedFiles(File[] files) {
		try {
			for (File file : files) {
				FtpAndArchivedFile(file);
			}
		} catch (Throwable e) {
			throw new RuntimeException("Cannot upload file(s)", e);
		}
	}
	
	private void FtpAndArchivedFile(File file) {
		try {
			this.ftpService.uploadFile(file);
			this.archiveService.moveFileToArchive(file);
		} catch (Throwable e) {
			throw new RuntimeException(String.format("Cannot upload file %s : ",
					file.getAbsolutePath()), e);
		}
	}
	
	public MonitorThread(MonitorProperties properties, LogServiceWrapper logService, 
			FtpServiceWrapper ftpService, ArchiveServiceWrapper archiveService) {
		this.properties = properties;
		this.logService = logService;
		this.ftpService = ftpService;
		this.archiveService = archiveService;
	}
}
