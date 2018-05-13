package com.cloudflare.access.atlassian.base.support;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PluginUpdateAvailability {

	@XmlElement
	private boolean newVersionAvailable;

	@XmlElement
	private String newVersion;

	public PluginUpdateAvailability() {
		this.newVersionAvailable = false;
	}

	public PluginUpdateAvailability(String newVersion) {
		this.newVersionAvailable = true;
		this.newVersion = newVersion;
	}

	public boolean hasNewVersionAvailable() {
		return this.newVersionAvailable;
	}

	public void setNewVersionAvailable(boolean newVersionAvailable) {
		this.newVersionAvailable = newVersionAvailable;
	}

	public String getNewVersion() {
		return newVersion;
	}

	public void setNewVersion(String newVersion) {
		this.newVersion = newVersion;
	}
}
