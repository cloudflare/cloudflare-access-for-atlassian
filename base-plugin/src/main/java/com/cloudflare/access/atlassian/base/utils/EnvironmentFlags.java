package com.cloudflare.access.atlassian.base.utils;

import org.springframework.core.env.Environment;

public interface EnvironmentFlags {

	static String FILTERS_DISABLED = "cloudflareAccessPlugin.filters.disabled";


	public static boolean isFiltersDisabled(Environment env) {
		return "true".equalsIgnoreCase(env.getProperty(FILTERS_DISABLED, "false"));
	}
}
