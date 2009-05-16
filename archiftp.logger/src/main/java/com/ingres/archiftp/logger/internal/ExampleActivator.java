package com.ingres.archiftp.logger.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import com.ingres.archiftp.logger.Logger;

public final class ExampleActivator implements BundleActivator {
	
	private ServiceTracker logServiceTracker;
	
	public void start(BundleContext bc) throws Exception {
		this.logServiceTracker = getServiceTracker(LogService.class, bc);
		this.logServiceTracker.open();
		Logger.setTracker(this.logServiceTracker);
	}

	public void stop(BundleContext bc) throws Exception {
		this.logServiceTracker.close();
	}
	
	@SuppressWarnings("unchecked")
	private ServiceTracker getServiceTracker(Class service, BundleContext bc) {
		return new ServiceTracker(bc, service.getName(), null);
	}
}
