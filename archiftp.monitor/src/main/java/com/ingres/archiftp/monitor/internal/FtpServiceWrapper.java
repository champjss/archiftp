package com.ingres.archiftp.monitor.internal;

import java.io.File;

import org.osgi.util.tracker.ServiceTracker;

import com.ingres.archiftp.ftp.FtpService;

public class FtpServiceWrapper {

	private ServiceTracker tracker;
	
	public void uploadFile(File file) {
		FtpService service = getService();
		if (service != null) {
			service.uploadFile(file);
		}
		else {
			throw new RuntimeException(String.format("Cannot upload file %s : "
					+ "FtpService (com.ingres.archiftp.ftp) not found",
					file.getAbsolutePath()));
		}
	}
	
	private FtpService getService() {
		return (FtpService)this.tracker.getService();
	}
	
	public FtpServiceWrapper(ServiceTracker tracker) {
		this.tracker = tracker;
	}
	
}
