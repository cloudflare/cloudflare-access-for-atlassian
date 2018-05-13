package com.cloudflare.access.atlassian.common.exception;

public class ConfigurationException extends RuntimeException {

	private static final long serialVersionUID = 5700369651255772645L;

	public ConfigurationException(String msg) {
		super(msg);
	}

	public ConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
