package com.cloudflare.access.atlassian.jira.auth;

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
import javax.servlet.http.HttpServletResponse;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cloudflare.access.atlassian.common.context.AuthenticationContext;
import com.cloudflare.access.atlassian.common.context.EnvironmentAuthenticationContext;
import com.cloudflare.access.atlassian.jira.util.PluginUtils;

@Named("CloudflareAccessLogoutFilter")
public class CloudflareAccessLogoutFilter implements Filter{

	private static final Log log = Logger.getInstance(CloudflareAccessLogoutFilter.class);

	@ComponentImport
	private PluginAccessor pluginAcessor;

	private AuthenticationContext authContext;

	@Inject
	public CloudflareAccessLogoutFilter(PluginAccessor pluginAcessor) {
		this.pluginAcessor = Objects.requireNonNull(pluginAcessor, "PluginAccessor instance not injected by DI container");
		this.authContext = new EnvironmentAuthenticationContext();
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if(pluginAcessor.isPluginEnabled(PluginUtils.getPluginKey()) == false) {
			chain.doFilter(request, response);
			return;
		}

		log.debug("Redirecting user to cloudflare logout at " + authContext.getLogoutUrl());
		final HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.sendRedirect(authContext.getLogoutUrl());
	}


}
