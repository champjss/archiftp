package com.ingres.archiftp.logpage.internal;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import com.ingres.archiftp.logger.Logger;

public class LogpageManagedService implements ManagedService {

	private LogpageProperties managedProperties;
	private Logger logger;
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
			this.logger.warning("Configuration logpage-url is not set. " +
					"If you not set it, This service cannot work properly.");
		}
	}
	
	private void logUpdated() {
		this.logger.info(String.format(
				"Configuration updated. logpage-url:%s",
				this.managedProperties.getAlias()));
	}
	
	private void restartServiceForNewProperties() {
		this.logpageService.startLogpage();
	}

	public LogpageManagedService(LogpageProperties managedProperties, 
			Logger logger, LogpageServiceImpl logpageService) {
		this.managedProperties = managedProperties;
		this.logger = logger;
		this.logpageService = logpageService;
	}
	
}
