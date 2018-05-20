package com.cloudflare.access.atlassian.common.config;

import com.cloudflare.access.atlassian.common.context.AuthenticationContext;
import com.cloudflare.access.atlassian.common.http.AtlassianInternalHttpProxyConfig;

public interface PluginConfiguration {

	AtlassianInternalHttpProxyConfig getInternalProxyConfig();

	AuthenticationContext getAuthenticationContext();

}
