package org.microx.archiftp.logpage.internal;

import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import org.microx.archiftp.logpage.LogpageService;

public final class LogpageActivator implements BundleActivator {
	private final String pid = "org.microx.archiftp.logpage";

	private BundleContext context;
	private ServiceTracker logServiceTracker;
	private ServiceTracker httpServiceTracker;
	private ServiceTracker logReaderServiceTracker;
	private LogpageServiceImpl logpageService;

	public void start(BundleContext context) throws Exception {
		this.context = context;
		this.logServiceTracker = getLogServiceTracker();
		this.logServiceTracker.open();
		this.httpServiceTracker = getHttpServiceTracker();
		this.httpServiceTracker.open();
		this.logReaderServiceTracker = getLogReaderServiceTracker();
		this.logReaderServiceTracker.open();

		this.logpageService = new LogpageServiceImpl(this.logServiceTracker,
				this.httpServiceTracker, this.logReaderServiceTracker);
		registerManagedService(logpageService);
		registerLogpageService(logpageService);
	}

	public void stop(BundleContext context) throws Exception {
		this.logpageService.unregisterServlet();
		this.httpServiceTracker.close();
		this.logReaderServiceTracker.close();
		this.logServiceTracker.close();
	}

	private ServiceTracker getLogServiceTracker() {
		return new ServiceTracker(this.context, LogService.class.getName(), null);
	}
	
	private ServiceTracker getHttpServiceTracker() {
		return new ServiceTracker(this.context, HttpService.class.getName(), null);
	}
	
	private ServiceTracker getLogReaderServiceTracker() {
		return new ServiceTracker(this.context, LogReaderService.class.getName(), null);
	}

	private void registerLogpageService(Object LogpageService) {
		if (LogpageService instanceof LogpageService) {
			Dictionary properties = getLogpageServiceProperties();
			this.context.registerService(LogpageService.class.getName(), LogpageService, properties);
		}
	}

	private Dictionary getLogpageServiceProperties() {
		ConfigurationAdmin configAdminService = getConfigurationAdminService();
		Dictionary properties = new Properties();

		try {
			properties = configAdminService.getConfiguration(this.pid).getProperties();
		}
		catch (Exception e) {
			logError("Unexpected exception when get LogpageService's properties.", e);
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
			System.out.println("[" + nameOfLevel + "] " + message);
			e.printStackTrace();
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
