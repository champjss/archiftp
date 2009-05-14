package com.ingres.archiftp.logpage.internal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

public class HttpServiceWrapper {
	
	private ServiceTracker tracker;

	public HttpServiceWrapper(ServiceTracker tracker) {
		this.tracker = tracker;
	}

	public void registerServlet(String alias, HttpServlet servlet) {
		HttpService service = getHttpService();
		if (service != null) {
			try {
				service.registerServlet(alias, servlet, null, null);
			}
			catch (ServletException e) {
				throw new RuntimeException(String.format("Cannot register servlet %s : "
						+ "this servlet has registered with an other alias.", alias), e);
			}
			catch (NamespaceException e) {
				throw new RuntimeException(String.format("Cannot register servlet %s : "
						+ "the alias is already in use.", alias), e);
			}
			catch (IllegalArgumentException e) {
				throw new RuntimeException(String.format("Cannot register servlet %s : "
						+ "invalid argument.", alias), e);
			}
		}
		else {
			throw new RuntimeException(String.format("Cannot register servlet %s : "
					+ "HttpService (org.osgi.service.http.HttpService) not found.", alias));
		}
	}

	public void unregisterServlet(String alias) {
		HttpService service = getHttpService();
		if (service != null) {
			try {
				service.unregister(alias);
			}
			catch (IllegalArgumentException e) {
				throw new RuntimeException(String.format("Cannot unregister servlet %s : "
						+ "no registration for the alias, or the calling bundle was not " 
						+ "the bundle which registered the alias.", alias), e);
			}
		}
		else {
			throw new RuntimeException(String.format("Cannot register servlet %s : "
					+ "HttpService (org.osgi.service.http.HttpService) not found.", alias));
		}
	}

	private HttpService getHttpService() {
		try {
			return (HttpService) (this.tracker.waitForService(10000));
		}
		catch (InterruptedException e) {
			// another thread has interrupted the current thread.
			return null;
		}
	}
	
}
