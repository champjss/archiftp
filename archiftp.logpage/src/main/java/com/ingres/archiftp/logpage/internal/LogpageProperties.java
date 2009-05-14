package com.ingres.archiftp.logpage.internal;

public class LogpageProperties {

	private String alias;

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public boolean isInitialized() {
		if (this.alias != null) {
			return true;
		}
		
		return false;
	}

}
