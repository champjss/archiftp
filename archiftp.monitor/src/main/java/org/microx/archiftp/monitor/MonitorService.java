package org.microx.archiftp.monitor;

public interface MonitorService {
	void startMonitor();
	void stopMonitor();
	boolean isMonitoring();
}
