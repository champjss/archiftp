package com.ingres.archiftp.ftp.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.net.ftp.FTPClient;

import com.ingres.archiftp.logger.Logger;

public class FtpWorker {

	private FtpProperties properties;
	private Logger logger;
	private FTPClient ftp = new FTPClient();

	public void uploadFile(File file) {
		String hostname = this.properties.getHostname();
		int port = this.properties.getPort();
		String username = this.properties.getUsername();
		String password = this.properties.getPassword();
		String directory = this.properties.getDirectory();

		try {
			checkIsFileExists(file);
			connect(hostname, port);
			passv();
			login(username, password);
			changeDirectory(directory);
		} catch (Exception e) {
			String message = String.format(
					"Cannot upload file '%s' to '%s:%d' with username '%s' "
					+ "at directory '%s' (fail before upload process begins).",
					file.getAbsolutePath(), hostname, port, username, directory);
			throw new RuntimeException(message, e);
		}

		try {
			storeFile(directory, file);
		} catch (Exception e) {
			try {
				disconnect();
			} catch (Exception e1) {
				// We can't do things more than this.
			}
			
			String message = String.format(
					"Cannot upload file '%s' to '%s:%d' with username '%s' "
					+ "at directory '%s' (fail in upload process).", file
					.getAbsolutePath(), hostname, port, username, directory); 
			throw new RuntimeException(message, e);
		}
		
		try {
			disconnect();
		} catch (Exception e) {
		}
	}

	private void connect(String hostname, int port) {
		try {
			ftp.connect(hostname, port);
		} catch (Exception e) {
			String message = String.format("Connect to '%s:%d' failed.", hostname, port);
			this.logger.debug(message, e);
			throw new RuntimeException(message, e);
		}
	}

	private void passv() {
		try {
			ftp.pasv();
		} catch (Exception e) {
			String message = "Entering passive mode failed.";
			this.logger.debug(message);
			throw new RuntimeException(message, e);
		}
	}

	private void login(String username, String password) {
		boolean loginResult = false;
		
		try {
			loginResult = ftp.login(username, password);
		} catch (Exception e) {
			String message = String.format(
					"Login with username '%s' failed.", username);
			this.logger.debug(message, e);
			throw new RuntimeException(message, e);
		}
		
		if (!loginResult) {
			String message = String.format(
					"Login with username '%s' failed " + 
					"(username and password combination maybe wrong).", username);
			this.logger.debug(message);
			throw new RuntimeException(message);
		}
	}

	private void disconnect() {
		try {
			ftp.disconnect();
		} catch (Exception e) {
			this.logger.debug("Disconnecting failed.", e);
		}
	}

	private void changeDirectory(String directory) {
		int cwdResultCode = 0;

		try {
			cwdResultCode = ftp.cwd(directory);
		} catch (Exception e) {
			String message = String.format(
					"Changing directory to '%s' failed.", directory);
			this.logger.debug(message, e);
			throw new RuntimeException(message, e);
		}

		if (!isReturnCodeOk(cwdResultCode)) {
			String message = String.format(
					"Changing directory to '%s' failed (with return code %d). "
					+ "The directory may not exits in the server.",
						directory, cwdResultCode);
			this.logger.debug(message);
			throw new RuntimeException(message);
		}
	}

	private boolean isReturnCodeOk(int returnCode) {
		return returnCode / 100 == 2;
	}

	private void storeFile(String directory, File file) {
		FileInputStream fileStream = getFileInputStream(file);
		String remotePath = "";
		boolean storeFileResult = false;

		try {
			remotePath = getStringOfFileNameInServer(directory, file);
			storeFileResult = ftp.storeFile(remotePath, fileStream);
		} catch (Exception e) {
			String message = String.format(
					"Uploading file '%s' to directory '%s' failed.", file
					.getAbsolutePath(), remotePath);
			this.logger.debug(message, e);
			throw new RuntimeException(message, e);
		}

		if (!storeFileResult) {
			String message = String.format(
					"Uploading file '%s' to directory '%s' failed.", file
					.getAbsolutePath(), remotePath);
			this.logger.debug(message);
			throw new RuntimeException(message);
		}
	}

	private FileInputStream getFileInputStream(File file) {
		FileInputStream fileStream = null;

		try {
			fileStream = new FileInputStream(file);
		} catch (Exception e) {
			String message = String.format(
					"Getting FileInputStream of file '%s' failed.", file
					.getAbsolutePath());
			this.logger.debug(message, e);
			throw new RuntimeException(message, e);
		}

		return fileStream;
	}

	private String getStringOfFileNameInServer(String directory, File file) {
		return getStringOfAbsolutePath(directory, file);
	}

	private String getStringOfAbsolutePath(String directory, File file) {
		return String.format("%s%s%s", directory, File.separator, file
				.getName());
	}

	private void checkIsFileExists(File file) throws FileNotFoundException {
		if (!file.exists()) {
			String message = String.format(
					"File %s is not found.", file.getAbsolutePath());
			this.logger.debug(message);
			throw new FileNotFoundException(message);
		}
	}

	public FtpWorker(FtpProperties properties, Logger logger) {
		this.properties = properties;
		this.logger = logger;
	}
}
