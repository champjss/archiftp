package com.ingres.archiftp.ftp.internal;

import java.io.File;

import com.ingres.archiftp.ftp.FtpService;

public final class FtpServiceImpl implements FtpService {

	private FtpProperties properties;
	private LogServiceWrapper logService;
	private FtpWorker worker;
	
	public void uploadFile(File file) {
		String oldFilePath = file.getAbsolutePath();
		
		if (this.properties.isInitialized()) {
			this.worker.uploadFile(file);
			this.logService.debug(String.format("file %s uploaded to %s at directory %s.", 
					oldFilePath, this.properties.getHostname(), this.properties.getDirectory()));
		}
		else {
			throw new RuntimeException("Cannot upload file %s : "
					+ "Some configuration(s) maybe incorrect or missing.");
		}
	}
	
	public FtpServiceImpl(FtpProperties properties, LogServiceWrapper logService) {
		this.properties = properties;
		this.logService = logService;
		this.worker = new FtpWorker(properties);
	}
	
}
