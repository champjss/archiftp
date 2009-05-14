package com.ingres.archiftp.ftp.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.net.ftp.FTPClient;

public class FtpWorker {

	private FtpProperties properties;
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
			login(username, password);
			changeDirectoryAndUploadFile(directory, file);
			disconnect();
		} catch (Throwable e) {
			throw new RuntimeException(String.format(
					"Cannot upload file %s to %s:%d with username:%s, password:%s "
							+ "at %s", file.getAbsolutePath(), hostname, port,
					username, password, directory), e);
		}
	}

	private void connect(String hostname, int port) throws Exception {
		try {
			ftp.connect(hostname, port);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Cannot connect to %s:%s (%s).", 
					hostname, port, e.getClass().getName()));
		}
	}

	private void login(String username, String password) throws Exception {
		try {
			ftp.login(username, password);
		} catch (Exception e) { 
			throw new RuntimeException(String.format("Cannot login with username '%s' (%s).", 
					username, e.getClass().getName()));
		}
	}

	private void changeDirectoryAndUploadFile(String directory, File file) {
		changeDirectory(directory);
		storeFile(directory, file);
	}

	private void disconnect() {
		try {
			ftp.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void changeDirectory(String directory){
		try {
			ftp.cwd(directory);
		} catch (Throwable e) {
			throw new RuntimeException(String.format("Cannot change directory to '%s' (%s).", 
					directory, e.getClass().getName()));
		}
	}

	private void storeFile(String directory, File file) {
		FileInputStream fileStream = getFileInputStream(file);
		String remotePath;

		try {
			remotePath = getStringOfFileNameInServer(directory, file);
			ftp.storeFile(remotePath, fileStream);
		} catch (Exception e) {
			throw new RuntimeException(String.format(
					"Cannot upload file '%s' to directory %s in server (%s).", 
					file.getAbsolutePath(), directory, e.getClass().getName()));
		}
	}

	private FileInputStream getFileInputStream(File file) {
		FileInputStream fileStream = null;

		try {
			fileStream = new FileInputStream(file);
		} catch (Exception e) {
			throw new RuntimeException(String.format(
					"Cannot get FileInputStream of file '%s' (%s).", file.getAbsolutePath(), 
					e.getClass().getName()));
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
			throw new FileNotFoundException(String.format("File %s not found.",
					file.getAbsolutePath()));
		}
	}
	
	public FtpWorker(FtpProperties properties) {
		this.properties = properties;
	}
}
