package com.cloudflare.access.atlassian.common.config;

import static com.cloudflare.access.atlassian.common.config.EnvUtils.getEnvValueOrDefault;

import com.cloudflare.access.atlassian.common.context.AuthenticationContext;
import com.cloudflare.access.atlassian.common.context.EnvironmentAuthenticationContext;
import com.cloudflare.access.atlassian.common.http.AtlassianInternalHttpProxyConfig;

public class EnvironmentPluginConfiguration implements PluginConfiguration{

	private static final String CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_ADDRESS = "CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_ADDRESS";
	private static final String CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_PORT = "CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_PORT";

	@Override
	public AtlassianInternalHttpProxyConfig getInternalProxyConfig() {
		String host = getEnvValueOrDefault(CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_ADDRESS, "localhost");
		int port = Integer.valueOf(EnvUtils.getEnvValueOrThrow(CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_PORT));
		return new AtlassianInternalHttpProxyConfig(host, port);
	}

	@Override
	public AuthenticationContext getAuthenticationContext() {
		return new EnvironmentAuthenticationContext();
	}

}
