package com.cloudflare.access.atlassian.jira.auth;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudflare.access.atlassian.base.auth.CloudflareAccessService;
import com.cloudflare.access.atlassian.common.config.PluginConfiguration;
import com.cloudflare.access.atlassian.jira.config.ConfigurationService;

@Named("CloudflareAccessAuthenticationFilter")
public class CloudflareAccessAuthenticationFilter implements Filter{

	private static final Logger log = LoggerFactory.getLogger(CloudflareAccessAuthenticationFilter.class);

	@Inject
	private CloudflareAccessService cloudflareAccess;
	@Inject
	private ConfigurationService configurationService;

	@Inject
	public CloudflareAccessAuthenticationFilter(CloudflareAccessService cloudflareAccess, ConfigurationService configurationService) {
		this.cloudflareAccess = cloudflareAccess;
		this.configurationService = configurationService;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		Optional<PluginConfiguration> pluginConfiguration = configurationService.getPluginConfiguration();
		if(pluginConfiguration.isPresent()) {
			cloudflareAccess.setAuthContext(pluginConfiguration.get().getAuthenticationContext());
			cloudflareAccess.processAuthRequest(httpRequest, httpResponse, chain);
		}else {
			chain.doFilter(httpRequest, httpResponse);
		}
	}

}
