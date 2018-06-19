package com.cloudflare.access.atlassian.jira.auth;

import java.io.IOException;

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

import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.cloudflare.access.atlassian.base.auth.CloudflareAccessService;

@Named("CloudflareAccessLogoutFilter")
public class CloudflareAccessLogoutFilter implements Filter{

	private CloudflareAccessService cloudflareAccess;

	@Inject
	public CloudflareAccessLogoutFilter(CloudflareAccessService cloudflareAccess) {
		this.cloudflareAccess = cloudflareAccess;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		ComponentLocator.getComponent(RememberMeService.class).removeRememberMeCookie(httpRequest, httpResponse);

		cloudflareAccess.processLogoutRequest(httpRequest, httpResponse, chain);
	}


}
