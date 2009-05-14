package com.ingres.archiftp.monitor.internal;

import java.io.File;

import org.osgi.util.tracker.ServiceTracker;

import com.ingres.archiftp.archive.ArchiveService;

public class ArchiveServiceWrapper {

	private ServiceTracker tracker;

	public void moveFileToArchive(File file) {
		ArchiveService service = getService();
		if (service != null) {
			service.moveFileToArchive(file);
		} else {
			throw new RuntimeException(String.format("Cannot move file %s : "
					+ "ArchiveService (com.ingres.archiftp.archive) not found",
					file.getAbsolutePath()));
		}
	}

	private ArchiveService getService() {
		return (ArchiveService) this.tracker.getService();
	}

	public ArchiveServiceWrapper(ServiceTracker tracker) {
		this.tracker = tracker;
	}

}
