package org.microx.archiftp.archive.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;

import org.microx.archiftp.archive.ArchiveService;
import org.microx.archiftp.archive.PermissionDeniedException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public final class ArchiveServiceImpl implements ArchiveService, ManagedService {

	// Services
	private ServiceTracker logServiceTracker;

	// Managed properties
	private String archivePath;
	private boolean seperateFolderByDate = false;

	// Other properties
	private File archive;

	public ArchiveServiceImpl(ServiceTracker logServiceTracker) {
		this.logServiceTracker = logServiceTracker;
	}

	// ArchiveService
	public void moveFileToArchive(File file) throws FileNotFoundException,
			PermissionDeniedException {
		checkIsMovingAvailable(file);
		moveFile(file);
	}

	private void checkIsMovingAvailable(File file) throws FileNotFoundException,
			PermissionDeniedException {
		checkIsFileAvailable(file);
		checkIsArchiveAvailable();
	}

	private void checkIsFileAvailable(File file) throws FileNotFoundException,
			PermissionDeniedException {
		if (file.exists() == false) {
			throw new FileNotFoundException(String.format("File '%s' not found.",
					file.getAbsolutePath()));
		}
		if (file.getParentFile().canWrite() == false) {
			throw new PermissionDeniedException(String.format("No permission to move file '%s'.",
					file.getAbsolutePath()));
		}
	}

	private void checkIsArchiveAvailable() throws FileNotFoundException, PermissionDeniedException {
		if (this.archive.exists() == false) {
			throw new FileNotFoundException(String.format("Archive '%s' not found.",
					this.archive.getAbsolutePath()));
		}
		if (this.archive.canWrite() == false) {
			throw new PermissionDeniedException(String.format(
					"No permission to write to archive '%s'.", this.archive.getAbsolutePath()));
		}
	}

	private void moveFile(File file) {
		File newFile = getNewFileInArchive(file);
		file.renameTo(newFile);
		logDebugFileMoved(file, newFile);
	}

	private File getNewFileInArchive(File file) {
		if (this.seperateFolderByDate == true) {
			return getNewFileInArchiveInFolderOfDate(file);
		}
		else {
			return getNewFileInArchiveAtRoot(file);
		}

	}

	private File getNewFileInArchiveInFolderOfDate(File file) {
		String directoryPath = makeDirectoryForToday().getAbsolutePath();
		String fileName = file.getName();
		String newFileName = getStringOfAbsolutePath(directoryPath, fileName);
		return new File(newFileName);
	}

	private File makeDirectoryForToday() {
		String archivePath = this.archive.getAbsolutePath();
		String currentDateString = getStringOfCurrentDate();
		String todayDirectoryPath = getStringOfAbsolutePath(archivePath, currentDateString);
		File todayDirectory = new File(todayDirectoryPath);

		if (todayDirectory.exists() == false) {
			todayDirectory.mkdir();
		}

		return todayDirectory;
	}

	private String getStringOfCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String result = dateFormat.format(new Date());
		return result;
	}

	private File getNewFileInArchiveAtRoot(File file) {
		String directoryPath = this.archive.getAbsolutePath();
		String fileName = file.getName();
		String newFileName = getStringOfAbsolutePath(directoryPath, fileName);
		return new File(newFileName);
	}

	private String getStringOfAbsolutePath(String directoryPath, String fileName) {
		return String.format("%s%s%s", directoryPath, File.separator, fileName);
	}

	// ManagedService
	@SuppressWarnings("unchecked")
	public void updated(Dictionary properties) throws ConfigurationException {
		if (properties != null) {
			updateArchive(properties);
			updateSeperateFolderByDate(properties);
			logInfoPropertiesUpdated();
		}
	}

	@SuppressWarnings("unchecked")
	private void updateArchive(Dictionary properties) {
		String newArchivePath = (String) properties.get("archive-path");
		setArchivePath(newArchivePath);
	}

	@SuppressWarnings("unchecked")
	private void updateSeperateFolderByDate(Dictionary properties) throws ConfigurationException {
		try {
			boolean newValue = (Boolean) properties.get("seperate-folder-by-date");
			setSeperateFolderByDate(newValue);
		}
		catch (ClassCastException e) {
			throw new ConfigurationException("seperate-folder-by-date",
					"seperate-folder-by-date must be only 'true' or 'false'", e);
		}
	}

	public String getArchivePath() {
		return archivePath;
	}

	public void setArchivePath(String archivePath) {
		this.archivePath = archivePath;
		this.archive = new File(archivePath);
	}

	public boolean isSeperateFolderByDate() {
		return seperateFolderByDate;
	}

	public void setSeperateFolderByDate(boolean seperateFolderByDate) {
		this.seperateFolderByDate = seperateFolderByDate;
	}

	// Log
	private void logDebugFileMoved(File file, File newFile) {
		String logMessage = String.format("File was moved to archive. (%s -> %s)",
				file.getAbsolutePath(), newFile.getAbsolutePath());
		logDebug(logMessage);
	}

	private void logInfoPropertiesUpdated() {
		String logMessage = String.format("Properties of ArchiveService updated. {%s=%s, %s=%s}",
				"archive-path", this.archivePath, "seperate-folder-by-date",
				this.seperateFolderByDate);
		logInfo(logMessage);
	}

	private void logDebug(String message) {
		log(LogService.LOG_DEBUG, message);
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
