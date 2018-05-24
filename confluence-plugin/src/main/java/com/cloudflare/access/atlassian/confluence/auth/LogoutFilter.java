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

import com.cloudflare.access.atlassian.base.auth.CloudflareAccessService;

@Named("CloudflareAccessLogoutFilter")
public class LogoutFilter implements Filter{

	private CloudflareAccessService cloudflareAccess;

	@Inject
	public LogoutFilter(CloudflareAccessService cloudflareAccess) {
		this.cloudflareAccess = Objects.requireNonNull(cloudflareAccess, "CloudflareAccessService instance not injected by DI container");
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

		cloudflareAccess.processLogoutRequest(httpRequest, httpResponse, chain);
	}


}
