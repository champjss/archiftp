package org.microx.archiftp.historywriter.internal;

import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import org.microx.archiftp.historywriter.HistoryWriterService;

public final class HistoryWriterActivator implements BundleActivator {

	private final String pid = "org.microx.archiftp.historywriter";

	private BundleContext context;
	private ServiceTracker logServiceTracker;

	public void start(BundleContext context) throws Exception {
		this.context = context;
		this.logServiceTracker = getLogServiceTracker();
		this.logServiceTracker.open();

		HistoryWriterService historyWriterService = new HistoryWriterServiceImpl(
				this.logServiceTracker);
		registerManagedService(historyWriterService);
		registerEventHandlerService(historyWriterService);
	}

	public void stop(BundleContext context) throws Exception {
		this.logServiceTracker.close();
	}

	private ServiceTracker getLogServiceTracker() {
		return new ServiceTracker(this.context, LogService.class.getName(), null);
	}

	@SuppressWarnings("unchecked")
	private void registerEventHandlerService(Object HistoryWriterService) {
		if (HistoryWriterService instanceof HistoryWriterService) {
			Dictionary properties = getHistoryWriterServiceProperties();
			this.context.registerService(EventHandler.class.getName(), HistoryWriterService,
					properties);
		}
	}

	@SuppressWarnings("unchecked")
	private Dictionary getHistoryWriterServiceProperties() {
		ConfigurationAdmin configAdminService = getConfigurationAdminService();
		Dictionary properties = new Properties();

		try {
			properties = configAdminService.getConfiguration(this.pid).getProperties();
		}
		catch (Exception e) {
			logError("Unexpected exception when get HistoryWriterService's properties.", e);
		}

		if (properties == null) {
			properties = new Properties();
		}
		properties.put(EventConstants.EVENT_TOPIC,
				new String[] { "org/microx/archiftp/FILE_UPLOADED" });

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
