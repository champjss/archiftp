package com.ingres.archiftp.ftp.internal;

import java.io.File;

import com.ingres.archiftp.ftp.FtpService;
import com.ingres.archiftp.logger.Logger;

public final class FtpServiceImpl implements FtpService {

	private FtpProperties properties;
	private Logger logger;
	private FtpWorker worker;
	
	public void uploadFile(File file) {
		String oldFilePath = file.getAbsolutePath();
		
		if (this.properties.isInitialized()) {
			try {
				this.worker.uploadFile(file);
				this.logger.debug(String.format("File '%s' is uploaded.", oldFilePath));
			} catch (Exception e) {
				String message = String.format("Cannot upload file '%s' (an error occurs).", 
						file.getAbsolutePath());
				this.logger.error(message, e);
				throw new RuntimeException(message, e);
			}
		}
		else {
			String message = String.format("Cannot upload file '%s' "
					+ "(some configurations maybe incorrect or missing).", file.getAbsolutePath());
			throw new RuntimeException(message);
		}
	}
	
	public FtpServiceImpl(FtpProperties properties, Logger logger) {
		this.properties = properties;
		this.logger = logger;
		this.worker = new FtpWorker(properties, logger);
	}
	
}
