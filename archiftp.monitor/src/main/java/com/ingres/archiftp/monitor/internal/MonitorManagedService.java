package com.ingres.archiftp.monitor.internal;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import com.ingres.archiftp.logger.Logger;
import com.ingres.archiftp.monitor.MonitorService;

public class MonitorManagedService implements ManagedService {

	private MonitorProperties managedProperties;
	private Logger logger;
	private MonitorService monitorService;
	
	@SuppressWarnings("unchecked")
	public void updated(Dictionary properties) throws ConfigurationException {
		if (properties != null) {;
			updatedMonitorPath(properties);
			try {
				updatedMonitorInterval(properties);
			} catch (Throwable e) {
				throw new ConfigurationException("monitor-interval", 
						"monitor-interval must be positive long number.", e);
			}
			logUpdated();
			startMonitoringIfNeed();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void updatedMonitorPath(Dictionary properties) {
		Object property = properties.get("monitor-path");
		if (property != null) {
			this.managedProperties.setMonitorPath((String)property);
		}
		else {
			this.logger.warning("Configuration monitor-path is not set. " +
					"If you not set it, This service cannot work properly.");
		}
	}
	
	@SuppressWarnings("unchecked")
	private void updatedMonitorInterval(Dictionary properties) {
		Object property = properties.get("monitor-interval");
		if (property != null) {
			try {
				Long interval = ((Long)property);
				if (interval <= 0) {
					throw new RuntimeException("monitor-interval is invalid. " + 
							"It must be positive long number.");
				}
				this.managedProperties.setMonitorInterval(interval);
			}
			catch (Throwable e) {
				throw new RuntimeException("monitor-interval is invalid. " + 
						"It must be positive long number.", e);
			}
		}
		else {
			this.logger.warning("Configuration hostname is not set. "
					+ "Using default value: 21");
		}
	}
	
	private void logUpdated() {
		this.logger.info(String.format(
				"Configuration updated. monitor-path:%s, monitor-interval:%d",
				this.managedProperties.getMonitorPath(), this.managedProperties.getMonitorInterval()));
	}
	
	private void startMonitoringIfNeed() {
		if (!this.monitorService.isMonitoring()) {
			this.monitorService.startMonitor();
		}
	}
	
	public MonitorManagedService(MonitorProperties managedProperties, Logger logger, 
			MonitorService monitorService) {
		this.managedProperties = managedProperties;
		this.logger = logger;
		this.monitorService = monitorService;
	}

}
