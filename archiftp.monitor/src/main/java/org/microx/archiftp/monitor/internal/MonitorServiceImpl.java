package org.microx.archiftp.monitor.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.management.ServiceNotFoundException;

import org.microx.archiftp.archive.ArchiveService;
import org.microx.archiftp.archive.PermissionDeniedException;
import org.microx.archiftp.ftp.FtpService;
import org.microx.archiftp.monitor.MonitorService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public final class MonitorServiceImpl implements MonitorService, ManagedService {

	// Services
	private ServiceTracker logServiceTracker;
	private ServiceTracker ftpServiceTracker;
	private ServiceTracker archiveServiceTracker;
	private ServiceTracker eventAdminServiceTracker;

	// Managed Properties
	private String monitorPath;
	private long monitorInterval = 10000;

	// Other properties
	private Thread monitorThread;
	private long waitTimeForUnlockFile = 500;
	private boolean monitorStopFlag = false;
	private boolean firstRunPassed = false;

	public MonitorServiceImpl(ServiceTracker logServiceTracker, ServiceTracker ftpServiceTracker,
			ServiceTracker archiveServiceTracker, ServiceTracker eventAdminServiceTracker) {
		this.logServiceTracker = logServiceTracker;
		this.ftpServiceTracker = ftpServiceTracker;
		this.archiveServiceTracker = archiveServiceTracker;
		this.eventAdminServiceTracker = eventAdminServiceTracker;
	}

	public void startMonitor() {
		stopMonitorThreadIfExists();
		this.monitorThread = new MonitorThread();
		this.monitorThread.start();
		logDebugMonitorStart();
	}

	class MonitorThread extends Thread {
		private String currentMonitorPath;
		private long currentMonitorInterval;

		private File monitorDirectory;

		public void run() {
			while (monitorStopFlag == false) {
				checkForChangedPropertiesAndChange();
				if (isMonitorDirectoryAvailable() == false) {
					break;
				}
				doMonitorJob();
				waitForInterval();
			}
		}

		private synchronized void checkForChangedPropertiesAndChange() {
			if (this.currentMonitorPath != monitorPath) {
				setCurrentMonitorPath(monitorPath);
			}

			if (this.currentMonitorInterval != monitorInterval) {
				setCurrentMonitorInterval(monitorInterval);
			}
		}

		private synchronized void setCurrentMonitorPath(String path) {
			this.currentMonitorPath = path;
			this.monitorDirectory = new File(this.currentMonitorPath);
		}

		private synchronized void setCurrentMonitorInterval(long interval) {
			this.currentMonitorInterval = interval;
		}

		private boolean isMonitorDirectoryAvailable() {
			if (this.monitorDirectory.exists() == false) {
				logErrorMonitorDirectoryNotFound(this.monitorDirectory);
				return false;
			}
			if (this.monitorDirectory.canRead() == false
					|| this.monitorDirectory.canWrite() == false) {
				logErrorMonitorNoPermission(this.monitorDirectory);
				return false;
			}

			return true;
		}

		private void doMonitorJob() {
			File[] files = this.monitorDirectory.listFiles();
			for (File file : files) {
				try {
					doJobsForFile(file);
				}
				catch (IOException e) {
					logErrorIOException(file, e);
					break;
				}
				catch (ServiceNotFoundException e) {
					logErrorServiceNotFound(file, e);
					break;
				}
			}
		}

		private void doJobsForFile(File file) throws ServiceNotFoundException, IOException {
			try {
				doFtpJob(file);
				postEventForHistoryWriter(file);
				waitForUnlockFile();
				doArchiveJob(file);
			}
			catch (FileNotFoundException e) {
				logErrorFileNotFound(file, e);
			}
		}

		private void doFtpJob(File file) throws FileNotFoundException, IOException,
				ServiceNotFoundException {
			FtpService ftp = getFtpService();
			if (ftp != null) {
				ftp.uploadFile(file);
			}
			else {
				throw new ServiceNotFoundException("FTPService not found.");
			}
		}

		private void postEventForHistoryWriter(File file) {
			Event event = getEventForHistoryWriter(file);
			EventAdmin service = getEventAdminService();

			if (service != null) {
				service.postEvent(event);
			}
			else {
				logWarningEventAdminServiceNotFound();
			}
		}

		@SuppressWarnings("unchecked")
		private Event getEventForHistoryWriter(File file) {
			String message = getEventMessageForHistoryWriter(file);
			Dictionary properties = new Hashtable();
			properties.put(EventConstants.MESSAGE, message);
			return new Event("org/microx/archiftp/FILE_UPLOADED", properties);
		}

		private String getEventMessageForHistoryWriter(File file) {
			return String.format("'%s' was uploaded.", file.getAbsolutePath());
		}

		private void doArchiveJob(File file) throws FileNotFoundException, ServiceNotFoundException {
			ArchiveService archive = getArchiveService();
			if (archive != null) {
				try {
					archive.moveFileToArchive(file);
				}
				catch (PermissionDeniedException e) {
					logErrorPermissionDenied(file, e);
				}
			}
			else {
				throw new ServiceNotFoundException("ArchiveService not found.");
			}
		}

		private void waitForInterval() {
			try {
				Thread.sleep(this.currentMonitorInterval);
			}
			catch (InterruptedException e) {
				logErrorThreadInterrupted(e);
			}
		}
		
				
		private void waitForUnlockFile() {
			try {
				Thread.sleep(currentMonitorInterval);
			}
			catch (InterruptedException e) {
				logErrorThreadInterrupted(e);
			}
		}
	}

	private EventAdmin getEventAdminService() {
		if (this.eventAdminServiceTracker == null) {
			return null;
		}

		return (EventAdmin) this.eventAdminServiceTracker.getService();
	}

	private void stopMonitorThreadIfExists() {
		if (isMonitoring()) {
			stopMonitor();
		}
		logDebugMonitorStop();
	}

	public boolean isMonitoring() {
		if (this.monitorThread != null) {
			return this.monitorThread.getState() != State.TERMINATED
					&& this.monitorThread.getState() != State.NEW;
		}
		else {
			return false;
		}
	}

	private FtpService getFtpService() {
		if (this.ftpServiceTracker == null) {
			return null;
		}

		return (FtpService) this.ftpServiceTracker.getService();
	}

	private ArchiveService getArchiveService() {
		if (this.archiveServiceTracker == null) {
			return null;
		}

		return (ArchiveService) this.archiveServiceTracker.getService();
	}

	public void stopMonitor() {
		this.monitorStopFlag = true;
	}

	@SuppressWarnings("unchecked")
	public void updated(Dictionary properties) throws ConfigurationException {
		if (properties != null) {
			updateMonitorPath(properties);
			updateMonitorInterval(properties);
			logInfoPropertiesUpdated();
			autoStartIfFirstRun();
		}
	}

	@SuppressWarnings("unchecked")
	private void updateMonitorPath(Dictionary properties) {
		String newMonitorPath = (String) properties.get("monitor-path");
		setMonitorPath(newMonitorPath);
	}

	@SuppressWarnings("unchecked")
	private void updateMonitorInterval(Dictionary properties) throws ConfigurationException {
		long newMonitorInterval;

		try {
			newMonitorInterval = (Long) properties.get("monitor-interval");
		}
		catch (ClassCastException e) {
			throw new ConfigurationException("monitor-interval",
					"Monitor interval must be long positive number.", e);
		}

		if (newMonitorInterval < 1) {
			throw new ConfigurationException("monitor-interval",
					"Monitor interval must be long positive number.");
		}
	}

	private void autoStartIfFirstRun() {
		if (this.firstRunPassed == false) {
			startMonitor();
			this.firstRunPassed = true;
		}
	}

	public String getMonitorPath() {
		return monitorPath;
	}

	public void setMonitorPath(String monitorPath) {
		this.monitorPath = monitorPath;
	}

	public long getMonitorInterval() {
		return monitorInterval;
	}

	public void setMonitorInterval(long monitorInterval) {
		this.monitorInterval = monitorInterval;
	}

	private void logErrorMonitorDirectoryNotFound(File file) {
		String logMessage = String.format("Directory '%s' to monitor not found.",
				file.getAbsolutePath());
		logError(logMessage);
	}

	private void logErrorMonitorNoPermission(File file) {
		String logMessage = String.format(
				"Can't monitor directory '%s' because permission denied.", file.getAbsolutePath());
		logError(logMessage);
	}

	private void logErrorIOException(File file, Exception e) {
		String logMessage = String.format("Upload file '%s' failed. (IOException)",
				file.getAbsolutePath());
		logError(logMessage, e);
	}

	private void logErrorServiceNotFound(File file, Exception e) {
		String logMessage = String.format("Upload and moving file '%s' failed. "
				+ "(FtpService and/or ArchiveService not found)", file.getAbsolutePath());
		logError(logMessage, e);
	}

	private void logErrorFileNotFound(File file, Exception e) {
		String logMessage = String.format("Upload and moving file '%s' failed. "
				+ "(File not found)", file.getAbsolutePath());
		logError(logMessage, e);
	}

	private void logErrorPermissionDenied(File file, Exception e) {
		String logMessage = String.format("Moving file '%s' failed (Permission denied).",
				file.getAbsolutePath());
		logError(logMessage, e);
	}

	private void logWarningEventAdminServiceNotFound() {
		String logMessage = "EventAdminService not found. History not be wrote.";
		logWarning(logMessage);
	}

	private void logErrorThreadInterrupted(Exception e) {
		String logMessage = "Monitor thread was interrupted.";
		logError(logMessage, e);
	}

	private void logDebugMonitorStart() {
		String logMessage = "Monitoring is started.";
		logInfo(logMessage);
	}

	private void logDebugMonitorStop() {
		String logMessage = "Monitoring is stopped.";
		logInfo(logMessage);
	}

	private void logInfoPropertiesUpdated() {
		String logMessage = String.format("Properties of MonitorService updated. "
				+ "{%s=%s, %s=%d}", "monitor-path", this.monitorPath, "monitor-interval",
				this.monitorInterval);
		logInfo(logMessage);
	}

	private void logError(String message) {
		log(LogService.LOG_ERROR, message);
	}

	private void logError(String message, Exception e) {
		log(LogService.LOG_ERROR, message, e);
	}

	private void logWarning(String message) {
		log(LogService.LOG_WARNING, message);
	}

	private void logInfo(String message) {
		log(LogService.LOG_INFO, message);
	}

	private void log(int level, String message) {
		LogService logService = getLogService();
		if (logService != null) {
			logService.log(level, message);
		}
		else {
			String nameOfLevel = getNameOfLevel(level);
			System.out.println(String.format("[%s] %s", nameOfLevel, message));
		}
	}

	private void log(int level, String message, Exception e) {
		LogService logService = getLogService();
		if (logService != null) {
			logService.log(level, message, e);
		}
		else {
			String nameOfLevel = getNameOfLevel(level);
			System.out.println(String.format("[%s] %s (%s: %s)", nameOfLevel, message, e.getClass()
					.getName(), e.getMessage()));
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
