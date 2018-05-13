package com.cloudflare.access.atlassian.common.config;

import com.cloudflare.access.atlassian.common.exception.ConfigurationException;

public abstract class EnvUtils {

	public static String getEnvValueOrThrow(String key) {
		String value = System.getenv(key);
		if(value == null || value.trim().isEmpty()) {
			throw new ConfigurationException(String.format("Environment variable '%s' not set", key));
		}
		return value;
	}

	public static String getEnvValueOrDefault(String key, String defautlValue) {
		String value = System.getenv(key);
		if(value == null || value.trim().isEmpty()) {
			return defautlValue;
		}
		return value;
	}
}
