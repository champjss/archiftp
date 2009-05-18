package com.ingres.archiftp.archive.internal;

import java.io.File;

import com.ingres.archiftp.archive.ArchiveService;
import com.ingres.archiftp.logger.Logger;

public final class ArchiveServiceImpl implements ArchiveService {

	private ArchiveProperties properties;
	private Logger logger;
	private ArchiveWorker worker;

	public void moveFileToArchive(File file) {
		if (this.properties.isInitialized()) {
			try {
				this.worker.moveFileToArchive(file);
			} catch (RuntimeException e) {
				String message = String.format("Cannot move file %s to archive (an error occurs).", 
						file.getAbsolutePath());
				throw new RuntimeException(message, e);
			}
		} else {
			String message = String.format("Cannot move file %s to archive " +
					"(some configurations maybe incorrect or missing).", 
					file.getAbsolutePath());
			throw new RuntimeException(message);
		}
		
		String message = String.format("File '%s' is moved to archive.", file.getAbsolutePath(), 
				this.properties.getArchivePath());
		this.logger.debug(message);
	}

	public ArchiveServiceImpl(ArchiveProperties properties,
			Logger logger) {
		this.properties = properties;
		this.logger = logger;
		this.worker = new ArchiveWorker(properties, logger);
	}

}
