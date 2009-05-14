package com.ingres.archiftp.archive.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import com.ingres.archiftp.archive.ArchiveService;

public final class ArchiveActivator implements BundleActivator {
	
	private ServiceTracker logServiceTracker;
	private String pid = "com.ingres.archiftp.archive";
	
	public void start(BundleContext bc) throws Exception {
		this.logServiceTracker = getServiceTracker(LogService.class, bc);
		this.logServiceTracker.open();
		
		// Create LogServiceWrapper
		ServiceReference archiveServiceReference = getServiceReference(ArchiveService.class, bc);
		ServiceReference managedServiceReference = getServiceReference(ManagedService.class, bc);
		LogServiceWrapper logArchiveService = new LogServiceWrapper(
				archiveServiceReference, this.logServiceTracker);
		LogServiceWrapper logManagedService = new LogServiceWrapper(
				managedServiceReference, this.logServiceTracker);
		
		ArchiveProperties properties = new ArchiveProperties();
		
		// Create services
		ArchiveService archiveService = new ArchiveServiceImpl(properties, logArchiveService);
		ManagedService logpageManagedService = new ArchiveManagedService(
				properties, logManagedService);
		
		// Register services
		registerManagedService((ArchiveManagedService)logpageManagedService, bc);
		registerArchiveService(archiveService, bc);
	}

	public void stop(BundleContext bc) throws Exception {
		this.logServiceTracker.close();
	}
	
	@SuppressWarnings("unchecked")
	private void registerManagedService(ArchiveManagedService service, BundleContext bc) {
		Dictionary properties = new Properties();
		properties.put("service.pid", this.pid);
		bc.registerService(ManagedService.class.getName(), service, properties);
	}
	
	@SuppressWarnings("unchecked")
	private void registerArchiveService(ArchiveService service, BundleContext bc) {
		Dictionary properties = getServiceProperties(bc);
		bc.registerService(ArchiveService.class.getName(), service, properties);
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
