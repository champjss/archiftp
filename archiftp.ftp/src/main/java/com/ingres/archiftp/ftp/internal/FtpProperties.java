package com.ingres.archiftp.ftp.internal;

public class FtpProperties {
	
	private String hostname;
	private int port = 21;
	private String username = "anonymous";
	private String password = "";
	private String directory = "";
	
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	
	public boolean isInitialized() {
		if (this.hostname != null) {
			return true;
		}
		
		return false;
	}
	
}
