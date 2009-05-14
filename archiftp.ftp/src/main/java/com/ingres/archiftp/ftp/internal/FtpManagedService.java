package com.ingres.archiftp.ftp.internal;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class FtpManagedService implements ManagedService {

	private FtpProperties managedProperties;
	private LogServiceWrapper logService;

	@SuppressWarnings("unchecked")
	public void updated(Dictionary properties) throws ConfigurationException {
		if (properties != null) {
			updatedHostname(properties);
			try {
				updatedPort(properties);
			} catch (Throwable e) {
				throw new ConfigurationException("port", 
						"port must be an integer between 1-65535.", e);
			}
			updatedUsername(properties);
			updatedPassword(properties);
			updatedDirectory(properties);
			logUpdated();
		}
	}

	@SuppressWarnings("unchecked")
	private void updatedHostname(Dictionary properties) {
		Object property = properties.get("hostname");
		if (property != null) {
			this.managedProperties.setHostname((String) property);
		} else {
			this.logService.warning("Configuration hostname is not set. "
					+ "If you not set it, This service cannot work properly.");
		}
	}

	@SuppressWarnings("unchecked")
	private void updatedPort(Dictionary properties) {
		Object property = properties.get("port");
		if (property != null) {
			try {
				int port = (Integer)property;
				if (port < 1 || port > 65535) {
					throw new RuntimeException("port is invalid. " + 
							"It must be an integer between 1-65535.");
				}
				this.managedProperties.setPort(port);
			} catch (Throwable e) {
				throw new RuntimeException("port is invalid. " + 
				"It must be an integer between 1-65535.", e);
			}
		} else {
			this.logService.warning("Configuration hostname is not set. "
					+ "Using default value: 21");
		}
	}

	@SuppressWarnings("unchecked")
	private void updatedUsername(Dictionary properties) {
		Object property = properties.get("username");
		if (property != null) {
			this.managedProperties.setUsername((String) property);
		} else {
			this.logService.warning("Configuration hostname is not set. "
					+ "Using default value: 'anonymous'");
		}
	}

	@SuppressWarnings("unchecked")
	private void updatedPassword(Dictionary properties) {
		Object property = properties.get("password");
		if (property != null) {
			this.managedProperties.setPassword((String) property);
		} else {
			this.logService.warning("Configuration hostname is not set. "
					+ "Using default value: ''");
		}
	}

	@SuppressWarnings("unchecked")
	private void updatedDirectory(Dictionary properties) {
		Object property = properties.get("directory");
		if (property != null) {
			this.managedProperties.setDirectory((String) property);
		} else {
			this.logService.warning("Configuration hostname is not set. "
					+ "Using default value: '/'");
		}
	}

	private void logUpdated() {
		this.logService
				.info(String.format(
						"Configuration updated. hostname:%s, port:%d, username:%s, " + 
						"password:******, directory:%s",
						this.managedProperties.getHostname(),
						this.managedProperties.getPort(),
						this.managedProperties.getUsername(),
						this.managedProperties.getDirectory()));
	}

	public FtpManagedService(FtpProperties managedProperties,
			LogServiceWrapper logService) {
		this.managedProperties = managedProperties;
		this.logService = logService;
	}

}
