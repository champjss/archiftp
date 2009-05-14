package com.ingres.archiftp.logpage.internal;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class LogpageManagedService implements ManagedService {

	private LogpageProperties managedProperties;
	private LogServiceWrapper logService;
	private LogpageServiceImpl logpageService;
	
	@SuppressWarnings("unchecked")
	public void updated(Dictionary properties) throws ConfigurationException {
		if (properties != null) {
			updatedAlias(properties);
			logUpdated();
			restartServiceForNewProperties();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void updatedAlias(Dictionary properties) {
		Object property = properties.get("logpage-url");
		if (property != null) {
			this.managedProperties.setAlias((String)property);		
		}
		else {
			this.logService.warning("Configuration logpage-url is not set. " +
					"If you not set it, This service cannot work properly.");
		}
	}
	
	private void logUpdated() {
		this.logService.info(String.format(
				"Configuration updated. logpage-url:%s",
				this.managedProperties.getAlias()));
	}
	
	private void restartServiceForNewProperties() {
		this.logpageService.startLogpage();
	}

	public LogpageManagedService(LogpageProperties managedProperties, 
			LogServiceWrapper logService, LogpageServiceImpl logpageService) {
		this.managedProperties = managedProperties;
		this.logService = logService;
		this.logpageService = logpageService;
	}
	
}
