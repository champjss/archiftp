package com.ingres.archiftp.archive.internal;

import java.io.File;

import com.ingres.archiftp.archive.ArchiveService;

public final class ArchiveServiceImpl implements ArchiveService {

	private ArchiveProperties properties;
	private LogServiceWrapper logService;
	private ArchiveWorker worker;

	public void moveFileToArchive(File file) {
		if (this.properties.isInitialized()) {
			try {
				this.worker.moveFileToArchive(file);
			} catch (RuntimeException e) {
				throw new RuntimeException("Cannot move file %s to archive : An error occurs.", e);
			}
		} else {
			throw new RuntimeException("Cannot move file %s to archive : "
					+ "Some configuration(s) maybe incorrect or missing.");
		}
		
		this.logService.debug(String.format("file %s moved to archive %s", file.getAbsolutePath(), 
				this.properties.getArchivePath()));
	}

	public ArchiveServiceImpl(ArchiveProperties properties,
			LogServiceWrapper logService) {
		this.properties = properties;
		this.logService = logService;
		this.worker = new ArchiveWorker(properties);
	}

}
