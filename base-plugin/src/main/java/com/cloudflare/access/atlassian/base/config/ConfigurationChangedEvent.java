package com.cloudflare.access.atlassian.base.config;

import org.springframework.context.ApplicationEvent;

public class ConfigurationChangedEvent extends ApplicationEvent {
	private static final long serialVersionUID = 8756790793491374578L;

	public ConfigurationChangedEvent(Object source) {
		super(source);
	}
}
