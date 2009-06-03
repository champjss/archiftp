package org.microx.archiftp.historywriter.internal;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;

import org.microx.archiftp.historywriter.HistoryWriterService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public final class HistoryWriterServiceImpl implements HistoryWriterService, EventHandler,
		ManagedService {

	private ServiceTracker logServiceTracker;
	private String historyFilePath = "history";
	private boolean seperateHistoryFileByDate = false;
	private String historyPathSuffix = ".txt";

	private FileWriter historyFileWriter;
	private Date currentDate;
	private DateFormat dateFormatForFileName = new SimpleDateFormat("yyyyMMdd");
	private DateFormat dateFormatForHistory = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public HistoryWriterServiceImpl(ServiceTracker logServiceTracker) {
		this.logServiceTracker = logServiceTracker;

	}

	public void handleEvent(Event event) {
		try {
			setCurrentDate();
			setAndOpenHistoryFileWriter();
			writeHistory(event);
			closeHistoryFileWriter();
		}
		catch (IOException e) {
			logErrorIOException(e);
			return;
		}
	}

	private void setCurrentDate() {
		this.currentDate = new Date();
	}

	private void setAndOpenHistoryFileWriter() throws IOException {
		String fileName;
		if (this.seperateHistoryFileByDate) {
			fileName = getFileNameAppendedWithDateAndSuffix();
		}
		else {
			fileName = getFileNameAppendedWithSuffix();
		}
		this.historyFileWriter = new FileWriter(fileName, true);
	}

	private String getFileNameAppendedWithDateAndSuffix() {
		String dateString = this.dateFormatForFileName.format(this.currentDate);
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.historyFilePath).append('.').append(dateString).append(this.historyPathSuffix);
		return buffer.toString();
	}
	
	private String getFileNameAppendedWithSuffix() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.historyFilePath).append(this.historyPathSuffix);
		return buffer.toString();
	}

	private void writeHistory(Event event) throws IOException {
		if (event != null && this.historyFileWriter != null) {
			String message = getHistoryMessage(event);
			this.historyFileWriter.write(message);
		}
	}

	private String getHistoryMessage(Event event) {
		String stringOfDateTime = this.dateFormatForHistory.format(this.currentDate);
		String messageText = (String) event.getProperty(EventConstants.MESSAGE);
		StringBuffer buff = new StringBuffer();
		buff.append('[').append(stringOfDateTime).append(']');
		buff.append(' ').append(messageText).append(System.getProperty("line.separator"));
		return buff.toString();
	}

	private void closeHistoryFileWriter() throws IOException {
		this.historyFileWriter.flush();
		this.historyFileWriter.close();
	}

	public void updated(Dictionary properties) throws ConfigurationException {
		if (properties != null) {
			updateHistoryFilePath(properties);
			updateSeperateHistoryFileByDate(properties);
			logInfoPropertiesUpdated();
		}
	}

	private void updateHistoryFilePath(Dictionary properties) throws ConfigurationException {
		String newFilePath = (String) properties.get("history-path");
		setHistoryFilePath(newFilePath);
	}

	private void updateSeperateHistoryFileByDate(Dictionary properties)
			throws ConfigurationException {
		try {
			boolean newValue = ((Boolean) properties.get("seperate-file-by-date")).booleanValue();
			setSeperateHistoryFileByDate(newValue);
		}
		catch (ClassCastException e) {
			throw new ConfigurationException("seperate-file-by-date",
					"seperate-file-by-date must be only 'true' or 'false'", e);
		}
	}
	
	private void updateHistoryFileSuffix(Dictionary properties) throws ConfigurationException {
		String newFilePath = (String) properties.get("history-path-suffix");
		setHistoryPathSuffix(newFilePath);
	}

	public String getHistoryFilePath() {
		return historyFilePath;
	}

	public void setHistoryFilePath(String historyFilePath) {
		this.historyFilePath = historyFilePath;
	}

	public boolean isSeperateHistoryFileByDate() {
		return seperateHistoryFileByDate;
	}

	public void setSeperateHistoryFileByDate(boolean seperateHistoryFileByDate) {
		this.seperateHistoryFileByDate = seperateHistoryFileByDate;
	}
	
	public String getHistoryPathSuffix() {
		return historyPathSuffix;
	}
	
	public void setHistoryPathSuffix(String historyPathSuffix) {
		this.historyPathSuffix = historyPathSuffix;
	}

	private void logErrorIOException(Exception e) {
		String logMessage = "Cannot write history to file. "
				+ "Is history path set to unwriteable folder.";
		logError(logMessage, e);
	}

	private void logInfoPropertiesUpdated() {
		StringBuffer logMessage = new StringBuffer();
		logMessage.append("Properties of HistoryWriter updated. {history-path=");
		logMessage.append(this.historyFilePath);
		logMessage.append(", seperate-file-by-date=");
		logMessage.append(this.seperateHistoryFileByDate);
		logMessage.append(", history-path-suffix=");
		logMessage.append(this.historyPathSuffix);
		logMessage.append("}");
		logInfo(logMessage.toString());
	}

	private void logError(String message, Exception e) {
		log(LogService.LOG_ERROR, message, e);
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
			System.out.println("[" + nameOfLevel + "]" + message);
		}
	}

	private void log(int level, String message, Exception e) {
		LogService logService = getLogService();
		if (logService != null) {
			logService.log(level, message, e);
		}
		else {
			String nameOfLevel = getNameOfLevel(level);
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
