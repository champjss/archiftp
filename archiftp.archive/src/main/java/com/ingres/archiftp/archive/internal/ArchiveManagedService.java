package com.ingres.archiftp.archive.internal;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class ArchiveManagedService implements ManagedService {

	private ArchiveProperties managedProperties;
	private LogServiceWrapper logService;

	@SuppressWarnings("unchecked")
	public void updated(Dictionary properties) throws ConfigurationException {
		if (properties != null) {
			updatedAlias(properties);
			logUpdated();
		}
	}

	@SuppressWarnings("unchecked")
	private void updatedAlias(Dictionary properties) {
		Object property = properties.get("archive-path");
		if (property != null) {
			this.managedProperties.setArchivePath((String)property);
		}
		else {
			this.logService.warning("Configuration archive-path is not set. " +
					"If you not set it, This service cannot work properly.");
		}
	}

	private void logUpdated() {
		this.logService.info(String.format(
				"Configuration updated. archive-path:%s",
				this.managedProperties.getArchivePath()));
	}

	public ArchiveManagedService(ArchiveProperties managedProperties,
			LogServiceWrapper logService) {
		this.managedProperties = managedProperties;
		this.logService = logService;
	}

}
