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
			try {
				this.worker.uploadFile(file);
				this.logService.debug(String.format("File '%s' is uploaded.", oldFilePath));
			} catch (Exception e) {
				String message = String.format("Cannot upload file '%s' (an error occurs).", 
						file.getAbsolutePath());
				this.logService.error(message, e);
				throw new RuntimeException(message, e);
			}
		}
		else {
			String message = String.format("Cannot upload file '%s' "
					+ "(some configurations maybe incorrect or missing).", file.getAbsolutePath());
			throw new RuntimeException(message);
		}
	}
	
	public FtpServiceImpl(FtpProperties properties, LogServiceWrapper logService) {
		this.properties = properties;
		this.logService = logService;
		this.worker = new FtpWorker(properties, logService);
	}
	
}
