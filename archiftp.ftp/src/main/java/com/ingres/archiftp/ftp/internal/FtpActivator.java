package com.ingres.archiftp.ftp.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import com.ingres.archiftp.ftp.FtpService;
import com.ingres.archiftp.logger.Logger;

public final class FtpActivator implements BundleActivator {

	private String pid = "com.ingres.archiftp.ftp";
	
	public void start(BundleContext bc) throws Exception {
		// Create Logger
		Logger loggerFtpService = new Logger();
		Logger loggerManagedService = new Logger();
		
		// Create Properties
		FtpProperties properties = new FtpProperties();
		
		// Create services
		FtpService ftpService = new FtpServiceImpl(properties, loggerFtpService);
		ManagedService logpageManagedService = new FtpManagedService(
				properties, loggerManagedService);
		
		// Register services
		registerManagedService((FtpManagedService)logpageManagedService, bc);
		registerFtpService(ftpService, bc);
		
		// Create ServiceReference
		ServiceReference ftpServiceReference = getServiceReference(FtpService.class, bc);
		ServiceReference managedServiceReference = getServiceReference(ManagedService.class, bc);
		
		// Set ServiceReference to Logger
		loggerFtpService.setReference(ftpServiceReference);
		loggerManagedService.setReference(managedServiceReference);
	}

	public void stop(BundleContext bc) throws Exception {
	}
	
	@SuppressWarnings("unchecked")
	private void registerManagedService(FtpManagedService service, BundleContext bc) {
		Dictionary properties = new Properties();
		properties.put("service.pid", this.pid);
		bc.registerService(ManagedService.class.getName(), service, properties);
	}
	
	@SuppressWarnings("unchecked")
	private void registerFtpService(FtpService service, BundleContext bc) {
		Dictionary properties = getServiceProperties(bc);
		bc.registerService(FtpService.class.getName(), service, properties);
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
	private ServiceReference getServiceReference(Class service, BundleContext bc) {
		return bc.getServiceReference(service.getName());
	}
	
}
