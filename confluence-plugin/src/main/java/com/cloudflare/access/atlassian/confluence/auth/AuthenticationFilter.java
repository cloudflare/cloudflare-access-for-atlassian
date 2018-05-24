package com.cloudflare.access.atlassian.confluence.auth;

import java.io.IOException;
import java.util.Objects;

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
import com.cloudflare.access.atlassian.common.config.EnvironmentPluginConfiguration;
import com.cloudflare.access.atlassian.common.http.AtlassianInternalHttpProxy;

@Named("CloudflareAccessAuthenticationFilter")
public class AuthenticationFilter implements Filter{

	private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

	private CloudflareAccessService cloudflareAccess;

	@Inject
	public AuthenticationFilter(CloudflareAccessService cloudflareAccess) {
		this.cloudflareAccess = cloudflareAccess;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Initializing internal proxy...");
		AtlassianInternalHttpProxy.INSTANCE.init(new EnvironmentPluginConfiguration().getInternalProxyConfig());
		log.debug("Filter initialized");
	}

	@Override
	public void destroy() {
		log.debug("Shutting down internal proxy...");
		AtlassianInternalHttpProxy.INSTANCE.shutdown();
		log.debug("Filter destroyed");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		Objects.requireNonNull(cloudflareAccess, "CloudflareAccessService instance not injected by DI container");
		cloudflareAccess.processAuthRequest(httpRequest, httpResponse, chain);
	}

}
