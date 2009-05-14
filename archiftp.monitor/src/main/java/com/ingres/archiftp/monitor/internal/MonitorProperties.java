package com.ingres.archiftp.monitor.internal;

public class MonitorProperties {

	private String monitorPath;
	private long monitorInterval;
	
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
	
	public boolean isInitialized() {
		if (this.monitorPath != null) {
			return true;
		}
		
		return false;
	}
	
}
