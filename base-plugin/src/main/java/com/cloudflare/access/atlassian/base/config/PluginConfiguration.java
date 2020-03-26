package com.cloudflare.access.atlassian.base.config;

import java.util.Optional;

import com.cloudflare.access.atlassian.common.context.AuthenticationContext;

public interface PluginConfiguration {

	AuthenticationContext getAuthenticationContext();

	Optional<String> getAllowedEmailDomain();

	UserMatchingAttribute getUserMatchingAttribute();
}
