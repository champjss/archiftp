package com.ingres.archiftp.archive.internal;

public class ArchiveProperties {

	private String archivePath;

	public String getArchivePath() {
		return archivePath;
	}

	public void setArchivePath(String archivePath) {
		this.archivePath = archivePath;
	}
	
	public boolean isInitialized() {
		if (this.archivePath != null) {
			return true;
		}
		
		return false;
	}
	
}
