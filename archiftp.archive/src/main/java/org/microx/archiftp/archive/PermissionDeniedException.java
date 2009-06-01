package org.microx.archiftp.archive;

public class PermissionDeniedException extends Exception {
	public PermissionDeniedException() {
		super();
	}
	
	public PermissionDeniedException(String message) {
		super(message);
	}
}
