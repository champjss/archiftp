package com.ingres.archiftp.logpage.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import com.ingres.archiftp.logger.Logger;
import com.ingres.archiftp.logpage.LogpageService;

public final class LogpageActivator implements BundleActivator {
	
	private ServiceTracker httpServiceTracker;
	private ServiceTracker logReaderServiceTracker;
	private String pid = "com.ingres.archiftp.logpage";
	
	public void start(BundleContext bc) throws Exception {
		// Create Logger
		Logger loggerLogpageService = new Logger();
		Logger loggerManagedService = new Logger();
		
		// Create ServiceTracker
		this.httpServiceTracker = getServiceTracker(HttpService.class, bc);
		this.httpServiceTracker.open();
		this.logReaderServiceTracker = getServiceTracker(LogReaderService.class, bc);
		this.logReaderServiceTracker.open();
		
		// Create other ServiceWrapper
		HttpServiceWrapper httpService = new HttpServiceWrapper(this.httpServiceTracker);
		LogReaderServiceWrapper logReaderService = 
			new LogReaderServiceWrapper(this.logReaderServiceTracker);
		
		LogpageProperties properties = new LogpageProperties();
		
		// Create services
		LogpageService logpageService = new LogpageServiceImpl(properties, loggerLogpageService, 
				httpService, logReaderService);
		ManagedService logpageManagedService = new LogpageManagedService(
				properties, loggerManagedService, (LogpageServiceImpl)logpageService);
		
		// Register services
		registerManagedService((LogpageManagedService)logpageManagedService, bc);
		registerLogpageService(logpageService, bc);
		
		// Create ServiceReference
		ServiceReference logpageServiceReference = getServiceReference(LogpageService.class, bc);
		ServiceReference managedServiceReference = getServiceReference(ManagedService.class, bc);
		
		// Set ServiceReference to Logger
		loggerLogpageService.setReference(logpageServiceReference);
		loggerManagedService.setReference(managedServiceReference);
	}

	public void stop(BundleContext bc) throws Exception {
		this.httpServiceTracker.close();
		this.logReaderServiceTracker.close();
	}
	
	@SuppressWarnings("unchecked")
	private void registerManagedService(LogpageManagedService service, BundleContext bc) {
		Dictionary properties = new Properties();
		properties.put("service.pid", this.pid);
		bc.registerService(ManagedService.class.getName(), service, properties);
	}
	
	@SuppressWarnings("unchecked")
	private void registerLogpageService(LogpageService service, BundleContext bc) {
		Dictionary properties = getServiceProperties(bc);
		bc.registerService(LogpageService.class.getName(), service, properties);
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
