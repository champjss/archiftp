package org.microx.archiftp.monitor.internal;

import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import org.microx.archiftp.archive.ArchiveService;
import org.microx.archiftp.ftp.FtpService;
import org.microx.archiftp.monitor.MonitorService;

public final class MonitorActivator implements BundleActivator {

	private final String pid = "org.microx.archiftp.monitor";

	private BundleContext context;
	private ServiceTracker logServiceTracker;
	private ServiceTracker ftpServiceTracker;
	private ServiceTracker archiveServiceTracker;

	public void start(BundleContext context) throws Exception {
		this.context = context;
		this.logServiceTracker = getLogServiceTracker();
		this.ftpServiceTracker = getFtpServiceTracker();
		this.archiveServiceTracker = getArchiveServiceTracker();
		this.logServiceTracker.open();
		this.ftpServiceTracker.open();
		this.archiveServiceTracker.open();

		MonitorService MonitorService = new MonitorServiceImpl(this.logServiceTracker,
				this.ftpServiceTracker, this.archiveServiceTracker);
		registerManagedService(MonitorService);
		registerMonitorService(MonitorService);
	}

	public void stop(BundleContext context) throws Exception {
		this.ftpServiceTracker.close();
		this.archiveServiceTracker.close();
		this.logServiceTracker.close();
	}

	private ServiceTracker getLogServiceTracker() {
		return new ServiceTracker(this.context, LogService.class.getName(), null);
	}

	private ServiceTracker getFtpServiceTracker() {
		return new ServiceTracker(this.context, FtpService.class.getName(), null);
	}

	private ServiceTracker getArchiveServiceTracker() {
		return new ServiceTracker(this.context, ArchiveService.class.getName(), null);
	}

	@SuppressWarnings("unchecked")
	private void registerMonitorService(Object MonitorService) {
		if (MonitorService instanceof MonitorService) {
			Dictionary properties = getMonitorServiceProperties();
			this.context.registerService(MonitorService.class.getName(), MonitorService, properties);
		}
	}

	@SuppressWarnings("unchecked")
	private Dictionary getMonitorServiceProperties() {
		ConfigurationAdmin configAdminService = getConfigurationAdminService();
		Dictionary properties = new Properties();

		try {
			properties = configAdminService.getConfiguration(this.pid).getProperties();
		}
		catch (Exception e) {
			logError("Unexpected exception when get MonitorService's properties.", e);
		}

		return properties;
	}

	private ConfigurationAdmin getConfigurationAdminService() {
		ServiceReference reference = getConfigurationAdminServiceReference();
		return (ConfigurationAdmin) this.context.getService(reference);
	}

	private ServiceReference getConfigurationAdminServiceReference() {
		return this.context.getServiceReference(ConfigurationAdmin.class.getName());
	}

	@SuppressWarnings("unchecked")
	private void registerManagedService(Object managedService) {
		if (managedService instanceof ManagedService) {
			Dictionary properties = new Properties();
			properties.put(Constants.SERVICE_PID, this.pid);
			this.context.registerService(ManagedService.class.getName(), managedService, properties);
		}
	}

	private void logError(String message, Exception e) {
		log(LogService.LOG_ERROR, message, e);
	}

	private void log(int level, String message, Exception e) {
		LogService logService = getLogService();
		if (logService != null) {
			logService.log(level, message, e);
		}
		else {
			String nameOfLevel = getNameOfLevel(level);
			String exceptionClass = e.getClass().getName();
			String exceptionMessage = e.getMessage();
			System.out.println(String.format("[%s] %s (%s, %s)", nameOfLevel, message,
					exceptionClass, exceptionMessage));
		}
	}

	private String getNameOfLevel(int level) {
		switch (level) {
			case LogService.LOG_DEBUG:
				return "Debug";
			case LogService.LOG_ERROR:
				return "Error";
			case LogService.LOG_INFO:
				return "Info";
			case LogService.LOG_WARNING:
				return "Warning";
			default:
				return "";
		}
	}

	private LogService getLogService() {
		if (this.logServiceTracker == null) {
			return null;
		}
		
		return (LogService) this.logServiceTracker.getService();
	}

}
