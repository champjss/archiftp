package com.ingres.archiftp.monitor;

public interface MonitorService {
	void startMonitor();
	void stopMonitor();
	boolean isMonitoring();
}
