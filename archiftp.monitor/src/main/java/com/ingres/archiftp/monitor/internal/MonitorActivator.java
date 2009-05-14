package com.ingres.archiftp.monitor.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import com.ingres.archiftp.archive.ArchiveService;
import com.ingres.archiftp.ftp.FtpService;
import com.ingres.archiftp.monitor.MonitorService;

public final class MonitorActivator implements BundleActivator {

	private MonitorService monitorService;
	private ServiceRegistration registration;
	private ServiceTracker logServiceTracker;
	private ServiceTracker archiveServiceTracker;
	private ServiceTracker ftpServiceTracker;
	private String pid = "com.ingres.archiftp.monitor";
	
	public void start(BundleContext bc) throws Exception {
		this.logServiceTracker = getServiceTracker(LogService.class, bc);
		this.logServiceTracker.open();
		this.archiveServiceTracker = getServiceTracker(ArchiveService.class, bc);
		this.archiveServiceTracker.open();
		this.ftpServiceTracker = getServiceTracker(FtpService.class, bc);
		this.ftpServiceTracker.open();
		
		// Create LogServiceWrapper
		ServiceReference monitorServiceReference = getServiceReference(MonitorService.class, bc);
		ServiceReference managedServiceReference = getServiceReference(ManagedService.class, bc);
		LogServiceWrapper logMonitorService = new LogServiceWrapper(
				monitorServiceReference, this.logServiceTracker);
		LogServiceWrapper logManagedService = new LogServiceWrapper(
				managedServiceReference, this.logServiceTracker);
		
		// Create other ServiceWrapper
		ArchiveServiceWrapper archiveService = new ArchiveServiceWrapper(this.archiveServiceTracker);
		FtpServiceWrapper ftpService = new FtpServiceWrapper(this.ftpServiceTracker);
		
		MonitorProperties properties = new MonitorProperties();
		
		// Create services
		this.monitorService = new MonitorServiceImpl(properties, logMonitorService, 
				ftpService, archiveService);
		ManagedService monitorManagedService = new MonitorManagedService(
				properties, logManagedService, monitorService);
		
		// Register services
		registerManagedService((MonitorManagedService)monitorManagedService, bc);
		registerMonitorService(monitorService, bc);
	}

	public void stop(BundleContext bc) throws Exception {
		this.registration.unregister();
		
		// Stop monitor thread
		this.monitorService.stopMonitor();
		
		this.ftpServiceTracker.close();
		this.archiveServiceTracker.close();
		this.logServiceTracker.close();
	}
	
	@SuppressWarnings("unchecked")
	private void registerManagedService(MonitorManagedService service, BundleContext bc) {
		Dictionary properties = new Properties();
		properties.put("service.pid", this.pid);
		bc.registerService(ManagedService.class.getName(), service, properties);
	}
	
	@SuppressWarnings("unchecked")
	private void registerMonitorService(MonitorService service, BundleContext bc) {
		Dictionary properties = getServiceProperties(bc);
		this.registration = bc.registerService(MonitorService.class.getName(), service, properties);
	}
	
	@SuppressWarnings("unchecked")
	private Dictionary getServiceProperties(BundleContext bc) { 
		ServiceReference reference = getServiceReference(ConfigurationAdmin.class, bc);;
        ConfigurationAdmin confAdmin = (ConfigurationAdmin)bc.getService(reference);
        
        Dictionary properties = new Properties();
        if (confAdmin != null) {
        	try {
				properties = confAdmin.getConfiguration(pid).getProperties();
				return properties;
			} catch (IOException e) {
				// access to persistent storage fails.
			}
        }

		return properties;
	}
	
	@SuppressWarnings("unchecked")
	private ServiceTracker getServiceTracker(Class service, BundleContext bc) {
		return new ServiceTracker(bc, service.getName(), null);
	}
	
	@SuppressWarnings("unchecked")
	private ServiceReference getServiceReference(Class service, BundleContext bc) {
		return bc.getServiceReference(service.getName());
	}
	
}
