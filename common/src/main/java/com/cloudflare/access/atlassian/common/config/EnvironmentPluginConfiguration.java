package com.cloudflare.access.atlassian.common.config;

import static com.cloudflare.access.atlassian.common.config.EnvUtils.getEnvValueOrDefault;

import com.cloudflare.access.atlassian.common.context.AuthenticationContext;
import com.cloudflare.access.atlassian.common.context.EnvironmentAuthenticationContext;
import com.cloudflare.access.atlassian.common.http.AtlassianInternalHttpProxyConfig;

public class EnvironmentPluginConfiguration implements PluginConfiguration{

	private static final String CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_ADDRESS = "CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_ADDRESS";
	private static final String CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_PORT = "CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_PORT";
	private static final String CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_HTTPS = "CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_HTTPS";

	@Override
	public AtlassianInternalHttpProxyConfig getInternalProxyConfig() {
		String host = getEnvValueOrDefault(CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_ADDRESS, "localhost");
		int port = Integer.parseInt(EnvUtils.getEnvValueOrThrow(CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_PORT));
		boolean useHttps = Boolean.parseBoolean(getEnvValueOrDefault(CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_HTTPS, "false"));
		return new AtlassianInternalHttpProxyConfig(host, port, useHttps);
	}

	@Override
	public AuthenticationContext getAuthenticationContext() {
		return new EnvironmentAuthenticationContext();
	}

}
